# OpenTransition Documentation

![OpenTransition](Screenshot_20251206-141719.png)

Welcome to the official documentation for **OpenTransition** - a transition tracking application specifically designed for transgender people, focusing on photo tracking and milestone management.

## What is OpenTransition?

OpenTransition is the active successor to the original **TransTracks** application. TransTracks was retired from the Google Play Store in 2025 and its repository is now archived. OpenTransition continues to develop independently, providing a private and secure way for transgender individuals to document their transition journey through photos, audio recordings, and milestones. If you are a former TransTracks user, see [Import & Export](features/import-export.md) for migration instructions.

!!! info "Built on TransTracks (now retired)"
    OpenTransition originated from the excellent work of the original TransTracks developers. TransTracks was retired from the Play Store in 2025; OpenTransition is the recommended continuation. [Learn more about credits and attribution →](credits.md)

## Key Features

### 📸 Photo Tracking
Track your transition with photos organized by different body types (face, body, custom areas). Compare photos side-by-side to see your progress over time.

### 📸 Face-Detection Camera
CameraX-powered camera with ML Kit face detection automatically guides you to frame your face consistently for better before/after comparisons.

### 🎙️ Audio Tracking
Record voice samples and track vocal changes over time with automatic pitch (F0) and formant (F1/F2) analysis. Visualize your voice journey with waveforms and progression charts.

### 🎯 Milestone Management
Record and celebrate important milestones in your transition journey. Track dates, add descriptions, and associate photos with specific achievements.

### 🖼️ Gallery View
Browse your transition photos in an organized gallery. Filter by date, body type, and milestones to find exactly what you're looking for.

### 🔒 Privacy & Security
- App lock with PIN, pattern, or biometric (fingerprint/face)
- Disguised app icon (train mode)
- Secure local storage
- Optional SQLCipher database encryption
- Decoy vault (separate vault with different passcode)
- Optional cloud sync with Firebase

### ⌚ Wear OS Companion
Trigger photo capture and view recent milestones directly from your Android smartwatch via the Wearable Data Layer API.

### 🎨 Customization
- Multiple theme options (Pink, Blue, Purple, Green)
- Flexible photo organization
- Custom milestone types

### 💾 Data Management
- Import/Export your data (`.ttbackup` format)
- Firebase cloud backup
- Room + SQLCipher local database (optional encrypted)

## Technology Stack

- **Language**: Kotlin
- **UI Framework**: Android SDK with Material Design (XML Views + ViewBinding)
- **Primary Database**: Room + SQLCipher (optional encryption via Android Keystore)
- **Legacy Import**: Realm Kotlin (backwards compatibility for importing TransTracks backups only)
- **Architecture**: MVVM with Domain layer, RxJava 3 reactive streams
- **Navigation**: Android Navigation Component (SafeArgs)
- **Reactive Programming**: RxJava 3 + RxRelay + RxBinding
- **Camera**: CameraX + ML Kit face detection
- **Authentication**: Biometric (BiometricPrompt BIOMETRIC_STRONG) + Firebase Auth
- **Cloud Services**: Firebase (Auth, Firestore, Analytics, Crashlytics)
- **Wear OS**: Wearable Data Layer API
- **Build System**: Gradle (monorepo: `:mobile`, `:wear`, `:shared`)

## Project Structure

This is a **monorepo** containing three modules:

| Module | Description |
|--------|-------------|
| `:mobile` | Main Android phone/tablet app (`com.shelbeely.opentransition`) |
| `:wear` | Wear OS companion app (`com.shelbeely.opentransition.wear`) |
| `:shared` | Common data models and Wearable Data Layer constants |

## Quick Links

- [Getting Started](getting-started/overview.md) - Set up your development environment
- [Architecture Overview](architecture/overview.md) - Understand the app structure
- [Contributing Guidelines](contributing/guidelines.md) - Learn how to contribute
- [Features Documentation](features/photo-tracking.md) - Explore all features in detail
- [Import from TransTracks](features/import-export.md) - Migrate your TransTracks data

## Community & Support

- 🐛 [Report Issues](https://github.com/shelbeely/OpenTransition/issues)
- 💡 [Feature Requests](https://github.com/shelbeely/OpenTransition/issues/new)
- 🤝 [Contributing](contributing/guidelines.md)
- 🏆 [Credits & Attribution](credits.md)

## License

OpenTransition is free and open-source software licensed under the GNU General Public License v3.0.

```
Original Copyright (C) 2018 - 2021 TransTracks
Fork modifications and rebranding by the OpenTransition contributors

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.
```

See the [LICENSE](https://github.com/shelbeely/OpenTransition/blob/main/LICENSE) file for more details.

## Project Information

- **Package Name**: `com.shelbeely.opentransition`
- **App Name**: OpenTransition
- **Minimum Android SDK**: 21 (Android 5.0)
- **Target Android SDK**: 36
- **Current Version**: 1.3.x
