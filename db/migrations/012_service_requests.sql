CREATE TABLE crm_service_requests (
  id integer GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  requester_id integer NOT NULL REFERENCES users ON DELETE CASCADE,
  revision_id integer NOT NULL REFERENCES crm_service_revisions,
  study_id integer REFERENCES research_records ON DELETE SET NULL,
  requester_category text NOT NULL CHECK(requester_category IN ('student','faculty')),
  requester_college text,
  subject text NOT NULL CHECK(length(subject) BETWEEN 1 AND 160),
  description text NOT NULL CHECK(length(description)<=4000),
  submission_token uuid NOT NULL,
  fingerprint text NOT NULL,
  status text NOT NULL DEFAULT 'Submitted' CHECK(status IN ('Submitted','In review','For correction','Completed')),
  version integer NOT NULL DEFAULT 1 CHECK(version>0),
  submitted_at timestamptz NOT NULL DEFAULT clock_timestamp(),
  UNIQUE(requester_id, submission_token)
);
CREATE INDEX crm_service_request_owner ON crm_service_requests(requester_id,id DESC);

CREATE FUNCTION crm_processes_requests() RETURNS boolean
LANGUAGE sql STABLE AS $$
  SELECT public.crm_is_staff() AND EXISTS(
    SELECT 1 FROM public.crm_staff_permissions WHERE user_id=public.crm_user_id() AND permission='request_processing'
  )
$$;
REVOKE ALL ON FUNCTION crm_processes_requests() FROM PUBLIC;
GRANT EXECUTE ON FUNCTION crm_processes_requests() TO crm_app;

CREATE TABLE crm_service_request_files (
  id uuid PRIMARY KEY,
  requester_id integer NOT NULL REFERENCES users ON DELETE CASCADE,
  attempt_token uuid NOT NULL,
  request_id integer REFERENCES crm_service_requests ON DELETE CASCADE,
  requirement integer NOT NULL CHECK(requirement>=0),
  filename text NOT NULL,
  mime text NOT NULL,
  bytes bytea NOT NULL CHECK(octet_length(bytes) BETWEEN 1 AND 10000000),
  sha256 text NOT NULL,
  created_at timestamptz NOT NULL DEFAULT clock_timestamp(),
  UNIQUE(requester_id,attempt_token,requirement)
);
CREATE INDEX crm_service_uploaded_request ON crm_service_request_files(request_id);
CREATE INDEX crm_service_staged_request ON crm_service_request_files(requester_id,created_at) WHERE request_id IS NULL;

CREATE TABLE crm_service_resubmissions (
  request_id integer NOT NULL REFERENCES crm_service_requests ON DELETE CASCADE,
  previous_version integer NOT NULL,
  fingerprint text NOT NULL,
  resubmitted_at timestamptz NOT NULL,
  PRIMARY KEY(request_id,previous_version)
);

CREATE TABLE crm_service_request_reviews (
  id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  request_id integer NOT NULL REFERENCES crm_service_requests ON DELETE CASCADE,
  reviewer_id integer REFERENCES users ON DELETE SET NULL,
  from_status text NOT NULL,
  to_status text NOT NULL,
  checklist jsonb NOT NULL,
  feedback text NOT NULL,
  missing_items jsonb NOT NULL,
  next_action text NOT NULL,
  internal_remark text NOT NULL,
  deadline_at timestamptz,
  checkpoint jsonb,
  correction_reason text,
  reviewed_at timestamptz NOT NULL DEFAULT clock_timestamp()
);
CREATE INDEX crm_service_request_latest_review ON crm_service_request_reviews(request_id,id DESC);

CREATE TABLE crm_service_request_updates (
  id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  request_id integer NOT NULL REFERENCES crm_service_requests ON DELETE CASCADE,
  kind text NOT NULL CHECK(kind IN ('receipt','review','correction','completion','deadline')),
  available_at timestamptz NOT NULL DEFAULT clock_timestamp()
);
CREATE INDEX crm_service_request_updates_visible ON crm_service_request_updates(request_id,available_at);

ALTER TABLE crm_service_requests ENABLE ROW LEVEL SECURITY;
ALTER TABLE crm_service_requests FORCE ROW LEVEL SECURITY;
CREATE POLICY request_read ON crm_service_requests FOR SELECT TO crm_app
  USING(requester_id=crm_user_id() OR crm_processes_requests());
CREATE POLICY request_insert ON crm_service_requests FOR INSERT TO crm_app
  WITH CHECK(requester_id=crm_user_id() AND crm_is_researcher());
CREATE POLICY request_update ON crm_service_requests FOR UPDATE TO crm_app
  USING(requester_id=crm_user_id() OR crm_processes_requests())
  WITH CHECK(requester_id=crm_user_id() OR crm_processes_requests());

ALTER TABLE crm_service_request_files ENABLE ROW LEVEL SECURITY;
ALTER TABLE crm_service_request_files FORCE ROW LEVEL SECURITY;
CREATE POLICY file_read ON crm_service_request_files FOR SELECT TO crm_app
  USING(requester_id=crm_user_id() OR (request_id IS NOT NULL AND crm_processes_requests()));
CREATE POLICY file_insert ON crm_service_request_files FOR INSERT TO crm_app
  WITH CHECK(requester_id=crm_user_id() AND crm_is_researcher());
CREATE POLICY file_assign ON crm_service_request_files FOR UPDATE TO crm_app
  USING(requester_id=crm_user_id()) WITH CHECK(requester_id=crm_user_id());
CREATE POLICY expired_upload_cleanup ON crm_service_request_files FOR DELETE TO crm_app
  USING(requester_id=crm_user_id() AND request_id IS NULL AND created_at<now()-interval '24 hours');
ALTER TABLE crm_service_resubmissions ENABLE ROW LEVEL SECURITY;
ALTER TABLE crm_service_resubmissions FORCE ROW LEVEL SECURITY;
CREATE POLICY resubmission_owner ON crm_service_resubmissions FOR ALL TO crm_app
  USING(EXISTS(SELECT 1 FROM public.crm_service_requests r WHERE r.id=request_id AND r.requester_id=crm_user_id()))
  WITH CHECK(EXISTS(SELECT 1 FROM public.crm_service_requests r WHERE r.id=request_id AND r.requester_id=crm_user_id()));
ALTER TABLE crm_service_request_reviews ENABLE ROW LEVEL SECURITY;
ALTER TABLE crm_service_request_reviews FORCE ROW LEVEL SECURITY;
CREATE POLICY review_staff ON crm_service_request_reviews FOR ALL TO crm_app
  USING(crm_processes_requests()) WITH CHECK(crm_processes_requests());
ALTER TABLE crm_service_request_updates ENABLE ROW LEVEL SECURITY;
ALTER TABLE crm_service_request_updates FORCE ROW LEVEL SECURITY;
CREATE POLICY updates_read ON crm_service_request_updates FOR SELECT TO crm_app
  USING(EXISTS(SELECT 1 FROM public.crm_service_requests r WHERE r.id=request_id));
CREATE POLICY updates_write ON crm_service_request_updates FOR INSERT TO crm_app
  WITH CHECK(EXISTS(SELECT 1 FROM public.crm_service_requests r WHERE r.id=request_id));
CREATE POLICY updates_replace_deadline ON crm_service_request_updates FOR DELETE TO crm_app
  USING(kind='deadline' AND crm_processes_requests());

CREATE FUNCTION crm_requester_profile(person integer)
RETURNS TABLE(display_name text,email text,college text)
LANGUAGE sql STABLE SECURITY DEFINER SET search_path=pg_catalog,public AS $$
  SELECT u.display_name,u.email,u.college FROM public.users u WHERE u.id=person
    AND (public.crm_processes_requests() OR person=public.crm_user_id())
$$;
CREATE FUNCTION crm_researcher_review(request integer)
RETURNS TABLE(id bigint,from_status text,to_status text,feedback text,missing_items jsonb,
  next_action text,deadline_at timestamptz,checkpoint jsonb,reviewed_at timestamptz)
LANGUAGE sql STABLE SECURITY DEFINER SET search_path=pg_catalog,public AS $$
  SELECT v.id,v.from_status,v.to_status,v.feedback,v.missing_items,v.next_action,v.deadline_at,v.checkpoint,v.reviewed_at
  FROM public.crm_service_request_reviews v JOIN public.crm_service_requests r ON r.id=v.request_id
  WHERE r.id=request AND r.requester_id=public.crm_user_id() ORDER BY v.id DESC
$$;
REVOKE ALL ON FUNCTION crm_requester_profile(integer),crm_researcher_review(integer) FROM PUBLIC;
GRANT EXECUTE ON FUNCTION crm_requester_profile(integer),crm_researcher_review(integer) TO crm_app;
GRANT SELECT,INSERT ON crm_service_requests,crm_service_request_files,crm_service_request_reviews,crm_service_request_updates TO crm_app;
GRANT UPDATE(status,version) ON crm_service_requests TO crm_app;
GRANT UPDATE(request_id) ON crm_service_request_files TO crm_app;
GRANT DELETE ON crm_service_request_files,crm_service_request_updates TO crm_app;
GRANT SELECT,INSERT ON crm_service_resubmissions TO crm_app;
GRANT USAGE,SELECT ON SEQUENCE crm_service_requests_id_seq,crm_service_request_reviews_id_seq,crm_service_request_updates_id_seq TO crm_app;
