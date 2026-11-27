import test from 'node:test';
import assert from 'node:assert/strict';
import express from 'express';
import request from 'supertest';
import {completeRequestValidation,validate,z,id} from '../src/security/validation.js';

test('every route rejects unused inputs while retaining named contracts and HTTP methods',async()=>{
  const app=express();app.use(express.json());
  const router=express.Router();
  router.get('/:id',validate(z.strictObject({before:id.optional()}),'query'),(req,res)=>res.json(req.validated));
  app.use('/api/v1/items',router);
  app.post('/api/v1/empty',(req,res)=>res.json({ok:true}));
  completeRequestValidation(app);
  assert.deepEqual(Object.keys(router.stack[0].route.methods),['get']);
  await request(app).get('/api/v1/items/4?before=3').expect(200);
  await request(app).get('/api/v1/items/not-an-id').expect(400);
  await request(app).get('/api/v1/items/4?unrecognized=true').expect(400);
  await request(app).post('/api/v1/empty').send({}).expect(200);
  await request(app).post('/api/v1/empty?unrecognized=true').send({}).expect(400);
  await request(app).post('/api/v1/empty').send({unexpected:'field'}).expect(400);
  await request(app).get('/api/v1/empty').expect(404);
});

test('named JSON bodies and binary uploads retain their parsed data',async()=>{
  const app=express();app.use(express.json());
  app.post('/json',validate(z.strictObject({title:z.string()})),(req,res)=>res.json(req.validated.body));
  app.post('/binary',express.raw({type:'application/octet-stream',limit:16}),
    (req,res)=>res.json({bytes:req.validated.body.length}));
  completeRequestValidation(app);
  const json=await request(app).post('/json').send({title:'Study'}).expect(200);
  assert.equal(json.body.title,'Study');
  const binary=await request(app).post('/binary').set('Content-Type','application/octet-stream').send(Buffer.from('pdf')).expect(200);
  assert.equal(binary.body.bytes,3);
});
