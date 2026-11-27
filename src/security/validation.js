import {z} from 'zod';
export const id=z.union([z.number(),z.string().regex(/^[1-9][0-9]*$/)]).pipe(z.coerce.number().int().min(1).max(2147483647));
const text=max=>z.string().trim().min(1).max(max);
export const research=z.strictObject({title:text(200),abstract:z.string().max(4000).default(''),research_year:z.number().int().min(1900).max(9999).nullable().default(null),department:text(100).nullable().default(null)});
export const login=z.strictObject({email:z.email().max(254).transform(x=>x.toLowerCase()),password:z.string().min(1).max(128)});
export const feedback=z.strictObject({researcher_visible_feedback:z.string().max(4000),internal_staff_notes:z.string().max(4000)});
export const member=z.strictObject({user_id:id});
export const validationFields=issues=>Object.fromEntries(issues.map(issue=>[issue.path.join('.')||'_form','Check this field.']));
export const validate=(schema,source='body',includeFields=false)=>{
  const middleware=(req,res,next)=>{
    const parsed=schema.safeParse(req[source]??{});
    if(!parsed.success)return res.status(400).json({error:'Invalid request',request_id:req.requestId,
      ...(includeFields?{field_errors:validationFields(parsed.error.issues)}:{})});
    req.validated??={};req.validated[source]=parsed.data;next();
  };
  middleware.validationSource=source;
  return middleware;
};

// Install after route registration. Validate otherwise-unused request sources
// immediately before the endpoint handler, after auth, parsers and named schemas.
// New endpoints inherit strict empty body/query contracts unless they opt into a
// named schema. Binary uploads retain their route-specific parser/content checks.
export function completeRequestValidation(app){
  const seen=new Set();
  function visit(stack){
    if(!stack||seen.has(stack))return;
    seen.add(stack);
    for(const layer of stack){
      if(layer.route){
        const route=layer.route;
        if(typeof route.path==='string'&&route.path.startsWith('/demo'))continue;
        const declared=new Set(route.stack.map(item=>item.handle.validationSource).filter(Boolean));
        const defaults=(req,res,next)=>{
          req.validated??={};
          for(const source of ['params','query','body']){
            if(declared.has(source))continue;
            let schema=z.strictObject({});
            if(source==='params'){
              const fields={};
              for(const key of Object.keys(req.params??{})){
                fields[key]=['id','studyId','memberId'].includes(key)?id:
                  ['documentId','file'].includes(key)?z.uuid():z.string().min(1).max(200);
              }
              schema=z.strictObject(fields);
            }else if(source==='body'&&Buffer.isBuffer(req.body)){
              schema=z.instanceof(Buffer).refine(value=>value.length>0&&value.length<=25000000);
            }
            const parsed=schema.safeParse(req[source]??{});
            if(!parsed.success)return res.status(400).json({error:'Invalid request',request_id:req.requestId});
            req.validated[source]=parsed.data;
          }
          next();
        };
        // Express route.dispatch reads this ordered stack; create a real Layer
        // through Route.all, then move it ahead of the final endpoint handler.
        const hadAll=route.methods._all;
        route.all(defaults);
        if(!hadAll)delete route.methods._all;
        const inserted=route.stack.pop();
        route.stack.splice(route.stack.length-1,0,inserted);
      }else visit(layer.handle?.stack);
    }
  }
  visit(app.router.stack);
}
export {z};

export const search=z.strictObject({keyword:z.string().max(200),year:z.number().int().min(1900).max(9999).nullable().optional(),department:text(100).nullable().optional(),limit:z.number().int().min(1).max(100).optional(),offset:z.number().int().min(0).max(10000).optional()});
export const duplicateReview=z.strictObject({title:text(200).refine(value=>/[\p{L}\p{N}]/u.test(value.normalize('NFKC'))),abstract:text(4000),year:z.number().int().min(1900).max(9999).nullable().optional(),department:text(100).nullable().optional(),exclude_id:id.optional()});
export const consent=z.strictObject({policy_version:z.literal('2026-10-v1'),choices:z.strictObject({institutional_analytics:z.boolean(),email_notifications:z.boolean()})});
export const empty=z.strictObject({});
const step=z.string().regex(/^[A-Za-z0-9_-]{1,64}$/);
const gate=z.enum(['received','verified']);
export const algorithm=z.strictObject({steps:z.array(step).min(1).max(5000),dependencies:z.array(z.tuple([step,step])).max(25000),completed_actions:z.array(step).max(5000),requirements:z.record(step,z.array(z.strictObject({id:step,gate})).max(50000)),evidence:z.array(z.strictObject({id:step,state:gate})).max(50000),durations:z.record(step,z.number().int().min(0).max(525600))});
export const deadline=z.string().datetime({precision:3}).or(z.string().datetime({precision:0})).nullable();
export const ticket=z.strictObject({service_type:text(100),deadline_at:deadline.optional(),submission_token:z.uuid().optional()});
export const version=z.strictObject({expected_version:z.number().int().min(1).max(2147483647)});
