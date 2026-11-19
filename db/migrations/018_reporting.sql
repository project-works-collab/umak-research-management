CREATE FUNCTION crm_reporting_snapshot() RETURNS jsonb
LANGUAGE sql STABLE SECURITY DEFINER SET search_path=pg_catalog,public AS $$
  SELECT jsonb_build_object('generated_at',clock_timestamp(),
    'research',coalesce((SELECT jsonb_agg(jsonb_build_object(
      'id',r.id,'title',r.title,'college',r.department,'academic_period',g.academic_period,
      'category',g.registrant_category,'adviser',g.adviser,'stage',r.lifecycle_state,
      'completed',r.completed_at IS NOT NULL,'presented',r.presented_at IS NOT NULL,'published',r.published_at IS NOT NULL,
      'researchers',coalesce((SELECT jsonb_agg(jsonb_build_object('id',u.id,'name',coalesce(u.display_name,u.email)))
        FROM public.study_members m JOIN public.users u ON u.id=m.user_id WHERE m.study_id=r.id),'[]'::jsonb),
      'services',coalesce((SELECT jsonb_agg(DISTINCT s.code) FROM public.crm_service_requests q
        JOIN public.crm_service_revisions v ON v.id=q.revision_id JOIN public.crm_services s ON s.id=v.service_id
        WHERE q.study_id=r.id),'[]'::jsonb)))
      FROM public.research_records r JOIN public.crm_research_registrations g ON g.study_id=r.id),'[]'::jsonb),
    'requests',coalesce((SELECT jsonb_agg(jsonb_build_object(
      'id',q.id,'college',q.requester_college,'category',q.requester_category,'status',q.status,
      'service_code',s.code,'service',v.details->>'name',
      'submitted_date',(q.submitted_at AT TIME ZONE 'Asia/Manila')::date))
      FROM public.crm_service_requests q JOIN public.crm_service_revisions v ON v.id=q.revision_id
      JOIN public.crm_services s ON s.id=v.service_id),'[]'::jsonb))
  WHERE EXISTS(SELECT 1 FROM public.users u JOIN public.crm_staff_permissions p ON p.user_id=u.id
    WHERE u.id=public.crm_user_id() AND u.active AND u.role='staff' AND p.permission='reporting')
$$;
REVOKE ALL ON FUNCTION crm_reporting_snapshot() FROM PUBLIC;
GRANT EXECUTE ON FUNCTION crm_reporting_snapshot() TO crm_app;
