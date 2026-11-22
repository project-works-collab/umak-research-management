# University of Makati Research Management and Lifecycle Information System (UMak-RMLIS) Privacy Policy

**University of Makati | Center for Research Management**  
**Prepared for academic evaluation: 22 November 2026**  
**Effective date: pending institutional approval**

**Final academic submission — proposed policy pending institutional adoption.**

## 1. Purpose, scope and institutional status

This proposed policy explains the handling of personal data in **UMak-RMLIS**, developed for the University of Makati's Center for Research Management (CRM). The University of Makati is referred to below as UMak. It covers public visitors, student and faculty researchers, authorized staff, and other people whose information appears in research or service records. It addresses the Android application, its backend services, and associated records and documents.

The project team has confirmed this system name. That confirmation resolves the application's naming issue; it does not constitute institutional approval of the application or this policy.

UMak identifies itself as a Personal Information Controller in its official privacy materials. For an adopted university service, UMak would determine the purposes and means of processing through its authorized offices. The formal designation of the system operator, records custodians, privacy escalation responsibilities and service administrators remains subject to institutional confirmation. Student project assignments do not establish official university appointments.

This draft is intended to supplement the University's official **Data Privacy Policy Statement**, including the *Data Privacy Notice for Data Subjects* and relevant student and employee notices. It does not replace those institutional instruments or extend their general descriptions of university services into claims about this application's capabilities. The applicable institutional version must be confirmed before adoption; source-access limitations are recorded in section 14. [Institutional sources appear in section 14.](#14-philippine-legal-and-institutional-references)

The repository documents a synthetic academic demonstration, including a hosted demo. Reviewed source code establishes implemented capabilities, not authorization to operate a production university service. Statements below distinguish those capabilities from proposed obligations and deployment arrangements that remain unverified.

## 2. Personal data and its sources

Information comes from account holders, researchers submitting team details and documents, authorized staff entering or correcting records, and system-generated transactions. Historical research records may also be encoded by authorized staff with a source reference and original registration date.

| Category | Information processed and purpose |
| --- | --- |
| Account and profile | Institutional email, display name, college, student or faculty category, staff role and permissions, account status, verification and password-change records. These support identity, access and account administration. Passwords are stored as adaptive hashes, not recoverable plaintext credentials. |
| Authentication | Verification-code digests, activation or password-change challenges, session-token hashes, session issue and expiry information, and failed-attempt records. These support verification, sign-in, session control and abuse prevention. |
| Research registration and registry | Title, abstract, year or academic period, college or department, registrant and team identifiers, adviser name, keywords, methodology, expected outcomes, research type, target date and submitted declarations. These support registration, team access, registry maintenance and duplicate-record checking. |
| Research lifecycle | Stages, milestone and evidence references, next actions, responsible-contact details, deadlines, completion records, presentation events, publication citations and correction reasons. These support research lifecycle tracking; recording an event does not itself grant academic approval. |
| Services and consultations | Selected service and form revision, subject, description, linked study, requester category and college at submission, attachments, submission dates, statuses, checklists, feedback, correction history, processing checkpoints, next actions, deadlines and account updates. Consultation tickets separately record service type, optional deadline, queue status and assigned staff. |
| Documents and reviews | Submitted PDF or DOCX contents, filenames, file types, sizes, checksums, versions and timestamps; researcher-visible feedback and restricted staff remarks or notes. These support authorized review and document management. |
| Reporting and accountability | Research and service-utilization reports, staff account and record-change histories, consent choices and policy version, deletion requests and completion records, and deletion-recovery identifiers. These support administration and accountability. |
| Technical security | Actor and record identifiers, action and time, request identifiers, IP addresses, selected change metadata, and rate-limiting information. These support access control, incident investigation and integrity checks. Infrastructure providers may maintain additional logs depending on the deployment. |

Education-related information can be sensitive personal information under Philippine law. Uploaded documents may also contain sensitive or privileged information about researchers or participants. The applicable protection depends on the contents, not merely the file label.

Provide accurate, relevant information and include another person's data only where appropriately authorized. Avoid unnecessary identifiers, identifiable participant datasets, health details or confidential material in abstracts, request descriptions and files. A research declaration or upload is not a substitute for applicable ethics review, participant information and consent, or another lawful basis. This application does not administer general university medical, payroll, CCTV or social-media records merely because those activities appear in broader UMak notices.

## 3. Purposes and lawful processing

Processing is intended to administer accounts; register and maintain research; manage authorized reviews, consultations and service requests; record research lifecycle stages, milestones and independent outcomes; produce permitted administrative reports; secure the service; and handle privacy requests and lawful records obligations.

Before institutional deployment, UMak must document the legal basis for each purpose and data category. Under section 12 of the [Data Privacy Act](https://privacy.gov.ph/data-privacy-act/), ordinary personal information may be processed on an applicable basis such as specific consent, contractual necessity, a legal obligation, public-authority functions, or legitimate interests subject to the statutory conditions. An academic relationship alone does not establish contractual necessity for every use. A general reference to a university mandate does not identify a particular legal obligation.

Sensitive personal information and privileged information require a separately applicable exception under section 13. Contractual necessity or legitimate interests under section 12 alone do not authorize their processing. Consent, where relied upon, must meet the applicable requirements; processing under an existing law or regulation must satisfy that exception's safeguards. Public disclosure requires its own assessment.

Any legitimate-interest reliance must be supported by purpose, necessity and balancing assessments under NPC Circular No. 2023-07. The University's charter, education and archives legislation provide relevant institutional context; they do not create blanket permission for unrelated collection, publication or indefinite retention. Transparency, legitimate purpose and proportionality govern all processing under the [DPA IRR](https://privacy.gov.ph/wp-content/uploads/2023/06/IRR_RA-10173-as-amended.pdf).

If information is necessary to process a particular request, omission may prevent that transaction. Optional processing must be identified separately and must not be imposed as a condition of unrelated core services.

## 4. Registration, verification and access

Public visitors can browse published service information, instructions and available blank forms, and search approved public research metadata without registering. Browsing or downloading a blank form does not itself create an account, service request or research registration, although technical connection and security data may still be processed.

Students and faculty self-register using an eligible institutional email address, name, college, researcher category and password. Email verification is required before normal account access. Eligibility depends on the institutional addresses accepted for the service; possessing a UMak address does not automatically establish eligibility.

Authorized account administrators create staff accounts, assign permissions, activate or deactivate accounts and initiate credential recovery. A person issued temporary credentials must replace them before normal protected access. Researcher registration does not confer staff privileges. Researchers can update their name and college; email changes require administrative assistance.

Private access depends on an active authenticated account and the relevant research membership, review assignment or staff permission. Permissions separately cover content, requests, registry, research lifecycle updates, reporting and account administration. Naming an adviser in a record does not automatically give that person an account or access.

The revised requirements and user stories both support researcher self-registration and email verification. The exact versions reviewed and remaining implementation decisions are recorded in [Reviewer appendix A](#appendix-a-requirements-and-implementation-review).

## 5. Public research and restricted records

The public catalogue shows approved records' identifiers, titles, abstracts, approval status, research year and department. Full manuscripts, account emails, team lists, author names, keywords, internal notes and individual service requests are outside that public view.

Research lifecycle tracking covers registration, monitoring, stages, milestones and outcomes and is separate from catalogue approval. Completion, presentation and publication are separate outcomes; recording one does not automatically record another or require a fixed sequence. An outcome does not itself authorize public disclosure. Publication authority and the treatment of personal or confidential information must be confirmed before real records are published.

Research teams can access their permitted records. Assigned reviewers and authorized staff receive access appropriate to their responsibilities. Researcher-visible feedback is separated from internal staff remarks. Some registry and reporting responsibilities cover multiple studies; these are not universal permissions for all employees. Reports and exports must be protected according to their contents.

Duplicate-title checks, queue ordering and research lifecycle planning assist human decisions. They do not establish plagiarism, grant clearance or guarantee completion dates. Raise concerns with the responsible office.

## 6. Research documents and uploads

Submit only relevant documents you are authorized to provide. The reviewed upload workflows accept supported PDF and DOCX files and may reject unsupported, oversized or invalid documents. Follow the instructions for the selected service and protect any private download link.

File-size rules differ between implemented workflows and are not yet reconciled with the proposed requirements. A limit displayed for one workflow must not be assumed to apply to another. Correction submissions may retain earlier files and review history rather than replacing every previous copy.

The exact limits, retained-version behavior and required pre-deployment decision are set out in [Reviewer appendix A](#appendix-a-requirements-and-implementation-review). Those technical differences do not establish an approved university exception. No production release should proceed until the governing upload rule and the Terms, screens and backend have been aligned.

## 7. Consent and optional uses

Separate preferences can be recorded for optional institutional analytics and email notifications; both may be declined. Recording a preference does not mean that the optional service is operating or that an adopted policy has been accepted.

Verification messages, in-application request updates and authorized administrative reports serve different purposes. Existing research and service reporting requires its own established lawful basis and should not be confused with optional analytics.

Before activating an optional purpose, UMak must explain its scope, recipients and consequences and obtain valid consent where required under NPC Circular No. 2023-04. Declining or withdrawing optional consent must not affect unrelated services. Until an accessible preference-change process is verified, direct requests to the privacy contact in section 12. Withdrawal applies to future consent-based processing; it does not automatically invalidate prior lawful processing or remove records retained on another lawful basis. Using the application is not blanket consent.

## 8. Sharing, providers and external channels

Permitted recipients include research team members, assigned reviewers and staff with the necessary permissions. Disclosure to other university offices, regulators or external institutions requires a lawful purpose, appropriate authority and the minimum necessary information. No standing reporting integration with CHED, DOST or other government recipients was established by this review. Legal demands require assessment rather than unrestricted automatic disclosure.

Hosting, verification messages, document storage and recovery can involve service providers. The demonstration's arrangements do not establish approved production providers, locations or safeguards. Before institutional use, UMak must confirm and disclose material provider arrangements, assess transfers and establish appropriate protections. Neither Philippines-only hosting nor an unrestricted right to transfer data abroad is asserted.

For controller-to-controller sharing, apply [NPC Circular No. 2020-03](https://privacy.gov.ph/wp-content/uploads/2021/01/Circular-Data-Sharing-Agreement-amending-16-02-21-Dec-2020-clean-copy-FINAL-LYA-and-JDN-signed-minor-edit.pdf), which superseded Circular No. 16-02, and [NPC Advisory No. 2025-01](https://privacy.gov.ph/wp-content/uploads/2025/07/SGD-2025-01-DSA-Clarification.pdf). A data-sharing agreement is not a universal prerequisite or a substitute for a lawful basis. Processor outsourcing is distinct and requires appropriate instructions, safeguards and accountability.

Some services direct applicants to an external submission channel. Opening a link is not confirmation of a submitted application request. Read that channel's instructions and privacy notice before sending information. Provider and demonstration details appear in the reviewer appendix.

## 9. Retention, deletion and shared research

Retain records only for justified purposes and applicable legal requirements. Public-record management and disposal must take account of [RA No. 9470](https://elibrary.judiciary.gov.ph/thebookshelf/showdocs/2/19575), applicable National Archives rules and an approved university retention schedule. That law does not make every research record permanent or prescribe a universal five-year period. No approved system-specific retention schedule was verified.

Students and faculty can request account deletion through the implemented backend, subject to an explicit staff review confirmation. Staff-account deletion requires separate administrative review. The responsible office must assess lawful retention, other people's rights and preservation duties and explain any justified limitation. This policy creates no automatic clearance hold or blanket refusal rule.

**Deleting the recorded owner's account can also remove coauthored studies and related records. The current mechanism does not selectively preserve all shared research.** This risk must be resolved before destructive deletion is enabled for real institutional records. Ownership transfer, selective retention or another approved safeguard must first be established and verified. This is a proposed production-release condition, not a claim that such protection is already implemented; see the [release blocker in appendix A](#shared-record-preservation-production-release-blocker).

Deleting live records does not immediately erase every backup, retained audit record, document version, local draft or downloaded copy. Recovery and cleanup arrangements require verified operation, approved retention and reconciliation. No immediate complete erasure, fixed backup expiry or guaranteed recovery time is promised. Retained security and deletion metadata also need a justified purpose and retention period.

## 10. Security and information on devices

The reviewed application uses account authentication, access permissions, validation and audit records to help protect information. Their effectiveness depends on secure configuration, maintenance and authorized operation. No absolute security, independent certification, active multi-factor authentication or operational disaster-recovery service is promised by this draft.

The Android application protects stored session material through device-backed encryption and caches public content for offline access. Registration draft text is stored separately in private application preferences and can be deleted locally; application-level encryption for that draft store was not established. Signing out should not be assumed to erase all drafts or exported files. Downloads and reports can remain on a device or in a destination selected by its user.

Protect devices and credentials and remove unnecessary drafts and downloads, especially on shared devices. Private submissions need connectivity; cached information may be outdated.

Before institutional operation, security and governance must be assessed against [NPC Circular No. 2023-06](https://privacy.gov.ph/wp-content/uploads/2024/03/NPC-Circular-Repeal-16-01-Signed.pdf), which repealed Circular No. 16-01. Source controls do not establish completed privacy impact assessments, training, provider reviews or incident arrangements. The reviewer appendix distinguishes tested controls from configuration-dependent capabilities.

## 11. Security incidents and personal data breaches

Suspected loss, unauthorized access, disclosure or alteration of personal data should be reported promptly to the university privacy contact. For an adopted service, UMak must establish responsible personnel and procedures for containment, assessment, evidence preservation, recovery and required reporting. No formally appointed UMak-RMLIS incident-response team was verified.

Under **section 20(f) of [Republic Act No. 10173](https://privacy.gov.ph/data-privacy-act/)** and [NPC Circular No. 16-03, Personal Data Breach Management](https://privacy.gov.ph/wp-content/uploads/2022/01/sgd-npc-circular-16-03-personal-data-breach-management.pdf), mandatory notification applies when sensitive personal information or information enabling identity fraud is reasonably believed to have been acquired by an unauthorized person and the acquisition is likely to give rise to a real risk of serious harm to affected data subjects.

When mandatory notification applies, **both the NPC and affected data subjects must be notified within seventy-two (72) hours** upon knowledge of, or reasonable belief that, the personal data breach has occurred, subject to the Circular's permitted exceptions and procedures. Notification should explain the breach, information involved, protective measures and available assistance. Incomplete initial information is not a reason to wait for the entire investigation before making required notification; supplemental reporting follows the applicable rules.

[NPC Advisory No. 2026-02](https://privacy.gov.ph/wp-content/uploads/2026/05/Advisory-Clarification-on-the-Submission-of-Personal-Data-Breach-Notification-through-DBNMS_11-May-2026.pdf) clarifies notification-related requests through the Data Breach Notification Management System. Filing a request does not suspend obligations or imply approval; NPC approvals must be express and in writing. Security incidents that do not meet mandatory notification criteria still require appropriate assessment and documentation.

## 12. Data-subject rights and contacts

Subject to the conditions and limitations of Philippine law, individuals may seek information about processing; reasonable access to their personal data; correction of inaccuracies; objection or withdrawal of consent where applicable; and blocking, removal or erasure when legal grounds exist. Data portability applies under the statutory conditions, rather than as an unconditional right to every institutional document. Individuals may seek damages where legally warranted and complain to the NPC. Restrictions on a request require a lawful explanation; an internal staff-only label is not by itself a determination of access rights.

Requests should describe the concern and identify the relevant account or record. Identity and authority should be verified proportionately without collecting unnecessary identification. Use a secure channel for sensitive supporting documents. The institution must confirm the approved intake, escalation and response arrangements before release; this draft does not invent service deadlines or promise an automated export facility.

**University privacy inquiries:** the supplied official UMak policy publishes **[dprms@umak.edu.ph](mailto:dprms@umak.edu.ph)** for its Center for Data Protection and Records Management. The current official [office page](https://www.umak.edu.ph/centers/cdprm/) uses the heading *Center for Archives and Information Governance* while retaining data-protection office descriptions. Confirm the current office designation, DPO contact and UMak-RMLIS routing before adoption; no named individual is appointed by this draft.

**Research-service inquiries:** **[research@umak.edu.ph](mailto:research@umak.edu.ph)** appears in an [official UMak research announcement](https://www.umak.edu.ph/call-for-paper-6th-research-congress-umak/). Its publication supports a research contact, not verification of a dedicated privacy-request channel or current system-support arrangement.

**Independent regulator:** the [National Privacy Commission](https://privacy.gov.ph/) provides information on rights and complaint procedures. Contacting the university does not remove any right to seek an available NPC remedy.

## 13. Amendments and decisions before adoption

An adopted policy should identify its approving authority, version and effective date. Material changes to purposes, disclosure, retention or optional processing should be explained through verified institutional channels before they apply, with fresh consent where required. Continued use alone must not be treated as consent to unrelated new purposes.

Before institutional adoption, UMak must confirm:

- The operating authority, DPO and responsible offices, and current institutional privacy instruments.
- Production eligibility, staff permissions, verification and privacy-request procedures, and documentary alignment with the latest requirements.
- Purpose-specific lawful bases, treatment of sensitive data, collaborator access and public-catalogue publication authority.
- A consistent upload rule across research documents, requests, requirements and Terms.
- Providers, locations, recipients, contracts, cross-border protections and verified security and incident arrangements.
- Retention schedules, lawful preservation grounds, coauthored-record handling, local-draft guidance, backup disposal and recovery reconciliation.

This policy was reviewed alongside the [revised academic Terms and Conditions](TERMS_AND_CONDITIONS_FINAL_DRAFT.md). Both carry the same pending-adoption status and disclose the upload discrepancy and shared-record deletion blocker. They are a coordinated pair of proposed academic drafts, not an adopted or deployment-cleared policy suite. The [original Terms](TERMS_AND_CONDITIONS.md) remain unchanged for comparison and contain historical claims that the revised drafts do not endorse.

## 14. Philippine legal and institutional references

The following sources inform this draft within their applicable scope; listing a law does not independently authorize every processing activity.

1. **Republic Act No. 10173, Data Privacy Act of 2012**, especially sections 3, 11–14, 16–18, 20 (including **20(f)** on breach notification) and 21: [official NPC text](https://privacy.gov.ph/data-privacy-act/).
2. **Implementing Rules and Regulations of RA No. 10173**, including processing principles, lawful criteria, safeguards and data-subject rights: [NPC amended reference copy](https://privacy.gov.ph/wp-content/uploads/2023/06/IRR_RA-10173-as-amended.pdf).
3. **NPC Circular No. 16-03, Personal Data Breach Management**; **Circular No. 2023-06, Security of Personal Data in the Government and the Private Sector**; **Circular No. 2020-03, Data Sharing Agreements**; **Circular No. 2023-04, Guidelines on Consent**; **Circular No. 2023-07, Guidelines on Legitimate Interest**; and **Circular No. 2022-04**, concerning registration of processing systems, automated decision-making/profiling notifications and DPO designation: [official NPC issuance register](https://privacy.gov.ph/pips-and-pics/advisories-circulars/). Circulars 16-01 and 16-02 are not the current security and data-sharing baselines. Circular 2020-01 concerns loan-related transactions and is not relied upon for this research system.
4. **NPC Advisory No. 2025-01**, clarification of data-sharing agreement requirements, and **NPC Advisory No. 2026-02**, clarification of breach-notification submissions: official texts linked in sections 8 and 11. Current issuances were checked against the NPC register on 22 November 2026. The consent circular and some university pages were verified through official indexed material or the register where direct retrieval was restricted; inaccessible individual texts are not represented as independently retrieved. NPC Circular No. 2024-01 amends NPC procedural rules and is not the primary breach-management regulation.
5. **Republic Act No. 9470, National Archives of the Philippines Act of 2007**, concerning public-record management, preservation and authorized disposal: [Supreme Court E-Library](https://elibrary.judiciary.gov.ph/thebookshelf/showdocs/2/19575).
6. **Batas Pambansa Blg. 232, Education Act of 1982**, particularly section 9(4) on access to one's school records and their confidentiality: [Senate legislative reference and reproduced text](https://ldr.senate.gov.ph/legislative%2Bissuances/Batas%20Pambansa%20232). Relevant provisions apply subject to subsequent legislation; this policy does not assert that every provision remains unamended.
7. **Republic Act No. 10175, Cybercrime Prevention Act of 2012**, relevant to unlawful access and interference: [Senate legislative reference](https://ldr.senate.gov.ph/legislative%2Bissuances/Republic%20Act%20No.%2010175). It is not cited as establishing a universal one-year log-retention duty.
8. **Makati City Ordinance No. 2020-059, Revised UMak Charter**: the ordinance number and description appear in the supplied official UMak policy's student and employee consent forms and the official [UMak alumni registration notice](https://umak.edu.ph/alumni/register). An authenticated complete ordinance and any amendments were not obtained from an authoritative repository. Its exact operative provisions and relevance to particular processing purposes remain for institutional legal verification.
9. **University of Makati, Data Privacy Policy Statement**, including the *Data Privacy Notice for Data Subjects*, employee notice and consent forms, in the supplied 15-page capture of the [official Privacy Policy page](https://www.umak.edu.ph/privacy-policy/); and the official [Data Privacy Notice for Students](https://umak.edu.ph/olrog/assets/pdf/DATA%20PRIVACY%20NOTICE%20FOR%20STUDENTS.pdf). The official student notice was located through indexed university content; direct access returned an access restriction. The supplied capture supports the institutional principles and published privacy email used here, but does not establish the current adoption date or completeness of subsequent revisions.

## Reviewer appendix — academic evidence and deployment decisions

This appendix records engineering evidence and unresolved decisions. It is separate from the user-facing policy above. Its proposed release conditions do not claim institutional adoption or implemented safeguards.

### Appendix A. Requirements and implementation review

**Current requirements reviewed:** `REQUIREMENTS_ENGINEERING_v6.13.11.2026.docx`, supplied for this review, compared with `USER-STORY_v5.13.11.2026.xlsx`. Both filenames identify 13 November 2026. The full requirements text was inspected, including researcher and staff access, registration, request handling, research registry and outcomes, security and backup requirements, and the Test Conditions and Proposed Rules. The earlier `REQUIREMENTS_ENGINEERING_v3.14.10.2026.pdf` is retained below only as a historical comparison source. No institutional approval is inferred from these filenames or the project specification.

The supplied v6 requirements replace the earlier missing-v5 source limitation for this review. This document does not claim to have inspected the separate v5 file mentioned in the review image. The source comparison now uses the newer v6 supplied by the project team.

**Account model confirmed:** v6 S-A01, C-01 and the Account access rule require researchers to self-register with approved institutional email, verify email ownership before protected functions, and sign in without routine manual CRM approval when active. Staff accounts and permissions are provisioned only by authorized account administrators. The v5 user stories and reviewed backend agree. The earlier v3-only account conflict is resolved at the requirements level and is no longer listed as an unresolved design decision. Operational eligibility, approved configuration and institutional adoption still require confirmation. Email verification establishes email ownership, not independent certification of researcher category or academic claims.

V6's Registration controls rule requires expiry, resend/rate limits and retention of unverified registrations to be defined in approved configuration and retention procedures. The source's current code expiry and resend controls do not establish approval of those settings or an operational unverified-account disposal schedule. Institutional single sign-on remains outside the v6 scope.

**Research lifecycle comparison:** v6 distinguishes stages, milestones and independent completion, presentation and publication outcomes. Its proposed stage vocabulary includes “For revision,” while the backend stores `UnderReview`. These labels must be mapped or reconciled before deployment; they must not be treated as automatically synonymous. This is separate from the resolved account-model issue and does not authorize a linear sequence among research outcomes.

The exact documentary sources available for comparison are identified by SHA-256 below. These hashes identify the exact reviewed files; they do not establish institutional approval. V6 is the current supplied requirements baseline; v3 is historical.

| Source | SHA-256 |
| --- | --- |
| `REQUIREMENTS_ENGINEERING_v6.13.11.2026.docx` | `42660c73fe4445d4c364ff9f5ed4d3d426e170fd5533ba60628b535dd42e039a` |
| `REQUIREMENTS_ENGINEERING_v3.14.10.2026.pdf` | `62dfa5f69c4dc4a7202c51e23b0194c109a2f93c4004fe3796831bc0025dc579` |
| `USER-STORY_v5.13.11.2026.xlsx` | `b8c423e9645226d176da192065d5607aa13c92a862bb400005f3f293b81d28b8` |

#### Upload discrepancy and required governing decision

| Workflow | Reviewed technical behavior |
| --- | --- |
| Service-request attachments | Maximum **10,000,000 bytes per file**; up to **five files per submission attempt**. |
| Correction submissions | Up to five distinct requirement slots across previous and incoming files; earlier versions can remain, so there is no five-file lifetime maximum across all retained versions. |
| Staff registry documents and service blank forms | Maximum **10,000,000 bytes per file**. |
| Separate study-document/manuscript route | Maximum **25,000,000 bytes per file**. This is not an institutionally approved exception. |

V6's Upload limits rule specifies a proposed **10 MB per-file PDF/DOCX baseline** for both research documents and service attachments and **five files per service request**. S-02 requires client validation and repeated server validation before storage. The v5 user stories' S-02 criteria 7–11 and C-03 document criteria support that baseline. These are documented intended rules, not proof of institutional adoption or full implementation. The separate 25,000,000-byte manuscript route conflicts with that per-file baseline, and retained correction versions require clarification of the per-request file count. The original Terms' blanket 25 MB rule is historical; the revised Terms disclose the divergence.

**The intended specification baseline is now confirmed as 10 MB, but the institutionally approved governing rule and retained-version interpretation remain to be confirmed.** Align the revised Terms, screens, validation and all backend routes with the adopted rule before deployment. This documentary review does not change technical limits or authorize the 25 MB route as an exception.

#### Shared-record preservation: production-release blocker

**Do not enable destructive account deletion for real institutional records until an approved shared-record preservation safeguard has been implemented and verified.** The current recorded-owner cascade can delete coauthored research and dependent records. Merely documenting it or requiring staff confirmation does not resolve the risk.

The responsible institution and technical team must establish ownership transfer, selective lawful retention or another approved mechanism; define how other researchers' records and rights are protected; and verify the selected behavior before release. The review condition is a proposed release blocker, not a claim that the application already enforces a deletion hold or that every deletion request must be refused. Requests and lawful remedies must remain available. No particular preservation procedure, retention period or institutional responsibility is invented here.

#### Implemented controls and deployment boundaries

- **Authentication:** configured institutional-domain allowlist and optional address restrictions; six-digit, single-use codes expire after 15 minutes. Protected access checks active and verified accounts. Normal SMTP delivery is configurable. The hosted synthetic demo uses restricted addresses and a protected inbox without external email; it is not production email verification.
- **Access and storage:** database and API permissions restrict studies, requests, feedback and internal notes. Request/registry files are database-backed; the separate manuscript route uses configured S3-compatible storage and signed URLs lasting 600 seconds. These mechanisms are not a public manuscript-release policy.
- **Security:** source includes Argon2id password hashing, signed and server-validated sessions, revocation, throttling, audit histories and audit-chain verification. Privileged operators remain a trust boundary. HTTPS, database TLS, scanning, storage protections and backup operation depend on verified deployment configuration. No MFA integration or universal TLS guarantee is established.
- **Providers and recovery:** documentation describes Render/Supabase demonstration hosting, SMTP and S3-compatible services. Contracts, regions, subprocessors and approved production arrangements are unverified. Encrypted backup, cleanup and deletion-replay tools exist; enabled schedules, version disposal, export freshness and recovery reconciliation need operational evidence.
- **Devices:** Android Keystore-backed session encryption, public offline cache, private-preference registration drafts, local-draft deletion and report exports are present in source. The draft store is not claimed to have application-level encryption. Device instrumentation and production infrastructure were not rerun in this review.
- **Optional processing:** preference recording is implemented, but optional analytics and general email-delivery integration are not established. Administrative reporting and in-application updates are separate capabilities.

### Appendix B. Reproducible implementation and checked test evidence

**Implementation revision:** [`d73faf6a50a9b5b1ae6c7be4282dee46f449be59`](https://github.com/a-a123456789/umak-research-management/tree/d73faf6a50a9b5b1ae6c7be4282dee46f449be59), committed before this review; Git tree `d554150464e1fb1543f61f63d48b8dc02648bdee`. This commit includes the reviewed pagination changes, their test and Android test-class selector. All tracked implementation paths (`src`, `db`, `android`, `scripts`, `test`, dependency manifests) matched that commit before the test run; they remain unchanged in this revision. Untracked planning, benchmark and output files are excluded from this implementation baseline. The policy/Terms edits in this review are subsequent documentation changes and are not claimed to be present in that commit.

Primary sources: [authentication](../../src/auth), [application routes](../../src/app.js), [schema/migrations](../../db), [service requests](../../src/requests/service-requests.js), [registry](../../src/research/registry.js), [research lifecycle progress](../../src/research/progress.js), [document validation](../../src/storage/validate.js), [Android flows](../../android/app/src/main/java/ph/edu/umak/crm/app), [device storage](../../android/core/src/main/java/ph/edu/umak/crm), [hosted demo](../HOSTED_DEMO.md) and [privacy/recovery handoff](../implementation/PRIVACY.md). For immutable versions, use these paths under the linked revision rather than assuming the current branch is unchanged.

**Checked test run:** 22 November 2026, Asia/Manila; command `node scripts/synthetic-acceptance.js --full-tests`, executed against a disposable local database on implementation revision `d73faf6a50a9b5b1ae6c7be4282dee46f449be59`. The process exited successfully. The actual output was inspected: **66 passed, zero failed, zero skipped**. This replaces the earlier unsupported standalone verification claim with a checked, revision-specific record. It is local synthetic evidence, not independent institutional review or production certification.

The [embedded test-log excerpt](#checked-test-log-excerpt) below preserves the named test results and runner totals from that run. Full captured stdout/stderr SHA-256: `d1ead294389ac77d3dc374db2d2c50fccdabf90c3fe2372ccca78a947e3d9a08`. Database initialization and other setup output are omitted from the excerpt; the hash identifies the complete captured stream, not the excerpt. No separate evidence deliverable is created.

#### Checked test-log excerpt

<details>
<summary>Local backend test results — 22 November 2026</summary>

```text
✔ worker response validates complete shapes, types and input membership (0.569584ms)
✔ algorithm API: authentication, staff boundary, schema and standalone agreement (136.9985ms)
✔ worker failures, timeout, concurrency, cancellation and recovery are bounded (183.740708ms)
✔ worker protocol rejects incomplete objects, nonzero exit and oversized output (87.127792ms)
✔ malformed worker results return 502 through the authenticated HTTP route (40.164791ms)
✔ EDF heap orders overdue, tied and absent deadlines; inputs and outputs are isolated (0.551125ms)
✔ EDF randomized mixed workloads agree with sorted reference including position updates (111.364875ms)
✔ EDF heap rejects invalid/duplicate IDs and handles absent updates (0.20625ms)
✔ deadlines require real calendar instants in explicit UTC (0.754875ms)
✔ researcher profile persists permitted edits without changing account identity (78.08775ms)
✔ consultation retry returns one ticket after concurrent sends (11.837792ms)
✔ trigram typo retrieval, normalization, metadata filters and draft privacy (18.4705ms)
✔ researcher workspace lists and updates only the owner’s drafts (11.076209ms)
✔ EDF queue uses deadline, submission and ID ties; claim is durable and audited (33.383667ms)
✔ ticket scope, version conflicts, reschedule, cancel and resolve permissions (19.02875ms)
✔ 16 concurrent claimers never assign a ticket twice; empty queue returns 204 (31.01325ms)
✔ ticket mutation rolls back if audit insertion fails (3.683792ms)
✔ migration backfills existing titles, preserves visibility and is repeatable (8.219ms)
✔ locked earliest ticket is skipped and becomes eligible again after rollback (7.64175ms)
✔ search threshold is transaction-local and does not leak pooled session settings (4.276375ms)
✔ native RLS fails closed, isolates profiles and prevents pooled identity leaks (99.103625ms)
✔ assigned reviewer allowed, unrelated staff/faculty denied; membership grants and revokes access (20.938792ms)
✔ internal feedback cannot cross researcher/API/database boundaries (10.575583ms)
✔ audit history is append-only with actor/target/IP and no private content (13.009958ms)
✔ Argon2id salts, malformed hashes, Unicode, legacy verification and upgrade (83.331792ms)
✔ temporary activation is staff-provisioned, expiring, hashed and single-use (49.003959ms)
✔ shared failed-login window is atomic across workers and expires individually (55.456667ms)
✔ atomic token bucket cannot overshoot and refills elapsed capacity (1.335333ms)
✔ PDF/document validation rejects spoofing, encrypted archives and oversized buffers (2.312333ms)
✔ private upload/download checks membership and assignment; failed scanner leaves no object (191.867084ms)
✔ baseline: anonymous private research denied before record access (12.396333ms)
✔ baseline: researcher denied staff audit endpoint (3.169167ms)
✔ baseline: private research lookup binds identity in parameterized API predicate (2.538792ms)
✔ baseline: dependency failure returns sanitized JSON (2.404667ms)
✔ baseline: malformed and oversized JSON rejected (5.640333ms)
✔ baseline: legacy opaque tokens rejected before session lookup (3.394541ms)
✔ DOCX validates container and rejects macros, missing parts and expansion limits (128.873916ms)
✔ ClamAV scanner protocol accepts clean response and rejects malware (5.476584ms)
✔ encrypted backups roundtrip; modified bytes/wrong key and weak key-file permissions fail (5.608ms)
✔ public cache catalogue has stable ETag and checksummed blank template (6.928375ms)
✔ production database refuses plaintext/unverified configuration (0.1345ms)
✔ real SDK presigner binds private key and ten-minute expiry without cloud calls (7.2235ms)
✔ persisted lifecycle keeps independent outcomes and enforces scope/version (105.405333ms)
✔ revisions preserve prior bytes/checksums and reject stale predecessors (215.691708ms)
✔ audit verifier detects content changes, missing tails, and rollback-safe appends (15.598459ms)
✔ researcher account becomes usable after verification in a local SMTP inbox (133.162583ms)
✔ SEC-01 public search returns only approved records; exact SQL injection is neutralized (64.141542ms)
✔ authentication rejects missing, invalid, expired and revoked tokens (34.436083ms)
✔ SEC-02 own record succeeds; another student record hidden; staff allowed (8.301834ms)
✔ record creation binds ownership; blocks privilege fields; logs changes (9.084791ms)
✔ consent: optional choices can both be false; identity/time cannot be supplied (6.649625ms)
✔ SEC-11 runtime database role cannot alter schema or modify audit history (5.334208ms)
✔ deletion requires staff review; cascades personal data and invalidates sessions (18.126042ms)
✔ unknown routes deny by default; malformed/oversized JSON handled (2.179292ms)
✔ login throttles repeated attempts with HTTP 429 (55.354208ms)
✔ SEC-09 public search caps large synthetic result sets at 100 (16.047958ms)
✔ JWT sessions authenticate, expire at 15 minutes and reject tampering (0.739125ms)
✔ signed claims still require intended issuer, audience, lifetime and subject (0.254833ms)
✔ production refuses missing or malformed signing keys (0.1105ms)
✔ interactive session lease expires exactly at thirty minutes (0.074792ms)
✔ skill admission rejects drift, unregistered skills, capabilities and symlinks (32.129916ms)
✔ registry directory uses validated descending cursor and retains authorization (15.305583ms)
✔ progress directory uses validated descending cursor and retains authorization (4.574334ms)
✔ saved evidence and task completion drive readiness and remaining critical path (72.667208ms)
✔ cycles and inconsistent completed dependencies are rejected by the Python engine (35.975333ms)
✔ planning requires the complete saved milestone set and does not invent durations (0.25125ms)
ℹ tests 66
ℹ suites 0
ℹ pass 66
ℹ fail 0
ℹ cancelled 0
ℹ skipped 0
ℹ todo 0
ℹ duration_ms 3766.3925
```

</details>

### Appendix C. Coordinated draft status and assistance disclosure

The [revised Terms](TERMS_AND_CONDITIONS_FINAL_DRAFT.md) were reviewed alongside this policy for account access, public/private boundaries, research lifecycle outcomes, uploads, deletion, local storage, privacy, provider disclosures and remedies. Both now use the supplied v6 requirements and identify the same upload implementation discrepancy, stage-vocabulary reconciliation and shared-record release blocker. The previously missing current-requirements source is no longer a limitation. They remain **coordinated proposed academic drafts with unresolved release conditions**; neither is an adopted or deployment-ready institutional policy. The original policy and Terms remain unchanged for comparison.

**AI-assistance disclosure:** OpenAI Codex assisted with source inspection, comparison of supplied materials, drafting, restructuring and local verification commands. The project team remains responsible for checking accuracy, legal and institutional interpretation, source completeness and the final submission. No independent lawyer, university official, DPO or another team member is represented as having approved this revision.

Relative Markdown file links and local section links were checked from `docs/policies/` after placement. Links outside the repository remain subject to the official sites' access restrictions recorded in section 14; this review does not claim every external page was retrievable. If these drafts are relocated or exported, links must be rechecked for that destination.
