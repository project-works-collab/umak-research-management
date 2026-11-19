-- Supabase default grants are independent of PUBLIC grants. The Express API uses crm_app,
-- not Supabase's browser-facing anon/authenticated roles.
DO $acl$
DECLARE role_name text; object record;
BEGIN
 FOR role_name IN SELECT rolname FROM pg_roles WHERE rolname IN ('anon','authenticated') LOOP
  EXECUTE format('REVOKE ALL ON ALL TABLES IN SCHEMA public FROM %I',role_name);
  EXECUTE format('REVOKE ALL ON ALL SEQUENCES IN SCHEMA public FROM %I',role_name);
  FOR object IN SELECT p.oid::regprocedure AS identity FROM pg_proc p JOIN pg_namespace n ON n.oid=p.pronamespace
    WHERE n.nspname='public' AND p.proname LIKE 'crm_%' LOOP
   EXECUTE format('REVOKE ALL ON FUNCTION %s FROM %I',object.identity,role_name);
  END LOOP;
  EXECUTE format('ALTER DEFAULT PRIVILEGES IN SCHEMA public REVOKE ALL ON TABLES FROM %I',role_name);
  EXECUTE format('ALTER DEFAULT PRIVILEGES IN SCHEMA public REVOKE ALL ON SEQUENCES FROM %I',role_name);
  EXECUTE format('ALTER DEFAULT PRIVILEGES IN SCHEMA public REVOKE ALL ON FUNCTIONS FROM %I',role_name);
 END LOOP;
END $acl$;
ALTER TABLE schema_migrations ENABLE ROW LEVEL SECURITY;
ALTER TABLE crm_hosted_demo_seed ENABLE ROW LEVEL SECURITY;
ALTER TABLE crm_demo_mail ENABLE ROW LEVEL SECURITY;
CREATE POLICY crm_demo_mail_runtime ON crm_demo_mail FOR ALL TO crm_app USING(true) WITH CHECK(true);
ALTER TABLE crm_password_challenges ENABLE ROW LEVEL SECURITY;
ALTER TABLE crm_account_history ENABLE ROW LEVEL SECURITY;
ALTER TABLE crm_registry_corrections ENABLE ROW LEVEL SECURITY;
ALTER TABLE crm_registry_files ENABLE ROW LEVEL SECURITY;
