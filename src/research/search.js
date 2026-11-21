// Both indexed and exhaustive predicates use PostgreSQL's exact pg_trgm scorer.
// The exhaustive variant is for benchmark/oracle use, never an HTTP option.
export function searchSql({duplicates=false,exhaustive=false,publicProjection=true}={}) {
  const fuzzy=exhaustive?'similarity(normalized_title,crm_normalize_title($1)) >= 0.3':'normalized_title % crm_normalize_title($1)';
  const match=duplicates?fuzzy:`(${fuzzy} OR normalized_title LIKE '%'||crm_normalize_title($1)||'%')`;
  return `SELECT id,title,abstract,status,research_year,department,
    similarity(normalized_title,crm_normalize_title($1)) AS similarity
    FROM ${duplicates||!publicProjection?'research_records':'crm_public_research()'} WHERE ${duplicates?'TRUE':"status='Approved'"}
    AND ($2::integer IS NULL OR research_year=$2)
    AND ($3::text IS NULL OR department=$3)
    AND (${duplicates?match:`crm_normalize_title($1)='' OR ${match}`})
    ORDER BY similarity DESC,id ASC LIMIT $4 OFFSET $5`;
}
export function parseSearch(body,{duplicates=false}={}) {
  if(!body||typeof body!=='object'||Array.isArray(body)||Object.keys(body).some(k=>!['keyword','year','department','limit','offset'].includes(k))) return null;
  const {keyword,year=null,department=null,limit=100,offset=0}=body;
  if(typeof keyword!=='string'||keyword.length>200||
    (duplicates&&!keyword.trim())||
    (year!==null&&(!Number.isInteger(year)||year<1900||year>9999))||
    (department!==null&&(typeof department!=='string'||!department.trim()||department.length>100))||
    !Number.isInteger(limit)||limit<1||limit>100||!Number.isInteger(offset)||offset<0||offset>10000) return null;
  return [keyword,year,department,limit,offset];
}
export async function searchResearch(db,args,options) {
  const client=await db.connect();
  try {
    await client.query('BEGIN');
    // Transaction-local: never leak thresholds through the connection pool.
    await client.query("SELECT set_config('pg_trgm.similarity_threshold','0.3',true)");
    const result=await client.query(searchSql(options),args);
    await client.query('COMMIT');return result.rows;
  } catch(e){await client.query('ROLLBACK');throw e;}
  finally{client.release();}
}
export async function duplicateCandidates(db,{title,year=null,department=null,exclude_id=null}) {
  const client=await db.connect();
  try {
    await client.query('BEGIN');
    await client.query("SELECT set_config('pg_trgm.similarity_threshold','0.3',true)");
    const result=await client.query(`SELECT id,title,abstract,status,research_year,department,
      normalized_title=crm_normalize_title($1) AS exact_title,
      similarity(normalized_title,crm_normalize_title($1)) AS trigram_score
      FROM research_records WHERE normalized_title<>'' AND
      (normalized_title % crm_normalize_title($1) OR normalized_title LIKE '%'||crm_normalize_title($1)||'%')
      AND ($2::integer IS NULL OR research_year=$2)
      AND ($3::text IS NULL OR department=$3)
      AND ($4::integer IS NULL OR id<>$4)
      ORDER BY exact_title DESC,trigram_score DESC,id ASC LIMIT 50`,[title,year,department,exclude_id]);
    await client.query('COMMIT');return result.rows;
  } catch(error){await client.query('ROLLBACK');throw error;}finally{client.release();}
}
