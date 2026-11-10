# CRM Android app, public cache and session vault

Android library with a launchable `:app` module, API 26+, compile SDK 35, JDK 17/21 and pinned Gradle 8.9 wrapper. Dependency versions are explicit compatibility pins, not claims of latest releases.

```sh
./gradlew :app:assembleDebug assembleDebugAndroidTest :app:lintDebug lintDebug
./gradlew connectedDebugAndroidTest
```

Set `ANDROID_HOME` or an ignored `local.properties` SDK path. The `:app` launcher integrates sign-in, logout, Keystore sessions, public resource refresh/listing, stale/empty indicators and offline plain-text template viewing. Enter the HTTPS API origin; each installation binds its cache/session to that origin. Research editing/review screens are outside this minimal app. `PublicSync.schedule(context, "https://your-crm-host")` installs a 12-hour network-constrained refresh. Query Room on a background thread; display the last-sync time and offline/stale state. A first install without data must show an empty state. Blank-template bytes are app-private files; Room holds only public metadata and file paths. Public API contents in this repository are marked synthetic and require institutional replacement.

`SessionVault` encrypts optional persistent secrets with an Android Keystore AES-256-GCM key and fresh IV; the ciphertext lives in `noBackupFilesDir`. Keep short-lived access tokens in memory if persistence is unnecessary. Call `clear()` on logout, handle `read()==null` by signing in, and never log the returned secret. The library does not add refresh-token rotation to the server.

Instrumentation tests cover Keystore encryption/tamper/logout and Room reopen without network. For an airplane-mode/process-death UI check in the new launcher: sync content, open a template, force-stop, disable networking, relaunch, then verify directory/instructions/template and stale indication. Use Refresh public resources while online, then verify the saved items after restart in airplane mode. Also test expired sessions and offline logout. Android app compilation/device verification has not been performed for this change.
