# Maximal Material 3 Implementation Plan

This document outlines the plan to implement maximal Material 3 components following https://m3.material.io/ guidelines.

## Current Implementation Status

### ✅ Already Implemented
- Top App Bar (Small, 64px)
- Navigation Drawer (Modal)
- Cards (Filled, Elevated with gradient overlay)
- Buttons (Filled, Text)
- Dialogs (with extra-large corners)
- Text Fields (Outlined)
- Snackbar/Toast
- Checkbox
- Select/Dropdown
- Color System (Full M3 tokens)
- Elevation System (5 levels)
- Shape System (6 corner sizes)
- Typography Scale
- State Layers

### 🚀 To Be Added for Maximal M3

#### High Priority Components
1. **FAB (Floating Action Button)**
   - Extended FAB for "Add Photo"
   - Small FAB for quick actions
   - FAB with icon + label

2. **Chips**
   - Filter chips for photo types (Face/Body)
   - Input chips for tags
   - Assist chips for suggestions

3. **Segmented Buttons**
   - Photo type selector (Face/Body/All)
   - View mode selector (Grid/List)

4. **Progress Indicators**
   - Linear progress for uploads
   - Circular progress for loading
   - Determinate and indeterminate

5. **Badges**
   - Notification badges on nav items
   - Count badges for photos/milestones

6. **Lists**
   - Three-line list items for milestones
   - List with leading icons
   - List with trailing metadata

7. **Dividers**
   - Full-width dividers
   - Inset dividers
   - Middle dividers

8. **Search Bar**
   - Material 3 search bar
   - Search with suggestions
   - Clear button

#### Medium Priority Components
9. **Icon Buttons**
   - Standard icon buttons
   - Filled icon buttons
   - Tonal icon buttons
   - Outlined icon buttons

10. **Menus**
    - Dropdown menus
    - Context menus
    - Menu items with icons

11. **Tooltips**
    - Plain tooltips
    - Rich tooltips

12. **Bottom Sheet**
    - Modal bottom sheet for actions
    - Standard bottom sheet for filters

13. **Navigation Bar** (Bottom navigation for mobile)
    - Home, Gallery, Milestones, Settings

14. **Navigation Rail** (Side navigation for tablet/desktop)
    - Vertical navigation with icons + labels

15. **Tabs**
    - Primary tabs for main sections
    - Secondary tabs for sub-sections

#### Low Priority (Nice to Have)
16. **Sliders**
    - For photo comparison
    - For timeline scrubbing

17. **Switches**
    - Settings toggles

18. **Radio Buttons**
    - Photo type selection

19. **Date Pickers**
    - Milestone date selection
    - Photo date filtering

20. **Time Pickers**
    - For scheduled reminders

21. **Carousel**
    - Photo carousel viewer

22. **Data Tables**
    - Milestone timeline table

## Implementation Priority

### Phase 1: Essential UX Improvements (Immediate)
- Extended FAB for quick photo adding
- Segmented buttons for filters
- Chips for tags and filters
- Progress indicators for data loading
- Badges for counts
- Improved lists for milestones

### Phase 2: Enhanced Navigation (Next)
- Bottom Navigation Bar (mobile)
- Navigation Rail (tablet/desktop)
- Search bar
- Improved menus

### Phase 3: Advanced Features (Future)
- Bottom sheets for actions
- Tabs for organization
- Date/time pickers
- Sliders for comparisons
- Carousel for photos

## Material 3 Design Tokens to Enhance

### Additional Color Roles
- Surface Tint (for elevated surfaces)
- Scrim (for modal overlays)
- Shadow (for elevation)
- Inverse variants

### Motion Tokens
- Duration tokens (short, medium, long, extra-long)
- Easing curves (emphasized, standard, decelerate, accelerate)
- Animation presets

### State Tokens
- Hover opacity: 8%
- Focus opacity: 12%
- Press opacity: 12%
- Drag opacity: 16%
- Disabled opacity: 38%

## Component Specifications

### FAB Specifications
```css
/* Extended FAB */
height: 56px
min-width: 80px
corner-radius: 16px
elevation: level-3 (lowered), level-4 (resting)
padding: 0 16px

/* Standard FAB */
width: 56px
height: 56px
corner-radius: 16px
elevation: level-3 (lowered), level-4 (resting)

/* Small FAB */
width: 40px
height: 40px
corner-radius: 12px
elevation: level-3 (lowered), level-4 (resting)
```

### Chip Specifications
```css
/* Filter Chip */
height: 32px
corner-radius: 8px
padding: 0 16px (no icon), 0 16px 0 8px (with icon)
elevation: level-0 (unselected), level-1 (selected)

/* Input Chip */
height: 32px
corner-radius: 8px
padding: 0 4px 0 12px
elevation: level-0

/* Assist Chip */
height: 32px
corner-radius: 8px
padding: 0 16px
elevation: level-0
```

### Segmented Button Specifications
```css
height: 40px
corner-radius: 20px (outer), 0 (inner segments)
outline: 1px outline color
min-width: 48px
```

### Progress Indicator Specifications
```css
/* Linear Progress */
height: 4px
corner-radius: 0
track-color: surface-variant
indicator-color: primary

/* Circular Progress */
diameter: 48px (medium), 24px (small)
stroke-width: 4px
track-color: surface-variant
indicator-color: primary
```

### Badge Specifications
```css
/* Large Badge (with count) */
height: 16px
min-width: 16px
corner-radius: 8px
padding: 0 4px
font-size: 11px

/* Small Badge (dot) */
width: 6px
height: 6px
corner-radius: 3px
```

### List Item Specifications
```css
/* One-line */
height: 56px
padding: 8px 16px

/* Two-line */
height: 72px
padding: 8px 16px

/* Three-line */
height: 88px
padding: 12px 16px

leading-element: 24x24 icon or 40x40 avatar
trailing-element: 24x24 icon or text
```

### Search Bar Specifications
```css
height: 56px
corner-radius: 28px (full-screen), 0 (docked)
elevation: level-3
padding: 0 16px
```

## Accessibility Requirements

All components must meet:
- WCAG AA contrast ratios (4.5:1 for text)
- 48x48dp minimum touch targets
- Keyboard navigation support
- Screen reader support
- Focus indicators (2px outline)
- State announcements

## Motion Guidelines

### Duration
- Simple: 100ms
- Medium: 200-300ms
- Complex: 400-500ms
- Large/full-screen: 500-700ms

### Easing
- Standard: cubic-bezier(0.2, 0.0, 0, 1.0)
- Emphasized: cubic-bezier(0.0, 0.0, 0, 1.0)
- Decelerate: cubic-bezier(0.0, 0.0, 0.2, 1.0)
- Accelerate: cubic-bezier(0.4, 0.0, 1.0, 1.0)

## Implementation Checklist

### Immediate (This Session)
- [ ] Add Extended FAB for "Add Photo"
- [ ] Add Segmented Buttons for photo filters
- [ ] Add Filter Chips for photo types
- [ ] Add Progress Indicators (circular for loading)
- [ ] Add Badges to navigation items
- [ ] Enhance Lists for milestones
- [ ] Add proper Dividers
- [ ] Add Search Bar for gallery
- [ ] Update Icon Buttons with variants
- [ ] Add Bottom Navigation for mobile
- [ ] Implement all state layers properly
- [ ] Add motion/animation tokens
- [ ] Update focus indicators

### Next Phase
- [ ] Navigation Rail for desktop
- [ ] Bottom Sheets for actions
- [ ] Tabs for organization
- [ ] Menus with proper styling
- [ ] Tooltips for help
- [ ] Date/Time Pickers
- [ ] Switches for settings
- [ ] Radio buttons where appropriate

### Future Enhancements
- [ ] Sliders for comparisons
- [ ] Carousel for photos
- [ ] Data tables for timeline
- [ ] Advanced animations
- [ ] Gesture support
- [ ] Haptic feedback (PWA API)

## Resources
- Official M3 Guidelines: https://m3.material.io/
- Components: https://m3.material.io/components
- Foundations: https://m3.material.io/foundations
- Styles: https://m3.material.io/styles
