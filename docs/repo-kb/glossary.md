# Glossary

| Term | Meaning |
|---|---|
| **OpenTransition** | This project. Active successor to TransTracks. |
| **TransTracks** | The retired predecessor app. OpenTransition can import its `.ttbackup` archives. |
| **`:mobile`** | The phone/tablet Android app module (`com.shelbeely.opentransition`). |
| **`:wear`** | The Wear OS companion app module (`com.shelbeely.opentransition.wear`). |
| **`:shared`** | Android library used by both `:mobile` and `:wear`. |
| **Wearable Data Layer API** | Google API used for `:mobile` ↔ `:wear` communication. Provided by `play-services-wearable`. |
| **Disguised mode** | UI feature that hides the app's real identity behind an alternate icon/title. |
| **Decoy vault** | A second vault with its own passcode, surfaced when the decoy code is entered. |
| **`secrets.properties`** | Local-only file with AdMob and Firebase secrets. The `.example` variant ships test IDs. |
| **`google-services.json`** | Firebase project descriptor. Real values are CI-injected; a dummy stub is used in dev/agent runs. |
| **KSP** | Kotlin Symbol Processing — used by Room. Replaces KAPT in this project. |
| **SafeArgs** | Jetpack Navigation gradle plugin that generates type-safe argument classes for nav destinations. |
| **SQLCipher** | Optional Room database encryption (`net.zetetic:android-database-sqlcipher:4.5.4`). |
| **Realm Kotlin SDK** | Used **read-only** to import legacy TransTracks `.ttbackup` archives. |
| **Fastlane supply** | Tool used in `ci.yml` to upload AABs and metadata to Google Play. |
| **Boycott check** | CI guard that blocks PRs introducing dependencies on a configured boycott list. |
| **Pass 1 / pass 2 / refresh** | Knowledge-base build modes from the `repo-knowledge-base` skill. Pass 1 = root-level inventory + agent instructions; later passes go feature-by-feature. |
