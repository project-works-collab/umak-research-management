# Hosted UMak workflow demo

The hosted demo uses the existing Render service and Supabase database. It does not require a Mac, local SMTP service, ADB forwarding, or a self-signed certificate on presenters' devices.

- Render start command: `npm run start:demo`.
- Required settings: `CRM_DEMO_MODE=umak`, `ALLOW_SYNTHETIC_SETUP=yes`, existing `DATABASE_URL`, `JWT_SIGNING_KEY`, and a demo-only `DEMO_PASSWORD` of at least 12 characters.
- Apply migrations through 021 before deploying. The Supabase installation originally had the 007 schema without a migration-history table; the reviewed migration transaction records that baseline before applying newer migrations.
- Seed with `seedHostedDemo` through the privileged database connection. The seed ledger prevents duplicates and preserves subsequent demo edits. Existing records and credentials are retained.
- `/demo/inbox` uses the shared demo password and an eight-hour HttpOnly/Secure cookie. Codes are persisted in the database and displayed for 15 minutes. Codes remain single-use and expiry-enforced by the verification workflow.
- Signup addresses must be `student.demo01@umak.edu.ph`, `faculty.demo01@umak.edu.ph`, or another numbered variant. Seeded personas use `demo.student@umak.edu.ph`, `demo.faculty@umak.edu.ph`, `demo.content@umak.edu.ph`, `demo.processing@umak.edu.ph`, `demo.registry@umak.edu.ph`, `demo.progress@umak.edu.ph`, `demo.reporting@umak.edu.ph`, and `demo.admin@umak.edu.ph`. Each staff persona has only its named permission.
- No external mail transport is used. Other university addresses are rejected. This is a synthetic workflow demonstration, not a real university account service.
- Synthetic attachments undergo file-format and size validation. The demo deliberately does not claim production antivirus scanning. TF-IDF integration remains separate.

`npm run test:demo:hosted` exercises an isolated database, repeatable seeding, D1 reporting, reserved UMak signup, protected inbox authentication, verification and sign-in. `node scripts/synthetic-acceptance.js --full-tests` runs the full backend test suite on a separate disposable cluster.

Generated environment exports, SQL containing password hashes, recovery snapshots and presenter credentials belong under `.private/` or the ignored `outputs/manual-demo/`. Never commit those files.
