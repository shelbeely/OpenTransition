# Unknowns

Open items the source did not fully answer during the pass-1 KB build. These are not bugs — they are documentation gaps.

## SDK targets disagree between docs and source

**Resolved 2026-04-30.**

`mobile/build.gradle` is the source of truth: `minSdkVersion 26`, `targetSdkVersion 35`, `compileSdk 36`. The "Repository layout" table in [`.github/copilot-instructions.md`](../../../.github/copilot-instructions.md) and the "What NOT to do" floor (previously `minSdk 21`, `targetSdk 36`) have been updated to `(minSdk 26, targetSdk 35, compileSdk 36)` and `minSdkVersion below 26 or targetSdkVersion above 36` respectively. The KB [`architecture.md`](../architecture.md) already recorded the source-derived values; mirror page [`questions/index.md`](../questions/index.md) updated to mark resolved.

## Pass-2 scope is documented but not yet executed

Per-feature, per-entity, and per-file pages are intentionally deferred (see [`progress.md`](./progress.md)). They are not "unknown" in the sense of contradicting the source — they are simply not yet documented.

## CI secret name list may be incomplete

[`configuration.md`](../configuration.md) lists the well-known secrets (`KEYSTORE_64`, `STORE_PASS`, `KEY_ALIAS`, `KEY_PASS`, plus the secrets read by `prepare-secrets.sh` / `prepare-google-services.sh`). The pass-1 build did not exhaustively read every `env:` block in every workflow; future passes should diff `.github/ci-scripts/*.sh` against the workflow `env:` declarations to confirm completeness.

## Camera selector is hard-coded to front-facing

`CameraXHandler(useFrontCamera = true)` is instantiated by `CameraFragment` without surfacing a UI toggle. `setFaceDetectionEnabled` exists but is never called from the UI (only the *overlay* visibility is toggled). Captured by [`features/photo-capture.md`](../features/photo-capture.md). It is unclear whether this is intentional (selfie-first journal) or a missing feature. Source of truth: `mobile/src/main/java/com/shelbeely/opentransition/ui/camera/CameraFragment.kt`.

## No automated tests located for the photo-capture, import, or app-lock paths

A `find mobile/src/test mobile/src/androidTest -name '*.kt'` did not surface tests for `CameraFragment`/`CameraXHandler`/`FaceDetectionAnalyzer`, `RealmBackupImporter`/`MainActivity.processImport`, or `LockFragment`/`BiometricPromptHelper`. The three feature pages flag this in their Implementation Map. A future testing pass should confirm whether tests exist elsewhere (e.g. instrumented suites not enumerated here) or file follow-up work in [`DEFERRED_WORK.md`](../../../DEFERRED_WORK.md).

## Disguised launcher icon is documented in prose only

`README.md` describes a disguised launcher icon, and `AndroidManifest.xml` declares an `activity-alias` (`MainActivityDefault`). The full set of aliases and the toggle code path were not enumerated for [`features/app-lock.md`](../features/app-lock.md), which only covers the lock-screen "trains" disguise. A standalone feature page is deferred.

## Lock screen has no "pattern" lock type

The pass-2 brief mentioned "pattern" as one of the lock types. The source `LockType` enum in `SettingsManager.kt` only defines `off`, `normal`, `trains`, and `biometric`. The KB ([`features/app-lock.md`](../features/app-lock.md)) records the source-derived set as authoritative.

## Realm vs. Room field drift

The legacy Realm `AudioAnalysis` (`mobile/src/main/java/com/shelbeely/opentransition/data/AudioAnalysis.kt`) carries a `transcript: String` field that is **not** present on the Room twin `AudioAnalysisEntity`. Either (a) the transcript is intentionally not persisted long-term and only used in-session, or (b) the Room migration is incomplete. Source authoritative; flagged for a maintainer to confirm intent. Recorded by [`data/room-entities.md`](../data/room-entities.md) and [`data/realm-schema.md`](../data/realm-schema.md).
