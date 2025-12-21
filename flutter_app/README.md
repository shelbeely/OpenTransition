# OpenTransition - Flutter

This is the Flutter cross-platform version of OpenTransition, a transition tracking application made specifically for transgender people.

## 🚀 Migration Status

This Flutter application is a complete rewrite of the original Android (Kotlin/Java) application. The migration is currently in progress.

### Current Progress: ~50% Complete

#### ✅ Phase 1-2: Infrastructure (100%)
- [x] Flutter project structure
- [x] Dependencies (40+ packages)
- [x] Material Design 3 theme
- [x] Database (SQLite + encryption)
- [x] Core services (Photo, Milestone, Backup, Analytics)
- [x] State management (Provider)
- [x] Platform detection

#### ✅ Phase 4: Core Features (100%)
- [x] Photo capture (platform-aware)
- [x] Photo viewing with zoom/pan
- [x] Gallery grid (customizable)
- [x] Milestone CRUD
- [x] Timeline view by month

#### ✅ Phase 5: Advanced Features (75%)
- [x] Backup/Export/Restore
- [x] Firebase Analytics
- [ ] TransTracks import
- [ ] ML face detection
- [ ] Crashlytics

#### ✅ Phase 6: UI/UX (85%)
- [x] Theme switching
- [x] Responsive layouts
- [x] Platform info
- [ ] Advanced animations

#### ✅ Phase 7: Multi-Platform (80%)
- [x] Android & iOS
- [x] Web (PWA)
- [x] Windows, macOS, Linux (configured)

## 📱 Platform Support

### ✅ Fully Supported
- **Android** - Phones and tablets (SDK 21+)
- **iOS** - iPhone and iPad (iOS 12+)
- **Web** - Modern browsers (PWA support)

### 🔄 Configured & Ready
- **Windows** - Windows 10+
- **macOS** - macOS 10.15+
- **Linux** - Ubuntu 20.04+

## 🎯 Features

### Working Features
- 📸 Photo capture with type selection (face/body/custom)
- 🖼️ Photo gallery with grid customization (2/3/4 columns)
- 🔍 Photo detail viewer with zoom/pan
- 📅 Timeline view grouped by month
- 🎯 Milestone management with CRUD operations
- 💾 Backup/Export/Restore functionality
- 🎨 Theme switching (Light/Dark/System)
- ⚙️ Settings persistence
- 📊 Firebase Analytics integration
- 📱 Responsive design for all screen sizes
- 🌐 Multi-platform support

### Coming Soon
- 🔒 PIN lock with secure storage
- 🎭 Decoy vault
- 🔄 TransTracks data import
- 🤖 ML face detection
- 🔥 Crashlytics integration
- ☁️ Firestore sync

## 🛠️ Development Setup

### Prerequisites

- Flutter SDK (3.0.0 or higher)
- Dart SDK
- Android Studio / Xcode (for mobile)
- Firebase project (for authentication and analytics)

### Installation

1. Clone the repository:
```bash
git clone https://github.com/shelbeely/OpenTransition.git
cd OpenTransition/flutter_app
```

2. Install dependencies:
```bash
flutter pub get
```

3. Set up Firebase:
   - Create a Firebase project
   - Download `google-services.json` (Android) and `GoogleService-Info.plist` (iOS)
   - Place them in `android/app/` and `ios/Runner/` respectively

4. Run the app:
```bash
# Android
flutter run -d android

# iOS
flutter run -d ios

# Web
flutter run -d chrome

# Windows
flutter run -d windows

# macOS
flutter run -d macos

# Linux
flutter run -d linux
```

## 📦 Dependencies

Key dependencies used in this project:

**State & Data:**
- provider (state management)
- sqflite, sqflite_sqlcipher (database)
- shared_preferences, flutter_secure_storage (storage)

**UI & Platform:**
- camera, image_picker (photos)
- local_auth (biometrics)
- file_picker (file operations)

**Firebase:**
- firebase_auth, firebase_analytics, firebase_crashlytics, cloud_firestore

**Utilities:**
- intl (internationalization)
- uuid (unique IDs)
- image (image processing)

See `pubspec.yaml` for the complete list.

## 🏗️ Architecture

The app follows a clean architecture pattern:

```
lib/
├── main.dart                 # App entry point
├── models/                   # Data models (Photo, Milestone)
├── services/                 # Business logic & data services
│   ├── database_service.dart
│   ├── photo_service.dart
│   ├── milestone_service.dart
│   ├── backup_service.dart
│   ├── analytics_service.dart
│   └── storage_service.dart
├── providers/                # State management
│   ├── photo_provider.dart
│   ├── milestone_provider.dart
│   └── settings_provider.dart
├── screens/                  # UI screens
│   ├── splash_screen.dart
│   ├── home_screen.dart
│   ├── photos_screen.dart
│   ├── photo_detail_screen.dart
│   ├── gallery_screen.dart
│   ├── timeline_screen.dart
│   ├── milestones_screen.dart
│   ├── settings_screen.dart
│   └── backup_screen.dart
├── widgets/                  # Reusable UI components
├── theme/                    # App theming
└── utils/                    # Utility functions
```

## 🔐 Security

- Database encryption using SQLCipher
- Secure storage for sensitive data
- Biometric authentication support (mobile)
- PIN/Pattern lock (in development)
- Encrypted backups
- Decoy vault support (planned)

## 📊 Statistics

- **32 Dart files** (~8,200 lines of code)
- **7 services** for business logic
- **3 state providers** for UI state
- **16 screens** implemented
- **6 platforms** supported

## 🤝 Contributing

This is a work in progress. Contributions are welcome!

See the main repository README for contribution guidelines.

## 📄 License

GPL v3 - Same as the original Android application

## 🔗 Links

- [Original Android Version](../)
- [Documentation](https://shelbeely.github.io/OpenTransition)
- [GitHub Repository](https://github.com/shelbeely/OpenTransition)
- [Platform Support Details](./PLATFORMS.md)
- [Migration Strategy](./MIGRATION.md)
- [Progress Tracking](./PROGRESS.md)

## 🎯 Roadmap

### Short-term (Current Sprint)
- PIN lock with secure storage
- Crashlytics integration
- ML face detection
- TransTracks import tool

### Medium-term
- Firestore sync
- Desktop biometric auth
- Advanced animations
- Comprehensive testing

### Long-term
- Feature parity with Android
- App store releases (iOS, Android, Web)
- Desktop releases (Windows, macOS, Linux)
- Performance optimization
- Accessibility improvements
