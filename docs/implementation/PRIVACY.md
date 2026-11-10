# Privacy behavior and production handoff

User-facing academic proposals: [Privacy Policy](../policies/PRIVACY_POLICY.md) and [Terms and Conditions](../policies/TERMS_AND_CONDITIONS.md). Both await institutional adoption; see the [adoption schedule](../policies/README.md#institutional-adoption-schedule).

Reconciled 10 November 2026 against `1a305391339b8258ab2f05737717aa0038fd3d97`. The application uses synthetic teaching data. Source implementation and prior test evidence do not establish an operational university service.

## Data and access

Accounts include email, role, password hashes and session records. Research includes ownership/membership and reviewer assignments. Native FORCE RLS and explicit API projections restrict private studies to members and assigned staff. Researcher-visible feedback and internal notes are separate. Approved catalogue title, abstract, year and department are public; manuscript files are not. Publication authority for those fields needs institutional confirmation.

Consultation tickets store requester, service type, optional deadline, submission/claim times, status, assigned staff and version. Researchers see their own tickets; staff access the processing queue. These endpoints do not collect a freeform consultation narrative.

## Consent and security metadata

The consent endpoint derives user identity from authentication and records server time. It accepts policy version `2026-10-v1` and separate boolean `institutional_analytics` / `email_notifications` choices. Both may be false. Repeated submissions append history; the latest record represents current preferences. No analytics or email-delivery integration is active. Publishing the new documents does not change the accepted API version or implement terms acceptance.

The consent payload does not accept an IP address, user agent, real name or client timestamp. However, the application as a whole **does** process security metadata: `audit_history` includes actor/target/action, old/new metadata, timestamp, request ID and IP address; shared throttling also uses IP information. Sensitive field contents are excluded from audit payloads. Runtime permissions prevent direct history modification, but a privileged database owner remains a separate trust risk. No cryptographic audit chain is claimed.

Core processing needs an institutionally established lawful basis and notice; optional consent is not a substitute. Student DPO assignments are coursework responsibilities, not official appointments.

## Reviewed deletion

Student and faculty accounts may submit an empty-object deletion request. Staff completion requires `confirm_review: true` and calls `crm_complete_deletion` transactionally. Staff account deletion needs separate administrative review. The transaction removes eligible account records and cascading owned research/related records, retains appropriate request/audit metadata, and queues document cleanup. It is not a selective shared-study preservation process: ownership, coauthors, legal holds and retention must be reviewed before completion.

Deleting the requester cascades consultation deletion; removing an assigned staff account clears its assignment reference. Minimal retained security/request metadata needs an approved basis and retention period.

## Recovery and erasure boundaries

Encrypted snapshot/restore tools, object cleanup, independent encrypted deletion-event export/replay and systemd unit templates are implemented in source. The earlier statement that all production-erasure controls were unimplemented is superseded by the [implementation guide](IAS_IMPLEMENTATION.md). Existing evidence and remaining operational checks are recorded in [implementation evidence](../../evidence/ias-plan/IMPLEMENTATION.md).

Deployment, offsite configuration, enabled timers and populated-object recovery require their own verification. The proposed deletion export interval can leave recent, unexported deletions absent after a disaster; restore quarantine and operator reconciliation remain necessary. A versioned object-store delete marker does not erase older versions. Retention/lifecycle rules and independent deletion evidence must be governed before release.

Soft deletion hides records; hard deletion removes live rows but not backups. Per-person cryptographic shredding is not implemented. It would require separately governed keys and verified destruction of every usable copy. No immediate or guaranteed total-erasure claim is made.

## Adoption handoff

Confirm operator/DPO contacts, purpose-specific legal bases, publication permissions, providers/regions, retention/holds, shared-study handling and request procedures using the [policy schedule](../policies/README.md#institutional-adoption-schedule). The supplied research-office email is not verified as the institution's privacy-request channel. No database, consent contract, deployment or institutional policy was changed by this documentation update.
