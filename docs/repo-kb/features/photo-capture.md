# Feature: Photo Capture (CameraX + ML Kit Face Detection)

## User-Facing Behavior

The user opens the in-app camera (default front-facing) to take a journal photo. A live preview is shown with optional alignment overlays — a face bounding box, head-tilt indicator, "chin up / chin down" guidance, and a face-detected indicator — to help frame consistent self-portraits over time. The user can toggle the overlays on/off (a Snackbar confirms "Guides enabled / disabled"). Tapping the capture button writes a JPEG and routes the user to the **Assign Photo** flow, where the photo gets a date, type (face/body/custom album) and is persisted to the gallery.

If the camera permission has not been granted, the fragment requests it; if denied, a Snackbar with a "Settings" action is shown.

## Implementation Map

| Layer | Files |
|---|---|
| UI fragment | [`mobile/src/main/java/com/shelbeely/opentransition/ui/camera/CameraFragment.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/camera/CameraFragment.kt) |
| Layout | [`mobile/src/main/res/layout/fragment_camera.xml`](../../../mobile/src/main/res/layout/fragment_camera.xml) (referenced as `R.layout.fragment_camera`) |
| Overlay view | [`mobile/src/main/java/com/shelbeely/opentransition/ui/widget/CameraOverlayView.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/widget/CameraOverlayView.kt) |
| CameraX wiring | [`mobile/src/main/java/com/shelbeely/opentransition/background/CameraXHandler.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/background/CameraXHandler.kt) |
| Face analyzer | [`mobile/src/main/java/com/shelbeely/opentransition/background/FaceDetectionAnalyzer.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/background/FaceDetectionAnalyzer.kt) |
| Output file path | [`mobile/src/main/java/com/shelbeely/opentransition/util/FileUtil.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/util/FileUtil.kt) (`getTempImageFile`, `getNewImageFile`) |
| Downstream save | [`mobile/src/main/java/com/shelbeely/opentransition/ui/assignphoto/AssignPhotosFragment.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/assignphoto/AssignPhotosFragment.kt) → [`domain/AssignPhotosDomain.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/domain/AssignPhotosDomain.kt) (writes via `FileUtil.getNewImageFile` and `copyToRealm`) |
| Manifest permissions | [`mobile/src/main/AndroidManifest.xml`](../../../mobile/src/main/AndroidManifest.xml) (`CAMERA` permission) |
| Tests | None located for the camera path. See [`_state/unknowns.md`](../_state/unknowns.md). |

## Flow

1. **Navigation in.** Other screens navigate to `R.id.cameraFragment` with SafeArgs (`type`, `destinationToPopTo`, `epochDay`).
2. **Permission check.** `CameraFragment.checkCameraPermission()` either calls `startCamera()` directly or launches `RequestPermission`.
3. **Camera bind.** `CameraXHandler.startCamera()` resolves `ProcessCameraProvider`, then `bindCameraUseCases()` builds three use cases and binds them to the fragment lifecycle:
   - `Preview` → wired to the `PreviewView` from the layout binding.
   - `ImageCapture` with `CAPTURE_MODE_MAXIMIZE_QUALITY`.
   - `ImageAnalysis` (640×480, `STRATEGY_KEEP_ONLY_LATEST`) backed by `FaceDetectionAnalyzer` on a single-thread `Executors.newSingleThreadExecutor()`.
   - `CameraSelector.DEFAULT_FRONT_CAMERA` is used by default (`useFrontCamera = true`).
4. **Per-frame analysis.** `FaceDetectionAnalyzer` builds an ML Kit `FaceDetector` with `PERFORMANCE_MODE_FAST`, `LANDMARK_MODE_ALL`, classification + contour disabled, and `enableTracking()`. For each frame, the first detected face is reduced to a `FaceDetectionResult.FaceDetected` carrying head Euler angles (pitch / yaw / roll), face-to-image area ratio, and four landmark positions (eyes, nose base, mouth bottom). If no face is found it emits `FaceDetectionResult.NoFace`; on failure it emits `Error(exception)`.
5. **Overlay update.** Results are forwarded via the `onFaceDetection` callback into `binding.overlayView.updateFaceDetection(result)` (`CameraOverlayView`). The overlay paints a face box, landmarks, and alignment guides keyed off ideal pose ranges (`±10°` pitch/roll, `±15°` yaw, face area `0.08–0.15` of frame).
6. **Capture.** `CameraFragment.takePhoto()` calls `CameraXHandler.takePhoto()`, which writes to a temp JPEG produced by `FileUtil.getTempImageFile()` (`<filesDir>/temp/<yyyy-MM-dd-HH-mm-ss>_<uuid>.jpg`).
7. **Hand-off.** On success, `CameraFragment.navigateToAssignPhoto(file)` navigates via `CameraFragmentDirections.actionGlobalAssignPhotos(...)` passing the temp `file://` URI, the album type, the pop-to destination, and the epoch day.
8. **Persist.** `AssignPhotosDomain` decodes EXIF, picks the photo date, calls `FileUtil.getNewImageFile(date)` (under `<filesDir>/photos/`), copies bytes from the URI into that file, and `copyToRealm(photo, UpdatePolicy.ALL)` to persist the gallery entry.
9. **Cleanup.** `CameraFragment.onDestroyView()` calls `CameraXHandler.shutdown()`, which closes the analyzer, unbinds all use cases, and shuts down the camera executor.

## Config and Environment

- **Permissions:** `android.permission.CAMERA` (declared in `AndroidManifest.xml`, requested at runtime).
- **Front vs. back camera:** Hard-coded `useFrontCamera = true` in `CameraXHandler` — no user-facing toggle was found in the source. (Logged in unknowns.)
- **Face detection on/off:** `CameraXHandler.setFaceDetectionEnabled(...)` exists but no UI call site was found. The fragment exposes only an *overlay* toggle (`btnToggleOverlay`) which calls `binding.overlayView.setOverlaysEnabled(...)`; the analyzer keeps running.
- **Output location:** Capture writes to `<context.filesDir>/temp/`; persisted photos move to `<context.filesDir>/photos/` (see `FileUtil.getPhotosDirectory()`).
- **Build deps:** CameraX core/lifecycle/view + ML Kit Face Detection are declared in [`mobile/build.gradle`](../../../mobile/build.gradle); see [`dependency-map.md`](../dependency-map.md) "Camera & ML".
- **Analytics:** `AnalyticsUtil.logEvent(Event.CameraFragmentShown)` fires once per fragment view.

## Failure Modes

- **Permission denied** → Snackbar with `R.string.camera_permission_required_message` and a "Settings" deep-link via `Utils.goToDeviceSettings`. Camera is *not* started.
- **Camera bind exception** → `bindCameraUseCases()` swallows the exception via `printStackTrace()` and the preview stays blank. `startCamera`'s outer `try/catch` calls `onError`, which surfaces a Snackbar.
- **No face in frame** → analyzer emits `FaceDetectionResult.NoFace`; the overlay shows the "no face" state. Capture is *not* gated on face presence — the user can still take the picture.
- **ML Kit detection failure** → `Error(exception)` propagates to the overlay; analyzer keeps processing subsequent frames.
- **`ImageCapture` not initialized** → `takePhoto()` synthesizes an `ImageCaptureException(ERROR_UNKNOWN, ...)` and the fragment shows "Photo capture failed: ...".
- **Disk write failure during capture** → ML Kit's `OnImageSavedCallback.onError` fires; same Snackbar path.
- **Lifecycle leak** → mitigated by `cameraProvider.unbindAll()` and `cameraExecutor.shutdown()` in `CameraXHandler.shutdown()`, called from `onDestroyView`.

## How to Modify Safely

- Keep all CameraX use-case binding inside `bindCameraUseCases()` and rebind when toggling face detection — the existing `setFaceDetectionEnabled` already calls `bindCameraUseCases` again.
- If you add a *gating* requirement (e.g. "must have face detected to capture"), do it in `CameraFragment.takePhoto()` by inspecting the last `FaceDetectionResult`; do not move the check into the analyzer (which runs on a background executor).
- The face analyzer must call `imageProxy.close()` on every frame (it currently does so in `addOnCompleteListener` and the `mediaImage == null` branch) — never short-circuit those paths.
- The output file is created by `FileUtil.getTempImageFile()` under `filesDir/temp/`; do not switch this to `cacheDir` without also updating `FileUtil.clearTempFolder()` and the `Assign Photo` consumer that reads back via `Uri.fromFile(...)`.
- If you change the camera selector to back-facing, expose it via SafeArgs or settings rather than flipping the constructor default.
- `FaceDetectorOptions` performance mode is `FAST`; switching to `ACCURATE` increases per-frame latency — verify the `STRATEGY_KEEP_ONLY_LATEST` analyzer keeps up.
- Update [`features/index.md`](./index.md) and [`_state/coverage.md`](../_state/coverage.md) if the camera entry point or permissions change.

## Evidence

- [`mobile/src/main/java/com/shelbeely/opentransition/ui/camera/CameraFragment.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/camera/CameraFragment.kt)
- [`mobile/src/main/java/com/shelbeely/opentransition/background/CameraXHandler.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/background/CameraXHandler.kt)
- [`mobile/src/main/java/com/shelbeely/opentransition/background/FaceDetectionAnalyzer.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/background/FaceDetectionAnalyzer.kt)
- [`mobile/src/main/java/com/shelbeely/opentransition/ui/widget/CameraOverlayView.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/widget/CameraOverlayView.kt)
- [`mobile/src/main/java/com/shelbeely/opentransition/util/FileUtil.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/util/FileUtil.kt)
- [`mobile/src/main/java/com/shelbeely/opentransition/domain/AssignPhotosDomain.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/domain/AssignPhotosDomain.kt)
- [`mobile/src/main/AndroidManifest.xml`](../../../mobile/src/main/AndroidManifest.xml)
- [`mobile/build.gradle`](../../../mobile/build.gradle) (CameraX + ML Kit dependencies)
