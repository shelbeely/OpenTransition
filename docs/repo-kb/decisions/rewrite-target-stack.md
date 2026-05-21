# Rewrite Target Stack

> Proposal for what the next-generation OpenTransition should be built on, drawn
> from the current-state evidence in Phases 1–5 of this knowledge-base refresh.
> This is **not** a commitment to rewrite; it is a documented set of options to
> evaluate. Every "remove X" or "keep Y" claim cites the page that justifies it.

## Constraints (non-negotiable)

These are the invariants the rewrite **must** preserve. They come from the
feature pages and the upstream diff:

1. **`.ttbackup` round-trip**, including with archived-upstream backups. See [`data/ttbackup-format.md`](../data/ttbackup-format.md).
2. **No gendered defaults in voice features.** User-defined goals only. See [`features/audio-tracking.md`](../features/audio-tracking.md), [`VoiceGoalEntity.kt:18`](../../../mobile/src/main/java/com/shelbeely/opentransition/database/room/entities/VoiceGoalEntity.kt).
3. **Wear path strings** (`/opentransition/...`) and capability names (`opentransition_wear_app`) are wire-format. See [`apis/wearable-data-layer.md`](../apis/wearable-data-layer.md).
4. **GPL-3.0-or-later** license — both upstream and OpenTransition.
5. **`minSdk = 21`** unless explicitly raised by user decision.
6. **App-private storage only** — no photo or audio bytes leave the device by default. See [`data/file-layout.md`](../data/file-layout.md) and [`apis/firebase-contracts.md`](../apis/firebase-contracts.md).
7. **`decoyLockCode` is never mirrored to Firestore** — see [`apis/firebase-contracts.md`](../apis/firebase-contracts.md).
8. **Hashed lock codes only** at rest — see [`features/app-lock.md`](../features/app-lock.md). Plaintext storage is a regression.

## Recommended stack

| Layer | Recommendation | Why |
|---|---|---|
| Build | AGP 8.x, Kotlin 2.0+, KSP (no kapt), Convention Plugins, Version Catalogs | Already in flight ([`.github/copilot-instructions.md`](../../copilot-instructions.md)); upstream is on AGP 8.1.2 / Kotlin 1.9.10 ([upstream `build.gradle`](https://github.com/TransTracks/TransTracks-Android/blob/f8560a1aa643e06fa9cfb9dd7c8b4bcbf802f5c9/build.gradle)). |
| Module shape | Keep three modules (`:mobile`, `:wear`, `:shared`); add a `:core-database` and `:core-domain` for Compose-native rewrite | Existing split is justified by `apis/wearable-data-layer.md`; the shared module already isolates the wire format. |
| UI | **Single-target Jetpack Compose** + Material 3 + Compose-Navigation 3 | Phase 2 pages show every screen already has a `*Screen.kt` Compose counterpart; XML+ViewBinding is being phased out. Skill: [`migrate-xml-views-to-jetpack-compose`](../../skills/migrate-xml-views-to-jetpack-compose/SKILL.md). |
| State | **Kotlin Coroutines + Flow** end-to-end; ViewModels with `StateFlow` for UI and `SharedFlow` for events | Current code mixes RxJava with `kotlinx-coroutines-rx3`. Skill: [`rxjava-to-coroutines-migration`](../../skills/rxjava-to-coroutines-migration/SKILL.md). |
| DI | **Hilt** | Today's code passes singletons via `OpenTransitionApp.instance.domainManager`. Hilt removes the global app reference. Skill: [`android-architecture`](../../skills/android-architecture/SKILL.md). |
| Local persistence | **Room only** (drop Realm) | Phase 3 confirmed Realm is used only by import + a handful of legacy adapters + one Wear write path — see [`data/realm-models.md`](../data/realm-models.md). |
| Encrypted persistence | **SQLCipher via Room** (`androidx.sqlite` + `net.zetetic:sqlcipher-android`) | Mechanism already wired in [`AppDatabase.kt:129-134`](../../../mobile/src/main/java/com/shelbeely/opentransition/database/AppDatabase.kt). Toggle stays off until Realm is gone — see [`features/encrypted-database.md`](../features/encrypted-database.md). |
| Networking | None beyond Firebase | Phase 4 confirmed no HTTP client is used. See [`apis/firebase-contracts.md`](../apis/firebase-contracts.md). |
| Camera | **CameraX** only | Drop legacy `android.hardware.Camera` (`CameraHandler.kt:88` TODO). See [`features/photo-capture.md`](../features/photo-capture.md). |
| ML | **ML Kit Face Detection** | Already used; no replacement justified. |
| Audio DSP | Keep in-process (no native deps); add **real formant extraction (LPC)** to fill the documented gap in [`features/audio-tracking.md`](../features/audio-tracking.md) | The current zeroed-formants state is misleading. |
| Wear OS | Same Data-Layer surface, but split message handlers into discrete classes; introduce schema-version key for `DataItem` payloads | See [`apis/wearable-data-layer.md`](../apis/wearable-data-layer.md) §Change Notes. |
| Testing | JVM unit tests for DSP + DAOs; Compose UI tests for `*Screen.kt`; Hilt instrumentation for end-to-end | Only `:shared` has tests today. Skill: [`android-testing`](../../skills/android-testing/SKILL.md). |
| Telemetry | Firebase Analytics + Crashlytics, both opt-in | Already the case; preserve. |
| Logging | `Timber` or `kotlin-logging`; route to `CrashLogger` + Crashlytics | Today's tree uses `android.util.Log` directly. |

## Removals justified by Phases 1–5

| Remove | Justification |
|---|---|
| **Realm Kotlin SDK** | Used only by `RealmBackupImporter`, `RealmToRoomMigration`, `MobileWearableListenerService` (one write + one read), and legacy adapters — see [`data/realm-models.md`](../data/realm-models.md). All can move to Room with a one-time legacy `.realm` parser that does not link the Realm runtime into the new app. |
| **`android.hardware.Camera` legacy fallback** | CameraX has covered the minSdk-21 case since AndroidX Camera 1.1 stable; the TODO in [`CameraHandler.kt:88`](../../../mobile/src/main/java/com/shelbeely/opentransition/background/CameraHandler.kt) acknowledges this. |
| **Direct `Realm.openDefault()` reads from RecyclerView adapters** | Audit ISSUE-004, ISSUE-012; Phase 2 [`features/photo-gallery-and-viewer.md`](../features/photo-gallery-and-viewer.md). |
| **`PATH_AUDIO_START` / `PATH_AUDIO_STOP` / `DATA_PATH_AUDIO`** | Dead in both directions per [`apis/wearable-data-layer.md`](../apis/wearable-data-layer.md). |
| **`PATH_SYNC_MILESTONES`** | Same — declared, never sent. |
| **The "fake formants" code path** | Either ship real LPC or remove the columns entirely. See [`features/audio-tracking.md`](../features/audio-tracking.md). |

## Migration order (load-bearing vs isolated)

### Phase A — Isolated, can be rewritten without breaking users

1. **Voice goal CRUD UI** — pure Room; only consumer is `GoalCreationSheet`. ([`features/audio-tracking.md`](../features/audio-tracking.md))
2. **Settings screen** — already mostly Compose; `SettingsDomain` can be replaced with a `ViewModel` + `Repository`. ([`features/settings.md`](../features/settings.md))
3. **Disguised mode toggle** — pure `PackageManager` call; trivial to port. ([`features/disguised-mode.md`](../features/disguised-mode.md))
4. **Decoy-vault swap mechanism** — already isolated in `DatabaseManager`. ([`features/decoy-vault.md`](../features/decoy-vault.md))

### Phase B — Load-bearing, requires sequencing

5. **Photo capture (CameraX)** — wires into the Wear remote-control bus. Rewrite must keep `ACTION_CAMERA_*` broadcasts or replace them with a shared `Flow`. ([`features/photo-capture.md`](../features/photo-capture.md))
6. **Photo gallery / viewer / edit** — must move off Realm reads in lock-step with the Realm-removal sequence below. ([`features/photo-gallery-and-viewer.md`](../features/photo-gallery-and-viewer.md))
7. **Milestone list + Wear sync** — touches `MobileWearableListenerService.handleSyncRequest`. Must be migrated to Room before Realm is removed. ([`features/milestones.md`](../features/milestones.md))

### Phase C — Realm removal (only after all of B is done)

8. Migrate `handleSyncRequest` to query Room.
9. Migrate `saveAudioToRealm` to write to Room (or to drop the Photo-row creation entirely, in favour of a dedicated `AudioRecording` entity).
10. Add `transcript` column to `AudioAnalysisEntity` (Room migration 3 → 4) to close the round-trip gap noted in [`data/realm-models.md`](../data/realm-models.md).
11. Replace `RealmBackupImporter`'s `Realm.open(config)` with a **fully read-only Realm-file parser** (or a one-time `.ttbackup` zip-only path that does not depend on Realm at all).
12. Drop the `io.realm.kotlin:library-base` dependency.

### Phase D — Encryption rollout

13. Enable `isEncryptedDatabaseFeatureAvailable()` and `isEncryptedDatabaseEnabled()` ([`SettingsManager.kt:408, 415`](../../../mobile/src/main/java/com/shelbeely/opentransition/util/settings/SettingsManager.kt)).
14. Migrate existing plaintext `.db` to encrypted form using the SQLCipher CLI pattern documented in [`ENCRYPTED_DATABASE.md`](../../../ENCRYPTED_DATABASE.md).

### Phase E — Audio honesty

15. Implement LPC/cepstral formant extraction in `AudioAnalysisUtil`, **or** drop `f1Mean`..`f4Mean` from the schema (migration 4 → 5) and remove the corresponding chart.
16. Add unit tests around `PitchTracker` to prevent regression of the documented metrics in [`features/audio-tracking.md`](../features/audio-tracking.md).

### Phase F — Wire-format cleanup (only after wear users have updated)

17. Remove `PATH_AUDIO_START`, `PATH_AUDIO_STOP`, `DATA_PATH_AUDIO`, `PATH_SYNC_MILESTONES` from `WearableConstants`. Bump a schema-version key on all live `DataItem` paths.

## Out of scope for this proposal

- Native (Kotlin Multiplatform / iOS) — explicit non-goal: upstream iOS app exists separately ([upstream `README.md`](https://github.com/TransTracks/TransTracks-Android/blob/f8560a1aa643e06fa9cfb9dd7c8b4bcbf802f5c9/README.md)).
- Server backend — explicit non-goal (`apis/firebase-contracts.md` confirms no custom backend).
- Replacing Firebase — outside the scope of this rewrite plan; would need a separate decision page if pursued.

## Open questions before rewrite begins

See [`_state/unknowns.md`](../_state/unknowns.md). Resolve them before locking the order above.
