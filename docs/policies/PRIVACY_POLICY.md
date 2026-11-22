# UMak-RMLIS Privacy Policy

**Final academic submission — proposed policy pending institutional adoption.**

Prepared: 21 November 2026. Effective date: **[pending institutional approval]**.

This policy describes the proposed handling of personal information in the **University of Makati Research Management and Lifecycle Information System (UMak-RMLIS)**, also referred to as the CRM application. It covers student researchers, faculty researchers, review committee members, and CRM administrative staff. The proposed operator and Personal Information Controller (PIC) is the **University of Makati (UMak)** through its Center for Research Management (CRM); **[the authorized legal entity, responsible office, and approval authority must be confirmed]**. The current repository is a synthetic-data teaching implementation, not an operational university service.

---

## 1. Information Used and Its Purposes

Pursuant to the principles of **Transparency, Legitimate Purpose, and Proportionality** under Republic Act No. 10173 (Data Privacy Act of 2012), UMak-RMLIS is designed to handle only information necessary for academic research administration:

| Information Category | Specific Data Elements | System / Administrative Purpose |
| --- | --- | --- |
| **Account & Authentication** | Institutional email address (`@umak.edu.ph`), display name, college affiliation, account identifier, system role (student researcher, faculty researcher, reviewer, staff), password hashes, email verification records, and session tokens. | Establish and authenticate accounts, enforce role-based access control (RBAC), verify academic eligibility, and secure active sessions. |
| **Research Registry & Metadata** | Research titles, abstracts, keywords, academic year, department/college, author ownership, coauthor team memberships, research adviser, and assigned reviewer panelists. | Maintain the institutional research registry, track research lifecycle progress, coordinate peer evaluations, and support academic reporting. |
| **Submitted Manuscripts & Documents** | Uploaded research proposals, draft manuscripts, revision documents, and document metadata (filename, file size, media type, SHA-256 digest, upload timestamp). | Receive, store, and manage research materials for authorized evaluation, format checking, and archival preservation. |
| **Evaluation Feedback & Internal Notes** | Reviewer scores, structured criteria feedback, researcher-visible review comments, and separate internal CRM administrative notes. | Communicate evaluation outcomes to research teams while maintaining restricted administrative deliberation records. |
| **Consultation & Service Queue** | Service category (e.g., statistical consultation, ethics coordination, formatting), optional requested deadline, status, assigned consultant, and scheduling timestamps. | Process service requests, organize the staff consultation queue, track delivery milestones, and coordinate advising sessions. |
| **Privacy Preferences & Consent Records** | Consent choices for optional processing (e.g., optional email notifications, institutional research analytics), policy version acknowledged (`2026-10-v1`), and server-recorded timestamps. | Record user preferences and maintain an auditable consent history pursuant to NPC accountability standards. |
| **Deletion Requests & Review Records** | Formal account deletion requests, review status (`confirm_review`), administrative determinations, and deletion audit ledger entries. | Process, evaluate, and record eligible account deletion requests subject to academic holds and archival retention rules. |
| **Technical Security & Audit Metadata** | Actor identifiers, target entity identifiers, action performed, request identifiers, client IP addresses, and event timestamps. | Protect system integrity, investigate security incidents, enforce rate limits, and maintain accountability under RA 10175. |

### Data Minimization and Research Ethics
Users must submit only information needed for legitimate research administration. Uploaded manuscripts and service requests must exclude unnecessary personal information, sensitive personal data, or privileged information regarding human research participants. Authors must obtain institutional research ethics clearance from the **UMak Research Ethics Committee (UMak-REC)** and secure prior informed consent before submitting data derived from research participants.

### No Commercial Tracking or Behavioral Advertising
The verified implementation does not include commercial advertising networks, third-party behavioral analytics, or marketing delivery mechanisms. Recording an optional preference does not mean that an external analytics or notification service is active.

---

## 2. Lawful Processing, Account Access, and Optional Choices

### Account Access and Self-Registration
Account access is structured around specific university roles:
* **Researcher Self-Registration:** Student and faculty researchers self-register using their active institutional email address (`@umak.edu.ph`). Identity ownership is verified through a one-time verification code sent to that address.
* **Staff and Administrator Accounts:** Staff and administrative accounts cannot be self-registered. They are provisioned and assigned specific granular permissions (`content_management`, `request_processing`, `registry_management`, `progress_updates`, `reporting`, `account_administration`) exclusively by authorized CRM account administrators.
* **Public Browsing:** Approved catalogue information (research title, abstract, keywords, academic year, department, authors) is accessible for public browsing and search without requiring an account or signing in.

### Lawful Bases for Processing
Before real use, the institution must review and approve the specific lawful basis for each processing purpose under **Sections 12 and 13 of Republic Act No. 10173**:
1. **General Personal Information (Section 12):** Processing for account management, research registry administration, and evaluation is grounded on compliance with legal obligations under the Revised UMak Charter (Makati City Ordinance 2020-059) and the Education Act of 1982 (B.P. Blg. 232), contractual/academic relationship fulfillment, and legitimate educational interests.
2. **Sensitive Personal Information (Section 13):** Academic evaluations and disciplinary records containing sensitive personal data require strict adherence to Section 13 criteria (such as explicit consent, specific statutory authority, or necessity for the protection of lawful rights). **[Approved purpose-by-purpose lawful bases are pending institutional review.]**

### Optional Choices and Withdrawal of Consent
Where processing relies on consent (such as optional research digest emails or institutional research analytics), the choice must be specific, voluntary, and informed. Core research administration and consultation services are never conditioned upon consent to optional features. Users may decline or withdraw consent at any time through their account settings without affecting their academic standing. Submitting preferences is not acceptance of the Terms and Conditions.

---

## 3. Public Information vs. Restricted Academic Records

* **Approved Public Catalogue:** Only approved catalogue information is intended for public search: research title, abstract, keywords, academic year, authors, and college/department. Authors must ensure that submitted abstracts contain no confidential, proprietary, or participant-identifying personal data. **[The publication approval procedure and authority must be confirmed.]**
* **Restricted Manuscripts and Drafts:** Unpublished manuscripts, proposals, and working drafts are restricted to authorized research team members, assigned advisers, designated reviewers, and authorized CRM staff. Staff status within UMak does not grant universal access to private studies.
* **Confidentiality of School Records:** In compliance with B.P. Blg. 232 (Section 9, Item 4), student academic records and evaluative submissions are maintained with strict confidentiality.
* **Segregation of Administrative Notes:** Internal administrative notes and reviewer coordination remarks are segregated from researcher-facing evaluation comments and are accessible only to authorized staff.
* **Temporary Document Access:** Manuscript downloads, where enabled, utilize short-lived, signed access URLs. Recipients must not share or publish these temporary links.

---

## 4. Recipients, Sharing, and External Transfers

Personal data may be handled by authorized research team members, assigned advisers, designated reviewers, CRM administrative staff, and authorized systems personnel:
* **Institutional Offices:** Coordination with the Office of the University Registrar (OUR) or Center for Integrative Communications (CIC) occurs only for authorized academic and graduation verification.
* **Regulatory Disclosures:** Disclosures to government agencies (e.g., CHED, DOST) or law enforcement must be legally mandated, assessed for necessity, and restricted to what the law requires.
* **Third-Party Providers & Data Sharing:** Contracted infrastructure or storage providers operate under written agreements enforcing confidentiality and security safeguards. External inter-agency transfers require formal Data Sharing Agreements (DSA) pursuant to NPC regulations.
* **Cross-Border Transfers:** The prototype code supports private object storage, but no specific cloud provider or overseas transfer has been approved. Any foreign hosting or transfer requires prior privacy impact assessment, institutional review, and binding safeguards. **[Actual providers, storage locations, and safeguards must be confirmed before deployment.]**

---

## 5. File Upload Thresholds and Technical Capacities

To balance service availability with storage governance, UMak-RMLIS applies the following technical constraints:
* **Service Request Attachments (Intake Forms):** Up to **10 MB per file**, with a maximum of **five (5) files per service request** (PDF or DOCX), as specified in Requirements Engineering and implemented in client request forms.
* **Manuscript Document Repository Uploads:** Up to **25,000,000 bytes (~25 MB) per document file** (PDF or DOCX), as enforced by backend document storage validation.
* **Reconciliation Note:** **[Institutional confirmation is required to harmonize and establish unified storage limits across all intake channels.]**
* **Inspection:** Files undergo format verification, MIME-type checks, and security scanning. Corrupted files, password-protected archives, and disguised executables are rejected.

---

## 6. Retention, Backups, and Account Deletion

Information should be retained only for approved purposes and applicable legal, academic, or records-management requirements under the **National Archives of the Philippines Act of 2007 (RA 9470)**:
* **Retention Schedules:** **[Retention periods and their starting events for accounts, research registry records, evaluation files, logs, and document versions remain subject to institutional approval.]**
* **Reviewed Account Deletion:** Student and faculty researchers may submit an account deletion request for staff review. Deletion review must verify that no active academic holds, integrity proceedings, or coauthor rights are compromised.
* **Deletion Scope and Backups:** Approved deletion removes live database records and queues document removal. Requesting deletion does not promise instantaneous erasure from historical disaster-recovery backups or older object versions. Restored backups must be reconciled against deletion audit records before service resumes. Per-person cryptographic shredding is not implemented.
* **Application Removal:** Uninstalling a mobile client or discontinuing web access does not delete records already held by the institution.

---

## 7. Rights of the Data Subject and Contact Channels

Under Section 16 of Republic Act No. 10173, registered users and data subjects are entitled to:
1. **Right to be Informed:** Understand how personal data is collected, processed, and safeguarded.
2. **Right to Reasonable Access:** Request reasonable access to personal data held in the system.
3. **Right to Rectification:** Dispute inaccuracies or errors and request prompt correction.
4. **Right to Erasure or Blocking:** Request suspension, withdrawal, or removal of personal data when outdated, false, unlawfully obtained, or no longer necessary, subject to university archival obligations under RA 9470.
5. **Right to Object:** Object to processing based on consent or optional research analytics.
6. **Right to Data Portability:** Request an electronic copy of submitted research metadata in a structured format.
7. **Right to File a Complaint:** File a complaint with the institution or the National Privacy Commission (NPC).

### Institutional Privacy Contacts
Proposed research-office contact supplied for coursework: **research@umak.edu.ph**. **[Confirm that this mailbox accepts privacy requests; insert the authorized DPO contact, postal address, and secure request channel before adoption.]**

* **University Data Protection Office:** Center for Data Protection and Records Management Services (CDPRMS), University of Makati, J.P. Rizal Ext., West Rembo, Makati City. Email: **dprms@umak.edu.ph**.
* **National Privacy Commission:** Official portal: [https://privacy.gov.ph/](https://privacy.gov.ph/).

Do not transmit passwords, government identity cards, or sensitive manuscripts through unconfirmed email channels.

---

## 8. Security Safeguards, Mobile Use, and Breach Notification

### Security Safeguards
The application is designed to restrict access through RBAC, hash credentials, protect sessions, validate document payloads, and record security audit events. The repository includes code for local database transactions and object storage adapters. However, production infrastructure controls (such as live cloud configurations, hardware firewalls, and enterprise disaster recovery) require operational implementation and verification. No multi-factor authentication (2FA) is currently implemented; authentication relies on institutional email verification and password credentials.

### Mobile Offline Library
The mobile library supports local caching for approved public catalogue information and protected session storage. It is an educational library, not a fully released standalone app. Private manuscripts and internal notes are excluded from offline caches. Users remain responsible for securing their personal mobile devices.

### Mandatory Breach Notification (NPC and Data Subjects)
In accordance with **Section 20(f) of Republic Act No. 10173**, **NPC Circular No. 16-03**, and **NPC Circular No. 2024-01**:
1. When sensitive personal information or information enabling identity fraud is reasonably believed to have been acquired by an unauthorized person, and the PIC or Commission believes such acquisition is likely to give rise to a real risk of serious harm, the institution shall notify **both the National Privacy Commission and affected data subjects within seventy-two (72) hours** of knowledge or reasonable belief of the breach.
2. Notification to affected data subjects may be postponed or exempted only if authorized by the NPC where notification would hinder a criminal investigation or is determined contrary to public interest.
3. The institution must maintain an incident response procedure to contain breaches, assess impact, and mitigate harm.

---

## 9. Policy Amendments and Institutional Adoption

**[Account eligibility, procedural rules for minors, and terms acceptance workflows must be approved by the university.]** The institution must publish an understandable notice with its approved version and effective date prior to operational adoption. The preparation date in this document reflects coursework completion and is not an institutional effective date.

---

## Reviewer and Technical Appendix

- **System Baseline:** Pinned repository revision `1a305391339b8258ab2f05737717aa0038fd3d97`. See [technical privacy handoff](../implementation/PRIVACY.md) and [system implementation guide](../implementation/IAS_IMPLEMENTATION.md).
- **Implementation Status:** Roles, password hashing, email verification, separate notes, consent recording, reviewed deletion transaction, document validation, and audit logging are implemented in source code. Cloud hosting, operational retention schedules, live DPO integration, and commercial services are pending institutional deployment.
- **Legal References:**
  1. Republic Act No. 10173 (Data Privacy Act of 2012) and its 2016 IRR (Rules IV–VI, VIII–IX).
  2. NPC Circular No. 16-03 and NPC Circular No. 2024-01 (Personal Data Breach Management and Breach Investigations).
  3. NPC Circular No. 2023-06 (Security of Personal Data in Government and Private Sector).
  4. Republic Act No. 9470 (National Archives of the Philippines Act of 2007).
  5. Batas Pambansa Blg. 232 (Education Act of 1982), Section 9(4).
  6. Republic Act No. 10175 (Cybercrime Prevention Act of 2012), Section 4(a).
  7. Makati City Ordinance 2020-059 (Revised UMak Charter).
  8. University of Makati, *Data Privacy Policy Statement*, CDPRMS official publication.

Related Document: [Terms and Conditions](TERMS_AND_CONDITIONS.md).
