import assert from 'node:assert/strict';
import {createServer} from 'node:http';
import {randomBytes} from 'node:crypto';
import {createApp} from '../../src/app.js';
import {hostedDemo} from '../../demo/hosted-runtime.js';
import {seedHostedDemo} from '../../scripts/seed-hosted-demo.js';
export async function checkHostedDemo(admin,db,password,servers){
  const client=await admin.connect();
  try{
    assert.deepEqual(await seedHostedDemo(client,password),{accounts:14,services:5,studies:2});
    const count=(await client.query('SELECT count(*)::int AS count FROM users')).rows[0].count;
    await seedHostedDemo(client,password);
    assert.equal((await client.query('SELECT count(*)::int AS count FROM users')).rows[0].count,count);
  }finally{client.release();}
  const env={CRM_DEMO_MODE:'umak',ALLOW_SYNTHETIC_SETUP:'yes',DEMO_PASSWORD:password,JWT_SIGNING_KEY:randomBytes(32).toString('hex')};
  const demo=hostedDemo(db,env);
  const server=createServer(createApp(db,{verification:demo.verification,scanner:demo.scanner,demo}));servers.push(server);
  await new Promise(resolve=>server.listen(0,'127.0.0.1',resolve));
  const origin='http://127.0.0.1:'+server.address().port;
  const post=(path,body,headers={})=>fetch(origin+path,{method:'POST',redirect:'manual',headers:{'Content-Type':'application/json',...headers},body:JSON.stringify(body)});
  const login=await post('/api/v1/auth/login',{email:'demo.reporting@umak.edu.ph',password});assert.equal(login.status,200);
  const token=(await login.json()).access_token;
  const reporting=await post('/api/v1/staff/reports',{type:'research'},{Authorization:'Bearer '+token});
  assert.equal(reporting.status,200);assert.deepEqual((await reporting.json()).metrics.map(row=>row[1]),[2,1,1,0,3]);
  const studentLogin=await post('/api/v1/auth/login',{email:'demo.student@umak.edu.ph',password});assert.equal(studentLogin.status,200);
  const email='student.demo01@umak.edu.ph';
  const input={email,password,confirm_password:password,display_name:'Hosted demo signup',college:'Demo College',role:'student'};
  assert.equal((await post('/api/v1/auth/researchers/register',input)).status,201);
  assert.equal((await post('/api/v1/auth/researchers/register',{...input,email:'actual.person@umak.edu.ph'})).status,422);
  const hidden=await (await fetch(origin+'/demo/inbox')).text();assert(!hidden.includes(email));
  const denied=await fetch(origin+'/demo/inbox/login',{method:'POST',redirect:'manual',headers:{'Content-Type':'application/x-www-form-urlencoded'},body:'password=incorrect'});assert.equal(denied.status,403);
  const inboxLogin=await fetch(origin+'/demo/inbox/login',{method:'POST',redirect:'manual',headers:{'Content-Type':'application/x-www-form-urlencoded'},body:new URLSearchParams({password})});
  assert.equal(inboxLogin.status,303);const cookie=inboxLogin.headers.get('set-cookie').split(';')[0];
  const inbox=await (await fetch(origin+'/demo/inbox',{headers:{Cookie:cookie}})).text();assert(inbox.includes(email));
  const code=inbox.match(/verification code is (\d{6})/)[1];
  assert.equal((await post('/api/v1/auth/researchers/verify',{email,code})).status,204);
  assert.equal((await post('/api/v1/auth/login',{email,password})).status,200);
  assert.equal((await post('/api/v1/auth/researchers/verify',{email,code})).status,400);
  assert((await (await fetch(origin+'/api/v1/demo/info')).json()).mode==='synthetic');
  console.log('Hosted demo seed repeatability, D1 reports, reserved UMak signup, protected inbox, verification and sign-in passed.');
}
