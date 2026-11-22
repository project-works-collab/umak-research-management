import test from 'node:test';
import assert from 'node:assert/strict';
import express from 'express';
import request from 'supertest';
import {researchRegistrationRoutes} from '../src/research/registrations.js';
import {registeredProgressRoutes} from '../src/research/progress.js';

for(const [name,install,path] of [
  ['registry',researchRegistrationRoutes,'/api/v1/staff/research-registrations'],
  ['progress',registeredProgressRoutes,'/api/v1/staff/research-progress']
]){
  test(`${name} directory uses validated descending cursor and retains authorization`,async()=>{
    const calls=[];let allowed=true;
    const db={async query(sql,params){
      if(sql.includes('crm_staff_permissions'))return {rowCount:allowed?1:0,rows:[]};
      if(sql.includes('crm_manages_progress'))return {rows:[{allowed}]};
      calls.push({sql,params});return {rows:[{id:71,title:'Actual older study'}]};
    }};
    const app=express();app.use(express.json());install(app,db,(req,_res,next)=>{req.user={id:8,role:'staff'};next();},async(_req,callback)=>callback(db));
    const first=await request(app).get(path);assert.equal(first.status,200);assert.equal(first.headers['cache-control'],'no-store');assert.equal(calls.at(-1).params.at(-1),null);
    const older=await request(app).get(path).query({before:72});assert.equal(older.status,200);assert.equal(calls.at(-1).params.at(-1),72);assert.match(calls.at(-1).sql,/ORDER BY (r\.)?id DESC LIMIT 100/);assert.match(calls.at(-1).sql,/id<\$\d/);
    const queries=calls.length;for(const cursor of ['0','-1','1 OR 1=1','2147483648'])assert.equal((await request(app).get(path).query({before:cursor})).status,400);assert.equal(calls.length,queries);
    allowed=false;assert.equal((await request(app).get(path).query({before:72})).status,403);assert.equal(calls.length,queries);
  });
}
