# UI Screenshots Automation

This repository automatically captures UI screenshots on multiple Android versions when visual changes are made to the Android app.

## How It Works

The GitHub Actions workflow (`.github/workflows/ui-screenshots.yml`) automatically runs when:
- Pull requests modify layouts (`app/src/main/res/layout/**`)
- Pull requests modify UI resources (`app/src/main/res/values/**`, `app/src/main/res/drawable/**`)
- Pull requests modify UI code (`app/src/main/java/**/ui/**`, `app/src/main/java/**/widget/**`)

## Multi-Version Testing

Screenshots are captured on **two Android versions** for comprehensive compatibility testing:

### Android 12 (API 31)
- **Device**: Pixel 5 emulator
- **Target**: Google APIs
- **Features**: Tests modern Android 12+ features including Material You, new permissions model
- **Artifact**: `ui-screenshots-api31-[PR#]`

### Android 10 (API 29) 
- **Device**: Pixel 3a emulator
- **Target**: Default (AOSP)
- **Features**: Tests pre-Android 12 compatibility, ensures backward compatibility
- **Artifact**: `ui-screenshots-api29-[PR#]`

## What Gets Captured

The workflow:
1. ✅ Builds the debug APK once (shared across all Android versions)
2. ✅ Launches Android emulators for both API levels in parallel
3. ✅ Installs and runs the app on each version
4. ✅ Navigates through key screens automatically
5. ✅ Captures screenshots at each step on each version
6. ✅ Generates separate HTML reports for each Android version
7. ✅ Posts a combined comment on the PR with links for both versions
8. ✅ Uploads separate artifacts for each Android version (30-day retention)

## Viewing Screenshots

### Option 1: PR Comment
When the workflow completes, a bot will comment on your PR with:
- Separate sections for each Android version
- List of captured screenshots per version
- Links to download artifacts for each version
- Screenshot counts and device information

### Option 2: Actions Artifacts
1. Go to the **Actions** tab in GitHub
2. Click on the "UI Screenshots" workflow run for your PR
3. Scroll to **Artifacts** section at the bottom
4. Download both artifacts:
   - `ui-screenshots-api31-[PR-number]` (Android 12)
   - `ui-screenshots-api29-[PR-number]` (Android 10)
5. Open `index.html` in each to view version-specific interactive reports

### Option 3: Direct Download
Artifacts are available for 30 days and can be downloaded via:
```bash
# Download Android 12 screenshots
gh run download <run-id> -n ui-screenshots-api31-<PR-number>

# Download Android 10 screenshots
gh run download <run-id> -n ui-screenshots-api29-<PR-number>
```

## Screenshot Coverage

Current automated screenshots include:
- **Home Screen**: Shows all gallery buttons including new Audio Gallery
- **Audio Gallery**: Empty state and navigation
- **Record Audio**: Recording interface
- **Reference Galleries**: Face/Body galleries for comparison
- **Navigation Flow**: Complete user journey

Each screen is captured on **both Android versions** for comparison.

## Customizing Screenshot Capture

To add more screenshots or modify the capture flow, edit the workflow file:
`.github/workflows/ui-screenshots.yml`

Key section to modify:
```yaml
script: |
  # Add your custom screenshot logic here
  adb shell input tap X Y  # Tap at coordinates
  sleep 2                   # Wait for UI to settle
  adb exec-out screencap -p > screenshots/my_screen.png
```

### Adding More Android Versions

To test on additional Android versions, add to the matrix in the workflow:

```yaml
matrix:
  include:
    - api-level: 29
      target: default
      arch: x86_64
      profile: pixel_3a
      android-version: "Android 10"
    - api-level: 31
      target: google_apis
      arch: x86_64
      profile: pixel_5
      android-version: "Android 12"
    - api-level: 33  # Add Android 13
      target: google_apis
      arch: x86_64
      profile: pixel_6
      android-version: "Android 13"
```

### Finding Tap Coordinates
1. Enable "Pointer location" in Developer Options on emulator
2. Tap where you want
3. Note the X/Y coordinates displayed
4. Use those in `adb shell input tap X Y`

## Workflow Architecture

The workflow is split into two jobs for efficiency:

### Job 1: Build APK (runs once)
- Builds the debug APK using `./gradlew assembleDebug`
- Uploads the APK as an artifact
- This runs **once** regardless of how many Android versions are tested

### Job 2: Screenshot Capture (runs in parallel)
- Downloads the pre-built APK
- Runs simultaneously for all Android versions in the matrix
- Each version captures screenshots independently
- Faster execution since APK is already built

**Benefits**: The APK is built once instead of separately for each Android version, reducing build time by ~50%.

## Troubleshooting

### No screenshots generated
- Check the workflow logs in Actions tab (both build and screenshot jobs)
- Verify the emulator started successfully for both versions
- Ensure APK built successfully in the build job
- Check that the APK artifact was uploaded and downloaded correctly

### Screenshots show wrong content
- Adjust sleep timings between taps (UI might need more time to load)
- Verify tap coordinates are correct for the emulator resolution
- Check that permissions were granted (audio, storage, etc.)
- Note: Coordinates may differ between Android versions/devices

### Workflow doesn't trigger
- Ensure your changes touch files in the monitored paths
- Check that the workflow file itself is valid YAML
- Verify you're on a pull request (not a direct push to main)

### Only one version runs
- Check the Actions tab to see if both jobs started
- Review logs for any errors in the matrix job that didn't complete
- Verify KVM is enabled (should be automatic on GitHub runners)

## Manual Testing

To test screenshot capture locally with an emulator:
```bash
# Build APK
./gradlew assembleDebug

# Start emulator with specific API level
emulator -avd Pixel_5_API_31 -no-snapshot-load

# Install on running emulator
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Capture a screenshot
adb exec-out screencap -p > screenshot.png

# Or use the emulator's built-in feature
adb shell screencap -p /sdcard/screenshot.png
adb pull /sdcard/screenshot.png
```

## Benefits

✅ **Multi-Version**: Tests on both Android 12+ and pre-12 automatically
✅ **Automatic**: No manual work needed for screenshots
✅ **Efficient**: APK built once and reused for all Android versions
✅ **Parallel**: Screenshot jobs run simultaneously for faster results
✅ **Consistent**: Same emulator setup every time
✅ **Historical**: 30-day artifact retention per version
✅ **Reviewable**: Screenshots available during code review
✅ **Documented**: HTML reports make it easy to browse
✅ **Persistent**: Workflow configuration saved in repository
✅ **Comprehensive**: Catches version-specific UI issues early

## Version-Specific Considerations

### Android 12+ (API 31)
- Material You dynamic colors
- Splash screen API
- Updated permission dialogs
- Notification changes
- Better support for large screens

### Android 10 (API 29)
- Pre-Material You design
- Classic splash screen
- Legacy permission model
- Ensures backward compatibility
- Represents large user base still on older versions

## Future Enhancements

Possible improvements:
- [ ] Add Android 13/14 testing
- [ ] Capture screenshots in multiple languages
- [ ] Test different screen sizes (tablet, phone, foldable)
- [ ] Add dark/light theme comparison per version
- [ ] Automated visual regression testing across versions
- [ ] Upload screenshots to dedicated hosting
- [ ] Compare screenshots between Android versions automatically

---

**Note**: The workflow uses `reactivecircus/android-emulator-runner` which provides hardware acceleration (KVM) and is optimized for CI environments. Each Android version runs in parallel for faster results.
