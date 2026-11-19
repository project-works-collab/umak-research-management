import express from 'express';
import {id,validate,validationFields,z} from '../security/validation.js';
import {validateDocument} from '../storage/validate.js';

const text=max=>z.string().trim().min(1).max(max);
const code=z.string().regex(/^[A-Z0-9][A-Z0-9-]{1,31}$/);
const serviceFields=z.strictObject({
  name:text(160),description:text(1200),purpose:text(1600),eligibility:text(1200),
  applicants:z.array(z.enum(['Student','Faculty'])).min(1).max(2),
  contact_name:text(160),contact_email:z.email().max(254),
  turnaround:text(120).optional(),channel:z.enum(['in_app','external']),
  instructions:z.array(text(500)).min(1).max(20),
  requirements:z.array(z.strictObject({title:text(180),required:z.boolean()})).max(20)
});
const uniqueApplicants=value=>!value.applicants||new Set(value.applicants).size===value.applicants.length;
const details=serviceFields.refine(uniqueApplicants,{path:['applicants']})
  .refine(value=>value.channel!=='in_app'||value.requirements.filter(item=>item.required).length<=5,{path:['requirements']});
const draftDetails=serviceFields.partial().refine(uniqueApplicants);
const columns=`s.id AS service_id,s.code,r.id,r.version,r.state,r.details,
  r.form_name,r.form_mime,r.form_updated_at,r.published_at,r.updated_at`;
const join='FROM crm_service_revisions r JOIN crm_services s ON s.id=r.service_id';
const limit=10_000_000;

function sendForm(row,res){
  if(!row?.form_bytes)return res.status(404).json({error:'Form not found'});
  res.set('Cache-Control','no-store').attachment(row.form_name).type(row.form_mime).send(row.form_bytes);
}

async function transaction(db,run){
  const client=await db.connect();
  try{
    await client.query('BEGIN');
    const result=await run(client);
    await client.query('COMMIT');
    return result;
  }catch(error){
    await client.query('ROLLBACK');
    throw error;
  }finally{
    client.release();
  }
}

function audit(client,revision,actor,action){
  return client.query('INSERT INTO crm_service_history(revision_id,actor_id,action) VALUES($1,$2,$3)',
    [revision,actor,action]);
}

export function serviceCatalogRoutes(app,db,auth){
  const publicRouter=express.Router();
  publicRouter.use((_req,res,next)=>{res.set('Cache-Control','no-store');next();});
  publicRouter.get('/',async(_req,res)=>{
    const rows=await db.query(`SELECT ${columns} ${join} WHERE r.state='published' ORDER BY s.code`);
    res.json(rows.rows);
  });
  publicRouter.get('/:id/form',validate(z.strictObject({id}),'params'),async(req,res)=>{
    const result=await db.query("SELECT form_name,form_mime,form_bytes FROM crm_service_revisions WHERE id=$1 AND state='published'",
      [req.validated.params.id]);
    sendForm(result.rows[0],res);
  });
  app.use('/api/v1/public/services',publicRouter);

  const staff=express.Router();
  staff.use(auth,async(req,res,next)=>{
    if(req.user.role!=='staff')return res.status(403).json({error:'Access denied'});
    const permission=await db.query("SELECT 1 FROM crm_staff_permissions WHERE user_id=$1 AND permission='content_management'",[req.user.id]);
    if(!permission.rowCount)return res.status(403).json({error:'Access denied'});
    res.set('Cache-Control','no-store');
    next();
  });
  staff.use(express.json({limit:'64kb'}));
  staff.get('/',async(_req,res)=>{
    const result=await db.query(`SELECT ${columns} ${join} ORDER BY s.code,r.version DESC`);
    res.json(result.rows);
  });
  staff.post('/drafts',validate(z.strictObject({code,details:draftDetails})),async(req,res)=>{
    const input=req.validated.body;
    const revision=await transaction(db,async client=>{
      await client.query('INSERT INTO crm_services(code) VALUES($1) ON CONFLICT DO NOTHING',[input.code]);
      const {rows:[service]}=await client.query('SELECT id FROM crm_services WHERE code=$1',[input.code]);
      await client.query('SELECT pg_advisory_xact_lock(7411,$1)',[service.id]);
      const {rows:[latest]}=await client.query('SELECT id,version,state FROM crm_service_revisions WHERE service_id=$1 ORDER BY version DESC LIMIT 1',[service.id]);
      if(latest?.state==='draft')return null;
      const {rows:[draft]}=await client.query(`INSERT INTO crm_service_revisions
        (service_id,version,state,details,form_name,form_mime,form_bytes,form_updated_at)
        SELECT $1,$2,'draft',$3,previous.form_name,previous.form_mime,previous.form_bytes,previous.form_updated_at
        FROM (SELECT 1) seed LEFT JOIN crm_service_revisions previous ON previous.id=$4
        RETURNING id,version,state`,[service.id,(latest?.version??0)+1,input.details,latest?.id??null]);
      await audit(client,draft.id,req.user.id,'draft_created');
      return draft;
    });
    if(!revision)return res.status(409).json({error:'This service already has a draft'});
    res.status(201).json(revision);
  });

  const revisionId=validate(z.strictObject({id}),'params');
  staff.post('/:id/edit',revisionId,validate(draftDetails),async(req,res)=>{
    const revision=await transaction(db,async client=>{
      const result=await client.query(`UPDATE crm_service_revisions SET details=$2,updated_at=clock_timestamp()
        WHERE id=$1 AND state='draft' RETURNING id,version,state`,[req.validated.params.id,req.validated.body]);
      if(result.rowCount)await audit(client,result.rows[0].id,req.user.id,'draft_updated');
      return result.rows[0];
    });
    if(!revision)return res.status(409).json({error:'Draft unavailable'});
    res.json(revision);
  });

  staff.put('/:id/form',revisionId,express.raw({type:'application/octet-stream',limit}),async(req,res)=>{
    const name=req.get('X-Form-Name')??'';
    if(!Buffer.isBuffer(req.body)||req.body.length>limit) return res.status(400).json({error:'Provide a PDF or DOCX up to 10 MB'});
    const mime=await validateDocument(req.body,name);
    const revision=await transaction(db,async client=>{
      const result=await client.query(`UPDATE crm_service_revisions SET
        form_name=$2,form_mime=$3,form_bytes=$4,form_updated_at=clock_timestamp(),updated_at=clock_timestamp()
        WHERE id=$1 AND state='draft' RETURNING id,version,state`,[req.validated.params.id,name,mime,req.body]);
      if(result.rowCount)await audit(client,result.rows[0].id,req.user.id,'form_replaced');
      return result.rows[0];
    });
    if(!revision)return res.status(409).json({error:'Draft unavailable'});
    res.json(revision);
  });
  staff.get('/:id/form',revisionId,async(req,res)=>{
    const result=await db.query('SELECT form_name,form_mime,form_bytes FROM crm_service_revisions WHERE id=$1',[req.validated.params.id]);
    sendForm(result.rows[0],res);
  });

  for(const action of ['publish','withdraw'])staff.post(`/:id/${action}`,revisionId,
    validate(z.strictObject({confirmed:z.literal(true)})),async(req,res)=>{
      const revision=await transaction(db,async client=>{
        const target=req.validated.params.id;
        const {rows:[entry]}=await client.query(`SELECT s.id ${join} WHERE r.id=$1`,[target]);
        if(!entry)return null;
        await client.query('SELECT pg_advisory_xact_lock(7411,$1)',[entry.id]);
        const {rows:[row]}=await client.query('SELECT state,details FROM crm_service_revisions WHERE id=$1 FOR UPDATE',[target]);
        const publishing=action==='publish';
        if(row.state!==(publishing?'draft':'published'))return null;
        if(publishing){
          const checked=details.safeParse(row.details);
          if(!checked.success)return {incomplete:true,field_errors:validationFields(checked.error.issues)};
        }
        if(publishing){
          const previous=await client.query(`UPDATE crm_service_revisions SET state='superseded',updated_at=clock_timestamp()
             WHERE service_id=$1 AND state='published' RETURNING id`,[entry.id]);
          if(previous.rowCount)await audit(client,previous.rows[0].id,req.user.id,'superseded');
        }
        const state=publishing?'published':'withdrawn';
        const result=await client.query(`UPDATE crm_service_revisions SET state=$2,updated_at=clock_timestamp(),
          published_at=CASE WHEN $2='published' THEN clock_timestamp() ELSE published_at END
          WHERE id=$1 RETURNING id,version,state`,[target,state]);
        await audit(client,target,req.user.id,state);
        return result.rows[0];
      });
      if(!revision)return res.status(409).json({error:'Revision is no longer available for this action'});
      if(revision.incomplete)return res.status(400).json({error:'Complete the highlighted service information before publishing.',field_errors:revision.field_errors});
      res.json(revision);
    });
  staff.get('/:id/history',revisionId,async(req,res)=>{
    const {rows}=await db.query(`SELECT h.action,h.actor_id,h.recorded_at,h.revision_id FROM crm_service_history h
      JOIN crm_service_revisions r ON r.id=h.revision_id
      WHERE r.service_id=(SELECT service_id FROM crm_service_revisions WHERE id=$1) ORDER BY h.id DESC`,
      [req.validated.params.id]);
    res.json(rows);
  });
  app.use('/api/v1/staff/services',staff);
}
