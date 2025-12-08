# Self-Hosting OpenTransition: Quick Start Guide

This document provides a quick overview of how to set up your own version of OpenTransition with complete independence from the original infrastructure.

## What You Asked For

> "I need to host my own version of the account's server to disconnect from the original."

**Good news!** OpenTransition doesn't require a separate server. It uses **Firebase** as the backend, which you can easily set up for yourself. This gives you complete control and independence.

## What is the "Account Server"?

OpenTransition uses **Firebase** (Google's Backend-as-a-Service) for:
- ✅ User authentication (sign up, sign in)
- ✅ Cloud sync of settings across devices
- ✅ Optional crash reporting and analytics

There's **no custom server code** to host. You just need your own Firebase project.

## Quick Setup (15 minutes)

### 1. Create Your Firebase Project

1. Go to https://console.firebase.google.com/
2. Click "Create a project"
3. Enter a name (e.g., "MyOpenTransition")
4. Complete the setup wizard

### 2. Enable Authentication

1. In Firebase Console → Authentication
2. Enable **Email/Password** (required)
3. Enable **Google** (recommended)

⚠️ **Important for Sign-In Crash Fix:**
- The app also tries to use Twitter and Apple sign-in
- If you don't enable these, the app may crash when clicking "Sign in"
- **Solution**: Either enable them OR remove them from code (see [AUTHENTICATION.md](AUTHENTICATION.md))

### 3. Set Up Firestore

1. In Firebase Console → Firestore Database
2. Click "Create database"
3. Start in **test mode** (for development)
4. Deploy the security rules from `firestore.rules` file

### 4. Configure Your App

1. Download `google-services.json` from Firebase Console
2. Place it in `app/` directory
3. Copy `secrets.properties.example` to `secrets.properties`
4. Build: `./gradlew clean build`

## Cost: FREE for Personal Use

Firebase free tier includes:
- Unlimited authentication users
- 50,000 Firestore reads/day
- 20,000 Firestore writes/day
- 1 GB storage

This is **more than enough** for personal use or small deployments.

## Complete Documentation

For detailed step-by-step instructions:

📖 **[Self-Hosted Firebase Setup Guide](docs/deployment/self-hosted-firebase.md)** (19KB, comprehensive)
- Complete setup instructions
- Firestore security rules
- Authentication configuration
- Troubleshooting guide
- Cost management
- Migration instructions

📖 **[Authentication Guide](AUTHENTICATION.md)** (7KB)
- Fix for sign-in crashes
- How to configure all providers
- Code examples for removing unused providers

📖 **[Quick Reference](FIREBASE.md)** (4.5KB)
- Quick troubleshooting
- Common commands
- File locations

## Files You Need to Know About

Created for you in this repository:

- `docs/deployment/self-hosted-firebase.md` - Main setup guide
- `AUTHENTICATION.md` - Authentication providers configuration
- `FIREBASE.md` - Quick reference
- `firestore.rules` - Security rules (copy to Firebase Console)
- `firebase.json` - Firebase CLI configuration
- `google-services.json.template` - Template (DON'T use directly)

Files you need to create:

- `app/google-services.json` - Download from YOUR Firebase Console
- `secrets.properties` - Copy from `secrets.properties.example`

## Common Issue: Sign-In Crash

**Problem**: App crashes when clicking "Sign in"

**Why**: The app code includes Twitter and Apple authentication, but these aren't enabled in your Firebase project.

**Quick Fix** (choose one):

**Option A**: Enable all providers in Firebase Console
- Go to Authentication → Sign-in method
- Enable Email, Google, Twitter, and Apple

**Option B**: Remove unused providers from code
- Edit `app/src/main/java/com/shelbeely/opentransition/ui/settings/SettingsFragment.kt`
- Comment out Twitter and Apple lines (around line 636-637)
- Rebuild the app

See [AUTHENTICATION.md](AUTHENTICATION.md) for detailed instructions.

## Next Steps

1. **Read** the full guide: [docs/deployment/self-hosted-firebase.md](docs/deployment/self-hosted-firebase.md)
2. **Create** your Firebase project
3. **Configure** authentication (at minimum: Email + Google)
4. **Download** `google-services.json`
5. **Build** and test the app

## Benefits of Self-Hosting

✅ **Complete Control** - Your Firebase project, your rules  
✅ **Data Privacy** - No data shared with original project  
✅ **Independence** - No dependency on original infrastructure  
✅ **Free Tier** - No cost for personal/small use  
✅ **Easy Setup** - 15 minutes to get running  
✅ **No Server Code** - Firebase handles everything  

## What You DON'T Need

❌ No server to deploy or maintain  
❌ No database server to set up  
❌ No domain name required  
❌ No SSL certificates to manage  
❌ No DevOps knowledge needed  
❌ No hosting costs (free tier)  

## Support

- 📖 [Full Setup Guide](docs/deployment/self-hosted-firebase.md)
- 🔐 [Authentication Guide](AUTHENTICATION.md)
- ⚡ [Quick Reference](FIREBASE.md)
- 🐛 [GitHub Issues](https://github.com/shelbeely/OpenTransition/issues)
- 💬 [GitHub Discussions](https://github.com/shelbeely/OpenTransition/discussions)

---

**Ready to get started?** Go to [docs/deployment/self-hosted-firebase.md](docs/deployment/self-hosted-firebase.md) for the complete guide!
