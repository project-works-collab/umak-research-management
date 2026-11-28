import {spawn} from 'node:child_process';
import {randomBytes} from 'node:crypto';
import {mkdtempSync,readFileSync,writeFileSync,rmSync} from 'node:fs';
import {tmpdir} from 'node:os';
import {join,resolve} from 'node:path';

if(process.env.CRM_DEMO_MODE!=='umak')throw Error('Hosted demo mode required');
if(!process.env.TFIDF_DATABASE_URL)throw Error('Configure the dedicated TF-IDF reader connection');
const reader=new URL(process.env.TFIDF_DATABASE_URL);
if(!/^crm_tfidf(?:\.|$)/.test(decodeURIComponent(reader.username)))throw Error('TF-IDF must use its dedicated read-only database role');
const ca=process.env.DB_TLS_CA_PEM??(readFileSync(process.env.DB_TLS_CA_FILE??new URL('../config/supabase-prod-ca-2021.crt',import.meta.url),'utf8'));
if(!ca?.includes('-----BEGIN CERTIFICATE-----'))throw Error('Trusted database CA is required');
const scratch=mkdtempSync(join(tmpdir(),'crm-hosted-'));
const caFile=join(scratch,'database-ca.pem');writeFileSync(caFile,ca,{mode:0o600});
const token=randomBytes(32).toString('hex');
const python=process.env.TFIDF_PYTHON_BIN??resolve('.render-tfidf/bin/python');
// The scorer stays on loopback and shares no public port with the API.
for(const key of ['sslrootcert','sslcert','sslkey','sslmode'])reader.searchParams.delete(key);
reader.searchParams.set('sslmode','verify-full');
reader.searchParams.set('sslrootcert',caFile);
reader.searchParams.set('ssl_min_protocol_version','TLSv1.3');
const children=[];
let stopping=false;
function stop(code=0){
 if(stopping)return;stopping=true;
 for(const child of children)child.kill('SIGTERM');
 const timer=setTimeout(()=>{for(const child of children)child.kill('SIGKILL');rmSync(scratch,{recursive:true,force:true});process.exit(code);},8000);
 Promise.all(children.map(child=>new Promise(done=>{if(child.exitCode!==null||child.signalCode!==null)done();else child.once('exit',done);})))
 .then(()=>{clearTimeout(timer);rmSync(scratch,{recursive:true,force:true});process.exit(code);});
}
function start(command,args,options){
 const child=spawn(command,args,{...options,stdio:'inherit'});children.push(child);
 child.once('error',()=>{console.error('Hosted child process could not start');stop(1);});
 child.once('exit',code=>{if(!stopping){console.error('Hosted child process exited');stop(code||1);}});
 return child;
}
process.on('SIGTERM',()=>stop());process.on('SIGINT',()=>stop());
start(python,['-m','uvicorn','app:app','--host','127.0.0.1','--port','8765'],{
 cwd:resolve('services/tfidf'),
 env:{PATH:process.env.PATH,LANG:'C.UTF-8',TFIDF_DATABASE_URL:reader.href,TFIDF_SERVICE_TOKEN:token,
 OPENBLAS_NUM_THREADS:'1',OMP_NUM_THREADS:'1',MKL_NUM_THREADS:'1'}
});
try{
 let ready=false;
 for(let attempt=0;attempt<90&&!stopping;attempt++){
  try{ready=(await fetch('http://127.0.0.1:8765/ready',{signal:AbortSignal.timeout(3000)})).ok;}catch{}
  if(ready)break;
  await new Promise(done=>setTimeout(done,1000));
 }
 if(!ready||stopping)throw Error('Scorer readiness failed');
 start(process.execPath,['demo/render-server.js'],{env:{...process.env,NODE_ENV:'production',
 DB_TLS_CA_FILE:caFile,TFIDF_REVIEW_ENABLED:'true',TFIDF_SERVICE_URL:'http://127.0.0.1:8765',TFIDF_SERVICE_TOKEN:token,PYTHON_BIN:python}});
}catch{console.error('TF-IDF archive could not become ready; check migrations, reader credentials and database CA');stop(1);}
