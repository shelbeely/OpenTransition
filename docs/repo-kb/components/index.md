# Components

A "component" here is a major reusable runtime unit: `Application`, `Activity`, `Fragment`, top-level Compose screen, `Service`, `WorkManager` worker, `BroadcastReceiver`, or `ContentProvider`. This page is the **flat top-level map** for the whole monorepo. Per-component deep-dives are not produced in this pass — instead, the responsible feature page (under [`features/`](../features/index.md)) carries the implementation detail.

> **Pattern note.** Most user-facing screens follow a **`*Fragment` + `*Screen.kt`** pair: the Fragment hosts a `ComposeView` and supplies state/callbacks to the matching `@Composable Screen`. This is the active migration shape from XML Views → Compose. Pairs are listed together below.

## `:mobile`

### Application

| Component | Source | Purpose |
|---|---|---|
| `OpenTransitionApp` | [`mobile/.../OpenTransitionApp.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/OpenTransitionApp.kt) | Process entry point. Initialises Firebase, AdMob, encrypted prefs, and triggers the legacy Realm → Room migration via `triggerAutomaticMigrationIfNeeded` (see [`features/import.md`](../features/import.md)). |

### Activities

| Component | Source | Purpose |
|---|---|---|
| `MainActivity` | [`mobile/.../ui/MainActivity.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/MainActivity.kt) | Single-Activity host for the Navigation 2.8 graph; also handles the `.ttbackup` `VIEW` intent (`processIntent` / `processImport`, see [`features/import.md`](../features/import.md)). The launcher-icon disguise is implemented as `<activity-alias>` entries in the manifest, not separate Activities. |

### Fragment + Screen pairs (Compose-on-Fragment hosts)

| Pair | Sources | Purpose |
|---|---|---|
| Home | [`HomeFragment.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/home/HomeFragment.kt) + [`HomeScreen.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/home/HomeScreen.kt) | Top-level dashboard / journal landing surface. |
| Gallery | [`GalleryFragment.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/gallery/GalleryFragment.kt) + [`GalleryScreen.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/gallery/GalleryScreen.kt) | Photo grid grouped by `epochDay`. |
| Single photo | [`SinglePhotoFragment.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/singlephoto/SinglePhotoFragment.kt) + [`SinglePhotoScreen.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/singlephoto/SinglePhotoScreen.kt) | Detail view for a single `PhotoEntity`. |
| Edit photo | [`EditPhotoFragment.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/editphoto/EditPhotoFragment.kt) + [`EditPhotoScreen.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/editphoto/EditPhotoScreen.kt) | Re-date / re-classify an existing photo. |
| Camera capture | [`CameraFragment.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/camera/CameraFragment.kt) | CameraX preview + ML Kit face overlay (no `*Screen.kt` — currently XML-based); see [`features/photo-capture.md`](../features/photo-capture.md). |
| Select photo (root) | [`SelectPhotoFragment.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/selectphoto/SelectPhotoFragment.kt) + [`SelectPhotoScreen.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/selectphoto/SelectPhotoScreen.kt) | Entry hub for photo-picker flows. |
| Select album | [`SelectAlbumFragment.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/selectphoto/selectalbum/SelectAlbumFragment.kt) + [`SelectAlbumScreen.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/selectphoto/selectalbum/SelectAlbumScreen.kt) | Lists device photo buckets. |
| Single album | [`SingleAlbumFragment.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/selectphoto/singlealbum/SingleAlbumFragment.kt) + [`SingleAlbumScreen.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/selectphoto/singlealbum/SingleAlbumScreen.kt) | Photo grid for one album. |
| Assign photo | [`AssignPhotosFragment.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/assignphoto/AssignPhotosFragment.kt) + [`AssignPhotoScreen.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/assignphoto/AssignPhotoScreen.kt) | Final hand-off step that writes a `PhotoEntity` (target of [`features/photo-capture.md`](../features/photo-capture.md)). |
| Add / edit milestone | [`AddEditMilestoneFragment.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/addeditmilestone/AddEditMilestoneFragment.kt) + [`AddEditMilestoneScreen.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/addeditmilestone/AddEditMilestoneScreen.kt) | Create/update a `MilestoneEntity`. |
| Milestones list | [`MilestonesFragment.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/milestones/MilestonesFragment.kt) + [`MilestonesScreen.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/milestones/MilestonesScreen.kt) | Browses `MilestoneEntity` rows. |
| Record audio | [`RecordAudioFragment.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/recordaudio/RecordAudioFragment.kt) + [`RecordAudioScreen.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/recordaudio/RecordAudioScreen.kt) | Voice recording capture; persists `AudioAnalysisEntity`. |
| Voice progress | [`VoiceProgressFragment.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/voiceprogress/VoiceProgressFragment.kt) + [`VoiceProgressScreen.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/voiceprogress/VoiceProgressScreen.kt) | Charts metrics over time, overlays `VoiceGoalEntity` bands. |
| Voice session detail | [`VoiceSessionDetailFragment.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/voicesession/VoiceSessionDetailFragment.kt) + [`VoiceSessionDetailScreen.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/voicesession/VoiceSessionDetailScreen.kt) | Drill-down for a single recording. |
| App lock | [`LockFragment.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/lock/LockFragment.kt) + [`LockScreen.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/lock/LockScreen.kt) | PIN / "trains" / biometric gate; switches real ↔ decoy vault (see [`features/app-lock.md`](../features/app-lock.md)). |
| Settings | [`SettingsFragment.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/settings/SettingsFragment.kt) + [`SettingsScreen.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/settings/SettingsScreen.kt) | App preferences; hosts the Settings-driven `.realm` import (`backupPickerLauncher`). |
| Credits | [`CreditsFragment.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/credits/CreditsFragment.kt) + [`CreditsScreen.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/credits/CreditsScreen.kt) | About / credits screen. |
| OSS licences | [`OssLicensesFragment.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/credits/OssLicensesFragment.kt) + [`OssLicensesScreen.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/credits/OssLicensesScreen.kt) | OSS attribution list. |

### Services

| Component | Source | Purpose |
|---|---|---|
| `MobileWearableListenerService` | [`mobile/.../wear/MobileWearableListenerService.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/wear/MobileWearableListenerService.kt) | Receives `MessageClient` / `DataClient` events from the Wear app. Manifest-registered with the `/opentransition` path prefix; full contract in [`apis/wearable-data-layer.md`](../apis/wearable-data-layer.md). |

### Content providers

| Component | Source | Purpose |
|---|---|---|
| `TransTracksFileProvider` | [`mobile/.../data/TransTracksFileProvider.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/data/TransTracksFileProvider.kt) | `FileProvider` subclass exposing app-private files via `xml/provider_paths`. Manifest authority `com.shelbeely.opentransition.data.TransTracksFileProvider`. |

### Workers / Receivers

None located. `grep -rE 'class .* : (Worker|CoroutineWorker|BroadcastReceiver)' mobile/src/main` returned no matches; the `AndroidManifest.xml` declares no `<receiver>` elements.

## `:wear`

### Activities

| Component | Source | Purpose |
|---|---|---|
| `MainActivity` | [`wear/.../MainActivity.kt`](../../../wear/src/main/java/com/shelbeely/opentransition/wear/MainActivity.kt) | Wear OS launcher. Compose-based UI built on Wear Compose Material/Foundation; routes to `CameraControlActivity` and `AudioRecordActivity`. |
| `CameraControlActivity` | [`wear/.../CameraControlActivity.kt`](../../../wear/src/main/java/com/shelbeely/opentransition/wear/CameraControlActivity.kt) | Remote shutter for the phone's `CameraFragment` over the Wearable Data Layer. |
| `AudioRecordActivity` | [`wear/.../AudioRecordActivity.kt`](../../../wear/src/main/java/com/shelbeely/opentransition/wear/AudioRecordActivity.kt) | Wear-side voice capture surface. |

### Composables / theme

| Component | Source | Purpose |
|---|---|---|
| `WearTheme.kt` | [`wear/.../theme/WearTheme.kt`](../../../wear/src/main/java/com/shelbeely/opentransition/wear/theme/WearTheme.kt) | Wear Compose Material theme wrapper. (Only `@Composable`-bearing file under `:wear` outside the activities themselves.) |

### Services

| Component | Source | Purpose |
|---|---|---|
| `WearableListenerService` | [`wear/.../WearableListenerService.kt`](../../../wear/src/main/java/com/shelbeely/opentransition/wear/WearableListenerService.kt) | Mirror of the mobile listener on the watch side. Manifest path-prefix `/opentransition`. Contract: [`apis/wearable-data-layer.md`](../apis/wearable-data-layer.md). |

### Workers / Receivers / Providers

None located.

## `:shared`

`:shared` is a plain Android library with no runtime components — it ships three Kotlin sources only:

| File | Source | Purpose |
|---|---|---|
| `WearableConstants.kt` | [`shared/.../WearableConstants.kt`](../../../shared/src/main/java/com/shelbeely/opentransition/shared/WearableConstants.kt) | Message paths & data keys for the Wearable Data Layer contract. |
| `models/MilestoneData.kt` | [`shared/.../models/MilestoneData.kt`](../../../shared/src/main/java/com/shelbeely/opentransition/shared/models/MilestoneData.kt) | `@Parcelize` DTO shared across mobile ↔ wear messages. |
| `util/WearableHelper.kt` | [`shared/.../util/WearableHelper.kt`](../../../shared/src/main/java/com/shelbeely/opentransition/shared/util/WearableHelper.kt) | Thin wrapper around `MessageClient` / `DataClient`. |

Full enumeration is documented in [`apis/wearable-data-layer.md`](../apis/wearable-data-layer.md); not duplicated here.

## What this page intentionally does **not** cover

- **ViewModels** — they sit beside their Fragments under each `ui/<feature>/` directory and are best documented in their feature page rather than as standalone components.
- **XML layouts** — pre-Compose, slated for migration; tracked in `audit-report/` and `DEFERRED_WORK.md`.
- **Per-component deep dives** — deferred. The pattern across the app is uniform enough (`*Fragment` hosts a `ComposeView` rendering a sibling `*Screen`) that pairing them in this index is the highest-value summary.

## Evidence

Inventory derived from:

```
find {mobile,wear,shared}/src/main -type f \( -name '*Activity.kt' -o -name '*Fragment.kt' \
  -o -name '*Service.kt' -o -name '*Worker.kt' -o -name '*Receiver.kt' -o -name '*Screen.kt' \)
grep -rE 'class .* : (Service|WearableListenerService|Worker|CoroutineWorker|BroadcastReceiver)' \
  --include='*.kt' {mobile,wear,shared}/src/main
grep -E '<service|<receiver|<provider' {mobile,wear}/src/main/AndroidManifest.xml
```

Run on the working tree at the time of this pass.
