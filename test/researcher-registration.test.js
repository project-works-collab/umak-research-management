import {test} from 'node:test';
import assert from 'node:assert/strict';
import {once} from 'node:events';
import {randomBytes} from 'node:crypto';
import pg from 'pg';
import request from 'supertest';
import {createApp} from '../src/app.js';
import {configuredVerification} from '../src/auth/verification-mail.js';
import {testInbox} from './support/smtp-inbox.js';

if(process.env.ALLOW_SYNTHETIC_SETUP!=='yes')throw Error('Requires isolated synthetic database');

test('researcher account becomes usable after verification in a local SMTP inbox',async()=>{
  const inbox=testInbox();
  inbox.server.listen(0,'127.0.0.1');
  await once(inbox.server,'listening');
  const admin=new pg.Pool({connectionString:process.env.ADMIN_DATABASE_URL});
  const db=new pg.Pool({connectionString:process.env.DATABASE_URL});
  const key=Buffer.from(randomBytes(32)).toString('hex');
  const verification=configuredVerification({
    CRM_SMTP_URL:`smtp://127.0.0.1:${inbox.server.address().port}`,
    CRM_VERIFICATION_KEY:key,CRM_VERIFICATION_FROM:'crm@example.test',
    CRM_INSTITUTIONAL_EMAIL_DOMAINS:'example.test'
  });
  const server=createApp(db,{verification});
  server.set('trust proxy',1);
  const app=request(server);
  const address=randomBytes(2);
  const clientIp=`198.18.${address[0]}.${address[1]}`;
  const fromClient=operation=>operation.set('X-Forwarded-For',clientIp);
  const email=`signup-${randomBytes(5).toString('hex')}@example.test`;
  const secondEmail=`rollback-${randomBytes(5).toString('hex')}@example.test`;
  const password='Local-password-for-registration-2026';
  const payload={email,password,confirm_password:password,role:'student',
    display_name:'Acceptance Researcher',college:'College of Science'};
  const send=(action,body)=>fromClient(app.post('/api/v1/auth/researchers/'+action)).send(body);
  const login=()=>fromClient(app.post('/api/v1/auth/login')).send({email,password});
  const code=()=>inbox.messages.at(-1).text.match(/verification code is (\d{6})/)[1];
  try{
    await send('register',{...payload,role:'staff'}).expect(400);
    await send('register',{...payload,confirm_password:'Mismatched-password'}).expect(400);
    await send('register',{...payload,email:'name@outside.test'}).expect(422);
    await send('register',payload).expect(201,{resend_after_seconds:45});
    assert.equal(inbox.messages.length,1);
    const originalCode=code();
    const pending=await login().expect(403);
    assert.equal(pending.body.code,'email_unverified');
    assert.equal(pending.body.access_token,undefined);
    await send('register',payload).expect(409);
    await send('resend',{email}).expect(429);

    await admin.query(`UPDATE researcher_verification SET sent_at=now()-interval '46 seconds',
      expires_at=now()-interval '1 second' WHERE user_id=(SELECT id FROM users WHERE email=$1)`,[email]);
    await send('verify',{email,code:originalCode}).expect(400);
    await send('resend',{email}).expect(200);
    assert.equal(inbox.messages.length,2);
    const currentCode=code();
    assert.notEqual(currentCode,originalCode);
    await send('verify',{email,code:originalCode}).expect(400);
    await send('verify',{email,code:currentCode}).expect(204);
    await send('verify',{email,code:currentCode}).expect(400);

    const session=await login().expect(200);
    const identity=await app.get('/api/v1/users/me')
      .set('Authorization','Bearer '+session.body.access_token).expect(200);
    assert.equal(identity.body.role,'student');
    assert.equal(identity.body.display_name,payload.display_name);
    assert.equal(identity.body.college,payload.college);
    const records=await admin.query(`SELECT
      (SELECT count(*)::int FROM research_records WHERE author_id=$1) AS studies,
      (SELECT count(*)::int FROM consultation_tickets WHERE requester_id=$1) AS requests`,[identity.body.id]);
    assert.deepEqual(records.rows[0],{studies:0,requests:0});

    await admin.query('UPDATE users SET active=false WHERE email=$1',[email]);
    const inactive=await login().expect(403);
    assert.equal(inactive.body.code,'account_unavailable');
    await app.get('/api/v1/users/me').set('Authorization','Bearer '+session.body.access_token).expect(401);

    inbox.rejectNext();
    await send('register',{...payload,email:secondEmail}).expect(503);
    assert.equal((await admin.query('SELECT id FROM users WHERE email=$1',[secondEmail])).rowCount,0);
  }finally{
    await admin.query('DELETE FROM users WHERE email=ANY($1::text[])',[[email,secondEmail]]);
    await db.end();
    await admin.end();
    await new Promise(resolve=>inbox.server.close(resolve));
  }
});
