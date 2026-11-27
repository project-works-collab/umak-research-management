import {createHmac,timingSafeEqual} from 'node:crypto';
import express from 'express';
import {validate,z} from '../src/security/validation.js';
import {requestLimiter} from '../src/security/limiter.js';

export const acceptsDemoAddress=address=>/^(?:demo[.-][a-z0-9.-]+|(?:student|faculty)\.demo[0-9]+)@umak\.edu\.ph$/.test(address);
const equal=(a,b)=>{const x=Buffer.from(a??''),y=Buffer.from(b??'');return x.length===y.length&&timingSafeEqual(x,y);};
const escape=value=>String(value).replace(/[&<>"']/g,character=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[character]));
function page(title,body){return `<!doctype html><html lang="en"><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>${title}</title><body><main><h1>${title}</h1><p>Synthetic UMak CRM demo. No email is sent to university mailboxes.</p>${body}</main></body></html>`;}

export function hostedDemo(pool,env=process.env){
  if(env.CRM_DEMO_MODE!=='umak')return null;
  if(env.ALLOW_SYNTHETIC_SETUP!=='yes'||!env.DEMO_PASSWORD||env.DEMO_PASSWORD.length<12||!env.JWT_SIGNING_KEY)
    throw Error('Hosted UMak demo requires explicit synthetic setup, demo password and signing key');
  const key=createHmac('sha256',env.JWT_SIGNING_KEY).update('umak-demo-verification-v1').digest();
  const sign=value=>createHmac('sha256',key).update('demo-inbox:'+value).digest('hex');
  const passwordDigest=value=>createHmac('sha256',key).update(value).digest('hex');
  const validCookie=req=>{
    const token=(req.get('cookie')??'').split(';').map(value=>value.trim()).find(value=>value.startsWith('crm_demo_inbox='))?.slice(15)??'';
    const [expiry,signature]=token.split('.');
    return /^\d{13}$/.test(expiry??'')&&Number(expiry)>Date.now()&&Number(expiry)<=Date.now()+8*3600_000&&equal(signature,sign(expiry));
  };
  return {
    verification:{key,domains:['umak.edu.ph'],acceptAddress:acceptsDemoAddress,async send(recipient,code){
      if(!acceptsDemoAddress(recipient))throw Error('Reserved demo addresses only');
      await pool.query('INSERT INTO crm_demo_mail(recipient,body) VALUES($1,$2)',
        [recipient,`Your verification code is ${code}. It expires in 15 minutes.`]);
    }},
    // Demo files still undergo format/size validation. This synthetic environment does not claim antivirus verification.
    scanner:async()=>true,
    mount(app){
      const router=express.Router();
      router.use((_req,res,next)=>{res.set({'Cache-Control':'no-store','X-Robots-Tag':'noindex, nofollow'});next();});
      router.get('/',async(req,res)=>{
        if(!validCookie(req))return res.type('html').send(page('Demo verification inbox',
          '<form method="post" action="/demo/inbox/login"><label>Demo password <input name="password" type="password" required autocomplete="current-password"></label><button>Open inbox</button></form>'));
        const messages=(await pool.query("SELECT recipient,body,created_at FROM crm_demo_mail WHERE created_at>now()-interval '15 minutes' ORDER BY id DESC LIMIT 100")).rows;
        res.type('html').send(page('Demo verification inbox',`<p><a href="/demo/inbox">Refresh inbox</a></p><p>Codes expire after 15 minutes. Use the code for your exact signup address.</p>${messages.length?messages.map(message=>`<article><h2>${escape(message.recipient)}</h2><p>${escape(message.body)}</p><time>${escape(message.created_at.toISOString())}</time></article>`).join(''):'<p>No recent verification codes. Create an account in the Android app first.</p>'}<form method="post" action="/demo/inbox/logout"><button>Close inbox</button></form>`));
      });
      router.post('/login',requestLimiter(pool,{scope:'demo-inbox-login',capacity:10,perSecond:1/90}),express.urlencoded({extended:false,limit:'2kb'}),validate(z.strictObject({password:z.string().max(128)})),async(req,res)=>{
        if(typeof req.body?.password!=='string'||!equal(passwordDigest(req.body.password),passwordDigest(env.DEMO_PASSWORD)))
          return res.status(403).type('html').send(page('Inbox access denied','<p>Incorrect demo password. <a href="/demo/inbox">Try again</a></p>'));
        const expiry=String(Date.now()+8*3600_000);
        res.set('Set-Cookie',`crm_demo_inbox=${expiry}.${sign(expiry)}; HttpOnly; Secure; SameSite=Strict; Path=/demo/inbox; Max-Age=28800`);
        res.redirect(303,'/demo/inbox');
      });
      router.post('/logout',(_req,res)=>{
        res.set('Set-Cookie','crm_demo_inbox=; HttpOnly; Secure; SameSite=Strict; Path=/demo/inbox; Max-Age=0');res.redirect(303,'/demo/inbox');
      });
      app.use('/demo/inbox',router);
      app.get('/api/v1/demo/info',(_req,res)=>res.set('Cache-Control','no-store').json({mode:'synthetic',domain:'umak.edu.ph',
        inbox:'/demo/inbox',signup_examples:['student.demo01@umak.edu.ph','faculty.demo01@umak.edu.ph'],
        antivirus:env.CLAMAV_HOST?'configured ClamAV; signature freshness and detection must be verified':'synthetic files only; no antivirus certification',tfidf:env.TFIDF_REVIEW_ENABLED==='true'&&env.TFIDF_SERVICE_URL&&env.TFIDF_SERVICE_TOKEN?'configured; readiness must be verified':'not integrated'}));
    }
  };
}
