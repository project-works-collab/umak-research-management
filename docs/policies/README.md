# CRM Policies — Institutional Documentation & Validation Record

**Final academic submission — proposed policy pending institutional adoption.**

- [Privacy Policy](PRIVACY_POLICY.md)
- [Terms and Conditions](TERMS_AND_CONDITIONS.md)
- [Preserved Previous Privacy Policy (Comparison Baseline)](PRIVACY_POLICY_PREVIOUS_VERSION.md)
- [Preserved Previous Terms and Conditions (Comparison Baseline)](TERMS_AND_CONDITIONS_PREVIOUS_VERSION.md)
- [Generator source archive](generator-original/README.md) — unadapted evidence, not approved policies
- [Technical privacy handoff](../implementation/PRIVACY.md)

Updated: 22 November 2026. This suite contains the revised Privacy Policy and Terms and Conditions for the **University of Makati Research Management and Lifecycle Information System (UMak-RMLIS)**, aligned with the official *University of Makati Data Privacy Policy Statement*, Philippine statutory laws, and the verified repository implementation.

---

## Change Summary (Addressing Validation Findings)

1. **System Name Consistency:** Unified the system name to **Research Management and Lifecycle Information System (UMak-RMLIS)** across all policy documents, removing conflicting references to "Library Information System."
2. **Account Access & Role Model:** Documented that student and faculty researchers self-register using their institutional email (`@umak.edu.ph`) and verify ownership via email verification codes. Staff accounts are created and granted granular permissions exclusively by authorized CRM account administrators. Public browsing remains accessible without authentication.
3. **Clear Academic Adoption Status:** Prominently marked both documents with `"Final academic submission — proposed policy pending institutional adoption"` and marked effective dates and operator appointments as pending institutional confirmation.
4. **Reconciled Upload Limits:** Clarified the distinction between **service request attachments** (up to 10 MB per file, max 5 files per request, matching Requirements Engineering and the client UI) and **manuscript repository uploads** (up to 25,000,000 bytes per document file, supported by backend storage). Flagged institutional harmonization as an open adoption item.
5. **Corrected Legal Provisions & Breach Reporting:** Updated legal citations to include **Section 20(f) of RA 10173**, **NPC Circular No. 16-03**, **NPC Circular No. 2024-01** (Breach Investigations), and **NPC Circular No. 2023-06** (Security of Personal Data). Explicitly specified the mandatory 72-hour notification rule to **both** the NPC and affected data subjects (subject to statutory exemptions/postponements). Clarified lawful processing criteria under Sections 12 and 13.
6. **Removed Unsupported Operational Claims:** Removed unverified promises regarding fixed retention durations, instant backup purging, 2FA/MFA, active analytics SDKs, and production DRaaS/cloud infrastructure, clearly separating implemented source features from deployment-dependent controls.
7. **Institutional Restraint:** Removed invented student disciplinary tribunal bodies, specific criminal/civil penalty terms, automatic deletion refusal rules, and exclusive court venue clauses. Referred instead to approved university codes (Student Handbook, Faculty Code, CSC rules) and applicable Philippine procedural law.
8. **Updated Technical Appendix & Links:** Linked all policies to pinned revision `1a305391339b8258ab2f05737717aa0038fd3d97` and verified all internal documentation links. Preserved comparison baselines (`PRIVACY_POLICY_PREVIOUS_VERSION.md` and `TERMS_AND_CONDITIONS_PREVIOUS_VERSION.md`).

---

## Unresolved Items Requiring Institutional Confirmation

Before operational adoption, the following decisions must be formally resolved by the responsible institutional functions:

| Item | Open Decision / Unresolved Item | Responsible Function | Required Institutional Action |
| --- | --- | --- | --- |
| **U1 Operator & Governance** | Official legal operator entity, responsible university office, and approval authority (Board of Regents / VPRE). | Administration & Legal | Formally designate the CRM under the Office of the Vice President for Research and Extensions and adopt the policy suite. |
| **U2 Minors & Eligibility** | Procedures and consent mechanisms for student researchers under the age of majority. | Academic Affairs & Legal | Define whether parental/guardian consent is required or if school enrollment and adviser supervision satisfy institutional requirements. |
| **U3 Privacy Contacts** | Official privacy routing and request verification for `research@umak.edu.ph` and `dprms@umak.edu.ph`. | CDPRMS (DPO) & CRM | Confirm the designated mailbox and procedure for receiving, verifying, and responding to data subject rights requests. |
| **U4 Lawful Bases per Purpose** | Documented lawful basis under RA 10173 Section 12 (personal data) and Section 13 (sensitive personal data). | DPO & Legal | Formally review and approve purpose-by-purpose lawful processing bases; confirm whether abstract publication is based on consent or academic charter fulfillment. |
| **U5 Upload Limit Harmonization** | Harmonization between 10 MB (service request attachments) and 25 MB (manuscript storage). | CRM, ITC, & Academic Council | Establish unified university policy on file upload sizes and storage quota allocations. |
| **U6 Retention Schedules** | Definitive retention periods and disposal triggers under RA 9470 (NAP Act). | Records Office & CDPRMS | Approve formal records disposition schedules for student theses, faculty papers, evaluation notes, and audit logs. |
| **U7 Hosting & Processor Agreements** | Production cloud hosting provider, storage regions, and Data Sharing / Processor Agreements. | ITC & Legal | Execute formal Data Sharing Agreements (DSA) and Cloud Service Provider SLAs ensuring local compliance. |
| **U8 Terms Acceptance Workflow** | In-app terms acknowledgment mechanism and version enforcement. | CRM & Development Team | Approve the UI/UX terms acceptance prompt and database version recording mechanism. |

---

## Feature-to-Clause Review

| Source at Pinned Revision (`1a305391339b8258ab2f05737717aa0038fd3d97`) | Policy Connection | Implementation Boundary |
| --- | --- | --- |
| [Application routes](../../src/app.js), [researcher registration](../../src/auth/researcher-registration.js), [account admin](../../src/auth/account-administration.js) | Self-registration with email verification, staff account provisioning with permissions, RBAC, public browsing | Verified in source; researchers self-register with 6-digit email code; staff accounts provisioned by admin; public search available without auth. |
| [Document routes](../../src/storage/routes.js), [storage validation](../../src/storage/validate.js) | PDF/DOCX manuscript storage, 25 MB limit, temporary signed access links | Verified in source; checks MIME type, parses structures, enforces 25,000,000 bytes. Service request intake in client UI uses 10 MB / 5 files. |
| [Implementation guide](../implementation/IAS_IMPLEMENTATION.md) and [technical privacy handoff](../implementation/PRIVACY.md) | Encryption, reviewed deletion transaction, deletion replay ledger, audit logs | Verified in source; deletion cascades live records; backup purging and object lifecycle rules require operational hosting verification. |
| [Algorithm contract](../api/ALGORITHM_API.md), [search and scheduling](../api/SEARCH_AND_CONSULTATIONS.md) | Similarity checking, structural readiness estimates, consultation queues | Verified in source; advisory decision-support tools; does not guarantee originality, completion date, or instant advising turnaround. |

---

## Statutory and Regulatory References

1. **Republic Act No. 10173 (Data Privacy Act of 2012):** Sections 11–14 (general principles, lawful processing of personal and sensitive personal data), Section 16 (rights of data subjects), Section 20(f) (72-hour breach notification), and 2016 IRR (Rules IV–VI, VIII–IX).
2. **National Privacy Commission (NPC) Issuances:**
   - NPC Circular No. 16-03 (Personal Data Breach Management);
   - NPC Circular No. 2024-01 (Breach Investigation Procedures);
   - NPC Circular No. 2023-06 (Security of Personal Data in Government and Private Sector);
   - NPC Circular No. 2022-04 (Registration of Personal Data Processing Systems and DPOs);
   - NPC Circular No. 2022-01 (Administrative Fines).
3. **Republic Act No. 10175 (Cybercrime Prevention Act of 2012):** Section 4(a) (illegal access, data interference, system interference, misuse of devices).
4. **Republic Act No. 9470 (National Archives of the Philippines Act of 2007):** Public records management, archival retention schedules, and lawful disposal standards.
5. **Batas Pambansa Blg. 232 (Education Act of 1982):** Chapter 2, Section 9(4) (confidentiality of student educational records).
6. **Republic Act No. 8293 (Intellectual Property Code of the Philippines):** Author copyright and moral rights protection.
7. **Republic Act No. 6713 (Code of Conduct and Ethical Standards for Public Officials and Employees):** Standards of professionalism and ethics for public university personnel.
8. **Republic Act No. 11032 (Ease of Doing Business and Efficient Government Service Delivery Act of 2018):** Streamlined administrative processing and queue management.
9. **Makati City Ordinance 2020-059 (Revised UMak Charter):** Municipal institutional charter and University Board of Regents resolutions.
10. **University of Makati Data Privacy Policy Statement:** Center for Data Protection and Records Management Services (CDPRMS), official publication.
