# Material Design Components Usage

## Overview

**Yes, OpenTransition is actively using Material Design Components for Android (MDC-Android)!**

The project is built on Material Design 3 (M3) using the official [material-components-android](https://github.com/material-components/material-components-android) library.

## Current Implementation

### Dependencies

The mobile app uses **Material Components version 1.13.0**:

```gradle
implementation 'com.google.android.material:material:1.13.0'
```

Additionally, the project includes support for Material Design 3 Expressive animations:

```gradle
implementation 'androidx.dynamicanimation:dynamicanimation:1.0.0'
```

### Theme

The app uses **Material 3** theming with four color variants:

- **BaseAppTheme**: `Theme.Material3.Light.NoActionBar`
  - PinkAppTheme (default)
  - BlueAppTheme
  - PurpleAppTheme
  - GreenAppTheme

All themes extend the M3 base theme and implement Material You color system with proper color roles:
- `colorPrimary`, `colorOnPrimary`
- `colorPrimaryContainer`, `colorOnPrimaryContainer`
- `colorSecondary`, `colorOnSecondary`
- `colorSecondaryContainer`, `colorOnSecondaryContainer`

**Location**: `/mobile/src/main/res/values/styles.xml`

### Material Components in Use

The following Material Design components are actively used throughout the app:

#### 1. **MaterialButton**
Used extensively for interactive elements with M3 styles:
- `Widget.Material3.Button` (filled buttons)
- `Widget.Material3.Button.TonalButton` (tonal buttons)
- `Widget.Material3.Button.TextButton` (text buttons)
- `Widget.Material3.Button.OutlinedButton` (outlined buttons)

**Examples:**
- Settings screen account actions
- Camera capture buttons
- Dialog action buttons

**Locations**: 
- `/mobile/src/main/res/layout/settings.xml`
- `/mobile/src/main/res/layout/fragment_camera.xml`

#### 2. **TextInputLayout**
Material text fields with M3 styling:
- `Widget.Material3.TextInputLayout.FilledBox`

Used for password entry, milestone editing, and form inputs.

**Locations**:
- `/mobile/src/main/res/layout/normal_lock.xml`
- `/mobile/src/main/res/layout/train_lock.xml`
- `/mobile/src/main/res/layout/set_password_dialog.xml`
- `/mobile/src/main/res/layout/enter_password_dialog.xml`
- `/mobile/src/main/res/layout/add_milestone.xml`

#### 3. **MaterialButtonToggleGroup**
Used for settings conflict resolution with toggle button groups.

**Location**: `/mobile/src/main/res/layout/settings_conflict_item.xml`

#### 4. **MaterialSwitch**
Material 3 switches for settings toggles:
- Analytics toggle
- Crash reports toggle

**Location**: `/mobile/src/main/res/layout/settings.xml`

#### 5. **LinearProgressIndicator**
M3 progress indicators for loading states:
- `Widget.Material3.LinearProgressIndicator`

**Location**: `/mobile/src/main/res/layout/settings.xml` (backup progress)

#### 6. **Snackbar**
Material Snackbar component for user feedback messages.

**Usage in code**:
- `com/shelbeely/opentransition/ui/settings/SettingsFragment.kt`
- `com/shelbeely/opentransition/ui/addeditmilestone/AddEditMilestoneFragment.kt`
- `com/shelbeely/opentransition/ui/lock/LockFragment.kt`

### Typography

The app uses Material 3 text appearances:

- `TextAppearance.Material3.HeadlineLarge` - Large headlines
- `TextAppearance.Material3.HeadlineMedium` - Medium headlines
- `TextAppearance.Material3.TitleMedium` - Titles
- `TextAppearance.Material3.BodyMedium` - Body text
- `TextAppearance.Material3.BodySmall` - Small body text

**Location**: Throughout layout files, especially in `/mobile/src/main/res/layout/settings.xml`

### Custom Styling

The project defines custom typography and shape styles that extend Material 3:

**Typography**: `/mobile/src/main/res/values/styles_typography.xml`
**Shapes**: `/mobile/src/main/res/values/styles_shapes.xml`

## Material Design 3 Features

### Current M3 Features
- ✅ Material 3 base theme
- ✅ Material You color system
- ✅ Material 3 components (buttons, text fields, switches, etc.)
- ✅ M3 typography scale
- ✅ Dynamic animation support (spring physics foundation)
- ✅ Proper elevation and surface tints
- ✅ Semantic color roles

### Potential Enhancements
The project has laid the groundwork for Material Design 3 Expressive:
- 🔄 Spring-based animations (library included, ready for implementation)
- 🔄 Emphasized typography variants
- 🔄 Morphing shapes
- 🔄 Vibrant color themes
- 🔄 Enhanced motion and transitions

## Architecture Integration

### Wear OS Support
The Wear OS companion app uses Wear-specific Material Design:

```gradle
implementation 'androidx.wear.compose:compose-material:1.3.0'
implementation 'androidx.wear.compose:compose-foundation:1.3.0'
```

**Location**: `/wear/build.gradle`

### Shared Module
The shared module focuses on data models and doesn't directly use Material components, maintaining clean separation of concerns.

## Resources

### Official Documentation
- [Material Design 3](https://m3.material.io/)
- [MDC-Android Documentation](https://m3.material.io/develop/android/mdc-android)
- [Material Components GitHub](https://github.com/material-components/material-components-android)

### Migration Status
The app has successfully migrated to Material Design 3:
- ✅ Updated from Material 2 to Material 3 themes
- ✅ Using M3 component styles
- ✅ Implementing M3 color system
- ✅ Using M3 typography scale
- ✅ Modern Material components (version 1.13.0)

## Summary

**OpenTransition fully embraces Material Design Components for Android**, using the latest Material Design 3 specifications. The app demonstrates best practices for M3 implementation with:

1. **Proper theming** using `Theme.Material3` base
2. **Semantic color roles** for dynamic theming
3. **Material 3 components** throughout the UI
4. **Consistent typography** using M3 text appearances
5. **Modern component styles** for buttons, inputs, and controls
6. **Foundation for Expressive design** with dynamic animation support

The implementation is production-ready and follows Material Design guidelines for creating beautiful, accessible Android applications.
