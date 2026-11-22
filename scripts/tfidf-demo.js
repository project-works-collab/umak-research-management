import {spawn} from 'node:child_process';
import {once} from 'node:events';
import {resolve} from 'node:path';
import {createServer} from 'node:net';
import {randomBytes} from 'node:crypto';
import assert from 'node:assert/strict';
import request from 'supertest';

export const tfidfScenario={
  title:'Campus Rainwater Harvesting Monitoring System',
  abstract:'This study designs a campus rainwater harvesting monitoring system using water level sensors and a dashboard to measure tank storage and reduce freshwater consumption.'
};

// Only called by the isolated synthetic fixture. Never provisions a deployed database.
export async function startTfidfDemo(admin,ownerUrl){
  const password=randomBytes(24).toString('hex');
  const token=randomBytes(32).toString('hex');
  await admin.query(`ALTER ROLE crm_tfidf LOGIN PASSWORD '${password}'`);
  const databaseUrl=new URL(ownerUrl);
  databaseUrl.username='crm_tfidf';databaseUrl.password=password;
  const reservation=createServer();
  await new Promise(resolve=>reservation.listen(0,'127.0.0.1',resolve));
  const port=reservation.address().port;
  await new Promise(resolve=>reservation.close(resolve));
  const url=`http://127.0.0.1:${port}`;
  const child=spawn(process.env.TFIDF_PYTHON??resolve('.private/tfidf-venv/bin/python'),
    ['-m','uvicorn','app:app','--host','127.0.0.1','--port',String(port)],{
      cwd:resolve('services/tfidf'),
      env:{...process.env,TFIDF_DATABASE_URL:databaseUrl.href,TFIDF_SERVICE_TOKEN:token},
      stdio:['ignore','ignore','pipe']
    });
  let failure,log='';
  child.on('error',error=>{failure=error;});
  child.stderr.on('data',chunk=>{log=(log+chunk.toString()).slice(-4000);});
  const stop=async()=>{
    if(child.exitCode===null&&child.signalCode===null&&!failure){
      child.kill('SIGTERM');await once(child,'exit');
    }
  };
  try{
    for(let attempt=0;attempt<150;attempt++){
      if(failure||child.exitCode!==null)throw Error('TF-IDF could not start. Run npm run demo:tfidf:setup. '+(failure?.message??log));
      try{
        const response=await fetch(url+'/ready',{signal:AbortSignal.timeout(1000)});
        if(response.ok){
          process.env.TFIDF_SERVICE_URL=url;process.env.TFIDF_SERVICE_TOKEN=token;
          process.env.TFIDF_REVIEW_ENABLED='true';
          return {stop};
        }
      }catch{}
      await new Promise(resolve=>setTimeout(resolve,200));
    }
    throw Error('TF-IDF readiness timed out: '+log);
  }catch(error){await stop();throw error;}
}

export async function seedTfidfDemo(admin){
  const {rows:[author]}=await admin.query("SELECT id FROM users WHERE role='staff' ORDER BY id LIMIT 1");
  const {rows:reviewers}=await admin.query("SELECT user_id FROM crm_staff_permissions WHERE permission='registry_management'");
  const records=[tfidfScenario,
    {title:tfidfScenario.title,abstract:'An interview study of student theater participation and public speaking confidence using qualitative classroom observations.'},
    {title:'Campus Rainwater Harvesting Sensor Dashboard',abstract:tfidfScenario.abstract}];
  const ids=[];
  for(const record of records){
    const {rows:[row]}=await admin.query(`INSERT INTO research_records
      (author_id,title,abstract,status,research_year,department)
      VALUES($1,$2,$3,'Approved',2026,'Demo College') RETURNING id`,[author.id,record.title,record.abstract]);
    ids.push(row.id);
    for(const reviewer of reviewers)await admin.query('INSERT INTO review_assignments(study_id,staff_id) VALUES($1,$2)',[row.id,reviewer.user_id]);
  }
  return ids;
}

export async function checkTfidfDemo(app,email,password,ids){
  const login=await request(app).post('/api/v1/auth/login').send({email,password}).expect(200);
  const review=body=>request(app).post('/api/v1/research/duplicate-review')
    .set('Authorization','Bearer '+login.body.access_token).send(body);
  const response=await review(tfidfScenario).expect(200);
  const exact=response.body.matches.find(item=>item.id===ids[0]);
  const unrelated=response.body.matches.find(item=>item.id===ids[1]);
  assert.ok(exact?.alert&&exact.tfidf_score>0.99,'Identical study must trigger review');
  assert.ok(unrelated&&!unrelated.alert,'Same title with unrelated abstract must not bypass the threshold');
  assert.ok(response.body.model_version);
  const excluded=await review({...tfidfScenario,exclude_id:ids[0]}).expect(200);
  assert.ok(!excluded.body.matches.some(item=>item.id===ids[0]));
  const empty=await review({title:'Zyzzyva xenolith quasar',abstract:'Unrelated vocabulary outside the synthetic archive.'}).expect(200);
  assert.deepEqual(empty.body.matches,[]);
  const token=process.env.TFIDF_SERVICE_TOKEN;
  try {
    process.env.TFIDF_SERVICE_TOKEN='invalid-demo-token';
    const unavailable=await review(tfidfScenario).expect(503);
    assert.equal(unavailable.body.error,'Duplicate review scoring unavailable');
    assert.equal(unavailable.body.matches,undefined);
  } finally { process.env.TFIDF_SERVICE_TOKEN=token; }
  console.log('TF-IDF real Python/database/API checks passed: matching study, unrelated abstract, exclusion, empty shortlist and explicit scoring failure.');
}
