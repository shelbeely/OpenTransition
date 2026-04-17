# Audio Tracking

*Developer documentation for audio tracking feature.*

## Overview

The audio tracking system allows users to record, store, analyze, and compare voice recordings over time. This feature is particularly useful for tracking vocal changes during transition.

## Features

### Audio Recording
- Record audio directly from the app
- Real-time waveform visualization during recording
- Save audio recordings with automatic timestamping
- Audio recordings are stored alongside face and body photos

### Audio Analysis
- Automatic pitch (F0) analysis
- Formant frequency analysis (F1, F2)
- Analysis results stored and displayed with recordings
- Visual representation through waveforms and analysis charts

### Audio Display

#### Home View
Audio recordings are displayed in a dedicated "Audio" section on the home screen with:
- Custom audio item layout (`home_adapter_audio_item.xml`)
- Play/pause button for quick playback
- Waveform visualization
- Compact display optimized for horizontal scrolling

#### Gallery View
Audio recordings have their own gallery with:
- Larger card layout for better content visibility (`gallery_adapter_audio_item.xml`)
- Card minimum height of 140dp to accommodate all content
- Play/pause control (56dp button)
- Date display
- Pitch and formant information
- Waveform visualization (80dp height)
- Selection checkbox for batch operations

## Implementation

### Data Model
Audio recordings use the same `Photo` model with `type = Photo.TYPE_AUDIO`:
- Audio files are stored in the app's media directory
- File path stored in `Photo.filePath`
- Analysis data stored in separate `AudioAnalysis` model
- Linked via `photoId` field

### UI Components

#### HomeGalleryAdapter
The `HomeGalleryAdapter` has been updated to properly handle audio items:
- `AudioViewHolder` class handles audio-specific display
- Separate layout (`home_adapter_audio_item.xml`) for audio items
- Play button with state management
- Integration with `AudioPlayerManager` for playback

#### GalleryAdapter
The `GalleryAdapter` includes:
- `AudioViewHolder` for gallery-specific audio display
- Larger card layout with comprehensive information
- Audio analysis data display (pitch, formants)
- Waveform visualization using `WaveformView`

### Audio Playback
- `AudioPlayerManager` handles audio playback across the app
- Supports play/pause toggle
- Tracks current playback state by photo ID
- Prevents multiple simultaneous playbacks

### Recording Flow
1. User navigates to audio recording screen from home view
2. `RecordAudioFragment` manages recording session
3. User records audio with real-time feedback
4. Audio analysis performed on recorded file
5. Recording saved with "Save Audio" button (not "Save Photo")
6. Photo model created with `TYPE_AUDIO`
7. Audio analysis results stored in `AudioAnalysis` model

## Key Files

### Layouts
- `mobile/src/main/res/layout/record_audio.xml` - Audio recording screen
- `mobile/src/main/res/layout/home_adapter_audio_item.xml` - Home view audio item
- `mobile/src/main/res/layout/gallery_adapter_audio_item.xml` - Gallery view audio card
- `mobile/src/main/res/layout/audio_comparison.xml` - Audio comparison view

### Kotlin Files
- `mobile/src/main/java/com/shelbeely/opentransition/ui/recordaudio/RecordAudioFragment.kt` - Recording logic
- `mobile/src/main/java/com/shelbeely/opentransition/ui/home/HomeGalleryAdapter.kt` - Home audio display
- `mobile/src/main/java/com/shelbeely/opentransition/ui/gallery/GalleryAdapter.kt` - Gallery audio display
- `mobile/src/main/java/com/shelbeely/opentransition/util/AudioPlayerManager.kt` - Playback management
- `mobile/src/main/java/com/shelbeely/opentransition/util/AudioAnalysisUtil.kt` - Audio analysis
- `mobile/src/main/java/com/shelbeely/opentransition/util/AudioRecorderUtil.kt` - Recording utilities

### Widgets
- `mobile/src/main/java/com/shelbeely/opentransition/ui/widget/WaveformView.kt` - Waveform display
- `mobile/src/main/java/com/shelbeely/opentransition/ui/widget/PitchProgressionView.kt` - Pitch visualization
- `mobile/src/main/java/com/shelbeely/opentransition/ui/widget/FormantChartView.kt` - Formant visualization

## User Experience Improvements

### Fixed Issues
1. **Audio files no longer confused as images**: Audio files are now handled by dedicated `AudioViewHolder` instead of trying to load them as images with Picasso
2. **Proper button labels**: Recording screen now shows "Save Audio" instead of "Save Photo"
3. **Larger gallery cards**: Audio cards in gallery view use minimum height of 140dp to prevent content overflow
4. **Better visual hierarchy**: Increased play button (56dp) and waveform (80dp) sizes for improved usability

### Best Practices
- Always use `AudioViewHolder` for displaying audio items
- Don't attempt to load audio files with image loading libraries (Picasso, Glide, etc.)
- Use appropriate string resources for audio-specific actions
- Provide adequate space in layouts for audio metadata display

## Future Enhancements
- Audio comparison between multiple recordings
- Export audio recordings
- Share audio recordings
- Advanced audio analysis features
- Audio quality settings
- Background noise reduction

## Related Documentation
- [Data Layer](../architecture/data-layer.md) - Photo and AudioAnalysis models
- [UI Layer](../architecture/ui-layer.md) - UI component architecture
- [Photo Tracking](photo-tracking.md) - Related photo tracking feature
- [Gallery](gallery.md) - Gallery feature documentation
