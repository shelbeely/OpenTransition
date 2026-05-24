# Flutter OpenTransition - Setup and Development Guide

## What is This?

This is a **complete Flutter project structure** for OpenTransition that can compile to both **Web** and **Android** platforms. This replaces the JavaScript web app with a shared codebase that will allow code reuse between web and a future Android app.

## Why Flutter?

- ✅ **Single Codebase**: Write once, deploy to web AND Android
- ✅ **Material 3**: Native Material Design 3 components
- ✅ **Performance**: Better performance than JavaScript
- ✅ **Shared Logic**: Same .ttbackup import/export code for both platforms
- ✅ **Future-Proof**: Easy to add iOS, Windows, macOS, Linux later

## Current Status

This is a **starter template** with:
- ✅ Complete project structure
- ✅ Material 3 theme (purple color scheme)
- ✅ Navigation structure (drawer + bottom nav)
- ✅ All screens (Home, Gallery, Milestones, Settings)
- ✅ Data models (Photo, Milestone)
- ✅ Service layer (Database, Backup)
- ✅ .ttbackup import/export framework

**What needs to be completed:**
- Database implementation (IndexedDB for web, SQLite for mobile)
- Photo picker integration
- Image storage and display
- Milestone CRUD operations
- Settings persistence
- PIN lock implementation

## Installation Steps

### 1. Install Flutter

```bash
# Download Flutter SDK
git clone https://github.com/flutter/flutter.git -b stable
export PATH="$PATH:`pwd`/flutter/bin"

# Verify installation
flutter doctor

# Install web tooling
flutter config --enable-web
```

### 2. Initialize the Project

```bash
cd flutter_webapp

# Create platform-specific files
flutter create . --platforms=web,android

# Get dependencies
flutter pub get
```

### 3. Run on Web

```bash
# Development mode with hot reload
flutter run -d chrome

# Or build for production
flutter build web --release

# Output will be in: build/web/
# Deploy to GitHub Pages, Netlify, Vercel, etc.
```

### 4. Run on Android

```bash
# List available devices
flutter devices

# Run on connected device/emulator
flutter run

# Or build APK
flutter build apk --release

# Output: build/app/outputs/flutter-apk/app-release.apk
```

## Project Structure Explained

```
flutter_webapp/
├── lib/
│   ├── main.dart                      # App entry point, navigation
│   │
│   ├── theme/
│   │   └── app_theme.dart             # Material 3 theme configuration
│   │
│   ├── models/
│   │   ├── photo.dart                 # Photo data model
│   │   └── milestone.dart             # Milestone data model
│   │
│   ├── services/
│   │   ├── database_service.dart      # Database abstraction
│   │   └── backup_service.dart        # .ttbackup import/export
│   │
│   ├── screens/
│   │   ├── home_screen.dart           # Home screen UI
│   │   ├── gallery_screen.dart        # Photo gallery UI
│   │   ├── milestones_screen.dart     # Milestones list UI
│   │   └── settings_screen.dart       # Settings UI
│   │
│   └── widgets/
│       └── (shared widgets will go here)
│
├── web/
│   ├── index.html                     # Web entry HTML
│   ├── manifest.json                  # PWA manifest
│   └── icons/                         # Web app icons
│
├── android/
│   └── (Android platform files)
│
├── pubspec.yaml                       # Dependencies configuration
└── README.md                          # This file
```

## Key Files

### `pubspec.yaml`
Contains all dependencies:
- `provider`: State management
- `shared_preferences`: Settings storage
- `image_picker`: Photo selection
- `sqflite`: Mobile database
- `idb_shim`: Web database (IndexedDB)
- `archive`: ZIP file handling for .ttbackup

### `lib/main.dart`
- App entry point
- Material 3 theme setup
- Navigation structure (drawer + bottom nav)
- Screen routing

### `lib/theme/app_theme.dart`
- Complete Material 3 theme
- Light and dark mode
- Purple color scheme matching Android app
- All component styles (cards, buttons, dialogs, etc.)

### `lib/services/backup_service.dart`
- Exports data to .ttbackup format (ZIP with data.json + images)
- Imports .ttbackup files from Android app
- Compatible with existing Android backup format

## Development Workflow

### 1. Hot Reload During Development

```bash
flutter run -d chrome
# Press 'r' to hot reload
# Press 'R' to hot restart
# Press 'q' to quit
```

### 2. Add New Features

1. Update models if needed (`lib/models/`)
2. Implement service methods (`lib/services/`)
3. Update UI screens (`lib/screens/`)
4. Test on web and Android

### 3. Build for Production

```bash
# Web
flutter build web --release
# Deploy build/web/ to hosting

# Android
flutter build appbundle --release
# Upload to Play Store
```

## Implementing Remaining Features

### Database (Priority 1)

**For Web (IndexedDB):**
```dart
// In lib/services/database_service.dart
import 'package:idb_shim/idb_browser.dart';

Future<void> _initWeb() async {
  final idbFactory = getIdbFactory()!;
  final db = await idbFactory.open('opentransition_db', version: 1,
    onUpgradeNeeded: (VersionChangeEvent event) {
      final db = event.database;
      db.createObjectStore('photos', keyPath: 'id');
      db.createObjectStore('milestones', keyPath: 'id');
    },
  );
  // Store db reference
}
```

**For Mobile (SQLite):**
```dart
// In lib/services/database_service.dart
import 'package:sqflite/sqflite.dart';
import 'package:path/path.dart';

Future<void> _initMobile() async {
  final dbPath = await getDatabasesPath();
  final path = join(dbPath, 'opentransition.db');
  
  final db = await openDatabase(
    path,
    version: 1,
    onCreate: (db, version) async {
      await db.execute('''
        CREATE TABLE photos (
          id TEXT PRIMARY KEY,
          type TEXT,
          imagePath TEXT,
          date TEXT,
          createdAt TEXT
        )
      ''');
      await db.execute('''
        CREATE TABLE milestones (
          id TEXT PRIMARY KEY,
          title TEXT,
          description TEXT,
          date TEXT,
          createdAt TEXT
        )
      ''');
    },
  );
  // Store db reference
}
```

### Photo Picker (Priority 2)

```dart
// In lib/screens/gallery_screen.dart
import 'package:image_picker/image_picker.dart';

Future<void> _pickImage() async {
  final picker = ImagePicker();
  final XFile? image = await picker.pickImage(source: ImageSource.gallery);
  
  if (image != null) {
    // Process image
    // Save to database
  }
}
```

### State Management (Priority 3)

```dart
// Create lib/providers/photo_provider.dart
import 'package:flutter/foundation.dart';
import '../models/photo.dart';
import '../services/database_service.dart';

class PhotoProvider extends ChangeNotifier {
  final DatabaseService _db;
  List<Photo> _photos = [];
  
  PhotoProvider(this._db);
  
  List<Photo> get photos => _photos;
  
  Future<void> loadPhotos() async {
    _photos = await _db.getPhotos();
    notifyListeners();
  }
  
  Future<void> addPhoto(Photo photo) async {
    await _db.addPhoto(photo);
    await loadPhotos();
  }
}
```

## Deploying

### Web Deployment

```bash
flutter build web --release
```

Deploy `build/web/` to:
- **GitHub Pages**: Push to `gh-pages` branch
- **Netlify**: Drag and drop `build/web/` folder
- **Vercel**: Connect GitHub repo
- **Firebase Hosting**: `firebase deploy`

### Android Deployment

```bash
flutter build appbundle --release
```

Upload `build/app/outputs/bundle/release/app-release.aab` to Google Play Console.

## Migration from JavaScript Web App

The JavaScript web app in `/webapp` directory will remain functional. This Flutter app is a **new implementation** that:

1. Uses the same .ttbackup format (fully compatible)
2. Provides better performance
3. Allows code sharing with future Android app
4. Uses native Material 3 components

**Migration strategy:**
1. Keep JavaScript web app as-is (it works!)
2. Develop Flutter app in parallel
3. Test feature parity
4. Switch to Flutter when ready

## Troubleshooting

### Flutter Doctor Issues

```bash
flutter doctor
# Fix any issues reported
```

### Dependency Issues

```bash
flutter pub get
flutter pub upgrade
flutter clean
```

### Web Build Issues

```bash
flutter config --enable-web
flutter create . --platforms=web
flutter build web --release
```

### Android Build Issues

```bash
flutter doctor --android-licenses
# Accept all licenses
```

## Next Steps

1. **Complete database implementation** (IndexedDB + SQLite)
2. **Add photo picker and display**
3. **Implement milestone CRUD**
4. **Add settings persistence**
5. **Implement PIN lock**
6. **Test .ttbackup import/export**
7. **Add unit tests**
8. **Deploy to web and Android**

## Resources

- [Flutter Documentation](https://docs.flutter.dev/)
- [Material 3 for Flutter](https://docs.flutter.dev/ui/design/material)
- [IndexedDB Package](https://pub.dev/packages/idb_shim)
- [SQLite Package](https://pub.dev/packages/sqflite)
- [Image Picker Package](https://pub.dev/packages/image_picker)

## License

GPL v3 - Same as the main OpenTransition project

## Support

For issues and questions:
- GitHub Issues: https://github.com/shelbeely/OpenTransition/issues
- Documentation: https://shelbeely.github.io/OpenTransition
