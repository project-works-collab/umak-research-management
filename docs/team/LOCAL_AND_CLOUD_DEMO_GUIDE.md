# Cloud and Local Demo: step-by-step presentation guide

The **UMak CRM Demo** app has a top button showing **Local Demo • Switch to Cloud** or **Cloud • Switch to Local Demo**. Tap it and confirm Switch. Sign in using the selected server's credentials. Accounts and records are separate; switching does not copy data. Unsaved form edits are discarded. Local drafts are stored separately for each server. Switching clears the public cache and saved sign-in; the old server session expires normally if no logout reaches it.

## Before presentation day

1. Use a Mac or Linux laptop with Node **24**, Python **3.12+**, PostgreSQL **18** including `initdb`, `pg_ctl`, `createdb`, and JDK **21**. Install Android Studio and its Android SDK platform 35/build tools, platform-tools and an emulator image. Have OpenSSL and Git available. Setup and the first Android build require internet to download dependencies. PostgreSQL tools must be on PATH; PostgreSQL.app or Homebrew works on macOS; Linux installations must expose the versioned PostgreSQL bin directory.
2. Copy the complete updated project to the new laptop, or commit the app/source changes and clone that commit. A plain clone of an older cloud commit will not contain this switch. Do not copy `.env`, `.private`, `node_modules`, generated certificates, passwords or old generated APKs. Keep source files including the new demo scripts and migration 023. The cloud server is not changed by these steps.
3. Open a terminal in the project folder. Run:

   ```sh
   node --version
   java -version
   python3 --version
   initdb --version
   npm ci --ignore-scripts
   npm run demo:tfidf:setup
   ```

   Node must report v24; Java must report 21. Set `JAVA_HOME` to your JDK21 installation if necessary. On this Mac the command is `export JAVA_HOME=$(/usr/libexec/java_home -v 21)`. On another laptop use its actual installation path.
4. Configure `ANDROID_HOME` to the Android SDK directory shown in Android Studio's SDK Manager. On macOS it is usually `$HOME/Library/Android/sdk`; on Linux usually `$HOME/Android/Sdk`. Add `$ANDROID_HOME/platform-tools` to PATH, or set `ADB` to the absolute adb executable. Run `adb devices`.
5. Choose **one** connected target: an Android phone over USB or an Android Studio emulator. Disconnect/stop extra targets so adb does not report multiple devices.

## Android phone over USB — recommended for poor signal

1. On the phone, enable Developer options (tap Build number seven times in Settings → About phone). Enable USB debugging in Developer options.
2. Connect a data-capable USB cable. Accept the phone's “Allow USB debugging?” prompt. Run `adb devices`; the phone must show `device`, not `unauthorized` or `offline`.
3. If an older UMak CRM Demo app was installed from a different laptop, Android may reject the different debug signing key. Back up anything needed, then uninstall **only UMak CRM Demo** from the phone. This removes that demo app's local data. Leave the regular/cloud app installed if desired.
4. In the project terminal run:

   ```sh
   npm run demo:manual
   ```

5. Wait for **MANUAL DEMO READY**. The launcher starts an isolated local database, local verification inbox and real TF-IDF scorer; builds the app with the laptop's generated local certificate; installs it; forwards the local API over USB; and opens the app. Keep this terminal running and the USB cable connected.
6. Open `outputs/manual-demo/DEMO-ACCESS.md` on the laptop. It contains the accounts, API address and verification inbox URL. The local UMak demo's shared password is `password1234`. Use `demo.student@umak.edu.ph` for researcher flows and the appropriate `demo.*` staff account for each workspace. These are local demo accounts, not cloud credentials.
7. The top button should show **Local Demo • Switch to Cloud**. Local mode works with the phone's mobile data and Wi-Fi turned off. Do not disconnect USB. The laptop itself needs no internet after dependencies and build tools are installed.

## Android Studio emulator

1. In Android Studio → Device Manager, start one Android emulator (API26 or newer).
2. Run `adb devices`; exactly one emulator should show `device`.
3. Run `npm run demo:manual` and wait for **MANUAL DEMO READY**.
4. Use the generated access guide above. ADB forwarding handles the connection; do not manually substitute `10.0.2.2` for the provided HTTPS origin.

## Switch between local and cloud during the demo

1. Finish or save current work. Tap the top server button and confirm **Switch**.
2. The app clears the saved session, clears public cached content and opens a fresh session on the selected server. Sign in again.
3. **Cloud** uses `https://umak-crm-api.onrender.com` and needs internet. Use a valid existing cloud account. Cloud features depend on what is actually deployed/configured there; this button does not deploy local TF-IDF or migrations to cloud.
4. **Local Demo** uses the running laptop fixture through USB/ADB. Use credentials in the current access guide. No internet is required.
5. If cloud signal deteriorates, switch back to Local Demo and sign in locally. There is no automatic failover, synchronization or copying of records. Prepare comparable local examples beforehand.

## TF-IDF showcase

1. In local mode, sign in as `demo.registry@umak.edu.ph` using the generated password.
2. Open Registry → Registered research → Add Research Entry.
3. Copy the title and abstract from `outputs/manual-demo/tfidf-scenario.json` into the form.
4. Tap **Review duplicates**. The identical study scores near 1.000; an unrelated abstract with the same title scores lower. Tap **Back to entry** to change text without creating a record.
5. Scores advise staff review. The synthetic pilot threshold does not establish plagiarism or real-world accuracy.

## Stop, restart, or move to another laptop

1. Stop the demo terminal with Ctrl+C. This shuts down its API, TF-IDF scorer and disposable database.
2. Restart using `npm run demo:manual`. Each launch creates fresh data, port and certificate; the shared local demo password remains `password1234` and reinstalls the matching app. Read the new access guide. Previous local records are not retained.
3. On another laptop, repeat the setup/build/install steps. Do not reuse an APK or generated certificate from the previous laptop: it points to that previous demo run. A different signing key may require uninstalling the old demo variant first.
4. Build and rehearse on the presentation laptop before the event, while internet is available. Bring a spare data cable and keep the laptop powered on/awake.

## Troubleshooting

| Symptom | Action |
|---|---|
| `adb: more than one device` | Leave one phone/emulator connected. |
| `unauthorized` | Unlock phone, approve USB debugging, reconnect cable. |
| `initdb` / `pg_ctl` missing | Install PostgreSQL18 and add its bin directory to PATH. Do not run initdb as root. |
| Java / Android build error | Select JDK21 and install SDK35/build tools in Android Studio. First builds need internet. |
| Local connection fails | Keep launcher running and USB connected. Restart `npm run demo:manual` to recreate forwarding and matching APK. |
| Certificate error | Rebuild/install through the launcher on the current laptop. Never disable TLS validation. |
| Package signature mismatch | Uninstall only the old UMak CRM Demo variant, accepting loss of its local data, then rerun. |
| Cloud login fails locally | Local and cloud accounts/passwords differ. Check the button and use the selected server's credentials. |
| Cloud lacks a feature | The switch does not update cloud code/services. Demonstrate that feature locally until separately deployed. |
| Local demo password stops working after restart | Use `password1234` for seeded local demo accounts; check that Local Demo is selected. |

## Verification and scope

API/TF-IDF check: `npm run demo:tfidf:check`. Full backend check: `node scripts/synthetic-acceptance.js --full-tests`. Android acceptance requires the connected emulator and JDK21: `CRM_TFIDF_DEMO=true npm run acceptance:android`.

The launcher is supported here on macOS and uses POSIX PostgreSQL tooling on Linux; Windows should use a Linux/macOS presentation laptop or a separately tested Windows port. WSL-to-Windows USB/Android integration is not covered by these commands. Wi-Fi/LAN access is intentionally not enabled: the fixture binds localhost and trusts a localhost certificate. USB is the supported physical-phone offline route. Local antivirus remains a simulated clean scanner, and local records are synthetic. Cloud hosting, real malware scanning, S3/offsite controls and production certification remain separate.

The cloud HTTPS address stays the same across laptops. Local HTTPS is the Node.js API on the current laptop: `https://127.0.0.1:<generated-port>`. The port and certificate change each launch. The launcher rebuilds/configures the app and USB forwarding automatically. `127.0.0.1` works on the phone through ADB forwarding; it is not a shared public address. Cloud passwords are unchanged.
