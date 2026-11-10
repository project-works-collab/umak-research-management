import {test,before,after} from 'node:test';
import assert from 'node:assert/strict';
import {randomUUID} from 'node:crypto';
import pg from 'pg';
import request from 'supertest';
import {PDFDocument} from 'pdf-lib';
import {createApp} from '../src/app.js';
import {hashPassword} from '../src/crypto.js';
import {scopedDatabase} from '../src/security/context.js';
if(process.env.ALLOW_SYNTHETIC_SETUP!=='yes')throw Error('Run only on a disposable, migrated synthetic database');
const pool=new pg.Pool({connectionString:process.env.DATABASE_URL});
const admin=new pg.Pool({connectionString:process.env.ADMIN_DATABASE_URL});
const scoped=scopedDatabase(pool),objects=new Map(),users=[],tokens={};
const password=randomUUID()+'Aa1!',emails={};
const app=createApp(pool,{objectStore:{async put(k,b){objects.set(k,b);},async remove(k){objects.delete(k);},async download(){return 'https://example.test/private';}},scanner:async()=>true});
const auth=(r,label='staff')=>r.set('Authorization',`Bearer ${tokens[label]}`);
const asStaff=fn=>scoped.storage.run({user:{id:users[1]},requestId:randomUUID(),ip:'192.0.2.1'},()=>fn(scoped.db));
let study,pdf;
before(async()=>{
 const hash=await hashPassword(password);
 for(const [label,role] of [['owner','student'],['staff','staff'],['outsider','staff']]){
  emails[label]=`integrity-${randomUUID()}@example.test`;
  users.push((await admin.query('INSERT INTO users(email,password_hash,role) VALUES($1,$2,$3) RETURNING id',[emails[label],hash,role])).rows[0].id);
  tokens[label]=(await request(app).post('/api/v1/auth/login').send({email:emails[label],password}).expect(200)).body.access_token;
 }
 study=(await auth(request(app).post('/api/v1/research'),'owner').send({title:'Integrity workflow fixture'}).expect(201)).body.id;
 await admin.query('INSERT INTO review_assignments(study_id,staff_id) VALUES($1,$2)',[study,users[1]]);
 const document=await PDFDocument.create();document.addPage();pdf=Buffer.from(await document.save());
});
after(async()=>{
 if(study)await admin.query('DELETE FROM research_records WHERE id=$1',[study]);
 await admin.query('DELETE FROM users WHERE id=ANY($1::int[])',[users]);
 await pool.end();await admin.end();
});
test('persisted lifecycle keeps independent outcomes and enforces scope/version',async()=>{
 const url=`/api/v1/research/${study}/lifecycle`;
 await auth(request(app).post(url+'/events'),'owner').send({event:'In-Progress',occurred_at:'2026-01-01',expected_version:1}).expect(403);
 await auth(request(app).post(url+'/events'),'outsider').send({event:'In-Progress',occurred_at:'2026-01-01',expected_version:1}).expect(404);
 const send=(event,expected_version,occurred_at='2026-01-01')=>auth(request(app).post(url+'/events')).send({event,expected_version,occurred_at});
 await send('Completed',1).expect(409);
 await send('In-Progress',1).expect(200);
 await send('UnderReview',1).expect(409);
 await send('UnderReview',2).expect(200);
 await send('Completed',3,'2026-02-01').expect(200);
 await send('Published',4,'2026-03-01').expect(200);
 await send('Presented',5,'2026-04-01').expect(200);
 await send('Published',6).expect(409);
 const state=(await auth(request(app).get(url),'owner').expect(200)).body;
 assert.equal(state.lifecycle_state,'Completed');assert.equal(state.lifecycle_version,6);
 assert.ok(state.completed_at.startsWith('2026-02-01'));assert.ok(state.published_at.startsWith('2026-03-01'));assert.ok(state.presented_at.startsWith('2026-04-01'));
 await send('Published',6,'2026-02-30').expect(400);
 // Runtime grants cannot bypass the controlled transition function.
 await assert.rejects(asStaff(db=>db.query("UPDATE research_records SET lifecycle_state='Registered' WHERE id=$1",[study])),{code:'42501'});
});
test('revisions preserve prior bytes/checksums and reject stale predecessors',async()=>{
 const path=`/api/v1/research/${study}/documents`;
 const upload=previous=>auth(request(app).post(path).query({filename:'draft.pdf',...(previous?{previous_document_id:previous}:{})}),'owner').set('Content-Type','application/octet-stream').send(pdf);
 const first=(await upload().expect(201)).body;
 const second=(await upload(first.id).expect(201)).body;
 assert.equal(first.revision_number,1);assert.equal(second.revision_number,2);
 assert.equal(second.series_id,first.id);assert.equal(second.previous_document_id,first.id);
 await upload(first.id).expect(409);
 await upload(randomUUID()).expect(400);
 const revisions=(await auth(request(app).get(path),'owner').expect(200)).body;
 assert.deepEqual(revisions.map(r=>r.revision_number),[1,2]);
 assert.ok(revisions.every(r=>r.sha256===first.sha256));assert.equal(objects.size,2);
 await auth(request(app).get(path),'outsider').expect(404);
 const audit=(await admin.query("SELECT new_value FROM audit_history WHERE target_table='documents' AND target_record_id=$1 AND action_type='INSERT'",[second.id])).rows[0].new_value;
 assert.equal(audit.sha256,second.sha256);assert.equal(audit.revision_number,2);
});
test('audit verifier detects content changes, missing tails, and rollback-safe appends',async()=>{
 const verify=()=>asStaff(db=>db.query('SELECT * FROM crm_verify_audit_chain()'));
 assert.equal((await verify()).rows[0].valid,true);
 await assert.rejects(pool.query('SELECT * FROM crm_verify_audit_chain()'),{code:'42501'});
 const c=await admin.connect();
 try{
  // Tamper only inside a rolled-back privileged fixture transaction.
  await c.query('BEGIN');await c.query("SELECT set_config('app.user_id',$1,true)",[String(users[1])]);
  await c.query("UPDATE audit_history SET action_type='forged' WHERE chain_index=(SELECT max(chain_index) FROM audit_history)");
  assert.equal((await c.query('SELECT * FROM crm_verify_audit_chain()')).rows[0].valid,false);
  await c.query('ROLLBACK');
  await c.query('BEGIN');await c.query("SELECT set_config('app.user_id',$1,true)",[String(users[1])]);
  await c.query('DELETE FROM audit_history WHERE chain_index=(SELECT max(chain_index) FROM audit_history)');
  assert.equal((await c.query('SELECT * FROM crm_verify_audit_chain()')).rows[0].valid,false);
  await c.query('ROLLBACK');
  await c.query('BEGIN');await c.query('UPDATE research_records SET title=title WHERE id=$1',[study]);await c.query('ROLLBACK');
 }finally{await c.query('ROLLBACK');c.release();}
 assert.equal((await verify()).rows[0].valid,true);
 const output=(await auth(request(app).get('/api/v1/admin/audit-history/verify')).expect(200)).body;
 assert.equal(output.valid,true);assert.match(output.head_hash,/^[a-f0-9]{64}$/);
});
