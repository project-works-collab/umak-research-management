import {spawn,execFileSync} from 'node:child_process';
import {mkdtemp,readFile,writeFile,mkdir,copyFile,rm} from 'node:fs/promises';
import {tmpdir,homedir} from 'node:os';
import {join,resolve} from 'node:path';
import {once} from 'node:events';
const directory=await mkdtemp(join(tmpdir(),'crm-manual-demo-'));
const adb=process.env.ADB??join(homedir(),'Library/Android/sdk/platform-tools/adb');
const outputDirectory=resolve('outputs/manual-demo');
const run=(file,args,options={})=>execFileSync(file,args,{stdio:'inherit',...options});
let fixture,port;
try{
  const cert=join(directory,'cert.pem'),key=join(directory,'key.pem');
  run('openssl',['req','-x509','-newkey','rsa:2048','-nodes','-keyout',key,'-out',cert,'-days','7',
    '-subj','/CN=localhost','-addext','subjectAltName=IP:127.0.0.1'],{stdio:'ignore'});
  fixture=spawn(process.execPath,['scripts/synthetic-acceptance.js','--umak-demo'],{
    env:{...process.env,CRM_SYNTHETIC_TLS_CERT:cert,CRM_SYNTHETIC_TLS_KEY:key},stdio:['ignore','pipe','inherit']});
  let output='';
  await new Promise((resolve,reject)=>{
    const timer=setTimeout(()=>reject(Error('Demo server startup timed out')),60_000);
    fixture.once('exit',code=>{clearTimeout(timer);reject(Error('Demo server stopped: '+code));});
    fixture.stdout.on('data',chunk=>{output+=chunk.toString();if(output.includes('Press Ctrl+C')){clearTimeout(timer);resolve();}});
  });
  const origin=output.match(/Synthetic API: (\S+)/)?.[1];
  const mailbox=output.match(/Mailbox: (\S+)/)?.[1];
  const fixtures=output.match(/Fixtures and demo password: (.+)/)?.[1];
  if(!origin||!mailbox||!fixtures)throw Error('Missing demo startup details');
  port=new URL(origin).port;
  const resources='android/app/build/generated/demoResources/raw';
  await mkdir(resources,{recursive:true});await copyFile(cert,join(resources,'crm_demo_ca.pem'));
  run('./gradlew',[':app:assembleDemo',':app:lintDemo','-PdemoOrigin='+origin],{cwd:'android'});
  run(adb,['reverse','tcp:'+port,'tcp:'+port]);
  run(adb,['install','-r','android/app/build/outputs/apk/demo/app-demo.apk']);
  run(adb,['shell','am','force-stop','ph.edu.umak.crm.app.demo']);
  run(adb,['shell','run-as ph.edu.umak.crm.app.demo mkdir -p shared_prefs']);
  const settings=`<?xml version="1.0" encoding="utf-8"?><map><string name="server">${origin}</string></map>`;
  run(adb,['shell',"run-as ph.edu.umak.crm.app.demo sh -c 'cat > shared_prefs/crm-settings.xml'"],
    {stdio:['pipe','inherit','inherit'],input:settings});
  run(adb,['shell','am','start','-n','ph.edu.umak.crm.app.demo/ph.edu.umak.crm.app.MainActivity']);
  await mkdir(outputDirectory,{recursive:true});
  const accounts=JSON.parse(await readFile(join(fixtures,'demo-accounts.json'),'utf8'));
  const guide=`# UMak synthetic manual demo\n\nReady on this Mac's connected Android emulator. Keep the demo process running.\n\nAPI: ${origin}\n\nVerification inbox: ${mailbox}\n\n## Create an account\n\nUse student.demo01@umak.edu.ph (Student) or faculty.demo01@umak.edu.ph (Faculty), select a college from the dropdown, and choose a password of at least 12 characters. Use a new numbered address for each signup. Read the six-digit code in the verification inbox on this Mac. No messages leave the demo mail sink. Non-demo UMak addresses are rejected.\n\n## Seeded accounts\n\nShared synthetic password: ${accounts.password}\n\n| Email | Role | Permissions |\n|---|---|---|\n${accounts.accounts.filter(account=>account.active!==false&&account.verification!=='pending'&&account.activation!=='invited').map(account=>`| ${account.email} | ${account.role} | ${(account.permissions??[]).join(', ')} |`).join('\n')}\n\nSample attachments: ${fixtures}\n\nThis is the separate UMak CRM Demo app. The database resets when this launcher is restarted. This does not configure the hosted API or other phones. TF-IDF is not yet integrated; this is a workflow demo.\n`;
  await writeFile(join(outputDirectory,'DEMO-ACCESS.md'),guide,{mode:0o600});
  await copyFile('android/app/build/outputs/apk/demo/app-demo.apk',join(outputDirectory,'umak-crm-demo.apk'));
  console.log(`MANUAL DEMO READY\nApp: UMak CRM Demo\nVerification inbox: ${mailbox}\nAccess guide: ${join(outputDirectory,'DEMO-ACCESS.md')}`);
  await Promise.race(['SIGINT','SIGTERM'].map(signal=>once(process,signal)));
}finally{
  if(port)try{run(adb,['reverse','--remove','tcp:'+port],{stdio:'ignore'});}catch{}
  if(fixture&&fixture.exitCode===null){fixture.kill('SIGTERM');await once(fixture,'exit');}
  await rm(directory,{recursive:true,force:true});
}
