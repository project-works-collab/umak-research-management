CREATE TABLE crm_demo_mail (
  id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  recipient text NOT NULL CHECK(recipient ~ '^(demo[.-][a-z0-9.-]+|(student|faculty)\.demo[0-9]+)@umak\.edu\.ph$'),
  body text NOT NULL CHECK(length(body)<=4000),
  created_at timestamptz NOT NULL DEFAULT clock_timestamp()
);
REVOKE ALL ON crm_demo_mail FROM PUBLIC;
GRANT SELECT,INSERT,DELETE ON crm_demo_mail TO crm_app;
GRANT USAGE ON SEQUENCE crm_demo_mail_id_seq TO crm_app;

CREATE TABLE crm_hosted_demo_seed (key text PRIMARY KEY,record_id integer NOT NULL);
REVOKE ALL ON crm_hosted_demo_seed FROM PUBLIC,crm_app;
