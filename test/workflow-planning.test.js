import {test} from 'node:test';
import assert from 'node:assert/strict';
import {createAlgorithmRunner} from '../src/algorithm.js';
import {evaluatePlanning,planningInput} from '../src/research/workflow-planning.js';

const run=createAlgorithmRunner();
function snapshot(){
  return {version:3,milestones:[
    {code:'proposal',title:'Proposal',status:'Completed',evidence_state:null},
    {code:'ethics',title:'Ethics',status:'Pending',evidence_state:'received'},
    {code:'collection',title:'Data collection',status:'Pending',evidence_state:null}
  ],definition:{tasks:[
    {code:'proposal',duration_minutes:60,prerequisites:[],requirements:[]},
    {code:'ethics',duration_minutes:30,prerequisites:['proposal'],requirements:[{id:'ethics',gate:'verified'}]},
    {code:'collection',duration_minutes:120,prerequisites:['ethics'],requirements:[]}
  ]}};
}
test('saved evidence and task completion drive readiness and remaining critical path',async()=>{
  const saved=snapshot();
  const before=await evaluatePlanning(saved,run);
  assert.equal(before.remaining_duration_minutes,150);
  assert.deepEqual(before.critical_path.map(row=>row.code),['ethics','collection']);
  assert.deepEqual(before.ready,[]);
  assert.deepEqual(before.blocked[0].reasons,['Record verified evidence for Ethics.']);
  assert.deepEqual(before.blocked[1].reasons,['Complete Ethics.']);
  saved.milestones[1].evidence_state='verified';
  const verified=await evaluatePlanning(saved,run);
  assert.deepEqual(verified.ready.map(row=>row.code),['ethics']);
  saved.milestones[1].status='Completed';
  const completed=await evaluatePlanning(saved,run);
  assert.equal(completed.remaining_duration_minutes,120);
  assert.deepEqual(completed.ready.map(row=>row.code),['collection']);
  saved.milestones[2].status='Not applicable';
  const finished=await evaluatePlanning(saved,run);
  assert.equal(finished.complete,true);
  assert.equal(finished.remaining_duration_minutes,0);
});

test('cycles and inconsistent completed dependencies are rejected by the Python engine',async()=>{
  const saved=snapshot();
  saved.definition.tasks[0].prerequisites=['collection'];
  const cyclic=await evaluatePlanning(saved,run);
  assert.equal(cyclic.error_type,'CYCLIC_DEPENDENCY_DETECTED');
  saved.definition.tasks[0].prerequisites=[];
  saved.milestones[0].status='Pending';
  saved.milestones[1].status='Completed';
  assert.ok((await evaluatePlanning(saved,run)).error);
});

test('planning requires the complete saved milestone set and does not invent durations',async()=>{
  const saved=snapshot();
  saved.definition.tasks.pop();
  assert.throws(()=>planningInput(saved),/each saved milestone/);
  assert.deepEqual(await evaluatePlanning({...saved,definition:null},run),{configured:false,version:3});
});
