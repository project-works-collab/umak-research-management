import pg from 'pg';
import {readFileSync} from 'node:fs';
import {randomUUID} from 'node:crypto';
import {PDFDocument} from 'pdf-lib';
import {hashPassword} from '../src/crypto.js';

export async function seedHostedDemo(db,password){
  if(!password||password.length<12)throw Error('Demo password must have at least 12 characters');
  const fixture=JSON.parse(readFileSync(new URL('../demo/acceptance/fixtures.json',import.meta.url),'utf8'));
  const hash=await hashPassword(password);
  const people=new Map(),studies=new Map(),revisions=new Map();
  await db.query('BEGIN');
  try{
    await db.query('SELECT pg_advisory_xact_lock(73641030)');
    async function saved(key,create){
      const prior=(await db.query('SELECT record_id FROM crm_hosted_demo_seed WHERE key=$1',[key])).rows[0];
      if(prior)return prior.record_id;
      const id=await create();
      await db.query('INSERT INTO crm_hosted_demo_seed VALUES($1,$2)',[key,id]);return id;
    }
    for(const person of fixture.identity.people.filter(person=>person.activation!=='invited')){
      const email=person.email.replace('@example.test','@umak.edu.ph');
      const id=await saved('account:'+person.key,async()=>{
        if((await db.query('SELECT id FROM users WHERE email=$1',[email])).rowCount)throw Error('Reserved demo address already exists; review before adopting it');
        const id=(await db.query(`INSERT INTO users(email,password_hash,role,display_name,college,active,email_verified_at,self_registered)
          VALUES($1,$2,$3,$4,'Demo College',$5,CASE WHEN $6 THEN now() ELSE NULL END,$7) RETURNING id`,
          [email,hash,person.role,person.name,person.active!==false,person.verification!=='pending',person.verification==='pending'])).rows[0].id;
        for(const permission of person.permissions??[])await db.query('INSERT INTO crm_staff_permissions VALUES($1,$2)',[id,permission]);
        return id;
      });people.set(person.key,id);
    }
    const pdf=await PDFDocument.create();pdf.addPage().drawText('Synthetic CRM application form');const bytes=Buffer.from(await pdf.save());
    for(const [key,state] of [['publishedInApp','published'],['publishedExternal','published'],['publishedSimple','published'],['draft','draft'],['withdrawn','withdrawn']]){
      const sample=fixture.catalog[key];
      const revision=await saved('service:'+sample.code,async()=>{
        if((await db.query('SELECT id FROM crm_services WHERE code=$1',[sample.code])).rowCount)throw Error('Reserved demo service already exists; review before adopting it');
        const service=(await db.query('INSERT INTO crm_services(code) VALUES($1) RETURNING id',[sample.code])).rows[0].id;
        const details={name:sample.name,description:'Synthetic service for group demonstrations.',purpose:'Demonstrate CRM workflows.',
          eligibility:'Student and faculty demo researchers.',applicants:['Student','Faculty'],contact_name:'Demo CRM',
          contact_email:'demo.content@umak.edu.ph',turnaround:'3–5 business days',channel:sample.channel??'in_app',
          instructions:[sample.channel==='external'?'Use the external channel described by demo staff.':'Download and complete the current form.'],
          requirements:(sample.requirements??[]).map(item=>({title:item.label,required:item.required}))};
        const form=key==='publishedInApp';
        const id=(await db.query(`INSERT INTO crm_service_revisions(service_id,version,state,details,form_name,form_mime,form_bytes,form_updated_at,published_at)
          VALUES($1,1,$2,$3,$4,$5,$6,CASE WHEN $6::bytea IS NOT NULL THEN now() END,CASE WHEN $2<>'draft' THEN now() END) RETURNING id`,
          [service,state,details,form?'demo-form.pdf':null,form?'application/pdf':null,form?bytes:null])).rows[0].id;
        await db.query('INSERT INTO crm_service_history(revision_id,actor_id,action) VALUES($1,$2,$3)',[id,people.get('content'),state==='draft'?'draft_created':state]);return id;
      });revisions.set(key,revision);
    }
    for(const study of fixture.D1.studies){
      const id=await saved('study:'+study.code,async()=>{
        const id=(await db.query(`INSERT INTO research_records(author_id,title,abstract,status,research_year,department,lifecycle_state,completed_at,presented_at,is_registered)
          VALUES($1,$2,'Synthetic research for group demonstrations.','Approved',2026,'Demo College',$3,$4,$5,true) RETURNING id`,
          [people.get(study.owner),study.title,study.stage==='Completed'?'Completed':'In-Progress',study.completedAt??null,study.presentedAt??null])).rows[0].id;
        for(const member of study.members)await db.query('INSERT INTO study_members VALUES($1,$2)',[id,people.get(member)]);
        await db.query(`INSERT INTO crm_research_registrations(study_id,registrant_id,registrant_category,submission_token,fingerprint,academic_period,adviser,registration_details)
          VALUES($1,$2,$3,$4,'hosted_demo','AY 2026-2027','Demo Adviser','{}')`,[id,people.get(study.owner),study.registrantCategory,randomUUID()]);
        await db.query('INSERT INTO crm_registered_progress(study_id,presentation_event,next_action) VALUES($1,$2,$3)',
          [id,study.presentation??'',study.stage==='Completed'?'':'Prepare the next milestone']);return id;
      });studies.set(study.code,id);
    }
    for(const request of fixture.D1.requests)await saved('request:'+request.code,async()=>
      (await db.query(`INSERT INTO crm_service_requests(requester_id,revision_id,study_id,requester_category,requester_college,subject,description,submission_token,fingerprint,status,submitted_at)
        VALUES($1,$2,$3,'student','Demo College','Synthetic group demo request','Synthetic demonstration',$4,'hosted_demo',$5,$6) RETURNING id`,
        [people.get(request.owner),revisions.get('publishedInApp'),studies.get(request.study),randomUUID(),request.status,request.submittedAt])).rows[0].id);
    await db.query('COMMIT');
    return {accounts:people.size,services:revisions.size,studies:studies.size};
  }catch(error){await db.query('ROLLBACK');throw error;}
}
if(process.argv[1]&&new URL(import.meta.url).pathname===process.argv[1]){
  if(process.env.ALLOW_SYNTHETIC_SETUP!=='yes'||process.env.CRM_DEMO_MODE!=='umak')throw Error('Explicit hosted synthetic demo flags required');
  const db=new pg.Client({connectionString:process.env.ADMIN_DATABASE_URL,connectionTimeoutMillis:10000});
  try{await db.connect();console.log('Hosted demo seed:',await seedHostedDemo(db,process.env.DEMO_PASSWORD));}finally{await db.end();}
}
