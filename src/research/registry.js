import {Router,raw} from 'express';
import {createHash} from 'node:crypto';
import {id,validate,z} from '../security/validation.js';
import {validateIsolated} from '../storage/isolated-validation.js';
import {requestLimiter} from '../security/limiter.js';

const text=max=>z.string().trim().max(max);
const period=z.union([z.literal(''),z.string().regex(/^AY [12][0-9]{3}-[12][0-9]{3}$/)
  .refine(value=>+value.slice(8)===+value.slice(3,7)+1)]);
const schema=z.strictObject({
  title:text(200).min(1),abstract:text(4000).default(''),college:text(100).default(''),
  academic_period:period.default(''),adviser:text(160).default(''),registrant_id:id,
  member_ids:z.array(id).min(1).max(21),historical:z.boolean().default(false),
  source_reference:text(500).default(''),original_registration_date:z.iso.date().nullable().default(null),
  reason:text(1000).default(''),duplicate_confirmed:z.boolean().default(false),
  duplicate_reason:text(1000).default(''),submission_token:z.uuid(),expected_version:id.optional()
});
const params=validate(z.strictObject({id}),'params');
const errors={
  denied:[403,'Access denied.'],missing:[404,'Research record not found.'],
  members:[400,'This researcher is already a team member.'],account:[400,'Select existing Researcher accounts.'],
  registrant:[400,'Keep the recorded registrant as a team member.'],
  reason:[400,'Enter a correction reason.'],source:[400,'Enter the historical source reference.'],
  adviser:[400,'Please enter the adviser name.'],required:[400,'Enter the college and academic period.'],
  conflict:[409,'This research record was updated. Reload before saving.'],
  duplicate:[409,'Possible duplicate research record.'],duplicate_reason:[400,'Enter a reason to create a separate record.'],
  token:[409,'This entry was already submitted. Refresh the registry.']
};
let uploads=0;
export function registryRoutes(app,db,auth,{scanner}){
  const router=Router();
  router.use(auth,async(_req,res,next)=>{
    res.set('Cache-Control','no-store');
    if(!(await db.query('SELECT crm_manages_registry() AS allowed')).rows[0].allowed)
      return res.status(403).json({error:'Access denied.'});
    next();
  });
  router.get('/candidate',validate(z.strictObject({email:z.email().max(254).transform(value=>value.toLowerCase())}),'query'),
    async(req,res)=>{
      const result=(await db.query('SELECT crm_registry_candidate($1) AS candidate',[req.validated.query.email])).rows[0].candidate;
      if(!result)return res.status(404).json({error:'No matching Researcher account.'});
      res.json(result);
    });
  router.get('/:id',params,async(req,res)=>{
    const record=(await db.query('SELECT crm_registry_record($1) AS record',[req.validated.params.id])).rows[0].record;
    if(!record)return res.status(404).json({error:'Research record not found.'});
    res.json(record);
  });
  async function save(req,res){
    const target=req.validated.params?.id??null;
    const input=req.validated.body;
    if(target!==null&&input.expected_version===undefined)return res.status(400).json({error:'Reload the record before correcting it.'});
    const result=(await db.query('SELECT crm_registry_save($1,$2,$3) AS result',
      [target,input.expected_version??null,input])).rows[0].result;
    if(result.error){
      const [status,error]=errors[result.error]??[400,'Review the registry details.'];
      return res.status(status).json({error:result.matching_id?error+' REG-'+result.matching_id+' · '+result.matching_title:error,...(result.matching_id?{
        matching_id:result.matching_id,matching_title:result.matching_title
      }:{})});
    }
    res.status(target===null?201:200).json(result.record);
  }
  router.post('/',validate(schema),save);
  router.post('/:id/corrections',params,validate(schema),save);
  router.post('/:id/documents',params,requestLimiter(db,{capacity:12,perSecond:1/30,scope:'registry-files'}),
    validate(z.strictObject({filename:text(185).min(1),upload_token:z.uuid(),previous_id:z.uuid().optional()}),'query'),
    (req,res,next)=>{
      if(!scanner||uploads>=2)return res.status(503).json({error:'Document upload is unavailable. Try again.'});
      if(!req.is('application/octet-stream'))return res.status(415).json({error:'Upload a PDF or DOCX file.'});
      uploads++;
      raw({type:'application/octet-stream',limit:10_000_000})(req,res,error=>{if(error)uploads--;next(error);});
    },async(req,res)=>{
      try{
        const {filename,upload_token,previous_id}=req.validated.query;
        const mime=await validateIsolated(req.body,filename);
        if(!await scanner(req.body))return res.status(400).json({error:'Document rejected'});
        const digest=createHash('sha256').update(req.body).digest('hex');
        const result=(await db.query('SELECT crm_registry_file_save($1,$2,$3,$4,$5,$6,$7) AS result',
          [req.validated.params.id,previous_id??null,upload_token,filename,mime,req.body,digest])).rows[0].result;
        if(result!=='saved'){
          const [status,error]=errors[result]??[409,'Document version changed. Reload the version list.'];
          return res.status(status).json({error});
        }
        res.status(201).json({saved:true});
      }finally{uploads--;}
    });
  for(const prefix of ['/api/v1/staff/registry','/api/v1/researcher/registrations']){
    app.get(prefix+'/:id/documents',auth,params,async(req,res)=>{
      const records=await db.query('SELECT * FROM crm_registry_file_list($1)',[req.validated.params.id]);
      res.set('Cache-Control','no-store').json(records.rows);
    });
    app.get(prefix+'/:id/documents/:file',auth,validate(z.strictObject({id,file:z.uuid()}),'params'),async(req,res)=>{
      const {id,file}=req.validated.params;
      const saved=(await db.query('SELECT * FROM crm_registry_file_get($1,$2)',[id,file])).rows[0];
      if(!saved)return res.status(404).json({error:'Document not found.'});
      res.set('Cache-Control','no-store').attachment(saved.filename).type(saved.mime).send(saved.bytes);
    });
  }
  app.use('/api/v1/staff/registry',router);
}
