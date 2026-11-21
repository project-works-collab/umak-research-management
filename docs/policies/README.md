# CRM policies — institutional documentation

**Institutional Policy Suite — University of Makati Center for Research Management**

- [Privacy Policy](PRIVACY_POLICY.md)
- [Terms and Conditions](TERMS_AND_CONDITIONS.md)
- [Generator source archive](generator-original/README.md) — unadapted evidence, not approved policies
- [Technical privacy handoff](../implementation/PRIVACY.md)

Updated: 21 November 2026. This policy suite aligns the University of Makati Research Management and Library Information System (UMak-RMLIS / CRM Application) with the official *University of Makati Data Privacy Policy Statement* and applicable Philippine statutory and regulatory requirements.

## Institutional adoption schedule

| Decision | Required information / review | Responsible institutional function |
| --- | --- | --- |
| A1 Operator and eligibility | Legal entity (University of Makati), operating office (Center for Research Management - CRM), approval authority (Board of Regents / VPRE), account eligibility, minors procedure, effective date and version | Administration and legal |
| A2 Contacts and rights | DPO contact (`dprms@umak.edu.ph`); research office contact (`research@umak.edu.ph`); physical postal address (J.P. Rizal Ext., West Rembo, Makati City); identity verification and response procedure | DPO (CDPRMS) and CRM |
| A3 Purposes and permissions | Lawful basis per purpose under RA 10173 Sections 12 & 13, research ethics compliance (UMak-REC), coauthor authority, evaluation permissions, and public catalogue approval | DPO, research office, and legal |
| A4 Providers and operations | Hosting infrastructure, backup and DRaaS safeguards, processor agreements, Information Technology Center (ITC) controls, and mobile offline caching rules | ITC and DPO |
| A5 Retention and erasure | Retention schedules under the National Archives of the Philippines Act (RA 9470), academic holds, shared-study protection, and deletion-ledger re-application | CDPRMS, DPO, and ITC |
| A6 Service procedures | Membership and reviewer assignments, evaluation dispute resolution, consultation scheduling under RA 11032, and terms amendment notices | CRM and legal |

## Feature-to-clause review

| Source at the pinned revision | Policy connection | Evidence boundary |
| --- | --- | --- |
| [Application routes](../../src/app.js), [validation](../../src/security/validation.js), [database migrations](../../db/migrations) | Accounts, research membership, assigned reviewers, separate notes, consent, reviewed deletion, and audit metadata | Source inspection; role-based access control, consent choices, and reviewed deletion workflows |
| [Document routes](../../src/storage/routes.js), [storage adapter](../../src/storage/s3.js) | Restricted PDF/DOCX uploads, 25 MB limit, temporary signed access URLs | File inspection, MIME validation, and temporary signed download links |
| [Implementation guide](../implementation/IAS_IMPLEMENTATION.md) and [implementation evidence](../../evidence/ias-plan/IMPLEMENTATION.md) | Encryption, recovery, deletion replay, Android offline library, and security audit logs | Security safeguards, encrypted session storage, and deletion ledger integrity |
| [Algorithm contract](../api/ALGORITHM_API.md), [search and scheduling](../api/SEARCH_AND_CONSULTATIONS.md) | Suggestions, structural estimates, and consultation queue ordering | Decision-support suggestions; no automatic academic clearance or guaranteed turnaround |

## References

1. Republic of the Philippines, [Republic Act No. 10173, Data Privacy Act of 2012](https://privacy.gov.ph/data-privacy-act/), especially sections 11–14, 16, 18, 20–21. General principles (Transparency, Legitimate Purpose, Proportionality), lawful criteria, data subject rights, security safeguards, and breach notification.
2. National Privacy Commission, [Implementing Rules and Regulations of the Data Privacy Act of 2012](https://privacy.gov.ph/implementing-rules-regulations-data-privacy-act-2012/), 2016, rules IV–VI and VIII–IX.
3. National Privacy Commission Issuances: NPC Circular 16-01 (DPO registration), NPC Circular 16-02 (Data Sharing Agreements), and NPC Circular 16-03 (Personal Data Breach Management).
4. Republic Act No. 10175, [Cybercrime Prevention Act of 2012](https://lawphil.net/statutes/repacts/ra2012/ra_10175_2012.html), section 4(a) (illegal access, data interference, system interference, misuse of devices).
5. Republic Act No. 9470, [National Archives of the Philippines Act of 2007](https://www.officialgazette.gov.ph/2007/05/21/republic-act-no-9470/), governing the management, retention, and disposition of public and academic records.
6. Batas Pambansa Blg. 232, [Education Act of 1982](https://www.officialgazette.gov.ph/1982/09/11/batas-pambansa-blg-232/), Chapter 2, Section 9(4) (student rights to access own school records and maintain their confidentiality).
7. Republic Act No. 8293, [Intellectual Property Code of the Philippines](https://www.ipophil.gov.ph/intellectual-property-code-implementing-rules-and-regulations/), protecting author moral rights and original research copyright.
8. Republic Act No. 6713, [Code of Conduct and Ethical Standards for Public Officials and Employees](https://www.officialgazette.gov.ph/1989/02/20/republic-act-no-6713/).
9. Republic Act No. 11032, [Ease of Doing Business and Efficient Government Service Delivery Act of 2018](https://www.officialgazette.gov.ph/2018/05/28/republic-act-no-11032/).
10. City Government of Makati, [Makati City Ordinance 2020-059 (Revised UMak Charter)](https://www.umak.edu.ph/) and University Board of Regents resolutions.
11. University of Makati, *Data Privacy Policy Statement*, Center for Data Protection and Records Management Services (CDPRMS), official publication.
