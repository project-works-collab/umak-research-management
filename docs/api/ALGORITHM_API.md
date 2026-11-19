# Algorithm service contract

`POST /api/v1/algorithm/solve` requires a valid staff bearer session. It evaluates a supplied synthetic planning scenario. Registered research also uses the saved workflow integration described below.

Send `Content-Type: application/json`. The object must contain exactly these six fields:

| Field | JSON type and constraint |
|---|---|
| steps | 1–5,000 unique ASCII IDs, 1–64 letters/digits/underscore/hyphen |
| dependencies | At most 25,000 unique `[prerequisite, dependent]` pairs referencing steps; acyclic |
| completed_actions | Unique existing step IDs; every completed step's prerequisites must also be completed |
| requirements | Map from existing step IDs to lists of `{id, gate}`; gate is `received` or `verified`; at most 50,000 memberships; no duplicate pair per step |
| evidence | At most 50,000 unique `{id, state}` records; state is `received` or `verified` |
| durations | Exactly one integer minute estimate per step, 0–525,600; booleans and fractions rejected |

Verified evidence satisfies both gates; received evidence satisfies only the received gate. Receipt of payment proof can make a staff verification action ready; it never marks verification complete. Completed steps contribute zero remaining minutes. These are engineering bounds, not measured CRM workloads.

Example: `algocom/example.json`. Run standalone: `python3 algocom/worker.py < algocom/example.json`.

Success HTTP 200 returns `status` (`SUCCESS` or `COMPLETE`), the readiness fields defined by `prototype.py`, `earliest_finish_minutes` (step-to-integer map), `remaining_duration_minutes` (integer), `critical_path` (ordered incomplete step IDs), `duration_model` (limitations), and `metrics.worker_elapsed_ms` (nonnegative number). A critical path certifies the longest remaining prerequisite chain, assuming unlimited parallel capacity; evidence waits, staffing, external delays and calendar dates are excluded.

Errors: 400 malformed JSON or invalid graph/state; 401 unauthenticated; 403 non-staff; 413 body over 2 MiB; 503 capacity/unavailable worker; 504 five-second timeout; 502 failed/invalid/oversized worker output. Algorithm validation returns `{status:"ERROR", error_type, message}`; worker lifecycle errors use the same structured envelope; JSON-parser and authorization errors use the application's `{error}` envelope. See executable tests for exact values. Client disconnect cancels the child; no HTTP response is deliverable after disconnect.

Express spawns a fixed Python script without a shell, with no database credentials, two concurrent workers per server process, no waiting queue, a five-second wall timeout, and an 8 MiB stdout ceiling. Python reads at most 2 MiB. These controls bound ordinary prototype requests; they do not constitute an OS memory sandbox or multi-instance admission control.

## Machine-readable contracts

JSON Schema Draft 2020-12 contracts: `algocom/schemas/request.schema.json` and `response.schema.json`. These are worker request/result contracts; transport errors retain their documented envelopes. Schema validation covers structure; Python checks aggregate membership counts, references, unique evidence IDs, cycles and completed-state consistency. Express validates complete result shapes, types, membership and completion invariants before returning success; malformed worker output becomes HTTP 502 WORKER_PROTOCOL.

## Registered research workflow

Android research progress uses the same Python runner through authorized CRM endpoints.

| Endpoint | Behavior |
|---|---|
| GET /api/v1/researcher/registrations/:id/planning | Linked researchers read readiness, blocking reasons and the remaining critical path. |
| GET /api/v1/staff/research-progress/:id/planning | Staff with progress-update permission read the calculation and saved definition. |
| POST /api/v1/staff/research-progress/:id/workflow | Save expected_version and definition after validating the graph with Python. |
| POST /api/v1/staff/research-progress/:id/events | Milestone events record evidence review state and enforce configured prerequisites. |

Each definition contains tasks with code, integer duration_minutes, prerequisites (milestone codes), and requirements (objects with id and a received or verified gate). It covers the study's milestone set exactly once. Cycles, missing milestones, invalid references, duplicate edges and inconsistent completed prerequisites are rejected before saving. Staff configure the minutes. An unconfigured study returns configured=false and displays configuration guidance.

The backend builds the algorithm payload from one authorized database snapshot. Completed and Not applicable milestones satisfy dependencies and contribute zero remaining duration. Pending and In progress milestones remain in the calculation. An explicit evidence_state is saved by authorized staff together with a nonempty evidence reference. Free text alone does not satisfy an evidence gate.

Starting or completing a milestone checks configured prerequisites and evidence, including evidence recorded in that save. Reverting an upstream milestone is rejected when completed downstream prerequisites would become inconsistent. Workflow and progress saves share the research version, so a concurrent change requires reload. Completed task evidence gates are treated as historical when calculating pending work.

Responses include the research version, saved definition, ready and blocked tasks with display names, remaining work minutes, earliest finish minutes and critical path. Staff saves reload the calculation. Researchers refresh saved progress to see the same result. Estimates describe dependency work with unlimited parallel capacity and exclude staffing, evidence waits and calendar dates.

Run node --test test/workflow-planning.test.js for real Python-engine checks. Run npm run acceptance:synthetic -- --smoke for saved graph, evidence, access, conflict and milestone gating checks against isolated PostgreSQL. The optional CatalogHttpsTest Android journey configures a workflow, completes a prerequisite, verifies evidence and checks the linked researcher's updated estimate.
