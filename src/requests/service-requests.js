import {Router,raw} from 'express';
import {createHash,randomUUID} from 'node:crypto';
import {deadline,id,validate,z} from '../security/validation.js';
import {validateIsolated} from '../storage/isolated-validation.js';
import {requestLimiter} from '../security/limiter.js';

const maxBytes=10_000_000;
let activeUploads=0;
const requiredIndex=z.number().int().min(0).max(24);
const status=z.enum(['Submitted','In review','For correction','Completed']);
const text=max=>z.string().trim().max(max);
const files=z.array(z.uuid()).max(5).default([]);
const attempt=z.uuid();
const idParams=validate(z.strictObject({id}),'params');
const create=z.strictObject({
  revision_id:id,study_id:id.nullable().default(null),subject:text(160).min(1),
  description:text(4000).default(''),submission_token:attempt,files
});
const review=z.strictObject({
  expected_version:id,status,feedback:text(4000).default(''),internal_remark:text(4000).default(''),
  missing_items:z.array(requiredIndex).max(20).default([]),next_action:text(1000).default(''),
  deadline_at:deadline.default(null),checklist:z.array(z.enum(['Pending','Verified','Needs correction','Not applicable'])).max(20),
  checkpoint:z.strictObject({name:text(120).min(1),date:z.iso.date(),reference:text(160).default('')}).nullable().default(null),
  correction_reason:text(1000).optional()
});
const resubmission=z.strictObject({expected_version:id,files}).refine(value=>value.files.length>0);
const list=z.strictObject({before:id.optional(),status:status.optional(),service:text(32).optional(),
  from:z.iso.date().optional(),to:z.iso.date().optional()}).refine(values=>!values.from||!values.to||values.from<=values.to);
const columns=`r.id,r.subject,r.description,r.study_id,r.status,r.version,r.submitted_at,
  r.requester_category,r.requester_college,r.revision_id,s.code AS service_code,v.details->>'name' AS service`;
const sources=`FROM crm_service_requests r JOIN crm_service_revisions v ON v.id=r.revision_id
  JOIN crm_services s ON s.id=v.service_id`;

function fingerprint(value){
  return createHash('sha256').update(value).digest('hex');
}
function fail(status,message){
  return {failure:{status,message}};
}
function reply(res,result,successStatus=200){
  if(result.failure)return res.status(result.failure.status).json({error:result.failure.message});
  return res.status(successStatus).json(result.value);
}
async function transaction(db,run){
  const client=await db.connect();
  try{
    await client.query('BEGIN');
    const result=await run(client);
    await client.query(result.failure?'ROLLBACK':'COMMIT');
    return result;
  }catch(error){
    await client.query('ROLLBACK');
    throw error;
  }finally{
    client.release();
  }
}
async function update(client,id,kind){
  await client.query('INSERT INTO crm_service_request_updates(request_id,kind) VALUES($1,$2)',[id,kind]);
}
function requiredFiles(checklist,indices){
  return checklist.every((entry,index)=>!entry.required||indices.has(index));
}
async function stagedFiles(client,user,ids){
  const result=await client.query(`SELECT id,requirement FROM crm_service_request_files
    WHERE requester_id=$1 AND id=ANY($2::uuid[]) AND request_id IS NULL FOR UPDATE`,[user,ids]);
  const rows=result.rows;
  const indices=new Set(rows.map(row=>row.requirement));
  return rows.length===ids.length&&indices.size===ids.length?indices:null;
}
async function summary(client,request){
  const result=await client.query(`SELECT ${columns} ${sources} WHERE r.id=$1`,[request]);
  return result.rows[0];
}

export function serviceRequestRoutes(app,db,auth,{scanner}={}){
  const researcher=Router();
  const staff=Router();
  for(const router of [researcher,staff]){
    router.use(auth,(_req,res,next)=>{res.set('Cache-Control','no-store');next();});
  }
  researcher.use((req,res,next)=>{
    if(!['student','faculty'].includes(req.user.role))return res.status(403).json({error:'Access denied'});
    next();
  });
  staff.use(async(_req,res,next)=>{
    const result=await db.query('SELECT crm_processes_requests() AS allowed');
    if(!result.rows[0].allowed)return res.status(403).json({error:'Access denied'});
    next();
  });

  researcher.get('/linked-studies',async(_req,res)=>{
    const result=await db.query(`SELECT id,title FROM research_records WHERE crm_member(id)
      AND (status='Approved' OR is_registered) ORDER BY title,id`);
    res.json(result.rows);
  });
  researcher.get('/updates',async(req,res)=>{
    const result=await db.query(`SELECT u.id,u.request_id,u.kind,u.available_at,v.details->>'name' AS service
      FROM crm_service_request_updates u JOIN crm_service_requests r ON r.id=u.request_id
      JOIN crm_service_revisions v ON v.id=r.revision_id
      WHERE r.requester_id=$1 AND u.available_at<=now() ORDER BY u.available_at DESC,u.id DESC LIMIT 100`,[req.user.id]);
    res.json(result.rows);
  });
  researcher.post('/files',requestLimiter(db,{capacity:12,perSecond:1/30,scope:'service-request-files'}),
    validate(z.strictObject({filename:text(185).min(1),attempt_token:attempt,
      requirement:z.coerce.number().pipe(requiredIndex)}),'query'),
    (req,res,next)=>{
      if(!scanner||activeUploads>=2)return res.status(503).json({error:'Upload failed. Try again.'});
      if(!req.is('application/octet-stream'))return res.status(415).json({error:'Upload a PDF or DOCX file.'});
      activeUploads++;
      raw({type:'application/octet-stream',limit:maxBytes})(req,res,error=>{
        if(error)activeUploads--;
        next(error);
      });
    },async(req,res)=>{
      try{
      const {filename,attempt_token,requirement}=req.validated.query;
      if(!Buffer.isBuffer(req.body)||req.body.length===0)return res.status(400).json({error:'Upload a PDF or DOCX file.'});
      const mime=await validateIsolated(req.body,filename);
      if(!await scanner(req.body))return res.status(400).json({error:'Document rejected'});
      const hash=fingerprint(req.body);
      const result=await transaction(db,async client=>{
        const user=req.user.id;
        await client.query('SELECT pg_advisory_xact_lock(7512,$1)',[user]);
        await client.query(`DELETE FROM crm_service_request_files WHERE requester_id=$1 AND request_id IS NULL
          AND created_at<now()-interval '24 hours'`,[user]);
        const prior=await client.query(`SELECT id,filename,sha256,request_id FROM crm_service_request_files
          WHERE requester_id=$1 AND attempt_token=$2 AND requirement=$3`,[user,attempt_token,requirement]);
        const existing=prior.rows[0];
        if(existing){
          if(existing.sha256===hash&&existing.filename===filename)return {value:{id:existing.id,filename}};
          return fail(409,'Use a new upload attempt for this file.');
        }
        const count=await client.query(`SELECT count(*)::int AS count FROM crm_service_request_files
          WHERE requester_id=$1 AND attempt_token=$2`,[user,attempt_token]);
        if(count.rows[0].count>=5)return fail(400,'You can attach up to 5 files.');
        const fileId=randomUUID();
        await client.query(`INSERT INTO crm_service_request_files
          (id,requester_id,attempt_token,requirement,filename,mime,bytes,sha256) VALUES($1,$2,$3,$4,$5,$6,$7,$8)`,
          [fileId,user,attempt_token,requirement,filename,mime,req.body,hash]);
        return {value:{id:fileId,filename}};
      });
      reply(res,result,201);
      }finally{
        activeUploads--;
      }
    });
  researcher.post('/',validate(create),async(req,res)=>{
    const input=req.validated.body;
    const user=req.user.id;
    const digest=fingerprint(JSON.stringify({...input,files:[...input.files].sort()}));
    const result=await transaction(db,async client=>{
      await client.query('SELECT pg_advisory_xact_lock(7512,$1)',[user]);
      const earlier=await client.query(`SELECT id,fingerprint FROM crm_service_requests WHERE requester_id=$1 AND submission_token=$2`,
        [user,input.submission_token]);
      if(earlier.rowCount){
        const prior=earlier.rows[0];
        return prior.fingerprint===digest?{value:await summary(client,prior.id)}:fail(409,'Submission token already used.');
      }
      const catalog=await client.query(`SELECT details FROM crm_service_revisions WHERE id=$1 AND state='published' FOR SHARE`,
        [input.revision_id]);
      const service=catalog.rows[0]?.details;
      const category=req.user.role==='faculty'?'Faculty':'Student';
      if(!service||service.channel!=='in_app'||!service.applicants.includes(category))
        return fail(403,'This service does not accept in-app requests from this account.');
      if(input.study_id!==null){
        const linked=await client.query("SELECT id FROM research_records WHERE id=$1 AND crm_member(id) AND (status='Approved' OR is_registered)",[input.study_id]);
        if(!linked.rowCount)return fail(400,'Select a linked research study you can access.');
      }
      const indices=await stagedFiles(client,user,input.files);
      if(!indices||!requiredFiles(service.requirements,indices))return fail(400,'Complete the highlighted required fields.');
      const {rows:[identity]}=await client.query('SELECT college FROM users WHERE id=$1',[user]);
      const {rows:[saved]}=await client.query(`INSERT INTO crm_service_requests
        (requester_id,revision_id,study_id,requester_category,requester_college,subject,description,submission_token,fingerprint)
        VALUES($1,$2,$3,$4,$5,$6,$7,$8,$9) RETURNING id`,[user,input.revision_id,input.study_id,
        req.user.role,identity.college,input.subject,input.description,input.submission_token,digest]);
      await client.query('UPDATE crm_service_request_files SET request_id=$1 WHERE id=ANY($2::uuid[])',[saved.id,input.files]);
      await update(client,saved.id,'receipt');
      return {value:await summary(client,saved.id)};
    });
    reply(res,result,201);
  });
  researcher.post('/:id/resubmit',idParams,validate(resubmission),async(req,res)=>{
    const id=req.validated.params.id;
    const input=req.validated.body;
    const digest=fingerprint(JSON.stringify([...input.files].sort()));
    const result=await transaction(db,async client=>{
      const {rows:[current]}=await client.query(`SELECT r.status,r.version,v.details
        FROM crm_service_requests r JOIN crm_service_revisions v ON v.id=r.revision_id
        WHERE r.id=$1 AND r.requester_id=$2 FOR UPDATE OF r`,[id,req.user.id]);
      if(!current)return fail(404,'Request not found.');
      const {rows:[previousSubmission]}=await client.query(`SELECT fingerprint,resubmitted_at
        FROM crm_service_resubmissions WHERE request_id=$1 AND previous_version=$2`,[id,input.expected_version]);
      if(previousSubmission){
        if(previousSubmission.fingerprint!==digest)return fail(409,'Reload the current request before resubmitting.');
        return {value:{...await summary(client,id),resubmitted_at:previousSubmission.resubmitted_at}};
      }
      if(current.status!=='For correction'||current.version!==input.expected_version)return fail(409,'Reload the current request before resubmitting.');
      const incoming=await stagedFiles(client,req.user.id,input.files);
      if(!incoming)return fail(400,'Correct the highlighted information.');
      const previousReviews=await client.query('SELECT missing_items FROM crm_researcher_review($1) LIMIT 1',[id]);
      if((previousReviews.rows[0]?.missing_items??[]).some(index=>!incoming.has(index)))
        return fail(400,'Complete the highlighted required fields.');
      const previous=await client.query('SELECT DISTINCT requirement FROM crm_service_request_files WHERE request_id=$1',[id]);
      const total=new Set([...incoming,...previous.rows.map(row=>row.requirement)]);
      if(total.size>5)return fail(400,'You can attach up to 5 files.');
      if(!requiredFiles(current.details.requirements,total))return fail(400,'Complete the highlighted required fields.');
      await client.query('UPDATE crm_service_request_files SET request_id=$1 WHERE id=ANY($2::uuid[])',[id,input.files]);
      await client.query("UPDATE crm_service_requests SET status='Submitted',version=version+1 WHERE id=$1",[id]);
      const {rows:[event]}=await client.query(`INSERT INTO crm_service_request_updates(request_id,kind)
        VALUES($1,'receipt') RETURNING available_at`,[id]);
      await client.query(`INSERT INTO crm_service_resubmissions(request_id,previous_version,fingerprint,resubmitted_at)
        VALUES($1,$2,$3,$4)`,[id,input.expected_version,digest,event.available_at]);
      return {value:{...await summary(client,id),resubmitted_at:event.available_at}};
    });
    reply(res,result);
  });

  for(const [router,staffView] of [[researcher,false],[staff,true]]){
    router.get('/',validate(list,'query'),async(req,res)=>{
      const {before,status,service,from,to}=req.validated.query;
      const result=await db.query(`SELECT ${columns} ${staffView?",p.display_name AS requester_name,p.email AS requester_email":""}
        ${sources} ${staffView?'LEFT JOIN LATERAL crm_requester_profile(r.requester_id) p ON true':''}
        WHERE ($1::int IS NULL OR r.id<$1) AND ($2::text IS NULL OR r.status=$2)
          AND ($3::text IS NULL OR s.code=$3)
          AND ($4::date IS NULL OR (r.submitted_at AT TIME ZONE 'Asia/Manila')::date >= $4)
          AND ($5::date IS NULL OR (r.submitted_at AT TIME ZONE 'Asia/Manila')::date <= $5)
          AND ($6::int IS NULL OR r.requester_id=$6)
        ORDER BY r.id DESC LIMIT 100`,[before??null,status??null,service??null,from??null,to??null,staffView?null:req.user.id]);
      res.json(result.rows);
    });
    router.get('/:id',idParams,async(req,res)=>{
      const id=req.validated.params.id;
      const request=await db.query(`SELECT ${columns},v.details->'requirements' AS requirements
        ${staffView?',p.display_name AS requester_name,p.email AS requester_email':''} ${sources}
        ${staffView?'LEFT JOIN LATERAL crm_requester_profile(r.requester_id) p ON true':''} WHERE r.id=$1`,[id]);
      if(!request.rowCount)return res.status(404).json({error:'Request not found.'});
      const attachments=await db.query(`SELECT DISTINCT ON(requirement) id,filename,mime,requirement,created_at
        FROM crm_service_request_files WHERE request_id=$1 ORDER BY requirement,created_at DESC,id`,[id]);
      const reviews=await db.query(staffView?`SELECT * FROM crm_service_request_reviews WHERE request_id=$1 ORDER BY id DESC`:
        'SELECT * FROM crm_researcher_review($1)',[id]);
      res.json({...request.rows[0],attachments:attachments.rows,reviews:reviews.rows});
    });
    router.get('/:id/files/:file',validate(z.strictObject({id,file:z.uuid()}),'params'),async(req,res)=>{
      const {id,file}=req.validated.params;
      const result=await db.query('SELECT filename,mime,bytes FROM crm_service_request_files WHERE id=$1 AND request_id=$2',[file,id]);
      const stored=result.rows[0];
      if(!stored)return res.status(404).json({error:'Request file not found.'});
      res.attachment(stored.filename).type(stored.mime).send(stored.bytes);
    });
  }

  staff.post('/:id/reviews',idParams,validate(review),async(req,res)=>{
    const id=req.validated.params.id;
    const input=req.validated.body;
    const result=await transaction(db,async client=>{
      const {rows:[current]}=await client.query(`SELECT r.status,r.version,v.details,
        (SELECT deadline_at FROM crm_service_request_reviews WHERE request_id=r.id ORDER BY id DESC LIMIT 1) AS prior_deadline
        FROM crm_service_requests r
        JOIN crm_service_revisions v ON v.id=r.revision_id WHERE r.id=$1 FOR UPDATE OF r`,[id]);
      if(!current)return fail(404,'Request not found.');
      if(current.version!==input.expected_version)return fail(409,'This request was updated. Reload before saving');
      const allowed={
        Submitted:['In review'],'In review':['In review','For correction','Completed'],
        'For correction':[],Completed:['Completed']
      };
      if(!allowed[current.status].includes(input.status))return fail(400,'This status change is not allowed.');
      if(current.status==='Completed'&&!input.correction_reason)return fail(400,'Enter the correction reason.');
      const count=current.details.requirements.length;
      if(input.checklist.length!==count||input.missing_items.some(index=>index>=count)||
        new Set(input.missing_items).size!==input.missing_items.length)
        return fail(400,'Review the document checklist.');
      if(input.status==='For correction'&&(!input.missing_items.length||!input.next_action))
        return fail(400,'Enter the missing items and next action.');
      await client.query(`INSERT INTO crm_service_request_reviews
        (request_id,reviewer_id,from_status,to_status,checklist,feedback,missing_items,next_action,internal_remark,deadline_at,checkpoint,correction_reason)
        VALUES($1,$2,$3,$4,$5,$6,$7,$8,$9,$10,$11,$12)`,[id,req.user.id,current.status,input.status,
        JSON.stringify(input.checklist),input.feedback,JSON.stringify(input.missing_items),input.next_action,
        input.internal_remark,input.deadline_at,input.checkpoint,input.correction_reason??null]);
      await client.query('UPDATE crm_service_requests SET status=$2,version=version+1 WHERE id=$1',[id,input.status]);
      const kind=input.status==='For correction'?'correction':input.status==='Completed'?'completion':'review';
      await update(client,id,kind);
      const previousDeadline=current.prior_deadline?.toISOString()??null;
      const newDeadline=input.deadline_at?new Date(input.deadline_at).toISOString():null;
      if(previousDeadline!==newDeadline){
        await client.query("DELETE FROM crm_service_request_updates WHERE request_id=$1 AND kind='deadline' AND available_at>now()",[id]);
        if(newDeadline)await client.query(`INSERT INTO crm_service_request_updates(request_id,kind,available_at)
          SELECT $1,'deadline',greatest(now(),$2::timestamptz-interval '24 hours') WHERE $2::timestamptz>now()`,[id,newDeadline]);
      }
      return {value:await summary(client,id)};
    });
    reply(res,result);
  });
  app.use('/api/v1/service-requests',researcher);
  app.use('/api/v1/staff/service-requests',staff);
}
