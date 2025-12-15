# Gemini Nano AI Features

## Overview

OpenTransition now includes on-device AI capabilities powered by Google's Gemini Nano through ML Kit GenAI APIs. These features run entirely on your device, ensuring privacy while providing intelligent assistance.

## Standard Features

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

## 🌟 Unique Custom Features (Powered by Prompt API)

### Transition Journey Narrative Generator

**What it does:** Analyzes your milestones, photos, and timeline to create a personalized, encouraging story about your transition journey.

**How it works:**
- Reads your milestone titles and descriptions
- Considers how long you've been tracking (days since start)
- Looks at how many photos you've taken
- Generates a warm, supportive 2-3 sentence narrative celebrating your progress

**Example output:**
> "Your journey of 120 days shows incredible dedication and growth. With 5 milestones documented, you're building a powerful story of authenticity and courage. Keep celebrating every step forward!"

**Use cases:**
- Display on home screen for motivation
- Include in exported reports
- Share milestone anniversaries
- Reflect on progress during difficult times

**Why it's unique:** This isn't a generic AI response - it's specifically designed to understand and celebrate transgender transition journeys with empathy and encouragement.

### Milestone Sentiment Celebration

**What it does:** Reads the emotional tone of your milestone and generates an appropriate supportive response.

**How it works:**
- Analyzes the text of your milestone
- Detects emotional tone (joyful, challenging, reflective, etc.)
- Generates ONE encouraging sentence that matches your mood
- Keeps it personal and authentic

**Example:**
- **Your milestone:** "Today I got my first HRT prescription! I'm so nervous but excited to finally start this chapter."
- **AI response:** "What a courageous step - your excitement and bravery shine through as you begin this important journey!"

**Use cases:**
- Auto-generate supportive responses when adding milestones
- Provide validation during challenging moments
- Celebrate victories with personalized messages
- Create a more interactive journaling experience

### Photo Comparison Progress Insights

**What it does:** Takes two of your photos from different time periods and generates encouraging observations about visible changes.

**How it works:**
- Accepts two photo bitmaps (earlier and later)
- Analyzes visual changes between them
- Considers the time span between photos
- Generates encouraging, specific observations

**Example:**
> "Comparing photos from 6 months apart shows beautiful confidence emerging - from the subtle differences in your expression to how you hold yourself, your authentic self is shining through more each day."

**Use cases:**
- When comparing before/after in gallery
- Creating progress collages
- Documenting HRT effects over time
- Celebrating visual milestones

**Why it's special:** Uses multimodal AI (text + images) to provide insights that are more meaningful than generic compliments - it actually "sees" and acknowledges your journey.

## Technical Details

### Requirements
- **Minimum Android Version**: Android 8.0 (API 26)
- **Device Compatibility**: Works best on newer Android devices with Gemini Nano support
  - Pixel 9 and later
  - Samsung Galaxy S25 series
  - Select Xiaomi devices
  - Other flagship devices with compatible hardware

### Privacy & Security
- **All processing happens on-device** - your data never leaves your phone
- No internet connection required for AI features
- No data is sent to cloud servers
- Follows the app's existing privacy-first approach
- Especially important for sensitive transition-related content

### Current Status
The ML Kit GenAI APIs are in beta. The code infrastructure is complete with:
- ✅ Standard features (proofread, rewrite) - structure ready
- ✅ Custom Prompt API features - fully designed and documented
- 🔧 Final API integration pending class name confirmation
- 🔧 Prompt API dependency ready (will be uncommented when released)

Some features may show "not available" messages depending on:
- Device compatibility
- ML Kit GenAI library availability in your region
- Beta API limitations

## How to Use

### Using Standard AI Features

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

### Using Custom Narrative Features (Coming Soon)

These features will be accessible through:
- **Home Screen**: "Generate Journey Story" button
- **Milestone Details**: "Get AI Encouragement" option
- **Gallery Comparison**: "Analyze Progress" when viewing two photos

### Tips for Best Results

- **Write something first**: The AI works best with at least a few sentences
- **Be authentic**: Even if grammar isn't perfect, genuine expression helps
- **Iterate**: Try different styles to find what resonates
- **Personalize**: AI suggestions are a starting point - make them yours
- **Trust the process**: The journey narrative feature learns from your actual milestones

## Privacy Considerations

### Why On-Device AI Matters for Transition Tracking

Transition documentation is deeply personal and sensitive. Traditional cloud AI would require sending your:
- Milestone descriptions
- Personal photos
- Timeline information
- Progress narratives

With Gemini Nano's on-device processing:
- ✅ Everything stays on your phone
- ✅ No servers see your data
- ✅ Works offline
- ✅ No data mining or profiling
- ✅ Complete control

## Technical Implementation (For Developers)

### Core API Structure

```kotlin
val geminiManager = GeminiNanoManager.getInstance(context)

// Standard features
geminiManager.proofread(text)
geminiManager.rewriteFormal(text)
geminiManager.rewriteCasual(text)

// Custom journey narrative
geminiManager.generateJourneyNarrative(
    milestones = listOf("Title" to "Description"),
    daysSinceStart = 120,
    photoCount = 45
)

// Milestone celebration
geminiManager.generateMilestoneCelebration(milestoneText)

// Photo comparison
geminiManager.generatePhotoComparisonInsight(
    earlierPhoto, laterPhoto, daysBetween
)
```

### Prompt Engineering

The custom features use carefully crafted prompts:

**Journey Narrative Prompt Structure:**
```
You are a supportive friend helping someone document their transition journey.
Based on their progress, write a warm, encouraging 2-3 sentence narrative.

Journey details:
- Days tracking: [X]
- Photos taken: [Y]
- Recent milestones: [...]

Write an uplifting, personal narrative celebrating their journey.
Focus on growth, courage, and progress. Keep it warm and authentic.
```

**Key design principles:**
- Supportive, not clinical
- Specific to transition context
- Validates both achievements and challenges
- Maintains privacy and respect
- Encourages continued documentation

## Troubleshooting

### "AI features not available on this device"
- Your device may not support Gemini Nano yet
- Ensure you have the latest version of Google Play Services
- Some features may be region-restricted during beta

### "AI processing failed"
- Check for model download (one-time, requires internet initially)
- Restart the app
- Clear app cache if problems persist

### Text is empty error
- Enter some text before using AI features
- Minimum 2-3 words recommended

### Custom features not showing
- These require the Prompt API (alpha release)
- Will appear when Prompt API becomes available
- Check for app updates

## Future Enhancements

Planned features include:
- **Timeline Summaries**: Month/year recaps of your journey
- **Pattern Recognition**: "You seem to document more during..."
- **Goal Suggestions**: Based on your milestones
- **Community Insights**: Anonymous, aggregated patterns (opt-in)
- **Voice Journaling**: Speak your milestones, AI transcribes and enhances
- **Multi-language Support**: Document in your preferred language

## Credits

- **ML Kit GenAI**: Google's on-device AI framework
- **Gemini Nano**: Google's lightweight language model
- **Prompt API**: Custom AI request interface
- **OpenTransition Team**: Integration, prompt engineering, and custom use case design

## Learn More

- [ML Kit GenAI Documentation](https://developer.android.com/ai/gemini-nano/ml-kit-genai)
- [Prompt API Guide](https://developers.google.com/ml-kit/genai/prompt/android)
- [Google AI Blog](https://ai.googleblog.com/)
- [OpenTransition GitHub](https://github.com/shelbeely/OpenTransition)

---

*Last updated: December 2024*
*ML Kit GenAI Version: 1.0.0-beta1*
*Custom Features: Transition Journey Narratives, Sentiment Celebration, Progress Insights*
