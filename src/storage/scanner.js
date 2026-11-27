import net from 'node:net';
import tls from 'node:tls';
import {readFileSync} from 'node:fs';
// ClamAV INSTREAM: no shell, no user-controlled paths, bounded frames/time/response.
export function clamScanner({host=process.env.CLAMAV_HOST,port=Number(process.env.CLAMAV_PORT??3310),
  caFile=process.env.CLAMAV_TLS_CA_FILE,serverName=process.env.CLAMAV_TLS_SERVERNAME??host,
  certFile=process.env.CLAMAV_TLS_CERT_FILE,keyFile=process.env.CLAMAV_TLS_KEY_FILE,
  production=process.env.NODE_ENV==='production'}={}){
  if(!host)return null;
  if(production&&!caFile)throw Error('Production scanner requires verified TLS');
  if(Boolean(certFile)!==Boolean(keyFile))throw Error('Scanner client certificate and key must be configured together');
  const secure=caFile?{ca:readFileSync(caFile),servername:serverName,rejectUnauthorized:true,minVersion:'TLSv1.3',
    ...(certFile?{cert:readFileSync(certFile),key:readFileSync(keyFile)}:{})}:null;
  return bytes=>new Promise((resolve,reject)=>{
    const socket=secure?tls.connect({host,port,...secure}):net.createConnection({host,port});let response='';
    const fail=()=>{socket.destroy();reject(Object.assign(Error('Scanner unavailable'),{httpStatus:503}));};
    socket.setTimeout(30_000,fail);socket.on('error',fail);
    socket.on('data',chunk=>{response+=chunk.toString('utf8');if(response.length>4096)fail();});
    socket.on('end',()=>{
      socket.destroy();
      if(/^stream: OK\0?$/.test(response.trim()))resolve(true);
      else if(response.includes('FOUND'))resolve(false);else fail();
    });
    socket.on(secure?'secureConnect':'connect',async()=>{
      const write=chunk=>new Promise((ok,no)=>socket.write(chunk,e=>e?no(e):ok()));
      try{
        await write(Buffer.from('zINSTREAM\0'));
        for(let i=0;i<bytes.length;i+=65536){const chunk=bytes.subarray(i,i+65536),size=Buffer.alloc(4);size.writeUInt32BE(chunk.length);await write(size);await write(chunk);}
        await write(Buffer.alloc(4));
      }catch{fail();}
    });
  });
}
