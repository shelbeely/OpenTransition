# Feature: Photo Capture

> Take a face/body photo using the in-app camera or import from system photos.
> Page structure follows `.github/skills/repo-knowledge-base/SKILL.md` §"Fifth Pass: Features".

## Summary

Two capture paths exist:

1. **In-app camera** — CameraX preview with optional ML-Kit face overlay, two photo "types" (face/body) chosen before capture.
2. **System photo picker** — Picks one or more pre-existing photos and assigns them to a date + type.

Saved photos are written to app-private storage and recorded as `PhotoEntity` rows in Room. The Wear OS companion can remote-trigger capture and remote-control the on-phone camera while it is open.

## Entry points

| Surface | Component | Path |
|---|---|---|
| Camera screen | `CameraFragment` | [`ui/camera/CameraFragment.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/camera/CameraFragment.kt) |
| System picker (multi-select) | `SelectPhotoFragment` + `SelectAlbumFragment` / `SingleAlbumFragment` | [`ui/selectphoto/`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/selectphoto/) |
| Post-capture assignment | `AssignPhotosFragment` | [`ui/assignphoto/AssignPhotosFragment.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/assignphoto/AssignPhotosFragment.kt) |
| Wear remote trigger | `MobileWearableListenerService.handlePhotoTrigger` | [`wear/MobileWearableListenerService.kt:163-166`](../../../mobile/src/main/java/com/shelbeely/opentransition/wear/MobileWearableListenerService.kt) |

## State holders

- `AssignPhotosDomain` — staged photos awaiting date/type assignment ([`domain/AssignPhotosDomain.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/domain/AssignPhotosDomain.kt)).
- No `ViewModel` is used; state is held by the Domain object obtained from `OpenTransitionApp.instance.domainManager.assignPhotosDomain`.

## Camera plumbing

| Concern | Where |
|---|---|
| CameraX session lifecycle | [`background/CameraXHandler.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/background/CameraXHandler.kt) |
| Legacy `android.hardware.Camera` fallback (per ISSUE-list `TODO`) | [`background/CameraHandler.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/background/CameraHandler.kt) |
| ML Kit face detection overlay | [`background/FaceDetectionAnalyzer.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/background/FaceDetectionAnalyzer.kt), drawn by [`ui/widget/CameraOverlayView.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/widget/CameraOverlayView.kt) |
| Permission gating | [`background/StoragePermissionHandler.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/background/StoragePermissionHandler.kt); `Manifest.permission.CAMERA` declared in [`AndroidManifest.xml`](../../../mobile/src/main/AndroidManifest.xml) |
| Image file location | App-private under `filesDir`/`getMediaFile(...)` ([`util/FileUtil.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/util/FileUtil.kt)) |

## Persistence touched

| Data | Sink | DAO |
|---|---|---|
| `PhotoEntity { id, epochDay, timestamp, filePath, type }` | Room `photos` table | [`PhotoDao`](../../../mobile/src/main/java/com/shelbeely/opentransition/database/room/dao/PhotoDao.kt) |
| Image file bytes | App-private filesystem (see [`data/file-layout.md`](../data/file-layout.md)) | n/a |

`Photo.type` values: `TYPE_FACE = 0`, `TYPE_BODY = 1`, `TYPE_AUDIO = 2` ([`data/Photo.kt:66-70`](../../../mobile/src/main/java/com/shelbeely/opentransition/data/Photo.kt)).

> Note: as of this pass, legacy UI adapters still read photos from Realm directly (see [`audit-report/07-issues-and-bugs.md`](../../../audit-report/07-issues-and-bugs.md) ISSUE-004). Newly-captured photos written through `RecordAudioFragment`/`MobileWearableListenerService` are also persisted to Realm for backward compatibility ([`MobileWearableListenerService.kt:228-249`](../../../mobile/src/main/java/com/shelbeely/opentransition/wear/MobileWearableListenerService.kt)).

## External APIs

- **CameraX** (`androidx.camera.*`) for preview + image capture.
- **ML Kit Face Detection** for live face overlay (no analysis results are persisted).
- **Wearable Data Layer** message `PATH_TRIGGER_PHOTO` (body: `"face"` or `"body"`) and `PATH_CAMERA_{SHUTTER,ZOOM,FLASH,SWITCH}` — see [`apis/wearable-data-layer.md`](../apis/wearable-data-layer.md).

## Known issues (audit cross-reference)

- 🟠 **Watch-triggered capture only logs the trigger**: `handlePhotoTrigger` is a `Log.d` line — it does not open the camera UI ([`MobileWearableListenerService.kt:163-166`](../../../mobile/src/main/java/com/shelbeely/opentransition/wear/MobileWearableListenerService.kt)).
- 🟡 Camera adapters open Realm on the main thread ([`audit-report/07-issues-and-bugs.md`](../../../audit-report/07-issues-and-bugs.md) ISSUE-012).
- 🟡 `CameraHandler.kt:88` TODO: legacy Camera replaced by CameraX VideoCapture eventually ([`audit-report/08-unfinished-features.md`](../../../audit-report/08-unfinished-features.md)).

## Invariants

- Per-photo `id` is a stable UUID-string ([`Photo.kt:36-37`](../../../mobile/src/main/java/com/shelbeely/opentransition/data/Photo.kt), [`PhotoEntity.kt:17-22`](../../../mobile/src/main/java/com/shelbeely/opentransition/database/room/entities/PhotoEntity.kt)).
- `epochDay` is `LocalDate.toEpochDay()` (UTC date). The viewer/gallery groups by it.
- `timestamp` is `System.currentTimeMillis()` at capture/save.
- `filePath` is an **absolute path inside `Context.filesDir`** — never an SDK media-store URI.

## Tests

Search returns no instrumented or unit tests directly covering capture; `:shared:testDebugUnitTest` covers wear-helper only ([`shared/src/test/.../WearableHelperTest.kt`](../../../shared/src/test/java/com/shelbeely/opentransition/shared/util/WearableHelperTest.kt)).

## Open questions

See [`_state/unknowns.md`](../_state/unknowns.md) → "Photo capture".
