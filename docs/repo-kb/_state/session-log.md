# Session Log

## 2026-04-29 21:46 — Pass 1 bootstrap

### Goal

Move the `repo-knowledge-base` skill to the location specified by the [GitHub Agent Skills docs](https://docs.github.com/en/copilot/concepts/agents/about-agent-skills), then apply the skill to seed `docs/repo-kb/`.

### Commands Run

| Command | Result |
|---|---|
| `git mv repo-knowledge-base-SKILL.md .github/skills/repo-knowledge-base/SKILL.md` | OK |
| `find . -path ./node_modules -prune -o -name SKILL.md -print` | OK — confirmed 50+ existing skills under `.github/skills/` |
| `cat settings.gradle build.gradle gradle.properties` | OK — captured Kotlin/AGP/Nav/Realm versions and version-constant scheme |
| `head` of each `.github/workflows/*.yml` | OK — captured triggers for all 9 workflows |
| `grep '^\s+(implementation\|api\|kapt\|ksp\|...)' mobile/build.gradle wear/build.gradle shared/build.gradle` | OK — captured key dependencies |

No build / test / lint commands were run in this session — pass 1 only adds documentation (no Kotlin/Gradle code changes), so the existing CI signal on this branch is unchanged.

### Files Read

- `README.md`, `ARCHITECTURE.md`, `MONOREPO.md` (root)
- `build.gradle`, `settings.gradle`, `gradle.properties`, `mobile/build.gradle`, `wear/build.gradle`, `shared/build.gradle`
- `.github/copilot-instructions.md`
- All 9 `.github/workflows/*.yml`
- The skill itself (`.github/skills/repo-knowledge-base/SKILL.md`) for templates

### Files Created or Updated

Created:

- `.github/skills/repo-knowledge-base/SKILL.md` (moved from repo root)
- `docs/repo-kb/index.md`, `repo-map.md`, `architecture.md`, `quickstart-for-agents.md`, `build-and-release.md`, `testing.md`, `configuration.md`, `security-and-risk.md`, `maintenance-guide.md`, `glossary.md`, `dependency-map.md`, `data-flow.md`
- `docs/repo-kb/{features,components,apis,data,workflows,decisions,questions,files}/index.md`
- `docs/repo-kb/_state/{progress,coverage,session-log,unknowns,command-log}.md`
- `.github/instructions/repo-kb.instructions.md`
- `AGENTS.md`

Updated:

- `.github/copilot-instructions.md` (KB pointer appended; existing rules preserved)

Removed:

- `repo-knowledge-base-SKILL.md` (repo root) — moved, not deleted

### Key Findings

- `mobile/build.gradle` actually sets `minSdkVersion 26` and `targetSdkVersion 35`, while `.github/copilot-instructions.md` describes the mobile module as `minSdk 21 / targetSdk 36`. Logged in [`unknowns.md`](./unknowns.md) and [`questions/index.md`](../questions/index.md). KB takes the source values as authoritative per the skill's "source is final authority" rule.
- App version constants are centralised in root `build.gradle` so `:mobile` and `:wear` cannot drift — important context for any future versioning change.
- The `boycott-check.yml` workflow + `.github/scripts/boycott_check.py` enforces a dependency boycott list — agents adding deps must keep this passing.

### Problems

None blocking.

### Next Recommended Action

Start pass 2: `features/photo-capture.md` (the CameraX + ML Kit pipeline is the most code-heavy area unique to this app).

---

## 2026-04-30 04:23 — Refresh + pass 3 start

### Goal

Re-apply the `repo-knowledge-base` skill: refresh pass-1 inventory against the current source, then advance one deferred item using the contract template from §"Eighth Pass: APIs, Routes, and Data Contracts".

### Commands Run

| Command | Result |
|---|---|
| `find shared/src wear/src mobile/.../wear -type f` | Discovered 3 source files in `:shared` (not 2), 4 in `wear/.../wear/`, 1 in `mobile/.../wear/` |
| `view shared/.../WearableConstants.kt`, `models/MilestoneData.kt`, `util/WearableHelper.kt` | Captured the full constant set and helper API |
| `view wear/.../WearableListenerService.kt`, `wear/src/main/AndroidManifest.xml` | Captured wear-side dispatch and intent-filter / permissions |
| `grep -E 'PATH_\|DATA_PATH_' mobile/.../MobileWearableListenerService.kt` | Captured the mobile-side dispatch table |

No build / test / lint commands were run — pass 3 only added documentation.

### Files Read

- All three `:shared` Kotlin sources + `WearableHelperTest.kt`
- `wear/src/main/java/com/shelbeely/opentransition/wear/WearableListenerService.kt`
- `wear/src/main/AndroidManifest.xml`
- `mobile/src/main/java/com/shelbeely/opentransition/wear/MobileWearableListenerService.kt`
- `.github/skills/repo-knowledge-base/SKILL.md` §"Eighth Pass: APIs, Routes, and Data Contracts" (template)

### Files Created or Updated

Created:

- `docs/repo-kb/apis/wearable-data-layer.md` — full Mobile↔Wear contract page following the skill's per-contract template (Location / Purpose / Inputs / Outputs / Validation / Auth-Security / Callers / Side Effects / Related Tests / Evidence).

Updated:

- `docs/repo-kb/repo-map.md` — corrected `:wear` and `:shared` source layouts (added `AudioRecordActivity`, `CameraControlActivity`, `theme/WearTheme.kt`, `util/WearableHelper.kt` and its test, noted mobile-side `MobileWearableListenerService`).
- `docs/repo-kb/architecture.md` — pointed module-boundaries section at the new contract page; added `WearableHelper` to the `:shared` summary.
- `docs/repo-kb/data-flow.md` — pointed the mobile↔wear flow at the new contract page and `WearableHelper`.
- `docs/repo-kb/apis/index.md` — promoted the Mobile↔Wear contract from "deferred" to ✅ documented; added a Documented? column.
- `docs/repo-kb/_state/coverage.md` — APIs row updated; Source-entrypoints note acknowledges the two listener services.
- `docs/repo-kb/_state/progress.md` — split deferred work into pass 2 (features) and pass 3 (contracts); ticked the Wearable contract.

### Key Findings

- `:shared` has **three** Kotlin sources, not two — pass 1 missed `util/WearableHelper.kt` (with JVM unit test). Now indexed.
- `:wear` has **four** Kotlin source files (`MainActivity`, `AudioRecordActivity`, `CameraControlActivity`, `WearableListenerService`) plus a Compose theme — pass 1 listed only two.
- `:mobile` has its own listener service at `mobile/src/main/java/com/shelbeely/opentransition/wear/MobileWearableListenerService.kt` mirroring the wear-side dispatch.
- The wire format includes **far more paths than pass 1 implied** — full camera-control and audio-streaming paths exist.
- The `parseMilestones` helper swallows all deserialization exceptions and returns `emptyList()` — captured as a Validation note for any future schema change.

### Problems

- I drifted from the skill template on first draft of `wearable-data-layer.md` (used my own headings) and only fixed it after the user prompted me to re-read the skill. Recorded as a process lesson: **always re-open `SKILL.md` at the start of any session that touches the KB**.

### Next Recommended Action

Pick the next pass-2 / pass-3 item from `progress.md`. Lowest-risk, highest-value next: `data/room-entities.md` — Room schemas are already exported under `mobile/schemas/`, which gives a strict source of truth.

---

## 2026-04-30 — Features pass: photo-capture, import, app-lock

### Goal

Run the `repo-knowledge-base` skill's "Fifth Pass: Features" template against three concrete OpenTransition features: the CameraX + ML Kit photo-capture pipeline, the TransTracks `.ttbackup` import (and its sister `RealmBackupImporter`/`RealmToRoomMigration` flow), and the app-lock + decoy-vault path.

### Commands Run

| Command | Result |
|---|---|
| `find mobile/src/main/java/com/shelbeely/opentransition -type f` | OK — enumerated 163 source files |
| `grep -rln 'CameraX\|ImageCapture\|FaceDetect' mobile/src/main/java/...` | OK — five files surfaced |
| `grep -rn 'ttbackup\|RealmBackupImporter\|RealmToRoomMigration' mobile/src/main/` | OK — manifests, MainActivity, settings flow, importer all located |
| `grep -rln 'BiometricPrompt\|LockType\|decoy\|vault' mobile/src/main/java/...` | OK — lock fragment, settings, DatabaseManager, KeystoreManager located |
| `view` (multiple) on the Kotlin sources for the three features | OK |

No build / test / lint commands were run — this was a documentation pass; no Kotlin/Gradle code was modified.

### Files Read

- `mobile/src/main/java/com/shelbeely/opentransition/ui/camera/CameraFragment.kt`
- `mobile/src/main/java/com/shelbeely/opentransition/background/CameraXHandler.kt`
- `mobile/src/main/java/com/shelbeely/opentransition/background/FaceDetectionAnalyzer.kt`
- `mobile/src/main/java/com/shelbeely/opentransition/ui/widget/CameraOverlayView.kt`
- `mobile/src/main/java/com/shelbeely/opentransition/util/FileUtil.kt`
- `mobile/src/main/java/com/shelbeely/opentransition/domain/AssignPhotosDomain.kt` (relevant slice)
- `mobile/src/main/java/com/shelbeely/opentransition/ui/MainActivity.kt` (`processIntent`, `processImport`, lock lifecycle)
- `mobile/src/main/java/com/shelbeely/opentransition/database/migration/RealmBackupImporter.kt`
- `mobile/src/main/java/com/shelbeely/opentransition/database/migration/RealmToRoomMigration.kt`
- `mobile/src/main/java/com/shelbeely/opentransition/ui/settings/SettingsFragment.kt` (import-backup picker)
- `mobile/src/main/java/com/shelbeely/opentransition/util/RealmConfigurations.kt`
- `mobile/src/main/java/com/shelbeely/opentransition/OpenTransitionApp.kt` (migration trigger)
- `mobile/src/main/AndroidManifest.xml` (`.ttbackup` intent filters, CAMERA permission)
- `mobile/src/main/java/com/shelbeely/opentransition/ui/lock/{LockFragment,LockUi,LockScreen}.kt`
- `mobile/src/main/java/com/shelbeely/opentransition/util/BiometricPromptHelper.kt`
- `mobile/src/main/java/com/shelbeely/opentransition/util/settings/SettingsManager.kt` (`LockType`, `LockDelay`, decoy + lock-code accessors)
- `mobile/src/main/java/com/shelbeely/opentransition/database/{DatabaseManager,AppDatabase,KeystoreManager}.kt`
- `.github/skills/repo-knowledge-base/SKILL.md` §"Fifth Pass: Features" (template)

### Files Created or Updated

Created:

- `docs/repo-kb/features/photo-capture.md` — Camera fragment + `CameraXHandler` + `FaceDetectionAnalyzer` + `CameraOverlayView`, hand-off to `AssignPhotosDomain`.
- `docs/repo-kb/features/import.md` — `.ttbackup` zip path through `MainActivity.processImport` (writes legacy Realm), Settings-driven `.realm` path through `RealmBackupImporter` (writes Room), and the in-place `RealmToRoomMigration`.
- `docs/repo-kb/features/app-lock.md` — `LockFragment`/`LockScreen`, lock delay, biometric path, real/decoy vault switching via `DatabaseManager`, and the SQLCipher passphrase storage in `KeystoreManager` (key insight: SQLCipher passphrase is *not* derived from the user PIN).

Updated:

- `docs/repo-kb/features/index.md` — added "Documented" markers for the three new pages, corrected the row that previously said "PIN / pattern / biometric" (no pattern lock exists in source), and rewrote the next-pass note.
- `docs/repo-kb/_state/coverage.md` — Features row updated to reflect the three new pages while remaining "Partial".
- `docs/repo-kb/_state/unknowns.md` — appended four new entries: front-camera-only default, missing tests for the three features, disguised-launcher prose-only coverage, and the absence of a "pattern" lock type.

### Key Findings

- The `.ttbackup` import in `MainActivity.processImport` writes into the **legacy Realm DB** (`Realm.openDefault()` + `copyToRealm(...)`), not Room. The new Room database only sees the data once `RealmToRoomMigration` runs, which is gated on the user enabling the encrypted database in Settings. This is a non-obvious ordering dependency worth flagging.
- The Settings-flow "Import Backup" expects a *raw* `.realm` file (not a `.ttbackup` zip). It opens that file inside `cacheDir` with a fresh `RealmConfiguration` and reads only — `RealmBackupImporter` calls `realm.query(...)` exclusively, never `write`.
- `KeystoreManager` mints a 256-bit `SecureRandom` passphrase per vault and stores it in `EncryptedSharedPreferences("opentransition_db_keys")`. The user's PIN does not factor into SQLCipher key derivation — the PIN only gates which passphrase the SQLCipher layer is asked to use. This contradicts the Features-pass brief's hypothesis ("likely SQLCipher passphrase derivation per `ENCRYPTED_DATABASE.md`") and is now documented in `features/app-lock.md` §"Vault switching mechanics".
- `LockType` enum has no `pattern` value — only `off`/`normal`/`trains`/`biometric`. Logged in `_state/unknowns.md`.
- `LockFragment.showBiometricPrompt` carries a load-bearing comment about Navigation 2.8.x silently dropping `popBackStack()` when `FragmentManager.isStateSaved == true`. The fix is `lifecycle.withStarted { ... }`. Documented in `app-lock.md` §"Failure Modes" and §"How to Modify Safely" so future agents do not regress it.
- A non-fatal Crashlytics path still exists for users whose lock codes were saved with the example salt accidentally shipped previously. Documented in `app-lock.md` §"Password / train path" step 4.

### Problems

None blocking. No source code was modified; no builds were attempted.

### Next Recommended Action

Continue the Features pass for the remaining inventory in `features/index.md` — milestone management is the highest-value next page (cross-cuts `:mobile` and `:wear` and ties in with the already-documented `apis/wearable-data-layer.md`). After that, the disguised-launcher feature should get its own page (it is currently only mentioned in passing in `app-lock.md`).

## 2026-04-30 — Data + Components pass

### Goal

Run the **Data + Components pass** of the `repo-knowledge-base` skill: enumerate Room entities/DAOs/migrations, the legacy Realm schema, and produce a top-level components map.

### Files Read

- `docs/repo-kb/index.md`, `architecture.md`, `data/index.md`, `components/index.md`, `_state/coverage.md`, `_state/unknowns.md`, `_state/session-log.md`
- `.github/skills/repo-knowledge-base/SKILL.md` (Eighth Pass + Fourth Pass templates)
- `ENCRYPTED_DATABASE.md`
- `mobile/src/main/java/com/shelbeely/opentransition/database/AppDatabase.kt`
- All four entities under `mobile/.../database/room/entities/`
- All four DAOs under `mobile/.../database/room/dao/`
- Legacy Realm models: `mobile/.../data/{Photo,Milestone,AudioAnalysis}.kt` and `util/RealmConfigurations.kt`, `util/Realms.kt`
- `mobile/src/main/AndroidManifest.xml` and `wear/src/main/AndroidManifest.xml` (services / providers / receivers)
- Component inventory via `find {mobile,wear,shared}/src/main -type f \( -name '*Activity.kt' -o -name '*Fragment.kt' -o -name '*Service.kt' -o -name '*Worker.kt' -o -name '*Receiver.kt' -o -name '*Screen.kt' \)`

No source files under `mobile/`, `wear/`, `shared/` were modified. No gradle commands were run.

### Files Created

- `docs/repo-kb/data/room-entities.md` — `AppDatabase` v3, four entities (`milestones`, `photos`, `audio_analysis`, `voice_goals`), every DAO query summarised by purpose, both migrations (`MIGRATION_1_2`, `MIGRATION_2_3`) including DDL summary, the SQLCipher `SupportFactory` integration point, and links to the exported schema JSON files.
- `docs/repo-kb/data/realm-schema.md` — Read-only Realm models (`Photo`, `Milestone`, `AudioAnalysis`) with their fields, the single `RealmConfiguration.default`, the read-only enforcement evidence, and a link (not a duplicate) to `features/import.md` for the ingestion flow.

### Files Updated

- `docs/repo-kb/components/index.md` — Replaced the placeholder with a real top-level components map: `OpenTransitionApp`, `MainActivity`, ~17 Fragment+Screen pairs, `MobileWearableListenerService`, `TransTracksFileProvider`, three Wear Activities, `WearableListenerService`, and the three `:shared` files. No per-component pages.
- `docs/repo-kb/data/index.md` — Linked the two new pages; reworded the next-pass note.
- `docs/repo-kb/_state/coverage.md` — Data row updated; Components row → "Partial (index only)"; struck off the `room-entities.md` task.
- `docs/repo-kb/_state/unknowns.md` — Appended "Realm vs. Room field drift" entry.

### Key Findings

- `AppDatabase` declares **no** type converters, foreign keys, or indices. The `AudioAnalysisEntity.photoId` ↔ `PhotoEntity.id` link is logical only (not enforced by SQLite). Worth knowing before adding any SQL JOIN that assumes referential integrity.
- Two migrations only (1→2 adds eight extended audio metric columns + `sessionSummaryText`; 2→3 creates `voice_goals`). Both are pure DDL — no row-level data movement. Schema export skips `2.json` (only `1.json` and `3.json` are checked in under `mobile/schemas/`).
- SQLCipher integration is a **single** call site: `Room.databaseBuilder(...).openHelperFactory(SupportFactory(passphrase))` inside `AppDatabase.buildDatabase`, gated on `SettingsManager.isEncryptedDatabaseEnabled()`. Per-vault passphrase comes from `KeystoreManager` (already documented in `features/app-lock.md`).
- Realm `AudioAnalysis` has a `transcript: String` field; `AudioAnalysisEntity` does not. Logged in `_state/unknowns.md` as "Realm vs. Room field drift".
- Components inventory is highly uniform: 17 `*Fragment` + `*Screen.kt` pairs, one outlier (`CameraFragment` — still XML-based, no `Screen.kt`). The mobile app has **zero** `Worker`s and **zero** `BroadcastReceiver`s; the only `Service` is `MobileWearableListenerService`. Wear has `WearableListenerService` plus three Activities and no other components. `:shared` is three `.kt` files and zero runtime components.

### Problems

None blocking. No source code modified; no commits or pushes performed.

### Next Recommended Action

Continue Features pass on remaining items in `features/index.md` (milestones management is highest-value cross-module candidate). Optionally: create a small `apis/firestore.md` once the cloud-sync surface is mapped, and a `components/` per-pair page only for the screens whose ViewModels carry non-trivial business logic (the AudioAnalysis pipeline is the leading candidate).

## 2026-04-30 — Validation + finalization pass

### Goal

Resolve the long-open SDK-targets question, re-validate documented agent-safe Gradle commands inside the Copilot Cloud Agent runner, and update KB state files (`unknowns.md`, `questions/index.md`, `command-log.md`, `coverage.md`).

### Commands Run

| Command | Result | Time |
|---|---|---|
| `./gradlew :mobile:lintDebug --no-daemon --console=plain` | **Failed** — 2 errors, 190 warnings (pre-existing baseline; first failure `HomeScreen.kt:133` `LocalContextGetResourceValueCall`) | 3m 51s |
| `./gradlew :mobile:testDebugUnitTest --no-daemon --console=plain` | Passed | 35s |
| `./gradlew :mobile:assembleDebug --no-daemon --console=plain` | Passed | 2m 45s |
| `./gradlew test --no-daemon --console=plain` | Passed | 1m 37s |

Logs captured under `audit-report/runtime-artifacts/2026-04-30/{lintDebug,testDebugUnitTest,assembleDebug,test}.log`.

### Files Updated

- `.github/copilot-instructions.md` — "Repository layout" `:mobile` row updated from `(minSdk 21, targetSdk 36)` to `(minSdk 26, targetSdk 35, compileSdk 36)`; "What NOT to do" SDK-floor line updated from `minSdkVersion below 21` to `minSdkVersion below 26` (source = truth per `mobile/build.gradle`).
- `docs/repo-kb/_state/unknowns.md` — SDK-targets section marked Resolved 2026-04-30.
- `docs/repo-kb/questions/index.md` — Question marked Resolved 2026-04-30, with the resolution recorded.
- `docs/repo-kb/_state/command-log.md` — Four command entries appended (one Failed, three Passed).
- `docs/repo-kb/_state/coverage.md` — Next-Pass items 6 and 7 ticked; new item 9 added for the lint baseline follow-up.

### Key Findings

- `:mobile:assembleDebug` runs in under 3 minutes in the Cloud Agent runner — well within the 8-minute time-box.
- `:mobile:testDebugUnitTest` and `./gradlew test` are clean; the JVM unit-test suite is healthy across `:mobile`, `:wear`, `:shared`.
- `:mobile:lintDebug` is **not** clean: 2 errors + 190 warnings exist in `main` today. The documented "run lint before opening a PR" guidance should expect this baseline until the `LocalContextGetResourceValueCall` errors in `HomeScreen.kt` are addressed.
- No source under `mobile/`, `wear/`, `shared/` was modified; the SDK reconciliation was a docs-only change because the source-derived values (`minSdk 26`, `targetSdk 35`, `compileSdk 36`) are authoritative per the repo-kb rule.

### Issues Encountered

None blocking. No commits, no pushes.

### Next Recommended Action

Fix the 2 pre-existing `:mobile:lintDebug` errors so the documented pre-PR lint gate is green again, then continue the Features pass on milestones / disguised launcher / audio tracking / encrypted-DB standalone / Wear capture+milestones (per `coverage.md` Next-Pass items 8–9).
