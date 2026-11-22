# University of Makati Research Management and Lifecycle Information System (UMak-RMLIS) Terms and Conditions

**University of Makati | Center for Research Management**  
**Prepared for academic evaluation: 22 November 2026**  
**Effective date: Pending institutional approval**

**Final academic submission — proposed policy pending institutional adoption.**

## 1. Purpose, scope and status

These proposed Terms describe appropriate use of UMak-RMLIS, an academic research administration platform developed for the University of Makati's Center for Research Management (CRM). They cover public visitors, student and faculty researchers, authorized reviewers and staff using the Android application and its backend services. The University of Makati is referred to below as UMak.

The project team has confirmed the system name. Neither that confirmation nor this academic document establishes institutional adoption, a binding university agreement, or authorization for live institutional operation. The repository includes a synthetic academic demonstration and a documented hosted demo. Demonstration accounts, content and infrastructure must not be treated as an approved university service.

The obligations below are proposed conditions for an adopted service. Applicable law and approved UMak policies remain authoritative. Before release, the institution must determine the operating authority, eligibility, administrative responsibilities, applicable acceptance process and effective version of these Terms. No official signatory or approval body is appointed by this document.

Read these Terms with the [UMak-RMLIS Privacy Policy](PRIVACY_POLICY_FINAL_DRAFT.md). That document explains personal-data processing and supplements UMak's official **Data Privacy Policy Statement** and relevant institutional notices. These Terms do not replace those instruments or establish consent to unrelated processing.

## 2. Accounts, eligibility and verification

The intended researcher model permits student and faculty self-registration using eligible institutional email addresses. Registration collects email, name, college, researcher category and password. The backend restricts registration to student or faculty categories and checks the configured institutional-domain allowlist and any additional configured address restrictions. It does not automatically accept every address associated with UMak.

Normal sign-in and protected sessions require an active account and completed email verification. The implemented verification method uses a single-use six-digit code expiring after 15 minutes. External email delivery depends on a configured SMTP service. The hosted demonstration instead uses designated synthetic addresses and a protected demo inbox without external email delivery; that inbox is not an approved production verification channel.

Authorized account administrators create staff accounts and assign permissions. Public registration cannot create staff accounts or confer administrative privileges. Staff permissions separately cover content management, request processing, registry management, progress updates, reporting and account administration. Holding a staff account does not grant unrestricted access to every document.

The reviewed Requirements Engineering baseline instead proposes staff-provisioned accounts and excludes public researcher self-registration. The team's intended model and implemented registration route differ from that baseline. This discrepancy requires institutional reconciliation before deployment; no formal amendment of the requirements is asserted. Approved production domains, eligibility and staff assignments also remain to be confirmed.

## 3. Authentication and account responsibilities

Use only an account you are authorized to operate. Provide accurate information, protect passwords, verification codes, temporary credentials and session material, and avoid sharing credentials. Sign out on shared devices and report suspected compromise promptly through the contacts in section 14.

The implemented researcher password rule requires at least 12 characters. Staff-created temporary credentials and administrator-initiated recovery require password replacement before normal protected access. Sessions are validated by the server and can expire or be revoked; a saved device session does not override account deactivation or permissions. Multi-factor authentication was not established in the reviewed implementation.

Researchers can update their profile name and college through implemented controls. Email changes require administrative assistance. An entered adviser name does not provision an adviser account or grant record access. Institutional email verification confirms access to the configured email address; it is not certification of every academic claim made by its holder.

## 4. Authorized use and prohibited conduct

Use UMak-RMLIS for authorized research, service and administrative activities. Users should:

- Provide relevant, accurate account, research and request information and correct known errors through permitted channels.
- Respect research-team membership, review assignments, confidentiality and permission boundaries.
- Submit only documents they are authorized to provide, with necessary permissions for third-party material and personal data.
- Follow applicable research ethics requirements and approved UMak procedures; omit unnecessary participant identifiers and confidential information.
- Protect downloaded documents and reports and share them only with authorized recipients.

Do not impersonate another person, obtain access beyond authorization, intercept private communications, disclose restricted records, alter records or audit information without authority, introduce malicious files, bypass validation, or disrupt the service. Security testing requires authorization from the responsible operator; this draft does not itself authorize testing against university or provider systems.

Suspected misuse may require review under applicable law and approved university procedures. These Terms create no automatic finding of misconduct, financial penalty, academic hold, disciplinary sanction or mandatory account termination. Similarity results and technical validation are not conclusive findings of plagiarism, copyright infringement or research misconduct.

## 5. Research registration and research lifecycle

Researchers can register studies, provide research metadata and declarations, identify eligible existing researcher accounts as team members, and view records they are permitted to access. Authorized staff can maintain registry entries, encode historical records with source information, correct records with a reason and retain change history. The reviewed registration route requires an adviser name for student research and permits it to be blank for faculty research; the institutional rule remains subject to confirmation.

Research lifecycle tracking distinguishes registration, recorded stages, milestones, evidence references, next actions and outcomes. Authorized progress staff can record updates and corrections within the implemented controls. Researchers can view permitted research lifecycle information; they do not receive staff update permissions merely by registering a study.

**Completion, presentation and publication are separate outcomes.** Recording one does not automatically record another or require a fixed sequence among them. A milestone records a particular requirement or activity; it is not interchangeable with an overall research lifecycle stage or an outcome.

Registration, a stored stage, a milestone or an outcome does not independently establish academic approval, research ethics clearance, publication authority, copyright ownership, acceptance of research or institutional certification. Those matters remain governed by applicable law and approved UMak procedures. Registry entry is also separate from public-catalogue approval.

Duplicate-title checks and research lifecycle planning estimates support human review. They do not certify originality, replace a responsible office or committee, or guarantee completion dates. Actual milestone applicability and approval procedures require institutional confirmation.

## 6. Services, consultations and request processing

Public visitors can browse published service information, requirements, instructions and available blank forms. Review the stated eligibility, required and optional documents, current form version and submission channel before preparing a request. Browsing or downloading a blank form does not itself create a service request, account or research registration.

Authenticated researchers can submit requests for services configured for in-application submission, provide a subject and description, attach permitted files and link an accessible eligible study. A successful save generates a request identifier, submission time and status. Treat a request as stored only after server confirmation. Retry controls support duplicate prevention; a pending screen or selected file alone is not a receipt.

Researchers can view their permitted request history, files, statuses, feedback, missing requirements, next actions and deadlines when recorded. Authorized processing staff review requests, record checklists and checkpoints, return correction instructions and update status. Internal staff remarks are separate from researcher-visible feedback. Correction resubmissions retain the request identifier and earlier review history; earlier attachment versions can remain stored.

The implemented request statuses are Submitted, In review, For correction and Completed. A service-request Completed status records that workflow's processing result; it does not automatically complete the linked study or record presentation or publication. Authorized corrections to completed requests require a reason and history.

Consultation tickets separately record service type, optional deadline, queue status and staff assignment. Queue ordering assists staff organization; it is not a guaranteed appointment or response time. In-application updates and configured deadline reminders are distinct from external email delivery. No guaranteed processing time, appointment availability or automatic service approval is established here. Confirm the applicable service requirements with the responsible university office.

## 7. Document submissions and upload restrictions

The reviewed backend accepts supported **PDF (.pdf)** and **DOCX (.docx)** documents, subject to file-content and format validation. A filename extension alone does not make a file acceptable. Do not upload irrelevant documents, disguised executable content, corrupted files or material intended to bypass controls.

| Workflow | Current technical restriction |
| --- | --- |
| Service-request attachments | Maximum **10,000,000 bytes per file**. |
| Service-request submission or correction attempt | Up to **five files per submission attempt**. |
| Requirement slots | Maximum **five distinct requirement slots** across existing and incoming files. |
| Correction history | Earlier attachment versions may remain after resubmission; five is not a lifetime maximum across all retained files. |
| Staff registry documents and service blank forms | Maximum **10,000,000 bytes per file**. |
| Separate study-document/manuscript route | Maximum **25,000,000 bytes per file**, without increasing the service-request limit. |

The Requirements Engineering baseline proposes 10 MB per file for research documents and service attachments and five files per service request. The original Terms instead describe a blanket 25 MB submission limit. These rules remain unreconciled. The separate manuscript route is an implemented technical distinction, **not an institutionally approved exception**. The institution must reconcile requirements, retained-version treatment, interfaces, backend limits and governing documents before deployment.

Normal backend upload routes depend on the required configured services, including scanning for the relevant private-document workflows. The synthetic demonstration does not establish production antivirus scanning. Validation or scanning does not guarantee that every harmful file will be detected. Services can reject files or be unavailable when validation, configuration or capacity requirements are not met.

Request and registry documents can be stored in database-backed records. The separate manuscript service uses configured object storage and temporary signed download links. Access remains permission-controlled. Do not forward private download links or redistribute confidential documents to unauthorized recipients.

## 8. Public records, restricted information and reports

The reviewed public research projection includes approved records' **identifiers, titles, abstracts, approval status, research year and department**. It excludes full manuscripts, account emails, team lists, author names, keywords, internal notes and individual service requests. Public-catalogue search can also return a computed relevance score; this does not expose additional private record fields.

Public browsing grants no access to restricted records. Private access depends on authenticated roles, research membership, review assignment or the relevant staff permission. Registry and reporting permissions can cover records across studies for authorized administrative purposes; they do not rely solely on individual reviewer assignments. All employees do not automatically receive private access.

Authorized reporting staff can generate filtered research and service-utilization reports and use implemented Android export functions for PDF and Excel summaries. Reports and exported files must be protected according to their contents. A reporting permission does not authorize unrelated disclosure or publication of identifiable data.

Before real catalogue publication, the institution must confirm the publication basis and authority and review metadata for personal or confidential information. A recorded research lifecycle outcome does not itself authorize public release. Technical access controls reduce risk but do not guarantee absolute confidentiality or prevent every subsequent disclosure by an authorized recipient.

## 9. Authorship and intellectual property

Research authorship and intellectual property rights remain governed by **Republic Act No. 8293, the Intellectual Property Code of the Philippines, as amended**, applicable law, valid agreements and approved UMak policies. The applicable rules may depend on joint authorship, employment, commissioning or other circumstances; these Terms do not declare that every submitted work belongs solely to its submitting account holder.

An application's recorded owner or registrant is a technical identifier used for record relationships and access. It is not a determination of sole legal authorship or copyright ownership. Adding or removing a team account does not by itself assign intellectual property rights.

Submitting a document allows it to enter the relevant administrative workflow, subject to the necessary legal authority and permissions for that processing. Submission does not, under these proposed Terms, create a blanket assignment of copyright, waiver of moral rights, exclusive publication right or royalty-free institutional licence. Any licence or broader publication permission must rest on an applicable legal basis or valid, appropriately authorized agreement.

Users should respect other authors' rights and submit material only where its use is authorized. Public metadata availability and technical ability to download a file do not themselves authorize unrestricted reuse of the underlying work. The system does not independently adjudicate authorship disputes or certify that a submission complies with every intellectual property requirement.

## 10. Account administration, restrictions and deletion

Authorized account administrators can activate or deactivate accounts, manage permissions, correct researcher categories and initiate credential recovery. These technical functions do not establish an unlimited contractual power or a formal disciplinary decision. Grounds, responsibilities, review and communication for an adopted service must follow applicable law and approved institutional procedures.

Student and faculty accounts can request deletion through the implemented backend. Staff completion requires an explicit review confirmation; staff-account deletion requires separate administrative review. A backend capability does not establish a verified user-facing interface for every request or a fixed institutional response deadline.

**Deleting an account can delete studies for which it is the recorded owner, including coauthored studies, and dependent records.** Removing an account that is only a coauthor does not itself invoke that ownership cascade. The mechanism does not selectively preserve all shared studies. This behavior requires institutional and technical review before production adoption, including the rights of other researchers and lawful preservation obligations.

Deletion decisions must consider applicable privacy law, authorized records procedures, lawful retention and other affected people's rights, with an explanation of justified limitations. These Terms impose no automatic prohibition because coauthors exist, academic or financial clearance hold, or invented refusal rule. They do not promise automatic shared-record preservation.

Live-record deletion is distinct from erasure of backups, object-storage versions, retained audit and deletion metadata, local drafts or downloaded files. Source tools support cleanup and deletion-event replay during recovery, but schedules and reliable operation require deployment verification and reconciliation. No permanent or immediate erasure of every copy is promised. Retention must follow justified purposes and applicable records law; no system-specific retention schedule was verified.

## 11. Personal data, consent and incidents

The [UMak-RMLIS Privacy Policy](PRIVACY_POLICY_FINAL_DRAFT.md) provides the detailed notice on information collected, purposes, lawful bases, recipients, retention, safeguards and rights. UMak must establish each processing purpose's lawful basis under the Data Privacy Act and its IRR, with a separately applicable exception for sensitive or privileged information. An academic relationship, system registration or acceptance of Terms is not blanket permission for every use.

The backend records separate optional institutional-analytics and email-notification choices, including choices to decline both. Preference records do not establish operation of optional analytics or general email delivery. Existing administrative reporting needs its own lawful basis. Withdrawal, objection, access, correction, erasure or blocking, portability and other remedies apply under the conditions explained in the Privacy Policy and Philippine law. A restriction on internal notes is not by itself a legal answer to a data-subject access request.

These Terms expressly recognize UMak's official **Data Privacy Policy Statement**, *Data Privacy Notice for Data Subjects*, and applicable student and employee notices. The application policy supplements those instruments; it does not supersede approved university requirements. Current versions, office designations and system-specific routing require confirmation.

Report suspected compromise, loss or unauthorized disclosure promptly to the privacy contact. For an adopted service, responsible institutional personnel must assess and manage incidents. Under **section 20(f) of RA No. 10173 and NPC Circular No. 16-03**, qualifying breaches require notification to **both the NPC and affected data subjects within the applicable 72-hour period**, subject to lawful exceptions and procedures. Not every incident meets mandatory notification criteria. A postponement request does not automatically suspend obligations or constitute NPC approval. The Privacy Policy explains the criteria and NPC Advisory No. 2026-02. This draft appoints no incident-response team.

## 12. Security, availability and device storage

The reviewed source includes salted adaptive password hashing, signed sessions with server validation and revocation, database and application access controls, audit histories, validation and throttling. Source configurations support HTTPS, protected database connections, scanning, private storage and encrypted recovery tools. Their effectiveness depends on configuration, maintenance and authorized operation. Source code and local tests do not establish enterprise certification or approved production safeguards.

The Android application uses Keystore-backed encryption for stored session material and maintains public content for offline access. It also saves account-scoped registration drafts in private application preferences, with a local deletion control. Those drafts are distinct from encrypted session storage; application-level draft encryption is not established. Signing out should not be assumed to erase all drafts, reports or downloads. Exported or downloaded files may remain in device storage or destinations chosen by the user.

Protect devices and retained files, especially on shared devices, and remove unnecessary local drafts and copies. Private submissions require connectivity; cached information may be outdated. A local draft is not necessarily a server submission.

No continuous availability, fixed processing or recovery time, particular backup schedule, complete malware protection or absolute security is promised. Maintenance, network failures and configuration problems can interrupt access. Future service levels and recovery arrangements require institutional approval and operational evidence. These limitations do not exclude mandatory statutory duties or data-subject remedies.

## 13. External services and submission channels

A service may direct applicants to an external form or submission channel. Opening or completing that channel does not automatically create an in-application request, receipt or research record. Follow the channel's applicable instructions and privacy notice; confirm acceptance with the responsible office.

The source supports SMTP, PostgreSQL and S3-compatible storage and backup services. Deployment documents describe Render and Supabase for the synthetic demonstration. They do not establish approved production providers, contracts, processing locations, cross-border arrangements or subcontractors. Such arrangements must be assessed and disclosed before institutional use; neither Philippines-only hosting nor unrestricted international transfer is assumed.

Controller-to-controller data sharing must have a lawful basis and applicable safeguards. NPC Circular No. 2020-03 and Advisory No. 2025-01 distinguish lawful sharing from the optional execution of a data-sharing agreement; an agreement alone does not authorize processing. Processor outsourcing is a separate arrangement requiring appropriate instructions, safeguards and accountability. Users should not treat an external link or technical connection as authorization to disclose restricted records.

## 14. Institutional procedures and contacts

Academic review, ethics clearance, service eligibility, publication, record preservation and formal administrative action remain matters for authorized university functions under applicable law and approved procedures. UMak-RMLIS records and supports those activities but does not replace their authority. No particular committee jurisdiction, compulsory sanction or institutional service guarantee is created here.

**Privacy inquiries:** **[dprms@umak.edu.ph](mailto:dprms@umak.edu.ph)** is published in the supplied official university privacy material. That material names the Center for Data Protection and Records Management. The official [office page](https://www.umak.edu.ph/centers/cdprm/) uses the heading *Center for Archives and Information Governance* while retaining data-protection descriptions. The current office designation, DPO contact and application-specific route remain for confirmation. These Terms appoint no named DPO.

**Research-service inquiries:** **[research@umak.edu.ph](mailto:research@umak.edu.ph)** is supported by an [official UMak research announcement](https://www.umak.edu.ph/call-for-paper-6th-research-congress-umak/). Its publication does not verify a current dedicated privacy-request or system-support channel. Avoid sending sensitive documents until the appropriate secure route is confirmed.

## 15. Amendments and institutional adoption

These proposed Terms have no institutional effective date until adoption. Future adoption and material amendments require authorized institutional review, an identified version and effective date, and appropriate communication to affected users. Student developers do not acquire authority to impose university policy through this document.

An approved acceptance process must distinguish agreement to service conditions from consent to particular personal-data purposes. Continued use must not automatically be treated as consent to unrelated new processing. No unsupported notice period is prescribed.

Before adoption, the institution must resolve the account-model and upload discrepancies; approve eligibility and permissions; confirm publication authority, provider arrangements, lawful bases and retention schedules; address deletion effects on coauthored studies; verify security and recovery arrangements; and establish responsible contacts and procedures. The confirmed system name is not an unresolved decision.

The [original Terms](TERMS_AND_CONDITIONS.md), original Privacy Policy and finalized Privacy Policy remain preserved. This draft replaces their unsupported assertions only within its proposed text; it does not amend those documents or establish precedence over adopted university rules.

## 16. Philippine law and available remedies

Applicable Philippine law provides the legal framework. Academic and service concerns may be raised through approved UMak procedures, whose details must be confirmed for an adopted service. Personal-data concerns may be pursued through the rights and remedies under RA No. 10173 and applicable [NPC procedures](https://privacy.gov.ph/).

These Terms impose no exclusive court venue, mandatory arbitration, waiver of statutory remedies or additional exhaustion requirement. They do not exclude mandatory protections or determine criminal, civil or disciplinary liability merely from a technical record or an alleged violation.

## 17. Legal and institutional references

References apply within their actual scope; they do not authorize every processing activity or establish adoption of these Terms.

1. **RA No. 10173, Data Privacy Act of 2012**, particularly sections 11–14, 16–18, 20 and 21, and its **IRR**: [official NPC Act](https://privacy.gov.ph/data-privacy-act/) and [amended IRR reference](https://privacy.gov.ph/wp-content/uploads/2023/06/IRR_RA-10173-as-amended.pdf). These address processing, rights, security and accountability.
2. **NPC Circular No. 16-03**, *Personal Data Breach Management*; **2023-06**, *Security of Personal Data in the Government and the Private Sector*; **2020-03**, *Data Sharing Agreements*; **2023-04**, *Guidelines on Consent*; **2023-07**, *Guidelines on Legitimate Interest*; and **2022-04**, concerning processing-system registration, automated decision-making/profiling notifications and DPO designation: [official NPC register](https://privacy.gov.ph/pips-and-pics/advisories-circulars/). Circulars 2023-06 and 2020-03 replaced the earlier 16-01 security and 16-02 sharing baselines. Circular 2024-01 concerns procedural amendments, not the primary breach-management rules.
3. **NPC Advisory No. 2025-01**, [data-sharing clarification](https://privacy.gov.ph/wp-content/uploads/2025/07/SGD-2025-01-DSA-Clarification.pdf), and **NPC Advisory No. 2026-02**, [breach-notification submissions](https://privacy.gov.ph/wp-content/uploads/2026/05/Advisory-Clarification-on-the-Submission-of-Personal-Data-Breach-Notification-through-DBNMS_11-May-2026.pdf).
4. **RA No. 8293, Intellectual Property Code of the Philippines, as amended**: [IPOPHL legislation and implementing rules](https://www.ipophil.gov.ph/intellectual-property-code-implementing-rules-and-regulations/). Applicable authorship, ownership, economic and moral rights depend on the law and valid agreements; no blanket assignment is imposed here.
5. **RA No. 10175, Cybercrime Prevention Act of 2012**, relevant to unlawful access and interference: [Senate legislative reference](https://ldr.senate.gov.ph/legislative%2Bissuances/Republic%20Act%20No.%2010175). Prohibited conduct under these proposed Terms is not automatically a determination that statutory offence elements are satisfied.
6. **RA No. 9470, National Archives of the Philippines Act of 2007**, relevant to public-record preservation and authorized disposition: [Supreme Court text](https://elibrary.judiciary.gov.ph/thebookshelf/showdocs/2/19575). It does not establish a universal retention period for every application record.
7. **Batas Pambansa Blg. 232, Education Act of 1982**, particularly section 9(4) on access to one's school records and confidentiality, subject to subsequent applicable legislation: [Senate text](https://ldr.senate.gov.ph/legislative%2Bissuances/Batas%20Pambansa%20232).
8. **Makati City Ordinance No. 2020-059, Revised UMak Charter**: identified in the supplied official UMak privacy material and the [university alumni registration notice](https://umak.edu.ph/alumni/register). An authenticated complete ordinance and amendments were not obtained; precise operative provisions and system-specific authority remain for institutional legal verification. The reference is not blanket processing or publication authority.
9. **UMak Data Privacy Policy Statement**, *Data Privacy Notice for Data Subjects*, relevant employee notices and consent forms in the supplied capture of the [official Privacy Policy page](https://www.umak.edu.ph/privacy-policy/), and the [official student notice](https://umak.edu.ph/olrog/assets/pdf/DATA%20PRIVACY%20NOTICE%20FOR%20STUDENTS.pdf). Direct access to some university materials remains restricted; indexed official content and the supplied capture support the references, without establishing the latest approved version. The [finalized application Privacy Policy](PRIVACY_POLICY_FINAL_DRAFT.md) records the source and contact limitations in detail.

## 18. Academic implementation review basis

Reviewed on **22 November 2026** against Git HEAD **`b2d9388557696876c369986418ecf4b9365587bd`** and the available working tree. Pre-existing changes included pagination edits in [registration](../../src/research/registrations.js) and [progress](../../src/research/progress.js), changes in `scripts/android-synthetic-acceptance.js`, and untracked planning, benchmark, output, policy and pagination-test files. They were preserved. Only this new Terms draft was created for this task; neither Privacy Policy was changed. The baseline is not a clean immutable checkout or production certification.

Primary evidence comprised [application routes](../../src/app.js), [authentication and account administration](../../src/auth), [schema and migrations](../../db), [research registry](../../src/research/registry.js), [service catalogue](../../src/services/catalog.js), [service requests](../../src/requests/service-requests.js), [consultations](../../src/consultations/routes.js), [document routes](../../src/storage/routes.js), [reporting](../../src/reporting/reports.js), [Android flows](../../android/app/src/main/java/ph/edu/umak/crm/app), [device storage](../../android/core/src/main/java/ph/edu/umak/crm), [hosted-demo documentation](../HOSTED_DEMO.md), [deployment configuration](../SUPABASE_RENDER_DEPLOYMENT_PLAN.md) and [privacy/recovery handoff](../implementation/PRIVACY.md). Source takes priority over stale implementation descriptions.

Requirements were compared with the locally supplied **REQUIREMENTS_ENGINEERING_v3.14.10.2026.pdf**, including researcher and staff modules, authorization and backup requirements, and proposed rules 2–15. Besides account and upload differences, proposed research-stage labels differ from stored stage identifiers. Institutional confirmation must settle intended labels and workflows. Milestones, completion, presentation and publication remain distinct; the requirements do not prove an adopted service level or successful production operation.

| Evidence classification | Application to these Terms |
| --- | --- |
| Implemented and locally verified | Existing backend tests and synthetic workflow checks exercise registration verification, permissions, public/private boundaries, requests and corrections, registry documents, research lifecycle records, reporting, consent, deletion and security controls within their test scope. |
| Implemented in source, not operationally verified here | Android exports, session encryption, draft persistence and public cache; configured email, storage and recovery integrations. No new device or production-infrastructure certification is claimed. |
| Partially implemented | Optional preferences exist without established optional analytics or general email delivery; deletion does not selectively preserve shared studies. |
| Proposed | Institutional service procedures, requirements targets and these Terms pending adoption. |
| Deployment-dependent | Providers, locations, verified TLS configuration, live scanning, backup schedules, cleanup and restore reconciliation. |
| Requiring institutional approval | Account/requirements reconciliation, upload policy, eligibility, permissions, publication, lawful bases, retention, coauthor preservation and responsible offices. |

Verification used `node scripts/synthetic-acceptance.js --full-tests` and `node scripts/synthetic-acceptance.js --smoke` against separate disposable local databases. The backend suite passed **66 tests**, with no failures or skipped tests. The synthetic signup, catalogue, research registration, service-request correction, research lifecycle planning, account administration, registry-document and reporting checks also passed, together with their eight selected tests. These results verify local synthetic behavior only. Android instrumentation and production infrastructure were not rerun. No tests or source files were changed.

Official legal sources and the NPC register were checked during this review. Access restrictions on some individual documents and university pages were retained as evidence limitations, rather than treated as proof of current institutional approval.
