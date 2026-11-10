# UMak-RMLIS Privacy Policy

**Final academic submission — proposed policy pending institutional adoption.**

Prepared: 10 November 2026. Effective date: **[pending institutional approval]**.

This policy describes the proposed handling of personal information in the University of Makati Research Management and Library Information System (UMak-RMLIS), also referred to as the CRM application. It covers student researchers, faculty researchers and CRM staff. The proposed operator is the University of Makati through its Center for Research Management; **[the authorized legal entity, responsible office and approval authority must be confirmed]**. The current repository is a synthetic-data teaching implementation, not an operational university service.

## 1. Information used and its purposes

| Information | Purpose |
| --- | --- |
| Email address, account identifier, role, credentials and session records | Establish and protect accounts and determine permitted access |
| Research titles, abstracts, year, department, ownership, team membership and reviewer assignments | Maintain the research registry and coordinate authorized evaluation |
| Submitted manuscripts and document metadata | Receive and manage research material for authorized review |
| Researcher-visible feedback and separate internal staff notes | Communicate review outcomes and support restricted review administration |
| Consultation service type, optional deadline, status, assignment and scheduling timestamps | Process consultation requests and organize the staff queue |
| Consent choices, policy version and server-recorded timestamps | Record optional preferences and their history |
| Deletion requests and review outcomes | Review and carry out eligible deletion requests |
| Security and activity metadata, including actor identifiers, record identifiers, action, time, request identifiers and IP addresses | Protect the service, investigate incidents and maintain accountability |

Provide only information needed for your research or service request. Avoid unnecessary personal information about research participants or other people in manuscripts and feedback. Obtain the appropriate authority and follow applicable research-ethics requirements before submitting another person's information. A research purpose does not by itself authorize unrestricted disclosure.

The verified implementation does not include advertising, behavioral analytics or marketing delivery. It does not establish collection of page-view duration or operating-system telemetry. Recording an optional preference does not mean that the corresponding analytics or email service is active.

## 2. Lawful processing and optional choices

Before real use, the institution must approve and disclose the lawful basis for each purpose, including any separate requirements for sensitive personal information. **[Approved purpose-by-purpose lawful bases are pending.]** Core research administration must not be made dependent on consent to unrelated optional processing.

Where processing relies on consent, the choice must be specific and voluntary. You may decline optional choices and later withdraw consent without invalidating earlier lawful processing. The application records separate choices for institutional analytics and email notifications; both may be false. If these services are introduced, their purposes and providers must be explained before activation. Submitting preferences is not acceptance of the Terms and Conditions.

## 3. Public information and restricted records

Only approved catalogue information is intended for public search: research title, abstract, research year and department. Approval must therefore include permission to disclose those fields, including personal information contained in an abstract. **[The publication-approval procedure and authority must be confirmed.]**

Private studies and manuscripts are restricted to authorized research members and assigned evaluation staff. Being a staff member does not grant access to every private study. Researcher-visible feedback is distinct from internal staff notes, which are excluded from researcher responses. Authorized administrative personnel may access necessary security records for their duties.

Catalogue approval does not make manuscript files public. Download access, where enabled, uses temporary links; recipients must not share those links with unauthorized people.

## 4. Recipients, providers and transfers

Information may be handled by authorized research-team members, assigned reviewers, appropriately authorized administrators and contracted providers needed to operate the service. Disclosures required by law must be assessed and limited to what is necessary.

**[Actual providers, processing locations, backup locations, transfer arrangements and safeguards must be confirmed before deployment.]** The code supports private object storage, but this policy does not assert that a particular cloud provider has been deployed. It does not assert that Google Play Services or an analytics SDK is in use. No routine transmission of anonymized analytics is established by the current implementation.

Any provider engagement or cross-border processing must undergo institutional privacy review and appropriate contractual and security arrangements before use. Publication of this document is not authorization for an international transfer.

## 5. Retention, deletion and backups

Information should be kept only for approved purposes and applicable legal, academic or records-management requirements. **[Retention periods and their starting events for accounts, research records, manuscripts, logs, requests, backups, object versions and deletion evidence remain subject to approval.]** The generator's example 12- and 24-month periods are not adopted.

Student and faculty researchers can request account deletion for staff review. Review must consider legal holds, retention requirements, shared research, coauthor interests and publication records. Eligible live records may be deleted after review; requesting deletion is not a promise that every related record will immediately disappear.

Deleting a live account does not automatically remove old backups or stored document versions. Object cleanup and recovery procedures must be operated under the approved retention schedule. Restored data must be checked against deletion evidence before service resumes. The current account-deletion transaction can remove owned research and related records, so shared studies require specific review before completion. Per-person cryptographic erasure is not implemented.

Uninstalling a future mobile application or ceasing website use does not delete records already held by the operator.

## 6. Your rights and contact channels

Subject to applicable conditions and exceptions, you may seek information about processing, access and correction, object to processing, request blocking or erasure, and exercise applicable portability rights. Consent-based processing may be challenged or consent withdrawn. You may raise a privacy complaint with the institution or the National Privacy Commission.

Proposed research-office contact supplied for this coursework: **research@umak.edu.ph**. **[Confirm that this mailbox accepts privacy requests; insert the authorized DPO contact, postal address and secure request channel before adoption.]** It is not identified here as a verified DPO mailbox. Do not send passwords, identity documents or manuscripts to an unconfirmed channel. Identity checks should be proportionate, and the institution must establish its request-handling and response procedure.

## 7. Security, mobile use and incidents

The service is designed to restrict access, protect credentials and sessions, validate submissions and record security events. Operational hosting, encryption, backup and incident-response arrangements require verification before real use. No system can guarantee prevention of every security incident.

The repository includes a mobile library for offline public information and protected session storage, not a complete released mobile app. Public offline copies can become outdated. Private manuscripts and internal staff notes are not intended for that public-content cache. Device access and downloaded copies remain important user responsibilities.

The institution must assess incidents, contain harm and notify affected people and the National Privacy Commission when legal notification requirements apply. This document does not assert that a breach occurred or that a notification was sent.

## 8. Eligibility and policy changes

**[Account eligibility and any procedure for minors must be approved.]** This policy does not assume a universal Philippine digital-consent age of 16. The institution must determine any appropriate guardian involvement and legal basis for processing.

Before adoption or material changes, the institution should publish an understandable notice with its approved version and effective date. Consent must be sought again where legally necessary. The preparation date above is not an institutional effective date.

## Reviewer appendix

- **Feature baseline:** commit `1a305391339b8258ab2f05737717aa0038fd3d97`; see [technical privacy handoff](../PRIVACY.md) and [implementation guide](../IAS_IMPLEMENTATION.md). Implementation does not establish deployment or legal approval.
- **Legal review:** RA 10173 sections 11–14, 16, 18, 20–21 and its IRR inform purpose limitation, lawful processing, rights, safeguards and accountability. Sensitive data needs separate assessment; the policy does not invent a blanket lawful basis. See the [reference and adoption record](README.md).
- **Adoption gate:** complete all decisions in the [institutional adoption schedule](README.md#institutional-adoption-schedule), verify actual operations and obtain genuine approval before activation.
- **Source and assistance:** adapted from the user's App Privacy Policy Generator export and earlier CRM academic policy material. ChatGPT/Codex assisted feature comparison, Philippine-source review, clause adaptation and document checks. Human institutional review remains pending. The [unchanged generator original](generator-original/privacy-generator-original.md) is source evidence, not the operative policy.

Related document: [Terms and Conditions](TERMS_AND_CONDITIONS.md).
