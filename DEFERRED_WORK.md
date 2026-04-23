# Deferred Work — Play Store Deployment Readiness

> **Context:** This document captures all out-of-scope changes identified during the
> *Play Store deployment readiness* sprint (branch `copilot/prepare-google-play-deployment`,
> PR #86 + follow-up quality polish). Every item here was intentionally deferred rather than
> forgotten. Pick up from this list for the next sprint.
>
> Source material: `audit-report/07-issues-and-bugs.md` and
> `audit-report/16-prioritized-action-plan.md`.

---

## Issues fixed in this sprint (for reference)

| Issue | Title | Status |
|-------|-------|--------|
| ISSUE-001 | Register Wearable capabilities (`wear.xml`) | ✅ Done |
| ISSUE-002 | CI Play Store package name | ✅ Done |
| ISSUE-003 | `handleSyncRequest` mobile→wear milestone sync | ✅ Done |
| ISSUE-004 | Hide misleading "Encrypt my database" toggle | ✅ Done (toggle hidden) |
| ISSUE-005 | Audio analysis labeled as estimated averages | ✅ Done |
| ISSUE-006 | Room schema export + remove `fallbackToDestructiveMigration` | ✅ Done |
| ISSUE-007 | `WearableListenerService` handler bodies | ✅ Done |
| ISSUE-009 | Handler leak in `AudioRecordActivity` | ✅ Done |
| ISSUE-010 | Audio shipped via `ChannelClient` instead of `DataClient` | ✅ Done |
| ISSUE-011 | Source-node validation in `MobileWearableListenerService` | ✅ Done |
| ISSUE-015 | Remove unused `navigation-compose` dependency | ✅ Done |
| ISSUE-017 | Hard-coded UI strings moved to `strings.xml` | ✅ Done |
| ISSUE-019 | CI builds and deploys Wear app | ✅ Done |
| ISSUE-020 | CI secret name drift corrected | ✅ Done |
| ISSUE-021 | Dependabot reviewer updated to `shelbeely` | ✅ Done |
| ISSUE-023 | `Application.onCreate` eager work moved off main thread | ✅ Done |
| ISSUE-024 | `ARCHITECTURE.md` / `WEAR_APP_FEATURES.md` Known Gaps sections | ✅ Done |
| ISSUE-026 | `lint { abortOnError true }` added to CI | ✅ Done |

---

## Deferred issues (NEXT — 1 month horizon)

### ISSUE-008 🟠 — Watch milestone cache only updates while `MainActivity` is foreground
**File:** `wear/.../MainActivity.kt:134-140`
`dataClient.addListener` is registered in `onResume` and removed in `onPause`. The background
`WearableListenerService` now processes `DATA_PATH_MILESTONES` but does not persist received data
to a local cache.
**To do:** Write received milestones into Wear **DataStore** inside `handleMilestoneSync`
so they are available when the main activity is not running (e.g., from a Tile or notification).
**Skill:** `android-data-layer`, `android-coroutines`

---

### ISSUE-012 🟡 — `Realm.openDefault()` on main thread inside RecyclerView adapters
**Files:** `GalleryAdapter.kt:47`, `HomeGalleryAdapter.kt:58`, `MilestonesAdapter.kt:38`
Realm `query(...).find()` chains are called synchronously on the main thread.
**To do:** Switch to `asFlow()` / `asObservable()` and observe on a background scheduler to avoid
jank on devices with many photos.
**Skill:** `kotlin-concurrency-expert`, `android-coroutines`

---

### ISSUE-013 🟠 — `MobileWearableListenerService` writes audio to bare Realm, bypassing `DatabaseManager`
**File:** `mobile/.../wear/MobileWearableListenerService.kt`
Audio metadata received from the watch is still written via an inline `RealmConfiguration.Builder`
rather than through `DatabaseManager.getDatabase(context)`.
**To do:** Route through `DatabaseManager` (or the planned Repository abstraction) so the write
respects the encrypted-DB toggle once that feature is completed.
**Skill:** `android-data-layer`

---

### ISSUE-014 🟡 — `SettingsFragment` `lifecycleScope.launch(Dispatchers.IO)` pattern
**File:** `mobile/.../ui/settings/SettingsFragment.kt:900-916`
IO dispatcher kept in scope for the full continuation; UI work after suspension risks a logged
`CancellationException` if the view is destroyed.
**To do:** Replace with `lifecycleScope.launch { withContext(Dispatchers.IO) { … } }`.
**Skill:** `kotlin-concurrency-expert`

---

### ISSUE-016 🟡 — Dead `WearTheme.kt` + unused Compose deps in `:wear` (~3 MB APK bloat)
**Files:** `wear/build.gradle`, `wear/.../theme/WearTheme.kt`
183-LOC Wear Compose theme is fully written but has zero callers. The Compose deps are pulled in
for it.
**To do (option A):** Start the Wear Compose migration and wire `WearTheme` into at least
`MainActivity`.
**To do (option B — quick win):** Delete `WearTheme.kt` and remove the `androidx.compose.*` /
`androidx.wear.compose:compose-material` deps until the migration actually begins.
**Skill:** `migrate-xml-views-to-jetpack-compose`, `compose-ui`

---

### ISSUE-018 🟡 — `.gitignore` contains both `/app/google-services.json` and `/mobile/google-services.json`
**File:** `.gitignore:40`
The `/app/` path is a legacy upstream artifact; the canonical location is `/mobile/`.
**To do:** Remove the `/app/google-services.json` line.

---

### play-services-auth stuck at 20.7.0
**File:** `mobile/build.gradle`
`com.google.android.gms:play-services-auth:20.7.0` was intentionally **not** upgraded because
version 21.x requires `minSdk 23`. The current `minSdkVersion` is 21.
**To do:** Raise `minSdkVersion` to 23 (dropping ~1.5 % of devices per current Android distribution)
and then upgrade to `play-services-auth:21.x`. Coordinate with the `firebase-ui-auth` migration
below.
**Constraint:** Project rules prohibit changing `minSdkVersion` without discussion.

---

### firebase-ui-auth migration to FirebaseAuth + Credential Manager
**File:** `mobile/src/main/java/.../ui/settings/SettingsFragment.kt` and related auth code
`firebase-ui-auth` is archived / unmaintained. Migrating to the official
`FirebaseAuth` SDK + Android Credential Manager is a multi-step refactor:
1. Replace `AuthUI.getInstance().signInIntent` flow with a Credential Manager `GetCredentialRequest`.
2. Update all sign-in result handlers.
3. Remove the `firebase-ui-auth` and `firebaseui` dependencies.
**Skill:** `android-architecture`

---

### SQLCipher upgrade to 4.6+ (16 KB page alignment)
**File:** `mobile/build.gradle`
Current `net.zetetic:android-database-sqlcipher:4.5.4` does not support 16 KB page alignment
required for Android 15 (API 35+) devices with 16 KB page size.
**To do:** Upgrade to `4.6.1` or later and verify the Room integration.
**Skill:** `android-gradle-logic`

---

### Coil — consolidate image loading (remove Picasso)
**Files:** Various adapters
Both Picasso and Coil are declared as dependencies. Picasso (`2.8`) is deprecated.
**To do:** Replace all `Picasso.get().load(…)` calls with `AsyncImage` (Compose) or
`coilload { … }` (Views), then remove the Picasso dependency.
**Skill:** `coil-compose`

---

### Wear `PARTIAL_WAKE_LOCK` during audio recording
**File:** `wear/.../AudioRecordActivity.kt`
The watch screen can turn off mid-recording, interrupting capture.
**To do:** Acquire a `PARTIAL_WAKE_LOCK` (or use `ForegroundService`) for the duration of recording
and release it in `stopRecording()`.
**Skill:** `android-coroutines`

---

### `CoroutineExceptionHandler` baseline
**Files:** `TransTracksApp.kt` and key `CoroutineScope` sites
Unhandled coroutine exceptions are currently logged by the default handler only.
**To do:** Install a project-wide `CoroutineExceptionHandler` that routes uncaught exceptions to
Crashlytics (when enabled) and logs them at `ERROR` level.
**Skill:** `kotlin-concurrency-expert`

---

### `WearableHelper` round-trip unit tests in `:shared`
**File:** `shared/.../util/WearableHelper.kt`
No tests cover serialization/deserialization of `MilestoneData` payloads over the Data Layer.
**To do:** Add JUnit tests exercising `syncMilestones` → `parseMilestoneData` round-trips with
edge cases (empty list, max-size list, Unicode milestone titles).
**Skill:** `android-testing`

---

### Adopt Detekt + ktlint
**Files:** Root `build.gradle`
No static analysis beyond Android Lint is configured.
**To do:** Add `detekt` and `ktlint` Gradle plugins; enable `detekt` in CI as a required check.
**Skill:** `android-gradle-logic`

---

### Adopt Gradle Version Catalog (`libs.versions.toml`)
**Files:** `build.gradle`, `mobile/build.gradle`, `wear/build.gradle`
Dependencies and versions are declared inline with string interpolation, making cross-module
version alignment error-prone.
**To do:** Migrate to a `gradle/libs.versions.toml` version catalog.
**Skill:** `android-gradle-logic`

---

### Pre-allocate `Paint`/`Path` in `FormantChartView`
**File:** `mobile/.../ui/widget/FormantChartView.kt`
`DrawAllocation` lint warning: `Paint` and `Path` objects are allocated inside `onDraw`.
**To do:** Hoist allocations to field-level initialisation.
**Skill:** `compose-performance-audit`

---

### Add `CONTRIBUTING.md` and Wear dev-setup guide
**Files:** repo root
No contributor guide exists explaining how to set up the Wear emulator pairing, run
instrumented tests, or work on the `:shared` module.
**To do:** Create `CONTRIBUTING.md` with build setup, coding conventions, and PR checklist.

---

### Remove `/app/` path from `.gitignore`
**File:** `.gitignore`
The legacy `/app/google-services.json` gitignore entry is a holdover from the upstream
TransTracks repo (which used an `app/` module). It is harmless but misleading.
**To do:** Delete that line.

---

## Deferred issues (LATER — quarter+ horizon)

| # | Title | Issue / Reference | Skill |
|---|-------|-------------------|-------|
| 34 | Split `SettingsFragment.kt` (961 LOC) | ISSUE-022 | `android-architecture` |
| 35 | Migrate mobile XML Activities → Compose (single-Activity host) | audit-report/03 | `migrate-xml-views-to-jetpack-compose`, `navigation-3` |
| 36 | Migrate Wear UI from Views → Wear Compose (use `WearTheme`) | ISSUE-016 | `migrate-xml-views-to-jetpack-compose` |
| 37 | Add Wear Tile + Complication | audit-report/04 | — |
| 38 | Real DSP audio analysis (LPC / cepstrum) — replace fake formant numbers | ISSUE-005 | — |
| 39 | Complete Room/SQLCipher migration — route all Realm reads through Repository | ISSUE-004 | `android-data-layer` |
| 40 | Complete decoy-vault UI flow | audit-report/08 | `android-architecture` |
| 41 | Add Wear ambient mode / always-on support | audit-report/04 | — |
| 42 | Add Wear rotary input support | audit-report/04 | — |
| 43 | Add Baseline Profile + Macrobenchmark | audit-report/11 | `compose-performance-audit` |
| 44 | End-to-end RxJava → Coroutines + Flow migration | audit-report/02 | `rxjava-to-coroutines-migration` |
| 45 | Add screenshot tests (Roborazzi) | audit-report/12 | `android-testing` |
| 46 | Adopt Hilt or Koin (or formalise manual DI) | audit-report/02 | `android-architecture` |
| 47 | F-Droid `foss` flavor (drop AdMob / Crashlytics / Firebase) | audit-report/15 | `r8-analyzer` |
| 48 | Dependency lockfile + verification metadata | audit-report/14 | `android-gradle-logic` |
| 49 | Move color palette + date helpers to `:shared` | audit-report/09 | `android-architecture` |
| 50 | In-app review prompt after 5 milestones | `MainActivity.kt:198` TODO | — |
| 51 | Password recovery via Firebase | `SettingsFragment.kt:432` TODO | — |
| 52 | Encrypt settings JSON before Firestore write | `SettingsManager.kt:392` TODO | — |
| 53 | Auto-attach audio to today's date if photos exist | `RecordAudioFragment.kt:201` TODO | — |
| 54 | Decoy-vault export | `SettingsFragment.kt:776` TODO | — |
| 55 | Wire `PATH_CAMERA_SHUTTER/_ZOOM/_FLASH/_SWITCH` on mobile side | audit-report/08 §data-layer | `android-data-layer` |
| 56 | Wire `DATA_PATH_SETTINGS` producer + consumer | audit-report/08 §data-layer | `android-data-layer` |
| 57 | Wire `PATH_MILESTONE_UPDATE` producer (mobile side) | audit-report/08 §data-layer | `android-data-layer` |
| 58 | Replace `CameraHandler` with `CameraX VideoCapture` | `CameraHandler.kt:88` TODO | — |
| 59 | Replace Wearable Support Library 2.9.0 (deprecated by Google) | ISSUE-027 | `android-gradle-logic` |
| 60 | Surface migration progress in Settings UI | `SettingsFragment.kt:558` TODO | — |

---

## Wearable Data Layer path completeness (current state after this sprint)

| Path | Producer | Consumer | Status after sprint |
|------|----------|----------|---------------------|
| `PATH_TRIGGER_PHOTO` | wear ✅ | mobile ✅ | **Functional** (capability now registered) |
| `PATH_REQUEST_SYNC` | wear ✅ | mobile ✅ | **Functional** (implemented in this sprint) |
| `PATH_MILESTONE_UPDATE` | mobile ❌ | wear ✅ (service) | Dead — producer not yet wired |
| `PATH_CAMERA_SHUTTER/_ZOOM/_FLASH/_SWITCH` | wear ✅ | mobile ❌ | Dead — mobile listener missing |
| `DATA_PATH_MILESTONES` | mobile ✅ (helper) | wear ✅ (service, no DataStore) | Partially functional — cache not persisted |
| `DATA_PATH_SETTINGS` | mobile ❌ | wear ❌ | Dead both sides |
| `DATA_PATH_AUDIO` | wear ✅ (`ChannelClient`) | mobile ✅ | **Functional** |

---

*Last updated: 2026-04-23 — sprint: Play Store deployment readiness*
