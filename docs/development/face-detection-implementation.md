# Face Detection & Landmarks Implementation

## Overview

This document describes the face detection and landmarks feature implementation for OpenTransition, designed to provide a dysphoria-safe UX for taking progress photos with consistent framing.

## Architecture

### Components

1. **FaceDetectionAnalyzer** - ML Kit face detection analyzer
2. **CameraXHandler** - Modern CameraX-based camera manager
3. **CameraOverlayView** - Custom view for rendering alignment guides
4. **CameraFragment** - Fragment coordinating camera and face detection UI

### Technology Stack

- **CameraX 1.3.1** - Modern Android camera API
  - Preview use case for camera viewfinder
  - ImageCapture for taking high-quality photos
  - ImageAnalysis for real-time face detection
  
- **ML Kit Face Detection 16.1.6** - On-device face detection
  - Performance mode: FAST (real-time preview)
  - Landmark mode: ALL (eyes, nose, mouth detection)
  - Classification mode: NONE (no smile/eyes open detection)
  - Tracking enabled for consistent face ID across frames

## Features

### Real-Time Face Detection

The analyzer detects faces in the camera preview and provides:

- **Face bounds** - Rectangular boundary around detected face
- **Head pose** - Euler angles for pitch (nod), yaw (turn), roll (tilt)
- **Facial landmarks** - Position of eyes, nose, and mouth
- **Distance estimation** - Face size relative to frame for consistent framing

### Visual Guidance Overlays

The overlay view provides real-time feedback:

1. **Face Alignment Box**
   - Green when all pose criteria met
   - Yellow when adjustments needed
   
2. **Shoulder Line Guide**
   - Horizontal line for shoulder alignment
   
3. **Center Alignment Guide**
   - Vertical line for centering
   
4. **Guidance Text**
   - "Chin up" / "Chin down" based on head pitch
   - "Turn left" / "Turn right" based on head yaw
   - "Level your head" when tilted
   - "Move closer" / "Move back" based on face size
   - "✓ Perfect alignment" when all criteria met

5. **Facial Landmarks**
   - Cyan dots marking eyes, nose, and mouth positions

### Dysphoria-Safe Design Principles

1. **Optional Overlays**
   - Toggle button to show/hide all guides
   - Minimal, non-intrusive UI design
   
2. **Privacy-Focused**
   - All processing happens on-device
   - No face data leaves the device
   - No face recognition or identity tracking
   
3. **Supportive Guidance**
   - Gentle, non-judgmental feedback
   - Focus on consistency, not appearance
   - Clear, actionable instructions

4. **Material 3 Expressive**
   - Bouncy spring animations on button presses
   - Smooth, playful interactions
   - Emotionally engaging UI

## Ideal Pose Criteria

The overlay uses the following thresholds for optimal framing:

```kotlin
val idealPitchRange = -10f..10f  // Head level (neutral)
val idealYawRange = -15f..15f    // Face forward
val idealRollRange = -10f..10f   // Head not tilted
val idealFaceRatioRange = 0.08f..0.15f  // Appropriate distance
```

These ranges ensure:
- Consistent head position across photos
- Optimal face size for detail
- Comparable framing for progress tracking

## Usage

### Navigation

The camera can be accessed from:

1. **Home Screen** - "Take Photo" button
2. **Gallery Screen** - "Add Photo" camera option

Both navigate to `CameraFragment` with these arguments:
- `type: Int` - Photo type (FACE, BODY, AUDIO)
- `destinationToPopTo: Int` - Where to return after photo capture
- `epochDay: BoxedLong?` - Optional date for the photo

### Camera Permissions

The app handles camera permissions using modern `ActivityResultContracts`:

```kotlin
private val requestPermissionLauncher = registerForActivityResult(
    ActivityResultContracts.RequestPermission()
) { isGranted: Boolean ->
    if (isGranted) {
        startCamera()
    } else {
        showPermissionDeniedMessage()
    }
}
```

### Face Detection Flow

1. Camera preview starts
2. ImageAnalysis processes frames at ~30fps
3. ML Kit detects faces and landmarks
4. Results passed to overlay view
5. Overlay renders guides and feedback
6. User captures photo when aligned
7. Photo saved and passed to assignment flow

### Backward Compatibility

The existing `CameraHandler` (using Intent-based camera) remains in place but is no longer used by `HomeFragment` and `GalleryFragment`. This allows:

- Gradual migration to new camera
- Fallback if issues arise
- Support for older integration points

## Code Organization

```
mobile/src/main/java/com/shelbeely/opentransition/
├── background/
│   ├── CameraXHandler.kt           # CameraX camera manager
│   ├── FaceDetectionAnalyzer.kt    # ML Kit face detection
│   └── CameraHandler.kt            # Legacy camera (kept for compatibility)
├── ui/
│   ├── camera/
│   │   └── CameraFragment.kt       # Main camera UI
│   └── widget/
│       └── CameraOverlayView.kt    # Overlay rendering
└── util/
    └── AnalyticsUtil.kt            # Analytics event tracking
```

## Dependencies

Added to `mobile/build.gradle`:

```gradle
// CameraX for modern camera implementation
def camerax_version = "1.3.1"
implementation "androidx.camera:camera-core:${camerax_version}"
implementation "androidx.camera:camera-camera2:${camerax_version}"
implementation "androidx.camera:camera-lifecycle:${camerax_version}"
implementation "androidx.camera:camera-view:${camerax_version}"

// ML Kit for face detection
implementation 'com.google.mlkit:face-detection:16.1.6'
```

## Testing Considerations

### Manual Testing Checklist

- [ ] Camera permissions flow (grant/deny)
- [ ] Face detection accuracy in various lighting
- [ ] Overlay rendering performance (60fps target)
- [ ] Guidance text correctness for all pose angles
- [ ] Photo capture quality
- [ ] Overlay toggle functionality
- [ ] Navigation to/from camera
- [ ] Photo assignment after capture
- [ ] Different Android versions (API 21+)
- [ ] Different screen sizes and orientations
- [ ] Battery impact of continuous face detection

### Performance Notes

- Face detection runs on background thread
- ImageAnalysis uses `STRATEGY_KEEP_ONLY_LATEST` to prevent backlog
- Overlay draws on UI thread but minimal (simple shapes/text)
- Camera shutdown properly releases resources

## Future Enhancements

Possible improvements (not implemented):

1. **Pose History Tracking**
   - Save reference pose from previous photo
   - Overlay ghosted outline for matching
   - Show distance from previous pose

2. **Adaptive Thresholds**
   - Learn user's typical pose
   - Adjust ideal ranges based on history

3. **Photo Comparison**
   - Side-by-side with previous photo
   - Alignment diff visualization

4. **Accessibility**
   - Audio feedback for pose adjustments
   - Haptic feedback when aligned
   - Voice commands for capture

5. **Advanced Landmarks**
   - Facial contours for more precise alignment
   - Face mesh for 3D pose estimation

## Resources

- [CameraX Documentation](https://developer.android.com/training/camerax)
- [ML Kit Face Detection](https://developers.google.com/ml-kit/vision/face-detection)
- [Material Design 3 Expressive](https://m3.material.io/)
- [Android Spring Animations](https://developer.android.com/develop/ui/views/animations/physics)

## Credits

Implementation by OpenTransition contributors, building on:
- Google CameraX team
- Google ML Kit team
- Material Design team
