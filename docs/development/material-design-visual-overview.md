# Material Design Components - Visual Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                    OpenTransition App                           │
│                Material Design 3 Implementation                 │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│  DEPENDENCIES (mobile/build.gradle)                             │
├─────────────────────────────────────────────────────────────────┤
│  ✓ com.google.android.material:material:1.13.0                 │
│  ✓ androidx.dynamicanimation:dynamicanimation:1.0.0            │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│  THEMES (values/styles.xml)                                     │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  BaseAppTheme (Theme.Material3.Light.NoActionBar)              │
│       │                                                         │
│       ├── PinkAppTheme   (default)                             │
│       ├── BlueAppTheme                                          │
│       ├── PurpleAppTheme                                        │
│       └── GreenAppTheme                                         │
│                                                                 │
│  Color Roles:                                                   │
│    • colorPrimary / colorOnPrimary                             │
│    • colorPrimaryContainer / colorOnPrimaryContainer           │
│    • colorSecondary / colorOnSecondary                         │
│    • colorSecondaryContainer / colorOnSecondaryContainer       │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│  MATERIAL COMPONENTS IN USE                                     │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  Buttons:                                                       │
│    ✓ MaterialButton                                            │
│      • Widget.Material3.Button (Filled)                        │
│      • Widget.Material3.Button.TonalButton                     │
│      • Widget.Material3.Button.TextButton                      │
│      • Widget.Material3.Button.OutlinedButton                  │
│    ✓ MaterialButtonToggleGroup                                 │
│                                                                 │
│  Inputs:                                                        │
│    ✓ TextInputLayout (FilledBox style)                         │
│    ✓ MaterialSwitch                                            │
│                                                                 │
│  Feedback:                                                      │
│    ✓ Snackbar                                                   │
│    ✓ LinearProgressIndicator                                   │
│                                                                 │
│  Typography:                                                    │
│    ✓ TextAppearance.Material3.HeadlineLarge                    │
│    ✓ TextAppearance.Material3.HeadlineMedium                   │
│    ✓ TextAppearance.Material3.TitleMedium                      │
│    ✓ TextAppearance.Material3.BodyMedium                       │
│    ✓ TextAppearance.Material3.BodySmall                        │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│  KEY SCREENS USING MATERIAL COMPONENTS                          │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  Settings Screen (settings.xml):                               │
│    • MaterialButton (sign in, export, import, etc.)            │
│    • MaterialSwitch (analytics, crash reports)                 │
│    • LinearProgressIndicator (backup progress)                 │
│    • M3 Typography throughout                                  │
│                                                                 │
│  Camera Screen (fragment_camera.xml):                          │
│    • MaterialButton (capture, gallery, flip)                   │
│                                                                 │
│  Lock Screens (normal_lock.xml, train_lock.xml):               │
│    • TextInputLayout (password entry)                          │
│                                                                 │
│  Dialogs:                                                       │
│    • TextInputLayout (password, email, name dialogs)           │
│    • MaterialButton (dialog actions)                           │
│                                                                 │
│  Code (Kotlin):                                                 │
│    • Snackbar (SettingsFragment, AddEditMilestoneFragment)     │
│    • Material component imports throughout                     │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│  ADDITIONAL SUPPORT                                             │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  Wear OS Module:                                                │
│    • androidx.wear.compose:compose-material:1.3.0              │
│    • androidx.wear.compose:compose-foundation:1.3.0            │
│                                                                 │
│  Animations:                                                    │
│    • Dynamic Animation library for spring physics              │
│    • Ready for Material Design 3 Expressive                    │
└─────────────────────────────────────────────────────────────────┘

CONCLUSION:
===========
✅ Fully using Material Design Components for Android
✅ Material Design 3 compliant
✅ Modern component library (v1.13.0)
✅ Material You color system implemented
✅ Production-ready Material implementation
