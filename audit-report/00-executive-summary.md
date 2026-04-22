# 00 — Executive Summary

> One-page overview for a non-technical stakeholder.
> See `README.md` for the full document index.

## Headline

OpenTransition is a **functional, actively-rebranded fork of TransTracks** that has begun three large
modernisation efforts in parallel — **(a)** migrating the UI from XML Views to Jetpack Compose,
**(b)** migrating the database from Realm to encrypted Room, and **(c)** adding a Wear OS companion. **None
of these three migrations is complete**, and they overlap in ways that make the codebase look more modern
than it actually is at runtime.

The handheld app is shippable today (the upstream TransTracks heritage is mature). The Wear OS app and
the encrypted-database feature are **alpha-quality demonstrations** that should not be relied on in
production.

## Overall repo health

| Score | Tier |
|------:|------|
| **5 / 10** | Functional but with real concerns. |

Justification: the mobile app's core photo/milestone flows are robust and well-tested by years of
upstream production use. Around them, three half-finished migrations, an entirely non-functional
Wear↔mobile contract (capabilities never registered), and a Play Store CI step pointing at the wrong
`packageName` keep the overall score in the middle.

### Sub-scores

| Area | Score | Notes |
|------|------:|-------|
| 📱 Mobile app | **6 / 10** | Mature core; Compose/Room migrations half-done; one big god-object (`SettingsFragment`, 961 LOC). |
| ⌚ Wear OS app | **3 / 10** | XML-Views `Activity`s, `Compose` declared but never rendered, `WearableListenerService` is mostly empty stubs, never registers a capability. |
| 🔗 Shared module | **6 / 10** | Tiny (3 files, ~170 LOC) but tidy. Schema has no version field; uses Gson. |
| 🔁 Mobile↔Wear integration | **2 / 10** | The contract documented in `ARCHITECTURE.md` is not implemented: capabilities are never registered, mobile→wear milestone sync is a `// TODO` log line, the wear listener handlers are empty bodies. |

## Top 5 strengths

1. **Mature core photo / milestone domain** inherited from TransTracks (well-typed `Photo`, `Milestone`,
   `AudioAnalysis` models with stable `.ttbackup` JSON contracts) — `mobile/src/main/java/com/shelbeely/opentransition/data/`.
2. **Solid build hygiene at the wrapper level**: Kotlin 2.0.20, AGP 8.13.0, `compileSdk 36`, Compose BOM
   2025.05.01, KSP for Room, `coreLibraryDesugaring`, `nonTransitiveRClass=false` (intentional for legacy R),
   Gradle wrapper validation in CI — `build.gradle:1-37`, `mobile/build.gradle:32-69`, `gradle.properties:9-19`.
3. **Defence-in-depth privacy posture**: app lock with biometrics (`BiometricPromptHelper.kt`), optional
   SQLCipher database encryption with a hardware-backed `MasterKey`-protected passphrase
   (`KeystoreManager.kt:103-123`), a "decoy vault" mode, and a "quick hide" feature with `FLAG_SECURE`
   wired through `MainActivity.onAttachedToWindow` — `mobile/src/main/java/com/shelbeely/opentransition/ui/MainActivity.kt:196-205`.
4. **Thoughtful Compose theming scaffolding** with a pink/blue/purple/green palette, Material You dynamic
   colour fallback, and a separate motion-token / spring system —
   `mobile/src/main/java/com/shelbeely/opentransition/ui/theme/`.
5. **CI is not toy-grade**: instrumented tests run on a real emulator on every PR
   (`.github/workflows/pr-debug.yml:25-44`), Gradle wrapper validation, screenshot regeneration job,
   release pipeline with Play Store upload, Dependabot config present.

## Top 5 critical issues (ranked by severity)

| # | Severity | Title | Evidence |
|---|---------|-------|----------|
| 1 | 🔴 | **Wearable capability strings are never registered.** No `wear.xml` resource exists in either app and no `addLocalCapability` call is made. Every `capabilityClient.getCapability(CAPABILITY_MOBILE_APP, FILTER_REACHABLE)` returns zero nodes → all watch buttons silently no-op. | `wear/src/main/java/com/shelbeely/opentransition/wear/MainActivity.kt:101-117`, `shared/src/main/java/com/shelbeely/opentransition/shared/WearableConstants.kt:18-20`, no `wear.xml` anywhere in `wear/src` or `mobile/src`. |
| 2 | 🔴 | **Play Store deploy uploads to the wrong app.** The CI deploy step sets `packageName: com.drspaceboo.transtracks`, but the app's actual `applicationId` is `com.shelbeely.opentransition`. Either the upload fails, or it is silently overwriting the upstream TransTracks listing. | `.github/workflows/ci.yml:82-89` vs `mobile/build.gradle:36`. |
| 3 | 🔴 | **Two databases run side-by-side; almost all UI still uses Realm.** Room/SQLCipher exists (`AppDatabase.kt`, `KeystoreManager.kt`), but `HomeGalleryAdapter`, `GalleryAdapter`, `MilestonesAdapter`, `GalleryFragment`, `SinglePhotoFragment`, `RecordAudioFragment`, `MainActivity`, `SettingsFragment` all read/write directly via `Realm.openDefault()`. The "encrypted database" toggle in Settings encrypts a database that holds essentially nothing. | `grep -rn "Realm.openDefault" mobile/src/main/java` (15 hits in active UI), `mobile/src/main/java/com/shelbeely/opentransition/database/AppDatabase.kt:89` (`fallbackToDestructiveMigration` + "TODO before production"). |
| 4 | 🟠 | **`AudioAnalysisUtil` returns hard-coded fake values.** README and Settings claim "pitch/formant analysis" — the implementation returns `f0Mean = 150f`, `f0Min = 120f`, `f0Max = 180f`, etc. for every audio file. User-visible correctness defect. | `mobile/src/main/java/com/shelbeely/opentransition/util/AudioAnalysisUtil.kt:64-75`. |
| 5 | 🟠 | **`MobileWearableListenerService` opens an unencrypted Realm and writes there directly.** Bypasses `DatabaseManager`, ignores the user's encrypted-DB toggle, and the audio file itself is written to plain `filesDir`. | `mobile/src/main/java/com/shelbeely/opentransition/wear/MobileWearableListenerService.kt:144-202`. |

(The full ordered backlog is in `16-prioritized-action-plan.md`.)

## "If I only had 1 week" — fix these

Strict ROI ordering. None of these requires touching domain logic.

1. **Register wearable capabilities** by adding a `wear.xml` resource (`<string-array name="android_wear_capabilities">`) on both sides, OR call `Wearable.getCapabilityClient(this).addLocalCapability(...)` in each app's `Application.onCreate`. Single-day fix, makes the entire Wear app go from "broken" to "working". (`ISSUE-001`)
2. **Fix the Play Store `packageName`** in `.github/workflows/ci.yml:86` from `com.drspaceboo.transtracks` → `com.shelbeely.opentransition`. (`ISSUE-002`)
3. **Either implement or hide the milestone-sync mobile→wear path.** Currently `handleSyncRequest()` is a `Log.d` line and the Wear UI permanently shows "Milestones: 0". Either implement (call `WearableHelper.syncMilestones` from `handleSyncRequest`) or remove the button. (`ISSUE-003`)
4. **Decide one database.** If shipping encrypted DB: migrate the UI off `Realm.openDefault()` to `DatabaseManager.getDatabase()`. If not: hide the encrypted-DB toggle and keep Realm. The current state is the worst of both. (`ISSUE-004`)
5. **Stop shipping the fake audio-analysis numbers.** Either label them "estimated typical values" in the UI, or hide the analysis screen until a real DSP is wired in. (`ISSUE-005`)

## Maturity stage per platform

| Platform | Stage | Rationale |
|----------|-------|-----------|
| 📱 Mobile (handheld) | **Late beta / early production** | Inherited TransTracks core is production-grade; the Compose+Room+Encryption efforts are alpha and overlap with the legacy code path. |
| ⌚ Wear OS | **Prototype** | Demo-quality. Listener service is mostly empty bodies; Compose theme is dead code; capability discovery is broken; no Tiles/Complications/ambient/rotary support. |
| 🔗 Shared | **Alpha** | Three small files, no schema versioning, JSON over Gson without back-compat strategy. |
| 🔁 Integration | **Prototype** | Documented but not implemented. `ARCHITECTURE.md` describes a contract that the code does not honour. |
