# CRM policies — academic submission

**Final academic submission — proposed policy pending institutional adoption.**

- [Privacy Policy](PRIVACY_POLICY.md)
- [Terms and Conditions](TERMS_AND_CONDITIONS.md)
- [Generator source archive](generator-original/README.md) — unadapted evidence, not approved policies
- [Technical privacy handoff](../implementation/PRIVACY.md)

Prepared 10 November 2026 against repository commit `1a305391339b8258ab2f05737717aa0038fd3d97`. This update adds documentation, not application features, deployment or a terms-acceptance mechanism. Original Word/PDF coursework copies remain unchanged and describe their own preparation snapshot. These Markdown copies incorporate the subsequently supplied generator text and corrections.

## Institutional adoption schedule

| Decision | Required information / review | Responsible institutional function |
| --- | --- | --- |
| A1 Operator and eligibility | Legal entity, responsible office, approval authority, account eligibility, minors procedure, effective date and version | Administration and legal |
| A2 Contacts and rights | Confirm research@umak.edu.ph routing; authorized DPO and postal address; secure request channel; identity verification and response procedure | DPO and service owner |
| A3 Purposes and permissions | Lawful basis per purpose, sensitive-data conditions, coauthor authority, evaluation permissions and public catalogue approval | DPO, research office and legal |
| A4 Providers and operations | Providers, primary/backup regions, transfers and safeguards, processor agreements, verified hosting/mobile/recovery controls | IT and DPO |
| A5 Retention and erasure | Period and trigger for each category, legal holds, shared-study handling, backup/object-version expiry and deletion-ledger retention | Records office, DPO and IT |
| A6 Service procedures | Membership and reviewer changes, feedback disputes, restrictions/appeals, scheduling, outage alternatives, acknowledgment and change notices | Service owner and legal |

Before adoption: complete A1–A6, review both documents together, verify actual operational claims, obtain genuine approval and publish the approved version. Student C-suite assignments do not establish official institutional appointments.

## Feature-to-clause review

| Source at the pinned revision | Policy connection | Evidence boundary |
| --- | --- | --- |
| [Application routes](../../src/app.js), [validation](../../src/security/validation.js), [database migrations](../../db/migrations) | Accounts, research membership, assigned reviewers, separate notes, consent, reviewed deletion and audit metadata | Source inspection; no new deployment or database exercise performed for this documentation update |
| [Document routes](../../src/storage/routes.js), [storage adapter](../../src/storage/s3.js) | Restricted PDF/DOCX uploads, size limit, temporary access | Adapter implementation does not prove live cloud configuration |
| [Implementation guide](../implementation/IAS_IMPLEMENTATION.md) and [implementation evidence](../../evidence/ias-plan/IMPLEMENTATION.md) | Encryption, recovery, deletion replay, Android library and remaining checks | Existing evidence retains its original scope; timers, object-version expiry and device integration require operational verification |
| [Algorithm contract](../api/ALGORITHM_API.md), [search and scheduling](../api/SEARCH_AND_CONSULTATIONS.md) | Suggestions, structural estimates and consultation ordering | No originality guarantee, guaranteed appointment or automatic academic decision |

## Generator corrections

The supplied originals retain generic analytics, Google Play Services, foreign-law, retention, age, content-licensing and liability clauses. They are archived unchanged to preserve provenance. The adapted policies remove unsupported claims and identify institutional decisions instead. No EU representative, transparency report, adopted retention period or institutional approval is asserted. The user's later instruction is to retain the repository's current public visibility; repository publication is not policy adoption.

## References

1. Republic of the Philippines, [Republic Act No. 10173, Data Privacy Act of 2012](https://privacy.gov.ph/data-privacy-act/), especially sections 11–14, 16, 18, 20–21. Legal basis and sensitive-data requirements must be assessed per purpose.
2. National Privacy Commission, [Implementing Rules and Regulations of the Data Privacy Act of 2012](https://privacy.gov.ph/implementing-rules-regulations-data-privacy-act-2012/), 2016, rules IV–VI and VIII–IX. These qualify rights, retention, safeguards and notification duties.
3. National Privacy Commission, [Breach Reporting](https://privacy.gov.ph/pips-and-pics/breach-reporting/), accessed 10 November 2026. Notification depends on applicable conditions; the policies do not treat every incident as a reportable breach.
4. Republic of the Philippines, [Republic Act No. 10175, Cybercrime Prevention Act of 2012](https://lawphil.net/statutes/repacts/ra2012/ra_10175_2012.html), section 4(a), unauthorized access and interference context.
5. Nishant Srivastava, [App Privacy Policy Generator](https://github.com/nisrulz/app-privacy-policy-generator). User-supplied generated text dated 10 November 2026 is preserved in the source archive. The generator is a starting template, not evidence of compliance or deployment.

## Assistance and validation

ChatGPT/Codex assisted adaptation of the supplied generator output, comparison with repository features, authoritative-source review and document/link checks. Institutional legal review, contact confirmation and operational verification remain outstanding. This is not a claim of completed human review.
