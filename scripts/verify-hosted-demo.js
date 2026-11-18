import assert from 'node:assert/strict';
import {randomUUID} from 'node:crypto';
import {mkdirSync,writeFileSync} from 'node:fs';
const origin=process.env.HOSTED_DEMO_ORIGIN??'https://umak-crm-api.onrender.com';
if(!origin.startsWith('https://'))throw Error('Hosted verification requires HTTPS');
const password=process.env.DEMO_PASSWORD;if(!password)throw Error('Demo credential is required');
const checks=[];
async function call(path,{token,body,method=body===undefined?'GET':'POST',expected=200,headers={}}={}){
 const response=await fetch(origin+path,{method,redirect:'manual',signal:AbortSignal.timeout(60000),
  headers:{...(token?{Authorization:'Bearer '+token}:{}),...(body===undefined?{}:{'Content-Type':'application/json'}),...headers},
  body:body===undefined?undefined:JSON.stringify(body)});
 assert.equal(response.status,expected,path+': '+await response.clone().text());return response;
}
const json=async(path,options)=> (await call(path,options)).json();
const login=async(email)=>(await json('/api/v1/auth/login',{body:{email,password}})).access_token;
const info=await json('/api/v1/demo/info');assert.equal(info.mode,'synthetic');checks.push('Hosted synthetic mode');
const email='student.demo'+Date.now()+'@umak.edu.ph';
await call('/api/v1/auth/researchers/register',{body:{email,password,confirm_password:password,display_name:'Hosted verification researcher',college:'Hosted Validation College',role:'student'},expected:201});
await call('/api/v1/auth/researchers/register',{body:{email:'actual.person@umak.edu.ph',password,confirm_password:password,display_name:'Not a demo identity',college:'Demo College',role:'student'},expected:422});
assert(!(await (await call('/demo/inbox')).text()).includes(email));
const inboxLogin=await fetch(origin+'/demo/inbox/login',{method:'POST',redirect:'manual',signal:AbortSignal.timeout(60000),
 headers:{'Content-Type':'application/x-www-form-urlencoded'},body:new URLSearchParams({password})});
assert.equal(inboxLogin.status,303);const cookie=inboxLogin.headers.get('set-cookie').split(';')[0];
const inbox=await (await call('/demo/inbox',{headers:{Cookie:cookie}})).text();
const ownMessage=inbox.split('<h2>'+email+'</h2>')[1];assert(ownMessage);const code=ownMessage.match(/verification code is (\d{6})/)[1];
await call('/api/v1/auth/researchers/verify',{body:{email,code},expected:204});
await call('/api/v1/auth/researchers/verify',{body:{email,code},expected:400});
const researcher=await login(email);checks.push('UMak signup, protected hosted inbox, single-use verification and sign-in');
const reporting=await login('demo.reporting@umak.edu.ph');
const report=await json('/api/v1/staff/reports',{token:reporting,body:{type:'research',college:'Demo College'}});
assert.deepEqual(report.metrics.map(row=>row[1]),[2,1,1,0,3]);
const utilization=await json('/api/v1/staff/reports',{token:reporting,body:{type:'service',college:'Demo College'}});
assert.deepEqual(utilization.metrics.map(row=>row[1]),[2,1,1]);checks.push('Live D1 research and utilization reconciliation');
const admin=await login('demo.admin@umak.edu.ph');const accounts=await json('/api/v1/staff/accounts',{token:admin});assert(accounts.some(row=>row.email===email));
await call('/api/v1/staff/accounts',{token:reporting,expected:403});checks.push('Account directory and staff permission denial');
const catalog=await json('/api/v1/public/services');assert.equal(catalog.length,3);
const full=catalog.find(row=>row.code==='SVC-DEMO-A'),simple=catalog.find(row=>row.code==='SVC-DEMO-SIMPLE');assert(full&&simple);
const form=Buffer.from(await (await call('/api/v1/public/services/'+full.id+'/form')).arrayBuffer());assert.equal(form.subarray(0,5).toString(),'%PDF-');checks.push('Public catalog and real PDF download');
const study=await json('/api/v1/researcher/registrations',{token:researcher,body:{submission_token:randomUUID(),title:'Hosted validation '+Date.now(),college:'Hosted Validation College',academic_period:'AY 2026-2027',adviser:'Demo Adviser'},expected:201});
const request=await json('/api/v1/service-requests',{token:researcher,body:{revision_id:simple.id,study_id:study.id,subject:'Hosted workflow validation',submission_token:randomUUID()},expected:201});
const processor=await login('demo.processing@umak.edu.ph');
let reviewed=await json('/api/v1/staff/service-requests/'+request.id+'/reviews',{token:processor,body:{expected_version:request.version,status:'In review',checklist:[],feedback:'Hosted review started',internal_remark:'PRIVATE-HOSTED-CHECK'}});
reviewed=await json('/api/v1/staff/service-requests/'+request.id+'/reviews',{token:processor,body:{expected_version:reviewed.version,status:'Completed',checklist:[],feedback:'Hosted review completed'}});
const ownRequest=await json('/api/v1/service-requests/'+request.id,{token:researcher});assert.equal(ownRequest.status,'Completed');assert(!JSON.stringify(ownRequest).includes('PRIVATE-HOSTED-CHECK'));checks.push('Linked registration, request review, completion and internal-note privacy');
const registrar=await login('demo.registry@umak.edu.ph');
const registry=await json('/api/v1/staff/registry/'+study.id,{token:registrar});assert(registry.linked_requests.some(row=>row.id===request.id));
const uploaded=await fetch(origin+'/api/v1/staff/registry/'+study.id+'/documents?'+new URLSearchParams({filename:'demo-form.pdf',upload_token:randomUUID()}),
 {method:'POST',signal:AbortSignal.timeout(60000),headers:{Authorization:'Bearer '+registrar,'Content-Type':'application/octet-stream'},body:form});
assert.equal(uploaded.status,201,await uploaded.clone().text());
const documents=await json('/api/v1/researcher/registrations/'+study.id+'/documents',{token:researcher});assert.equal(documents.length,1);
assert.deepEqual(Buffer.from(await(await call('/api/v1/researcher/registrations/'+study.id+'/documents/'+documents[0].id,{token:researcher})).arrayBuffer()),form);checks.push('Registry relationships, real document upload and linked download');
const progress=await login('demo.progress@umak.edu.ph');let saved=await json('/api/v1/staff/research-progress/'+study.id,{token:progress});
const tasks=saved.milestones.map((row,index,rows)=>({code:row.code,duration_minutes:30,prerequisites:index?[rows[index-1].code]:[],requirements:[]}));
saved=await json('/api/v1/staff/research-progress/'+study.id+'/workflow',{token:progress,body:{expected_version:saved.version,definition:{tasks}}});assert.equal(saved.remaining_duration_minutes,150);
const today=new Intl.DateTimeFormat('en-CA',{timeZone:'Asia/Manila',year:'numeric',month:'2-digit',day:'2-digit'}).format(new Date());
await json('/api/v1/staff/research-progress/'+study.id+'/events',{token:progress,body:{kind:'milestone',expected_version:saved.version,milestone_code:tasks[0].code,status:'Completed',effective_date:today,evidence:'Synthetic hosted evidence',evidence_state:'verified'}});
const planning=await json('/api/v1/researcher/registrations/'+study.id+'/planning',{token:researcher});assert.equal(planning.remaining_duration_minutes,120);checks.push('Python Kahn/set readiness and DAG planning through live workflow');
for(const token of [researcher,reporting,admin,processor,registrar,progress])await call('/api/v1/auth/logout',{token,body:{},expected:204});
const evidence={origin,verified_at:new Date().toISOString(),checks,synthetic_user:email,study_id:study.id,request_id:request.id};
mkdirSync('outputs/manual-demo',{recursive:true});writeFileSync('outputs/manual-demo/RENDER-VERIFICATION.json',JSON.stringify(evidence,null,2),{mode:0o600});
console.log(JSON.stringify(evidence,null,2));
