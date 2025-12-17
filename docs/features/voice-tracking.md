# Voice Tracking

*Developer documentation for the voice tracking and audio analysis feature.*

## Overview

The voice tracking feature allows users to record audio samples and track vocal changes throughout their transition. The system provides automatic voice analysis including formant frequencies and pitch data, helping users monitor progress in voice feminization or masculinization.

!!! note "Current Implementation Status"
    The current voice analysis provides **estimated** formant values based on typical vocal characteristics and audio metadata. For production use requiring precise formant extraction, the implementation should be enhanced with a DSP library using Linear Predictive Coding (LPC) or similar advanced techniques. See the Future Enhancements section for details.

## Architecture

### Components

The voice tracking feature consists of several key components:

1. **Recording UI** (`RecordAudioFragment`, `RecordAudioView`)
2. **Audio Recording** (`AudioRecorderUtil`)
3. **Audio Analysis** (`AudioAnalysisUtil`)
4. **Data Models** (`Photo` with `TYPE_AUDIO`, `AudioAnalysis`)
5. **Visualization Widgets** (`FormantChartView`, `PitchProgressionView`, `WaveformView`)
6. **Audio Playback** (`AudioPlayerManager`)

### Data Flow

```
User → RecordAudioFragment → AudioRecorderUtil → Audio File
                                                     ↓
                                            AudioAnalysisUtil
                                                     ↓
                                            AudioAnalysis (Realm)
                                                     ↓
                           Photo (TYPE_AUDIO) ← Linked via photoId
                                                     ↓
                                    Gallery/Home Display with Visualizations
```

## Recording Audio

### User Interface

**File**: `app/src/main/java/com/shelbeely/opentransition/ui/recordaudio/RecordAudioFragment.kt`

The recording interface provides:
- Record/Stop button with visual feedback
- Real-time recording timer
- Date selection for the recording
- Save and Cancel actions
- Analysis progress indicator

### Recording Process

```kotlin
// Start recording
val tempFile = FileUtil.getTempAudioFile()
audioRecorder.startRecording(tempFile)

// Stop recording
audioRecorder.stopRecording()

// Save with analysis
val analysis = AudioAnalysisUtil.analyzeAudioFile(audioFile)
saveToDatabase(audioFile, analysis)
```

### Audio Format

**File**: `app/src/main/java/com/shelbeely/opentransition/util/AudioRecorderUtil.kt`

Recordings use the following configuration:
- **Format**: MPEG-4 container (`.m4a`)
- **Codec**: AAC (Advanced Audio Coding)
- **Sample Rate**: 44,100 Hz
- **Bit Rate**: 128 kbps
- **Audio Source**: Microphone

This format provides good quality for voice analysis while maintaining reasonable file sizes.

### Permissions

Recording requires the `RECORD_AUDIO` permission:

```xml
<uses-permission android:name="android.permission.RECORD_AUDIO" />
```

Permission is requested at runtime when the user attempts to record.

## Audio Analysis

### AudioAnalysis Data Model

**File**: `app/src/main/java/com/shelbeely/opentransition/data/AudioAnalysis.kt`

The `AudioAnalysis` model stores voice analysis results:

```kotlin
class AudioAnalysis : RealmObject {
    @PrimaryKey var id: String = UUID.randomUUID().toString()
    var photoId: String = ""  // Links to Photo object
    
    // Pitch (Fundamental Frequency)
    var f0Mean: Float = 0f   // Average pitch
    var f0Min: Float = 0f    // Minimum pitch
    var f0Max: Float = 0f    // Maximum pitch
    var f0StdDev: Float = 0f // Pitch variability
    
    // Formant Frequencies
    var f1Mean: Float = 0f   // First formant (tongue height)
    var f2Mean: Float = 0f   // Second formant (tongue position)
    var f3Mean: Float = 0f   // Third formant
    var f4Mean: Float = 0f   // Fourth formant
    
    var durationSeconds: Float = 0f
    var analysisTimestamp: Long = 0
}
```

### Voice Metrics Explained

#### Fundamental Frequency (F0) - Pitch

F0 represents the pitch of the voice:
- **Typical Masculine Range**: 85-180 Hz
- **Androgynous Range**: 145-165 Hz
- **Typical Feminine Range**: 165-255 Hz

The analysis captures:
- **f0Mean**: Average pitch across the recording
- **f0Min/f0Max**: Pitch range
- **f0StdDev**: Pitch variability (intonation patterns)

#### Formant Frequencies

Formants are resonant frequencies of the vocal tract that characterize voice quality:

**F1 (First Formant)**: 200-1000 Hz
- Related to tongue height
- Lower F1 = higher tongue position
- Affects vowel quality

**F2 (Second Formant)**: 800-2500 Hz
- Related to tongue front/back position
- Higher F2 = more forward tongue position
- Critical for voice feminization/masculinization

**F3 (Third Formant)**: 2000-3500 Hz
- Related to lip rounding and vocal tract length

**F4 (Fourth Formant)**: 3000-4500 Hz
- Higher-order resonance characteristics

**Analysis Implementation**

**File**: `app/src/main/java/com/shelbeely/opentransition/util/AudioAnalysisUtil.kt`

!!! note "Current Implementation"
    The current implementation provides estimated formant values based on typical vocal characteristics. For production use with precise formant extraction, consider integrating a DSP (Digital Signal Processing) library that implements Linear Predictive Coding (LPC) or other advanced formant analysis techniques.

The following shows the current analysis approach (simplified pseudocode for documentation):

```kotlin
fun analyzeAudioFile(audioFile: File): AudioAnalysis? {
    // Extract audio metadata using MediaExtractor
    val extractor = MediaExtractor()
    extractor.setDataSource(audioFile.absolutePath)
    
    // Get duration and format information
    val audioFormat = getAudioFormat(extractor)
    val durationSeconds = audioFormat.getDuration()
    
    // Current implementation: Estimate formants based on typical values
    // Real implementation would use DSP library for accurate extraction
    val analysis = AudioAnalysis().apply {
        f0Mean = estimatedPitch
        f1Mean = estimateF1(f0Mean)
        f2Mean = estimateF2(f0Mean)
        // ... other values
        durationSeconds = duration
        analysisTimestamp = System.currentTimeMillis()
    }
    
    return analysis
}
```

### Future Enhancements

For more accurate voice analysis, consider:

1. **DSP Libraries**: Integrate libraries like TarsosDSP or Praat-based tools for real-time formant extraction
2. **Real-time Analysis**: Provide live feedback during recording
3. **Advanced Metrics**: Add jitter, shimmer, and harmonics-to-noise ratio
4. **Machine Learning**: Train models to provide transition-specific voice feedback

## Visualization

### Waveform Display

**File**: `app/src/main/java/com/shelbeely/opentransition/ui/widget/WaveformView.kt`

Displays the audio waveform amplitude over time, helping users visualize the recording quality and patterns.

### Pitch Progression Chart

**File**: `app/src/main/java/com/shelbeely/opentransition/ui/widget/PitchProgressionView.kt`

Shows how pitch (F0) changes over time across multiple recordings, making it easy to see progress in voice work.

### Formant Chart

**File**: `app/src/main/java/com/shelbeely/opentransition/ui/widget/FormantChartView.kt`

Displays F1 vs F2 formant plot:
- Helps visualize vowel space
- Shows voice characteristics relative to typical ranges
- Enables comparison between recordings

```kotlin
// Display formant chart
formantChartView.setAnalysis(audioAnalysis)
formantChartView.setComparisonAnalysis(previousAnalysis) // Optional
```

## Data Storage

### Audio Files

Audio recordings are stored in the app's private storage:

```kotlin
// Get new audio file path
val audioFile = FileUtil.getNewAudioFile(date)

// File location: app-private/files/audio/YYYY-MM-DD_HHmmss.m4a
```

### Database Storage

Audio recordings are stored as `Photo` objects with `TYPE_AUDIO`:

```kotlin
// Photo type constants
const val TYPE_FACE = 0
const val TYPE_BODY = 1
const val TYPE_AUDIO = 2  // Voice recordings

val photo = Photo().apply {
    epochDay = date.toEpochDay()
    timestamp = System.currentTimeMillis()
    filePath = audioFile.absolutePath
    type = Photo.TYPE_AUDIO  // Value: 2
}

// Linked AudioAnalysis
val analysis = AudioAnalysis().apply {
    photoId = photo.id
    // ... analysis data
}
```

This approach allows audio recordings to:
- Appear in the main gallery alongside photos
- Be associated with specific dates
- Be included in exports and backups
- Share the same infrastructure as photos

## Gallery Integration

### Display in Gallery

Audio recordings appear in the gallery with a distinct visual indicator:

```kotlin
when (photo.type) {
    Photo.TYPE_AUDIO -> {
        // Display audio icon, waveform preview, or formant data
        showAudioPreview(photo)
    }
    else -> showPhotoPreview(photo)
}
```

### Audio Playback

**File**: `app/src/main/java/com/shelbeely/opentransition/util/AudioPlayerManager.kt`

The `AudioPlayerManager` handles audio playback in the gallery:

```kotlin
val audioPlayer = AudioPlayerManager()

// Play audio
audioPlayer.play(audioFile)

// Pause
audioPlayer.pause()

// Stop
audioPlayer.stop()

// Release resources
audioPlayer.release()
```

## Comparison Features

Users can compare audio recordings to track progress:

1. **Side-by-side Analysis**: View formant data for two recordings
2. **Formant Chart Overlay**: Overlay multiple recordings on the formant chart
3. **Pitch Progression**: Line graph showing pitch changes over time
4. **Statistical Comparison**: Compare averages, ranges, and variability

## Import/Export

### Export Format

Audio recordings are included in the `.ttbackup` export:

```json
{
  "photos": [
    {
      "id": "uuid",
      "type": 2,  // TYPE_AUDIO
      "filePath": "audio/recording.m4a",
      "timestamp": 1234567890000,
      "epochDay": 19000
    }
  ],
  "audioAnalyses": [
    {
      "id": "uuid",
      "photoId": "photo-uuid",
      "f0Mean": 180.5,
      "f0Min": 150.0,
      "f0Max": 210.0,
      "f1Mean": 650.0,
      "f2Mean": 1700.0,
      // ... other formant data
    }
  ]
}
```

### Import Process

When importing:
1. Audio files are copied to the audio storage directory
2. Photo entries are created with `TYPE_AUDIO`
3. AudioAnalysis entries are linked to their photos
4. Files are validated for existence and format

## User Experience Considerations

### Recording Tips

Provide users with guidance for consistent recordings:

- Record in a quiet environment
- Maintain consistent microphone distance
- Use the same device when possible
- Record similar phrases or vowel sounds
- Record at the same time of day

### Privacy

Audio recordings contain sensitive voice data:

- Stored in app-private storage (not accessible by other apps)
- Protected by app lock if enabled
- Included in encrypted backups
- Never uploaded without explicit user consent

### Performance

Audio analysis runs on a background thread to avoid blocking the UI:

```kotlin
Thread {
    val analysis = AudioAnalysisUtil.analyzeAudioFile(recordedFile)
    handler.post {
        // Update UI with results
        updateAnalysisDisplay(analysis)
    }
}.start()
```

## Testing

### Unit Tests

Test audio analysis logic:

```kotlin
@Test
fun testFormantEstimation() {
    val f0 = 180f
    val f1 = AudioAnalysisUtil.estimateF1(f0)
    assertTrue(f1 in 600f..800f)
}
```

### Integration Tests

Test full recording workflow:

```kotlin
@Test
fun testRecordAndAnalyze() {
    val tempFile = FileUtil.getTempAudioFile()
    val success = audioRecorder.startRecording(tempFile)
    assertTrue(success)
    
    Thread.sleep(2000) // Record for 2 seconds
    
    audioRecorder.stopRecording()
    val analysis = AudioAnalysisUtil.analyzeAudioFile(tempFile)
    assertNotNull(analysis)
    assertTrue(analysis.durationSeconds > 0)
}
```

## Known Limitations

1. **Formant Analysis**: Current implementation uses estimated values rather than true DSP-based formant extraction
2. **Background Noise**: No noise cancellation or filtering
3. **Real-time Feedback**: Analysis is performed after recording completes
4. **File Size**: Long recordings can consume significant storage

## Future Roadmap

- [ ] Integrate advanced DSP library for accurate formant extraction
- [ ] Add real-time pitch and formant feedback during recording
- [ ] Implement noise reduction and filtering
- [ ] Add voice training exercises and recommendations
- [ ] Support for importing existing audio files
- [ ] Vocal range analysis (speaking vs singing voice)
- [ ] Spectrogram visualization
- [ ] AI-powered voice coaching suggestions

## Related Documentation

- [Data Layer](../architecture/data-layer.md) - Audio data models
- [UI Layer](../architecture/ui-layer.md) - Recording interface
- [Photo Tracking](photo-tracking.md) - Related photo management
- [Import/Export](import-export.md) - Backup with audio

## Resources

### Voice Science

External resources for learning about voice and formants:

- [r/transvoice Community (Reddit)](https://www.reddit.com/r/transvoice/) - Active community providing voice training support and feedback
- [r/transvoice Wiki (Reddit)](https://www.reddit.com/r/transvoice/wiki/index) - Comprehensive guides covering voice training techniques and exercises
- [Formant Analysis (Wikipedia)](https://en.wikipedia.org/wiki/Formant) - Technical explanation of formants and acoustic phonetics

### Libraries for Future Integration

Open-source tools that could enhance voice analysis:

- [TarsosDSP (GitHub)](https://github.com/JorenSix/TarsosDSP) - Java library for real-time audio processing and pitch detection
- [Praat (External)](https://www.fon.hum.uva.nl/praat/) - Professional phonetics software for detailed voice analysis
- [Essentia (External)](https://essentia.upf.edu/) - C++ library for audio and music analysis with Python bindings
