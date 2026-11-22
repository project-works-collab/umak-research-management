# TF-IDF duplicate review service

This private FastAPI service fits scikit-learn TF-IDF on research record titles and abstracts and scores the PostgreSQL trigram shortlist. It does not use the Colab notebook at runtime.

Deploy on Render as a Python Private Service with root directory `services/tfidf`, build command `pip install -r requirements.txt`, and start command `uvicorn app:app --host 0.0.0.0 --port 10000 --workers 1`. Set `PYTHON_VERSION=3.12.9`, `TFIDF_DATABASE_URL` (credentials for the `crm_tfidf` role provisioned by migration 022), and a random `TFIDF_SERVICE_TOKEN`. Use the same workspace and region as the Node service.

Set `TFIDF_SERVICE_URL` to the Render internal service URL, set the same `TFIDF_SERVICE_TOKEN` on Node, and set `TFIDF_REVIEW_ENABLED=true` only after `/ready` succeeds. The Node endpoint is `POST /api/v1/research/duplicate-review`, staff-only, with `{title,abstract,year?,department?,exclude_id?}`. It returns five advisory matches at most. Its current alert threshold is the full-precision value from the synthetic benchmark, and its response labels that threshold as pilot-only. Calibrate on independently labeled UMak records before interpreting alerts as validated.

The Python service checks the archive on a scoring or readiness request when its last check is at least 60 seconds old, and rebuilds its vectors when any title or abstract changes. Candidate text is compared with the current snapshot; a mismatch forces a refresh. A scoring failure returns HTTP 503 to staff rather than an empty match list.

Run `pytest services/tfidf/test_app.py` with this service's requirements installed.

After migration 022, the database administrator must set a strong password and enable login for `crm_tfidf` with `ALTER ROLE crm_tfidf LOGIN PASSWORD ...`. Store the resulting URL only in Render environment variables. The role has column-limited SELECT rights and its own archive read policy.
