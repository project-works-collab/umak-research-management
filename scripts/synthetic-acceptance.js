import {checkHostedDemo} from '../test/support/hosted-demo-smoke.js';
import {execFileSync} from 'node:child_process';
import {once} from 'node:events';
import {createServer} from 'node:http';
import https from 'node:https';
import {createServer as tcpServer} from 'node:net';
import {randomBytes,randomUUID} from 'node:crypto';
import {mkdtemp,readFile,writeFile,rm} from 'node:fs/promises';
import {createWriteStream} from 'node:fs';
import {tmpdir} from 'node:os';
import {join} from 'node:path';
import {pipeline} from 'node:stream/promises';
import assert from 'node:assert/strict';
import pg from 'pg';
import {PDFDocument} from 'pdf-lib';
import {ZipFile} from 'yazl';
import {createApp} from '../src/app.js';
import {configuredVerification} from '../src/auth/verification-mail.js';
import {hashPassword,tokenHash} from '../src/crypto.js';
import {testInbox} from '../test/support/smtp-inbox.js';

const fixture=JSON.parse(await readFile(new URL('../demo/acceptance/fixtures.json',import.meta.url),'utf8'));
const umakDemo=process.argv.includes('--umak-demo');
if(umakDemo){
  for(const person of fixture.identity.people)person.email=person.email.replace('@example.test','@umak.edu.ph');
  fixture.identity.domain='umak.edu.ph';
  fixture.identity.sender='demo.verification@umak.edu.ph';
  fixture.identity.freshSignup='student.demo01@umak.edu.ph';
}
const demoAddress=address=>/^(?:demo[.-][a-z0-9.-]+|(?:student|faculty)\.demo[0-9]+)@umak\.edu\.ph$/.test(address);
const host='127.0.0.1';
const directory=await mkdtemp(join(tmpdir(),'crm-synthetic-'));
const data=join(directory,'postgres');
const dbPassword=randomBytes(24).toString('hex');
const ownerPassword=randomBytes(24).toString('hex');
const demoPassword=randomBytes(18).toString('hex');
const servers=[];
let running=false,db,admin;

async function listen(server){
  await new Promise((resolve,reject)=>{
    server.once('error',reject);
    server.listen(0,host,resolve);
  });
  return server.address().port;
}

function command(name,args,env=process.env){
  execFileSync(name,args,{env,stdio:'inherit'});
}

async function attachmentFixtures(){
  const pdf=await PDFDocument.create();
  pdf.addPage().drawText('Synthetic CRM application form',{x:50,y:700,size:20});
  const bytes=Buffer.from(await pdf.save());
  await writeFile(join(directory,'demo-form.pdf'),bytes);
  await writeFile(join(directory,'over-limit.pdf'),Buffer.concat([bytes,Buffer.alloc(11*1024*1024)]));
  await writeFile(join(directory,'unsupported.png'),Buffer.from(
    'iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+/lS0AAAAASUVORK5CYII=','base64'));
  const docx=new ZipFile();
  for(const [name,text] of [
    ['[Content_Types].xml','<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types"><Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/><Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/></Types>'],
    ['_rels/.rels','<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/></Relationships>'],
    ['word/document.xml','<w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"><w:body><w:p><w:r><w:t>Synthetic CRM supporting document</w:t></w:r></w:p></w:body></w:document>']
  ])docx.addBuffer(Buffer.from(text),name);
  docx.end();
  await pipeline(docx.outputStream,createWriteStream(join(directory,'supporting-document.docx')));
}

async function seed(){
  const client=await admin.connect();
  try{
    await client.query('BEGIN');
    const passwordHash=await hashPassword(demoPassword);
    const users=new Map();
    let invitationCode;
    for(const person of fixture.identity.people){
      const active=person.active!==false&&person.activation!=='invited';
      const user=await client.query(`INSERT INTO users
        (email,password_hash,role,display_name,college,active,email_verified_at,self_registered)
        VALUES($1,$2,$3,$4,$5,$6,CASE WHEN $7 THEN now() ELSE NULL END,$8) RETURNING id`,
        [person.email,person.activation==='invited'?'!':passwordHash,person.role,person.name,
          fixture.edgeCases.college,active,person.verification!=='pending',person.verification==='pending']);
      const userId=user.rows[0].id;
      users.set(person.key,userId);
      for(const permission of person.permissions??[]){
        await client.query('INSERT INTO crm_staff_permissions(user_id,permission) VALUES($1,$2)',
          [userId,permission]);
      }
      if(person.verification==='pending'){
        await client.query(`INSERT INTO researcher_verification(user_id,code_hash,sent_at,expires_at)
          VALUES($1,$2,now()-interval '16 minutes',now()-interval '1 minute')`,
          [userId,randomBytes(32).toString('hex')]);
      }
      if(person.activation==='invited'){
        invitationCode=randomBytes(32).toString('hex');
        await client.query(`INSERT INTO account_activations(token_hash,user_id,expires_at)
          VALUES($1,$2,now()+interval '15 minutes')`,[tokenHash(invitationCode),userId]);
      }
    }
    const studies=new Map();
    for(const study of fixture.D1.studies){
      const row=await client.query(`INSERT INTO research_records
        (author_id,title,abstract,status,research_year,department,lifecycle_state,completed_at)
        VALUES($1,$2,'Synthetic acceptance record','Approved',2026,$3,$4,$5) RETURNING id`,
        [users.get(study.owner),study.title,fixture.edgeCases.college,
          study.stage==='Completed'?'Completed':'In-Progress',study.completedAt??null]);
      studies.set(study.code,row.rows[0].id);
      await client.query('UPDATE research_records SET is_registered=true,presented_at=$2 WHERE id=$1',
        [row.rows[0].id,study.presentedAt??null]);
      await client.query(`INSERT INTO crm_research_registrations(study_id,registrant_id,registrant_category,
        submission_token,fingerprint,academic_period,adviser,registration_details)
        VALUES($1,$2,$3,$4,'synthetic_d1',$5,'Demo Adviser','{}')`,
        [row.rows[0].id,users.get(study.owner),study.registrantCategory,randomUUID(),fixture.edgeCases.academicPeriod]);
      for(const member of study.members){
        await client.query('INSERT INTO study_members VALUES($1,$2)',[row.rows[0].id,users.get(member)]);
      }
      await client.query('INSERT INTO review_assignments VALUES($1,$2)',[row.rows[0].id,users.get('registry')]);
    }
    const requests=new Map();
    for(const ticket of fixture.D1.requests){
      const result=await client.query(`INSERT INTO consultation_tickets
        (requester_id,service_type,status,submitted_at,assigned_staff_id,claimed_at)
        VALUES($1,$2,$3,$4,$5,$4) RETURNING id`,
        [users.get(ticket.owner),ticket.service,ticket.status==='Completed'?'resolved':'claimed',
          ticket.submittedAt,users.get('processing')]);
      requests.set(ticket.code,result.rows[0].id);
    }
    await client.query('COMMIT');
    const mapping={people:Object.fromEntries(users),studies:Object.fromEntries(studies),
      consultations:Object.fromEntries(requests),scenarioOnly:[
        'Institution-specific milestone applicability rules require CRM confirmation',
        'Production mail delivery, malware scanning and deployed acceptance require staging configuration'
      ]};
    return {mapping,invitationCode};
  }catch(error){
    await client.query('ROLLBACK');
    throw error;
  }finally{
    client.release();
  }
}

try{
  const portReservation=tcpServer();
  const pgPort=await listen(portReservation);
  await new Promise(resolve=>portReservation.close(resolve));
  const passwordFile=join(directory,'postgres-password');
  await writeFile(passwordFile,ownerPassword,{mode:0o600});
  command('initdb',['-D',data,'--auth-local=trust','--auth-host=scram-sha-256','-U','postgres','--pwfile='+passwordFile]);
  command('pg_ctl',['-D',data,'-l',join(directory,'postgres.log'),'-o',`-p ${pgPort} -h ${host} -k ${directory}`,'-w','start']);
  running=true;
  command('createdb',['-h',host,'-p',String(pgPort),'-U','postgres','crm_synthetic'],
    {...process.env,PGPASSWORD:ownerPassword});
  const ownerUrl=`postgresql://postgres:${ownerPassword}@${host}:${pgPort}/crm_synthetic`;
  const appUrl=`postgresql://crm_app:${dbPassword}@${host}:${pgPort}/crm_synthetic`;
  command(process.execPath,['scripts/setup-db.js'],{...process.env,ADMIN_DATABASE_URL:ownerUrl,
    DATABASE_URL:appUrl,APP_DB_PASSWORD:dbPassword,DEMO_PASSWORD:demoPassword,ALLOW_SYNTHETIC_SETUP:'yes'});
  admin=new pg.Pool({connectionString:ownerUrl});
  work: {
  if(process.argv.includes('--full-tests')){
    const {readdirSync}=await import('node:fs');
    const files=readdirSync('test').filter(name=>name.endsWith('.test.js')).map(name=>'test/'+name);
    command(process.execPath,['--test','--test-concurrency=1',...files],{...process.env,
      ADMIN_DATABASE_URL:ownerUrl,DATABASE_URL:appUrl,ALLOW_SYNTHETIC_SETUP:'yes',ALLOW_ISOLATED_DEMO:'yes',DEMO_PASSWORD:demoPassword});
    break work;
  }
  if(process.argv.includes('--hosted-smoke')){
    db=new pg.Pool({connectionString:appUrl,max:10});
    await checkHostedDemo(admin,db,demoPassword,servers);
    break work;
  }
  const {mapping,invitationCode}=await seed();
  db=new pg.Pool({connectionString:appUrl,max:10});
  await writeFile(join(directory,'fixture-ids.json'),JSON.stringify(mapping,null,2));
  await writeFile(join(directory,'demo-password.txt'),demoPassword+'\n',{mode:0o600});
  await writeFile(join(directory,'invitation-code.txt'),invitationCode+'\n',{mode:0o600});
  await attachmentFixtures();

  const inbox=testInbox({domain:fixture.identity.domain,acceptAddress:umakDemo?demoAddress:undefined});
  servers.push(inbox.server);
  const smtpPort=await listen(inbox.server);
  const verification=configuredVerification({
    CRM_SMTP_URL:`smtp://${host}:${smtpPort}`,CRM_VERIFICATION_FROM:fixture.identity.sender,
    CRM_INSTITUTIONAL_EMAIL_DOMAINS:fixture.identity.domain,CRM_VERIFICATION_KEY:randomBytes(32).toString('hex')
  });
  if(umakDemo)verification.acceptAddress=demoAddress;
  const mailbox=createServer((_req,res)=>{
    res.writeHead(200,{'Content-Type':'text/plain; charset=utf-8','Cache-Control':'no-store'});
    res.end(inbox.messages.map(message=>message.text).join('\n\n---\n\n')||'No synthetic mail received.\n');
  });
  servers.push(mailbox);
  const mailboxPort=await listen(mailbox);
  const app=createApp(db,{verification,scanner:async()=>true});
  const cert=process.env.CRM_SYNTHETIC_TLS_CERT;
  const key=process.env.CRM_SYNTHETIC_TLS_KEY;
  if(Boolean(cert)!==Boolean(key))throw Error('Provide both synthetic API TLS files together');
  const api=cert?https.createServer({cert:await readFile(cert),key:await readFile(key)},app):createServer(app);
  servers.push(api);
  const apiPort=await listen(api);
  const apiUrl=`${cert?'https':'http'}://${host}:${apiPort}`;
  const {rows:[contentManager]}=await admin.query('SELECT id FROM users WHERE email=$1',[fixture.identity.people.find(person=>person.key==='content').email]);
  const syntheticForm=await readFile(join(directory,'demo-form.pdf'));
  for(const [key,state] of [['publishedInApp','published'],['publishedExternal','published'],
    ['publishedSimple','published'],['draft','draft'],['withdrawn','withdrawn']]){
    const sample=fixture.catalog[key];
    const service=await admin.query('INSERT INTO crm_services(code) VALUES($1) RETURNING id',[sample.code]);
    const details={
      name:sample.name,description:'Synthetic CRM service for acceptance testing.',
      purpose:'Exercise the published service workflow.',eligibility:'Student and faculty researchers.',
      applicants:['Student','Faculty'],contact_name:'Demo CRM',contact_email:fixture.identity.sender,
      turnaround:'3–5 business days',channel:sample.channel??'in_app',
      instructions:[sample.channel==='external'?'Contact the demo CRM mailbox to submit externally.':'Download the current blank form.'],
      requirements:(sample.requirements??[]).map(item=>({title:item.label,required:item.required}))
    };
    const previous=key==='publishedInApp';
    for(const version of previous?[1,2]:[1]){
      const revisionState=previous&&version===1?'superseded':state;
      const form=previous?syntheticForm:null;
      const revision=await admin.query(`INSERT INTO crm_service_revisions
        (service_id,version,state,details,form_name,form_mime,form_bytes,form_updated_at,published_at)
        VALUES($1,$2,$3,$4,$5,$6,$7,CASE WHEN $7::bytea IS NOT NULL THEN now() END,
          CASE WHEN $3<>'draft' THEN now() END) RETURNING id`,
        [service.rows[0].id,version,revisionState,details,form?'demo-form.pdf':null,form?'application/pdf':null,form]);
      await admin.query('INSERT INTO crm_service_history(revision_id,actor_id,action) VALUES($1,$2,$3)',
        [revision.rows[0].id,contentManager.id,revisionState==='draft'?'draft_created':revisionState]);
    }
  }

  const d1Revision=(await admin.query(`SELECT v.id FROM crm_service_revisions v JOIN crm_services s ON s.id=v.service_id
    WHERE s.code='SVC-DEMO-A' AND v.state='published'`)).rows[0].id;
  for(const ticket of fixture.D1.requests){
    await admin.query(`INSERT INTO crm_service_requests(requester_id,revision_id,study_id,requester_category,
      requester_college,subject,description,submission_token,fingerprint,status,submitted_at)
      VALUES($1,$2,$3,'student',$4,'Synthetic D1 service request','Synthetic source',$5,'synthetic_d1',$6,$7)`,
      [mapping.people[ticket.owner],d1Revision,mapping.studies[ticket.study],fixture.edgeCases.college,
        randomUUID(),ticket.status,ticket.submittedAt]);
  }

  if(umakDemo){
    const email='student.demo99@umak.edu.ph';
    const probe=createServer(app);servers.push(probe);
    const probePort=await listen(probe);
    const request=async(path,body)=>{
      const response=await fetch('http://'+host+':'+probePort+path,{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify(body)});
      return {status:response.status,body:response.status===204?null:await response.json()};
    };
    const password=demoPassword;
    assert.equal((await request('/api/v1/auth/researchers/register',{email,password,confirm_password:password,
      display_name:'Demo Signup Verification',college:'Demo College',role:'student'})).status,201);
    assert.equal(inbox.messages.at(-1).recipient,email);
    const code=inbox.messages.at(-1).text.match(/verification code is (\d{6})/)[1];
    assert.equal((await request('/api/v1/auth/researchers/verify',{email,code})).status,204);
    assert.equal((await request('/api/v1/auth/login',{email,password})).status,200);
    assert.equal((await request('/api/v1/auth/researchers/register',{email:'real.person@umak.edu.ph',password,
      confirm_password:password,display_name:'Disallowed',college:'Demo College',role:'student'})).status,422);
    await writeFile(join(directory,'demo-accounts.json'),JSON.stringify({password:demoPassword,accounts:fixture.identity.people},null,2),{mode:0o600});
    console.log('UMak synthetic signup, inbox verification, sign-in and reserved-address checks passed.');
  }
  console.log(`Synthetic API: ${apiUrl}\nMailbox: http://${host}:${mailboxPort}/\nFixtures and demo password: ${directory}`);
  if(process.argv.includes('--smoke')){
    if(cert)throw Error('Run the HTTP smoke check without optional TLS files');
    const send=async(path,body,expected)=>{
      const response=await fetch(apiUrl+path,{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify(body)});
      assert.equal(response.status,expected);
      return response.status===204?{}:response.json();
    };
    const credentials={email:fixture.identity.freshSignup,password:demoPassword};
    const root='/api/v1/auth';
    const login=(email,expected)=>send(root+'/login',{email,password:demoPassword},expected);
    const catalog='/api/v1/public/services';
    const services=await (await fetch(apiUrl+catalog)).json();
    assert.deepEqual(services.map(row=>row.code),['SVC-DEMO-A','SVC-DEMO-B','SVC-DEMO-SIMPLE']);
    assert.equal(services[0].version,2);
    const blank=await fetch(apiUrl+catalog+'/'+services[0].id+'/form');
    assert.equal(blank.status,200);
    assert.deepEqual(Buffer.from(await blank.arrayBuffer()),syntheticForm);
    const studentAccess=await login('demo.student@example.test',200);
    const unrelated=await login('demo.other@example.test',200);
    const reviewer=await login('demo.processing@example.test',200);
    const member=await login('demo.member@example.test',200);
    const registrar=await login('demo.registry@example.test',200);
    const progressEditor=await login('demo.progress@example.test',200);
    const requestPath='/api/v1/service-requests';
    const staffRequestPath='/api/v1/staff/service-requests';
    const apiCall=(token,path,method='GET',body,headers={})=>fetch(apiUrl+path,{
      method,headers:{Authorization:'Bearer '+token,...headers},body
    });
    const jsonCall=(token,path,body)=>apiCall(token,path,'POST',JSON.stringify(body),{'Content-Type':'application/json'});
    await admin.query('DELETE FROM login_attempts'); // Isolate independent authentication scenarios.
    // Exercise real account, registry and report endpoints using restricted staff identities.
    const administrator=await login('demo.admin@example.test',200);
    const reporter=await login('demo.reporting@example.test',200);
    const d1Research=await (await jsonCall(reporter.access_token,'/api/v1/staff/reports',{type:'research'})).json();
    assert.deepEqual(d1Research.metrics.map(row=>row[1]),[2,1,1,0,3]);
    const d1Student=await (await jsonCall(reporter.access_token,'/api/v1/staff/reports',{type:'research',category:'student'})).json();
    assert.deepEqual(d1Student.metrics.map(row=>row[1]),[1,1,1,0,2]);
    const d1Service=await (await jsonCall(reporter.access_token,'/api/v1/staff/reports',{type:'service'})).json();
    assert.deepEqual(d1Service.metrics.map(row=>row[1]),[2,1,1]);
    assert.equal(d1Service.service_counts[0][1],2);
    const accountsPath='/api/v1/staff/accounts';
    const directoryResponse=await (await apiCall(administrator.access_token,accountsPath)).json();
    const adminAccount=directoryResponse.find(account=>account.email==='demo.admin@example.test');
    assert.equal((await jsonCall(administrator.access_token,accountsPath+'/'+adminAccount.id+'/state',
      {active:false,confirmed:true})).status,400);
    assert.equal((await jsonCall(administrator.access_token,accountsPath+'/'+adminAccount.id+'/permissions',
      {permissions:[]})).status,400);
    const studentAccount=directoryResponse.find(account=>account.email==='demo.student@example.test');
    assert.equal((await jsonCall(administrator.access_token,accountsPath+'/'+studentAccount.id+'/permissions',
      {permissions:['reporting']})).status,400);
    assert.equal((await jsonCall(administrator.access_token,accountsPath+'/'+studentAccount.id+'/category',
      {category:'faculty'})).status,200);
    const savedCategory=await (await jsonCall(reporter.access_token,'/api/v1/staff/reports',{type:'research',category:'student'})).json();
    assert.deepEqual(savedCategory.metrics.map(row=>row[1]),[1,1,1,0,2]);
    assert.equal((await jsonCall(administrator.access_token,accountsPath+'/'+studentAccount.id+'/category',
      {category:'student'})).status,200);
    const historyResponse=await apiCall(administrator.access_token,accountsPath+'/'+studentAccount.id+'/history');
    assert.equal(historyResponse.status,200);
    const historyBody=await historyResponse.text();
    assert.equal(JSON.parse(historyBody).length,2);
    assert(!historyBody.includes('password_hash'));
    const lockedEmail=await apiCall(studentAccess.access_token,'/api/v1/users/me','PATCH',
      JSON.stringify({email:'changed@example.test'}),{'Content-Type':'application/json'});
    assert.equal(lockedEmail.status,403);

    assert.equal((await apiCall(studentAccess.access_token,accountsPath)).status,403);
    const provision=await jsonCall(administrator.access_token,accountsPath,{
      email:'provisioned@example.test',display_name:'Provisioned Reporter',college:'Demo College',permissions:['reporting']
    });
    assert.equal(provision.status,201,await provision.clone().text());
    const createdAccount=await provision.json();
    const challenge=await send(root+'/login',{email:'provisioned@example.test',password:createdAccount.temporary_credential},200);
    assert.equal(challenge.requires_password_change,true);
    assert.equal(challenge.access_token,undefined);
    await send(root+'/replace-temporary-password',{challenge:challenge.challenge,
      password:createdAccount.temporary_credential,confirmation:createdAccount.temporary_credential},400);
    const newStaffPassword='Synthetic newStaffPassword password 2026!';
    await send(root+'/replace-temporary-password',{challenge:challenge.challenge,password:newStaffPassword,confirmation:newStaffPassword},204);
    await send(root+'/replace-temporary-password',{challenge:challenge.challenge,password:newStaffPassword,confirmation:newStaffPassword},400);
    const provisioned=await send(root+'/login',{email:'provisioned@example.test',password:newStaffPassword},200);
    assert.equal((await apiCall(provisioned.access_token,accountsPath)).status,403);
    const renewal=await jsonCall(provisioned.access_token,root+'/activity',{});
    assert.equal(renewal.status,200);
    const renewed=await renewal.json();
    assert.equal(renewed.expires_in,1800);

    assert.equal((await jsonCall(administrator.access_token,accountsPath+'/'+createdAccount.account.id+'/state',
      {active:false,confirmed:true})).status,200);
    assert.equal((await apiCall(renewed.access_token,'/api/v1/users/me')).status,401);
    const registryPath='/api/v1/staff/registry';
    assert.equal((await apiCall(reviewer.access_token,registryPath+'/candidate?email=demo.student%40example.test')).status,403);
    const registryCandidate=await (await apiCall(registrar.access_token,registryPath+'/candidate?email=demo.student%40example.test')).json();
    const registryEntry={title:'Synthetic registry acceptance study',college:'Demo College',academic_period:'AY 2026-2027',
      adviser:'Demo Adviser',registrant_id:registryCandidate.id,member_ids:[registryCandidate.id],
      reason:'Synthetic source entry',submission_token:randomUUID()};
    const entryResponse=await jsonCall(registrar.access_token,registryPath,registryEntry);
    assert.equal(entryResponse.status,201,await entryResponse.clone().text());
    const entry=await entryResponse.json();
    const registryCorrection={...registryEntry,expected_version:entry.version,title:'Corrected synthetic registry study',reason:'Correct source title'};
    const registryCorrected=await jsonCall(registrar.access_token,registryPath+'/'+entry.id+'/corrections',registryCorrection);
    assert.equal(registryCorrected.status,200,await registryCorrected.clone().text());
    assert.equal((await registryCorrected.json()).history.length,2);
    assert.equal((await jsonCall(registrar.access_token,registryPath+'/'+entry.id+'/corrections',registryCorrection)).status,409);
    const duplicateEntry=await jsonCall(registrar.access_token,registryPath,{...registryEntry,title:registryCorrection.title,submission_token:randomUUID()});
    assert.equal(duplicateEntry.status,409,await duplicateEntry.clone().text());
    const documentQuery=new URLSearchParams({filename:'demo-form.pdf',upload_token:randomUUID()});
    const fileSave=await apiCall(registrar.access_token,registryPath+'/'+entry.id+'/documents?'+documentQuery,'POST',syntheticForm,
      {'Content-Type':'application/octet-stream'});
    assert.equal(fileSave.status,201,await fileSave.clone().text());
    const documents=await (await apiCall(registrar.access_token,registryPath+'/'+entry.id+'/documents')).json();
    assert.equal(documents.length,1);
    const nextDocument=new URLSearchParams({filename:'revised-form.pdf',upload_token:randomUUID(),previous_id:documents[0].id});
    const nextFile=await apiCall(registrar.access_token,registryPath+'/'+entry.id+'/documents?'+nextDocument,'POST',syntheticForm,
      {'Content-Type':'application/octet-stream'});
    assert.equal(nextFile.status,201,await nextFile.clone().text());
    assert.equal((await apiCall(registrar.access_token,registryPath+'/'+entry.id+'/documents?'+nextDocument,'POST',syntheticForm,
      {'Content-Type':'application/octet-stream'})).status,201);
    const oldBranch=new URLSearchParams({filename:'stale-form.pdf',upload_token:randomUUID(),previous_id:documents[0].id});
    assert.equal((await apiCall(registrar.access_token,registryPath+'/'+entry.id+'/documents?'+oldBranch,'POST',syntheticForm,
      {'Content-Type':'application/octet-stream'})).status,409);
    const savedVersions=await (await apiCall(studentAccess.access_token,'/api/v1/researcher/registrations/'+entry.id+'/documents')).json();
    assert.deepEqual(savedVersions.map(file=>file.version),[2,1]);
    const historicalResponse=await jsonCall(registrar.access_token,registryPath,{...registryEntry,title:'Historical synthetic record',
      historical:true,college:'',academic_period:'',adviser:'',source_reference:'DEMO-SOURCE-001',submission_token:randomUUID()});
    assert.equal(historicalResponse.status,201,await historicalResponse.clone().text());
    const historicalRecord=await historicalResponse.json();
    assert.equal(historicalRecord.original_registration_date,null);
    assert.equal(historicalRecord.historical,true);
    const downloadPath=registryDownloadPath(entry.id,documents[0].id);
    assert.equal((await apiCall(studentAccess.access_token,downloadPath)).status,200);
    assert.equal((await apiCall(unrelated.access_token,downloadPath)).status,404);
    const reportResponse=await jsonCall(reporter.access_token,'/api/v1/staff/reports',{type:'research',researcher:String(registryCandidate.id)});
    assert.equal(reportResponse.status,200,await reportResponse.clone().text());
    const report=await reportResponse.json();
    assert(report.rows.some(row=>row[0]==='REG-'+entry.id));
    assert.equal((await jsonCall(reviewer.access_token,'/api/v1/staff/reports',{type:'research'})).status,403);
    function registryDownloadPath(study,file){return '/api/v1/researcher/registrations/'+study+'/documents/'+file;}
    await admin.query('DELETE FROM login_attempts'); // Begin signup scenarios with a fresh limiter window.
    const registrationPath='/api/v1/researcher/registrations';
    const candidateResponse=await apiCall(studentAccess.access_token,
      registrationPath+'/team-candidate?email=demo.member%40example.test');
    assert.equal(candidateResponse.status,200);
    const candidate=await candidateResponse.json();
    const draftResponse=await jsonCall(studentAccess.access_token,'/api/v1/researcher/drafts',
      {title:'Unsubmitted practice draft',department:'College of Science'});
    assert.equal(draftResponse.status,201);
    const registrationDraft=await draftResponse.json();
    const registration={submission_token:randomUUID(),draft_id:registrationDraft.id,
      title:'Private registered study',college:'College of Science',academic_period:'AY 2026-2027',
      adviser:'Demo Adviser',member_ids:[candidate.id],declarations:['Synthetic declaration']};
    assert.equal((await jsonCall(studentAccess.access_token,registrationPath,{...registration,adviser:''})).status,400);
    assert.equal((await jsonCall(studentAccess.access_token,registrationPath,
      {...registration,member_ids:[candidate.id,candidate.id]})).status,400);
    const registeredResponse=await jsonCall(studentAccess.access_token,registrationPath,registration);
    assert.equal(registeredResponse.status,201);
    const registered=await registeredResponse.json();
    assert.equal(registered.id,registrationDraft.id);
    assert.equal(registered.stage,'Registered');
    assert.ok(registered.registered_at);
    const registrationRetry=await jsonCall(studentAccess.access_token,registrationPath,registration);
    assert.equal(registrationRetry.status,200);
    assert.equal((await registrationRetry.json()).id,registered.id);
    const registeredDetails=registrationPath+'/'+registered.id;
    const forMember=await apiCall(member.access_token,registeredDetails);
    assert.equal(forMember.status,200);
    assert.equal((await forMember.json()).team.length,2);
    assert.equal((await apiCall(unrelated.access_token,registeredDetails)).status,404);
    const privateSearch=await fetch(apiUrl+'/api/v1/research/search',{
      method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({keyword:registration.title})
    });
    assert.ok(!(await privateSearch.json()).some(row=>row.id===registered.id));
    const permittedRegistry=await apiCall(registrar.access_token,'/api/v1/staff/research-registrations?keyword=Demo%20Member');
    assert.ok((await permittedRegistry.json()).some(row=>row.id===registered.id));
    assert.equal((await apiCall(reviewer.access_token,'/api/v1/staff/research-registrations')).status,403);
    const progressPath='/api/v1/staff/research-progress/'+registered.id;
    assert.equal((await apiCall(reviewer.access_token,progressPath)).status,403);
    const initialProgress=await (await apiCall(progressEditor.access_token,progressPath)).json();
    assert.equal(initialProgress.stage,'Registered');
    const firstStage={kind:'stage',expected_version:initialProgress.version,stage:'In-Progress',
      effective_date:'2026-11-01',supporting_details:'Demo progress evidence',
      next_action:'Continue data collection',responsible_contact:'Demo CRM',internal_note:'Synthetic private progress note'};
    assert.equal((await jsonCall(progressEditor.access_token,progressPath+'/events',
      {...firstStage,stage:'Completed',completion_date:'2026-11-01'})).status,400);
    assert.equal((await jsonCall(progressEditor.access_token,progressPath+'/events',
      {...firstStage,effective_date:null})).status,400);
    const moved=await jsonCall(progressEditor.access_token,progressPath+'/events',firstStage);
    assert.equal(moved.status,200);
    let savedProgress=await moved.json();
    assert.equal(savedProgress.stage,'In-Progress');
    assert.equal((await jsonCall(progressEditor.access_token,progressPath+'/events',firstStage)).status,409);
    const privateProgress=await (await apiCall(member.access_token,registeredDetails+'/progress')).json();
    assert.equal(privateProgress.history[0].internal_note,undefined);
    assert.equal(privateProgress.next_action,firstStage.next_action);
    const milestoneEvent={kind:'milestone',expected_version:savedProgress.version,
      milestone_code:'collection',status:'In progress',effective_date:'2026-11-02',evidence:'DEMO-MILESTONE-001'};
    const milestoneResponse=await jsonCall(progressEditor.access_token,progressPath+'/events',milestoneEvent);
    assert.equal(milestoneResponse.status,200);
    savedProgress=await milestoneResponse.json();
    assert.equal(savedProgress.milestones.find(row=>row.code==='collection').status,'In progress');
    const complete=await jsonCall(progressEditor.access_token,progressPath+'/events',{
      kind:'stage',expected_version:savedProgress.version,stage:'Completed',effective_date:'2026-11-03',
      reference:'DEMO-COMPLETION-001',no_next_action:true,completion_date:'2026-11-03',
      completion_details:'Demo completed study'
    });
    assert.equal(complete.status,200);
    savedProgress=await complete.json();
    assert.equal(savedProgress.stage,'Completed');
    assert.equal(savedProgress.presentation.date,null);
    assert.equal(savedProgress.publication.date,null);
    const presentation=await jsonCall(progressEditor.access_token,progressPath+'/events',{
      kind:'presentation',expected_version:savedProgress.version,effective_date:'2026-11-04',event:'Demo Research Forum'
    });
    assert.equal(presentation.status,200);
    savedProgress=await presentation.json();
    assert.equal(savedProgress.presentation.event,'Demo Research Forum');
    const completionCorrection=await jsonCall(progressEditor.access_token,progressPath+'/events',{
      kind:'completion_correction',expected_version:savedProgress.version,reopen:true,reason:'Demo correction'
    });
    assert.equal(completionCorrection.status,200);
    savedProgress=await completionCorrection.json();
    assert.equal(savedProgress.stage,'In-Progress');
    assert.equal(savedProgress.completion.date,null);
    assert.equal(savedProgress.history.length,5);
    assert.equal((await apiCall(unrelated.access_token,registeredDetails+'/progress')).status,404);
    const planningPath=progressPath+'/planning';
    const unconfigured=await apiCall(progressEditor.access_token,planningPath);
    assert.equal((await unconfigured.json()).configured,false);
    const workflow={tasks:savedProgress.milestones.map((item,index)=>({
      code:item.code,duration_minutes:30*(index+1),
      prerequisites:index===0?[]:[savedProgress.milestones[index-1].code],requirements:[]
    }))};
    const cyclic=structuredClone(workflow);
    cyclic.tasks[0].prerequisites=[cyclic.tasks.at(-1).code];
    assert.equal((await jsonCall(progressEditor.access_token,progressPath+'/workflow',
      {expected_version:savedProgress.version,definition:cyclic})).status,400);
    assert.equal((await jsonCall(reviewer.access_token,progressPath+'/workflow',
      {expected_version:savedProgress.version,definition:workflow})).status,403);
    const configured=await jsonCall(progressEditor.access_token,progressPath+'/workflow',
      {expected_version:savedProgress.version,definition:workflow});
    assert.equal(configured.status,200);
    const planned=await configured.json();
    assert.equal(planned.remaining_duration_minutes,450);
    assert.equal(planned.ready[0].code,'proposal');
    assert.ok(planned.blocked.find(item=>item.code==='collection'));
    assert.equal((await apiCall(unrelated.access_token,registeredDetails+'/planning')).status,404);
    const linkedPlan=await (await apiCall(member.access_token,registeredDetails+'/planning')).json();
    assert.deepEqual(linkedPlan.critical_path,planned.critical_path);
    const blockedStart=await jsonCall(progressEditor.access_token,progressPath+'/events',{
      kind:'milestone',expected_version:planned.version,milestone_code:'collection',status:'Completed'
    });
    assert.equal(blockedStart.status,400);
    const finishProposal=await jsonCall(progressEditor.access_token,progressPath+'/events',{
      kind:'milestone',expected_version:planned.version,milestone_code:'proposal',status:'Completed'
    });
    assert.equal(finishProposal.status,200);
    const afterProposal=await finishProposal.json();
    const withEvidence=structuredClone(workflow);
    withEvidence.tasks[1].requirements=[{id:'ethics',gate:'verified'}];
    const evidenceConfig=await jsonCall(progressEditor.access_token,progressPath+'/workflow',
      {expected_version:afterProposal.version,definition:withEvidence});
    assert.equal(evidenceConfig.status,200);
    const gated=await evidenceConfig.json();
    assert.ok(gated.blocked.find(item=>item.code==='ethics').reasons.some(reason=>reason.includes('verified evidence')));
    const verifyEvidence=await jsonCall(progressEditor.access_token,progressPath+'/events',{
      kind:'milestone',expected_version:gated.version,milestone_code:'ethics',status:'Pending',
      evidence:'DEMO-ETHICS-VERIFIED',evidence_state:'verified'
    });
    assert.equal(verifyEvidence.status,200);
    const afterEvidence=await (await apiCall(member.access_token,registeredDetails+'/planning')).json();
    assert.equal(afterEvidence.ready[0].code,'ethics');
    assert.equal(afterEvidence.remaining_duration_minutes,420);
    assert.equal((await jsonCall(progressEditor.access_token,progressPath+'/workflow',
      {expected_version:planned.version,definition:workflow})).status,409);
    const upload=(token)=>apiCall(token,requestPath+'/files?filename=demo-form.pdf&requirement=0&attempt_token='+randomUUID(),
      'POST',syntheticForm,{'Content-Type':'application/octet-stream'});
    const uploaded=await upload(studentAccess.access_token);
    assert.equal(uploaded.status,201);
    const file=await uploaded.json();
    const submission={revision_id:services[0].id,study_id:mapping.studies['RES-DEMO-001'],subject:'Synthetic request',
      submission_token:randomUUID(),files:[file.id]};
    const first=await jsonCall(studentAccess.access_token,requestPath,submission);
    for(const invalid of [{subject:'x'.repeat(161)},{description:'x'.repeat(4001)}]){
      const bad=await jsonCall(studentAccess.access_token,requestPath,{...submission,...invalid});
      assert.equal(bad.status,400);
      const failure=await bad.json();
      assert.equal(failure.error,'Invalid request');
      assert.ok(failure.field_errors[Object.keys(invalid)[0]]);
    }
    assert.equal(first.status,201);
    const submitted=await first.json();
    assert.equal(submitted.status,'Submitted');
    assert.equal((await (await jsonCall(studentAccess.access_token,requestPath,submission)).json()).id,submitted.id);
    const requestDetail=requestPath+'/'+submitted.id;
    assert.equal((await apiCall(unrelated.access_token,requestDetail)).status,404);
    assert.equal((await apiCall(unrelated.access_token,requestDetail+'/files/'+file.id)).status,404);
    const reviewPath=staffRequestPath+'/'+submitted.id+'/reviews';
    const decision={expected_version:submitted.version,status:'In review',checklist:['Verified','Not applicable'],
      feedback:'Synthetic visible feedback',internal_remark:'Synthetic internal review text'};
    assert.equal((await jsonCall(reviewer.access_token,reviewPath,decision)).status,200);
    assert.equal((await jsonCall(reviewer.access_token,reviewPath,decision)).status,409);
    const researcherDetail=await (await apiCall(studentAccess.access_token,requestDetail)).json();
    assert.equal(researcherDetail.reviews[0].feedback,decision.feedback);
    assert.equal(researcherDetail.reviews[0].internal_remark,undefined);
    assert.equal(researcherDetail.attachments[0].id,file.id);
    const reviewStaffDetail=await (await apiCall(reviewer.access_token,staffRequestPath+'/'+submitted.id)).json();
    assert.equal(reviewStaffDetail.reviews[0].internal_remark,decision.internal_remark);
    const correction={expected_version:researcherDetail.version,status:'For correction',checklist:['Needs correction','Not applicable'],
      missing_items:[0],next_action:'Replace the supporting form',
      deadline_at:new Date(Date.now()+60*60*1000).toISOString()};
    const correctionResponse=await jsonCall(reviewer.access_token,reviewPath,correction);
    assert.equal(correctionResponse.status,200);
    const corrected=await correctionResponse.json();
    const notices=await (await apiCall(studentAccess.access_token,requestPath+'/updates')).json();
    assert.ok(notices.some(event=>event.kind==='deadline'&&event.request_id===submitted.id));
    const replacement=await upload(studentAccess.access_token);
    assert.equal(replacement.status,201);
    const replacementFile=await replacement.json();
    const redo={expected_version:corrected.version,files:[replacementFile.id]};
    const redone=await jsonCall(studentAccess.access_token,requestDetail+'/resubmit',redo);
    assert.equal(redone.status,200);
    const resubmitted=await redone.json();
    assert.equal(resubmitted.id,submitted.id);
    assert.equal(resubmitted.status,'Submitted');
    assert.equal((await (await jsonCall(studentAccess.access_token,requestDetail+'/resubmit',redo)).json()).resubmitted_at,
      resubmitted.resubmitted_at);
    const afterRedo=await (await apiCall(studentAccess.access_token,requestDetail)).json();
    assert.equal(afterRedo.attachments[0].id,replacementFile.id);
    assert.equal(afterRedo.reviews.length,2);
    const manager=await login('demo.content@example.test',200);
    const limited=await login('demo.limited@example.test',200);
    const staffPath='/api/v1/staff/services';
    const staffCall=(token,path,method='GET',body,headers={})=>fetch(apiUrl+staffPath+path,{
      method,headers:{Authorization:'Bearer '+token,...headers},body
    });
    const staffJson=(path,object)=>staffCall(manager.access_token,path,'POST',JSON.stringify(object),
      {'Content-Type':'application/json'});
    assert.equal((await staffCall(limited.access_token,'')).status,403);
    const retained=(await (await staffCall(manager.access_token,'')).json()).find(row=>row.state==='superseded');
    assert.equal((await fetch(apiUrl+catalog+'/'+retained.id+'/form')).status,404);
    assert.equal((await staffCall(manager.access_token,'/'+retained.id+'/form')).status,200);
    const created=await staffJson('/drafts',{code:'SVC-DEMO-A',details:services[0].details});
    assert.equal(created.status,201);
    const draft=await created.json();
    assert.equal((await fetch(apiUrl+catalog+'/'+draft.id+'/form')).status,404);
    assert.equal((await staffCall(manager.access_token,'/'+draft.id+'/form','PUT',syntheticForm,
      {'Content-Type':'application/octet-stream','X-Form-Name':'demo-form-v3.pdf'})).status,200);
    assert.equal((await staffJson('/'+draft.id+'/publish',{confirmed:false})).status,400);
    assert.equal((await staffJson('/'+draft.id+'/publish',{confirmed:true})).status,200);
    assert.equal((await fetch(apiUrl+catalog+'/'+services[0].id+'/form')).status,404);
    assert.equal((await fetch(apiUrl+catalog+'/'+draft.id+'/form')).status,200);
    assert.equal((await staffJson('/'+draft.id+'/withdraw',{confirmed:true})).status,200);
    assert.equal((await fetch(apiUrl+catalog+'/'+draft.id+'/form')).status,404);
    const audit=await (await staffCall(manager.access_token,'/'+draft.id+'/history')).json();
    assert.deepEqual(audit.slice(0,5).map(row=>row.action),
      ['withdrawn','published','superseded','form_replaced','draft_created']);
    const incomplete=await staffJson('/drafts',{code:'SVC-DEMO-INCOMPLETE',details:{name:'Unfinished draft'}});
    assert.equal(incomplete.status,201);
    const unfinished=await incomplete.json();
    const incompletePublish=await staffJson('/'+unfinished.id+'/publish',{confirmed:true});
    assert.equal(incompletePublish.status,400);
    const incompleteBody=await incompletePublish.json();
    assert.equal(incompleteBody.error,'Complete the highlighted service information before publishing.');
    assert.ok(Object.keys(incompleteBody.field_errors).length>0);
    assert.equal((await fetch(apiUrl+catalog+'/'+unfinished.id+'/form')).status,404);
    assert.equal((await staffJson('/'+unfinished.id+'/edit',services[0].details)).status,200);
    assert.equal((await staffJson('/'+unfinished.id+'/publish',{confirmed:true})).status,200);
    assert.equal((await login('demo.inactive@example.test',403)).code,'account_unavailable');
    assert.equal((await login('demo.unverified@example.test',403)).code,'email_unverified');
    await send(root+'/researchers/resend',{email:'demo.unverified@example.test'},200);
    assert.equal(inbox.messages.at(-1).recipient,'demo.unverified@example.test');
    await send(root+'/activate',{token:invitationCode,password:demoPassword},204);
    assert.ok((await login('demo.invite@example.test',200)).access_token);
    await send(root+'/researchers/register',{...credentials,confirm_password:demoPassword,
      display_name:'Synthetic Signup',college:fixture.edgeCases.college,role:'student'},201);
    assert.equal((await send(root+'/login',credentials,403)).code,'email_unverified');
    assert.equal(inbox.messages.at(-1).recipient,credentials.email);
    const code=inbox.messages.at(-1).text.match(/verification code is (\d{6})/)[1];
    await send(root+'/researchers/verify',{email:credentials.email,code},204);
    assert.ok((await send(root+'/login',credentials,200)).access_token);
    assert.ok((await (await fetch(`http://${host}:${mailboxPort}/`)).text()).includes(inbox.messages.at(-1).text));
    const student=await login('demo.student@example.test',200);
    const history=await fetch(apiUrl+'/api/v1/consultations',{headers:{Authorization:'Bearer '+student.access_token}});
    assert.equal(history.status,200);
    const rows=await history.json();
    assert.deepEqual(rows.map(row=>row.id).sort(),Object.values(mapping.consultations).sort());
    const expected=fixture.D1.expected;
    const aggregate=await admin.query(`SELECT count(DISTINCT r.id)::int AS studies,
      count(DISTINCT r.id) FILTER (WHERE r.lifecycle_state='Completed')::int AS completed,
      count(DISTINCT m.user_id)::int AS researchers
      FROM research_records r JOIN study_members m ON m.study_id=r.id
      WHERE r.id=ANY($1::int[])`,[Object.values(mapping.studies)]);
    assert.deepEqual(aggregate.rows[0],{studies:expected.studies,
      completed:expected.completedStudies,researchers:expected.distinctResearchers});
    assert.equal(rows.length,expected.requests);
    assert.equal(rows.filter(row=>row.status==='resolved').length,expected.completedRequests);
    assert.equal(rows.filter(row=>row.status==='claimed').length,expected.pendingRequests);
    command(process.execPath,['--test','test/researcher-registration.test.js','test/workflow-planning.test.js','test/session-tokens.test.js'],
      {...process.env,ADMIN_DATABASE_URL:ownerUrl,DATABASE_URL:appUrl,ALLOW_SYNTHETIC_SETUP:'yes'});
    console.log('Synthetic signup, catalog, registrations, requests, workflow planning, account administration, registry documents and D1 reporting checks passed.');
  }else{
    console.log('Press Ctrl+C to stop the isolated services.');
    await Promise.race(['SIGINT','SIGTERM'].map(signal=>once(process,signal)));
  }
  }
}finally{
  await Promise.all(servers.map(server=>new Promise(resolve=>{
    server.close(resolve);
    server.closeAllConnections?.();
  })));
  await db?.end();
  await admin?.end();
  if(running)command('pg_ctl',['-D',data,'-m','fast','-w','stop']);
  if(process.argv.includes('--smoke')||process.argv.includes('--hosted-smoke')||process.argv.includes('--full-tests'))await rm(directory,{recursive:true,force:true});
}
