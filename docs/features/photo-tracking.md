# Photo Tracking

*Developer documentation for photo tracking feature.*

## Overview

The photo tracking system allows users to capture, import, organize, and compare transition photos with enhanced camera controls.

## Enhanced Camera Features

### Camera Controls (CameraX)

The app includes an enhanced camera activity with professional-grade controls:

- **Flash Control**: Toggle between Off, On, and Auto flash modes
- **Camera Switching**: Easily switch between front and rear cameras
- **Grid Overlay**: Rule-of-thirds grid for better photo alignment
- **Self-Timer**: 3s, 5s, and 10s timer options for hands-free photos
- **Photo Comparison Overlay**: Ghost overlay of previous photos for consistent positioning

### Photo Comparison Overlay

The photo comparison overlay is particularly useful for tracking face and body changes over time. When taking a new photo:

1. The app can display a semi-transparent overlay of a previous photo
2. Users can adjust the opacity using a slider (0-100%)
3. The overlay helps maintain consistent:
   - Head position and angle (for face photos)
   - Body posture and camera distance (for body photos)
   - Lighting and background (for all photo types)

This feature significantly improves the quality of transition tracking by ensuring photos are comparable across time periods.

### Implementation Details

- **Framework**: CameraX 1.3.1 (AndroidX Camera API)
- **Minimum SDK**: 21 (Android 5.0)
- **Camera Permissions**: Handled by existing CameraHandler
- **Preferences**: Camera settings are persisted using SharedPreferences
- **Grid Overlay**: Custom `GridOverlayView` using Canvas drawing

## Usage

### For Users

The enhanced camera can be enabled/disabled in camera preferences. When enabled:
- Take photos with professional controls
- Use the grid for better composition
- Set a timer for hands-free selfies
- Compare with previous photos using the overlay

### For Developers

To launch the enhanced camera with overlay:

```kotlin
val overlayPhotoUri: Uri? = // URI of previous photo
CameraActivity.start(context, overlayPhotoUri)
```

Handle the result in `onActivityResult`:

```kotlin
if (requestCode == CameraActivity.REQUEST_CODE && resultCode == RESULT_OK) {
    val photoPath = data?.getStringExtra(CameraActivity.EXTRA_PHOTO_PATH)
    // Process the captured photo
}
```

## Architecture

The camera feature follows the app's architecture:

- **UI Layer**: `CameraActivity` (full-screen camera interface)
- **Background**: `CameraHandler` (permission management, legacy camera support)
- **Data Layer**: Photo storage via `FileUtil.getTempImageFile()`

## See Also

- [Data Layer](../architecture/data-layer.md) for Photo model details
- [Architecture Overview](../architecture/overview.md) for system design
