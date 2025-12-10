# AI Features Documentation

## Overview

OpenTransition now includes optional AI-powered features that can enhance your transition tracking experience. These features use an OpenAI-compatible API (such as OpenAI, OpenRouter, or other compatible services) to provide intelligent assistance with photo descriptions, progress insights, milestone suggestions, and photo comparisons.

**Important:** All AI features are completely optional and require you to provide your own API key. Your data is only sent to the AI service when you explicitly use an AI feature.

## Configuration

### Setting Up AI Features

1. **Navigate to Settings**
   - Open the app and tap the settings icon
   - Scroll down to the "AI Features" section

2. **Enable AI Features**
   - Toggle the "Enable AI Features" switch to ON
   - The "Configure AI" button will appear

3. **Configure Your API**
   - Tap "Configure AI"
   - Enter your API credentials:
     - **API Key**: Your OpenAI-compatible API key (e.g., from OpenAI or OpenRouter)
     - **API Base URL**: The API endpoint (default: `https://api.openai.com/v1`)
     - **Model**: The AI model to use (default: `gpt-3.5-turbo`)
   - Tap "Update" to save

### Getting an API Key

#### Option 1: OpenAI
1. Visit [platform.openai.com](https://platform.openai.com)
2. Create an account or sign in
3. Generate an API key from the API keys section
4. Use the default base URL: `https://api.openai.com/v1`
5. Choose a model like `gpt-3.5-turbo` or `gpt-4`

#### Option 2: OpenRouter
1. Visit [openrouter.ai](https://openrouter.ai)
2. Create an account
3. Generate an API key
4. Use base URL: `https://openrouter.ai/api/v1`
5. Choose from various available models

#### Option 3: Other Compatible Services
Any service that implements the OpenAI Chat Completions API is compatible. Check with your preferred AI service provider for:
- Their API key generation process
- Their API base URL
- Available model names

## AI Features

### 1. Photo Description Generator

Generate supportive and encouraging descriptions for your progress photos.

**How It Works:**
- The AI analyzes photo metadata (date, type, days since start)
- Generates a personalized, affirming description
- Focuses on acknowledging your journey and progress

**Use Cases:**
- Add meaningful captions to photos
- Document feelings and observations
- Create a narrative of your journey

**Note:** The AI cannot actually see your photos - it generates descriptions based on metadata and timeline information only.

### 2. Progress Insights

Get AI-powered analysis of your transition journey.

**What It Analyzes:**
- Days since starting your journey
- Number of photos taken
- Milestones recorded
- Recent milestone titles

**Output:**
- Encouraging insights about your consistency
- Recognition of your documented progress
- Personalized affirmations
- Observations about your journey timeline

### 3. Milestone Suggestions

Receive suggestions for milestones you might want to document.

**How It Works:**
- Considers your current timeline
- Reviews existing milestones
- Suggests relevant events to document
- Recognizes that every journey is unique

**Examples:**
- Medical appointments
- Coming out events
- Legal name changes
- Personal achievements
- Social milestones

### 4. Smart Photo Comparison

Get supportive analysis when comparing photos from different time periods.

**What It Provides:**
- Acknowledgment of time between photos
- Recognition of commitment to documentation
- Encouraging thoughts about progress
- Celebration of the journey

**Note:** Like photo descriptions, the AI cannot see your actual photos. The analysis focuses on timeline and commitment aspects.

## Privacy & Security

### Data Usage

- **Local Storage**: Your API key and settings are stored locally on your device
- **Not Synced**: AI settings are NOT synced to Firebase/cloud
- **On-Demand**: AI features only send data when you explicitly use them
- **No Photos Sent**: Your actual photos are NEVER sent to the AI service
- **Metadata Only**: Only text information (dates, titles, counts) is sent

### What Gets Sent to the AI Service

When you use an AI feature, the following information may be sent:
- Photo metadata (date, type, days since start)
- Milestone titles and dates
- Journey statistics (photo counts, milestone counts)
- Timeline information

**Never Sent:**
- Actual photo images
- Personal identifying information
- Location data
- Your name or account details

### API Key Security

- Your API key is stored encrypted on your device
- The app never sends your API key to any server except the configured AI service
- You can delete your API key at any time by disabling AI features

## Cost Considerations

AI API calls typically have associated costs. Here's what to consider:

### OpenAI Pricing (as of 2024)
- GPT-3.5-Turbo: ~$0.001 per request (very low cost)
- GPT-4: ~$0.03-0.06 per request
- Typical usage: 1-10 AI features per week = negligible cost

### OpenRouter Pricing
- Varies by model
- Many free and low-cost options available
- Check current pricing at openrouter.ai

### Controlling Costs
- Only use AI features when needed
- Choose cost-effective models
- Set API usage limits in your API provider dashboard
- Monitor usage through your provider's console

## Troubleshooting

### "Please configure your AI API key in settings first"
- You need to enable AI features and configure an API key
- Go to Settings > AI Features > Enable > Configure AI

### "Unable to generate AI response. Please check your API configuration."
Possible causes:
1. **Invalid API Key**: Verify your key is correct
2. **Wrong Base URL**: Ensure it matches your provider
3. **Invalid Model Name**: Check the model name is supported
4. **Network Issue**: Check your internet connection
5. **API Quota Exceeded**: Check your usage limits
6. **Service Outage**: The AI service may be temporarily down

### AI Responses Are Too Generic
- Try a more advanced model (e.g., gpt-4 instead of gpt-3.5-turbo)
- Ensure you have documented milestones and photos for better context
- Consider using a different AI service

### Configuration Not Saving
- Check that you tapped "Update" after entering values
- Ensure the app has storage permissions
- Try force-closing and reopening the app

## Best Practices

1. **Start Simple**: Try gpt-3.5-turbo first before upgrading to more expensive models
2. **Use Sparingly**: AI features are best used intentionally rather than constantly
3. **Review Responses**: AI-generated content should reflect your personal experience
4. **Edit as Needed**: Use AI suggestions as inspiration, not gospel
5. **Monitor Costs**: Check your API usage periodically
6. **Protect Your Key**: Never share your API key with others

## Future Enhancements

Potential future AI features being considered:
- Voice journal transcription
- Mood tracking insights
- Goal setting assistance
- Community story sharing (anonymous)
- Progress predictions
- Personalized reminders

## Support

### Questions?
- Check the [FAQ](https://shelbeely.github.io/OpenTransition/user-guide/faq/)
- Report issues at [GitHub Issues](https://github.com/shelbeely/OpenTransition/issues)

### Privacy Concerns?
- Review our [Privacy Policy](https://shelbeely.github.io/OpenTransition/user-guide/privacy/)
- AI features follow the same privacy principles as the rest of the app
- You have complete control over when and how AI is used

## Disclaimer

AI-generated content is created by third-party services and should be treated as suggestions, not medical or professional advice. OpenTransition is not responsible for the content generated by AI services. Your transition journey is unique and personal - AI features are just tools to help document it.
