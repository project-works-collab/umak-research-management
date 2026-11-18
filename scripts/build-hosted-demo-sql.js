import {readFileSync,readdirSync,mkdirSync,writeFileSync} from 'node:fs';
import {parseEnv} from 'node:util';
import {hashPassword} from '../src/crypto.js';
import {PDFDocument} from 'pdf-lib';
const env=parseEnv(readFileSync('.private/render-demo-deployment/render.env','utf8'));
const fixture=JSON.parse(readFileSync('demo/acceptance/fixtures.json','utf8'));
const hash=await hashPassword(env.DEMO_PASSWORD);
const pdf=await PDFDocument.create();pdf.addPage().drawText('Synthetic CRM application form');const form=Buffer.from(await pdf.save()).toString('base64');
const quote=value=>"'"+String(value).replaceAll("'","''")+"'";
let sql=`BEGIN; SELECT pg_advisory_xact_lock(73641029);
CREATE TABLE IF NOT EXISTS public.schema_migrations(name text PRIMARY KEY,applied_at timestamptz NOT NULL DEFAULT now());
DO $baseline$ BEGIN
 IF to_regclass('public.audit_chain_head') IS NULL OR to_regprocedure('public.crm_verify_audit_chain()') IS NULL
 THEN RAISE EXCEPTION 'Expected existing migration 007 schema; refusing to guess baseline'; END IF;
END $baseline$;
`;
for(const name of readdirSync('db/migrations').filter(name=>name.endsWith('.sql')).sort()){
 if(name<'008')sql+=`INSERT INTO schema_migrations(name) VALUES(${quote(name)}) ON CONFLICT DO NOTHING;\n`;
 else sql+=`DO $migration$ BEGIN IF NOT EXISTS(SELECT 1 FROM schema_migrations WHERE name=${quote(name)}) THEN
 EXECUTE $ddl$${readFileSync('db/migrations/'+name,'utf8')}$ddl$;
 INSERT INTO schema_migrations(name) VALUES(${quote(name)}); END IF; END $migration$;\n`;
}
sql+=`DO $seed$
DECLARE fixture jsonb=${quote(JSON.stringify(fixture))}::jsonb;
 person jsonb; service jsonb; study jsonb; ticket jsonb; permission text; member text;
 uid integer; sid integer; rid integer; study_id integer; address text; seed_key text; entry record;
 people jsonb='{}'; studies jsonb='{}'; services jsonb='{}'; details jsonb; checks jsonb; state text;
BEGIN
 PERFORM pg_advisory_xact_lock(73641030);
 FOR person IN SELECT value FROM jsonb_array_elements(fixture->'identity'->'people') LOOP
  IF person->>'activation'='invited' THEN CONTINUE; END IF;
  seed_key='account:'||(person->>'key');
  SELECT record_id INTO uid FROM crm_hosted_demo_seed WHERE key=seed_key;
  IF NOT FOUND THEN
   address=replace(person->>'email','@example.test','@umak.edu.ph');
   IF EXISTS(SELECT 1 FROM users WHERE email=address) THEN RAISE EXCEPTION 'Reserved demo address already exists: %',address; END IF;
   INSERT INTO users(email,password_hash,role,display_name,college,active,email_verified_at,self_registered)
    VALUES(address,${quote(hash)},person->>'role',person->>'name','Demo College',coalesce((person->>'active')::boolean,true),
     CASE WHEN person->>'verification'='pending' THEN NULL ELSE now() END,coalesce(person->>'verification'='pending',false)) RETURNING id INTO uid;
   FOR permission IN SELECT jsonb_array_elements_text(coalesce(person->'permissions','[]')) LOOP
    INSERT INTO crm_staff_permissions VALUES(uid,permission);
   END LOOP;
   INSERT INTO crm_hosted_demo_seed VALUES(seed_key,uid);
  END IF;
  people=people||jsonb_build_object(person->>'key',uid);
 END LOOP;
 FOR entry IN SELECT * FROM jsonb_each(fixture->'catalog') LOOP
  service=entry.value; state=CASE WHEN entry.key='draft' THEN 'draft' WHEN entry.key='withdrawn' THEN 'withdrawn' ELSE 'published' END;
  seed_key='service:'||(service->>'code');
  SELECT record_id INTO rid FROM crm_hosted_demo_seed WHERE key=seed_key;
  IF NOT FOUND THEN
   IF EXISTS(SELECT 1 FROM crm_services WHERE code=service->>'code') THEN RAISE EXCEPTION 'Reserved demo service already exists'; END IF;
   INSERT INTO crm_services(code) VALUES(service->>'code') RETURNING id INTO sid;
   SELECT coalesce(jsonb_agg(jsonb_build_object('title',x->>'label','required',x->'required')),'[]') INTO checks FROM jsonb_array_elements(coalesce(service->'requirements','[]')) x;
   details=jsonb_build_object('name',service->>'name','description','Synthetic service for group demonstrations.',
    'purpose','Demonstrate CRM workflows.','eligibility','Student and faculty demo researchers.','applicants',jsonb_build_array('Student','Faculty'),
    'contact_name','Demo CRM','contact_email','demo.content@umak.edu.ph','turnaround','3–5 business days',
    'channel',coalesce(service->>'channel','in_app'),'instructions',jsonb_build_array(CASE WHEN service->>'channel'='external'
      THEN 'Use the external channel described by demo staff.' ELSE 'Download and complete the current form.' END),'requirements',checks);
   INSERT INTO crm_service_revisions(service_id,version,state,details,form_name,form_mime,form_bytes,form_updated_at,published_at)
    VALUES(sid,1,state,details,CASE WHEN entry.key='publishedInApp' THEN 'demo-form.pdf' END,
      CASE WHEN entry.key='publishedInApp' THEN 'application/pdf' END,
      CASE WHEN entry.key='publishedInApp' THEN decode(${quote(form)},'base64') END,
      CASE WHEN entry.key='publishedInApp' THEN now() END,CASE WHEN state<>'draft' THEN now() END) RETURNING id INTO rid;
   INSERT INTO crm_service_history(revision_id,actor_id,action) VALUES(rid,(people->>'content')::integer,CASE WHEN state='draft' THEN 'draft_created' ELSE state END);
   INSERT INTO crm_hosted_demo_seed VALUES(seed_key,rid);
  END IF;
  services=services||jsonb_build_object(entry.key,rid);
 END LOOP;
 FOR study IN SELECT value FROM jsonb_array_elements(fixture->'D1'->'studies') LOOP
  seed_key='study:'||(study->>'code'); SELECT record_id INTO study_id FROM crm_hosted_demo_seed WHERE key=seed_key;
  IF NOT FOUND THEN
   INSERT INTO research_records(author_id,title,abstract,status,research_year,department,lifecycle_state,completed_at,presented_at,is_registered)
    VALUES((people->>(study->>'owner'))::integer,study->>'title','Synthetic research for group demonstrations.','Approved',2026,'Demo College',
     CASE WHEN study->>'stage'='Completed' THEN 'Completed' ELSE 'In-Progress' END,(study->>'completedAt')::date,(study->>'presentedAt')::date,true) RETURNING id INTO study_id;
   FOR member IN SELECT jsonb_array_elements_text(study->'members') LOOP INSERT INTO study_members VALUES(study_id,(people->>member)::integer); END LOOP;
   INSERT INTO crm_research_registrations(study_id,registrant_id,registrant_category,submission_token,fingerprint,academic_period,adviser,registration_details)
    VALUES(study_id,(people->>(study->>'owner'))::integer,study->>'registrantCategory',gen_random_uuid(),'hosted_demo','AY 2026-2027','Demo Adviser','{}');
   INSERT INTO crm_registered_progress(study_id,presentation_event,next_action) VALUES(study_id,coalesce(study->>'presentation',''),CASE WHEN study->>'stage'='Completed' THEN '' ELSE 'Prepare the next milestone' END);
   INSERT INTO crm_hosted_demo_seed VALUES(seed_key,study_id);
  END IF;
  studies=studies||jsonb_build_object(study->>'code',study_id);
 END LOOP;
 FOR ticket IN SELECT value FROM jsonb_array_elements(fixture->'D1'->'requests') LOOP
  seed_key='request:'||(ticket->>'code');
  IF NOT EXISTS(SELECT 1 FROM crm_hosted_demo_seed WHERE key=seed_key) THEN
   INSERT INTO crm_service_requests(requester_id,revision_id,study_id,requester_category,requester_college,subject,description,submission_token,fingerprint,status,submitted_at)
    VALUES((people->>(ticket->>'owner'))::integer,(services->>'publishedInApp')::integer,(studies->>(ticket->>'study'))::integer,
      'student','Demo College','Synthetic group demo request','Synthetic demonstration',gen_random_uuid(),'hosted_demo',ticket->>'status',(ticket->>'submittedAt')::timestamptz) RETURNING id INTO rid;
   INSERT INTO crm_hosted_demo_seed VALUES(seed_key,rid);
  END IF;
 END LOOP;
END $seed$;
COMMIT;
SELECT 'Hosted workflow schema and UMak demo seed ready' AS result,(SELECT count(*) FROM schema_migrations) AS migrations,(SELECT count(*) FROM crm_hosted_demo_seed) AS seeded_records;
`;
mkdirSync('.private/render-demo-deployment',{recursive:true,mode:0o700});writeFileSync('.private/render-demo-deployment/apply-hosted-demo.sql',sql,{mode:0o600});console.log('Private migration and seed SQL prepared.');
