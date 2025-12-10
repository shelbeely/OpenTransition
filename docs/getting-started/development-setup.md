# Development Setup

This guide walks you through setting up your development environment for OpenTransition.

## Step 1: Install Android Studio

1. Download [Android Studio](https://developer.android.com/studio) for your platform
2. Run the installer and follow the setup wizard
3. Install the Android SDK and necessary build tools

!!! tip "SDK Setup"
    During installation, ensure you install:
    
    - Android SDK Platform 36
    - Android SDK Build-Tools
    - Android Emulator
    - Android SDK Platform-Tools

## Step 2: Install Java Development Kit (JDK)

OpenTransition requires JDK 17.

### Option 1: Install via Android Studio

Android Studio includes a JDK. You can use it by configuring your project to use the embedded JDK.

### Option 2: Install Separately

=== "Windows"
    ```powershell
    # Download JDK 17 from Oracle or use a package manager
    # Then set JAVA_HOME environment variable
    setx JAVA_HOME "C:\Program Files\Java\jdk-17"
    ```

=== "macOS"
    ```bash
    # Using Homebrew
    brew install openjdk@17
    
    # Set JAVA_HOME in your shell profile
    echo 'export JAVA_HOME=$(/usr/libexec/java_home -v 17)' >> ~/.zshrc
    source ~/.zshrc
    ```

=== "Linux"
    ```bash
    # Ubuntu/Debian
    sudo apt update
    sudo apt install openjdk-17-jdk
    
    # Set JAVA_HOME
    echo 'export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64' >> ~/.bashrc
    source ~/.bashrc
    ```

Verify installation:

```bash
java -version
# Should show version 17.x.x
```

## Step 3: Clone the Repository

```bash
git clone https://github.com/shelbeely/OpenTransition.git
cd OpenTransition
```

## Step 4: Set Up Firebase

OpenTransition uses Firebase for authentication, cloud storage, analytics, and crash reporting.

### Create a Firebase Project

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Click "Add project" or "Create a project"
3. Enter a project name (e.g., "OpenTransition Dev")
4. Follow the setup wizard (Analytics is optional for development)

### Add Android App to Firebase

1. In the Firebase Console, click "Add app" and select Android
2. **Package name**: `com.shelbeely.opentransition`
3. **App nickname**: Optional (e.g., "OpenTransition Android")
4. **Debug signing certificate SHA-1**: Optional for development
5. Click "Register app"

### Download Configuration File

1. Download the `google-services.json` file
2. Place it in the `app/` directory of your project:
   ```
   OpenTransition/
   └── app/
       └── google-services.json  ← Place it here
   ```

!!! warning "Security Note"
    The `google-services.json` file contains sensitive API keys and should never be committed to version control. It's already listed in `.gitignore`.

### Enable Firebase Services

Enable the following services in your Firebase project:

1. **Authentication**
   - Go to Authentication > Sign-in method
   - Enable "Email/Password"
   - Enable "Google" sign-in

2. **Firestore Database**
   - Go to Firestore Database
   - Click "Create database"
   - Start in **test mode** for development
   - Choose a location close to your users

3. **Crashlytics** (Optional for development)
   - Go to Crashlytics
   - Click "Enable Crashlytics"

4. **Analytics** (Optional for development)
   - Automatically enabled when you create the project

## Step 5: Configure Secrets

OpenTransition uses a `secrets.properties` file for sensitive configuration.

1. Copy the example file:
   ```bash
   cp secrets.properties.example secrets.properties
   ```

2. Open `secrets.properties` and configure the values:

```properties
# Firebase AdMob App ID (use test ID for development)
ADS_APP_ID=ca-app-pub-3940256099942544~3347511713

# AdMob Ad Unit IDs (test IDs for development)
ADS_GALLERY_AD_ID=ca-app-pub-3940256099942544/9214589741
ADS_HOME_AD_ID=ca-app-pub-3940256099942544/9214589741
ADS_MILESTONES_AD_ID=ca-app-pub-3940256099942544/9214589741
ADS_SETTINGS_AD_ID=ca-app-pub-3940256099942544/9214589741

# Security salt for PIN/Pattern hashing (generate a random string)
CODE_SALT=your_random_salt_string_here_change_this
```

!!! tip "Generating CODE_SALT"
    Generate a secure random string for `CODE_SALT`:
    
    ```bash
    # Linux/macOS
    openssl rand -base64 32
    
    # Or use any random string generator
    ```

!!! warning "Production Values"
    The example values are test/development values. Never use them in production. Get your own AdMob IDs from [AdMob Console](https://apps.admob.com/).

## Step 6: Open Project in Android Studio

1. Launch Android Studio
2. Select "Open an Existing Project"
3. Navigate to the cloned `OpenTransition` directory
4. Click "OK"

Android Studio will:
- Import the Gradle project
- Download dependencies
- Index the project files

This may take several minutes on the first run.

## Step 7: Sync Gradle

Once the project is opened:

1. Click "Sync Project with Gradle Files" button (or File > Sync Project with Gradle Files)
2. Wait for the sync to complete
3. Resolve any errors that appear

!!! note "Common Sync Issues"
    - **Missing google-services.json**: Ensure the file is in `app/` directory
    - **Missing secrets.properties**: Ensure you copied and configured it
    - **SDK not found**: Install the required SDK version (36) in SDK Manager

## Step 8: Configure an Android Virtual Device (AVD)

If you don't have a physical Android device:

1. Open AVD Manager (Tools > Device Manager)
2. Click "Create Virtual Device"
3. Select a device definition (e.g., Pixel 5)
4. Select a system image (API 34 or higher recommended)
5. Click "Finish"

## Step 9: Build and Run

### Using Android Studio

1. Select your device/emulator from the device dropdown
2. Click the "Run" button (green play icon) or press `Shift + F10`
3. Wait for the build to complete and the app to launch

### Using Command Line

```bash
# Debug build
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug

# Run tests
./gradlew test

# Build and run
./gradlew build
```

## Verify Your Setup

If everything is configured correctly:

1. The app builds without errors
2. The app launches on your device/emulator
3. You can create an account or sign in
4. You can navigate through the app

## Troubleshooting

### Build Fails with "google-services.json missing"

**Solution**: Ensure `google-services.json` is in the `app/` directory.

### Build Fails with "secrets.properties not found"

**Solution**: Copy `secrets.properties.example` to `secrets.properties` and configure it.

### Gradle Sync Issues

**Solution**: 
```bash
# Clear Gradle cache
./gradlew clean
./gradlew build --refresh-dependencies
```

### SDK Licenses Not Accepted

**Solution**:
```bash
# Accept all SDK licenses
yes | $ANDROID_HOME/tools/bin/sdkmanager --licenses
```

### Firebase Authentication Doesn't Work

**Solution**: 
- Verify `google-services.json` is correct
- Check Firebase Console for enabled authentication methods
- Ensure app package name matches: `com.shelbeely.opentransition`

## Next Steps

Now that your environment is set up, learn how to build the app:

→ [Building the App](building.md)

## Additional Configuration

### Debug Keystore

For development, the project uses a debug keystore located at `keys/debug-keystore.jks`. This is included in the repository for development convenience.

**Credentials**:
- Store Password: `debugkey`
- Key Alias: `transtracks`
- Key Password: `debugkey`

!!! warning "Production Keystore"
    Never commit your production keystore to version control. Use environment variables or secure secret management for production releases.

### ProGuard Configuration

The release build uses ProGuard for code obfuscation and optimization. The rules are defined in `app/proguard-rules.pro`.

For development, ProGuard is disabled in debug builds for faster build times.
