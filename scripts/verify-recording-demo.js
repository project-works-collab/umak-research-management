import assert from 'node:assert/strict';
import {mkdirSync,writeFileSync} from 'node:fs';
const origin=process.env.HOSTED_DEMO_ORIGIN??'https://umak-crm-api.onrender.com';
if(new URL(origin).protocol!=='https:')throw Error('HTTPS origin required');
const password=process.env.DEMO_PASSWORD;
if(!password)throw Error('DEMO_PASSWORD required');
const checks=[];const sessions=[];
async function call(path,{body,token,expected=200,bytes=false}={}){
 const response=await fetch(origin+path,{method:body===undefined?'GET':'POST',redirect:'error',signal:AbortSignal.timeout(60000),
 headers:{...(body===undefined?{}:{'Content-Type':'application/json'}),...(token?{Authorization:'Bearer '+token}:{})},
 body:body===undefined?undefined:JSON.stringify(body)});
 assert.equal(response.status,expected,`${path}: expected HTTP ${expected}, got ${response.status}`);
 if(expected===204)return null;
 return bytes?Buffer.from(await response.arrayBuffer()):response.json();
}
async function login(role){
 const data=await call('/api/v1/auth/login',{body:{email:`demo.${role}@umak.edu.ph`,password}});
 assert.ok(data.access_token);sessions.push(data.access_token);return data.access_token;
}
try{
 assert.equal((await call('/health')).status,'ok');
 const info=await call('/api/v1/demo/info');assert.equal(info.mode,'synthetic');
 assert.ok(info.tfidf.startsWith('configured'),'TF-IDF is not configured');checks.push('HTTPS API and configured TF-IDF');
 const student=await login('student'),registry=await login('registry'),progress=await login('progress'),limited=await login('limited'),admin=await login('admin');
 checks.push('All five recording accounts sign in');
 const studies=await call('/api/v1/researcher/studies',{token:student});
 const study=studies.find(row=>row.title==='Demo Research A');assert.ok(study,'Demo Research A is missing');
 await call('/api/v1/staff/accounts',{token:limited,expected:403});checks.push('Limited staff access denied');
 const search=await call('/api/v1/research/search',{body:{keyword:'Demo Reserch A'},token:registry});
 assert.ok(search.some(row=>row.id===study.id));checks.push('Misspelled title retrieves Demo Research A');
 const title='Campus Rainwater Harvesting Monitoring System';
 const abstract='This study designs a campus rainwater harvesting monitoring system using water level sensors and a dashboard to measure tank storage and reduce freshwater consumption.';
 const review=await call('/api/v1/research/duplicate-review',{body:{title,abstract},token:registry});
 assert.equal(review.retrieval_scope,'all_authorized_records');assert.equal(review.threshold,0.85);assert.ok(review.scored_count>=3);
 assert.ok(review.matches.some(row=>row.title===title&&row.abstract===abstract&&row.alert&&row.tfidf_score>0.99),'Identical seeded study must alert');
 assert.ok(review.matches.some(row=>row.title===title&&row.abstract!==abstract&&!row.alert),'Unrelated seeded abstract must not alert');
 checks.push('Real TF-IDF: identical study alerts, unrelated abstract stays below threshold');
 const docs=await call(`/api/v1/staff/registry/${study.id}/documents`,{token:registry});assert.ok(docs.some(row=>row.version===1),'Document version 1 is missing');
 const file=docs.find(row=>row.version===1);
 const content=await call(`/api/v1/staff/registry/${study.id}/documents/${file.id}`,{token:registry,bytes:true});
 assert.equal(content.subarray(0,5).toString(),'%PDF-');checks.push('Seeded registry PDF downloads');
 const state=await call(`/api/v1/staff/research-progress/${study.id}`,{token:progress});
 assert.equal(state.completion.date,'2026-10-28');assert.equal(state.presentation.date,'2026-10-30');
 assert.equal(state.publication.date,null,'Publication already recorded; choose a fresh demonstration record');checks.push('Lifecycle dates ready for publication recording');
 const accounts=await call('/api/v1/staff/accounts',{token:admin});const target=accounts.find(row=>row.email==='demo.limited@umak.edu.ph');assert.ok(target);
 assert.ok(Array.isArray(await call(`/api/v1/staff/accounts/${target.id}/history`,{token:admin})));checks.push('Account audit history accessible');
 const catalog=await call('/api/v1/public/services');const form=catalog.find(row=>row.code==='SVC-DEMO-A');assert.ok(form);
 assert.equal((await call(`/api/v1/public/services/${form.id}/form`,{bytes:true})).subarray(0,5).toString(),'%PDF-');checks.push('Public service form downloads for offline rehearsal');
 const evidence={origin,verified_at:new Date().toISOString(),result:'PASS',checks,study_id:study.id,tfidf_scored_count:review.scored_count,
 limitations:['API verification only; device recording and offline cache require rehearsal.','No publication, permission, or document mutations were made.']};
 mkdirSync('outputs/hosted-demo',{recursive:true});writeFileSync('outputs/hosted-demo/recording-verification.json',JSON.stringify(evidence,null,2));
 console.log(JSON.stringify(evidence,null,2));
}finally{
 await Promise.allSettled(sessions.map(token=>call('/api/v1/auth/logout',{token,body:{},expected:204})));
}
