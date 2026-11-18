CREATE TABLE crm_staff_permissions (
  user_id integer NOT NULL REFERENCES users ON DELETE CASCADE,
  permission text NOT NULL CHECK(permission IN ('content_management','request_processing',
    'registry_management','progress_updates','reporting','account_administration')),
  PRIMARY KEY(user_id, permission)
);
GRANT SELECT ON crm_staff_permissions TO crm_app;

CREATE TABLE crm_services (
  id integer GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  code text UNIQUE NOT NULL CHECK(code ~ '^[A-Z0-9][A-Z0-9-]{1,31}$')
);
CREATE TABLE crm_service_revisions (
  id integer GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  service_id integer NOT NULL REFERENCES crm_services,
  version integer NOT NULL CHECK(version>0),
  state text NOT NULL CHECK(state IN ('draft','published','withdrawn','superseded')),
  details jsonb NOT NULL CHECK(jsonb_typeof(details)='object'),
  form_name text,
  form_mime text,
  form_bytes bytea CHECK(octet_length(form_bytes) BETWEEN 1 AND 10000000),
  form_updated_at timestamptz,
  published_at timestamptz,
  updated_at timestamptz NOT NULL DEFAULT clock_timestamp(),
  CHECK((form_name IS NULL)=(form_bytes IS NULL) AND
        (form_mime IS NULL)=(form_bytes IS NULL) AND
        (form_updated_at IS NULL)=(form_bytes IS NULL)),
  UNIQUE(service_id, version)
);
CREATE UNIQUE INDEX crm_service_one_published ON crm_service_revisions(service_id) WHERE state='published';
CREATE UNIQUE INDEX crm_service_one_draft ON crm_service_revisions(service_id) WHERE state='draft';
CREATE TABLE crm_service_history (
  id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  revision_id integer NOT NULL REFERENCES crm_service_revisions,
  actor_id integer REFERENCES users ON DELETE SET NULL,
  action text NOT NULL CHECK(action IN ('draft_created','draft_updated','form_replaced','published','withdrawn','superseded')),
  recorded_at timestamptz NOT NULL DEFAULT clock_timestamp()
);
GRANT SELECT,INSERT ON crm_services TO crm_app;
GRANT SELECT,INSERT,UPDATE ON crm_service_revisions TO crm_app;
GRANT SELECT,INSERT ON crm_service_history TO crm_app;
GRANT USAGE,SELECT ON SEQUENCE crm_services_id_seq,crm_service_revisions_id_seq,crm_service_history_id_seq TO crm_app;
