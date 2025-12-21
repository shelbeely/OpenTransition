# OpenTransition - Flutter

This is the Flutter cross-platform version of OpenTransition, a transition tracking application made specifically for transgender people.

## 🚀 Migration Status

This Flutter application is a complete rewrite of the original Android (Kotlin/Java) application. The migration is currently in progress.

### Current Progress

#### ✅ Phase 1: Project Setup (Completed)
- [x] Flutter project structure created
- [x] Dependencies configured in pubspec.yaml
- [x] Material Design 3 theme setup
- [x] Android configuration files

#### ✅ Phase 2: Core Infrastructure (In Progress)
- [x] Database service (SQLite with encryption support)
- [x] Authentication service (Firebase Auth)
- [x] Storage service (secure storage + shared preferences)
- [x] Data models (Photo, Milestone)
- [ ] State management providers
- [ ] Navigation system

#### ✅ Phase 3: UI Screens (Basic Structure)
- [x] Splash screen
- [x] Lock screen with biometric support
- [x] Home screen with bottom navigation
- [x] Photos screen (placeholder)
- [x] Milestones screen (placeholder)
- [x] Gallery screen (placeholder)
- [x] Settings screen (structure complete)

#### 🔄 Phase 4-9: To Be Implemented
- [ ] Camera integration
- [ ] Photo management
- [ ] Milestone management
- [ ] Firebase integration
- [ ] Backup/Export functionality
- [ ] TransTracks import
- [ ] ML Kit face detection
- [ ] iOS support
- [ ] Testing
- [ ] Documentation

## 📱 Features (Planned)

- 📸 **Photo Tracking** - Face, body, and custom area photos
- 🎯 **Milestone Management** - Track important events
- 🖼️ **Gallery View** - Browse and compare progress photos
- 🔒 **Privacy & Security** - App lock, biometric auth, encryption
- 🎭 **Decoy Vault** - Separate vault with different passcode
- 💾 **Data Control** - Export, backup, and sync
- 🔄 **Import from TransTracks** - Migrate from Android version
- 🎨 **Material Design 3** - Modern, beautiful UI
- 📱 **Cross-Platform** - iOS and Android support

## 🛠️ Development Setup

### Prerequisites

- Flutter SDK (3.0.0 or higher)
- Dart SDK
- Android Studio / Xcode
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
flutter run
```

## 📦 Dependencies

Key dependencies used in this project:

- **UI & State Management**: provider, material_design_icons_flutter
- **Database**: sqflite, sqflite_sqlcipher
- **Storage**: shared_preferences, flutter_secure_storage
- **Authentication**: firebase_auth, local_auth
- **Camera & Photos**: camera, image_picker, photo_manager
- **ML**: google_mlkit_face_detection
- **Utilities**: intl, uuid, rxdart, encrypt

See `pubspec.yaml` for the complete list.

## 🏗️ Architecture

The app follows a clean architecture pattern with separation of concerns:

```
lib/
├── main.dart                 # App entry point
├── models/                   # Data models
├── services/                 # Business logic & data services
├── screens/                  # UI screens
├── widgets/                  # Reusable UI components
├── theme/                    # App theming
└── utils/                    # Utility functions
```

## 🔐 Security

- Database encryption using SQLCipher
- Secure storage for sensitive data
- Biometric authentication support
- PIN/Pattern lock
- Encrypted backups
- Decoy vault support

## 🤝 Contributing

This is a work in progress. Contributions are welcome!

## 📄 License

GPL v3 - Same as the original Android application

## 🔗 Links

- [Original Android Version](../)
- [Documentation](https://shelbeely.github.io/OpenTransition)
- [GitHub Repository](https://github.com/shelbeely/OpenTransition)
