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

The intended researcher access model is self-registration by students and faculty using institutional email accounts, followed by verification before normal access. The backend implements this model: it accepts only student or faculty categories and email domains on the configured allowlist, with any additional configured address restrictions. It collects name, college and password and checks a single-use, six-digit verification code that expires after 15 minutes. Unverified accounts cannot complete normal sign-in or use protected sessions. Approved production domains and email delivery arrangements still require institutional confirmation; the source does not hardcode every UMak address as eligible.

The hosted demonstration instead restricts registration to designated synthetic addresses and displays codes in a protected demo inbox without external email delivery. That inbox is not an approved production verification channel.

Authorized account administrators create staff accounts, assign specified permissions, activate or deactivate accounts and initiate credential recovery. Temporary credentials require replacement before normal protected access. Researcher self-registration does not confer staff permissions. Profile name and college can be updated through implemented controls; email changes require administrative assistance.

The reviewed Requirements Engineering baseline proposes staff-provisioned accounts and excludes public self-registration. It therefore differs from the team's intended researcher model and the implemented self-registration route. The institution must confirm the deployment model and reconcile the requirements before release. No formal amendment of that baseline is claimed.

Private functions require an active authenticated account. Access depends on research membership, review assignment or the relevant staff permission. Staff permissions separately cover content, request processing, registry, progress, reporting and account administration. A person's adviser name in a record does not automatically create an adviser account or grant access.

## 5. Public research and restricted records

The reviewed public research projection exposes approved records' identifiers, titles, abstracts, approval status, research year and department. Full manuscripts, account emails, team lists, author names, keywords, internal notes and individual service requests are outside that public projection.

Research lifecycle tracking covers registration, monitoring, stages, milestones and outcomes and is separate from catalogue approval. Completion, presentation and publication are separate outcomes; recording one does not automatically record another or require them to occur in a fixed sequence. A recorded research lifecycle outcome does not automatically authorize public disclosure. Before publishing real records, UMak must confirm publication authority and review abstracts for personal or confidential information.

Research teams access their permitted private records. Assigned reviewers and authorized staff access information through the relevant controls. Researcher-visible feedback is separated from internal staff remarks. Registry and reporting permissions can expose information across records for their authorized administrative purposes; they are not limited in every case to an individual review assignment. Reports and exported summaries must receive protection appropriate to their contents.

Duplicate-title checks, consultation queue ordering and workflow-planning estimates support administrative decisions. They do not themselves establish plagiarism, approve research, grant clearance or guarantee a completion date. Concerns about a record or its processing should be raised with the responsible office.

## 6. Documents and upload limits

Only supported PDF and DOCX documents are accepted by the reviewed upload routes. File validation and, in the normal backend configuration, a configured scanner are required. The synthetic demo does not establish production malware scanning.

**Current technical limits differ from the proposed institutional baseline:**

| Upload workflow | Verified implementation |
| --- | --- |
| Service-request attachments | Maximum **10,000,000 bytes per file**; up to **five files per submission attempt**. Correction resubmissions can add retained versions. The backend also limits distinct requirement slots across existing and incoming files to five; it does not impose a five-file lifetime cap across all versions. |
| Staff registry documents and service blank forms | Maximum **10,000,000 bytes per file**. |
| Separate study-document/manuscript route | Maximum **25,000,000 bytes per file**; this does not raise the service-request limit. |

The reviewed Requirements Engineering document proposes PDF/DOCX files up to 10 MB each for both service requests and research documents, and five files per service request. The existing Terms describe 25,000,000 bytes per submission. No evidence establishes institutional approval of a permanent distinction between manuscript and request limits. UMak must approve a consistent rule and reconcile requirements, interfaces, backend routes and Terms before institutional release. This policy records the present discrepancy; it does not authorize larger uploads as an exception.

Some files are held in database-backed request or registry records; the separate study-document service uses configured object storage and temporary signed download links. Access remains restricted. Do not redistribute private documents or access links to unauthorized recipients.

## 7. Consent and optional uses

The backend can record separate choices for institutional analytics and email notifications, with a policy version and server timestamp; both choices may be false. Later submissions record updated preferences. These records do not prove that optional services are operating or that an adopted policy has been accepted.

The reviewed application does not establish an optional analytics integration or general optional email-notification delivery. Verification email and in-application request updates are separate functions. Permission-controlled research and service reports already exist and must have their own established lawful basis; they must not be described as inactive merely because an optional analytics choice exists.

Before an optional purpose is activated, UMak must explain its scope, recipients and consequences and obtain valid consent where required under NPC Circular No. 2023-04. Withdrawal must be available without affecting unrelated services. Until a user-facing preference workflow is verified, requests may be directed to the privacy contact in section 12. Withdrawal affects consent-based future processing and does not automatically invalidate prior lawful processing or remove records retained on another lawful basis. Use of the application is not blanket consent to all possible processing.

## 8. Sharing, providers and external channels

Permitted recipients may include research team members, assigned reviewers and staff with the necessary permissions. Any additional disclosure to other UMak offices, regulators or external institutions requires an identified lawful purpose, minimum necessary information and appropriate authority. No standing integration with CHED, DOST or other government recipients was established by this review. Valid legal demands require assessment rather than automatic unrestricted disclosure.

The source supports SMTP verification, PostgreSQL database hosting, S3-compatible document storage and backup infrastructure. Deployment documents describe a Render and Supabase synthetic demonstration. Those documents do not establish approved production providers, contracts, subprocessors, storage locations or operational safeguards. External submission links direct users to another channel; opening one is not confirmation that an in-app request was submitted. Review that channel's notice before sending information.

For controller-to-controller sharing, apply [NPC Circular No. 2020-03](https://privacy.gov.ph/wp-content/uploads/2021/01/Circular-Data-Sharing-Agreement-amending-16-02-21-Dec-2020-clean-copy-FINAL-LYA-and-JDN-signed-minor-edit.pdf), which superseded Circular No. 16-02, and [NPC Advisory No. 2025-01](https://privacy.gov.ph/wp-content/uploads/2025/07/SGD-2025-01-DSA-Clarification.pdf). A data-sharing agreement is not a universal prerequisite or a substitute for a lawful basis. Processor outsourcing is a distinct arrangement requiring appropriate instructions, safeguards and accountability.

Before production use, UMak must confirm the provider and recipient inventory, disclose material arrangements, assess transfers and establish appropriate contractual or other protections. No Philippines-only hosting or prohibition on cross-border transfers is asserted. The reviewed source contains no advertising or commercial tracking integration; this is not a guarantee concerning future deployments or provider logging.

## 9. Retention, deletion and recovery

Records should be retained only for justified purposes and applicable legal requirements. Public-record retention and disposal must take account of [RA No. 9470](https://elibrary.judiciary.gov.ph/thebookshelf/showdocs/2/19575), applicable National Archives rules and an approved university retention schedule. That law does not make every research record permanent or establish a universal five-year period. No approved system-specific schedule was verified for accounts, research, service records, documents, logs or backups.

Students and faculty can submit an account-deletion request through the implemented backend. Staff completion requires an explicit review confirmation. Staff-account deletion follows a separate administrative review path. These capabilities do not establish a verified in-app interface for every privacy request or an adopted response timetable.

Deleting a researcher account can delete research records for which that account is the recorded owner, including records with coauthors, together with dependent records. Removing a coauthor who is not the recorded owner does not by itself invoke that ownership cascade. Selective preservation of shared research is not implemented by the deletion mechanism. These consequences require institutional and technical review before production adoption. Before completion, the authorized office must assess lawful retention, other people's rights, shared records and any applicable preservation duty, and explain any justified refusal or limitation. This draft creates no automatic clearance hold, academic sanction or blanket prohibition on deletion.

Removing live database records does not immediately erase backups, retained audit or deletion records, downloaded files, or all object-storage versions. Source tools support encrypted backups, queued object cleanup and deletion-event replay during recovery. Actual schedules, offsite arrangements, version disposal and restore reconciliation depend on deployment and operator action. Recent deletion events may be missing from an earlier export. No fixed backup expiry, guaranteed recovery time or complete immediate erasure is promised. Retained deletion and security metadata also require a documented purpose and retention period.

## 10. Security and information on devices

The reviewed implementation includes salted Argon2id password hashing, signed sessions with server-side validation and revocation, inactivity controls, input validation, throttling, database access restrictions, permission checks and audit histories. Audit-chain mechanisms can help detect changes; they do not make privileged administrators incapable of altering a system. Secure deployment and monitoring remain necessary.

Source configurations support HTTPS, protected database connections, file validation and scanning, private document storage and encrypted recovery tools. Their effectiveness depends on configuration, maintenance and operational verification. The hosted demo and development variants must not be treated as evidence that every production safeguard is active. Multi-factor authentication was not established in the reviewed implementation. No absolute security, universal TLS version, independently certified environment, operational disaster-recovery service or particular physical control is guaranteed here.

The Android app protects stored session material through Android Keystore-backed encryption and caches public content for offline access. It also saves account-scoped registration draft text in private application preferences, with a local-draft deletion control. That draft store is distinct from encrypted session storage; this policy does not claim it has application-level encryption. Signing out must not be assumed to delete every local draft or previously exported file. Staff report exports and user-selected downloads may remain on devices or in destinations chosen by the user.

Use device access protection, avoid shared credentials and remove unneeded drafts and downloads, particularly on shared devices. Private submissions require connectivity; cached information may be outdated.

Before institutional operation, safeguards and governance must be assessed against [NPC Circular No. 2023-06](https://privacy.gov.ph/wp-content/uploads/2024/03/NPC-Circular-Repeal-16-01-Signed.pdf), *Security of Personal Data in the Government and the Private Sector*. It repealed Circular No. 16-01. Source controls do not establish completion of privacy impact assessments, training, provider reviews, incident arrangements or other required organizational measures.

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
- The registration model, eligibility, staff permissions, production verification channel and privacy-request procedures.
- Purpose-specific lawful bases, treatment of sensitive data, collaborator access and public-catalogue publication authority.
- A consistent upload rule across research documents, requests, requirements and Terms.
- Providers, locations, recipients, contracts, cross-border protections and verified security and incident arrangements.
- Retention schedules, lawful preservation grounds, coauthored-record handling, local-draft guidance, backup disposal and recovery reconciliation.

The existing [Terms and Conditions](TERMS_AND_CONDITIONS.md) were reviewed and remain unchanged. Their outdated system expansion must be aligned with the team-confirmed UMak-RMLIS name. Their 25 MB upload rule, coauthor-related deletion restrictions, local-storage description and broader institutional or operational claims also require reconciliation by the authorized institution. This draft does not affirm their unverified guarantees, sanctions, copyright provisions or exclusive court jurisdiction, and does not itself amend or replace them.

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

### Academic review basis

Implementation was reviewed on 22 November 2026 against Git HEAD **`b2d9388557696876c369986418ecf4b9365587bd`**, including the working-tree versions of [registration](../../src/research/registrations.js) and [progress routes](../../src/research/progress.js), which already contained uncommitted pagination changes. The starting working tree also included changes to `scripts/android-synthetic-acceptance.js` and untracked benchmark, planning, output and pagination-test files. The final-draft policy itself was untracked. These pre-existing changes were preserved; only this policy was edited during finalization. The reviewed baseline is therefore HEAD plus the available working tree, not a clean immutable checkout or a certified production deployment.

Primary evidence included [backend routes](../../src/app.js), [authentication](../../src/auth), [schema and migrations](../../db), [service requests](../../src/requests/service-requests.js), [research registry](../../src/research/registry.js), [reporting](../../src/reporting/reports.js), [document routes](../../src/storage/routes.js), [Android researcher flows](../../android/app/src/main/java/ph/edu/umak/crm/app/ResearcherScreen.java), [session storage](../../android/core/src/main/java/ph/edu/umak/crm/security/SessionVault.java), [hosted-demo boundaries](../HOSTED_DEMO.md), [deployment documentation](../SUPABASE_RENDER_DEPLOYMENT_PLAN.md), and [recovery/privacy documentation](../implementation/PRIVACY.md). Older explanatory documents were checked against current source rather than treated as conclusive.

The locally supplied **REQUIREMENTS_ENGINEERING_v3.14.10.2026.pdf**, particularly S-A01, S-01–S-05, staff modules, NFR03/NFR06 and proposed rules 2–15, was reviewed alongside repository plans. It proposes account, upload and operational rules; it does not establish institutional approval or proof that backup, availability or other service targets have been achieved. The registration and upload conflicts are expressly preserved above for resolution.

Verification for this finalization included the existing backend test suite through `node scripts/synthetic-acceptance.js --full-tests`, using a disposable local database. All **66 tests passed**, with no failures or skipped tests. Relevant checks cover institutional-domain rejection, blocked unverified sign-in, expired and reused verification codes, researcher privilege rejection, restricted records and files, consent validation, reviewed deletion, audit integrity, document validation, encryption utilities and session controls. No application tests were added or modified. Android behavior was inspected in source and existing device tests; Android instrumentation and production infrastructure were not rerun or certified by this policy review.

| Evidence status | Scope and limitation |
| --- | --- |
| Implemented and verified in local tests | Backend authentication and verification controls, access boundaries, consent recording, deletion behavior and the other tested safeguards described above. Synthetic results do not establish production approval. |
| Implemented in source, not operationally verified here | Android Keystore session storage, local registration drafts and public cache; deployed email, storage and recovery configurations. |
| Partially implemented | Optional preference recording exists; optional analytics and general email-notification delivery were not established. Deletion exists without selective preservation of coauthored records. |
| Proposed | Requirements Engineering rules and service, backup and availability targets remain proposals where not separately adopted and demonstrated. |
| Deployment-dependent | Provider locations and contracts, TLS configuration, live malware scanning, backup schedules, cleanup and recovery reconciliation. |
| Requiring institutional approval | Lawful bases, publication authority, account-model reconciliation, upload rules, retention, shared-record preservation, privacy contacts and operational responsibilities. The system name is team-confirmed. |
