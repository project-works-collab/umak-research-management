import {execFileSync} from 'node:child_process';
import {mkdir} from 'node:fs/promises';
import {resolve} from 'node:path';

const run=(file,args)=>execFileSync(file,args,{stdio:'inherit'});
await mkdir('.private',{recursive:true});
run(process.env.PYTHON_BIN??'python3',['-m','venv','.private/tfidf-bootstrap']);
const bootstrap=resolve('.private/tfidf-bootstrap/bin/python');
run(bootstrap,['-m','pip','install','uv==0.11.8']);
const uv=resolve('.private/tfidf-bootstrap/bin/uv');
run(uv,['python','install','3.12']);
run(uv,['venv','--python','3.12','--allow-existing','.private/tfidf-venv']);
run(uv,['pip','install','--python',resolve('.private/tfidf-venv/bin/python'),
  '-r','services/tfidf/requirements.txt','pytest==8.3.5','httpx==0.28.1']);
console.log('TF-IDF demo environment ready. Run npm run demo:tfidf:check or npm run demo:manual.');
