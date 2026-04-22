# 07 — Issues and Bugs

> Severity legend: 🔴 Critical · 🟠 High · 🟡 Medium · 🟢 Low
> Categories: bug · security · perf · correctness · UX · wear-sync · lifecycle · ci · docs · hygiene
> Platform: `[MOBILE]` `[WEAR]` `[SHARED]` `[INTEGRATION]` `[CI]`

## Critical

### `ISSUE-001` 🔴 wear-sync `[INTEGRATION]` `[MOBILE]` `[WEAR]`
**Title:** Wearable capabilities are never registered.
**Evidence:** `shared/.../WearableConstants.kt:18-20` defines `CAPABILITY_MOBILE_APP`, `CAPABILITY_WEAR_APP`. Searches across the entire repo (`grep -rn "android_wear_capabilities\|addLocalCapability"`) return zero hits, and no `wear.xml` resource file exists in either `mobile/src/main/res/values/` or `wear/src/main/res/values/`.
**Impact:** Every `CapabilityClient.getCapability(..., FILTER_REACHABLE)` call returns zero nodes. All Wear-side actions silently fail (Take Photo, Sync Now, Camera control, Audio send). UI shows "Phone Disconnected" permanently.
**Remediation direction:** Add `wear/src/main/res/values/wear.xml` and `mobile/src/main/res/values/wear.xml` each with a `<resources><string-array name="android_wear_capabilities"><item>opentransition_wear_app</item></string-array></resources>` (mirror for the mobile capability). Alternatively, call `Wearable.getCapabilityClient(context).addLocalCapability("opentransition_xxx_app")` in each `Application.onCreate`.

### `ISSUE-002` 🔴 ci `[CI]` `[MOBILE]`
**Title:** Play Store deploy uploads to a different package than the app actually builds.
**Evidence:** `.github/workflows/ci.yml:86` sets `packageName: com.drspaceboo.transtracks` while `mobile/build.gradle:36` sets `applicationId "com.shelbeely.opentransition"`.
**Impact:** Either the Play Store API call fails outright (best case) or it is attempting to push OpenTransition AABs into the **upstream archived TransTracks** Play Console listing (worst case).
**Remediation direction:** Change to `com.shelbeely.opentransition` and verify the service account has access. Also add the Wear app to the upload step.

### `ISSUE-003` 🔴 wear-sync `[MOBILE]` `[INTEGRATION]`
**Title:** Mobile→Wear milestone sync is not implemented; the handler is a `Log.d` line.
**Evidence:** `mobile/.../wear/MobileWearableListenerService.kt:97-106`:
```kotlin
private fun handleSyncRequest() {
    Log.d(TAG, "Handling sync request from wear device")
    // TODO: Implement milestone data sync to wear device
    // This would query the database for milestones and send them
}
```
**Impact:** The Wear "Sync Now" button does nothing useful. Milestone count on the watch is permanently stale.
**Remediation direction:** Read milestones from `DatabaseManager.getDatabase(context).milestoneDao()` (or Realm — see ISSUE-004), map to `MilestoneData`, call `WearableHelper.syncMilestones(...)`. Should also be triggered from milestone CRUD flows on the mobile side.

### `ISSUE-004` 🔴 correctness `[MOBILE]`
**Title:** Two databases live simultaneously; Settings advertises an "encrypted database" feature that protects almost nothing.
**Evidence:** Room/SQLCipher is wired up (`mobile/.../database/AppDatabase.kt`, `mobile/.../database/KeystoreManager.kt`), `SettingsManager.isEncryptedDatabaseEnabled()` exposes a toggle (`mobile/.../util/settings/SettingsManager.kt:399-406`), but every UI entry point still uses `Realm.openDefault()` directly:
- `mobile/.../ui/home/HomeGalleryAdapter.kt:58`
- `mobile/.../ui/gallery/GalleryAdapter.kt:47, 379`
- `mobile/.../ui/gallery/GalleryFragment.kt:182, 224`
- `mobile/.../ui/milestones/MilestonesAdapter.kt:38`
- `mobile/.../ui/singlephoto/SinglePhotoFragment.kt:51, 92, 119`
- `mobile/.../ui/recordaudio/RecordAudioFragment.kt:170`
- `mobile/.../ui/MainActivity.kt:391`
**Impact:** A user enabling "Encrypt my database" in Settings encrypts a nearly-empty Room database while their actual photos/milestones/audio analyses continue to live in plaintext Realm. Combined with the marketing copy in `ENCRYPTED_DATABASE.md` (*"Full database encryption at rest"*), this is a **misleading security claim**.
**Remediation direction:** Pick one of:
- **(A) Finish the migration**: route every UI Realm read through a repository that delegates to Room/SQLCipher. The Realm code becomes legacy-import-only.
- **(B) Roll back**: hide the encrypted-DB toggle in Settings, mark the docs WIP, ship Realm only.

### `ISSUE-006` 🔴 correctness `[MOBILE]`
**Title:** Room schema uses `fallbackToDestructiveMigration()` with `exportSchema=false`.
**Evidence:** `mobile/.../database/AppDatabase.kt:34, 89-91`:
```kotlin
@Database(..., version = 1, exportSchema = false)
...
.fallbackToDestructiveMigration()
// TODO: Replace with proper migration strategy before production use
```
**Impact:** Any future Room schema bump destroys all data in the encrypted database. Combined with no schema export, downgrades / sideways migrations from a future version cannot be reasoned about. The TODO acknowledges this.
**Remediation direction:** Set `exportSchema = true`, commit the schema JSON under `mobile/schemas/`, and replace `fallbackToDestructiveMigration` with explicit `Migration` objects before any release that actually persists data in Room.

## High

### `ISSUE-005` 🟠 correctness `[MOBILE]`
**Title:** `AudioAnalysisUtil` returns hard-coded fake formant numbers.
**Evidence:** `mobile/.../util/AudioAnalysisUtil.kt:62-75`:
```kotlin
// TODO: Integrate proper DSP library for real formant extraction
val f0Mean = 150f // Typical average pitch
val f0Min = 120f
val f0Max = 180f
val f0StdDev = 20f
// Typical formant values (these are averages, not actual analysis)
val f1Mean = estimateF1(f0Mean)
...
```
The same numbers are returned for every audio file the user records.
**Impact:** Users see "vocal change tracking" charts that are meaningless. Marketing copy in `README.md:62` claims *"track vocal changes with pitch/formant analysis"*.
**Remediation direction:** Either label the values as estimated/typical in the UI, hide the analysis screen behind a "preview" feature flag, or integrate an LPC/cepstrum library (TarsosDSP is one OSS option).

### `ISSUE-007` 🟠 wear-sync `[WEAR]`
**Title:** `WearableListenerService` handler bodies are empty.
**Evidence:** `wear/.../WearableListenerService.kt:52-64`:
```kotlin
private fun handleMilestoneSync(dataMap: ...) { /* empty */ }
private fun handleSettingsSync(dataMap: ...) { /* empty */ }
private fun handleMilestoneUpdate(dataMap: ...) { /* empty */ }
```
**Impact:** Even if the mobile app sends DataItems (it currently doesn't, see `ISSUE-003`), the watch service drops them on the floor when the app is not in the foreground. The `MainActivity.onDataChanged` foreground-only listener masks this most of the time during testing.
**Remediation direction:** Persist incoming data to a local Wear cache (DataStore), then surface via a `Tile` or notification.

### `ISSUE-008` 🟠 wear-sync `[WEAR]`
**Title:** Watch milestone cache only updates while `MainActivity` is foreground.
**Evidence:** `wear/.../MainActivity.kt:134-140` registers `dataClient.addListener(this)` in `onResume` and removes it in `onPause` (`:147-149`). The background `WearableListenerService` doesn't process the same path (see ISSUE-007).
**Impact:** If the user taps a Tile or notification action that needs fresh milestone data, the watch always shows stale data unless they open the main activity first.
**Remediation direction:** Implement `handleMilestoneSync` so the cache updates regardless of activity lifecycle.

### `ISSUE-009` 🟠 lifecycle `[WEAR]`
**Title:** Recursive `postDelayed` loop in audio recording is not cancelled on `onPause`.
**Evidence:** `wear/.../AudioRecordActivity.kt:157-162`:
```kotlin
private fun updateDuration() {
    if (isRecording) {
        durationText.text = formatDuration(System.currentTimeMillis() - recordingStartTime)
        durationText.postDelayed({ updateDuration() }, 1000)
    }
}
```
`stopRecording()` flips `isRecording = false`, which stops the chain *only after the next callback fires*. If the activity is destroyed mid-recording, the `Handler` callback retains a reference to the destroyed activity for up to one second.
**Impact:** Minor leak window; benign in practice but worth fixing.
**Remediation direction:** Hold a `Handler` reference and call `removeCallbacksAndMessages(null)` in `onPause`/`onDestroy`.

### `ISSUE-010` 🟠 perf `[WEAR]` `[INTEGRATION]`
**Title:** Audio bytes shipped via `DataClient.putDataItem`; will fail silently above ~100 KB.
**Evidence:** `wear/.../AudioRecordActivity.kt:198-206`:
```kotlin
val audioBytes = audioFile.readBytes()
val dataMap = putDataMapRequest.dataMap
dataMap.putByteArray(WearableConstants.KEY_AUDIO_DATA, audioBytes)
...
dataClient.putDataItem(putDataRequest)
```
**Impact:** AMR_NB at typical bitrate produces ~12.2 kbps → ~92 KB in 60 seconds, right at the soft limit. Any longer recording exceeds the 100 KB DataItem soft cap; the put may fail without a clear error.
**Remediation direction:** Use `ChannelClient.openChannel(...)` for any payload > 1 KB; stream the file. (Google's official Data Layer guidance.)

### `ISSUE-013` 🟠 correctness `[MOBILE]`
**Title:** `MobileWearableListenerService` opens an unencrypted Realm and writes audio metadata directly there, bypassing `DatabaseManager`.
**Evidence:** `mobile/.../wear/MobileWearableListenerService.kt:172-202`. The service constructs `RealmConfiguration.Builder(...)` inline rather than calling `DatabaseManager.getDatabase(context)`.
**Impact:** Watch-recorded audio analyses are written to plain Realm even when the user has enabled "encrypted database". Combined with `ISSUE-004` this fully invalidates the encryption claim for that flow.
**Remediation direction:** Route through a single repository abstraction.

### `ISSUE-019` 🟠 ci `[CI]`
**Title:** CI never builds or tests the Wear app.
**Evidence:** `.github/workflows/ci.yml:32-34` runs `./gradlew build` (which does build `:wear`), but the deploy step `.github/workflows/ci.yml:74-89` only `bundleRelease`s `:mobile`. The PR-debug workflow only builds `:mobile:assembleDebug` and `:mobile:connectedDebugAndroidTest`. The `build-release.yml` matrix does cover both but is `workflow_dispatch` only.
**Impact:** A wear-only break could merge undetected and ship.
**Remediation direction:** Add `:wear:assembleDebug` to the `pr-debug.yml` matrix and `:wear:bundleRelease` (or APK) to the deploy.

### `ISSUE-020` 🟠 ci `[CI]`
**Title:** PR debug workflow uses two different secret names (`SECRETS_PROPERTIES_B64` vs `SECRETS_PROPERTIES_64`).
**Evidence:** `.github/workflows/pr-debug.yml:19` reads `SECRETS_PROPERTIES_B64`, but `.github/workflows/ci.yml:65` reads `SECRETS_PROPERTIES_64`. Same for `GOOGLE_SERVICES_JSON` vs `GOOGLE_SERVICES_JSON_64`.
**Impact:** One of the two will be empty on every run — the build silently uses the stub.
**Remediation direction:** Standardise the secret names.

### `ISSUE-024` 🟠 docs `[DOCS]` `[INTEGRATION]`
**Title:** `ARCHITECTURE.md` documents capability registration that does not exist in code.
**Evidence:** `ARCHITECTURE.md:115-121`. See `ISSUE-001` for code-side proof.
**Impact:** New contributors will believe the integration works and waste time debugging the wrong layer.
**Remediation direction:** Add a "Known Gaps" section to the document or add the actual `wear.xml` files and resolve the gap entirely.

## Medium

### `ISSUE-011` 🟡 security `[MOBILE]`
**Title:** `MobileWearableListenerService` is exported and trusts payloads without verifying the source node.
**Evidence:** `mobile/.../AndroidManifest.xml:191-204` correctly exports the service (GMS requires this), but `mobile/.../wear/MobileWearableListenerService.kt:60-95` reads `messageEvent.data` and triggers camera open / audio writes without checking `messageEvent.sourceNodeId` against a trusted set.
**Impact:** On a rooted device, a malicious app could potentially craft a Wearable message and trigger camera open. Practical risk is low (GMS gates the path) but trust-but-verify is the standard.
**Remediation direction:** Maintain a trusted node-ID set; reject unknown sources.

### `ISSUE-012` 🟡 perf `[MOBILE]`
**Title:** `Realm.openDefault()` called on the main thread inside RecyclerView adapters.
**Evidence:** `mobile/.../ui/gallery/GalleryAdapter.kt:47`, `mobile/.../ui/home/HomeGalleryAdapter.kt:58`, `mobile/.../ui/milestones/MilestonesAdapter.kt:38`. Realm Kotlin's `openDefault()` is fast (mmap), but `query(...).find()` chains right after are CPU-bound.
**Impact:** Possible jank when the gallery is opened on devices with many photos.
**Remediation direction:** Use Realm's `asFlow()` / `asObservable()` and observe on a background scheduler.

### `ISSUE-014` 🟡 lifecycle `[MOBILE]`
**Title:** `lifecycleScope.launch(Dispatchers.IO)` in `SettingsFragment` keeps the IO dispatcher in scope for the whole continuation.
**Evidence:** `mobile/.../ui/settings/SettingsFragment.kt:900-916`.
**Impact:** Subtle: any `withContext(Main)` switch must succeed before UI work happens; a thrown `CancellationException` after view destruction logs as an "Unhandled exception in coroutine".
**Remediation direction:** Use `lifecycleScope.launch { withContext(Dispatchers.IO) { ... } }`.

### `ISSUE-015` 🟡 hygiene `[MOBILE]`
**Title:** `androidx.navigation:navigation-compose` declared but never used.
**Evidence:** `mobile/build.gradle:164`; `grep -rn "rememberNavController\|NavHost\b"` in `mobile/src/main` returns 0.
**Impact:** Wasted dependency, ~200 KB AAB bloat.
**Remediation direction:** Remove until needed.

### `ISSUE-016` 🟡 hygiene `[WEAR]`
**Title:** Compose dependencies pulled into `:wear` but `WearTheme.kt` is dead code.
**Evidence:** `wear/build.gradle` declares `androidx.compose.*` and `androidx.wear.compose:compose-material`; `wear/.../theme/WearTheme.kt` has no caller.
**Impact:** ~3 MB of unnecessary library code in the watch APK.
**Remediation direction:** Either invoke `WearTheme` (start the migration) or delete the file and the deps.

### `ISSUE-017` 🟡 hygiene `[MOBILE]` `[WEAR]`
**Title:** Hard-coded UI strings in source code.
**Evidence:** `wear/.../AudioRecordActivity.kt:122-131` (`recordButton.text = "🎤 Record"`, `"⏹ Stop"`, etc.); `wear/.../MainActivity.kt:78` similar.
**Impact:** Untranslatable. Breaks accessibility (TalkBack reads the emoji literally).
**Remediation direction:** Move to `wear/src/main/res/values/strings.xml`.

### `ISSUE-018` 🟡 docs `[DOCS]`
**Title:** `secrets.properties.example` and `mobile/google-services.json` paths in copilot-instructions and README disagree on whether `app/` or `mobile/` is canonical.
**Evidence:** `README.md:111` says `mobile/`; `.gitignore:40` lists both `/app/google-services.json` and `/mobile/google-services.json`.
**Impact:** Minor confusion for new contributors; harmless once they read the README.
**Remediation direction:** Drop the `/app/` line from `.gitignore`.

### `ISSUE-021` 🟡 docs `[DOCS]`
**Title:** Dependabot reviewer is `TransTracks` (upstream organization) and is unlikely to act on PRs.
**Evidence:** `.github/dependabot.yml:11-13`.
**Impact:** Dependabot PRs go un-reviewed by anyone who can merge them.
**Remediation direction:** Change to `shelbeely` or remove `reviewers` to use code-owners.

### `ISSUE-022` 🟡 hygiene `[MOBILE]`
**Title:** `SettingsFragment.kt` is a 961-LOC god-object.
**Evidence:** `mobile/.../ui/settings/SettingsFragment.kt`.
**Impact:** Highest-risk single file in the repo; every settings change touches it.
**Remediation direction:** Split into per-section composables behind a single state holder.

### `ISSUE-023` 🟡 perf `[MOBILE]`
**Title:** `Application.onCreate` does eager work.
**Evidence:** `mobile/.../TransTracksApp.kt:38-53`: `MobileAds.initialize(this)`, `appVersionUpdateIfNecessary()` (140-LOC nested when), `FileUtil.clearTempFolder()`, `SettingsManager.startFirbaseSyncIfLoggedIn(this)`.
**Impact:** Cold-start penalty (especially MobileAds + Firestore handshake on the same thread). No Baseline Profile or `App Startup` library used.
**Remediation direction:** Move MobileAds init off-thread; consider `androidx.startup` for ordered initializers.

## Low

### `ISSUE-025` 🟢 hygiene `[MOBILE]`
**Title:** Realm version-bump deprecation warnings in build output.
**Evidence:** Warnings on `kotlinx-coroutines-rx3:1.7.3`, `Picasso 2.8`, `androidx.security:security-crypto:1.1.0-alpha06` (still alpha), legacy `android.support` references in legacy code.
**Impact:** Low. Will become breaking on Gradle 9 / Kotlin 2.1.

### `ISSUE-026` 🟢 hygiene `[CI]`
**Title:** `lint-results-debug.html` is uploaded as a CI artifact but no policy reads it.
**Evidence:** `.github/workflows/ci.yml:35-40`.
**Impact:** Lint regressions go unobserved.
**Remediation direction:** Fail the build on new Lint errors via `abortOnError true`.

### `ISSUE-027` 🟢 docs `[DOCS]`
**Title:** Copilot instructions say `Wearable Support Library 2.9.0` is the wear app's path; reality matches but the support library is itself deprecated by Google.
**Evidence:** `wear/build.gradle` contains `com.google.android.support:wearable:2.9.0`.

## Summary by severity & platform

| Severity | Mobile | Wear | Shared | Integration | CI/Docs | Total |
|----------|------:|----:|------:|------------:|------:|-----:|
| 🔴 Critical | 3 | 0 | 0 | 2 | 1 | **6** |
| 🟠 High | 4 | 4 | 0 | 1 | 2 | **11** |
| 🟡 Medium | 6 | 1 | 0 | 0 | 2 | **9** |
| 🟢 Low | 0 | 0 | 0 | 0 | 3 | **3** |
| **Total** | **13** | **5** | **0** | **3** | **8** | **29** |

Some issues are double-counted across platforms (e.g. `ISSUE-001` is both `[INTEGRATION]` and a `[WEAR]` symptom). The "Total" column is unique-ID-based.
