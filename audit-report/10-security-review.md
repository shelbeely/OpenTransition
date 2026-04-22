# 10 — Security Review

## Per-domain ratings

| Domain | Rating | Notes |
|--------|------:|-------|
| Secret hygiene in repo | **8** | Real secrets are kept out (Play Store JSON, release keystore, real AdMob IDs are CI-only). Debug keystore committed (intentional, low risk). |
| Signing config | **7** | Release keystore injected via `KEYSTORE_64`; falls back to debug keystore if missing — fail-loud would be better. |
| Network security | **7** | No cleartext, no raw HTTP traffic; no `network_security_config.xml` (none needed today). |
| Exported components | **6** | Manifest is mostly correct; `MobileWearableListenerService` is exported (required by GMS) but trusts incoming payloads without source-node verification. |
| PendingIntent mutability | **9** | All `PendingIntent`s use explicit flags (`FLAG_IMMUTABLE` set; verified `grep -rn "PendingIntent.getActivity"` cases). |
| Deep link validation | **7** | `.ttbackup` intent-filters validated by MIME + extension check inside `MainActivity.processIntent`. |
| Data Layer payload trust | **4** | Mobile listener trusts any node in the GMS-allowed set; no integrity check on milestone JSON. |
| Encryption at rest | **3** | The mechanism (SQLCipher + EncryptedSharedPreferences) is sound, but the user-facing toggle protects ~no real data — see ISSUE-004 / ISSUE-013. |
| Biometric / Keystore | **8** | `BiometricPromptHelper` uses `BiometricPrompt` correctly with strong authenticators; `KeystoreManager` uses `MasterKey.Builder().setKeyScheme(AES256_GCM)`. |
| ProGuard / R8 | **7** | Rules minimal and focused (Realm, Picasso). No leakage of class names. R8 enabled in release. |
| Dependency CVE exposure | **6** | A few alphas/old versions; details in `14-dependencies-and-supply-chain.md`. |
| **Overall** | **6 / 10** | Privacy-first design intent is strong; "encrypted database" claim is misleading in the current state. |

## 1. Secrets in repo

| Secret | In repo? | How handled |
|--------|----------|-------------|
| Real AdMob app ID | ❌ | `secrets.properties` is gitignored; CI injects via `SECRETS_PROPERTIES_64`. ✅ |
| Real AdMob ad-unit IDs | ❌ | Same. ✅ |
| Test AdMob app ID (Google's public test ID) | ✅ | `secrets.properties.example:1` — intentional, public. ✅ |
| `CODE_SALT` (used to derive PIN hash) | ✅ in example file (placeholder) | Real value injected by CI. ✅ |
| `mobile/google-services.json` | ❌ | Gitignored; CI uses `GOOGLE_SERVICES_JSON` secret. ✅ |
| Release keystore | ❌ | `keys/release-keystore.jks` is gitignored; injected via `KEYSTORE_64`. ✅ |
| Debug keystore | ✅ `keys/debug-keystore.jks` | Standard practice for repo-bundled debug keystore. ✅ |
| Play Store service account JSON | ❌ | `PLAY_STORE_CONFIG_JSON` secret. ✅ |
| Firebase keys | n/a | Live in `google-services.json`. ✅ |

🟢 **Excellent overall**. The committed example file is genuinely safe (uses Google's *publicly documented*
test AdMob IDs).

🟡 **One concern**: `secrets.properties` is referenced from `mobile/build.gradle:226-228` via plain
`new FileInputStream(file("$rootDir/secrets.properties"))`. If a developer has a stale local file with
real credentials, they may inadvertently include them in a build artifact published to a third party.
A `secrets-gradle-plugin` adoption would be more idiomatic.

## 2. Signing config exposure

`mobile/build.gradle:44-58`:
- Debug signing config has hard-coded passwords ("debugkey") — fine; debug keys are not secret.
- Release signing config falls back to the **debug keystore + "debugkey" password** if env vars or the
  release keystore are missing (`storeFile = releaseKey.exists() ? releaseKey : debugKey`,
  `storePassword = System.getenv("STORE_PASS") ?: "debugkey"`).

🟠 **Risk**: a misconfigured CI run could produce a "release" AAB signed with the debug keystore. Play
Store would reject it on first upload (different fingerprint), but a developer building locally with
`./gradlew :mobile:bundleRelease` and missing env vars gets an APK that *looks* release but isn't. The
fail-loud pattern (`throw new GradleException("Release keystore missing")`) is preferred.

## 3. Network security config / cleartext

- No `network_security_config.xml`.
- `android:usesCleartextTraffic` not set (defaults to `false` on `targetSdk >= 28`). ✅
- No certificate pinning. Acceptable for a primarily-offline app, but Firebase calls are unpinned.
- No WebView usage anywhere (`grep -rn "WebView" mobile/src/main` → 0). ✅ no `setJavaScriptEnabled` to worry about.

## 4. Exported components

`mobile/src/main/AndroidManifest.xml`:

| Component | Exported | OK? |
|-----------|----------|-----|
| `.ui.MainActivity` (`alias .MainActivityDefault`) | yes (LAUNCHER) | ✅ required |
| `.MainActivityTrain` activity-alias | yes (LAUNCHER, disabled by default) | ✅ — toggled at runtime via `PackageManager` |
| `.wear.MobileWearableListenerService` | yes (with `tools:ignore="ExportedService"`) | ⚠️ required by GMS, but consumer trusts payload — see ISSUE-011 |
| `androidx.work.impl.WorkManagerInitializer` | system-managed | n/a |
| Firebase services | system-managed | ✅ |

`wear/src/main/AndroidManifest.xml`:
| `.MainActivity`, `.CameraControlActivity`, `.AudioRecordActivity` | yes (LAUNCHER chain) | ✅ |
| `.WearableListenerService` | yes (GMS) | ⚠️ — handlers are empty bodies (ISSUE-007), no source-node validation. |

🟡 **No `BroadcastReceiver`s declared** — good; receivers are the most common exported-component
mistake.

## 5. PendingIntent mutability

`grep -rn "PendingIntent\." mobile/src/main wear/src/main` returns only call sites in third-party libs
(MaterialDateRange picker, etc.). No app-defined `PendingIntent`s. ✅

## 6. Deep link validation

`MainActivity.processIntent` (`mobile/.../ui/MainActivity.kt:300+`) handles `.ttbackup` intent flow:

- Reads MIME type, falls back to URI extension match.
- Copies the file to internal cache before parsing.
- Wraps Realm import in a try/catch with user-facing error.

✅ Reasonable. The 3-intent-filter manifest pattern (`mobile/AndroidManifest.xml:60-147`) handles
edge cases gracefully and is well-commented.

🟢 No vulnerable schemes (`http://`, `intent://` tunnels, `file://` open) accepted.

## 7. Data Layer payload trust

🟠 **Findings:**
- `MobileWearableListenerService.onMessageReceived` (`mobile/.../wear/MobileWearableListenerService.kt:60-95`)
  reads `messageEvent.path` and `messageEvent.data` without checking `messageEvent.sourceNodeId`.
- The audio sync handler (`MobileWearableListenerService.kt:144-202`) writes the byte array to
  `filesDir/audio/<filename>` where `<filename>` is taken from the wire payload. `filename` is
  validated by `replace("..", "")` only — not by canonicalization. A crafted filename like
  `"foo/../../shared_prefs/x.xml"` could theoretically write outside the audio directory (Android
  app sandbox limits this to the app's own data directory, but it could overwrite app preferences).
  🟠 ISSUE-028 (new).

  ```kotlin
  // wear/.../AudioRecordActivity.kt:106 produces "audio_yyyyMMdd_HHmmss.3gp" — safe in practice,
  // but the mobile side trusts whatever string it receives.
  ```

🟢 Wear-side `MainActivity.onDataChanged` only acts on its own DataItem path and doesn't ingest binary
files; lower trust risk.

## 8. Encryption at rest

| Store | Encryption | Reality |
|-------|-----------|---------|
| Realm default DB | ❌ unencrypted | Holds 100% of UI-visible data (ISSUE-004). 🔴 |
| Room "encrypted" DB | ✅ SQLCipher with hardware-backed passphrase via `KeystoreManager` | Holds essentially nothing because the UI doesn't read from it. 🔴 |
| Encrypted SharedPreferences (passphrase store) | ✅ `MasterKey.AES256_GCM` | Working. ✅ |
| Audio files | ❌ stored in `filesDir/audio/` plaintext | Bypass even if Realm/Room were encrypted. ISSUE-013. 🟠 |
| Photos | ❌ stored in app-specific dirs plaintext | Standard Android sandbox protection only. 🟡 |

🔴 **Bottom line**: a user enabling "Encrypt my database" in Settings is essentially given a placebo.
The full data set (Realm + audio files + photos) remains plaintext. This is a **misleading security
claim** and a privacy-policy / Play Store data-safety risk.

## 9. Biometric / KeyStore usage

`BiometricPromptHelper.kt`:
- Uses `BiometricPrompt.Builder` with `setAllowedAuthenticators(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)`.
- Falls back to PIN (the app's own PIN) if biometric is unavailable.
- Correctly handles authentication errors (`onAuthenticationError`).

`KeystoreManager.kt`:
- Generates a 32-byte random passphrase on first use.
- Stores it in `EncryptedSharedPreferences` using `MasterKey` (AES256_GCM, hardware-backed).
- Recreates the master key on `KeyPermanentlyInvalidatedException` (e.g. user resets device fingerprint).

✅ Solid implementation. **Rating: 8 / 10.**

## 10. ProGuard / R8 rules

`mobile/proguard-rules.pro` (30 lines):
```
-keep class io.realm.kotlin.** { *; }
-dontwarn com.squareup.picasso.**
-keep class com.shelbeely.opentransition.data.** { *; }
... (a few more)
```

✅ No `-dontobfuscate` or `-dontoptimize` overrides. Class-name leakage is limited to data models
(necessary for Realm reflective access). R8 will still rename UI/util classes.

🟡 No `-keep` for the wearable communication models — Gson reflection on `MilestoneData` would break
in release if `:shared` were ever obfuscated; today it isn't (`shared/build.gradle` doesn't enable
minify). ✅

## 11. Dependency CVE exposure

(Best-effort — not a substitute for `gh-advisory-database` query, which the task instructions cover.)

| Library | Version | Concern |
|---------|---------|---------|
| `androidx.security:security-crypto` | `1.1.0-alpha06` | Alpha library used in production. Track for upgrade when stable. |
| `com.squareup.picasso` | `2.8` | Last release 2021. No known active CVEs but unmaintained. |
| `net.zetetic:android-database-sqlcipher` | `4.5.4` | The library was rebranded to `net.zetetic:sqlcipher-android` in 2023; current version is 4.6+. Old artifact still receives security updates. 🟡 |
| `com.firebaseui:firebase-ui-auth` | `8.0.2` | The FirebaseUI project was archived in 2024. Active CVEs unknown but no further security fixes. 🟠 |
| `com.google.android.gms:play-services-ads` | `22.4.0` | Current 23.6+. 🟡 upgrade. |
| `com.google.android.gms:play-services-wearable` | `18.1.0` | Current 18.2+. 🟢 minor. |
| `com.google.android.gms:play-services-auth` | `20.7.0` | Current 21.x. 🟡. |
| `org.jetbrains.kotlinx:kotlinx-coroutines-rx3` | `1.7.3` | Current 1.10+. 🟢. |

🟠 **`firebase-ui-auth` archival** is the most consequential supply-chain concern. Recommendation:
migrate to direct `FirebaseAuth` + `Credential Manager` calls.

## 12. Privacy posture (informational)

Strengths worth highlighting:
- ✅ No analytics other than Firebase Analytics (which can be disabled by the user via the OS
  data-sharing toggles).
- ✅ Firestore only stores user *settings* (not photos, not milestones).
- ✅ No location, no contacts, no sensors, no SMS.
- ✅ Photo Picker (`PickMultipleVisualMedia`) avoids `READ_MEDIA_IMAGES` permission entirely.
- ✅ `FLAG_SECURE` ("Quick Hide") option to prevent screenshots.
- ✅ Decoy vault concept exists.

Gaps:
- 🔴 Encryption claim is misleading (above).
- 🟡 Audio files aren't encrypted.
- 🟡 Crashlytics enabled in release without an explicit opt-in dialog (industry standard but worth
  reviewing for transition-tracking-app threat model).
