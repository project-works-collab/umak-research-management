import {spawn,execFileSync} from 'node:child_process';
import {mkdtemp,readFile,rm} from 'node:fs/promises';
import {tmpdir,homedir} from 'node:os';
import {join} from 'node:path';
import {once} from 'node:events';

// Builds and tests against a disposable database and a locally trusted HTTPS certificate.
// Never reads .env or connects to the deployment database.
const directory=await mkdtemp(join(tmpdir(),'crm-android-acceptance-'));
const adb=process.env.ADB??join(homedir(),'Library/Android/sdk/platform-tools/adb');
let fixture,port;
const run=(file,args,options={})=>execFileSync(file,args,{stdio:'inherit',...options});
try{
  const cert=join(directory,'cert.pem'),key=join(directory,'key.pem');
  run('openssl',['req','-x509','-newkey','rsa:2048','-nodes','-keyout',key,'-out',cert,'-days','1',
    '-subj','/CN=localhost','-addext','subjectAltName=IP:127.0.0.1,DNS:localhost'],{stdio:'ignore'});
  run('./gradlew',[':app:assembleDebug'],{cwd:'android'});
  fixture=spawn(process.execPath,['scripts/synthetic-acceptance.js'],{
    env:{...process.env,CRM_SYNTHETIC_TLS_CERT:cert,CRM_SYNTHETIC_TLS_KEY:key},stdio:['ignore','pipe','inherit']});
  let output='';
  const ready=new Promise((resolve,reject)=>{
    const timer=setTimeout(()=>reject(Error('Synthetic HTTPS server did not become ready')),60_000);
    fixture.once('exit',code=>{clearTimeout(timer);reject(Error('Synthetic server stopped: '+code));});
    fixture.stdout.on('data',chunk=>{
      output+=chunk.toString();
      if(output.includes('Press Ctrl+C')){clearTimeout(timer);resolve();}
    });
  });
  await ready;
  const origin=output.match(/Synthetic API: (\S+)/)?.[1];
  const fixtures=output.match(/Fixtures and demo password: (.+)/)?.[1];
  if(!origin||!fixtures)throw Error('Synthetic server did not provide its fixture paths');
  port=new URL(origin).port;
  run(adb,['reverse','tcp:'+port,'tcp:'+port]);
  run(adb,['install','-r','android/app/build/outputs/apk/debug/app-debug.apk']);
  run(adb,['shell','run-as ph.edu.umak.crm.app mkdir -p files']);
  run(adb,['shell',"run-as ph.edu.umak.crm.app sh -c 'cat > files/catalog-test-password'"],
    {stdio:['pipe','inherit','inherit'],input:await readFile(join(fixtures,'demo-password.txt'))});
  const der=execFileSync('openssl',['x509','-in',cert,'-outform','DER']);
  run('./gradlew',[':app:connectedDebugAndroidTest',':app:lintDebug',
    ...(process.env.CRM_ANDROID_TEST_CLASS?['-Pandroid.testInstrumentationRunnerArguments.class='+process.env.CRM_ANDROID_TEST_CLASS]:[]),
    '-Pandroid.testInstrumentationRunnerArguments.catalogOrigin='+origin,
    '-Pandroid.testInstrumentationRunnerArguments.catalogCert='+der.toString('base64')],{cwd:'android'});
}finally{
  if(port)try{run(adb,['reverse','--remove','tcp:'+port],{stdio:'ignore'});}catch{}
  if(fixture&&fixture.exitCode===null){fixture.kill('SIGTERM');await once(fixture,'exit');}
  await rm(directory,{recursive:true,force:true});
}
