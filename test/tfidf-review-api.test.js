import {test} from 'node:test';
import assert from 'node:assert/strict';
import request from 'supertest';
import {createApp} from '../src/app.js';
import {createSessionTokens} from '../src/security/session-tokens.js';

function fixture(role='staff') {
  const calls=[];
  const pool={async query(sql,params) {
    calls.push({sql,params});
    if(sql.includes('crm_take_token'))return {rowCount:1,rows:[{allowed:true}]};
    if(sql.includes('crm_session_user'))return {rowCount:1,rows:[{id:1,role}]};
    if(sql.includes('LIMIT 50'))return {rows:[
      {id:2,title:'Same title',abstract:'different study',exact_title:true,trigram_score:1},
      {id:3,title:'Related title',abstract:'same study',exact_title:false,trigram_score:.6}]};
    return {rowCount:0,rows:[]};
  }};
  pool.connect=async()=>({query:pool.query,release(){}});
  const sessionTokens=createSessionTokens({key:'cd'.repeat(32)});
  return {app:createApp(pool,{sessionTokens}),auth:`Bearer ${sessionTokens.issue(1)}`,calls};
}

test('staff review ranks exact titles first but alerts only above threshold',async()=>{
  const before={enabled:process.env.TFIDF_REVIEW_ENABLED,url:process.env.TFIDF_SERVICE_URL,token:process.env.TFIDF_SERVICE_TOKEN,fetch:globalThis.fetch};
  process.env.TFIDF_REVIEW_ENABLED='true';process.env.TFIDF_SERVICE_URL='http://scorer.internal';process.env.TFIDF_SERVICE_TOKEN='secret';
  globalThis.fetch=async(_url,options)=>{
    assert.equal(options.headers.authorization,'Bearer secret');
    assert.equal(JSON.parse(options.body).candidates.length,2);
    return {ok:true,json:async()=>({model_version:'v1',scores:[{id:2,score:.2},{id:3,score:.9}]})};
  };
  try {
    const {app,auth}=fixture();
    await request(app).post('/api/v1/research/duplicate-review').send({title:'Same title',abstract:'some abstract'}).expect(401);
    await request(app).post('/api/v1/research/duplicate-review').set('Authorization',auth).send({title:'---',abstract:'some abstract'}).expect(400);
    const student=fixture('student');
    await request(student.app).post('/api/v1/research/duplicate-review').set('Authorization',student.auth).send({title:'Same title',abstract:'some abstract'}).expect(403);
    const response=await request(app).post('/api/v1/research/duplicate-review').set('Authorization',auth).send({title:'Same title',abstract:'some abstract'}).expect(200);
    assert.deepEqual(response.body.matches.map(x=>[x.id,x.alert]),[[2,false],[3,true]]);
    assert.equal(response.body.model_version,'v1');
  } finally {
    globalThis.fetch=before.fetch;
    for(const [key,value] of [['TFIDF_REVIEW_ENABLED',before.enabled],['TFIDF_SERVICE_URL',before.url],['TFIDF_SERVICE_TOKEN',before.token]]){
      if(value===undefined)delete process.env[key];else process.env[key]=value;
    }
  }
});
