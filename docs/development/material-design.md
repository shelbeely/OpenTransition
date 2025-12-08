# Material Design 3 Support

OpenTransition fully supports **Material Design 3** (Material You) and **Material Expressive**, providing a modern, adaptive, and accessible user interface that follows Google's latest design guidelines.

## Overview

Material Design 3 (also known as Material You) is Google's latest design system that emphasizes personalization, accessibility, and dynamic theming. OpenTransition implements Material 3 to provide:

- **Dynamic Color**: Automatic color theming based on user wallpaper (Android 12+)
- **Updated Components**: Modern Material 3 components throughout the app
- **Improved Accessibility**: Enhanced touch targets and contrast ratios
- **Consistent Design Language**: Following Material 3 design tokens and patterns

## Material You Dynamic Colors

Starting with Android 12 (API 31+), OpenTransition supports Material You dynamic colors that automatically adapt to the user's system theme and wallpaper.

### How It Works

The app uses the `Theme.Material3.DynamicColors.Light.NoActionBar` theme on Android 12+ devices, which:

1. Extracts colors from the user's wallpaper
2. Generates a harmonious color palette
3. Applies the palette throughout the app
4. Updates automatically when the wallpaper changes

### Implementation

Dynamic colors are automatically enabled on supported devices through our theme configuration:

```xml
<!-- values-v31/themes.xml -->
<style name="PinkAppTheme" parent="Theme.Material3.DynamicColors.Light.NoActionBar">
    <!-- Base Material 3 theme with dynamic colors -->
</style>
```

For devices below Android 12, the app falls back to our custom color schemes defined in `values/styles.xml`.

## Material 3 Components

### Toolbar / AppBar

The home screen now uses a Material 3 `MaterialToolbar` that provides:

- **Elevated design**: Follows Material 3 elevation patterns
- **Icon actions**: Camera and settings icons integrated into the toolbar
- **Responsive layout**: Adapts to different screen sizes
- **Touch targets**: Meets Material accessibility guidelines (48dp minimum)

**Implementation:**
```xml
<com.google.android.material.appbar.MaterialToolbar
    android:id="@+id/home_toolbar"
    android:layout_width="match_parent"
    android:layout_height="?attr/actionBarSize"
    app:menu="@menu/home_toolbar"
    app:navigationIcon="@drawable/ic_photo_camera_white_24dp" />
```

### Material Buttons

All buttons have been updated to use Material 3 button components:

- **MaterialButton**: Primary and secondary actions
- **Text Buttons**: For gallery navigation (Widget.Material3.Button.TextButton)
- **Icon Buttons**: For camera and settings actions
- **Button Groups**: Support for Material button toggle groups (MaterialButtonToggleGroup)

**Example:**
```xml
<com.google.android.material.button.MaterialButton
    android:id="@+id/home_face_gallery"
    style="@style/Widget.Material3.Button.TextButton"
    android:text="@string/face_gallery" />
```

### Button Groups (MaterialButtonToggleGroup)

Material 3 button groups are used for mutually exclusive selections, such as:

- Photo type selection (Face, Body, Custom)
- View mode toggles
- Filter options

**Features:**
- Single or multiple selection support
- Material 3 styling
- Accessible toggle states
- Proper touch targets

**Example Usage:**
```xml
<com.google.android.material.button.MaterialButtonToggleGroup
    android:id="@+id/toggle_group"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    app:singleSelection="true">
    
    <com.google.android.material.button.MaterialButton
        style="@style/Widget.Material3.Button.OutlinedButton"
        android:text="Option 1" />
        
    <com.google.android.material.button.MaterialButton
        style="@style/Widget.Material3.Button.OutlinedButton"
        android:text="Option 2" />
</com.google.android.material.button.MaterialButtonToggleGroup>
```

## Material Expressive Support

Material Expressive is a variant of Material Design 3 that emphasizes:

- **Bold typography**: Larger, more prominent text styles
- **Vibrant colors**: Enhanced color contrast and saturation
- **Expressive motion**: More dramatic animations and transitions
- **Unique shapes**: Custom corner radii and container shapes

### Enabling Material Expressive

Material Expressive elements can be customized through theme attributes:

```xml
<style name="ExpressiveTheme" parent="BaseAppTheme">
    <item name="shapeAppearanceMediumComponent">@style/ShapeAppearance.Expressive.MediumComponent</item>
    <item name="shapeAppearanceLargeComponent">@style/ShapeAppearance.Expressive.LargeComponent</item>
    <item name="colorPrimary">@color/expressivePrimary</item>
    <item name="colorSecondary">@color/expressiveSecondary</item>
</style>
```

## Theme Configuration

OpenTransition provides multiple theme options:

### Available Themes

1. **Pink Theme** (Default)
   - Primary: Pink/Rose tones
   - Accent: Light blue
   - Best for: Traditional trans flag colors

2. **Blue Theme**
   - Primary: Light blue tones
   - Accent: Pink
   - Best for: Inverted trans flag colors

3. **Purple Theme**
   - Primary: Purple tones
   - Accent: Light blue
   - Best for: Non-binary and alternative aesthetics

4. **Green Theme**
   - Primary: Green tones
   - Accent: Blue
   - Best for: Nature-inspired, calming palette

### Theme Color Tokens

Each theme defines the following Material 3 color tokens:

- `colorPrimary`: Main brand color
- `colorOnPrimary`: Text/icons on primary color
- `colorPrimaryContainer`: Containers using primary
- `colorOnPrimaryContainer`: Content on primary containers
- `colorSecondary`: Accent color for secondary actions
- `colorOnSecondary`: Text/icons on secondary color
- `colorSecondaryContainer`: Containers using secondary
- `colorOnSecondaryContainer`: Content on secondary containers

## Accessibility Features

Material 3 improves accessibility with:

### Touch Targets
- Minimum 48dp × 48dp for all interactive elements
- Proper spacing between elements
- Adequate padding for comfortable interaction

### Contrast Ratios
- WCAG AA compliance for text contrast
- Enhanced contrast in light and dark modes
- Color-independent information presentation

### Typography
- Material 3 type scale
- Scalable text that respects user font size preferences
- Clear hierarchy with proper heading levels

### Screen Readers
- Proper content descriptions for all images and icons
- Logical focus order
- Meaningful labels for interactive elements

## Migration Guide

### Updating Custom Components

If you're extending the app with custom components, follow these guidelines:

#### 1. Use Material 3 Themes
```kotlin
// Instead of:
AppCompatButton(context)

// Use:
MaterialButton(context).apply {
    // Material 3 styling applied automatically
}
```

#### 2. Apply Material 3 Attributes
```xml
<!-- Instead of: -->
<Button style="@style/Widget.AppCompat.Button" />

<!-- Use: -->
<com.google.android.material.button.MaterialButton
    style="@style/Widget.Material3.Button.TonalButton" />
```

#### 3. Use Material Color System
```kotlin
// Instead of:
context.getColor(R.color.custom_primary)

// Use:
context.getColor(com.google.android.material.R.attr.colorPrimary)
```

## Testing Material 3 Features

### Device Compatibility
- Android 12+ (API 31+): Full Material You dynamic colors
- Android 5.0-11 (API 21-30): Static Material 3 themes

### Testing Dynamic Colors
1. Change your device wallpaper
2. Observe the app's color scheme update automatically
3. Verify all UI elements adapt to the new palette

### Testing Themes
1. Navigate to Settings → Appearance
2. Select different theme options
3. Verify consistent Material 3 styling across all screens

## Resources

### Official Documentation
- [Material Design 3 Guidelines](https://m3.material.io/)
- [Material Components Overview](https://m3.material.io/components)
- [Button Groups](https://m3.material.io/components/button-groups/overview)
- [Toolbars](https://m3.material.io/components/toolbars/overview)
- [Material You Dynamic Color](https://m3.material.io/styles/color/dynamic-color/overview)

### Android Documentation
- [Material Components for Android](https://github.com/material-components/material-components-android)
- [Material Design 3 in Compose](https://developer.android.com/jetpack/compose/designsystems/material3)
- [Dynamic Theming](https://developer.android.com/develop/ui/views/theming/dynamic-colors)

### Design Tools
- [Material Theme Builder](https://material-foundation.github.io/material-theme-builder/)
- [Color Tool](https://material.io/resources/color/)
- [Material Icons](https://fonts.google.com/icons)

## Troubleshooting

### Dynamic Colors Not Working

**Problem**: App doesn't adapt to wallpaper colors

**Solutions**:
1. Verify device is running Android 12+
2. Check that dynamic colors are enabled in system settings
3. Ensure the app theme extends `Theme.Material3.DynamicColors.*`

### Button Styling Issues

**Problem**: Buttons don't match Material 3 design

**Solutions**:
1. Use `MaterialButton` instead of `Button` or `AppCompatButton`
2. Apply Material 3 button styles: `Widget.Material3.Button.*`
3. Avoid overriding background directly; use `backgroundTint` instead

### Toolbar Not Displaying Correctly

**Problem**: Toolbar icons or menu items not visible

**Solutions**:
1. Verify menu resource is properly inflated
2. Check icon colors match background (use `?attr/colorOnSurface`)
3. Ensure `app:showAsAction="always"` is set for visible items

## Future Enhancements

Planned Material 3 improvements:

- [ ] Navigation rail for tablet layouts
- [ ] Bottom app bar alternative for home screen
- [ ] Material 3 cards for milestone items
- [ ] Extended FAB for quick photo capture
- [ ] Navigation drawer with Material 3 styling
- [ ] Dialog components with Material 3 design
- [ ] Snackbar updates with Material 3 styling
- [ ] Chip components for photo tags
- [ ] Material 3 text fields throughout

## Contributing

When adding new UI components:

1. **Always use Material 3 components** from the Material Components library
2. **Follow Material Design 3 guidelines** for layouts and spacing
3. **Test on multiple Android versions** (especially Android 12+ for dynamic colors)
4. **Ensure accessibility** with proper touch targets and contrast
5. **Document any custom Material 3 implementations** in this file

For questions or suggestions about Material Design implementation, please open an issue on GitHub.
