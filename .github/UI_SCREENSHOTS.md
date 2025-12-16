# UI Screenshots Automation

This repository automatically captures UI screenshots when visual changes are made to the Android app.

## How It Works

The GitHub Actions workflow (`.github/workflows/ui-screenshots.yml`) automatically runs when:
- Pull requests modify layouts (`app/src/main/res/layout/**`)
- Pull requests modify UI resources (`app/src/main/res/values/**`, `app/src/main/res/drawable/**`)
- Pull requests modify UI code (`app/src/main/java/**/ui/**`, `app/src/main/java/**/widget/**`)

## What Gets Captured

The workflow:
1. ✅ Builds the debug APK
2. ✅ Launches an Android emulator (API 29, Pixel 3a)
3. ✅ Installs and runs the app
4. ✅ Navigates through key screens automatically
5. ✅ Captures screenshots at each step
6. ✅ Generates an HTML report with all screenshots
7. ✅ Posts a comment on the PR with screenshot summary
8. ✅ Uploads screenshots as downloadable artifacts

## Viewing Screenshots

### Option 1: PR Comment
When the workflow completes, a bot will comment on your PR with:
- List of captured screenshots
- Link to download the artifact
- Screenshot count and status

### Option 2: Actions Artifacts
1. Go to the **Actions** tab in GitHub
2. Click on the "UI Screenshots" workflow run for your PR
3. Scroll to **Artifacts** section at the bottom
4. Download `ui-screenshots-[PR-number]` (contains all PNG files + HTML report)
5. Open `index.html` in a browser to view an interactive report

### Option 3: Direct Download
Artifacts are available for 30 days and can be downloaded via:
```bash
gh run download <run-id> -n ui-screenshots-<PR-number>
```

## Screenshot Coverage

Current automated screenshots include:
- **Home Screen**: Shows all gallery buttons including new Audio Gallery
- **Audio Gallery**: Empty state and navigation
- **Record Audio**: Recording interface
- **Reference Galleries**: Face/Body galleries for comparison
- **Navigation Flow**: Complete user journey

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

### Finding Tap Coordinates
1. Enable "Pointer location" in Developer Options on emulator
2. Tap where you want
3. Note the X/Y coordinates displayed
4. Use those in `adb shell input tap X Y`

## Troubleshooting

### No screenshots generated
- Check the workflow logs in Actions tab
- Verify the emulator started successfully
- Ensure APK built without errors

### Screenshots show wrong content
- Adjust sleep timings between taps (UI might need more time to load)
- Verify tap coordinates are correct for the emulator resolution
- Check that permissions were granted (audio, storage, etc.)

### Workflow doesn't trigger
- Ensure your changes touch files in the monitored paths
- Check that the workflow file itself is valid YAML
- Verify you're on a pull request (not a direct push to main)

## Manual Testing

To test screenshot capture locally with an emulator:
```bash
# Build APK
./gradlew assembleDebug

# Install on running emulator
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Capture a screenshot
adb exec-out screencap -p > screenshot.png

# Or use the emulator's built-in feature
adb shell screencap -p /sdcard/screenshot.png
adb pull /sdcard/screenshot.png
```

## Benefits

✅ **Automatic**: No manual work needed for screenshots
✅ **Consistent**: Same emulator setup every time
✅ **Historical**: 30-day artifact retention
✅ **Reviewable**: Screenshots available during code review
✅ **Documented**: HTML reports make it easy to browse
✅ **Persistent**: Workflow configuration saved in repository

## Future Enhancements

Possible improvements:
- [ ] Capture screenshots in multiple languages
- [ ] Test different screen sizes (tablet, phone)
- [ ] Add dark/light theme comparison
- [ ] Automated visual regression testing
- [ ] Upload screenshots to dedicated hosting
- [ ] Compare screenshots between commits

---

**Note**: The workflow uses `reactivecircus/android-emulator-runner` which provides hardware acceleration and is optimized for CI environments.
