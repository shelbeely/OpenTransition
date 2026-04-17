# Photo Tracking

Developer documentation for the photo tracking feature.

## Overview

The photo tracking system allows users to capture, import, organize, and compare transition photos. Photos are categorized by type and stored in the app's private internal storage with metadata in the Room database.

## Photo Types

| Type | Constant | Description |
|------|----------|-------------|
| Face | `Photo.TYPE_FACE` (0) | Facial progress photos — great for tracking HRT facial changes |
| Body | `Photo.TYPE_BODY` (1) | Full-body progress photos |
| Custom | `Photo.TYPE_CUSTOM` (2) | User-defined areas (hands, legs, hair, etc.) |
| Audio | `Photo.TYPE_AUDIO` (3) | Voice recordings (see [Audio Tracking](audio-tracking.md)) |

## CameraX Face-Detection Camera

OpenTransition includes a dedicated face-detection camera powered by **CameraX** and **ML Kit Face Detection**.

### How It Works

1. User taps the face photo capture button
2. The `RecordAudioFragment` / face camera screen opens via CameraX `Preview` + `ImageCapture`
3. ML Kit's `FaceDetector` runs in real time on the camera preview stream
4. A bounding-box overlay guides the user to centre their face in the frame
5. The shutter fires automatically when the face is well-centred, or the user taps manually
6. The captured image is copied to internal storage and the `Photo` Room entity is created

### Key Files

- `mobile/src/main/java/com/shelbeely/opentransition/camera/` — CameraX setup and ML Kit integration
- `mobile/src/main/res/layout/fragment_face_camera.xml` — Camera preview with overlay

### Dependencies

```gradle
def camerax_version = "1.3.1"
implementation "androidx.camera:camera-core:${camerax_version}"
implementation "androidx.camera:camera-camera2:${camerax_version}"
implementation "androidx.camera:camera-lifecycle:${camerax_version}"
implementation "androidx.camera:camera-view:${camerax_version}"

implementation 'com.google.mlkit:face-detection:16.1.6'
```

## Importing Photos

Users can also import existing photos from their device gallery using Android's `PickMultipleVisualMedia` activity result API (no legacy `READ_EXTERNAL_STORAGE` permission needed on Android 13+).

1. `MainActivity` registers `pickMediaLauncher` with `PickMultipleVisualMedia`
2. Selected URIs are passed to `AssignPhotosFragment` via SafeArgs
3. `AssignPhotosDomain.addPhotos()` copies each image to internal storage and inserts a `Photo` entity

## Data Model

```kotlin
// Room entity
@Entity(tableName = "photos")
data class PhotoEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val timestamp: Long,
    val epochDay: Long,
    val filename: String,
    val type: Int
)
```

Photos are stored in `mobile/src/main/java/com/shelbeely/opentransition/data/Photo.kt` (Realm version retained for backwards compatibility with TransTracks import) and the Room entity in the `database/` package.

## Photo File Storage

Photos are copied to the app's private internal storage:

```
/data/data/com.shelbeely.opentransition/files/images/
```

This directory is only accessible to the app and is excluded from Android backups by default. Use the Export feature to create a portable `.ttbackup` archive.

## Implementation

See [Data Layer](../architecture/data-layer.md) for Photo model details and [Architecture Overview](../architecture/overview.md) for system design.

## Related

- [Audio Tracking](audio-tracking.md) — Voice recording feature
- [Gallery](gallery.md) — Browsing and comparing photos
- [Import & Export](import-export.md) — Backup and migration
