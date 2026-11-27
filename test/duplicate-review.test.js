import test from 'node:test';
import assert from 'node:assert/strict';
import {reviewDuplicates} from '../src/research/duplicate-review.js';

function fixture(scores=[{id:9,score:.9},{id:4,score:.85}]){
  const calls=[];
  const db={async query(sql,args){
    calls.push({sql,args});
    if(sql.includes('LIMIT 100001'))return {rows:[{id:4},{id:9}]};
    // ID 9 was revoked while the scorer was running. It must not reach the client.
    return {rows:[{id:4,title:'Different wording',abstract:'same research'}]};
  }};
  const fetcher=async(url,options)=>{
    assert.equal(url.pathname,'/search');
    assert.deepEqual(JSON.parse(options.body).allowed_ids,[4,9]);
    return {ok:true,json:async()=>({model_version:'v1',scored_count:2,scores})};
  };
  return {calls,run:()=>reviewDuplicates(db,{title:'New title',abstract:'same research',year:2026,exclude_id:8},
    {serviceUrl:'https://private.example.test',serviceToken:'synthetic',fetcher})};
}

test('global similarity rechecks record access and applies a strict above-85-percent alert',async()=>{
  const {run,calls}=fixture();const result=await run();
  assert.deepEqual(result.matches.map(item=>[item.id,item.alert]),[[4,false]]);
  assert.equal(result.scored_count,2);
  assert.equal(result.threshold,.85);
  assert.deepEqual(calls[0].args,[2026,null,8]);
  assert.deepEqual(calls[1].args.slice(0,3),[2026,null,8]);
  assert(!calls[0].sql.includes('normalized_title %'));
});

test('private scorer cannot inject out-of-scope IDs, duplicate IDs, or invalid scores',async()=>{
  for(const scores of [[{id:7,score:.9},{id:4,score:.8}],
    [{id:4,score:.9},{id:4,score:.8}], [{id:4,score:1.1},{id:9,score:.8}]]){
    await assert.rejects(fixture(scores).run());
  }
});
