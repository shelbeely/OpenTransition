# Self-Hosted Firebase Setup

This guide explains how to set up your own Firebase project for OpenTransition, giving you complete control over user authentication and data synchronization independent from the original project.

## Overview

OpenTransition uses Firebase for:

- **Authentication**: User accounts with email/password and Google sign-in
- **Firestore Database**: Cloud synchronization of user settings across devices
- **Crashlytics**: Optional crash reporting
- **Analytics**: Optional usage analytics

By hosting your own Firebase project, you gain:

- ✅ Complete data ownership and control
- ✅ Independence from the original OpenTransition/TransTracks infrastructure
- ✅ Privacy for your users
- ✅ Ability to customize authentication and storage rules
- ✅ Control over costs and scaling

## Prerequisites

- Google account
- Firebase project (free tier is sufficient for personal/small deployments)
- OpenTransition source code cloned locally
- Android Studio installed

## Step 1: Create Your Firebase Project

### 1.1 Navigate to Firebase Console

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Sign in with your Google account

### 1.2 Create New Project

1. Click **"Add project"** or **"Create a project"**
2. Enter a project name (e.g., "MyOpenTransition" or "OpenTransition-Personal")
3. (Optional) Modify the project ID if desired
4. Click **"Continue"**

### 1.3 Configure Google Analytics

1. Choose whether to enable Google Analytics (optional but recommended for monitoring)
2. If enabled, select or create an Analytics account
3. Accept the terms and click **"Create project"**
4. Wait for project creation to complete (30-60 seconds)

## Step 2: Add Android App to Firebase

### 2.1 Register Your App

1. In the Firebase Console, click the Android icon to add an Android app
2. Fill in the required information:
   - **Android package name**: `com.shelbeely.opentransition`
     - ⚠️ This MUST match exactly, or you can customize it (see "Using Custom Package Name" below)
   - **App nickname**: (Optional) e.g., "OpenTransition Android"
   - **Debug signing certificate SHA-1**: (Optional for development)
3. Click **"Register app"**

### 2.2 Download Configuration File

1. Download the `google-services.json` file
2. Save it to your local machine

### 2.3 Add Configuration to Your Project

1. Copy `google-services.json` to the `app/` directory of your OpenTransition project:
   ```
   OpenTransition/
   └── app/
       └── google-services.json  ← Place it here
   ```

2. Verify the file is in the correct location:
   ```bash
   ls -la app/google-services.json
   ```

!!! warning "Security Note"
    Never commit `google-services.json` to version control. It's already listed in `.gitignore`, but double-check before pushing changes.

## Step 3: Configure Firebase Authentication

### 3.1 Enable Authentication Methods

1. In Firebase Console, go to **Authentication** (Build menu on left)
2. Click **"Get started"** if this is your first time
3. Go to the **"Sign-in method"** tab

### 3.2 Enable Email/Password Authentication

1. Click on **"Email/Password"**
2. Toggle **"Enable"** to ON
3. (Optional) Enable **"Email link (passwordless sign-in)"** if desired
4. Click **"Save"**

### 3.3 Enable Google Sign-In

1. Click on **"Google"**
2. Toggle **"Enable"** to ON
3. Enter a project support email (your email address)
4. Click **"Save"**

### 3.4 Enable Twitter Sign-In (Optional but Recommended)

!!! warning "Important: Avoid Sign-In Crashes"
    The app is configured to offer Twitter and Apple sign-in options. If these providers are not enabled in Firebase, the sign-in screen may crash or fail. Enable them or modify the code to remove them (see "Disabling Optional Providers" below).

To enable Twitter sign-in:

1. Click on **"Twitter"**
2. Follow the instructions to create a Twitter app at https://developer.twitter.com/
3. Get your Twitter API Key and API Secret
4. Enter them in Firebase
5. Add the callback URL from Firebase to your Twitter app settings
6. Toggle **"Enable"** to ON
7. Click **"Save"**

### 3.5 Enable Apple Sign-In (Optional but Recommended)

To enable Apple sign-in:

1. Click on **"Apple"**
2. Follow the instructions to configure Apple authentication
3. You'll need an Apple Developer account
4. Configure Service ID and other Apple-specific settings
5. Toggle **"Enable"** to ON
6. Click **"Save"**

### 3.6 Disabling Optional Providers (Alternative)

If you don't want to set up Twitter and Apple sign-in, you need to modify the app code:

1. Open `app/src/main/java/com/shelbeely/opentransition/ui/settings/SettingsFragment.kt`
2. Find the `showAuth()` function (around line 631)
3. Remove or comment out the Twitter and Apple builder lines:

```kotlin
private fun showAuth() {
    val mainActivity = activity as? MainActivity ?: return
    val providers = arrayListOf(
        AuthUI.IdpConfig.EmailBuilder().build(),
        AuthUI.IdpConfig.GoogleBuilder().build(),
        // AuthUI.IdpConfig.TwitterBuilder().build(),  // Commented out
        // AuthUI.IdpConfig.AppleBuilder().build()     // Commented out
    )

    mainActivity.signInLauncher.launch(
        AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(providers)
            .build()
    )
}
```

4. Rebuild the app:
   ```bash
   ./gradlew clean build
   ```

### 3.7 Configure Authorized Domains (Optional)

For production deployments:

1. Go to **"Settings"** tab in Authentication
2. Under **"Authorized domains"**, add your custom domains if any
3. For development, `localhost` is already authorized

## Step 4: Set Up Firestore Database

### 4.1 Create Firestore Database

1. In Firebase Console, go to **Firestore Database** (Build menu)
2. Click **"Create database"**
3. Choose a starting mode:
   - **Test mode**: Good for development (open access for 30 days)
   - **Production mode**: More secure, recommended for production
4. Choose a location:
   - Select a region close to your users (e.g., `us-east1`, `europe-west1`)
   - ⚠️ Cannot be changed after creation
5. Click **"Enable"**

### 4.2 Configure Security Rules

OpenTransition uses Firestore to store user settings. Here's the recommended security rules configuration:

1. Go to **Firestore Database** → **Rules** tab
2. Replace the default rules with the following:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // User settings document
    // Each user has a collection with their UID containing a settings document
    match /{userId}/{document=**} {
      // Users can only read/write their own data
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
  }
}
```

3. Click **"Publish"** to save the rules

!!! info "Security Rules Explanation"
    These rules ensure that:
    - Users must be authenticated to access data
    - Users can only access their own data (identified by their UID)
    - No user can read or modify another user's data

### 4.3 Understanding the Data Structure

OpenTransition stores settings in the following structure:

```
Firestore Root
└── {userId} (collection - user's Firebase Auth UID)
    └── settings (document)
        ├── lockCode: String
        ├── lockDelay: String
        ├── lockType: String
        ├── startDate: Long
        ├── theme: String
        ├── enableAnalytics: Boolean
        ├── enableCrashReports: Boolean
        ├── showAds: Boolean
        └── showWelcome: Boolean
```

This structure is created automatically when a user signs in and enables cloud sync in the app.

## Step 5: Configure Optional Services

### 5.1 Firebase Crashlytics (Optional)

For crash reporting:

1. Go to **Crashlytics** in Firebase Console
2. Click **"Enable Crashlytics"**
3. Follow the setup wizard
4. The app is already configured to use Crashlytics

### 5.2 Firebase Analytics (Optional)

Analytics is automatically enabled when you create the project. To configure:

1. Go to **Analytics** → **Events**
2. Review tracked events
3. Configure custom audiences or conversions as needed

Users can opt-out of analytics within the app settings.

## Step 6: Build and Deploy Your App

### 6.1 Verify Configuration

1. Ensure `google-services.json` is in `app/` directory
2. Ensure `secrets.properties` exists (copy from `secrets.properties.example`)
3. Verify Firebase is enabled in `app/build.gradle` (already configured)

### 6.2 Build the App

```bash
# Clean build
./gradlew clean

# Build debug APK
./gradlew assembleDebug

# Or build and install on connected device
./gradlew installDebug
```

### 6.3 Test Authentication

1. Launch the app on a device/emulator
2. Open Settings → Account
3. Sign in with email/password or Google
4. Verify authentication succeeds
5. Enable "Save to Cloud" option
6. Modify a setting (e.g., change theme)
7. Check Firestore Console to see the data synced

## Using a Custom Package Name

If you want to use a custom package name (e.g., for your own branding):

### 1. Update Firebase

1. Register a new Android app in Firebase with your custom package name
2. Download the new `google-services.json` with your package name

### 2. Update App Code

1. Rename package in `app/build.gradle`:
   ```gradle
   android {
       defaultConfig {
           applicationId "com.yourname.yourapptransition"
           // ...
       }
   }
   ```

2. Refactor package name in Android Studio:
   - Right-click on package in Project view
   - Refactor → Rename
   - Choose "Rename package"
   - Enter new package name

3. Update `AndroidManifest.xml` if needed

4. Clean and rebuild:
   ```bash
   ./gradlew clean build
   ```

## Managing Multiple Firebase Projects

You can maintain multiple Firebase configurations for different environments:

### Development and Production

1. Create two Firebase projects:
   - `OpenTransition-Dev` (for testing)
   - `OpenTransition-Prod` (for production)

2. Use different `google-services.json` files:
   ```
   app/
   ├── google-services.json          # Default (development)
   └── google-services-prod.json     # Production
   ```

3. Configure build variants in `app/build.gradle`:
   ```gradle
   android {
       buildTypes {
           debug {
               // Uses default google-services.json
           }
           release {
               // Copy google-services-prod.json to google-services.json
               // during release builds
           }
       }
   }
   ```

## Cost Considerations

Firebase offers a generous free tier:

### Free Tier Limits (Spark Plan)

- **Authentication**: Unlimited users
- **Firestore**: 
  - 1 GB storage
  - 50K reads/day
  - 20K writes/day
  - 20K deletes/day
- **Hosting**: 10 GB storage, 360 MB/day bandwidth
- **Crashlytics**: Unlimited

### Typical Usage for Personal App

For a small deployment (1-100 users):
- Settings sync generates minimal reads/writes
- Free tier is usually sufficient
- Authentication has no limits

### Upgrade Options

If you exceed free tier:
- **Blaze Plan**: Pay-as-you-go
- First 50K reads/20K writes still free
- Additional reads: $0.06 per 100K
- Additional writes: $0.18 per 100K

## Troubleshooting

### App Crashes When Clicking Sign In ⚠️

**Symptoms**: App crashes or shows error immediately when clicking "Sign in" button

**Root Cause**: The app is configured to offer Twitter and Apple sign-in options, but these providers are not enabled in your Firebase project.

**Solutions** (Choose one):

**Option A: Enable All Providers (Recommended)**
1. Go to Firebase Console → Authentication → Sign-in method
2. Enable **Email/Password** (required)
3. Enable **Google** (required)
4. Enable **Twitter** (follow setup wizard)
5. Enable **Apple** (follow setup wizard, requires Apple Developer account)

**Option B: Remove Unsupported Providers from Code**
1. Edit `app/src/main/java/com/shelbeely/opentransition/ui/settings/SettingsFragment.kt`
2. Find the `showAuth()` function (around line 631)
3. Comment out Twitter and Apple lines:
   ```kotlin
   private fun showAuth() {
       val mainActivity = activity as? MainActivity ?: return
       val providers = arrayListOf(
           AuthUI.IdpConfig.EmailBuilder().build(),
           AuthUI.IdpConfig.GoogleBuilder().build(),
           // AuthUI.IdpConfig.TwitterBuilder().build(),  // Removed
           // AuthUI.IdpConfig.AppleBuilder().build()     // Removed
       )
       mainActivity.signInLauncher.launch(
           AuthUI.getInstance().createSignInIntentBuilder()
               .setAvailableProviders(providers).build()
       )
   }
   ```
4. Rebuild: `./gradlew clean build`
5. Reinstall: `./gradlew installDebug`

**Option C: Enable Only Some Providers**
Mix and match based on what you want to support. Just ensure whatever providers are in the code are enabled in Firebase Console.

### App Can't Connect to Firebase

**Symptoms**: Authentication fails, "Failed to connect" errors

**Solutions**:
1. Verify `google-services.json` is in `app/` directory
2. Ensure package name matches: `com.shelbeely.opentransition`
3. Check Firebase Console → Project Settings → Your apps
4. Verify SHA-1 fingerprint is added (for Google Sign-In)

### Settings Don't Sync to Cloud

**Symptoms**: Settings aren't saved to Firestore

**Solutions**:
1. Check user is authenticated (Settings → Account)
2. Verify "Save to Cloud" is enabled
3. Check Firestore security rules allow access
4. View Firestore Console to see if document is created
5. Check app logs for error messages

### Authentication Fails

**Symptoms**: Can't sign in with email or Google

**Solutions**:
1. Verify Authentication is enabled in Firebase Console
2. For email: Check email/password provider is enabled
3. For Google: Verify Google Sign-In is enabled
4. Check authorized domains in Firebase Console
5. Ensure `google-services.json` matches your Firebase project

### Firestore Permission Denied

**Symptoms**: "Permission denied" when writing to Firestore

**Solutions**:
1. Check Firestore security rules in Firebase Console
2. Verify user is authenticated (`request.auth != null`)
3. Ensure rules allow access to user's own collection
4. Check user UID matches collection path

### SHA-1 Fingerprint Issues

For Google Sign-In, you need to add SHA-1 fingerprints:

#### Get Debug Keystore SHA-1

```bash
keytool -list -v -keystore keys/debug-keystore.jks -alias transtracks -storepass debugkey -keypass debugkey
```

#### Get Release Keystore SHA-1

```bash
keytool -list -v -keystore keys/release-keystore.jks -alias transtracks
```

#### Add to Firebase

1. Go to Firebase Console → Project Settings
2. Scroll to "Your apps"
3. Click on your Android app
4. Add SHA-1 fingerprints

## Data Migration

If migrating from another Firebase project:

### Export Data from Old Project

1. Go to old Firebase Console → Firestore Database
2. Click on "Import/Export"
3. Export to Google Cloud Storage bucket

### Import Data to New Project

1. Go to new Firebase Console → Firestore Database
2. Click on "Import/Export"
3. Import from the exported bucket
4. ⚠️ Ensure user UIDs match or users will need to re-authenticate

## Security Best Practices

### 1. Protect Your Configuration Files

- Never commit `google-services.json` to public repositories
- Use environment variables for sensitive keys
- Rotate credentials if accidentally exposed

### 2. Configure Firestore Security Rules

- Always use strict security rules in production
- Test rules before deploying
- Use Firebase Emulator Suite for local testing

### 3. Monitor Usage

- Set up billing alerts in Google Cloud Console
- Monitor authentication activity
- Review Firestore usage regularly

### 4. User Privacy

- Only sync necessary settings to cloud
- Inform users about cloud sync (app already does this)
- Provide opt-out options (app already has this)
- Consider data retention policies

## Support and Resources

### Official Documentation

- [Firebase Documentation](https://firebase.google.com/docs)
- [Firebase Android SDK](https://firebase.google.com/docs/android/setup)
- [Firestore Security Rules](https://firebase.google.com/docs/firestore/security/get-started)
- [Firebase Authentication](https://firebase.google.com/docs/auth)

### OpenTransition Resources

- [Development Setup](../getting-started/development-setup.md)
- [Architecture Overview](../architecture/overview.md)
- [Contributing Guidelines](../contributing/guidelines.md)

### Community

- [GitHub Issues](https://github.com/shelbeely/OpenTransition/issues)
- [GitHub Discussions](https://github.com/shelbeely/OpenTransition/discussions)

## Frequently Asked Questions

### Q: Do I need to host my own Firebase project?

**A:** No, it's optional. OpenTransition can work without Firebase (local-only mode). Firebase is only needed for cloud sync and multi-device support.

### Q: Can I use other backends instead of Firebase?

**A:** The current implementation is Firebase-specific. However, you could modify the `FirebaseSettingUtil` and `SettingsManager` classes to use a different backend. This would require code changes.

### Q: Is my data private with self-hosted Firebase?

**A:** Yes. With your own Firebase project:
- You control the project and billing account
- Data is stored in your Firebase instance
- No one else has access to your Firebase Console
- Users' data is isolated by authentication

### Q: What happens if I exceed the free tier?

**A:** Firebase will send you email notifications. You can:
- Upgrade to Blaze (pay-as-you-go) plan
- Optimize your usage
- Disable cloud sync for some users
- Monitor usage in Firebase Console

### Q: Can users migrate between Firebase projects?

**A:** Users would need to:
1. Export their data from the app (Settings → Backup)
2. Sign out from the old account
3. Update to app with new Firebase configuration
4. Sign in with new account
5. Import their data backup

The app doesn't automatically migrate between Firebase projects.

### Q: How do I backup my Firebase data?

**A:** Options:
1. Use Firestore Import/Export to Google Cloud Storage
2. Use Firebase Admin SDK to export data programmatically
3. Users can export from within the app (Settings → Backup)

---

**Congratulations!** You now have a self-hosted Firebase instance for OpenTransition with complete control over your users' authentication and data synchronization.
