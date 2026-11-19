import {Router} from 'express';
import {id,validate,z} from '../security/validation.js';
import {evaluatePlanning,workflowDefinition} from './workflow-planning.js';

const text=max=>z.string().trim().max(max);
const date=z.iso.date().nullable().optional();
const note=text(2000).optional();
const events=z.discriminatedUnion('kind',[
  z.strictObject({kind:z.literal('stage'),expected_version:id,
    stage:z.enum(['Registered','In-Progress','UnderReview','Completed']),effective_date:date,
    supporting_details:text(2000).optional(),reference:text(160).optional(),next_action:text(1000).optional(),
    no_next_action:z.boolean().default(false),responsible_contact:text(160).optional(),deadline:date,
    completion_date:date,completion_details:text(2000).optional(),internal_note:note}),
  z.strictObject({kind:z.literal('milestone'),expected_version:id,milestone_code:text(80),
    status:z.enum(['Pending','In progress','Completed','Not applicable']),effective_date:date,
    evidence:text(2000).optional(),evidence_state:z.enum(['received','verified']).nullable().optional(),internal_note:note}),
  z.strictObject({kind:z.literal('presentation'),expected_version:id,effective_date:date,
    event:text(300).optional(),reason:text(1000).optional(),internal_note:note}),
  z.strictObject({kind:z.literal('publication'),expected_version:id,effective_date:date,
    citation:text(1200).optional(),reason:text(1000).optional(),internal_note:note}),
  z.strictObject({kind:z.literal('completion_correction'),expected_version:id,reopen:z.boolean(),
    completion_date:date,completion_details:text(2000).optional(),reason:text(1000).optional(),internal_note:note})
]);
const errors={
  denied:[403,'Access denied.'],missing:[404,'Research record not found.'],
  conflict:[409,'This research record was updated. Reload before saving.'],
  transition:[400,'This research stage change is not allowed.'],
  effective_date:[400,'Please enter an effective date.'],
  support:[400,'Provide supporting details or a reference.'],
  action:[400,'Enter a next action or select No next action recorded.'],
  completion_date:[400,'Please enter the completion date.'],
  reason:[400,'Enter the completion correction reason.'],
  presentation:[400,'Enter the presentation date and event.'],
  publication:[400,'Enter the publication date and citation.'],
  milestone:[400,'Select an active milestone.'],
  evidence:[400,'Provide an evidence reference before marking evidence received or verified.']
};
const params=validate(z.strictObject({id}),'params');

export function registeredProgressRoutes(app,db,auth,run){
  async function snapshot(study,staff){
    return (await db.query('SELECT crm_research_workflow_snapshot($1,$2) AS record',[study,staff])).rows[0].record;
  }
  async function plan(record,res){
    try{
      return await evaluatePlanning(record,run);
    }catch(error){
      if(error.httpStatus){
        res.status(503).json({error:'Research planning is temporarily unavailable. Try again.'});
      }else{
        res.status(400).json({error:error.message});
      }
      return null;
    }
  }
  async function view(study,staff){
    return (await db.query('SELECT crm_registered_progress_view($1,$2) AS record',[study,staff])).rows[0].record;
  }
  app.get('/api/v1/researcher/registrations/:id/progress',auth,params,async(req,res)=>{
    const result=await view(req.validated.params.id,false);
    if(!result)return res.status(404).json({error:'Research record not found.'});
    res.set('Cache-Control','no-store').json(result);
  });

  app.get('/api/v1/researcher/registrations/:id/planning',auth,params,async(req,res)=>{
    res.set('Cache-Control','no-store');
    const record=await snapshot(req.validated.params.id,false);
    if(!record)return res.status(404).json({error:'Research record not found.'});
    const result=await plan(record,res);
    if(result)res.json(result);
  });

  const staff=Router();
  staff.use(auth,async(_req,res,next)=>{
    const permission=await db.query('SELECT crm_manages_progress() AS allowed');
    if(!permission.rows[0].allowed)return res.status(403).json({error:'Access denied.'});
    res.set('Cache-Control','no-store');
    next();
  });
  staff.get('/',async(_req,res)=>res.json((await db.query('SELECT * FROM crm_progress_studies() LIMIT 100')).rows));
  staff.get('/:id',params,async(req,res)=>{
    const result=await view(req.validated.params.id,true);
    if(!result)return res.status(404).json({error:'Research record not found.'});
    res.json(result);
  });
  staff.get('/:id/planning',params,async(req,res)=>{
    const record=await snapshot(req.validated.params.id,true);
    if(!record)return res.status(404).json({error:'Research record not found.'});
    const result=await plan(record,res);
    if(result)res.json(result);
  });
  staff.post('/:id/workflow',params,validate(z.strictObject({expected_version:id,definition:workflowDefinition})),
    async(req,res)=>{
      const study=req.validated.params.id;
      const record=await snapshot(study,true);
      if(!record)return res.status(404).json({error:'Research record not found.'});
      if(record.version!==req.validated.body.expected_version)
        return res.status(409).json({error:errors.conflict[1]});
      record.definition=req.validated.body.definition;
      const result=await plan(record,res);
      if(!result)return;
      if(result.error)return res.status(400).json({error:result.error});
      const saved=await db.query('SELECT crm_save_research_workflow($1,$2,$3) AS result',
        [study,record.version,record.definition]);
      const outcome=saved.rows[0].result;
      if(outcome!=='saved'){
        const [status,error]=errors[outcome]??[400,'Unable to save workflow.'];
        return res.status(status).json({error});
      }
      res.json({...result,version:record.version+1});
    });

  staff.post('/:id/events',params,validate(events),async(req,res)=>{
    const study=req.validated.params.id;
    const {expected_version,kind,...payload}=req.validated.body;
    if(kind==='milestone'){
      const record=await snapshot(study,true);
      if(!record)return res.status(404).json({error:'Research record not found.'});
      if(record.version!==expected_version)return res.status(409).json({error:errors.conflict[1]});
      const current=record.milestones.find(item=>item.code===payload.milestone_code);
      if(!current)return res.status(400).json({error:errors.milestone[1]});
      if(record.definition){
        const candidate=structuredClone(record);
        const milestone=candidate.milestones.find(item=>item.code===payload.milestone_code);
        // Gate the action using the current saved completion state and the evidence in this save.
        milestone.evidence_state=payload.evidence_state??null;
        if(['In progress','Completed'].includes(payload.status)&&!['Completed','Not applicable'].includes(current.status)){
          const analysis=await plan(candidate,res);
          if(!analysis)return;
          const blocked=analysis.blocked?.find(item=>item.code===payload.milestone_code);
          if(analysis.error||blocked)return res.status(400).json({
            error:analysis.error??blocked.reasons.join(' ')
          });
        }
        milestone.status=payload.status;
        const analysis=await plan(candidate,res);
        if(!analysis)return;
        if(analysis.error)return res.status(400).json({error:analysis.error});
      }
    }
    const result=await db.query('SELECT crm_save_registered_progress($1,$2,$3,$4) AS result',
      [study,expected_version,kind,payload]);
    const outcome=result.rows[0].result;
    if(outcome!=='saved'){
      const [status,error]=errors[outcome]??[400,'Invalid progress update.'];
      return res.status(status).json({error});
    }
    res.json(await view(study,true));
  });
  app.use('/api/v1/staff/research-progress',staff);
}
