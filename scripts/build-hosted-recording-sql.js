import {readFileSync,readdirSync,mkdirSync,writeFileSync} from 'node:fs';
// No passwords, credential changes, or destructive resets are emitted.
let sql='BEGIN; SELECT pg_advisory_xact_lock(73641029);\n';
for(const name of readdirSync('db/migrations').filter(n=>n.endsWith('.sql')&&n>='022').sort()){
 const body=readFileSync('db/migrations/'+name,'utf8');
 if(body.includes('$recording_migration$'))throw Error('Migration delimiter conflict');
 sql+=`DO $recording_migration$ BEGIN IF NOT EXISTS(SELECT 1 FROM schema_migrations WHERE name='${name}') THEN\n EXECUTE $recording_ddl$${body}$recording_ddl$;\n INSERT INTO schema_migrations(name) VALUES('${name}'); END IF; END $recording_migration$;\n`;
}
sql+='COMMIT;\n'+readFileSync('demo/hosted/recording-seed.sql','utf8');
mkdirSync('outputs/hosted-demo',{recursive:true});
writeFileSync('outputs/hosted-demo/prepare-recording.sql',sql);
console.log('Created outputs/hosted-demo/prepare-recording.sql (migrations and additive seed; no credentials).');
