# Hosted recording demo

The existing Render Node service runs the API and a private Python TF-IDF scorer together. The scorer binds only to loopback; no second public service or extra hosting plan is required by this configuration.

1. Apply migrations through 024 as the database owner. `npm run demo:hosted:sql` produces `outputs/hosted-demo/prepare-recording.sql`, containing conditional migrations 022–024 and the additive recording seed. Review and run it in the Supabase SQL Editor. It does not alter passwords or reset accounts, dates, permissions, or existing document versions.
2. Give the existing `crm_tfidf` role a strong login password privately. This role reads only research IDs, titles, and abstracts. Set Render's `TFIDF_DATABASE_URL` to its session-pooler connection, using `crm_tfidf.<project-reference>` as the pooler username. Keep `DATABASE_URL` on the existing `crm_app` role. Do not commit either URL.
3. Configure the existing service to deploy the branch containing these scripts. Build command: `npm run build:hosted-demo`. Start command: `npm run start:hosted-demo`. Keep the existing demo flags, demo password, JWT signing key, and `DATABASE_URL`. Node must be version 24. The build installs its own Python 3.12 interpreter and pinned scorer dependencies inside the project.
4. The launcher uses `config/supabase-prod-ca-2021.crt`, downloaded from the certificate link in Supabase's Database Settings. Both database clients verify certificates and require TLS 1.3. A different database provider must supply `DB_TLS_CA_FILE` or `DB_TLS_CA_PEM` with its trusted CA. `ALLOW_UNVERIFIED_DB_TLS` is not used.
5. Start a manual deployment after credentials and migrations are ready. The API starts only after the scorer passes archive readiness. If either child exits, the supervisor shuts down the other so Render can restart the service. The private service token is generated at startup and never exposed in the APK.
6. Run `node --env-file=<private-demo-env-file> scripts/verify-recording-demo.js`. It checks the five accounts, access denial, typo search, exact/unrelated TF-IDF matches, seeded PDF download, lifecycle dates, account history, and public form. It does not create accounts or change publication/permissions. Output: `outputs/hosted-demo/recording-verification.json`.
7. Build the hosted Android APK with JDK 21: `android/gradlew -p android :app:assembleRenderDemo :app:lintRenderDemo`. Distribute `android/app/build/outputs/apk/renderDemo/app-renderDemo.apk`. It uses the hosted API URL and contains no database credentials.
8. Rehearse the APK on a phone. Use existing demo accounts. For offline recording, cache the public form, turn off Wi-Fi and mobile data, show the failed refresh and cached content, then reconnect before the next segment.

## Seeded recording content

- Three registered rainwater studies: an exact title/abstract match, the same title with an unrelated abstract, and an alternate title with the same abstract. Only the synthetic demo student and registry reviewer are assigned.
- Version 1 of the existing public demo PDF on Demo Research A, only if it has no documents yet.
- Completion and presentation dates are preserved. Publication is left for the recording action. No fake account audit events are created; changing the limited account's permission during recording produces a real event.

This demo uses synthetic malware scanning unless real ClamAV is configured separately. Registry documents use the database version store. This deployment does not provision S3, offsite backups, or production restore scheduling.
