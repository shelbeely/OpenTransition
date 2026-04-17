# Logo and Icon Replacement Guide

This guide explains how to replace the logo, icon, and associated branding items in the OpenTransition Android application.

## Overview

The OpenTransition app uses Android's adaptive icon system along with various icon assets for different screen densities and purposes. To replace the branding, you'll need to update multiple files across different directories.

## Icon Files to Replace

### 1. Main App Launcher Icons

The primary app launcher icons are located in:

```
mobile/src/main/ic_launcher-playstore.png      (512x512 PNG for Play Store)
mobile/src/main/ic_launcher-web.png            (512x512 PNG for web)
```

### 2. Adaptive Icon Components

Android adaptive icons consist of separate foreground and background layers:

**Foreground Images:**
- `mobile/src/main/res/mipmap-mdpi/ic_launcher_foreground.png` (108x108 dp)
- `mobile/src/main/res/mipmap-hdpi/ic_launcher_foreground.png` (162x162 dp)
- `mobile/src/main/res/mipmap-xhdpi/ic_launcher_foreground.png` (216x216 dp)
- `mobile/src/main/res/mipmap-xxhdpi/ic_launcher_foreground.png` (324x324 dp)
- `mobile/src/main/res/mipmap-xxxhdpi/ic_launcher_foreground.png` (432x432 dp)

**Monochrome Images (for themed icons on Android 13+):**
- `mobile/src/main/res/mipmap-mdpi/ic_launcher_monochrome.png` (108x108 dp)
- `mobile/src/main/res/mipmap-hdpi/ic_launcher_monochrome.png` (162x162 dp)
- `mobile/src/main/res/mipmap-xhdpi/ic_launcher_monochrome.png` (216x216 dp)
- `mobile/src/main/res/mipmap-xxhdpi/ic_launcher_monochrome.png` (324x324 dp)
- `mobile/src/main/res/mipmap-xxxhdpi/ic_launcher_monochrome.png` (432x432 dp)

**Vector/XML Definitions:**
- `mobile/src/main/res/drawable/ic_launcher_foreground.xml`
- `mobile/src/main/res/drawable/ic_launcher_background.xml`

### 3. Adaptive Icon Configuration Files

These XML files define how the adaptive icons are composed:

```
mobile/src/main/res/mipmap-anydpi-v26/ic_launcher.xml
mobile/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml
```

### 4. Play Store Graphics

```
mobile/src/main/res/playstore/ic_playstore_512.png (512x512 PNG)
```

### 5. Train Variant Icons (if applicable)

The app may have alternate "train" variant icons:

```
mobile/src/main/ic_launcher_train-web.png
mobile/src/main/res/mipmap-anydpi-v26/ic_launcher_train.xml
mobile/src/main/res/mipmap-anydpi-v26/ic_launcher_train_round.xml
mobile/src/main/res/drawable/ic_launcher_train_background.xml
mobile/src/main/res/drawable/ic_launcher_train_foreground.xml
```

## Step-by-Step Replacement Process

### Step 1: Prepare Your Icon Assets

Before replacing icons, prepare the following:

1. **Main Icon Design** - Create your logo/icon design
2. **Background Layer** - Solid color or simple pattern (should work on any background)
3. **Foreground Layer** - Your actual icon/logo (should be centered in a 108x108 dp safe zone)
4. **Monochrome Version** - Single-color version for themed icons (Android 13+)

### Step 2: Generate Icon Assets

You can use Android Studio's Image Asset Studio to generate all required sizes:

1. Open Android Studio
2. Right-click on `mobile/src/main/res` folder
3. Select **New > Image Asset**
4. Choose "Launcher Icons (Adaptive and Legacy)"
5. Configure your foreground and background layers
6. Click "Next" and "Finish"

Alternatively, use online tools or manual export from design software:
- [Android Asset Studio](http://romannurik.github.io/AndroidAssetStudio/)
- [App Icon Generator](https://appicon.co/)

### Step 3: Replace Icon Files

Replace the files listed above with your new icon assets, ensuring:
- Correct dimensions for each density (mdpi, hdpi, xhdpi, xxhdpi, xxxhdpi)
- Proper naming conventions are maintained
- File formats match (PNG for bitmaps, XML for vectors)

### Step 4: Update XML Definitions (if needed)

If you're using vector drawables or changing colors, update:

**ic_launcher_background.xml:**
```xml
<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@color/your_background_color" />
    <!-- or -->
    <background android:drawable="@drawable/your_background_drawable" />
</adaptive-icon>
```

**ic_launcher_foreground.xml:**
```xml
<!-- Update with your vector paths or reference to drawable -->
```

### Step 5: Update Adaptive Icon Configurations

Verify that `ic_launcher.xml` and `ic_launcher_round.xml` reference your new assets:

```xml
<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@drawable/ic_launcher_background" />
    <foreground android:drawable="@drawable/ic_launcher_foreground" />
</adaptive-icon>
```

### Step 6: Clean and Rebuild

1. In Android Studio: **Build > Clean Project**
2. Then: **Build > Rebuild Project**
3. Uninstall the app from your test device
4. Install and run the app to see the new icon

### Step 7: Test on Multiple Devices

Test your new icon on:
- Different Android versions (especially Android 8.0+ for adaptive icons, 13+ for themed icons)
- Different launcher apps (stock, Nova Launcher, etc.)
- Different screen densities
- Light and dark themes

## Additional Branding Assets

Beyond launcher icons, you may also want to update:

### Splash Screen / Welcome Screen
- Check `mobile/src/main/res/layout/welcome.xml`
- Update any logo ImageViews or branding elements

### In-App Logos
- Search for logo references in layout files: `grep -r "logo" mobile/src/main/res/layout/`
- Update drawable resources as needed

### App Name
- Update in `mobile/src/main/res/values/strings.xml`:
  ```xml
  <string name="app_name">Your App Name</string>
  ```

### Theme Colors
- Update brand colors in `mobile/src/main/res/values/colors.xml`:
  ```xml
  <color name="colorPrimary">#YourColor</color>
  <color name="colorPrimaryDark">#YourColor</color>
  <color name="colorAccent">#YourColor</color>
  ```

## Best Practices

1. **Maintain Consistent Branding**: Ensure your icon is recognizable at all sizes
2. **Follow Material Design Guidelines**: Check [Material Design Icon Guidelines](https://material.io/design/iconography/)
3. **Safe Zone**: Keep important content within the safe zone to prevent clipping
4. **Contrast**: Ensure good contrast between foreground and background
5. **Simplicity**: Avoid overly complex designs that don't scale well
6. **Test Thoroughly**: Icons appear differently across devices and launchers

## Design Specifications

- **Launcher Icon Foreground**: 108x108 dp (with 72x72 dp safe zone)
- **Background**: 108x108 dp (can extend beyond safe zone)
- **Play Store Icon**: 512x512 px (PNG, 32-bit, no transparency)
- **Monochrome Icon**: Single color, used for themed icons

## Resources

- [Android Adaptive Icons Documentation](https://developer.android.com/guide/practices/ui_guidelines/icon_design_adaptive)
- [Material Design Icons](https://material.io/resources/icons/)
- [Android Asset Studio](http://romannurik.github.io/AndroidAssetStudio/)

## Troubleshooting

**Icon not updating after rebuild:**
- Clear app data and cache
- Uninstall and reinstall the app
- Clear build cache: Build > Clean Project

**Icon looks wrong on certain launchers:**
- Test with different adaptive icon shapes
- Ensure safe zone guidelines are followed
- Check background and foreground layer alignment

**Icon appears pixelated:**
- Verify correct DPI assets are present
- Ensure PNG assets are high quality
- Consider using vector drawables for scalability
