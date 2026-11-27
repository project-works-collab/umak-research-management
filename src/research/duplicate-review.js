import {z} from '../security/validation.js';

export const duplicateReviewThreshold=0.85;
const maxArchive=100000;
const responseSchema=z.strictObject({
  model_version:z.string().min(1).max(128),scored_count:z.number().int().min(0).max(maxArchive),
  scores:z.array(z.strictObject({id:z.number().int().positive(),score:z.number().min(0).max(1)})).max(5)
});

export async function reviewDuplicates(db,input,{serviceUrl,serviceToken,fetcher=fetch}={}) {
  const filters=[input.year??null,input.department??null,input.exclude_id??null];
  // RLS is applied here: the scorer must never choose the caller's access scope.
  const scope=await db.query(`SELECT id FROM research_records
    WHERE ($1::integer IS NULL OR research_year=$1)
      AND ($2::text IS NULL OR department=$2) AND ($3::integer IS NULL OR id<>$3)
    ORDER BY id LIMIT 100001`,filters);
  if(scope.rows.length>maxArchive)throw Error('Archive exceeds exact search capacity');
  const allowed=new Set(scope.rows.map(item=>item.id));
  const response=await fetcher(new URL('/search',serviceUrl),{
    method:'POST',signal:AbortSignal.timeout(5000),
    headers:{'content-type':'application/json',authorization:`Bearer ${serviceToken}`},
    body:JSON.stringify({title:input.title,abstract:input.abstract,allowed_ids:[...allowed]})
  });
  if(!response.ok)throw Error('Scoring service unavailable');
  const result=responseSchema.parse(await response.json());
  if(result.scored_count!==allowed.size||new Set(result.scores.map(item=>item.id)).size!==result.scores.length||
      result.scores.length!==Math.min(5,allowed.size)||result.scores.some(item=>!allowed.has(item.id)))
    throw Error('Invalid scorer scope');
  // Recheck RLS and filters after the private-service round trip, including revocations.
  const records=await db.query(`SELECT id,title,abstract,status,research_year,department,
      normalized_title=crm_normalize_title($5) AS exact_title
    FROM research_records WHERE id=ANY($4::integer[])
      AND ($1::integer IS NULL OR research_year=$1) AND ($2::text IS NULL OR department=$2)
      AND ($3::integer IS NULL OR id<>$3)`,[...filters,result.scores.map(item=>item.id),input.title]);
  const scores=new Map(result.scores.map(item=>[item.id,item.score]));
  const matches=records.rows.map(item=>({...item,tfidf_score:scores.get(item.id),
    alert:scores.get(item.id)>duplicateReviewThreshold,abstract_unavailable:!item.abstract}))
    .sort((a,b)=>b.tfidf_score-a.tfidf_score||a.id-b.id);
  return {matches,model_version:result.model_version,threshold:duplicateReviewThreshold,
    threshold_version:'professor-policy-85-v1',threshold_status:'policy_requires_institutional_calibration',
    retrieval_scope:'all_authorized_records',scored_count:result.scored_count};
}
