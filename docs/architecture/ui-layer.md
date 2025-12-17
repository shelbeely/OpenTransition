# UI Layer

*This page is under development.*

The UI Layer consists of Fragments, Activities, and UI components including:

- MainActivity - Main app activity
- Various Fragments for different screens
- Custom widgets and views
- ViewBinding for UI interactions

## Adapters

### HomeGalleryAdapter
Displays media items (face photos, body photos, and audio recordings) in the home screen's horizontal scrolling views.

**Key Features:**
- Separate ViewHolder classes for different media types:
  - `PhotoViewHolder` - Displays face and body photos using Picasso
  - `AudioViewHolder` - Displays audio recordings with waveform and playback controls
  - `AddViewHolder` - "Add" button for capturing new media
- Type-specific layouts based on `Photo.TYPE_*` constants
- Integration with `AudioPlayerManager` for audio playback

**Important:** Never use image loading libraries (Picasso, Glide) to load audio files. Always use the appropriate ViewHolder for each media type.

### GalleryAdapter
Displays media items in a grid layout within the gallery screen.

**Key Features:**
- Larger card layout for audio items with comprehensive information
- Audio cards include:
  - Play/pause button (56dp)
  - Date display
  - Pitch and formant analysis data
  - Waveform visualization (80dp height)
  - Selection checkbox for batch operations
- Minimum card height of 140dp for audio items to prevent content overflow

## Custom Widgets

### Audio Visualization Widgets
- `WaveformView` - Displays audio waveform
- `PitchProgressionView` - Shows pitch changes over time
- `FormantChartView` - Visualizes formant frequencies

These widgets are used in audio recording, display, and comparison screens.

See [Architecture Overview](overview.md) for the complete architecture description and [Audio Tracking](../features/audio-tracking.md) for audio feature details.
