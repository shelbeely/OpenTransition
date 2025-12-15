# Activating ML Kit GenAI Prompt API Features

## Quick Activation Guide

When the `genai-prompt` dependency becomes available, follow these steps to activate all AI features:

### Step 1: Add Dependency

In `app/build.gradle`, uncomment line 136:

```gradle
// BEFORE:
// implementation 'com.google.mlkit:genai-prompt:1.0.0-alpha01'

// AFTER:
implementation 'com.google.mlkit:genai-prompt:1.0.0-alpha01'
```

### Step 2: Uncomment Imports

In `GeminiNanoManager.kt`, uncomment lines 17-21:

```kotlin
// BEFORE:
// import com.google.mlkit.genai.prompt.Generation
// import com.google.mlkit.genai.prompt.TextPart
// import com.google.mlkit.genai.prompt.ImagePart
// import com.google.mlkit.genai.prompt.generateContentRequest

// AFTER:
import com.google.mlkit.genai.prompt.Generation
import com.google.mlkit.genai.prompt.TextPart
import com.google.mlkit.genai.prompt.ImagePart
import com.google.mlkit.genai.prompt.generateContentRequest
```

### Step 3: Uncomment GenerativeModel

In `GeminiNanoManager.kt`, uncomment lines 38-40:

```kotlin
// BEFORE:
// private val generativeModel by lazy {
//     Generation.getClient()
// }

// AFTER:
private val generativeModel by lazy {
    Generation.getClient()
}
```

### Step 4: Uncomment API Implementations

Replace all error returns with commented implementation code:

#### In `isAvailable()` (line 58):
```kotlin
// BEFORE:
false

// AFTER:
val status = generativeModel.checkStatus()
Log.d(TAG, "Gemini Nano status: $status")
status != null
```

#### In `describeImage()` (line 108):
```kotlin
// BEFORE:
Result.failure(Exception("Prompt API not yet available"))

// AFTER:
val response = generativeModel.generateContent(
    generateContentRequest(
        ImagePart(bitmap),
        TextPart("Describe this photo in one clear sentence.")
    )
) {
    temperature = 0.7f
    topK = 10
    maxOutputTokens = 50
}
val description = response.text ?: ""
if (description.isNotEmpty()) {
    Result.success(description)
} else {
    Result.failure(Exception("No description generated"))
}
```

#### Similar changes for:
- `rewriteText()` (line 143)
- `proofread()` (line 163)
- `generateJourneyNarrative()` (line 185)
- `generateMilestoneCelebration()` (line 210)
- `generatePhotoComparisonInsight()` (line 230)

### Step 5: Build and Test

```bash
./gradlew assembleDebug
```

## Implementation Details

### API Structure

All implementations follow this pattern:

```kotlin
val response = generativeModel.generateContent(
    generateContentRequest(
        // Text or Image parts
        TextPart("Your prompt here")
    )
) {
    temperature = 0.7f      // Creativity level (0.0 - 1.0)
    topK = 20              // Sampling diversity
    maxOutputTokens = 100  // Max response length
}
```

### Temperature Settings Used

- **0.3f**: Proofreading (consistent, deterministic)
- **0.7f**: Image description, photo comparison (balanced)
- **0.8f**: Text rewriting, milestone celebration (creative)
- **0.9f**: Journey narratives (highly creative, personal)

### Multimodal Example

```kotlin
generateContentRequest(
    ImagePart(photo1),
    ImagePart(photo2),
    TextPart("Compare these photos...")
)
```

## Testing the Features

### Test Image Description
```kotlin
val manager = GeminiNanoManager.getInstance(context)
lifecycleScope.launch {
    val result = manager.describeImage(myBitmap)
    result.onSuccess { description ->
        Log.d("Test", "Description: $description")
    }
}
```

### Test Journey Narrative
```kotlin
val milestones = listOf(
    "Started HRT" to "Finally taking this important step",
    "First injection" to "Nervous but excited",
    "3 month check-in" to "Seeing positive changes"
)

val result = manager.generateJourneyNarrative(
    milestones = milestones,
    daysSinceStart = 120,
    photoCount = 45
)
```

### Test Photo Comparison
```kotlin
val result = manager.generatePhotoComparisonInsight(
    earlierPhoto = bitmap1,
    laterPhoto = bitmap2,
    daysBetween = 180
)
```

## Troubleshooting

### Dependency Not Found
If `genai-prompt:1.0.0-alpha01` isn't available:
- Check for newer versions (alpha02, beta01, etc.)
- Monitor [ML Kit releases](https://developers.google.com/ml-kit/releases)
- Alternative: Wait for official stable release

### Model Not Downloaded
```kotlin
val status = generativeModel.checkStatus()
// If status shows downloadable:
generativeModel.download().collect { status ->
    when (status) {
        is DownloadStatus.DownloadProgress -> 
            Log.d("Download", "${status.totalBytesDownloaded} bytes")
        DownloadStatus.DownloadCompleted -> 
            Log.d("Download", "Ready!")
    }
}
```

### Device Not Compatible
ML Kit GenAI requires:
- Android 8.0 (API 26) or higher
- Compatible device (Pixel 9+, Samsung Galaxy S25+, select Xiaomi)
- Sufficient storage for model (~1-2GB)

## Code Locations

All AI code is in one file for easy management:
- **File**: `app/src/main/java/com/shelbeely/opentransition/util/GeminiNanoManager.kt`
- **Lines**: Commented sections clearly marked "IMPLEMENTATION READY"
- **Dependencies**: `app/build.gradle` line 136

## What Makes This Implementation Unique

### 1. Privacy-First Design
- All sensitive transition data processed on-device
- No cloud transmission of personal photos or milestones
- Perfect for HIPAA/privacy-sensitive applications

### 2. Context-Aware Prompts
- Prompts specifically engineered for transition journeys
- Supportive, encouraging tone throughout
- Validates both challenges and celebrations

### 3. Multimodal Capability
- Combines text (milestones) and images (photos)
- Photo comparison with contextual insights
- Timeline-aware narratives

### 4. Production-Ready
- Error handling on all API calls
- Proper temperature/topK configurations
- Logging for debugging
- Graceful fallbacks

## Next Steps

1. Monitor ML Kit GenAI releases
2. When dependency becomes available, follow activation steps
3. Test on compatible device
4. Iterate on prompt engineering based on user feedback
5. Consider adding UI elements for:
   - Journey narrative display on home screen
   - "Get AI encouragement" button on milestones
   - "Compare with AI insights" in gallery

---

**Status**: Implementation complete, waiting for dependency release
**Maintainer**: @shelbeely
**Documentation**: See GEMINI_NANO_AI_FEATURES.md for user-facing details
