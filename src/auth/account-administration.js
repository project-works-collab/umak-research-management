import {Router} from 'express';
import {randomBytes} from 'node:crypto';
import {hashPassword,verifyPassword,tokenHash} from '../crypto.js';
import {id,validate,z} from '../security/validation.js';
import {reserveLogin} from '../security/limiter.js';

export const permissionNames=['content_management','request_processing','registry_management','progress_updates','reporting','account_administration'];
const permissions=z.array(z.enum(permissionNames)).max(6).refine(items=>new Set(items).size===items.length);
const text=max=>z.string().trim().min(1).max(max);
const errors={
  denied:[403,'Access denied.'],missing:[404,'Account not found.'],
  duplicate:[409,'This institutional email is already registered.'],
  self:[400,'You cannot deactivate your own account.'],last_admin:[400,'Keep at least one active account administrator.'],
  researcher_permissions:[400,'Researcher accounts cannot receive staff permissions.'],
  category:[400,'Select Student or Faculty.'],reason:[400,'Enter the approved recovery reason.']
};

export function accountAdministrationRoutes(app,db,auth,{verification}){
  const accounts=Router();
  accounts.use(auth,async(_req,res,next)=>{
    res.set('Cache-Control','no-store');
    const permission=await db.query('SELECT crm_administers_accounts() AS allowed');
    if(!permission.rows[0].allowed)return res.status(403).json({error:'Access denied.'});
    next();
  });
  accounts.get('/',async(_req,res)=>res.json((await db.query('SELECT * FROM crm_account_directory()')).rows));
  accounts.get('/:id/history',validate(z.strictObject({id}),'params'),async(req,res)=>
    res.json((await db.query('SELECT * FROM crm_account_audit($1)',[req.validated.params.id])).rows));

  async function mutate(req,res,target,operation,payload,temporary){
    const {rows:[result]}=await db.query('SELECT crm_account_mutation($1,$2,$3) AS result',[target,operation,payload]);
    if(result.result.error){
      const [status,error]=errors[result.result.error]??[400,'Check the account information.'];
      return res.status(status).json({error});
    }
    return res.status(operation==='create'?201:200).json({...result.result,...(temporary?{temporary_credential:temporary}:{})});
  }
  accounts.post('/',validate(z.strictObject({email:z.email().max(254).transform(x=>x.toLowerCase()),
    display_name:text(160),college:text(160),permissions})),async(req,res)=>{
    const input=req.validated.body;
    if(!verification)return res.status(503).json({error:'Institutional email domains are not configured.'});
    if(!verification.domains.includes(input.email.split('@').at(-1))||(verification.acceptAddress&&!verification.acceptAddress(input.email)))return res.status(400).json({error:'Enter an accepted institutional email.'});
    const temporary=randomBytes(18).toString('base64url');
    await mutate(req,res,null,'create',{...input,password_hash:await hashPassword(temporary)},temporary);
  });
  for(const [operation,schema] of [
    ['state',z.strictObject({active:z.boolean(),confirmed:z.literal(true)})],
    ['permissions',z.strictObject({permissions})],
    ['category',z.strictObject({category:z.enum(['student','faculty'])})],
    ['recovery',z.strictObject({reason:text(1000),confirmed:z.literal(true)})]
  ])accounts.post('/:id/'+operation,validate(z.strictObject({id}),'params'),validate(schema),async(req,res)=>{
    const temporary=operation==='recovery'?randomBytes(18).toString('base64url'):null;
    await mutate(req,res,req.validated.params.id,operation,{...req.validated.body,
      ...(temporary?{password_hash:await hashPassword(temporary)}:{})},temporary);
  });

  app.post('/api/v1/auth/replace-temporary-password',validate(z.strictObject({
    challenge:z.string().regex(/^[a-f0-9]{64}$/),password:z.string().min(12).max(128),confirmation:z.string().max(128)
  })),async(req,res)=>{
    res.set('Cache-Control','no-store');
    const {challenge,password,confirmation}=req.validated.body;
    if(password!==confirmation)return res.status(400).json({error:'Passwords do not match.'});
    if(!await reserveLogin(db,'temporary-password:'+tokenHash(challenge),req.ip,req.requestId))
      return res.status(429).json({error:'Too many attempts. Wait before trying again.'});
    const old=(await db.query('SELECT crm_challenge_password($1) AS hash',[tokenHash(challenge)])).rows[0].hash;
    if(!old)return res.status(400).json({error:'Temporary credential challenge expired. Sign in again.'});
    if(await verifyPassword(password,old))return res.status(400).json({error:'Choose a password different from your temporary password.'});
    const saved=await db.query('SELECT crm_finish_password_change($1,$2) AS saved',[tokenHash(challenge),await hashPassword(password)]);
    if(!saved.rows[0].saved)return res.status(409).json({error:'Sign in again before replacing the password.'});
    await db.query('SELECT crm_finish_login($1)',[req.requestId]);
    res.sendStatus(204);
  });
  app.use('/api/v1/staff/accounts',accounts);
}
