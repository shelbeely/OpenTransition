# Source Inventory (Phase 1)

> Read-only walk of `mobile/`, `wear/`, `shared/`. Every row cites a file path. Roles
> are the author's intent inferred from the file; behavioural claims live in the
> per-feature pages, not here.

## Mobile (`mobile/src/main/java/com/shelbeely/opentransition/`)

### Top-level packages

| Package | Role | Evidence |
|---|---|---|
| `OpenTransitionApp.kt` | `Application` subclass; bootstraps `DomainManager`, `MobileAds`, Firebase sync, crash logger | [`OpenTransitionApp.kt:36-95`](../../../mobile/src/main/java/com/shelbeely/opentransition/OpenTransitionApp.kt) |
| `background/` | CameraX, legacy Camera, ML Kit face detector, storage permission handler | [`background/`](../../../mobile/src/main/java/com/shelbeely/opentransition/background/) |
| `data/` | Realm-backed legacy data classes (`Photo`, `Milestone`, `AudioAnalysis`) and `TransTracksFileProvider` | [`data/`](../../../mobile/src/main/java/com/shelbeely/opentransition/data/) |
| `database/` | Room `AppDatabase`, `DatabaseManager`, `KeystoreManager`, Realm→Room migration, Realm backup importer | [`database/`](../../../mobile/src/main/java/com/shelbeely/opentransition/database/) |
| `domain/` | `DomainManager` + per-screen Domain classes (no ViewModel framework); `VoiceGoalRepository` | [`domain/`](../../../mobile/src/main/java/com/shelbeely/opentransition/domain/) |
| `ui/` | All UI surfaces (Fragments + Compose screens + RecyclerView adapters + custom views) | [`ui/`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/) |
| `util/` | Pitch tracking, audio decode, encryption, file helpers, biometric helper, RxJava bridges | [`util/`](../../../mobile/src/main/java/com/shelbeely/opentransition/util/) |
| `wear/` | Mobile-side `MobileWearableListenerService` | [`wear/`](../../../mobile/src/main/java/com/shelbeely/opentransition/wear/) |

### Activities, Fragments, services, providers

| Type | Component | Path |
|---|---|---|
| Activity | `MainActivity` (singleTask, portrait) | [`ui/MainActivity.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/MainActivity.kt) |
| Activity-alias | `MainActivityDefault` (default launcher, OpenTransition icon) | [`AndroidManifest.xml`](../../../mobile/src/main/AndroidManifest.xml) |
| Activity-alias | `MainActivityTrain` (disguised launcher, "Train Tracks" label) | [`AndroidManifest.xml`](../../../mobile/src/main/AndroidManifest.xml) |
| Service | `MobileWearableListenerService` (exported, `wear:/opentransition` prefix) | [`wear/MobileWearableListenerService.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/wear/MobileWearableListenerService.kt) |
| Provider | `TransTracksFileProvider` (extends `androidx.core.content.FileProvider`) | [`data/TransTracksFileProvider.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/data/TransTracksFileProvider.kt) |

Fragments live under `ui/<feature>/<Feature>Fragment.kt`:

| Fragment | Path |
|---|---|
| `AddEditMilestoneFragment` | [`ui/addeditmilestone/AddEditMilestoneFragment.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/addeditmilestone/AddEditMilestoneFragment.kt) |
| `AssignPhotosFragment` | [`ui/assignphoto/AssignPhotosFragment.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/assignphoto/AssignPhotosFragment.kt) |
| `CameraFragment` | [`ui/camera/CameraFragment.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/camera/CameraFragment.kt) |
| `CreditsFragment`, `OssLicensesFragment` | [`ui/credits/`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/credits/) |
| `EditPhotoFragment` | [`ui/editphoto/EditPhotoFragment.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/editphoto/EditPhotoFragment.kt) |
| `GalleryFragment` | [`ui/gallery/GalleryFragment.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/gallery/GalleryFragment.kt) |
| `HomeFragment` | [`ui/home/HomeFragment.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/home/HomeFragment.kt) |
| `LockFragment` | [`ui/lock/LockFragment.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/lock/LockFragment.kt) |
| `MilestonesFragment` | [`ui/milestones/MilestonesFragment.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/milestones/MilestonesFragment.kt) |
| `RecordAudioFragment` | [`ui/recordaudio/RecordAudioFragment.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/recordaudio/RecordAudioFragment.kt) |
| `SelectPhotoFragment`, `SelectAlbumFragment`, `SingleAlbumFragment` | [`ui/selectphoto/`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/selectphoto/) |
| `SettingsFragment` | [`ui/settings/SettingsFragment.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/settings/SettingsFragment.kt) |
| `SinglePhotoFragment` | [`ui/singlephoto/SinglePhotoFragment.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/singlephoto/SinglePhotoFragment.kt) |
| `VoiceProgressFragment` | [`ui/voiceprogress/VoiceProgressFragment.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/voiceprogress/VoiceProgressFragment.kt) |
| `VoiceSessionDetailFragment` | [`ui/voicesession/VoiceSessionDetailFragment.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/voicesession/VoiceSessionDetailFragment.kt) |

### State holders

OpenTransition does **not** use AndroidX `ViewModel`. State is held by per-feature
**Domain** objects, lazily created and shared via the application-scoped
`DomainManager`. The full set is:

| Domain | Path |
|---|---|
| `DomainManager` (root) | [`domain/DomainManager.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/domain/DomainManager.kt) |
| `AddEditMilestoneDomain` | [`domain/AddEditMilestoneDomain.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/domain/AddEditMilestoneDomain.kt) |
| `AssignPhotosDomain` | [`domain/AssignPhotosDomain.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/domain/AssignPhotosDomain.kt) |
| `EditPhotoDomain` | [`domain/EditPhotoDomain.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/domain/EditPhotoDomain.kt) |
| `HomeDomain` | [`domain/HomeDomain.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/domain/HomeDomain.kt) |
| `SettingsDomain` | [`domain/SettingsDomain.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/domain/SettingsDomain.kt) |
| `VoiceGoalRepository` (not in `DomainManager` — constructed per-call) | [`domain/VoiceGoalRepository.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/domain/VoiceGoalRepository.kt) |

### Room DAOs and entities

| Type | Name | Path |
|---|---|---|
| Entity | `PhotoEntity` (table `photos`) | [`database/room/entities/PhotoEntity.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/database/room/entities/PhotoEntity.kt) |
| Entity | `MilestoneEntity` (table `milestones`) | [`database/room/entities/MilestoneEntity.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/database/room/entities/MilestoneEntity.kt) |
| Entity | `AudioAnalysisEntity` (table `audio_analysis`) | [`database/room/entities/AudioAnalysisEntity.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/database/room/entities/AudioAnalysisEntity.kt) |
| Entity | `VoiceGoalEntity` (table `voice_goals`, added in schema v3) | [`database/room/entities/VoiceGoalEntity.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/database/room/entities/VoiceGoalEntity.kt) |
| DAO | `PhotoDao` | [`database/room/dao/PhotoDao.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/database/room/dao/PhotoDao.kt) |
| DAO | `MilestoneDao` | [`database/room/dao/MilestoneDao.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/database/room/dao/MilestoneDao.kt) |
| DAO | `AudioAnalysisDao` | [`database/room/dao/AudioAnalysisDao.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/database/room/dao/AudioAnalysisDao.kt) |
| DAO | `VoiceGoalDao` | [`database/room/dao/VoiceGoalDao.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/database/room/dao/VoiceGoalDao.kt) |
| Room DB | `AppDatabase` (v3, `exportSchema = true`, declares `MIGRATION_1_2`, `MIGRATION_2_3`) | [`database/AppDatabase.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/database/AppDatabase.kt) |

Schema snapshots under [`mobile/schemas/com.shelbeely.opentransition.database.AppDatabase/`](../../../mobile/schemas/com.shelbeely.opentransition.database.AppDatabase/): `1.json`, `3.json` (note: no `2.json` is committed — see [`_state/unknowns.md`](./unknowns.md)).

### Realm models still kept (read-only for import)

| Class | Path | Used by |
|---|---|---|
| `Photo` (`RealmObject`) | [`data/Photo.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/data/Photo.kt) | `RealmBackupImporter`, `RealmToRoomMigration`, `MobileWearableListenerService.saveAudioToRealm` |
| `Milestone` (`RealmObject`) | [`data/Milestone.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/data/Milestone.kt) | Same + `MobileWearableListenerService.handleSyncRequest` |
| `AudioAnalysis` (`RealmObject`) | [`data/AudioAnalysis.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/data/AudioAnalysis.kt) | Same |

Realm modules: `RealmBackupImporter`, `RealmToRoomMigration` ([`database/migration/`](../../../mobile/src/main/java/com/shelbeely/opentransition/database/migration/)).

### `BroadcastReceiver` / `WorkManager` workers

No declared `BroadcastReceiver`s in `AndroidManifest.xml` other than the system-registered ones implied by `WearableListenerService` (which is a `Service`). No `WorkManager` workers in the source tree (`grep -rn "extends.*Worker\|: Worker\|androidx.work" mobile/src/main/java` returns zero hits as of this pass — see [`_state/unknowns.md`](./unknowns.md) if this turns out to be wrong).

The mobile listener service does send four in-app broadcasts (`ACTION_CAMERA_*`, `ACTION_AUDIO_RECEIVED`) via `Context.sendBroadcast` ([`MobileWearableListenerService.kt:55-63`](../../../mobile/src/main/java/com/shelbeely/opentransition/wear/MobileWearableListenerService.kt)).

## Wear (`wear/src/main/java/com/shelbeely/opentransition/wear/`)

| Component | Type | Path |
|---|---|---|
| `MainActivity` | Activity (entry tile) | [`wear/MainActivity.kt`](../../../wear/src/main/java/com/shelbeely/opentransition/wear/MainActivity.kt) |
| `CameraControlActivity` | Activity (remote-shutter UI) | [`wear/CameraControlActivity.kt`](../../../wear/src/main/java/com/shelbeely/opentransition/wear/CameraControlActivity.kt) |
| `AudioRecordActivity` | Activity (record + ship to phone) | [`wear/AudioRecordActivity.kt`](../../../wear/src/main/java/com/shelbeely/opentransition/wear/AudioRecordActivity.kt) |
| `WearableListenerService` | exported service for incoming Data Layer events | [`wear/WearableListenerService.kt`](../../../wear/src/main/java/com/shelbeely/opentransition/wear/WearableListenerService.kt) |
| `WearTheme.kt` | Compose theme tokens (currently dead code per audit ISSUE-016) | [`wear/theme/WearTheme.kt`](../../../wear/src/main/java/com/shelbeely/opentransition/wear/theme/WearTheme.kt) |

## Shared (`shared/src/main/java/com/shelbeely/opentransition/shared/`)

| File | Role | Path |
|---|---|---|
| `WearableConstants.kt` | message-paths, data-item paths, capability names, keys, photo/flash enums | [`shared/WearableConstants.kt`](../../../shared/src/main/java/com/shelbeely/opentransition/shared/WearableConstants.kt) |
| `models/MilestoneData.kt` | `@Parcelize` cross-app DTO | [`shared/models/MilestoneData.kt`](../../../shared/src/main/java/com/shelbeely/opentransition/shared/models/MilestoneData.kt) |
| `util/WearableHelper.kt` | helpers over `MessageClient` / `DataClient` | [`shared/util/WearableHelper.kt`](../../../shared/src/main/java/com/shelbeely/opentransition/shared/util/WearableHelper.kt) |

## User-visible strings (selected)

This inventory does **not** enumerate every `R.string.*`. The per-feature pages
reference the specific string resources they surface. `mobile/src/main/res/values/strings.xml`
is the canonical source.

## Source of truth for "what runs"

- App entry: `MainActivity` (via either `MainActivityDefault` or `MainActivityTrain` alias).
- Nav graph: [`mobile/src/main/res/navigation/main_nav.xml`](../../../mobile/src/main/res/navigation/main_nav.xml) (376 lines per `grep`).
- App lifecycle: `OpenTransitionApp.onCreate` installs `CrashLogger`, schedules `MobileAds.initialize` and Firebase setting sync off the main thread.

## Next

Phase 2 (per-feature pages) builds on this inventory. Anything the inventory
exposed as "unknown" was filed in [`_state/unknowns.md`](./unknowns.md).
