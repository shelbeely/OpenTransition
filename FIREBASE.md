# Firebase Setup Quick Reference

This is a quick reference for setting up Firebase for OpenTransition. For detailed instructions, see the [Self-Hosted Firebase Setup Guide](docs/deployment/self-hosted-firebase.md).

## What is Firebase?

Firebase is Google's Backend-as-a-Service (BaaS) platform that OpenTransition uses for:
- **Authentication**: User accounts and sign-in
- **Firestore**: Cloud storage for settings synchronization
- **Crashlytics**: Crash reporting (optional)
- **Analytics**: Usage tracking (optional)

## Do I Need Firebase?

**Short answer**: No, but it's recommended for cloud sync.

- **Local-only mode**: The app works fine without Firebase. All data stays on device.
- **Cloud sync mode**: Firebase enables syncing settings across multiple devices.

## Quick Setup Steps

1. **Create Firebase Project**
   - Go to https://console.firebase.google.com/
   - Click "Add project"
   - Enter project name
   - Complete wizard

2. **Add Android App**
   - Click Android icon in Firebase Console
   - Package name: `com.shelbeely.opentransition`
   - Download `google-services.json`
   - Place in `app/` directory

3. **Enable Authentication**
   - Firebase Console → Authentication
   - Enable Email/Password
   - Enable Google Sign-In

4. **Enable Firestore**
   - Firebase Console → Firestore Database
   - Create database (test mode for dev)
   - Deploy security rules from `firestore.rules`

5. **Build and Run**
   ```bash
   ./gradlew clean build
   ./gradlew installDebug
   ```

## Files in This Repository

- `firestore.rules` - Firestore security rules (deploy to Firebase Console)
- `firebase.json` - Firebase CLI configuration
- `google-services.json.template` - Template showing structure (don't use directly)
- `app/google-services.json` - Your actual config (not in git, you create this)
- `AUTHENTICATION.md` - Detailed guide for authentication providers
- `FIREBASE.md` - This quick reference file

## ⚠️ Important: Sign-In Crash Fix

**If the app crashes when you click "Sign in"**, it's because Twitter and Apple authentication are in the code but not enabled in Firebase.

**Quick Fix**: Choose one option:
1. **Enable all providers** in Firebase Console (Email, Google, Twitter, Apple)
2. **Remove unused providers** from code - see [AUTHENTICATION.md](AUTHENTICATION.md) for instructions

## Security Rules

The `firestore.rules` file contains the security rules for Firestore. Deploy them:

**Option 1: Firebase Console**
1. Open Firebase Console → Firestore → Rules
2. Copy contents of `firestore.rules`
3. Paste and publish

**Option 2: Firebase CLI**
```bash
npm install -g firebase-tools
firebase login
firebase init firestore
firebase deploy --only firestore:rules
```

## Cost

Firebase has a generous free tier:
- **Authentication**: Unlimited users
- **Firestore**: 50K reads/day, 20K writes/day, 1GB storage
- For personal use or small apps, free tier is usually sufficient

## Troubleshooting

### "App crashes when clicking Sign In" ⚠️
**Most Common Issue!**
- The app tries to use Twitter and Apple sign-in
- These providers must be enabled in Firebase OR removed from code
- See [AUTHENTICATION.md](AUTHENTICATION.md) for detailed fix

### "google-services.json not found"
- Download from Firebase Console → Project Settings → Your Apps
- Place in `app/` directory (not `app/src/`)

### "Authentication failed"
- Verify Email/Password enabled in Firebase Console
- Verify Google Sign-In enabled in Firebase Console
- Check package name matches: `com.shelbeely.opentransition`

### "Permission denied" in Firestore
- Deploy the security rules from `firestore.rules`
- Verify user is authenticated
- Check Firebase Console → Firestore → Rules

### "SHA-1 fingerprint required"
For Google Sign-In:
```bash
keytool -list -v -keystore keys/debug-keystore.jks \
  -alias transtracks -storepass debugkey -keypass debugkey
```
Add the SHA-1 to Firebase Console → Project Settings → Your Apps

## Documentation Links

- **Full Guide**: [docs/deployment/self-hosted-firebase.md](docs/deployment/self-hosted-firebase.md)
- **Development Setup**: [docs/getting-started/development-setup.md](docs/getting-started/development-setup.md)
- **Firebase Docs**: https://firebase.google.com/docs
- **Firestore Rules**: https://firebase.google.com/docs/firestore/security/get-started

## Support

- [GitHub Issues](https://github.com/shelbeely/OpenTransition/issues)
- [GitHub Discussions](https://github.com/shelbeely/OpenTransition/discussions)
- [Firebase Documentation](https://firebase.google.com/docs)
