import {test} from 'node:test';
import assert from 'node:assert/strict';
import {createHmac} from 'node:crypto';
import {createSessionTokens} from '../src/security/session-tokens.js';
const key='ab'.repeat(32);
test('JWT sessions authenticate, expire at 15 minutes and reject tampering',()=>{
  let instant=1000;
  const codec=createSessionTokens({key,now:()=>instant});
  const token=codec.issue(7);
  assert.equal(codec.verify(token).sub,'7');
  assert.notEqual(codec.issue(7),token);
  assert.equal(codec.verify(token.slice(0,-2)+'xx'),null);
  assert.equal(createSessionTokens({key:'cd'.repeat(32),now:()=>instant}).verify(token),null);
  instant=1900;assert.equal(codec.verify(token),null);
  instant=999;assert.equal(codec.verify(token),null);
  for(const invalid of [null,'','a.b.c','a'.repeat(3000),'ab'.repeat(32)])assert.equal(codec.verify(invalid),null);
});
test('signed claims still require intended issuer, audience, lifetime and subject',()=>{
  const codec=createSessionTokens({key,now:()=>1000});
  const [header,payload]=codec.issue(7).split('.');
  const claims=JSON.parse(Buffer.from(payload,'base64url'));
  for(const change of [{iss:'other'},{aud:'other'},{exp:99999},{sub:'0'},{jti:''},{iat:1001},{sub:{}}]){
    const data=header+'.'+Buffer.from(JSON.stringify({...claims,...change})).toString('base64url');
    assert.equal(codec.verify(data+'.'+createHmac('sha256',Buffer.from(key,'hex')).update(data).digest('base64url')),null);
  }
  const data=Buffer.from('{"alg":"none","typ":"JWT"}').toString('base64url')+'.'+payload;
  assert.equal(codec.verify(data+'.'+createHmac('sha256',Buffer.from(key,'hex')).update(data).digest('base64url')),null);
});
test('production refuses missing or malformed signing keys',()=>{
  assert.throws(()=>createSessionTokens({production:true,key:undefined}));
  for(const value of ['','password','ab'.repeat(16)])assert.throws(()=>createSessionTokens({key:value}));
});
