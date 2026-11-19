import pg from 'pg';
import { createApp } from './app.js';
import {readFileSync} from 'node:fs';
import https from 'node:https';
import http from 'node:http';
import {databaseConfig} from './security/database-config.js';
import {configuredStore} from './storage/s3.js';
import {clamScanner} from './storage/scanner.js';
import {configuredVerification} from './auth/verification-mail.js';
const isProduction = process.env.NODE_ENV === 'production';
const hasTlsFiles = Boolean(process.env.TLS_CERT_FILE && process.env.TLS_KEY_FILE);

if (isProduction && !hasTlsFiles && process.env.TRUST_PROXY !== '1' && process.env.TRUST_PROXY !== 'true' && !process.env.RENDER) {
  throw Error('Production API TLS certificate and key are required');
}

const db=new pg.Pool(databaseConfig());
await db.query('SELECT 1');
const app=createApp(db,{objectStore:configuredStore(),scanner:clamScanner(),verification:configuredVerification()});

if (process.env.TRUST_PROXY === '1' || process.env.TRUST_PROXY === 'true' || process.env.RENDER || (!hasTlsFiles && isProduction)) {
  app.set('trust proxy', 1);
}

const server=(hasTlsFiles?https.createServer({cert:readFileSync(process.env.TLS_CERT_FILE),key:readFileSync(process.env.TLS_KEY_FILE),minVersion:'TLSv1.3'},app):http.createServer(app));
server.requestTimeout=60_000;server.headersTimeout=15_000;
const port = Number(process.env.PORT || 3000);
const host = process.env.HOST || (isProduction ? '0.0.0.0' : '127.0.0.1');
server.listen(port, host, () => console.log(`CRM API listening on ${host}:${port}`));
for(const signal of ['SIGINT','SIGTERM']) process.on(signal,()=>server.close(async()=>{await db.end();process.exit(0);}));
