-- Serializes historical backfill with live audit inserts.
LOCK TABLE audit_history IN ACCESS EXCLUSIVE MODE;
ALTER TABLE audit_history ADD COLUMN chain_index bigint,
 ADD COLUMN previous_hash text, ADD COLUMN entry_hash text;
CREATE TABLE audit_chain_head (
 singleton boolean PRIMARY KEY DEFAULT true CHECK(singleton),
 chain_index bigint NOT NULL, entry_hash text NOT NULL
);
INSERT INTO audit_chain_head VALUES(true,0,repeat('0',64));
REVOKE ALL ON audit_chain_head FROM PUBLIC,crm_app;
CREATE FUNCTION crm_audit_digest(entry public.audit_history) RETURNS text
LANGUAGE sql IMMUTABLE SET search_path=pg_catalog,public SET timezone='UTC' AS $$
 SELECT encode(sha256(convert_to('crm-audit-v1:' || jsonb_build_object(
  'id',entry.id,'actor_ref',entry.actor_ref,'target_table',entry.target_table,
  'target_record_id',entry.target_record_id,'action_type',entry.action_type,
  'old_value',entry.old_value,'new_value',entry.new_value,
  'recorded_at',entry.recorded_at,'request_id',entry.request_id,'ip_address',entry.ip_address,
  'chain_index',entry.chain_index,'previous_hash',entry.previous_hash
 )::text,'UTF8')),'hex')
$$;
REVOKE ALL ON FUNCTION crm_audit_digest(public.audit_history) FROM PUBLIC;
DO $$ DECLARE entry public.audit_history; previous text:=repeat('0',64); position bigint:=0; BEGIN
 FOR entry IN SELECT * FROM public.audit_history ORDER BY id LOOP
  position:=position+1; entry.chain_index:=position;entry.previous_hash:=previous;
  entry.entry_hash:=public.crm_audit_digest(entry);
  UPDATE public.audit_history SET chain_index=position,previous_hash=previous,entry_hash=entry.entry_hash WHERE id=entry.id;
  previous:=entry.entry_hash;
 END LOOP;
 UPDATE public.audit_chain_head SET chain_index=position,entry_hash=previous;
END $$;
ALTER TABLE audit_history ALTER COLUMN chain_index SET NOT NULL,
 ALTER COLUMN previous_hash SET NOT NULL, ALTER COLUMN entry_hash SET NOT NULL,
 ADD UNIQUE(chain_index), ADD CHECK(chain_index>0),
 ADD CHECK(previous_hash ~ '^[a-f0-9]{64}$'), ADD CHECK(entry_hash ~ '^[a-f0-9]{64}$');
CREATE FUNCTION crm_chain_audit() RETURNS trigger LANGUAGE plpgsql SECURITY DEFINER
SET search_path=pg_catalog,public SET timezone='UTC' AS $$
DECLARE head public.audit_chain_head;
BEGIN
 SELECT * INTO STRICT head FROM public.audit_chain_head WHERE singleton FOR UPDATE;
 NEW.chain_index:=head.chain_index+1;NEW.previous_hash:=head.entry_hash;
 NEW.entry_hash:=public.crm_audit_digest(NEW);
 UPDATE public.audit_chain_head SET chain_index=NEW.chain_index,entry_hash=NEW.entry_hash WHERE singleton;
 RETURN NEW;
END $$;
REVOKE ALL ON FUNCTION crm_chain_audit() FROM PUBLIC;
CREATE TRIGGER audit_chain BEFORE INSERT ON audit_history FOR EACH ROW EXECUTE FUNCTION crm_chain_audit();
CREATE FUNCTION crm_verify_audit_chain() RETURNS TABLE(valid boolean,entries bigint,first_invalid bigint,head_hash text)
LANGUAGE plpgsql STABLE SECURITY DEFINER SET search_path=pg_catalog,public SET timezone='UTC' AS $$
DECLARE broken bigint; total bigint; tail text; head public.audit_chain_head;
BEGIN
 IF NOT public.crm_is_staff() THEN RAISE EXCEPTION 'Forbidden' USING ERRCODE='42501'; END IF;
 SELECT min(s.chain_index) FILTER(WHERE s.chain_index<>s.expected_index OR s.previous_hash<>s.expected_previous OR s.entry_hash<>s.computed),count(*)
 INTO broken,total FROM (
  SELECT a.chain_index,a.previous_hash,a.entry_hash,row_number() OVER(ORDER BY a.chain_index) expected_index,
   coalesce(lag(a.entry_hash) OVER(ORDER BY a.chain_index),repeat('0',64)) expected_previous,
   public.crm_audit_digest(a) computed FROM public.audit_history a
 ) s;
 SELECT a.entry_hash INTO tail FROM public.audit_history a ORDER BY a.chain_index DESC LIMIT 1;
 SELECT * INTO STRICT head FROM public.audit_chain_head WHERE singleton;
 RETURN QUERY SELECT broken IS NULL AND head.chain_index=total AND head.entry_hash=coalesce(tail,repeat('0',64)),total,broken,head.entry_hash;
END $$;
REVOKE ALL ON FUNCTION crm_verify_audit_chain() FROM PUBLIC;
GRANT EXECUTE ON FUNCTION crm_verify_audit_chain() TO crm_app;
-- Include account creation/changes and retain document content fingerprints in audit metadata.
CREATE OR REPLACE FUNCTION crm_audit_mutation() RETURNS trigger LANGUAGE plpgsql SECURITY DEFINER SET search_path=pg_catalog,public AS $$
DECLARE old_j jsonb; new_j jsonb; target text;
BEGIN
 IF TG_OP<>'INSERT' THEN old_j=to_jsonb(OLD); END IF;
 IF TG_OP<>'DELETE' THEN new_j=to_jsonb(NEW); END IF;
 target=coalesce(new_j->>'id',old_j->>'id',new_j->>'study_id',old_j->>'study_id');
 old_j=old_j-ARRAY['password_hash','email','abstract','internal_staff_notes','researcher_visible_feedback','object_key'];
 new_j=new_j-ARRAY['password_hash','email','abstract','internal_staff_notes','researcher_visible_feedback','object_key'];
 INSERT INTO public.audit_history(actor_ref,target_table,target_record_id,action_type,old_value,new_value,request_id,ip_address)
 VALUES(coalesce(public.crm_user_id()::text,'system'),TG_TABLE_NAME,target,TG_OP,old_j,new_j,
 nullif(current_setting('app.request_id',true),''),nullif(current_setting('app.ip',true),'')::inet);
 RETURN coalesce(NEW,OLD);
END $$;

-- Lifecycle remains separate from public catalogue approval.
ALTER TABLE research_records
 ADD COLUMN lifecycle_state text NOT NULL DEFAULT 'Registered' CHECK(lifecycle_state IN ('Registered','In-Progress','UnderReview','Completed')),
 ADD COLUMN lifecycle_version integer NOT NULL DEFAULT 1 CHECK(lifecycle_version>0),
 ADD COLUMN completed_at date, ADD COLUMN presented_at date, ADD COLUMN published_at date,
 ADD CHECK((lifecycle_state='Completed')=(completed_at IS NOT NULL));
CREATE FUNCTION crm_research_event(study integer,event text,occurred date,expected integer)
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
 ELSIF (current.lifecycle_state,event) IN (('Registered','In-Progress'),('In-Progress','UnderReview'),('UnderReview','In-Progress'),('UnderReview','Completed')) THEN
  current.lifecycle_state:=event;
  IF event='Completed' THEN current.completed_at:=occurred; END IF;
 ELSE RAISE EXCEPTION 'Invalid transition or duplicate outcome' USING ERRCODE='40001';
 END IF;
 RETURN QUERY UPDATE public.research_records SET lifecycle_state=current.lifecycle_state,
  completed_at=current.completed_at,presented_at=current.presented_at,published_at=current.published_at,
  lifecycle_version=lifecycle_version+1 WHERE id=study RETURNING *;
END $$;
REVOKE ALL ON FUNCTION crm_research_event(integer,text,date,integer) FROM PUBLIC;
GRANT EXECUTE ON FUNCTION crm_research_event(integer,text,date,integer) TO crm_app;

-- Each independent document starts a series; a revision references its exact predecessor.
ALTER TABLE documents ADD COLUMN series_id uuid, ADD COLUMN revision_number integer,
 ADD COLUMN previous_document_id uuid REFERENCES documents(id);
UPDATE documents SET series_id=id,revision_number=1;
ALTER TABLE documents ALTER COLUMN series_id SET NOT NULL, ALTER COLUMN revision_number SET NOT NULL,
 ADD CHECK(revision_number>0), ADD UNIQUE(series_id,revision_number), ADD UNIQUE(previous_document_id),
 ADD CHECK((revision_number=1)=(previous_document_id IS NULL));
CREATE INDEX document_study_series ON documents(study_id,series_id,revision_number);
CREATE FUNCTION crm_document_revision() RETURNS trigger LANGUAGE plpgsql SECURITY DEFINER SET search_path=pg_catalog,public AS $$
DECLARE previous public.documents;
BEGIN
 -- Serialize revision allocation with membership updates and other uploads for this study.
 PERFORM 1 FROM public.research_records WHERE id=NEW.study_id FOR UPDATE;
 IF NOT FOUND OR NOT public.crm_is_researcher() OR NOT public.crm_member(NEW.study_id) THEN RAISE EXCEPTION 'Forbidden' USING ERRCODE='42501'; END IF;
 IF NEW.previous_document_id IS NULL THEN NEW.series_id:=NEW.id;NEW.revision_number:=1;
 ELSE
  SELECT * INTO previous FROM public.documents WHERE id=NEW.previous_document_id AND study_id=NEW.study_id;
  IF NOT FOUND THEN RAISE EXCEPTION 'Previous document unavailable' USING ERRCODE='22023'; END IF;
  IF EXISTS(SELECT 1 FROM public.documents WHERE series_id=previous.series_id AND revision_number>previous.revision_number) THEN
   RAISE EXCEPTION 'Revision conflict' USING ERRCODE='40001';
  END IF;
  NEW.series_id:=previous.series_id;NEW.revision_number:=previous.revision_number+1;
 END IF;
 RETURN NEW;
END $$;
REVOKE ALL ON FUNCTION crm_document_revision() FROM PUBLIC;
CREATE TRIGGER document_revision BEFORE INSERT ON documents FOR EACH ROW EXECUTE FUNCTION crm_document_revision();
