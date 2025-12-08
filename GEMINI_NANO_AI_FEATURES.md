# Gemini Nano AI Features

## Overview

OpenTransition now includes on-device AI capabilities powered by Google's Gemini Nano through ML Kit GenAI APIs. These features run entirely on your device, ensuring privacy while providing intelligent assistance.

## Features Implemented

### 1. Milestone Text Enhancement

When creating or editing milestones, you'll find AI assistance buttons that help improve your text:

#### Proofread Button (✓ Proofread)
- Corrects grammar and spelling errors
- Improves text clarity and style
- Works in multiple languages
- Completely on-device processing

#### Improve Button (✨ Improve)
When clicked, offers four style options:
- **More Formal**: Rewrites text in a professional, formal tone
- **More Casual**: Makes text friendlier and more conversational
- **Make Shorter**: Condenses text while keeping key information
- **Make Longer**: Expands text with more detail and context

### 2. Future Features (Framework Ready)

#### Photo Description Generation
The framework is in place to automatically generate descriptions for photos:
- Improves accessibility for visually impaired users
- Helps organize and search through photos
- Provides context for transition progress
- Completely private - images never leave your device

## Technical Details

### Requirements
- **Minimum Android Version**: Android 8.0 (API 26)
- **Device Compatibility**: Works best on newer Android devices with Gemini Nano support
  - Pixel 9 and later
  - Samsung Galaxy S25 series
  - Other flagship devices with compatible hardware

### Privacy & Security
- **All processing happens on-device** - your data never leaves your phone
- No internet connection required for AI features
- No data is sent to cloud servers
- Follows the app's existing privacy-first approach

### Current Status
The ML Kit GenAI APIs are in beta (version 1.0.0-beta1). The code infrastructure is complete and ready to use. Some features may show "not available" messages depending on:
- Device compatibility
- ML Kit GenAI library availability in your region
- Beta API limitations

## How to Use

### Using AI to Improve Milestone Text

1. **Create or Edit a Milestone**
   - Navigate to Add/Edit Milestone screen
   - Enter your milestone title and description

2. **Use Proofread**
   - Type your description
   - Tap the "✓ Proofread" button
   - The text will be automatically corrected for grammar and spelling

3. **Use Improve**
   - Type your description
   - Tap the "✨ Improve" button
   - Choose your preferred style:
     - More Formal: For important milestones
     - More Casual: For personal notes
     - Make Shorter: To keep it concise
     - Make Longer: To add more detail

### Tips for Best Results

- **Write something first**: The AI works best with at least a few sentences
- **Be clear**: Even if grammar isn't perfect, clear intent helps
- **Iterate**: Try different styles to find what works best
- **Edit after**: AI suggestions are a starting point - personalize them

## Technical Implementation

### For Developers

The implementation includes:

```kotlin
// GeminiNanoManager.kt
class GeminiNanoManager {
    suspend fun proofread(text: String): Result<String>
    suspend fun rewriteFormal(text: String): Result<String>
    suspend fun rewriteCasual(text: String): Result<String>
    suspend fun rewriteShorter(text: String): Result<String>
    suspend fun rewriteLonger(text: String): Result<String>
    suspend fun describeImage(bitmap: Bitmap): Result<String>
}
```

### Dependencies Added

```gradle
implementation 'com.google.mlkit:genai-summarization:1.0.0-beta1'
implementation 'com.google.mlkit:genai-rewriting:1.0.0-beta1'
implementation 'com.google.mlkit:genai-proofreading:1.0.0-beta1'
implementation 'com.google.mlkit:genai-image-description:1.0.0-beta1'
```

## Troubleshooting

### "AI features not available on this device"
- Your device may not support Gemini Nano yet
- Ensure you have the latest version of Google Play Services
- Some features may be region-restricted during beta

### "AI processing failed"
- Check your internet connection for initial model download (one-time)
- Restart the app
- Clear app cache if problems persist

### Text is empty error
- Enter some text before using AI features
- Minimum 2-3 words recommended

## Future Enhancements

Planned features include:
- Photo descriptions for better accessibility
- Milestone title suggestions
- Timeline summaries
- Progress insights and trends
- Customizable AI styles and preferences

## Credits

- **ML Kit GenAI**: Google's on-device AI framework
- **Gemini Nano**: Google's lightweight language model
- **OpenTransition Team**: Integration and implementation

## Learn More

- [ML Kit GenAI Documentation](https://developer.android.com/ai/gemini-nano/ml-kit-genai)
- [Google AI Blog](https://ai.googleblog.com/)
- [OpenTransition GitHub](https://github.com/shelbeely/OpenTransition)

---

*Last updated: December 2024*
*ML Kit GenAI Version: 1.0.0-beta1*
