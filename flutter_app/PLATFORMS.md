# Multi-Platform Support

OpenTransition Flutter is designed to run on multiple platforms from a single codebase.

## Supported Platforms

### ✅ Fully Supported
- **Android** - Phones and tablets (SDK 21+)
- **iOS** - iPhone and iPad (iOS 12+)
- **Web** - Modern browsers (Chrome, Firefox, Safari, Edge)

### 🔄 In Development
- **Windows** - Windows 10 and later
- **macOS** - macOS 10.15 (Catalina) and later
- **Linux** - Ubuntu 20.04 and later

## Platform-Specific Features

### Mobile (Android & iOS)
- ✅ Camera capture with front/back switching
- ✅ Biometric authentication (fingerprint/face)
- ✅ Local notifications
- ✅ File system access
- ✅ SQLite database with encryption
- ✅ Firebase integration (Auth, Analytics, Crashlytics)
- ✅ Photo gallery access
- ✅ Share functionality

### Web
- ✅ Responsive design
- ✅ Browser-based photo upload
- ✅ IndexedDB storage
- ✅ Progressive Web App (PWA) support
- ⚠️ Limited camera access (requires user permission)
- ⚠️ No biometric authentication
- ⚠️ No local file system access (uses browser storage)

### Desktop (Windows, macOS, Linux)
- ✅ File system access
- ✅ SQLite database
- ✅ Keyboard shortcuts
- ✅ Window management
- 🔄 Camera access (Windows & macOS)
- 🔄 Biometric authentication (Windows Hello, Touch ID)
- 🔄 Native notifications
- ⚠️ Firebase may have limitations

## Building for Different Platforms

### Android
```bash
flutter build apk                 # Build APK
flutter build appbundle           # Build App Bundle for Play Store
flutter run -d android            # Run on connected device
```

### iOS
```bash
flutter build ios                 # Build iOS app
flutter run -d ios                # Run on connected device/simulator
```

### Web
```bash
flutter build web                 # Build for web
flutter run -d chrome             # Run in Chrome
flutter run -d web-server         # Run local dev server
```

### Windows
```bash
flutter build windows             # Build Windows app
flutter run -d windows            # Run on Windows
```

### macOS
```bash
flutter build macos               # Build macOS app
flutter run -d macos              # Run on macOS
```

### Linux
```bash
flutter build linux               # Build Linux app
flutter run -d linux              # Run on Linux
```

## Platform Detection

The app uses `PlatformUtils` class to detect the current platform:

```dart
import 'package:opentransition/utils/platform_utils.dart';

// Check platform
if (PlatformUtils.isWeb) {
  // Web-specific code
} else if (PlatformUtils.isMobile) {
  // Mobile-specific code
} else if (PlatformUtils.isDesktop) {
  // Desktop-specific code
}

// Check capabilities
if (PlatformUtils.supportsCameraCapture) {
  // Show camera button
}
```

## Platform-Specific Implementations

### Camera
- **Mobile**: Uses `camera` package for native camera
- **Web**: Uses `image_picker` with browser camera API
- **Desktop**: Uses `image_picker` with webcam access

### Storage
- **Mobile & Desktop**: SQLite with SQLCipher encryption
- **Web**: IndexedDB or browser storage (no encryption)

### Biometrics
- **Android**: Fingerprint, Face Unlock
- **iOS**: Touch ID, Face ID
- **Windows**: Windows Hello (planned)
- **macOS**: Touch ID (planned)
- **Web**: Not supported
- **Linux**: Not supported

### File System
- **Mobile & Desktop**: Full file system access
- **Web**: Browser-based storage only

## Development Tips

### Testing on All Platforms

1. **Mobile**: Use physical devices or emulators
   ```bash
   flutter devices              # List available devices
   flutter run -d <device-id>   # Run on specific device
   ```

2. **Web**: Test in multiple browsers
   ```bash
   flutter run -d chrome
   flutter run -d edge
   ```

3. **Desktop**: Build and test natively
   ```bash
   flutter run -d windows
   flutter run -d macos
   flutter run -d linux
   ```

### Conditional Compilation

Use platform checks to conditionally include platform-specific code:

```dart
import 'dart:io' show Platform;
import 'package:flutter/foundation.dart' show kIsWeb;

void initializePlatformFeatures() {
  if (kIsWeb) {
    // Web initialization
  } else if (Platform.isAndroid || Platform.isIOS) {
    // Mobile initialization
  } else {
    // Desktop initialization
  }
}
```

### Platform-Specific Packages

Some packages work differently on different platforms:

- `sqflite` - Mobile & Desktop (not Web)
- `shared_preferences` - All platforms
- `camera` - Mobile & Desktop (limited Web support)
- `local_auth` - Mobile & Desktop
- `firebase_*` - Mobile & Web (limited Desktop support)

## Known Limitations

### Web
- No local file system access
- No biometric authentication
- Camera requires browser permissions
- Database encryption not available
- Limited offline capabilities

### Desktop
- Firebase features may be limited
- Biometric auth requires platform support
- Camera quality may vary
- Some mobile-specific packages don't work

### Future Improvements

- [ ] Windows Hello integration
- [ ] macOS Touch ID integration
- [ ] Linux keyring integration
- [ ] Better web offline support
- [ ] Desktop camera optimization
- [ ] Cross-platform sync
- [ ] Unified notification system

## Testing Matrix

| Feature | Android | iOS | Web | Windows | macOS | Linux |
|---------|---------|-----|-----|---------|-------|-------|
| Camera | ✅ | ✅ | ⚠️ | 🔄 | 🔄 | ❌ |
| Biometrics | ✅ | ✅ | ❌ | 🔄 | 🔄 | ❌ |
| Database | ✅ | ✅ | ⚠️ | ✅ | ✅ | ✅ |
| Encryption | ✅ | ✅ | ❌ | ✅ | ✅ | ✅ |
| Firebase | ✅ | ✅ | ✅ | ⚠️ | ⚠️ | ⚠️ |
| Notifications | ✅ | ✅ | ⚠️ | 🔄 | 🔄 | 🔄 |

**Legend:**
- ✅ Fully supported
- ⚠️ Limited support
- 🔄 In development
- ❌ Not supported

## Contributing

When adding new features, always consider platform compatibility:

1. Check if the feature works on all platforms
2. Use platform detection for platform-specific code
3. Provide fallbacks for unsupported platforms
4. Update this documentation
5. Test on at least 2 platforms before submitting PR

## Resources

- [Flutter Platform Integration](https://docs.flutter.dev/platform-integration)
- [Building for Web](https://docs.flutter.dev/platform-integration/web)
- [Building for Desktop](https://docs.flutter.dev/platform-integration/desktop)
- [Conditional Imports](https://dart.dev/guides/libraries/create-library-packages#conditionally-importing-and-exporting-library-files)
