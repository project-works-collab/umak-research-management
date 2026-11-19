ALTER TABLE crm_research_registrations ADD COLUMN historical boolean NOT NULL DEFAULT false,
  ADD COLUMN source_reference text, ADD COLUMN original_registration_date date;
CREATE TABLE crm_registry_corrections (
  id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  study_id integer NOT NULL REFERENCES crm_research_registrations(study_id) ON DELETE CASCADE,
  actor_id integer REFERENCES users ON DELETE SET NULL,
  reason text NOT NULL,
  before_value jsonb NOT NULL,
  after_value jsonb NOT NULL,
  recorded_at timestamptz NOT NULL DEFAULT clock_timestamp()
);
CREATE TABLE crm_registry_files (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  study_id integer NOT NULL REFERENCES crm_research_registrations(study_id) ON DELETE CASCADE,
  previous_id uuid UNIQUE REFERENCES crm_registry_files(id),
  series_id uuid NOT NULL,
  version integer NOT NULL CHECK(version>0),
  upload_token uuid NOT NULL,
  filename text NOT NULL,
  mime text NOT NULL,
  bytes bytea NOT NULL CHECK(octet_length(bytes) BETWEEN 1 AND 10000000),
  sha256 text NOT NULL,
  actor_id integer REFERENCES users ON DELETE SET NULL,
  created_at timestamptz NOT NULL DEFAULT clock_timestamp(),
  UNIQUE(study_id,upload_token),
  UNIQUE(series_id,version)
);
REVOKE ALL ON crm_registry_corrections,crm_registry_files FROM PUBLIC,crm_app;

CREATE FUNCTION crm_manages_registry() RETURNS boolean
LANGUAGE sql STABLE SECURITY DEFINER SET search_path=pg_catalog,public AS $$
  SELECT EXISTS(SELECT 1 FROM public.users u JOIN public.crm_staff_permissions p ON p.user_id=u.id
    WHERE u.id=public.crm_user_id() AND u.active AND u.role='staff' AND p.permission='registry_management')
$$;
CREATE FUNCTION crm_registry_record(study integer) RETURNS jsonb
LANGUAGE sql STABLE SECURITY DEFINER SET search_path=pg_catalog,public AS $$
  SELECT jsonb_build_object('id',r.id,'research_id','REG-'||r.id,'title',r.title,'college',r.department,
    'abstract',r.abstract,'stage',r.lifecycle_state,'version',r.lifecycle_version,'registrant_id',g.registrant_id,
    'registrant_category',g.registrant_category,'academic_period',g.academic_period,'adviser',g.adviser,
    'historical',g.historical,'source_reference',g.source_reference,'original_registration_date',g.original_registration_date,
    'encoded_at',g.registered_at,
    'members',coalesce((SELECT jsonb_agg(jsonb_build_object('id',u.id,'name',coalesce(u.display_name,u.email),'email',u.email,'role',u.role) ORDER BY u.id)
      FROM public.study_members m JOIN public.users u ON u.id=m.user_id WHERE m.study_id=r.id),'[]'::jsonb),
    'linked_requests',coalesce((SELECT jsonb_agg(jsonb_build_object('id',q.id,'service',v.details->>'name','status',q.status))
      FROM public.crm_service_requests q JOIN public.crm_service_revisions v ON v.id=q.revision_id WHERE q.study_id=r.id),'[]'::jsonb),
    'history',coalesce((SELECT jsonb_agg(to_jsonb(c) ORDER BY c.id DESC) FROM public.crm_registry_corrections c WHERE c.study_id=r.id),'[]'::jsonb))
  FROM public.research_records r JOIN public.crm_research_registrations g ON g.study_id=r.id
    WHERE r.id=study AND public.crm_manages_registry()
$$;
CREATE FUNCTION crm_registry_candidate(address text) RETURNS jsonb
LANGUAGE sql STABLE SECURITY DEFINER SET search_path=pg_catalog,public AS $$
  SELECT jsonb_build_object('id',u.id,'name',coalesce(u.display_name,u.email),'email',u.email,'role',u.role)
  FROM public.users u WHERE u.email=address AND u.role IN ('student','faculty') AND public.crm_manages_registry()
$$;
CREATE FUNCTION crm_registry_save(study integer,expected integer,input jsonb)
RETURNS jsonb LANGUAGE plpgsql SECURITY DEFINER SET search_path=pg_catalog,public AS $$
DECLARE
 current public.research_records; existing public.crm_research_registrations; owner public.users;
 people integer[]; candidate integer; category text; prior jsonb; after_state jsonb;
 normalized text; v_period text; v_college text; v_adviser text; year integer;
BEGIN
  IF NOT public.crm_manages_registry() THEN RETURN jsonb_build_object('error','denied'); END IF;
  PERFORM pg_advisory_xact_lock(7730,1);
  SELECT array_agg(value::integer ORDER BY value::integer) INTO people FROM jsonb_array_elements_text(input->'member_ids') value;
  IF cardinality(people) IS NULL OR cardinality(people)<>(SELECT count(DISTINCT x) FROM unnest(people) x)
    THEN RETURN jsonb_build_object('error','members'); END IF;
  IF (SELECT count(*) FROM public.users WHERE id=ANY(people) AND role IN ('student','faculty'))<>cardinality(people)
    THEN RETURN jsonb_build_object('error','account'); END IF;
  SELECT * INTO owner FROM public.users WHERE id=(input->>'registrant_id')::integer AND role IN ('student','faculty');
  IF NOT FOUND OR NOT(owner.id=ANY(people)) THEN RETURN jsonb_build_object('error','registrant'); END IF;
  IF study IS NOT NULL THEN
    SELECT * INTO current FROM public.research_records WHERE id=study AND is_registered FOR UPDATE;
    IF NOT FOUND THEN RETURN jsonb_build_object('error','missing'); END IF;
    IF current.lifecycle_version<>expected THEN RETURN jsonb_build_object('error','conflict'); END IF;
    SELECT * INTO existing FROM public.crm_research_registrations WHERE study_id=study;
    IF existing.registrant_id<>owner.id THEN RETURN jsonb_build_object('error','registrant'); END IF;
    IF coalesce(input->>'reason','')='' THEN RETURN jsonb_build_object('error','reason'); END IF;
    category:=existing.registrant_category;
    prior:=public.crm_registry_record(study)-'history'-'linked_requests';
  ELSE
    category:=owner.role;
    prior:='{}'::jsonb;
    IF EXISTS(SELECT 1 FROM public.crm_research_registrations WHERE submission_token=(input->>'submission_token')::uuid AND registrant_id=owner.id)
      THEN RETURN jsonb_build_object('error','token'); END IF;
  END IF;
  normalized:=public.crm_normalize_title(input->>'title');
  SELECT r.id INTO candidate FROM public.research_records r
    WHERE r.is_registered AND public.crm_normalize_title(r.title)=normalized AND (study IS NULL OR r.id<>study)
      AND (SELECT array_agg(m.user_id ORDER BY m.user_id) FROM public.study_members m WHERE m.study_id=r.id)=people
    ORDER BY r.id LIMIT 1;
  IF candidate IS NOT NULL THEN
    IF NOT coalesce((input->>'duplicate_confirmed')::boolean,false)
      THEN RETURN jsonb_build_object('error','duplicate','matching_id',candidate,
        'matching_title',(SELECT title FROM public.research_records WHERE id=candidate)); END IF;
    IF coalesce(input->>'duplicate_reason','')='' THEN RETURN jsonb_build_object('error','duplicate_reason'); END IF;
  END IF;
  v_period:=coalesce(input->>'academic_period','');
  v_adviser:=coalesce(input->>'adviser','');
  v_college:=nullif(input->>'college','');
  IF NOT coalesce((input->>'historical')::boolean,false) THEN
    IF v_period='' OR v_college IS NULL THEN RETURN jsonb_build_object('error','required'); END IF;
    IF category='student' AND v_adviser='' THEN RETURN jsonb_build_object('error','adviser'); END IF;
  ELSE
    IF coalesce(input->>'source_reference','')='' THEN RETURN jsonb_build_object('error','source'); END IF;
  END IF;
  year:=CASE WHEN v_period='' THEN NULL ELSE substring(v_period FROM 4 FOR 4)::integer END;
  IF study IS NULL THEN
    INSERT INTO public.research_records(author_id,title,abstract,status,research_year,department,is_registered)
      VALUES(owner.id,input->>'title',coalesce(input->>'abstract',''),'Draft',year,v_college,true) RETURNING id INTO study;
    INSERT INTO public.crm_research_registrations(study_id,registrant_id,registrant_category,
      submission_token,fingerprint,academic_period,adviser,registration_details,historical,source_reference,original_registration_date)
      VALUES(study,owner.id,category,(input->>'submission_token')::uuid,'staff_entry',v_period,v_adviser,'{}',
        coalesce((input->>'historical')::boolean,false),nullif(input->>'source_reference',''),
        nullif(input->>'original_registration_date','')::date);
  ELSE
    UPDATE public.research_records SET title=input->>'title',abstract=coalesce(input->>'abstract',''),
      department=v_college,research_year=year,lifecycle_version=lifecycle_version+1 WHERE id=study;
    UPDATE public.crm_research_registrations SET academic_period=v_period,adviser=v_adviser,
      historical=coalesce((input->>'historical')::boolean,false),source_reference=nullif(input->>'source_reference',''),
      original_registration_date=nullif(input->>'original_registration_date','')::date WHERE study_id=study;
  END IF;
  DELETE FROM public.study_members WHERE study_id=study;
  INSERT INTO public.study_members SELECT study,unnest(people);
  after_state:=public.crm_registry_record(study)-'history'-'linked_requests';
  INSERT INTO public.crm_registry_corrections(study_id,actor_id,reason,before_value,after_value)
    VALUES(study,public.crm_user_id(),coalesce(input->>'reason','Staff research entry') ||
      CASE WHEN candidate IS NOT NULL THEN ' · Duplicate override: '||(input->>'duplicate_reason') ELSE '' END,prior,after_state);
  RETURN jsonb_build_object('record',public.crm_registry_record(study));
END $$;

CREATE FUNCTION crm_registry_file_list(study integer)
RETURNS TABLE(id uuid,filename text,mime text,series_id uuid,version integer,previous_id uuid,created_at timestamptz)
LANGUAGE sql STABLE SECURITY DEFINER SET search_path=pg_catalog,public AS $$
 SELECT f.id,f.filename,f.mime,f.series_id,f.version,f.previous_id,f.created_at
 FROM public.crm_registry_files f WHERE f.study_id=study
   AND (public.crm_manages_registry() OR (public.crm_is_researcher() AND public.crm_member(study)))
 ORDER BY f.series_id,f.version DESC
$$;
CREATE FUNCTION crm_registry_file_get(study integer,file uuid)
RETURNS TABLE(filename text,mime text,bytes bytea)
LANGUAGE sql STABLE SECURITY DEFINER SET search_path=pg_catalog,public AS $$
 SELECT f.filename,f.mime,f.bytes FROM public.crm_registry_files f WHERE f.study_id=study AND f.id=file
   AND (public.crm_manages_registry() OR (public.crm_is_researcher() AND public.crm_member(study)))
$$;
CREATE FUNCTION crm_registry_file_save(study integer,previous uuid,token uuid,name text,media text,content bytea,digest text)
RETURNS text LANGUAGE plpgsql SECURITY DEFINER SET search_path=pg_catalog,public AS $$
DECLARE prior public.crm_registry_files; series uuid; revision integer; saved public.crm_registry_files;
BEGIN
  IF NOT public.crm_manages_registry() THEN RETURN 'denied'; END IF;
  PERFORM 1 FROM public.research_records WHERE id=study AND is_registered FOR UPDATE;
  IF NOT FOUND THEN RETURN 'missing'; END IF;
  SELECT * INTO saved FROM public.crm_registry_files WHERE study_id=study AND upload_token=token;
  IF FOUND THEN
    IF saved.previous_id IS NOT DISTINCT FROM previous AND saved.filename=name AND saved.sha256=digest THEN RETURN 'saved'; END IF;
    RETURN 'conflict';
  END IF;
  IF previous IS NULL THEN series:=gen_random_uuid(); revision:=1;
  ELSE
    SELECT * INTO prior FROM public.crm_registry_files WHERE id=previous AND study_id=study;
    IF NOT FOUND OR EXISTS(SELECT 1 FROM public.crm_registry_files WHERE previous_id=previous) THEN RETURN 'conflict'; END IF;
    series:=prior.series_id; revision:=prior.version+1;
  END IF;
  INSERT INTO public.crm_registry_files(study_id,previous_id,series_id,version,upload_token,filename,mime,bytes,sha256,actor_id)
    VALUES(study,previous,series,revision,token,name,media,content,digest,public.crm_user_id());
  INSERT INTO public.audit_logs(actor_id,action) VALUES(public.crm_user_id(),'research.document_version_added');
  RETURN 'saved';
END $$;
REVOKE ALL ON FUNCTION crm_manages_registry(),crm_registry_record(integer),crm_registry_candidate(text),
 crm_registry_save(integer,integer,jsonb),crm_registry_file_list(integer),crm_registry_file_get(integer,uuid),
 crm_registry_file_save(integer,uuid,uuid,text,text,bytea,text) FROM PUBLIC;
GRANT EXECUTE ON FUNCTION crm_manages_registry(),crm_registry_record(integer),crm_registry_candidate(text),
 crm_registry_save(integer,integer,jsonb),crm_registry_file_list(integer),crm_registry_file_get(integer,uuid),
 crm_registry_file_save(integer,uuid,uuid,text,text,bytea,text) TO crm_app;
