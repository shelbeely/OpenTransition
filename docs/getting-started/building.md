# Building the App

Learn how to build, run, and package OpenTransition for different environments.

## Build Variants

OpenTransition has two build variants:

### Debug Build
- Used for development and testing
- Includes debug symbols
- No code obfuscation
- Test AdMob IDs
- LeakCanary memory leak detection
- Crashlytics disabled
- Faster build times

### Release Build
- Used for production
- Code obfuscation with ProGuard
- Production AdMob IDs
- Crashlytics enabled
- Optimized and minified
- Requires release keystore

## Building Debug Version

### Using Android Studio

1. Open the project in Android Studio
2. Select "Build" > "Build Bundle(s) / APK(s)" > "Build APK(s)"
3. Or click the "Run" button to build and install

### Using Gradle Command Line

```bash
# Build mobile debug APK
./gradlew :mobile:assembleDebug

# Build Wear OS debug APK
./gradlew :wear:assembleDebug

# Build all modules
./gradlew assembleDebug

# Output location (mobile)
ls -lh mobile/build/outputs/apk/debug/mobile-debug.apk
```

### Install Debug APK

```bash
# Install mobile app on connected device/emulator
./gradlew :mobile:installDebug

# Install Wear OS app (requires paired Wear OS device)
./gradlew :wear:installDebug

# Or use adb directly
adb install mobile/build/outputs/apk/debug/mobile-debug.apk
```

## Building Release Version

!!! warning "Release Keystore Required"
    Building a release version requires a release keystore. For production releases, you need a properly configured keystore with secure credentials.

### Prerequisites

1. **Release Keystore**: Generate or obtain your release keystore
2. **Environment Variables**: Set the following:
   - `STORE_PASS`: Keystore password
   - `KEY_ALIAS`: Key alias
   - `KEY_PASS`: Key password
3. **Keystore File**: Place at `keys/release-keystore.jks`

### Generate a Release Keystore (First Time Only)

```bash
# Navigate to keys directory
cd keys

# Generate keystore
keytool -genkey -v -keystore release-keystore.jks \
  -alias opentransition \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000
```

Follow the prompts to set passwords and enter your information.

!!! danger "Security Critical"
    - Never commit the release keystore to version control
    - Store keystore and passwords securely (use a password manager)
    - Back up your keystore in a secure location
    - Loss of keystore means you cannot update your app on Play Store

### Build Release APK

=== "Linux/macOS"
    ```bash
    export STORE_PASS="your_store_password"
    export KEY_ALIAS="your_key_alias"
    export KEY_PASS="your_key_password"
    
    ./gradlew :mobile:assembleRelease
    ```

=== "Windows (PowerShell)"
    ```powershell
    $env:STORE_PASS="your_store_password"
    $env:KEY_ALIAS="your_key_alias"
    $env:KEY_PASS="your_key_password"
    
    .\gradlew.bat :mobile:assembleRelease
    ```

The signed APK will be at: `mobile/build/outputs/apk/release/mobile-release.apk`

### Build Android App Bundle (AAB)

For Google Play Store distribution, build an Android App Bundle:

```bash
# Set environment variables (as shown above)
export STORE_PASS="your_store_password"
export KEY_ALIAS="your_key_alias"
export KEY_PASS="your_key_password"

# Build bundle
./gradlew :mobile:bundleRelease
```

The AAB will be at: `mobile/build/outputs/bundle/release/mobile-release.aab`

## Running on Device/Emulator

### Run on Connected Device

1. Enable USB debugging on your Android device
2. Connect via USB
3. Verify device is recognized:
   ```bash
   adb devices
   ```
4. Run from Android Studio or use:
   ```bash
   ./gradlew :mobile:installDebug
   ```

### Run on Emulator

1. Start your AVD from Android Studio Device Manager
2. Wait for emulator to boot
3. Run from Android Studio or use:
   ```bash
   ./gradlew :mobile:installDebug
   ```

### Run Specific Build Variant

```bash
# Install and run debug
./gradlew :mobile:installDebug
adb shell am start -n com.shelbeely.opentransition/.ui.MainActivity

# Install and run release (requires keystore)
./gradlew :mobile:installRelease
adb shell am start -n com.shelbeely.opentransition/.ui.MainActivity
```

## Testing Builds

### Run Unit Tests

```bash
# Run all mobile unit tests
./gradlew :mobile:testDebugUnitTest

# Run all module tests
./gradlew test

# View test report
open mobile/build/reports/tests/testDebugUnitTest/index.html
```

### Run Instrumentation Tests

```bash
# Run on connected device/emulator
./gradlew :mobile:connectedAndroidTest

# View test report
open mobile/build/reports/androidTests/connected/index.html
```

### Run Lint Checks

```bash
# Run mobile lint analysis
./gradlew :mobile:lintDebug

# View lint report
open mobile/build/reports/lint-results-debug.html
```

## Build Optimization

### Clean Build

```bash
# Clean previous builds
./gradlew clean

# Clean and build
./gradlew clean build
```

### Speed Up Builds

#### Enable Gradle Daemon

Add to `~/.gradle/gradle.properties`:

```properties
org.gradle.daemon=true
org.gradle.parallel=true
org.gradle.configureondemand=true
org.gradle.caching=true
```

#### Increase Heap Size

Add to `gradle.properties` in project root:

```properties
org.gradle.jvmargs=-Xmx4g -XX:MaxPermSize=2048m -XX:+HeapDumpOnOutOfMemoryError
```

#### Use Build Cache

```bash
./gradlew build --build-cache
```

## Build Configuration

### Version Information

Version information is defined in `mobile/build.gradle`:

```gradle
def major = 1
def minor = 3

def getVersionCode = { ->
    def envBuildNumber = System.getenv("BUILD_NUMBER")?.toInteger() ?: 0
    def baseBuildNumber = 25
    return baseBuildNumber + (major * 100) + minor + envBuildNumber
}

def getVersionName = { ->
    return "$major.$minor.${getVersionCode()}"
}
```

- **Major**: Incremented for major releases
- **Minor**: Incremented for minor updates
- **Build Number**: Auto-incremented in CI/CD (defaults to 0 locally)

### Changing Package Name

If forking for your own deployment:

1. Update `applicationId` in `mobile/build.gradle`:
   ```gradle
   defaultConfig {
       applicationId "com.yourname.yourapp"
       // ...
   }
   ```

2. Update package references in:
   - `AndroidManifest.xml`
   - All Kotlin source files
   - Firebase project configuration

3. Refactor package structure in Android Studio:
   - Right-click package in Project view
   - Refactor > Rename

## Build Outputs

### APK Files

Location: `mobile/build/outputs/apk/`

```
apk/
├── debug/
│   └── mobile-debug.apk
└── release/
    └── mobile-release.apk
```

### AAB Files

Location: `mobile/build/outputs/bundle/`

```
bundle/
└── release/
    └── mobile-release.aab
```

### Mapping Files (ProGuard)

Location: `mobile/build/outputs/mapping/release/`

```
mapping/
└── release/
    ├── mapping.txt        # Obfuscation mapping
    ├── seeds.txt          # Kept classes
    └── usage.txt          # Removed code
```

!!! important "Keep Mapping Files"
    Save `mapping.txt` for each release. You need it to:
    
    - Decode crash stack traces from ProGuard-obfuscated code
    - Debug production issues
    - Upload to Firebase Crashlytics for automatic deobfuscation

## Continuous Integration

OpenTransition uses GitHub Actions for CI/CD. See [CI/CD Documentation](../deployment/ci-cd.md) for details.

### Local CI Simulation

```bash
# Replicate CI build locally
cp ./secrets.properties.example ./secrets.properties
# Add your google-services.json to mobile/
./gradlew clean build

# Run all checks
./gradlew :mobile:check
```

## Troubleshooting

### Build Fails: "Execution failed for task ':mobile:processDebugGoogleServices'"

**Cause**: Missing or invalid `google-services.json`

**Solution**: Ensure `google-services.json` is in `mobile/` directory and is valid.

### Build Fails: "Could not read script '...secrets.properties'"

**Cause**: Missing `secrets.properties` file

**Solution**:
```bash
cp secrets.properties.example secrets.properties
```

### Release Build Fails: "Failed to read key from keystore"

**Cause**: Missing or incorrect release keystore

**Solution**:
- Ensure `keys/release-keystore.jks` exists
- Verify environment variables are set correctly
- Check keystore password is correct

### Out of Memory During Build

**Solution**: Increase Gradle heap size in `gradle.properties`:
```properties
org.gradle.jvmargs=-Xmx4096m
```

### Gradle Daemon Issues

**Solution**: Kill and restart daemon:
```bash
./gradlew --stop
./gradlew build
```

## Next Steps

- Learn about [Architecture](../architecture/overview.md)
- Explore [Features](../features/photo-tracking.md)
- Read [Development Guidelines](../development/code-style.md)
- Understand [CI/CD Pipeline](../deployment/ci-cd.md)
