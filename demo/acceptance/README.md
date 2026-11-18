# Synthetic CRM acceptance fixtures

Run from the repository root with Node 24, the installed project dependencies, and PostgreSQL tools on your PATH.

```sh
npm run acceptance:synthetic
```

The runner creates a fresh PostgreSQL cluster and starts localhost-only CRM and mail inbox servers on free ports. It does not read the project's `.env` or connect to its database. It prints the API and mailbox URLs and a private temporary directory with the generated demo password, invitation code, database ID mapping, and sample attachments. Press Ctrl+C to stop the services. Each run starts fresh.

Use `new-researcher@example.test` to exercise signup. Open the printed mailbox URL to read the code sent by `crm-verification@example.test`. The SMTP server accepts only `example.test` recipients and has no outbound delivery. The fixture also includes verified Student and Faculty accounts, unverified and inactive accounts, a user with no requests, an unrelated researcher, and an invited faculty researcher. The invitation expires 15 minutes after seeding.

Run a complete synthetic setup and smoke check without keeping the servers running.

```sh
npm run acceptance:synthetic -- --smoke
```

For Android testing, set `CRM_SYNTHETIC_TLS_CERT` and `CRM_SYNTHETIC_TLS_KEY` to local TLS files. The emulator must trust the issuing CA and the certificate must cover the origin used by the app. For the standard Android emulator that origin uses host alias `10.0.2.2`. Configure it on a fresh test installation. The default local HTTP mode is for API checks. The optional Android instrumentation test `CatalogHttpsTest` takes `catalogOrigin` and `catalogCert` (the base64 DER certificate) as runner arguments. Its staff journey also reads the generated demo password from `files/catalog-test-password` in the debug app’s private storage on the test emulator.

## Group demo account creation

Use the synthetic API and mailbox printed by the runner. These addresses work in that isolated demo environment; they are not accounts on the deployed API.

| Field | Student demo | Faculty demo |
|---|---|---|
| Name | Demo Student 01 | Demo Faculty 01 |
| Email | student.demo01@example.test | faculty.demo01@example.test |
| College | Demo College | Demo College |
| Category | Student | Faculty |
| Password | Choose at least 12 characters | Choose at least 12 characters |

Use a different address for each signup, such as student.demo02@example.test. The verification code appears at the printed mailbox URL; no real email account or institutional SMTP service is needed. Enter the actual generated code to finish registration.

For existing seeded logins, use demo.student@example.test, demo.faculty@example.test, demo.processing@example.test, demo.registry@example.test, demo.progress@example.test, demo.reporting@example.test, or demo.admin@example.test. Their shared password is generated for each isolated run and saved in the printed fixture directory as demo-password.txt. Do not use these passwords on real accounts. Creating a new researcher does not require this seeded-account password.

[Fixture definitions](fixtures.json) include the reconstructed D1 reporting expectations, sample catalog states, six individual staff permissions, and request and lifecycle edge cases. The runner seeds actual registrations, linked service requests, presentation outcomes, memberships, consultations and catalog revisions with a sample PDF. The smoke checks exercise account administration, permissions, registry correction/document versions and live D1 reports.

The Android HTTPS suite can be run with `npm run acceptance:android` using JDK 21 and a running emulator. It provisions its own temporary HTTPS fixture and credentials and tears them down after testing. This automated test server is separate from a shared staging/demo deployment. A shared deployment still needs the synthetic mail sink and accepted example.test domain configured there; the local runner alone does not configure the deployed API.

## UMak-domain manual demo on the connected emulator

```sh
JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home npm run demo:manual
```

This starts a fresh isolated fixture using `@umak.edu.ph`, checks signup/verification/sign-in, builds and installs the separate **UMak CRM Demo** Android variant, and opens it on the connected emulator. Keep this process running throughout the demo. Stop it with Ctrl+C. Restarting creates a fresh database and new seeded-account passwords.

Open `outputs/manual-demo/DEMO-ACCESS.md` for the current inbox URL, generated login password and persona list. New signup addresses are `student.demo01@umak.edu.ph`, `faculty.demo01@umak.edu.ph`, and other numbered variants. Seeded staff/researcher addresses begin `demo.`. Only these reserved patterns are accepted; real-looking UMak addresses are rejected. No mail leaves the local sink.

The demo variant trusts only its generated local certificate for its local API origin. Its application ID is `ph.edu.umak.crm.app.demo`; the regular app is separate. The APK in `outputs/manual-demo` targets this Mac's connected emulator through ADB forwarding. Installing that APK on another phone does not provide a working shared backend. Shared staging remains a separate deployment step.
