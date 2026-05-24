# OpenTransition Flutter Web App

This is a Flutter implementation of OpenTransition that can be compiled for both Web and Android platforms.

## Features

- 📸 Photo tracking (face, body types)
- 🎯 Milestone management
- 🖼️ Gallery view with filtering
- 🔒 PIN-based app lock
- 💾 Import/Export .ttbackup files (compatible with Android app)
- 🎨 Material 3 Design
- 📱 Responsive design (mobile & desktop)

## Prerequisites

- Flutter SDK 3.24 or later
- Dart SDK 3.5 or later

## Getting Started

### 1. Install Flutter

```bash
# Download Flutter SDK
git clone https://github.com/flutter/flutter.git -b stable
export PATH="$PATH:`pwd`/flutter/bin"

# Verify installation
flutter doctor
```

### 2. Initialize Project

```bash
cd flutter_webapp
flutter create . --platforms=web,android
flutter pub get
```

### 3. Run Web App

```bash
# Run in debug mode
flutter run -d chrome

# Build for production
flutter build web --release
```

### 4. Run on Android

```bash
# Connect device or start emulator
flutter devices

# Run on device
flutter run

# Build APK
flutter build apk --release
```

## Project Structure

```
flutter_webapp/
├── lib/
│   ├── main.dart                 # App entry point
│   ├── models/
│   │   ├── photo.dart           # Photo model
│   │   └── milestone.dart       # Milestone model
│   ├── services/
│   │   ├── database_service.dart # IndexedDB/SQLite wrapper
│   │   └── backup_service.dart   # .ttbackup import/export
│   ├── screens/
│   │   ├── home_screen.dart
│   │   ├── gallery_screen.dart
│   │   ├── milestones_screen.dart
│   │   └── settings_screen.dart
│   ├── widgets/
│   │   ├── photo_card.dart
│   │   ├── milestone_card.dart
│   │   └── app_drawer.dart
│   └── theme/
│       └── app_theme.dart        # Material 3 theme
├── web/
│   ├── index.html
│   ├── manifest.json
│   └── icons/
├── android/
│   └── (Android platform files)
├── pubspec.yaml
└── README.md
```

## Dependencies

Key packages used:
- `flutter`: SDK
- `shared_preferences`: Settings storage
- `path_provider`: File system access
- `image_picker`: Photo selection
- `sqflite`: Local database (mobile)
- `idb_shim`: IndexedDB (web)
- `archive`: ZIP file handling for .ttbackup
- `intl`: Date formatting

## Building for Production

### Web
```bash
flutter build web --release
# Output: build/web/
# Deploy to: GitHub Pages, Netlify, Vercel, Firebase Hosting
```

### Android
```bash
flutter build appbundle --release
# Output: build/app/outputs/bundle/release/app-release.aab
# Upload to Google Play Store
```

## License

GPL v3 - Same as the main OpenTransition project
