import test from 'node:test';
import assert from 'node:assert/strict';
import {duplicateCandidates} from '../src/research/search.js';

test('duplicate candidates use the benchmark retrieval predicate and exclude the source',async()=>{
  const calls=[];
  const client={query:async(sql,args)=>{calls.push([sql,args]);return {rows:[{id:7}]};},release:()=>{}};
  const rows=await duplicateCandidates({connect:async()=>client},{title:'Study',year:2026,department:'Computing',exclude_id:4});
  assert.deepEqual(rows,[{id:7}]);
  assert.deepEqual(calls[2][1],['Study',2026,'Computing',4]);
  assert.match(calls[2][0],/normalized_title % crm_normalize_title\(\$1\) OR normalized_title LIKE/);
  assert.match(calls[2][0],/LIMIT 50/);
  assert.equal(calls.at(-1)[0],'COMMIT');
});
