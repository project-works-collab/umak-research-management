ALTER TABLE research_records ADD COLUMN is_registered boolean NOT NULL DEFAULT false;
GRANT UPDATE(is_registered) ON research_records TO crm_app;

CREATE TABLE crm_research_registrations (
  study_id integer PRIMARY KEY REFERENCES research_records ON DELETE CASCADE,
  registrant_id integer NOT NULL REFERENCES users ON DELETE CASCADE,
  registrant_category text NOT NULL CHECK(registrant_category IN ('student','faculty')),
  submission_token uuid NOT NULL,
  fingerprint text NOT NULL,
  academic_period text NOT NULL,
  adviser text NOT NULL DEFAULT '',
  registration_details jsonb NOT NULL,
  registered_at timestamptz NOT NULL DEFAULT clock_timestamp(),
  UNIQUE(registrant_id,submission_token)
);
ALTER TABLE crm_research_registrations ENABLE ROW LEVEL SECURITY;
ALTER TABLE crm_research_registrations FORCE ROW LEVEL SECURITY;
CREATE POLICY registered_study_read ON crm_research_registrations FOR SELECT TO crm_app
  USING(crm_member(study_id) OR crm_assigned(study_id));
CREATE POLICY registration_create ON crm_research_registrations FOR INSERT TO crm_app
  WITH CHECK(registrant_id=crm_user_id() AND crm_owns_study(study_id) AND crm_is_researcher());
GRANT SELECT,INSERT ON crm_research_registrations TO crm_app;

CREATE FUNCTION crm_registration_candidate(address text)
RETURNS TABLE(id integer,display_name text,email text,role text,college text)
LANGUAGE sql STABLE SECURITY DEFINER SET search_path=pg_catalog,public AS $$
  SELECT u.id,coalesce(u.display_name,u.email),u.email,u.role,u.college
  FROM public.users u WHERE public.crm_is_researcher() AND u.id<>public.crm_user_id()
    AND u.email=address AND u.active AND u.email_verified_at IS NOT NULL AND u.role IN ('student','faculty')
$$;
CREATE FUNCTION crm_eligible_registration_members(people integer[])
RETURNS integer LANGUAGE sql STABLE SECURITY DEFINER SET search_path=pg_catalog,public AS $$
  SELECT count(*)::integer FROM public.users u
   WHERE public.crm_is_researcher() AND u.id=ANY(people) AND u.id<>public.crm_user_id()
     AND u.active AND u.email_verified_at IS NOT NULL AND u.role IN ('student','faculty')
$$;
CREATE FUNCTION crm_registration_team(study integer)
RETURNS TABLE(id integer,name text,role text)
LANGUAGE sql STABLE SECURITY DEFINER SET search_path=pg_catalog,public AS $$
  SELECT u.id,coalesce(u.display_name,u.email),u.role
  FROM public.study_members m JOIN public.users u ON u.id=m.user_id WHERE m.study_id=study
    AND (public.crm_can_access(study) OR EXISTS (
      SELECT 1 FROM public.crm_staff_permissions p WHERE public.crm_is_staff()
        AND p.user_id=public.crm_user_id() AND p.permission='registry_management'
    )) ORDER BY u.id
$$;
CREATE FUNCTION crm_registrations_for_registry()
RETURNS TABLE(id integer,title text,department text,academic_period text,adviser text,
  registrant_category text,registrant_name text,lifecycle_state text,registered_at timestamptz)
LANGUAGE sql STABLE SECURITY DEFINER SET search_path=pg_catalog,public AS $$
  SELECT r.id,r.title,r.department,g.academic_period,g.adviser,g.registrant_category,
    coalesce(u.display_name,u.email),r.lifecycle_state,g.registered_at
  FROM public.crm_research_registrations g JOIN public.research_records r ON r.id=g.study_id
    JOIN public.users u ON u.id=g.registrant_id
  WHERE public.crm_is_staff() AND EXISTS(SELECT 1 FROM public.crm_staff_permissions p
    WHERE p.user_id=public.crm_user_id() AND p.permission='registry_management')
$$;
REVOKE ALL ON FUNCTION crm_registration_candidate(text),crm_eligible_registration_members(integer[]),
  crm_registration_team(integer),crm_registrations_for_registry() FROM PUBLIC;
GRANT EXECUTE ON FUNCTION crm_registration_candidate(text),crm_eligible_registration_members(integer[]),
  crm_registration_team(integer),crm_registrations_for_registry() TO crm_app;
