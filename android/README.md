# CRM Android app, public cache and session vault

Java Android library with a launchable `:app` module, API 26+, compile SDK 35, JDK 17/21 and pinned Gradle 8.9 wrapper. Dependency versions are explicit compatibility pins, not claims of latest releases.

```sh
./gradlew :app:assembleDebug :core:assembleDebug :core:assembleDebugAndroidTest :app:lintDebug :core:lintDebug
./gradlew :core:connectedDebugAndroidTest
```

Set `ANDROID_HOME` or an ignored `local.properties` SDK path. The `:app` launcher integrates sign-in, logout, Keystore sessions, public resource refresh/listing, stale/empty indicators and offline plain-text template viewing. Enter the HTTPS API origin; each installation binds its cache/session to that origin. Research editing/review screens are outside this minimal app. `PublicSync.schedule(context, "https://your-crm-host")` installs a 12-hour network-constrained refresh. Query Room on a background thread; display the last-sync time and offline/stale state. A first install without data must show an empty state. Blank-template bytes are app-private files; Room holds only public metadata and file paths. Public API contents in this repository are marked synthetic and require institutional replacement.

`SessionVault` encrypts optional persistent secrets with an Android Keystore AES-256-GCM key and fresh IV; the ciphertext lives in `noBackupFilesDir`. Keep short-lived access tokens in memory if persistence is unnecessary. Call `clear()` on logout, handle `read()==null` by signing in, and never log the returned secret. The library does not add refresh-token rotation to the server.

Instrumentation tests cover Keystore encryption/tamper/logout and Room reopen without network. For an airplane-mode/process-death UI check in the new launcher: sync content, open a template, force-stop, disable networking, relaunch, then verify directory/instructions/template and stale indication. Use Refresh public resources while online, then verify the saved items after restart in airplane mode. Also test expired sessions and offline logout. The Java app, library and instrumentation APKs were built and both modules passed lint on 11 November 2026. Instrumentation tests have not been executed on a device; no device was attached.

## Source layout

Open this `android/` folder as the Android Studio project. It contains two modules:

- `app/`: launcher UI, sign-in and offline resource viewing.
- `core/`: Room cache, WorkManager sync, Keystore session vault and instrumentation tests.

The root Gradle files coordinate the modules; the root contains no application or library source. All source code is Java and Gradle scripts use Groovy. Room uses [Java annotation processing](https://developer.android.com/jetpack/androidx/releases/room).

Java folders match their package declarations. The application ID remains `ph.edu.umak.crm.app`, and core package names remain unchanged for persisted WorkManager jobs.

### Compact navigation in Android Studio

Choose **Android** in the Project tool window, then enable **Compact Middle Packages** (called **Hide Empty Middle Packages** in some versions) in its view options. This displays shared package prefixes together instead of expanding one folder per name. Local IDE preferences are ignored by Git.

Typical compact view:

```text
android
├── app
│   ├── manifests
│   └── java → ph.edu.umak.crm.app → MainActivity
├── core
│   ├── manifests
│   └── java → ph.edu.umak.crm
│       ├── cache
│       └── security
└── Gradle Scripts
```

### Updating an existing team checkout

After the repository cleanup is merged, save or commit local work and pull the updated branch. Close the old Android project and open the repository's `android/` directory in Android Studio, then sync Gradle. The modules should be `app` and `core`; run configurations should target `app`. Configure your local Android SDK if prompted. Enable compact packages using the view setting above on each machine. IDE workspace files, SDK paths, credentials and build caches stay local.
