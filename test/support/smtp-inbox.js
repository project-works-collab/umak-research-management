import net from 'node:net';

export function testInbox({domain="example.test",acceptAddress}={}){
  const messages=[];
  let rejectMail=false;
  const server=net.createServer(socket=>{
    socket.setEncoding('utf8');
    socket.write('220 localhost test inbox\r\n');
    let input='', message=null;
    let sender='', recipient='';
    socket.on('data',chunk=>{
      input+=chunk;
      if(input.length+(message?.length??0)>64*1024){
        socket.end('552 message too large\r\n');
        return;
      }
      let end;
      while((end=input.indexOf('\r\n'))>=0){
        const line=input.slice(0,end);
        input=input.slice(end+2);
        if(message!==null){
          if(line!=='.')message+='\n'+line;
          else{
            messages.push({sender,recipient,text:message});
            message=null;
            socket.write('250 accepted\r\n');
          }
        }else if(line.startsWith('EHLO ')||line.startsWith('HELO ')){
          socket.write('250 localhost\r\n');
        }else if(line.startsWith('MAIL FROM:')){
          sender=line.match(/<([^>]+)>/)?.[1]??'';
          socket.write(rejectMail?'451 temporary failure\r\n':'250 sender OK\r\n');
        }else if(line.startsWith('RCPT TO:')){
          recipient=line.match(/<([^>]+)>/)?.[1]??'';
          socket.write(recipient.toLowerCase().split('@').at(-1)===domain&&(!acceptAddress||acceptAddress(recipient.toLowerCase()))
            ?'250 recipient OK\r\n':'550 synthetic test recipients only\r\n');
        }else if(line==='DATA'){
          message='';
          socket.write('354 send mail\r\n');
        }else if(line==='QUIT'){
          socket.end('221 bye\r\n');
        }
      }
    });
  });
  return {server,messages,rejectNext(){rejectMail=true;}};
}
