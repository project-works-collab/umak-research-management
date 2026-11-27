# TF-IDF duplicate review service

This private FastAPI service fits scikit-learn TF-IDF on research record titles and abstracts. Its `/search` endpoint scores all record IDs authorized by the Node API; `/score` retains candidate-scoring compatibility. It does not use the Colab notebook at runtime.

Deploy on Render as a Python Private Service with root directory `services/tfidf`, build command `pip install -r requirements.txt`, and start command `uvicorn app:app --host 0.0.0.0 --port 10000 --workers 1`. Set `PYTHON_VERSION=3.12.9`, `TFIDF_DATABASE_URL` (credentials for the `crm_tfidf` role provisioned by migration 022), and a random `TFIDF_SERVICE_TOKEN`. Use the same workspace and region as the Node service.

Set `TFIDF_SERVICE_URL` to the Render internal service URL, set the same `TFIDF_SERVICE_TOKEN` on Node, and set `TFIDF_REVIEW_ENABLED=true` only after `/ready` succeeds. The Node endpoint is `POST /api/v1/research/duplicate-review`, staff-only, with `{title,abstract,year?,department?,exclude_id?}`. It returns five advisory matches at most. The review endpoint alerts for scores strictly greater than 0.85 and labels this as a policy requiring institutional calibration. It searches all caller-authorized records (up to 100,000), rather than a title shortlist; the Node API rechecks access before returning metadata. Calibrate on independently labeled UMak records before interpreting alerts as validated.

The Python service checks the archive on every global search; candidate-scoring and readiness requests check when their last check is at least 60 seconds old, and rebuilds its vectors when any title or abstract changes. Candidate text is compared with the current snapshot; a mismatch forces a refresh. A scoring failure returns HTTP 503 to staff rather than an empty match list.

Run `pytest services/tfidf/test_app.py` with this service's requirements installed.

## Local presentation demo

From the repository root:

```sh
npm run demo:tfidf:setup
npm run demo:tfidf:check
JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home npm run demo:manual
```

Setup installs a private Python 3.12 environment under `.private/tfidf-venv`.
The manual launcher requires a running Android emulator and JDK 21. It starts an
isolated PostgreSQL database, provisions the restricted scorer role, starts the
real Python service on localhost, waits for archive readiness, and verifies the
staff API before installing the demo app. Keep the launcher running during the
presentation. No deployment credentials or project `.env` database are used.
`TFIDF_PYTHON` can override the scorer interpreter.

Open `outputs/manual-demo/DEMO-ACCESS.md` for the generated credentials and scenario.
Sign in as `demo.registry@umak.edu.ph`, open **Registry → Registered research →
Add Research Entry**, and enter the title and abstract in
`outputs/manual-demo/tfidf-scenario.json`. Tap **Review duplicates**. The identical
study should score `1.000`; the same-title study with an unrelated abstract stays
below the configured policy threshold. Tap **Back to entry** to try different text without
saving. The global search covers the caller-authorized archive scope, including different titles. TF-IDF remains lexical and is not a semantic embedding model.

For API-only presentation, run `npm run demo:tfidf` and use the printed fixture
directory for credentials and scenario JSON. Each restart creates fresh data.

The real Android HTTPS journey can be checked with:

```sh
JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home \
CRM_TFIDF_DEMO=true \
CRM_ANDROID_TEST_CLASS='ph.edu.umak.crm.app.CatalogHttpsTest#registryStaffReviewsRealTfidfMatches' \
npm run acceptance:android
```

This local setup does not configure the hosted Render deployment.

After migration 022, the database administrator must set a strong password and enable login for `crm_tfidf` with `ALTER ROLE crm_tfidf LOGIN PASSWORD ...`. Store the resulting URL only in Render environment variables. The role has column-limited SELECT rights and its own archive read policy.

The archive-wide search changes are awaiting isolated runtime verification and deployment. See [checklist implementation updates](../../docs/implementation/CHECKLIST_GAP_CLOSURE.md).
