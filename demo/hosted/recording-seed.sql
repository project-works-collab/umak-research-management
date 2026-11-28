-- Additive, repeatable fixtures for the hosted synthetic recording environment.
-- Run as the database owner after migrations 022-024. Never resets real records.
BEGIN;
SELECT pg_advisory_xact_lock(73641030);
DO $seed$
DECLARE
 student_id integer; registry_id integer; sample jsonb; study integer;
 demo_a integer; form bytea; file_id uuid; seed_key text;
BEGIN
 SELECT record_id INTO student_id FROM crm_hosted_demo_seed WHERE key='account:student';
 SELECT record_id INTO registry_id FROM crm_hosted_demo_seed WHERE key='account:registry';
 SELECT record_id INTO demo_a FROM crm_hosted_demo_seed WHERE key='study:RES-DEMO-001';
 IF student_id IS NULL OR registry_id IS NULL OR demo_a IS NULL
 THEN RAISE EXCEPTION 'Run the base hosted synthetic demo seed first'; END IF;
 IF NOT EXISTS(SELECT 1 FROM users WHERE id=student_id AND email='demo.student@umak.edu.ph')
 OR NOT EXISTS(SELECT 1 FROM users WHERE id=registry_id AND email='demo.registry@umak.edu.ph')
 THEN RAISE EXCEPTION 'Demo seed identity mismatch'; END IF;
 FOR sample IN SELECT value FROM jsonb_array_elements('[
 {"key":"rainwater-exact","title":"Campus Rainwater Harvesting Monitoring System","abstract":"This study designs a campus rainwater harvesting monitoring system using water level sensors and a dashboard to measure tank storage and reduce freshwater consumption."},
 {"key":"rainwater-unrelated","title":"Campus Rainwater Harvesting Monitoring System","abstract":"An interview study of student theater participation and public speaking confidence using qualitative classroom observations."},
 {"key":"rainwater-alternate","title":"Campus Rainwater Harvesting Sensor Dashboard","abstract":"This study designs a campus rainwater harvesting monitoring system using water level sensors and a dashboard to measure tank storage and reduce freshwater consumption."}
 ]'::jsonb) LOOP
  seed_key='recording:'||(sample->>'key');
  SELECT record_id INTO study FROM crm_hosted_demo_seed WHERE key=seed_key;
  IF NOT FOUND THEN
   INSERT INTO research_records(author_id,title,abstract,status,research_year,department,lifecycle_state,is_registered)
    VALUES(student_id,sample->>'title',sample->>'abstract','Approved',2026,'Demo College','Registered',true)
    RETURNING id INTO study;
   INSERT INTO study_members(study_id,user_id) VALUES(study,student_id);
   INSERT INTO crm_research_registrations(study_id,registrant_id,registrant_category,submission_token,fingerprint,academic_period,adviser,registration_details)
    VALUES(study,student_id,'student',gen_random_uuid(),'recording_demo','AY 2026-2027','Demo Adviser','{}');
   INSERT INTO crm_registered_progress(study_id,next_action) VALUES(study,'Review the synthetic comparison example');
   INSERT INTO crm_hosted_demo_seed(key,record_id) VALUES(seed_key,study);
  ELSIF NOT EXISTS(SELECT 1 FROM research_records WHERE id=study) THEN
   RAISE EXCEPTION 'Recording seed marker points to a missing study';
  END IF;
  INSERT INTO review_assignments(study_id,staff_id) VALUES(study,registry_id) ON CONFLICT DO NOTHING;
 END LOOP;
 -- Use the existing published demo PDF, preserving any already uploaded versions.
 IF NOT EXISTS(SELECT 1 FROM crm_registry_files WHERE study_id=demo_a) THEN
  SELECT v.form_bytes INTO form FROM crm_service_revisions v
   JOIN crm_hosted_demo_seed s ON s.record_id=v.id AND s.key='service:SVC-DEMO-A'
   WHERE v.form_bytes IS NOT NULL;
  IF form IS NULL THEN RAISE EXCEPTION 'Published synthetic demo PDF is missing'; END IF;
  file_id=gen_random_uuid();
  INSERT INTO crm_registry_files(id,study_id,series_id,version,upload_token,filename,mime,bytes,sha256,actor_id)
   VALUES(file_id,demo_a,file_id,1,gen_random_uuid(),'demo-form.pdf','application/pdf',form,
    encode(sha256(form),'hex'),registry_id);
  INSERT INTO audit_logs(actor_id,action) VALUES(registry_id,'research.document_version_added');
 END IF;
END $seed$;
COMMIT;
SELECT key,record_id FROM crm_hosted_demo_seed WHERE key LIKE 'recording:%' ORDER BY key;
