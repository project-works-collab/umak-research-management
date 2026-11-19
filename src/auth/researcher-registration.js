import {createHmac,randomInt} from 'node:crypto';
import {hashPassword} from '../crypto.js';
import {reserveLogin} from '../security/limiter.js';
import {validate,z} from '../security/validation.js';

const email=z.email().max(254).transform(value=>value.toLowerCase());
const registration=z.strictObject({
  email,display_name:z.string().trim().min(1).max(160),college:z.string().trim().min(1).max(160),
  role:z.enum(['student','faculty']),password:z.string().min(12).max(128),
  confirm_password:z.string().min(12).max(128)
}).refine(values=>values.password===values.confirm_password);

export function registerResearcherRoutes(app,db,verification){
  function allowedEmail(address){
    return verification.domains.includes(address.split('@').at(-1))&&(!verification.acceptAddress||verification.acceptAddress(address));
  }

  function digest(address,code){
    return createHmac('sha256',verification.key).update(address).update(':').update(code).digest('hex');
  }

  async function guard(req,res,next){
    if(!verification) return res.status(503).json({error:'Researcher email verification is unavailable'});
    const address=req.validated.body.email;
    if(!allowedEmail(address))return res.status(422).json({error:'Enter an accepted institutional email'});
    if(!await reserveLogin(db,`verification:${req.path}:${address}`,`verification:${req.path}:${req.ip}`,req.requestId)){
      return res.set('Retry-After','900').status(429).json({error:'Too many attempts. Wait before trying again.'});
    }
    res.set('Cache-Control','no-store');
    next();
  }

  async function sendCode(address,store){
    const code=String(randomInt(0,1_000_000)).padStart(6,'0');
    const client=await db.connect();
    try{
      await client.query('BEGIN');
      const result=await store(client,digest(address,code));
      if(result!=='sent'){
        await client.query('ROLLBACK');
        return result;
      }
      try{
        await verification.send(address,code);
      }catch{
        await client.query('ROLLBACK');
        return 'mail_unavailable';
      }
      await client.query('COMMIT');
      return result;
    }catch(error){
      await client.query('ROLLBACK');
      throw error;
    }finally{
      client.release();
    }
  }

  function deliveryResponse(res,result,status){
    if(result==='duplicate')return res.status(409).json({error:'This institutional email is already registered.'});
    if(result==='mail_unavailable'||result==='collision')return res.status(503).json({error:'Unable to send the verification email. Try again.'});
    if(result==='cooldown')return res.set('Retry-After','45').status(429).json({error:'Wait before requesting another code.'});
    if(result==='unavailable')return res.status(400).json({error:'Verification is unavailable for this account.'});
    return res.status(status).json({resend_after_seconds:45});
  }

  app.post('/api/v1/auth/researchers/register',validate(registration),guard,async(req,res)=>{
    const {email,display_name,college,role,password}=req.validated.body;
    const stored=await hashPassword(password);
    const result=await sendCode(email,async(client,hash)=>{
      const response=await client.query('SELECT crm_register_researcher($1,$2,$3,$4,$5,$6) AS created',
        [email,stored,display_name,college,role,hash]);
      return response.rows[0].created?'sent':'duplicate';
    });
    return deliveryResponse(res,result,201);
  });

  app.post('/api/v1/auth/researchers/resend',validate(z.strictObject({email})),guard,async(req,res)=>{
    const {email}=req.validated.body;
    const result=await sendCode(email,async(client,hash)=>(await client.query(
      'SELECT crm_resend_researcher_verification($1,$2) AS result',[email,hash])).rows[0].result);
    return deliveryResponse(res,result,200);
  });

  app.post('/api/v1/auth/researchers/verify',validate(z.strictObject({email,code:z.string().regex(/^\d{6}$/)})),guard,async(req,res)=>{
    const {email,code}=req.validated.body;
    const result=await db.query('SELECT crm_verify_researcher($1,$2) AS verified',[email,digest(email,code)]);
    if(!result.rows[0].verified){
      return res.status(400).json({error:'This verification link or code is invalid, expired or already used.'});
    }
    return res.sendStatus(204);
  });
}
