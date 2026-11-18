ALTER TABLE crm_registered_milestones ADD COLUMN evidence_state text
  CHECK(evidence_state IN ('received','verified'));

CREATE TABLE crm_research_workflows (
  study_id integer PRIMARY KEY REFERENCES crm_research_registrations(study_id) ON DELETE CASCADE,
  definition jsonb NOT NULL,
  configured_by integer REFERENCES users ON DELETE SET NULL,
  configured_at timestamptz NOT NULL DEFAULT clock_timestamp()
);
REVOKE ALL ON crm_research_workflows FROM PUBLIC,crm_app;

CREATE FUNCTION crm_research_workflow_snapshot(study integer,staff_view boolean)
RETURNS jsonb LANGUAGE sql STABLE SECURITY DEFINER SET search_path=pg_catalog,public AS $$
  SELECT jsonb_build_object('version',r.lifecycle_version,'definition',w.definition,
    'milestones',coalesce((SELECT jsonb_agg(jsonb_build_object('code',d.code,'title',d.title,
      'status',coalesce(m.status,'Pending'),'evidence_state',m.evidence_state)
      ORDER BY d.display_order,d.code)
      FROM public.crm_milestone_definitions d
      LEFT JOIN public.crm_registered_milestones m ON m.study_id=r.id AND m.milestone_code=d.code
      WHERE d.active OR m.study_id IS NOT NULL),'[]'::jsonb))
    FROM public.research_records r
    JOIN public.crm_research_registrations g ON g.study_id=r.id
    LEFT JOIN public.crm_research_workflows w ON w.study_id=r.id
    WHERE r.id=study AND CASE WHEN staff_view THEN public.crm_manages_progress()
      ELSE public.crm_is_researcher() AND public.crm_member(study) END
$$;

CREATE FUNCTION crm_save_research_workflow(study integer,expected integer,definition jsonb)
RETURNS text LANGUAGE plpgsql SECURITY DEFINER SET search_path=pg_catalog,public AS $$
DECLARE current_version integer;
BEGIN
  IF NOT public.crm_manages_progress() THEN RETURN 'denied'; END IF;
  SELECT lifecycle_version INTO current_version FROM public.research_records WHERE id=study AND is_registered FOR UPDATE;
  IF NOT FOUND THEN RETURN 'missing'; END IF;
  IF current_version<>expected THEN RETURN 'conflict'; END IF;
  INSERT INTO public.crm_research_workflows(study_id,definition,configured_by)
    VALUES(study,definition,public.crm_user_id())
    ON CONFLICT(study_id) DO UPDATE SET definition=excluded.definition,
      configured_by=excluded.configured_by,configured_at=clock_timestamp();
  UPDATE public.research_records SET lifecycle_version=lifecycle_version+1 WHERE id=study;
  INSERT INTO public.audit_logs(actor_id,action) VALUES(public.crm_user_id(),'research.workflow_configured');
  RETURN 'saved';
END $$;

-- Preserve the validated lifecycle mutation and add evidence to the same locked transaction.
ALTER FUNCTION crm_save_registered_progress(integer,integer,text,jsonb) RENAME TO crm_save_registered_progress_base;
REVOKE EXECUTE ON FUNCTION crm_save_registered_progress_base(integer,integer,text,jsonb) FROM crm_app;
CREATE FUNCTION crm_save_registered_progress(study integer,expected integer,kind text,input jsonb)
RETURNS text LANGUAGE plpgsql SECURITY DEFINER SET search_path=pg_catalog,public AS $$
DECLARE result text;
BEGIN
  IF kind='milestone' AND nullif(input->>'evidence_state','') IS NOT NULL
    AND coalesce(input->>'evidence','')='' THEN RETURN 'evidence'; END IF;
  result:=public.crm_save_registered_progress_base(study,expected,kind,input);
  IF result='saved' AND kind='milestone' THEN
    UPDATE public.crm_registered_milestones SET evidence_state=nullif(input->>'evidence_state','')
      WHERE study_id=study AND milestone_code=input->>'milestone_code';
    UPDATE public.crm_registered_progress_events SET after_value=after_value ||
      jsonb_build_object('evidence_state',nullif(input->>'evidence_state',''))
      WHERE id=(SELECT max(id) FROM public.crm_registered_progress_events WHERE study_id=study);
  END IF;
  RETURN result;
END $$;
REVOKE ALL ON FUNCTION crm_research_workflow_snapshot(integer,boolean),
  crm_save_research_workflow(integer,integer,jsonb),
  crm_save_registered_progress(integer,integer,text,jsonb) FROM PUBLIC;
GRANT EXECUTE ON FUNCTION crm_research_workflow_snapshot(integer,boolean),
  crm_save_research_workflow(integer,integer,jsonb),
  crm_save_registered_progress(integer,integer,text,jsonb) TO crm_app;

ALTER FUNCTION crm_registered_progress_view(integer,boolean) RENAME TO crm_registered_progress_view_base;
REVOKE EXECUTE ON FUNCTION crm_registered_progress_view_base(integer,boolean) FROM crm_app;
CREATE FUNCTION crm_registered_progress_view(study integer,staff_view boolean)
RETURNS jsonb LANGUAGE sql STABLE SECURITY DEFINER SET search_path=pg_catalog,public AS $$
  SELECT CASE WHEN record IS NULL THEN NULL ELSE jsonb_set(record,'{milestones}',
    coalesce((SELECT jsonb_agg(item || jsonb_build_object('evidence_state',m.evidence_state))
      FROM jsonb_array_elements(record->'milestones') item
      LEFT JOIN public.crm_registered_milestones m ON m.study_id=study AND m.milestone_code=item->>'code'),
      '[]'::jsonb)) END
  FROM (SELECT public.crm_registered_progress_view_base(study,staff_view) AS record) source
$$;
REVOKE ALL ON FUNCTION crm_registered_progress_view(integer,boolean) FROM PUBLIC;
GRANT EXECUTE ON FUNCTION crm_registered_progress_view(integer,boolean) TO crm_app;
