CREATE TABLE crm_registered_progress (
  study_id integer PRIMARY KEY REFERENCES crm_research_registrations(study_id) ON DELETE CASCADE,
  next_action text NOT NULL DEFAULT '',
  responsible_contact text NOT NULL DEFAULT '',
  deadline date,
  completion_details text NOT NULL DEFAULT '',
  presentation_event text NOT NULL DEFAULT '',
  publication_citation text NOT NULL DEFAULT ''
);

CREATE TABLE crm_milestone_definitions (
  code text PRIMARY KEY,
  title text NOT NULL,
  display_order integer NOT NULL,
  active boolean NOT NULL DEFAULT true
);

INSERT INTO crm_milestone_definitions(code,title,display_order) VALUES
 ('proposal','Proposal submission and defense',1),
 ('ethics','Ethics and consent clearance',2),
 ('collection','Data collection and analysis',3),
 ('manuscript','Manuscript drafting and peer review',4),
 ('output','Final output and registration',5);

CREATE TABLE crm_registered_milestones (
  study_id integer REFERENCES crm_research_registrations(study_id) ON DELETE CASCADE,
  milestone_code text REFERENCES crm_milestone_definitions(code),
  status text NOT NULL CHECK(status IN ('Pending','In progress','Completed','Not applicable')),
  effective_date date,
  evidence text NOT NULL DEFAULT '',
  PRIMARY KEY(study_id,milestone_code)
);

CREATE TABLE crm_registered_progress_events (
  id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  study_id integer NOT NULL REFERENCES crm_research_registrations(study_id) ON DELETE CASCADE,
  actor_id integer REFERENCES users ON DELETE SET NULL,
  kind text NOT NULL CHECK(kind IN ('stage','milestone','presentation','publication','completion_correction')),
  before_value jsonb NOT NULL,
  after_value jsonb NOT NULL,
  effective_date date,
  supporting_details text NOT NULL DEFAULT '',
  reference text NOT NULL DEFAULT '',
  reason text NOT NULL DEFAULT '',
  internal_note text NOT NULL DEFAULT '',
  recorded_at timestamptz NOT NULL DEFAULT clock_timestamp()
);
CREATE INDEX crm_progress_history_by_study ON crm_registered_progress_events(study_id,id DESC);
REVOKE ALL ON crm_registered_progress,crm_milestone_definitions,crm_registered_milestones,
  crm_registered_progress_events FROM PUBLIC,crm_app;

CREATE FUNCTION crm_manages_progress() RETURNS boolean LANGUAGE sql STABLE SECURITY DEFINER
SET search_path=pg_catalog,public AS $$
  SELECT EXISTS(SELECT 1 FROM public.users u JOIN public.crm_staff_permissions p ON p.user_id=u.id
    WHERE u.id=public.crm_user_id() AND u.role='staff' AND u.active AND p.permission='progress_updates')
$$;

CREATE FUNCTION crm_progress_studies()
RETURNS TABLE(id integer,title text,stage text,version integer)
LANGUAGE sql STABLE SECURITY DEFINER SET search_path=pg_catalog,public AS $$
  SELECT r.id,r.title,r.lifecycle_state,r.lifecycle_version
    FROM public.research_records r WHERE r.is_registered AND public.crm_manages_progress()
    ORDER BY r.id DESC
$$;

CREATE FUNCTION crm_registered_progress_view(study integer,staff_view boolean)
RETURNS jsonb LANGUAGE sql STABLE SECURITY DEFINER SET search_path=pg_catalog,public AS $$
  SELECT jsonb_build_object('id',r.id,'title',r.title,'stage',r.lifecycle_state,'version',r.lifecycle_version,
    'registered_at',g.registered_at,
    'next_action',coalesce(p.next_action,''),'responsible_contact',coalesce(nullif(p.responsible_contact,''),'Center for Research Management'),
    'deadline',p.deadline,
    'completion',jsonb_build_object('date',r.completed_at,'details',coalesce(p.completion_details,'')),
    'presentation',jsonb_build_object('date',r.presented_at,'event',coalesce(p.presentation_event,'')),
    'publication',jsonb_build_object('date',r.published_at,'citation',coalesce(p.publication_citation,'')),
    'milestones',coalesce((
      SELECT jsonb_agg(jsonb_build_object('code',d.code,'title',d.title,'status',coalesce(m.status,'Pending'),
        'effective_date',m.effective_date) ||
        CASE WHEN staff_view THEN jsonb_build_object('evidence',coalesce(m.evidence,'')) ELSE '{}'::jsonb END
        ORDER BY d.display_order,d.code)
      FROM public.crm_milestone_definitions d
      LEFT JOIN public.crm_registered_milestones m ON m.milestone_code=d.code AND m.study_id=r.id
      WHERE d.active OR m.study_id IS NOT NULL
    ),'[]'::jsonb),
    'history',coalesce((
      SELECT jsonb_agg(jsonb_build_object('kind',e.kind,'before',e.before_value,'after',e.after_value,
        'effective_date',e.effective_date,'recorded_at',e.recorded_at)
        || CASE WHEN staff_view THEN jsonb_build_object('actor_id',e.actor_id,
          'supporting_details',e.supporting_details,'reference',e.reference,'reason',e.reason,
          'internal_note',e.internal_note) ELSE '{}'::jsonb END ORDER BY e.id DESC)
      FROM public.crm_registered_progress_events e WHERE e.study_id=r.id
    ),'[]'::jsonb))
  FROM public.research_records r JOIN public.crm_research_registrations g ON g.study_id=r.id
  LEFT JOIN public.crm_registered_progress p ON p.study_id=r.id
  WHERE r.id=study AND (CASE WHEN staff_view THEN public.crm_manages_progress()
                           ELSE public.crm_is_researcher() AND public.crm_member(study) END)
$$;

CREATE FUNCTION crm_save_registered_progress(study integer,expected integer,kind text,input jsonb)
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
    IF NOT coalesce((current.lifecycle_state,next_stage) IN (('Registered','In-Progress'),
       ('In-Progress','UnderReview'),('UnderReview','In-Progress'),('In-Progress','Completed')),false)
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

REVOKE ALL ON FUNCTION crm_manages_progress(),crm_progress_studies(),crm_registered_progress_view(integer,boolean),
  crm_save_registered_progress(integer,integer,text,jsonb) FROM PUBLIC;
GRANT EXECUTE ON FUNCTION crm_manages_progress(),crm_progress_studies(),crm_registered_progress_view(integer,boolean),
  crm_save_registered_progress(integer,integer,text,jsonb) TO crm_app;
