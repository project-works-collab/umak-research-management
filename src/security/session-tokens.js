import {createHmac,randomBytes,timingSafeEqual} from 'node:crypto';

const developmentKey=randomBytes(32);

// Roles always come from the database, never from client-controlled claims.
export function createSessionTokens({key=process.env.JWT_SIGNING_KEY||undefined,production=process.env.NODE_ENV==='production',now=()=>Math.floor(Date.now()/1000)}={}) {
  if(key!==undefined&&!/^[a-f0-9]{64,128}$/.test(key))throw Error('JWT_SIGNING_KEY must contain 32–64 random bytes encoded as lowercase hex');
  if(!key&&production)throw Error('JWT_SIGNING_KEY is required in production');
  const secret=key?Buffer.from(key,'hex'):developmentKey;
  const header=Buffer.from(JSON.stringify({alg:'HS256',typ:'JWT'})).toString('base64url');
  const sign=value=>createHmac('sha256',secret).update(value).digest();
  return {
    issue(userId){
      if(!Number.isSafeInteger(userId)||userId<1)throw Error('Invalid session subject');
      const iat=now();
      const payload=Buffer.from(JSON.stringify({iss:'umak-crm',aud:'umak-crm-api',sub:String(userId),iat,exp:iat+900,jti:randomBytes(32).toString('hex')})).toString('base64url');
      const content=`${header}.${payload}`;return `${content}.${sign(content).toString('base64url')}`;
    },
    verify(token){
      try {
        if(typeof token!=='string'||token.length>2048)return null;
        const parts=token.split('.');
        if(parts.length!==3||parts[0]!==header||parts.some(p=>!/^[A-Za-z0-9_-]+$/.test(p)))return null;
        const signature=Buffer.from(parts[2],'base64url');
        if(signature.toString('base64url')!==parts[2]||signature.length!==32||!timingSafeEqual(signature,sign(`${parts[0]}.${parts[1]}`)))return null;
        const claims=JSON.parse(Buffer.from(parts[1],'base64url').toString('utf8'));
        const instant=now();
        if(claims.iss!=='umak-crm'||claims.aud!=='umak-crm-api'||!Number.isSafeInteger(claims.iat)||!Number.isSafeInteger(claims.exp)||claims.exp-claims.iat!==900||claims.iat>instant||claims.exp<=instant||typeof claims.sub!=='string'||!/^\d+$/.test(claims.sub)||!Number.isSafeInteger(Number(claims.sub))||Number(claims.sub)<1||!/^[a-f0-9]{64}$/.test(claims.jti))return null;
        return claims;
      }catch{return null;}
    }
  };
}
