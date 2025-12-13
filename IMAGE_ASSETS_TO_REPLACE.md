# Image Assets to Replace - Complete File List

This document provides a comprehensive list of all image asset files that need to be replaced when rebranding the OpenTransition Android application with your own logo, icon, splash page, and other visual assets.

For detailed instructions on how to replace these assets, please refer to [LOGO_REPLACEMENT_GUIDE.md](LOGO_REPLACEMENT_GUIDE.md).

## Quick Reference

**Total Files to Replace:** 9 image files

## 1. App Launcher Icons (PNG Files)

These are the primary launcher icon files that appear on the home screen and app drawer:

### Main Icon Variant
- **File:** `app/src/main/ic_launcher-playstore.png`
  - **Size:** 512x512 px
  - **Format:** PNG (32-bit)
  - **Purpose:** Play Store listing icon
  - **Notes:** This is the icon users see in the Google Play Store

- **File:** `app/src/main/ic_launcher-web.png`
  - **Size:** 512x512 px
  - **Format:** PNG
  - **Purpose:** Web/promotional use
  - **Notes:** Used for web-based promotional materials

### Train Variant Icon (Alternative App Icon)
- **File:** `app/src/main/ic_launcher_train-web.png`
  - **Size:** 512x512 px
  - **Format:** PNG
  - **Purpose:** Train-themed alternative launcher icon for web/promotional use
  - **Notes:** This is an alternative icon theme that can be enabled by users

## 2. Adaptive Icon Vector Definitions (XML Files)

These XML files define the visual appearance of the launcher icons using vector graphics. Replace the content within these files to change the icon design:

### Main Icon Vector Drawables
- **File:** `app/src/main/res/drawable/ic_launcher_background.xml`
  - **Type:** Vector drawable (XML)
  - **Purpose:** Background layer of the adaptive launcher icon
  - **Current Design:** Pink gradient background (#f48fb1) with train illustration
  - **What to Change:** Replace the entire vector path data with your custom background design or solid color

- **File:** `app/src/main/res/drawable/ic_launcher_foreground.xml`
  - **Type:** Vector drawable (XML)
  - **Purpose:** Foreground layer of the adaptive launcher icon
  - **Current Design:** Blue gradient background with "tt" (TransTracks) logo
  - **What to Change:** Replace the entire vector path data with your custom logo/icon design

### Train Variant Vector Drawables
- **File:** `app/src/main/res/drawable/ic_launcher_train_background.xml`
  - **Type:** Vector drawable (XML)
  - **Purpose:** Background layer of the train-themed adaptive launcher icon
  - **Current Design:** Grey gradient background (#b0bec5) with train illustration
  - **What to Change:** Replace the entire vector path data with your alternative background design

- **File:** `app/src/main/res/drawable/ic_launcher_train_foreground.xml`
  - **Type:** Vector drawable (XML)
  - **Purpose:** Foreground layer of the train-themed adaptive launcher icon
  - **Current Design:** Blue gradient background with "tt" logo (same as main variant)
  - **What to Change:** Replace the entire vector path data with your alternative foreground design

## 3. Adaptive Icon Configuration Files (XML)

These files define how the adaptive icons are composed. You typically don't need to modify these unless you're changing the structure:

- **File:** `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml`
  - **Purpose:** Main adaptive icon configuration
  - **Notes:** References ic_launcher_background and ic_launcher_foreground

- **File:** `app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml`
  - **Purpose:** Round adaptive icon configuration (for launchers that use circular icons)
  - **Notes:** References ic_launcher_background and ic_launcher_foreground

- **File:** `app/src/main/res/mipmap-anydpi-v26/ic_launcher_train.xml`
  - **Purpose:** Train variant adaptive icon configuration
  - **Notes:** References ic_launcher_train_background and ic_launcher_train_foreground

- **File:** `app/src/main/res/mipmap-anydpi-v26/ic_launcher_train_round.xml`
  - **Purpose:** Round train variant adaptive icon configuration
  - **Notes:** References ic_launcher_train_background and ic_launcher_train_foreground

## 4. Background Images

### Splash Screen / Lock Screen Background
- **File:** `app/src/main/res/drawable-nodpi/train_track_background.jpg`
  - **Size:** 2340x4160 px (or similar high resolution)
  - **Format:** JPG
  - **Purpose:** Background image for the train-themed lock screen
  - **Current Design:** Photo of railroad/train tracks
  - **Usage:** Used in `res/layout/train_lock.xml` as the lock screen background
  - **What to Replace:** Replace with your custom background image for the alternative lock screen theme
  - **Notes:** This image should be high resolution to support various screen sizes and densities

## File Locations Summary

```
OpenTransition/
├── app/src/main/
│   ├── ic_launcher-playstore.png          [REPLACE: 512x512 PNG - Play Store icon]
│   ├── ic_launcher-web.png                [REPLACE: 512x512 PNG - Web icon]
│   ├── ic_launcher_train-web.png          [REPLACE: 512x512 PNG - Train variant web icon]
│   └── res/
│       ├── drawable/
│       │   ├── ic_launcher_background.xml       [REPLACE: Vector - Main icon background]
│       │   ├── ic_launcher_foreground.xml       [REPLACE: Vector - Main icon foreground]
│       │   ├── ic_launcher_train_background.xml [REPLACE: Vector - Train icon background]
│       │   └── ic_launcher_train_foreground.xml [REPLACE: Vector - Train icon foreground]
│       ├── drawable-nodpi/
│       │   └── train_track_background.jpg       [REPLACE: JPG - Lock screen background]
│       └── mipmap-anydpi-v26/
│           ├── ic_launcher.xml                  [OPTIONAL: Configuration only]
│           ├── ic_launcher_round.xml            [OPTIONAL: Configuration only]
│           ├── ic_launcher_train.xml            [OPTIONAL: Configuration only]
│           └── ic_launcher_train_round.xml      [OPTIONAL: Configuration only]
```

## Important Notes

### Icon Design Requirements
1. **Adaptive Icons:** Android uses a two-layer system (background + foreground)
   - Background: Can extend beyond the safe zone
   - Foreground: Keep important content within the 72x72 dp safe zone (out of 108x108 dp canvas)

2. **Aspect Ratio:** Icons should work when cropped to various shapes (circle, rounded square, squircle)

3. **Testing:** Test on multiple Android versions and launchers to ensure proper display

### What You DON'T Need to Replace

The app does NOT use PNG mipmap files for different densities (mdpi, hdpi, xhdpi, xxhdpi, xxxhdpi) because it uses Android's modern adaptive icon system with vector drawables. This means:

- ✅ You only need to update the vector XML files (`ic_launcher_background.xml` and `ic_launcher_foreground.xml`)
- ✅ You only need to provide the high-resolution PNG files listed above for Play Store and web use
- ❌ You do NOT need to generate multiple PNG sizes for different screen densities

### Recommended Workflow

1. **Design your icons** using design software (Adobe Illustrator, Figma, etc.)
2. **Export as SVG** and convert to Android vector drawable format
3. **Use Android Studio's Image Asset Studio** to generate all required files automatically:
   - Right-click `app/src/main/res` → New → Image Asset
   - Choose "Launcher Icons (Adaptive and Legacy)"
   - Import your foreground and background layers
   - Let Android Studio generate all files

4. **Manually replace:**
   - The three PNG files in `app/src/main/`
   - The background image `train_track_background.jpg` if you're keeping the train lock theme

### Additional Branding Elements

Beyond the files listed above, you may also want to customize:

- **App Name:** `app/src/main/res/values/strings.xml` - Change the `app_name` string
- **Theme Colors:** `app/src/main/res/values/colors.xml` - Modify brand colors
- **Gradient Backgrounds:** `app/src/main/res/drawable/gradient_background.xml` - Uses theme colors dynamically

### Documentation Screenshots

The `docs/` folder contains screenshots used in the project documentation (e.g., `docs/Screenshot_20251206-141719.png`). These are not part of the app's compiled assets but may appear in:
- GitHub Pages documentation
- README files
- Guides and tutorials

If you're rebranding the entire project, you may want to:
1. Take new screenshots of your customized app
2. Replace the documentation screenshots with your own
3. Update any references in markdown files

**Note:** Documentation screenshots are optional and don't affect the compiled app.

## Verification Checklist

After replacing all assets, verify:

- [ ] App icon displays correctly in launcher
- [ ] App icon displays correctly in settings
- [ ] App icon displays correctly in recent apps
- [ ] Play Store icon looks good (512x512)
- [ ] Icon works with different launcher shapes (circle, square, rounded square)
- [ ] Train variant icon (if used) displays correctly
- [ ] Lock screen background image loads and looks good
- [ ] No missing image errors in logcat
- [ ] Build completes successfully
- [ ] App installs and runs without crashes

## Support

For detailed step-by-step instructions, troubleshooting, and best practices, refer to:
- [LOGO_REPLACEMENT_GUIDE.md](LOGO_REPLACEMENT_GUIDE.md) - Comprehensive replacement guide
- [Android Adaptive Icons Documentation](https://developer.android.com/guide/practices/ui_guidelines/icon_design_adaptive)
- [Material Design Icons Guidelines](https://material.io/design/iconography/)
