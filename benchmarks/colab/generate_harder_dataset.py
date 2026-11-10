"""Generate deterministic fictional challenge data; no human-reviewed accuracy claims."""
import csv,json,pathlib,random,zipfile
ROOT=pathlib.Path(__file__).resolve().parent
OUT=ROOT/'harder';OUT.mkdir(exist_ok=True)
random.seed(734)
with (ROOT/'records.csv').open() as f:base=list(csv.DictReader(f))
records=[];queries=[];labels=[]
for i,r in enumerate(base):
    topic=i//6; population=i%6; district=population+1
    # Three same-topic hard negatives with deliberately overlapping wording.
    variants=[('survey','cross-sectional survey','240','logistic regression'),('trial','randomized controlled intervention','600','treatment-effect estimation'),('interview','qualitative interview study','35','thematic analysis'),('followup','three-year longitudinal cohort','420','survival analysis')]
    for suffix,method,size,analysis in variants:
        rr=dict(r);rr['id']=r['id']+'_'+suffix
        rr['title']=r['title'] if suffix in ('survey','trial') else r['title']+': '+('lived experiences' if suffix=='interview' else 'a longitudinal investigation')
        rr['abstract']=f"Study identifier SYN-{i:03}-{suffix}. A {method} of {size} participants conducted in District {district} in {r['year']}. The population and topic are described in the title. Analysis uses {analysis}. This is an independent study with its own participants and observations."
        # Identifiers document provenance only; exclude them from the model input.
        rr['abstract']=rr['abstract'].split('. ',1)[1]
        records.append(rr)
    title=r['title'];subject=title.removeprefix('Factors associated with ')
    specs=[
      ('ambiguous_exact',title,'A questionnaire study with 240 participants; logistic regression assesses associations. This submission describes the original cross-sectional survey.',r['id']+'_survey'),
      ('typo',title.replace('Factors','Factros').replace('associated','associatd'),'A cross-sectional survey of 240 participants using logistic regression.',r['id']+'_survey'),
      ('reworded',f'Predictors and correlates of {subject}',f'Questionnaire responses from 240 participants in District {district}, collected in {r["year"]}, are examined using logistic regression. This is a revised description of the original survey.',r['id']+'_survey'),
      ('method_paraphrase',f'Evaluating a targeted programme addressing {subject}',f'Random allocation of 600 participants tests an intervention in District {district}. This is another description of the existing randomized controlled trial, using treatment-effect estimation.',r['id']+'_trial'),
      ('same_title_new_study',title,f'A new independent mixed-methods study recruits 175 different participants in {int(r["year"])+2}. It shares no observations with any prior study and uses structural equation modeling.',None),
      ('negation',f'No association found: {subject}',f'A separate preregistered replication involving 310 new participants finds no association. It is not a resubmission of any existing study.',None),
      ('unseen_topic',f'Quantum error correction with topological codes: experiment {i}',f'A new laboratory experiment on quantum computing hardware, with device configuration {i}. No human participants.',None),
    ]
    split='dev' if population<3 else 'test'
    for category,qt,qa,positive in specs:
        qid=f'H{i:03}_{category}'
        queries.append(dict(query_id=qid,title=qt,abstract=qa,split=split,category=category,source_id=r['id'],duplicate_record_id=positive or ''))
# Labels created after corpus completion; topic identity defines related controls.
for q in queries:
    for rec in records:
        label='duplicate' if rec['id']==q['duplicate_record_id'] else ('related_distinct' if rec['id'][:3]==q['source_id'][:3] and q['category']!='unseen_topic' else 'unrelated')
        labels.append(dict(query_id=q['query_id'],record_id=rec['id'],label=label))
for name,data in [('records',records),('queries',queries),('labels',labels)]:
    with (OUT/f'{name}.csv').open('w',newline='') as f:
        w=csv.DictWriter(f,fieldnames=list(data[0]));w.writeheader();w.writerows(data)
nb=json.loads((ROOT/'duplicate_benchmark_colab.ipynb').read_text())
for c in nb['cells']:
    s=''.join(c['source'])
    if c['cell_type']=='code':
        s=s.replace("TEXT_MODE = 'title'", "TEXT_MODE = 'title_abstract'")
        s=s.replace('REPS = 5','REPS = 3')
        s=s.replace("positive_ids=queries[(queries.split=='test')&(queries.category!='distinct')].query_id.tolist()", "positive_ids=sorted(set(labels[labels.label=='duplicate'].query_id) & set(queries[queries.split=='test'].query_id))")
    if c['cell_type']=='markdown':
        s=s.replace('120 source studies, 480 queries, and 57,600 explicitly labeled pairs','480 corpus records, 840 queries, and 403,200 explicitly labeled pairs')
        s=s.replace('Primary evaluation uses titles only;', 'This challenge defaults to title + abstract;')
        s=s.replace('Run in Google Colab', '**Harder challenge dataset.** Identical titles can belong to independent studies. Exact title matches still bypass thresholds in this original pipeline, deliberately exposing that policy’s false alerts. They are candidates for review, not proven duplicates. Some title-only cases are inherently ambiguous. Warm timing uses three repetitions for a pilot; increase REPS for a final run.\n\nRun in Google Colab',1)
    c['source']=s.splitlines(True);c.pop('id',None)
(OUT/'duplicate_benchmark_harder_colab.ipynb').write_text(json.dumps(nb,indent=2))
(OUT/'README.md').write_text('''# Harder synthetic duplicate-review challenge

Upload duplicate_benchmark_harder_colab.ipynb to Colab, run all cells, and choose this folder's records.csv, queries.csv and labels.csv together. Use a fresh runtime or rerun all cells to replace previous data.

480 corpus records, 840 queries, 403,200 pair labels; 480 positive pairs. Four independent studies per source topic/population: survey, trial, interviews and cohort. Survey and trial deliberately share exactly the same title. Queries include ambiguous exact titles, typos, reworded surveys, reworded trials, same-title independent studies, independent negative-result replications, and unrelated queries.

Default: title + abstract. For an ablation set TEXT_MODE='title' and rerun from vector preparation onward. Do not compare text modes as though only the algorithm changed. Exact-title priority/bypass is retained to expose false positives. A title match does not prove study identity.

Development/test source groups are disjoint; topics and generation templates overlap. Corpus contains independent same-title records across both splits. Labels are fictional study-identity labels, not inferred from cosine scores. All source identity is excluded from model text. This is a deliberately constructed challenge, not a realistic estimate of prevalence or evidence of real-world model quality. Some paraphrases retain topic vocabulary; natural paraphrases and expert annotations remain future work.

Original notebook package pins are retained; Colab may warn about unrelated preinstalled dependencies. No dependency compatibility claim is made. Notebook is syntax-checked, not executed in Colab. Run time is longer than the initial pilot. Three timing repetitions are only a preview, not a final latency study. The generator lives in the parent directory.
''')
assert len(records)==480 and len(queries)==840 and len(labels)==403200
assert sum(x['label']=='duplicate' for x in labels)==480
with zipfile.ZipFile(OUT/'harder_colab_bundle.zip','w',zipfile.ZIP_DEFLATED) as z:
    for name in ['duplicate_benchmark_harder_colab.ipynb','records.csv','queries.csv','labels.csv','README.md']:z.write(OUT/name,name)
print(f'Created {len(records)} records, {len(queries)} queries, {len(labels)} labels')
