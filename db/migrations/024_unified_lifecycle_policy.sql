-- One lifecycle policy for legacy events and registered progress. Existing direct
-- In-Progress completion remains supported; UnderReview completion is now supported too.
CREATE FUNCTION crm_lifecycle_transition_allowed(source text,target text)
RETURNS boolean LANGUAGE sql IMMUTABLE SET search_path=pg_catalog AS $$
 SELECT coalesce((source,target) IN (('Registered','In-Progress'),('In-Progress','UnderReview'),('UnderReview','In-Progress'),('UnderReview','Completed'),('In-Progress','Completed')),false)
$$;
REVOKE ALL ON FUNCTION crm_lifecycle_transition_allowed(text,text) FROM PUBLIC;
GRANT EXECUTE ON FUNCTION crm_lifecycle_transition_allowed(text,text) TO crm_app;

CREATE OR REPLACE FUNCTION crm_research_event(study integer,event text,occurred date,expected integer)
RETURNS SETOF public.research_records LANGUAGE plpgsql SECURITY DEFINER SET search_path=pg_catalog,public AS $$
DECLARE current public.research_records;
BEGIN
 IF NOT public.crm_assigned(study) THEN RETURN; END IF;
 SELECT * INTO current FROM public.research_records WHERE id=study FOR UPDATE;
 IF NOT FOUND OR NOT public.crm_assigned(study) THEN RETURN; END IF;
 IF current.lifecycle_version<>expected THEN RAISE EXCEPTION 'Version conflict' USING ERRCODE='40001'; END IF;
 IF occurred IS NULL OR NOT isfinite(occurred) OR occurred>(clock_timestamp() AT TIME ZONE 'Asia/Manila')::date THEN RAISE EXCEPTION 'Invalid event date' USING ERRCODE='22023'; END IF;
 IF event='Presented' AND current.presented_at IS NULL THEN current.presented_at:=occurred;
 ELSIF event='Published' AND current.published_at IS NULL THEN current.published_at:=occurred;
 ELSIF public.crm_lifecycle_transition_allowed(current.lifecycle_state,event) THEN
  current.lifecycle_state:=event;
  IF event='Completed' THEN current.completed_at:=occurred; END IF;
 ELSE RAISE EXCEPTION 'Invalid transition or duplicate outcome' USING ERRCODE='40001';
 END IF;
 RETURN QUERY UPDATE public.research_records SET lifecycle_state=current.lifecycle_state,
  completed_at=current.completed_at,presented_at=current.presented_at,published_at=current.published_at,
  lifecycle_version=lifecycle_version+1 WHERE id=study RETURNING *;
END $$;

CREATE OR REPLACE FUNCTION crm_save_registered_progress_base(study integer,expected integer,kind text,input jsonb)
RETURNS text LANGUAGE plpgsql SECURITY DEFINER SET search_path=pg_catalog,public AS $$
DECLARE
  current public.research_records;
  progress public.crm_registered_progress;
  prior_milestone public.crm_registered_milestones;
  before_state jsonb;
  after_state jsonb;
  next_stage text;
  effective date;
  completion_date date;
  target_milestone text;
BEGIN
  IF NOT public.crm_manages_progress() THEN RETURN 'denied'; END IF;
  SELECT * INTO current FROM public.research_records r WHERE r.id=study AND r.is_registered FOR UPDATE;
  IF NOT FOUND THEN RETURN 'missing'; END IF;
  IF current.lifecycle_version<>expected THEN RETURN 'conflict'; END IF;
  IF kind='stage' THEN
    next_stage:=input->>'stage';
    IF NOT public.crm_lifecycle_transition_allowed(current.lifecycle_state,next_stage)
       THEN RETURN 'transition'; END IF;
    IF nullif(input->>'effective_date','') IS NULL THEN RETURN 'effective_date'; END IF;
    IF coalesce(input->>'supporting_details','')='' AND coalesce(input->>'reference','')='' THEN RETURN 'support'; END IF;
    IF (coalesce(input->>'next_action','')='')<>(coalesce((input->>'no_next_action')::boolean,false))
       THEN RETURN 'action'; END IF;
    IF next_stage='Completed' AND nullif(input->>'completion_date','') IS NULL THEN RETURN 'completion_date'; END IF;
  ELSIF kind='completion_correction' THEN
    IF current.lifecycle_state<>'Completed' THEN RETURN 'transition'; END IF;
    IF coalesce(input->>'reason','')='' THEN RETURN 'reason'; END IF;
    IF NOT coalesce((input->>'reopen')::boolean,false) AND nullif(input->>'completion_date','') IS NULL
       THEN RETURN 'completion_date'; END IF;
  ELSIF kind='presentation' THEN
    IF nullif(input->>'effective_date','') IS NULL OR coalesce(input->>'event','')='' THEN RETURN 'presentation'; END IF;
  ELSIF kind='publication' THEN
    IF nullif(input->>'effective_date','') IS NULL OR coalesce(input->>'citation','')='' THEN RETURN 'publication'; END IF;
  ELSIF kind='milestone' THEN
    target_milestone:=input->>'milestone_code';
    IF NOT EXISTS(SELECT 1 FROM public.crm_milestone_definitions WHERE code=target_milestone AND active)
       THEN RETURN 'milestone'; END IF;
  ELSE
    RETURN 'invalid';
  END IF;

  IF kind='presentation' AND current.presented_at IS NOT NULL AND coalesce(input->>'reason','')='' THEN RETURN 'reason'; END IF;
  IF kind='publication' AND current.published_at IS NOT NULL AND coalesce(input->>'reason','')='' THEN RETURN 'reason'; END IF;
  IF EXISTS (SELECT 1 FROM unnest(ARRAY[nullif(input->>'effective_date','')::date,
      nullif(input->>'completion_date','')::date]) d
      WHERE NOT isfinite(d) OR d>(clock_timestamp() AT TIME ZONE 'Asia/Manila')::date) THEN RETURN 'effective_date'; END IF;

  INSERT INTO public.crm_registered_progress(study_id) VALUES(study) ON CONFLICT DO NOTHING;
  SELECT * INTO progress FROM public.crm_registered_progress WHERE study_id=study;
  effective:=nullif(input->>'effective_date','')::date;
  completion_date:=nullif(input->>'completion_date','')::date;
  IF kind IN ('stage','completion_correction') THEN
    before_state:=jsonb_build_object('stage',current.lifecycle_state,'completion_date',current.completed_at);
    IF kind='completion_correction' THEN
      next_stage:=CASE WHEN (input->>'reopen')::boolean THEN 'In-Progress' ELSE 'Completed' END;
    END IF;
    UPDATE public.research_records SET lifecycle_state=next_stage,
      completed_at=CASE WHEN next_stage='Completed' THEN completion_date ELSE NULL END,
      lifecycle_version=lifecycle_version+1 WHERE id=study;
    IF kind='stage' THEN
      UPDATE public.crm_registered_progress SET next_action=coalesce(input->>'next_action',''),
        responsible_contact=coalesce(input->>'responsible_contact',progress.responsible_contact),
        deadline=nullif(input->>'deadline','')::date,
        completion_details=CASE WHEN next_stage='Completed' THEN coalesce(input->>'completion_details','') ELSE '' END
        WHERE study_id=study;
    ELSE
      UPDATE public.crm_registered_progress SET completion_details=CASE WHEN next_stage='Completed'
        THEN coalesce(input->>'completion_details','') ELSE '' END WHERE study_id=study;
    END IF;
    after_state:=jsonb_build_object('stage',next_stage,'completion_date',
      CASE WHEN next_stage='Completed' THEN completion_date ELSE NULL END);
    IF kind='completion_correction' THEN effective:=completion_date; END IF;
  ELSIF kind='presentation' THEN
    before_state:=jsonb_build_object('date',current.presented_at,'event',progress.presentation_event);
    UPDATE public.research_records SET presented_at=effective,lifecycle_version=lifecycle_version+1 WHERE id=study;
    UPDATE public.crm_registered_progress SET presentation_event=input->>'event' WHERE study_id=study;
    after_state:=jsonb_build_object('date',effective,'event',input->>'event');
  ELSIF kind='publication' THEN
    before_state:=jsonb_build_object('date',current.published_at,'citation',progress.publication_citation);
    UPDATE public.research_records SET published_at=effective,lifecycle_version=lifecycle_version+1 WHERE id=study;
    UPDATE public.crm_registered_progress SET publication_citation=input->>'citation' WHERE study_id=study;
    after_state:=jsonb_build_object('date',effective,'citation',input->>'citation');
  ELSE
    SELECT * INTO prior_milestone FROM public.crm_registered_milestones
      WHERE study_id=study AND milestone_code=target_milestone;
    before_state:=jsonb_build_object('code',target_milestone,'status',coalesce(prior_milestone.status,'Pending'),
      'effective_date',prior_milestone.effective_date);
    INSERT INTO public.crm_registered_milestones(study_id,milestone_code,status,effective_date,evidence)
      VALUES(study,target_milestone,input->>'status',effective,coalesce(input->>'evidence',''))
      ON CONFLICT(study_id,milestone_code) DO UPDATE SET status=excluded.status,
        effective_date=excluded.effective_date,evidence=excluded.evidence;
    UPDATE public.research_records SET lifecycle_version=lifecycle_version+1 WHERE id=study;
    after_state:=jsonb_build_object('code',target_milestone,'status',input->>'status','effective_date',effective);
  END IF;
  INSERT INTO public.crm_registered_progress_events(study_id,actor_id,kind,before_value,after_value,effective_date,
    supporting_details,reference,reason,internal_note)
    VALUES(study,public.crm_user_id(),kind,before_state,after_state,effective,
      coalesce(input->>'supporting_details',input->>'evidence',''),coalesce(input->>'reference',''),
      coalesce(input->>'reason',''),coalesce(input->>'internal_note',''));
  RETURN 'saved';
END $$;


REVOKE ALL ON FUNCTION crm_save_registered_progress_base(integer,integer,text,jsonb) FROM PUBLIC,crm_app;
