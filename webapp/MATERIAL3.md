# Material 3 Design System - Web App

The OpenTransition web app fully implements Material 3 design system to match the Android app's modern, accessible design language.

## Overview

The web app uses the same Material 3 design tokens, color system, and component patterns as the Android app, ensuring visual consistency across platforms.

## Material 3 Color System

### Light Theme
- **Primary**: `#6750A4` (Purple)
- **Secondary**: `#625B71` (Gray-purple)
- **Tertiary**: `#7D5260` (Mauve)
- **Error**: `#B3261E` (Red)
- **Background**: `#FEF7FF` (Light lavender)
- **Surface**: `#FEF7FF`

### Dark Theme
- **Primary**: `#D0BCFF` (Light purple)
- **Secondary**: `#CCC2DC` (Light gray-purple)
- **Tertiary**: `#EFB8C8` (Light mauve)
- **Error**: `#F2B8B5` (Light red)
- **Background**: `#1D1B20` (Dark gray)
- **Surface**: `#1D1B20`

### Color Roles
All Material 3 color roles are implemented:
- Primary, Secondary, Tertiary (with containers and on-colors)
- Surface variants (lowest, low, container, high, highest)
- Outline and outline-variant
- Error colors (with containers)
- Inverse colors for snackbars

## Material 3 Components

### 1. Top App Bar
- Height: 64px (Material 3 standard)
- Elevation: Level 2
- Background: Surface color
- Icons: On-surface-variant color
- State layers on icon buttons

### 2. Navigation Drawer
- Width: 360px (max 80vw on mobile)
- Modal type with backdrop
- Rounded corners (large: 16px on right side)
- Surface-container-low background
- Active item: Secondary-container background

### 3. Cards
Three types used:
- **Filled Cards**: Surface-container-high background
- **Elevated Cards**: Elevation level 1, surface background
- **Outlined Cards**: 1px outline border

All cards use:
- Medium corner radius (12px)
- Proper elevation levels
- State layers for interactions

### 4. Buttons

**Filled Button** (Primary):
```css
background: primary
color: on-primary
elevation: level 0
hover: level 1
```

**Text Button** (Secondary):
```css
background: transparent
color: primary
hover: secondary-container
```

**FAB** (Floating Action Button):
```css
background: primary-container
color: on-primary-container
elevation: level 3
hover: level 4
corner: large (16px)
```

### 5. Dialogs
- Extra-large corner radius (28px)
- Surface-container-high background
- Elevation level 3
- Backdrop: rgba(0, 0, 0, 0.32)

### 6. Form Inputs
- Outlined style (Material 3 default)
- Extra-small corner radius (4px)
- Outline color border
- Focus: Primary color border (2px)
- Labels: On-surface-variant, uppercase

### 7. Snackbar (Toast)
- Inverse-surface background
- Inverse-on-surface text
- Extra-small corner radius (4px)
- Elevation level 3
- Min width: 344px, Max: 672px

## Elevation System

Material 3 elevation with proper shadow layers:

- **Level 0**: No shadow (flat)
- **Level 1**: 1-3px shadow (cards, chips)
- **Level 2**: 2-6px shadow (app bar, navigation drawer)
- **Level 3**: 4-8px shadow (FAB, dialogs, menus)
- **Level 4**: 6-10px shadow (FAB hover)
- **Level 5**: 8-12px shadow (high-prominence surfaces)

## Shape System

Material 3 corner radius scale:

- **None**: 0px
- **Extra Small**: 4px (inputs, chips)
- **Small**: 8px (filters, smaller cards)
- **Medium**: 12px (cards, large chips)
- **Large**: 16px (FAB, bottom sheets)
- **Extra Large**: 28px (dialogs, large components)

## Typography

Material 3 typography scale applied:

- **Display**: 36px, 400 weight (large headlines)
- **Headline**: 28px, 400 weight (section titles)
- **Title**: 22px, 400-500 weight (app bar, card titles)
- **Body**: 16px, 400 weight (main content)
- **Label**: 14px, 500 weight (buttons, labels)
- **Caption**: 12px, 400 weight (helper text)

Letter spacing follows Material 3 guidelines:
- Display: 0
- Headline: 0
- Title: 0
- Body: 0.5px
- Label: 0.1px
- Caption: 0.4px

## State Layers

Interactive components use state layers:

- **Hover**: 8% opacity overlay
- **Focus**: 12% opacity overlay
- **Pressed**: 12% opacity overlay
- **Dragged**: 16% opacity overlay

Implemented via:
```css
transition: all 0.2s cubic-bezier(0.4, 0.0, 0.2, 1);
```

## Motion

Material 3 standard easing curve:
```css
cubic-bezier(0.4, 0.0, 0.2, 1)
```

Duration:
- Enter: 200ms
- Exit: 150ms
- Simple: 100ms
- Complex: 300ms

## Accessibility

Material 3 ensures:
- **Contrast ratios**: WCAG AA compliant (4.5:1 for text)
- **Touch targets**: Minimum 48x48px
- **Focus indicators**: Clear 2px borders
- **Semantic colors**: Consistent color roles
- **Dark theme**: Reduced eye strain

## Theme Switching

Supports three modes:
1. **Light**: Fixed light theme
2. **Dark**: Fixed dark theme
3. **Auto**: Follows system preference

Uses `prefers-color-scheme` media query for auto mode.

## Browser Support

Material 3 CSS features require:
- CSS Custom Properties (variables)
- CSS Grid and Flexbox
- Backdrop filter support
- Modern box-shadow syntax

**Supported browsers:**
- Chrome/Edge 90+
- Firefox 88+
- Safari 14+
- Opera 76+

## Comparison with Android App

| Feature | Android App | Web App |
|---------|-------------|---------|
| Material 3 Design | ✅ | ✅ |
| Color System | Full M3 palette | Full M3 palette |
| Dynamic Colors | ✅ (Android 12+) | ❌ (browser limitation) |
| Dark Theme | ✅ Auto | ✅ Auto |
| Elevation | ✅ | ✅ |
| Shape System | ✅ | ✅ |
| Typography | ✅ | ✅ |
| State Layers | ✅ | ✅ |
| Motion | ✅ | ✅ |

**Note**: Dynamic color (Material You) cannot be implemented in web apps as browsers don't provide access to wallpaper colors. The web app uses the standard Material 3 purple color scheme to match the Android app's default theme.

## Implementation Details

### CSS Architecture

Material 3 tokens are defined as CSS custom properties:
```css
:root {
    --md-sys-color-primary: #6750A4;
    --md-sys-elevation-level1: ...;
    --md-sys-shape-corner-medium: 12px;
}
```

Components use these tokens:
```css
.card {
    background-color: var(--md-sys-color-surface-container);
    border-radius: var(--md-sys-shape-corner-medium);
    box-shadow: var(--md-sys-elevation-level1);
}
```

### Future Enhancements

Possible future Material 3 features:
- [ ] Material You dynamic colors (if browser APIs become available)
- [ ] Motion presets library
- [ ] Additional Material 3 components (chips, navigation rail)
- [ ] Extended color schemes (blue, green, pink themes)
- [ ] High contrast themes for accessibility

## Resources

- [Material 3 Design System](https://m3.material.io/)
- [Material Design Color System](https://m3.material.io/styles/color/system/overview)
- [Material Design Components](https://m3.material.io/components)
- [Material Design Guidelines](https://m3.material.io/foundations)
