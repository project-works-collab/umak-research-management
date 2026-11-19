import {Router} from 'express';
import {validate,z} from '../security/validation.js';

const filter=z.string().trim().max(200).default('');
const schema=z.strictObject({
  type:z.enum(['research','service']),college:filter,academic_period:filter,
  category:z.enum(['','student','faculty']).default(''),
  stage:z.enum(['','Registered','In-Progress','UnderReview','Completed']).default(''),
  outcome:z.enum(['','completed','presented','published']).default(''),researcher:filter,
  adviser:filter,service:filter,from:z.iso.date().optional(),to:z.iso.date().optional(),
  status:z.enum(['','Submitted','In review','For correction','Completed']).default('')
}).refine(value=>!value.from||!value.to||value.from<=value.to);

export function makeReport(snapshot,filters){
  const same=(actual,expected)=>!expected||String(actual??'').toLowerCase()===expected.toLowerCase();
  const matchName=(actual,query)=>!query||String(actual??'').toLowerCase().includes(query.toLowerCase());
  const research=filters.type==='research';
  const matching=research?snapshot.research.filter(row=>
    same(row.college,filters.college)&&same(row.academic_period,filters.academic_period)&&
    same(row.stage,filters.stage)&&same(row.category,filters.category)&&(!filters.outcome||row[filters.outcome])&&
    matchName(row.adviser,filters.adviser)&&(!filters.researcher||row.researchers.some(person=>
      String(person.id)===filters.researcher||matchName(person.name,filters.researcher)))&&
    (!filters.service||row.services.includes(filters.service))
  ):snapshot.requests.filter(row=>
    same(row.college,filters.college)&&same(row.category,filters.category)&&same(row.status,filters.status)&&
    same(row.service_code,filters.service)&&(!filters.from||row.submitted_date>=filters.from)&&
    (!filters.to||row.submitted_date<=filters.to)
  );
  const metrics=research?[
    ['Distinct studies',matching.length],
    ['Completed',matching.filter(row=>row.completed).length],
    ['Presented',matching.filter(row=>row.presented).length],
    ['Published',matching.filter(row=>row.published).length],
    ['Distinct linked researchers',new Set(matching.flatMap(row=>row.researchers.map(person=>person.id))).size]
  ]:[
    ['Distinct requests',matching.length],
    ['Completed',matching.filter(row=>row.status==='Completed').length],
    ['Pending',matching.filter(row=>['Submitted','In review','For correction'].includes(row.status)).length]
  ];
  const definitions=research?[
    'Each matching registered research record is counted once, including studies with multiple linked service requests.',
    'Completed, presented and published counts are independent recorded outcomes.',
    'Linked researchers are distinct study members. Advisers count only when they are linked researchers.',
    'Research category uses the registrant category saved at registration. Period uses the study academic period.'
  ]:[
    'Each matching stored service request is counted once. Browsing, form downloads and research registration do not create request utilization.',
    'Pending includes Submitted, In review and For correction. Completed uses Completed status.',
    'Requester category and college use saved submission values. Period uses submission dates in Asia/Manila.'
  ];
  const keys=research?['college','academic_period','stage','outcome','category','researcher','adviser','service']:
    ['college','from','to','service','status','category'];
  const applied=Object.fromEntries(keys.map(key=>[key,filters[key]||'All']));
  const serviceCounts=new Map();
  if(!research)for(const row of matching)serviceCounts.set(row.service,(serviceCounts.get(row.service)??0)+1);
  return {
    type:filters.type,title:research?'Research Report':'Service Utilization',generated_at:snapshot.generated_at,
    filters:applied,definitions,metrics,empty:matching.length===0,
    service_counts:[...serviceCounts].sort(([a],[b])=>a.localeCompare(b)),
    columns:research?['Research ID','Title','Current stage']:['Request ID','Service','Status'],
    rows:matching.sort((a,b)=>a.id-b.id).map(row=>research?
      ['REG-'+row.id,row.title,row.stage]:['REQ-'+row.id,row.service,row.status])
  };
}

export function reportingRoutes(app,db,auth){
  const router=Router();
  router.use(auth,(_req,res,next)=>{res.set('Cache-Control','no-store');next();});
  router.post('/',validate(schema),async(req,res)=>{
    const result=await db.query('SELECT crm_reporting_snapshot() AS snapshot');
    const snapshot=result.rows[0].snapshot;
    if(!snapshot)return res.status(403).json({error:'Access denied.'});
    res.json(makeReport(snapshot,req.validated.body));
  });
  app.use('/api/v1/staff/reports',router);
}
