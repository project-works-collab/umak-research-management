# Checklist implementation updates

27 November 2026 Asia Manila

The changes below address the professor's ALGOCOM and IAS recommendations. They are local source changes awaiting isolated regression tests and deployment. They do not establish that every checklist item is operating in the hosted demo.

## Lifecycle and parallel outcomes

`algocom/lifecycle-policy.json` describes the lifecycle, presentation and publication regions. The Python model exposes their active leaves independently. Migration `024_unified_lifecycle_policy.sql` applies one transition predicate to both persisted lifecycle entry points. Both In-Progress and UnderReview can complete a study, retaining the existing registered-workflow path while adding the prototype's review-completion path. The Android editor offers both paths. Publication and presentation retain their independent dates; correcting an existing registered outcome requires an audit reason. Future occurrence/completion dates are rejected by the registered mutation.

Apply migration 024 before deploying the changed app. Existing history is retained. No migration has been applied to the hosted database during this task.

## Archive-wide duplicate review

The staff endpoint now derives the entire caller-authorized record scope from PostgreSQL RLS, with optional year, department and excluded-record filters. The private TF-IDF service's `/search` endpoint computes cosine scores across that scope, without a trigram-title shortlist. It returns up to five IDs and scores. The Node API validates service output and rechecks access and filters before returning current record metadata, so revocation during scoring cannot reveal a record.

An exact search has an explicit capacity of 100,000 authorized records. Larger scopes return service unavailable rather than silently truncating the comparison. The scorer refreshes the archive snapshot before each global search. The existing `/score` endpoint remains available for candidate-scoring compatibility.

Alerts use a strict score greater than 0.85, matching the professor's example policy. This replaces the synthetic benchmark threshold in the live review endpoint. The response labels it as a policy requiring institutional calibration. TF-IDF uses lexical word/bigram features; it is not Sentence-BERT, and this change does not claim validated detection of arbitrary synonymous paraphrases. Historical benchmark thresholds and results remain historical evidence.

## Request contracts and encrypted transport

Previously manual route-ID checks now use Zod. A final route-contract pass validates otherwise unused body/query sources and route parameters, while preserving named schemas and binary parsers. Unknown request fields are rejected. The demo inbox also has an explicit form schema.

Production database configuration no longer accepts `ALLOW_UNVERIFIED_DB_TLS=yes`. Deployments must provide a trusted `DB_TLS_CA_FILE`; the saved hosted demo export currently lacks one. Do not deploy the changed entry point without supplying that certificate configuration.

The ClamAV client supports verified TLS 1.3 and client certificates. Production configuration requires the scanner CA. `ops/clamav-stunnel.conf` and `ops/clamd.conf` keep raw clamd on loopback and expose a mutually authenticated TLS wrapper. Configure its certificates and refresh its signature database on the scanner host. The hosted demo entry point selects real ClamAV when configured, retaining the explicitly synthetic fallback only when it is absent. Public demo information distinguishes configuration from verified readiness.

## Verification state

Changed JavaScript sources passed `node --check`; changed Python sources passed AST parsing; the policy JSON parsed; `git diff --check` passed. These checks do not execute application code or prove runtime behavior.

New/updated tests cover lifecycle completion paths and independent outcomes, global retrieval with different titles, authorization filtering and revocation, malformed scorer output, exact threshold behavior, default request contracts, and refusal of the production TLS bypass. They have not been executed for this revision. The security skill requires OS-enforced network/filesystem and resource isolation for target-controlled execution. This Mac has no Docker engine, and the existing approved Linux sandbox covers only worker-result tests. Run the changed suites on a suitable isolated runner before treating them as passing evidence.

On 27 November, the existing public demo health endpoint returned HTTP 200. A verified connection to its public edge negotiated TLSv1.3 with TLS_AES_256_GCM_SHA384. That does not verify Render-to-origin, database, storage or scanner transport. Its public demo-info response reported TF-IDF not integrated and a synthetic antivirus scanner. Cloud deployment settings were not changed.

## Remaining deployment evidence

Choose and provision private manuscript storage and a separately controlled recovery destination. Supabase Storage, S3 and other compatible providers require provider-specific configuration and verification; the existing recovery scripts are AWS-oriented. Verify private access denial, signed-URL expiry, encryption metadata, and database/scanner transport. Deploy TF-IDF privately with its restricted archive reader and service token. Activate real ClamAV and verify clean/rejected fixtures with current signatures. Enable daily backup, periodic restore, deletion-export and freshness jobs. Restore a nonempty document fixture in quarantine and reconcile deletion events before reopening any recovery.

Provider accounts, certificates, cloud permissions and a recovery runner have not been provisioned. Corresponding checklist items must remain pending until this operational evidence exists.
