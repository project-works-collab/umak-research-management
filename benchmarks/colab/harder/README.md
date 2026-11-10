# Harder synthetic duplicate-review challenge

Upload duplicate_benchmark_harder_colab.ipynb to Colab, run all cells, and choose this folder's records.csv, queries.csv and labels.csv together. Use a fresh runtime or rerun all cells to replace previous data.

480 corpus records, 840 queries, 403,200 pair labels; 480 positive pairs. Four independent studies per source topic/population: survey, trial, interviews and cohort. Survey and trial deliberately share exactly the same title. Queries include ambiguous exact titles, typos, reworded surveys, reworded trials, same-title independent studies, independent negative-result replications, and unrelated queries.

Default: title + abstract. For an ablation set TEXT_MODE='title' and rerun from vector preparation onward. Do not compare text modes as though only the algorithm changed. Exact-title priority/bypass is retained to expose false positives. A title match does not prove study identity.

Development/test source groups are disjoint; topics and generation templates overlap. Corpus contains independent same-title records across both splits. Labels are fictional study-identity labels, not inferred from cosine scores. All source identity is excluded from model text. This is a deliberately constructed challenge, not a realistic estimate of prevalence or evidence of real-world model quality. Some paraphrases retain topic vocabulary; natural paraphrases and expert annotations remain future work.

Setup uses Sentence Transformers 6.2.0, Transformers 5.22.2 and Hugging Face Hub 1.32.0. Their declared Hub requirements are compatible with the reported Gradio/Diffusers constraints. Full Colab execution is unverified. Use a fresh runtime; rerun all methods after a library change. Notebook is syntax-checked, not executed in Colab. Run time is longer than the initial pilot. Three timing repetitions are only a preview, not a final latency study. The generator lives in the parent directory.
