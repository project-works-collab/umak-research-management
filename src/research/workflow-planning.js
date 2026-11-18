import {z} from '../security/validation.js';

const step=z.string().regex(/^[A-Za-z0-9_-]{1,64}$/);
export const workflowDefinition=z.strictObject({
  tasks:z.array(z.strictObject({
    code:step,duration_minutes:z.number().int().min(0).max(525600),
    prerequisites:z.array(step).max(100),
    requirements:z.array(z.strictObject({id:step,gate:z.enum(['received','verified'])})).max(100)
  })).min(1).max(100)
});

export function planningInput(snapshot,definition=snapshot.definition){
  const tasks=definition.tasks;
  const milestones=new Map(snapshot.milestones.map(item=>[item.code,item]));
  const codes=tasks.map(task=>task.code);
  if(new Set(codes).size!==codes.length||codes.length!==milestones.size||
      codes.some(code=>!milestones.has(code)))throw Error('Configure each saved milestone exactly once.');
  const payload={steps:codes,dependencies:[],completed_actions:[],requirements:{},evidence:[],durations:{}};
  for(const milestone of milestones.values()){
    if(milestone.evidence_state)payload.evidence.push({id:milestone.code,state:milestone.evidence_state});
  }
  for(const task of tasks){
    if(new Set(task.prerequisites).size!==task.prerequisites.length||
        task.prerequisites.some(code=>!milestones.has(code)||code===task.code)||
        task.requirements.some(item=>!milestones.has(item.id)))throw Error('Select valid milestone prerequisites and evidence sources.');
    payload.dependencies.push(...task.prerequisites.map(parent=>[parent,task.code]));
    payload.durations[task.code]=task.duration_minutes;
    const saved=milestones.get(task.code);
    const finished=['Completed','Not applicable'].includes(saved.status);
    if(finished)payload.completed_actions.push(task.code);
    // A finished task has already passed its evidence gate; historical edits keep its completion.
    payload.requirements[task.code]=finished?[]:task.requirements;
  }
  return payload;
}

export async function evaluatePlanning(snapshot,run,{definition=snapshot.definition,signal}={}){
  if(!definition)return {configured:false,version:snapshot.version};
  const payload=planningInput(snapshot,definition);
  const result=await run(payload,signal);
  if(result.status==='ERROR')return {configured:true,version:snapshot.version,error:result.message,
    error_type:result.error_type,definition};
  const labels=new Map(snapshot.milestones.map(item=>[item.code,item.title]));
  const evidence=new Map(snapshot.milestones.map(item=>[item.code,item.evidence_state]));
  const completed=new Set(payload.completed_actions);
  const blocked=result.blocked_steps.map(item=>{
    const task=definition.tasks.find(task=>task.code===item.step);
    const reasons=[
      ...task.prerequisites.filter(code=>!completed.has(code)).map(code=>'Complete '+labels.get(code)+'.'),
      ...task.requirements.filter(req=>evidence.get(req.id)!=='verified'&&
        !(req.gate==='received'&&evidence.get(req.id)==='received'))
        .map(req=>'Record '+req.gate+' evidence for '+labels.get(req.id)+'.')
    ];
    return {code:item.step,title:labels.get(item.step),reasons};
  });
  return {
    configured:true,version:snapshot.version,definition,
    ready:result.ready_steps.map(code=>({code,title:labels.get(code)})),blocked,
    critical_path:result.critical_path.map(code=>({code,title:labels.get(code)})),
    remaining_duration_minutes:result.remaining_duration_minutes,
    earliest_finish_minutes:result.earliest_finish_minutes,
    duration_model:result.duration_model,complete:result.status==='COMPLETE'
  };
}
