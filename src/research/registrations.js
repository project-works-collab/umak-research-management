import {Router} from 'express';
import {createHash} from 'node:crypto';
import {id,validate,z} from '../security/validation.js';

const text=max=>z.string().trim().max(max);
const period=z.string().regex(/^AY [12][0-9]{3}-[12][0-9]{3}$/)
  .refine(value=>+value.slice(8)===+value.slice(3,7)+1);
const schema=z.strictObject({
  submission_token:z.uuid(),draft_id:id.nullable().default(null),
  title:text(200).min(1),college:text(100).min(1),academic_period:period,
  adviser:text(160).default(''),member_ids:z.array(id).max(20).default([]),
  abstract:text(4000).default(''),keywords:text(1000).default(''),methodology:text(1000).default(''),
  expected_outcomes:text(2000).default(''),research_type:text(120).default(''),
  target_date:z.iso.date().nullable().default(null),declarations:z.array(text(240)).max(10).default([])
});
const columns=`r.id,r.title,r.department,r.abstract,r.lifecycle_state AS stage,r.author_id,
  g.academic_period,g.adviser,g.registrant_category,g.registered_at,g.registration_details`;
const source='FROM crm_research_registrations g JOIN research_records r ON r.id=g.study_id';
const fail=(status,error)=>({status,error});

async function savedRegistration(client,study){
  const {rows:[row]}=await client.query(`SELECT ${columns} ${source} WHERE r.id=$1`,[study]);
  return {...row,research_id:'REG-'+row.id};
}

export function researchRegistrationRoutes(app,db,auth){
  const router=Router();
  router.use(auth,(req,res,next)=>{
    res.set('Cache-Control','no-store');
    if(!['student','faculty'].includes(req.user.role))return res.status(403).json({error:'Access denied'});
    next();
  });
  router.get('/team-candidate',validate(z.strictObject({email:z.email().max(254).transform(x=>x.toLowerCase())}),'query'),
    async(req,res)=>{
      const {rows:[person]}=await db.query('SELECT * FROM crm_registration_candidate($1)',[req.validated.query.email]);
      if(!person)return res.status(404).json({error:'No matching active researcher account.'});
      res.json(person);
    });

  router.get('/',async(_req,res)=>{
    const {rows}=await db.query(`SELECT r.id,r.title,r.department,r.lifecycle_state AS stage,g.registered_at
      ${source} ORDER BY g.registered_at DESC,r.id DESC`);
    res.json(rows.map(row=>({...row,research_id:'REG-'+row.id})));
  });
  router.get('/:id',validate(z.strictObject({id}),'params'),async(req,res)=>{
    const study=req.validated.params.id;
    const {rows:[row]}=await db.query(`SELECT ${columns} ${source} WHERE r.id=$1`,[study]);
    if(!row)return res.status(404).json({error:'Research registration not found.'});
    const team=await db.query('SELECT * FROM crm_registration_team($1)',[study]);
    res.json({...row,research_id:'REG-'+row.id,team:team.rows});
  });
  router.post('/',validate(schema),async(req,res)=>{
    const body=req.validated.body;
    const owner=req.user.id;
    if(req.user.role==='student'&&!body.adviser)return res.status(400).json({error:'Please enter the adviser name.'});
    if(body.member_ids.includes(owner)||new Set(body.member_ids).size!==body.member_ids.length){
      return res.status(400).json({error:'This researcher is already a team member.'});
    }
    const members=[...body.member_ids].sort((a,b)=>a-b);
    const digest=createHash('sha256').update(JSON.stringify({...body,member_ids:members,
      declarations:[...body.declarations].sort()})).digest('hex');
    const client=await db.connect();
    const submit=async()=>{
      await client.query('SELECT pg_advisory_xact_lock(7610,$1)',[owner]);
      const {rows:[prior]}=await client.query(`SELECT study_id,fingerprint FROM crm_research_registrations
        WHERE registrant_id=$1 AND submission_token=$2`,[owner,body.submission_token]);
      if(prior)return prior.fingerprint===digest?{status:200,value:await savedRegistration(client,prior.study_id)}
        :fail(409,'Submission token already used.');
      const {rows:[eligible]}=await client.query('SELECT crm_eligible_registration_members($1::int[]) AS count',[members]);
      if(eligible.count!==members.length)return fail(400,'Select an existing active researcher account.');
      const core=[body.title,body.abstract,+body.academic_period.slice(3,7),body.college,owner];
      let study=body.draft_id;
      if(study===null){
        const result=await client.query(`INSERT INTO research_records(title,abstract,research_year,department,author_id,status)
          VALUES($1,$2,$3,$4,$5,'Draft') RETURNING id`,core);
        study=result.rows[0].id;
      }else{
        const result=await client.query(`UPDATE research_records r SET title=$1,abstract=$2,research_year=$3,department=$4
          WHERE r.author_id=$5 AND r.id=$6 AND r.status='Draft' AND r.lifecycle_state='Registered'
          AND NOT r.is_registered RETURNING r.id`,[...core,study]);
        if(!result.rowCount)return fail(409,'This draft cannot be registered. Reopen the latest record.');
      }
      await client.query('DELETE FROM study_members WHERE study_id=$1 AND user_id<>$2',[study,owner]);
      await client.query(`INSERT INTO study_members(study_id,user_id)
        SELECT $1,unnest($2::int[]) ON CONFLICT DO NOTHING`,[study,[owner,...members]]);
      const details={research_type:body.research_type,target_date:body.target_date,keywords:body.keywords,
        methodology:body.methodology,expected_outcomes:body.expected_outcomes,declarations:body.declarations};
      await client.query(`INSERT INTO crm_research_registrations
        (study_id,registrant_id,registrant_category,submission_token,fingerprint,academic_period,adviser,registration_details)
        VALUES($1,$2,$3,$4,$5,$6,$7,$8)`,[study,owner,req.user.role,body.submission_token,digest,
        body.academic_period,body.adviser,details]);
      await client.query('UPDATE research_records SET is_registered=true WHERE id=$1',[study]);
      await client.query("INSERT INTO audit_logs(actor_id,action) VALUES($1,'research.registered')",[owner]);
      return {status:201,value:await savedRegistration(client,study)};
    };
    try{
      await client.query('BEGIN');
      const result=await submit();
      await client.query(result.error?'ROLLBACK':'COMMIT');
      res.status(result.status).json(result.value??{error:result.error});
    }catch(error){
      await client.query('ROLLBACK');
      throw error;
    }finally{
      client.release();
    }
  });

  const registry=Router();
  registry.use(auth,async(req,res,next)=>{
    const permission=await db.query("SELECT 1 FROM crm_staff_permissions WHERE user_id=$1 AND permission='registry_management'",[req.user.id]);
    if(req.user.role!=='staff'||!permission.rowCount)return res.status(403).json({error:'Access denied'});
    res.set('Cache-Control','no-store');
    next();
  });
  registry.get('/',validate(z.strictObject({keyword:text(200).default(''),college:text(100).default(''),
    academic_period:text(40).default('')}),'query'),async(req,res)=>{
    const {keyword,college,academic_period}=req.validated.query;
    const {rows}=await db.query(`SELECT * FROM crm_registrations_for_registry() r
      WHERE ($1='' OR strpos(lower(concat_ws(' ', 'REG-'||r.id,r.title,r.registrant_name,r.adviser)),lower($1))>0
        OR EXISTS(SELECT 1 FROM crm_registration_team(r.id) t WHERE strpos(lower(t.name),lower($1))>0))
        AND ($2='' OR r.department=$2) AND ($3='' OR r.academic_period=$3)
      ORDER BY r.registered_at DESC,r.id DESC LIMIT 100`,[keyword,college,academic_period]);
    res.json(rows.map(row=>({...row,research_id:'REG-'+row.id})));
  });
  registry.get('/:id',validate(z.strictObject({id}),'params'),async(req,res)=>{
    const study=req.validated.params.id;
    const {rows:[row]}=await db.query('SELECT * FROM crm_registrations_for_registry() WHERE id=$1',[study]);
    if(!row)return res.status(404).json({error:'Research registration not found.'});
    const team=await db.query('SELECT * FROM crm_registration_team($1)',[study]);
    res.json({...row,research_id:'REG-'+row.id,team:team.rows});
  });
  app.use('/api/v1/researcher/registrations',router);
  app.use('/api/v1/staff/research-registrations',registry);
}
