# Authentication Providers Configuration

## Overview

OpenTransition uses Firebase Authentication with multiple sign-in providers. This file explains which providers are supported and how to configure them.

## Default Providers in Code

The app is currently configured to offer these sign-in options:

1. ✅ **Email/Password** - Required
2. ✅ **Google** - Recommended  
3. ⚠️ **Twitter** - Optional, requires setup
4. ⚠️ **Apple** - Optional, requires Apple Developer account

## Common Issue: App Crashes on Sign In

**Problem**: When you click "Sign in" in the app, it crashes or shows an error.

**Cause**: The app code includes Twitter and Apple sign-in, but these providers are not enabled in your Firebase project.

**Quick Fix**: You have two options:

### Option 1: Enable All Providers in Firebase (Recommended)

Follow the [Self-Hosted Firebase Setup Guide](docs/deployment/self-hosted-firebase.md#step-3-configure-firebase-authentication) to enable all providers.

### Option 2: Remove Unused Providers from Code

Edit `app/src/main/java/com/shelbeely/opentransition/ui/settings/SettingsFragment.kt`:

```kotlin
private fun showAuth() {
    val mainActivity = activity as? MainActivity ?: return
    val providers = arrayListOf(
        AuthUI.IdpConfig.EmailBuilder().build(),
        AuthUI.IdpConfig.GoogleBuilder().build()
        // Twitter and Apple removed for self-hosted setup
    )

    mainActivity.signInLauncher.launch(
        AuthUI.getInstance().createSignInIntentBuilder()
            .setAvailableProviders(providers).build()
    )
}
```

Then rebuild:
```bash
./gradlew clean build
./gradlew installDebug
```

## Provider Details

### Email/Password Authentication

**Status**: Required  
**Setup Difficulty**: Easy  
**Cost**: Free

**Firebase Setup**:
1. Go to Firebase Console → Authentication → Sign-in method
2. Click "Email/Password"
3. Toggle "Enable"
4. Click "Save"

**No additional configuration needed.**

### Google Sign-In

**Status**: Recommended  
**Setup Difficulty**: Easy  
**Cost**: Free

**Firebase Setup**:
1. Go to Firebase Console → Authentication → Sign-in method
2. Click "Google"
3. Toggle "Enable"
4. Enter project support email
5. Click "Save"

**Additional Requirements**:
- Add SHA-1 fingerprint to Firebase (for Android)
  ```bash
  keytool -list -v -keystore keys/debug-keystore.jks \
    -alias transtracks -storepass debugkey -keypass debugkey
  ```

### Twitter Sign-In

**Status**: Optional  
**Setup Difficulty**: Moderate  
**Cost**: Free (requires Twitter Developer account)

**Prerequisites**:
1. Twitter Developer account (https://developer.twitter.com/)
2. Create a Twitter app

**Firebase Setup**:
1. Go to Firebase Console → Authentication → Sign-in method
2. Click "Twitter"
3. Note the callback URL from Firebase
4. Go to Twitter Developer Portal:
   - Create new app or use existing
   - Get API Key and API Secret Key
   - Add Firebase callback URL to app settings
5. Enter Twitter API Key and Secret in Firebase
6. Toggle "Enable"
7. Click "Save"

**If you skip this**: Remove `AuthUI.IdpConfig.TwitterBuilder().build()` from code.

### Apple Sign-In

**Status**: Optional  
**Setup Difficulty**: Complex  
**Cost**: Requires Apple Developer Program membership ($99/year)

**Prerequisites**:
1. Apple Developer account
2. Apple Developer Program membership

**Firebase Setup**:
1. Go to Firebase Console → Authentication → Sign-in method
2. Click "Apple"
3. Follow Firebase's detailed Apple configuration wizard:
   - Create Service ID in Apple Developer Portal
   - Configure Sign in with Apple capability
   - Register domains and return URLs
   - Create private key for authentication
4. Enter Service ID, Team ID, Key ID, and Private Key in Firebase
5. Toggle "Enable"
6. Click "Save"

**If you skip this**: Remove `AuthUI.IdpConfig.AppleBuilder().build()` from code.

## Recommended Configurations

### For Personal/Self-Hosted Use

**Minimal Setup** (easiest):
```kotlin
val providers = arrayListOf(
    AuthUI.IdpConfig.EmailBuilder().build(),
    AuthUI.IdpConfig.GoogleBuilder().build()
)
```
- ✅ Easy to configure
- ✅ No additional accounts needed
- ✅ Works for most users

### For Public Distribution

**Full Setup** (best user experience):
```kotlin
val providers = arrayListOf(
    AuthUI.IdpConfig.EmailBuilder().build(),
    AuthUI.IdpConfig.GoogleBuilder().build(),
    AuthUI.IdpConfig.TwitterBuilder().build(),
    AuthUI.IdpConfig.AppleBuilder().build()
)
```
- ✅ Maximum user choice
- ✅ Better for Play Store distribution
- ⚠️ Requires setup for all providers

### For Development

**Email Only** (fastest):
```kotlin
val providers = arrayListOf(
    AuthUI.IdpConfig.EmailBuilder().build()
)
```
- ✅ Instant setup
- ✅ Good for testing
- ⚠️ Limited user experience

## Testing Authentication

After configuring providers:

1. Build and install the app:
   ```bash
   ./gradlew clean installDebug
   ```

2. Open the app and go to Settings

3. Tap "Sign in" or "Account"

4. Verify all configured providers appear

5. Test sign-in with each provider

## Debugging Authentication Issues

### Check Firebase Console

1. Go to Firebase Console → Authentication
2. Click "Sign-in method" tab
3. Verify your providers show "Enabled"

### Check Logcat

```bash
adb logcat | grep -i "firebase\|auth"
```

Look for error messages about missing configurations.

### Common Errors

**"Developer error: configuration is not valid"**
- Provider is in code but not enabled in Firebase
- Solution: Enable provider or remove from code

**"INVALID_CUSTOM_TOKEN"**
- Firebase configuration mismatch
- Solution: Verify `google-services.json` matches your project

**"Network error"**
- Firebase not reachable
- Solution: Check internet connection, verify API keys

## Security Best Practices

1. **Email/Password**:
   - Enforce strong password requirements in Firebase Console
   - Enable email verification
   - Set up password reset functionality

2. **Google Sign-In**:
   - Add SHA-1 fingerprints for all build types
   - Use different Google OAuth clients for debug/release

3. **Twitter Sign-In**:
   - Keep API keys secure
   - Use environment variables for sensitive data
   - Rotate keys periodically

4. **Apple Sign-In**:
   - Protect private key file
   - Never commit keys to version control
   - Use proper key management

## Additional Resources

- [Firebase Authentication Docs](https://firebase.google.com/docs/auth)
- [Firebase Auth UI Docs](https://firebase.google.com/docs/auth/android/firebaseui)
- [Self-Hosted Firebase Guide](docs/deployment/self-hosted-firebase.md)
- [Twitter Developer Portal](https://developer.twitter.com/)
- [Apple Sign In](https://developer.apple.com/sign-in-with-apple/)

## Support

If you continue to experience issues:

1. Check the [Self-Hosted Firebase Setup Guide](docs/deployment/self-hosted-firebase.md)
2. Review [GitHub Issues](https://github.com/shelbeely/OpenTransition/issues)
3. Check Firebase Console for error messages
4. Review app logs with `adb logcat`

---

**Quick Fix Reminder**: If sign-in crashes, either enable all providers in Firebase Console OR remove unused providers from `SettingsFragment.kt`.
