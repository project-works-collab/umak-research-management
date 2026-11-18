import nodemailer from 'nodemailer';

export function configuredVerification(env=process.env){
  const {CRM_SMTP_URL:url,CRM_VERIFICATION_FROM:from,CRM_VERIFICATION_KEY:key,
    CRM_INSTITUTIONAL_EMAIL_DOMAINS:domainList}=env;
  if(!url&&!from&&!key&&!domainList)return null;
  if(!url||!from||!key||!domainList||!(/^[a-f0-9]{64}$/i.test(key))){
    throw Error('Configure CRM_SMTP_URL, CRM_VERIFICATION_FROM, CRM_INSTITUTIONAL_EMAIL_DOMAINS and a 32-byte hex CRM_VERIFICATION_KEY together');
  }
  const domains=domainList.split(',').map(domain=>domain.trim().toLowerCase()).filter(Boolean);
  if(!domains.length)throw Error('At least one institutional email domain is required');
  const endpoint=new URL(url);
  if(!['smtp:','smtps:'].includes(endpoint.protocol)||endpoint.search||endpoint.hash){
    throw Error('CRM_SMTP_URL must be an smtp or smtps connection URL without query options');
  }
  const local=env.NODE_ENV!=='production'&&['127.0.0.1','localhost','[::1]'].includes(endpoint.hostname);
  const transport=nodemailer.createTransport({
    host:endpoint.hostname.replace(/^\[|\]$/g,''),
    port:endpoint.port?Number(endpoint.port):(endpoint.protocol==='smtps:'?465:587),
    secure:endpoint.protocol==='smtps:',
    requireTLS:!local,
    auth:endpoint.username?{user:decodeURIComponent(endpoint.username),pass:decodeURIComponent(endpoint.password)}:undefined,
    connectionTimeout:5000,greetingTimeout:5000,socketTimeout:10000,
    disableFileAccess:true,disableUrlAccess:true
  });
  return {
    key:Buffer.from(key,'hex'),
    domains,
    async send(to,code){
      const result=await transport.sendMail({from,to,subject:'Verify your UMak CRM researcher account',
        text:`Your verification code is ${code}. It expires in 15 minutes. If you did not request this code, ignore this email.`});
      if(result.accepted.length!==1)throw Error('Verification email was not accepted');
    }
  };
}
