# UI Layer

*This page is under development.*

The UI Layer consists of Fragments, Activities, and UI components including:

- **MainActivity** - Main app activity and navigation host
- **Fragments** - Screen-level UI components
  - HomeFragment - Dashboard and recent activity
  - GalleryFragment - Photo and audio gallery
  - RecordAudioFragment - Audio recording interface
  - MilestonesFragment - Milestone management
  - And more...
- **Custom Widgets** - Specialized UI components
  - FormantChartView - F1 vs F2 formant visualization
  - PitchProgressionView - Pitch changes over time
  - WaveformView - Audio waveform display
  - Gallery adapters for photos and audio
- **ViewBinding** - Type-safe UI interactions

## Key UI Components

### RecordAudioFragment

Provides the interface for recording audio samples with real-time timer, recording controls, and date selection. See [Voice Tracking](../features/voice-tracking.md) for details.

### Audio Visualization Widgets

Custom views for displaying voice analysis data:
- **FormantChartView**: Plots F1 vs F2 formants
- **PitchProgressionView**: Shows pitch changes across recordings
- **WaveformView**: Displays audio amplitude over time

See [Architecture Overview](overview.md) for the complete architecture description.
