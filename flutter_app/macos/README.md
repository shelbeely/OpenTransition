# macOS Platform Support

This directory contains macOS-specific configuration and code for OpenTransition.

## Prerequisites

- macOS 10.15 or later
- Xcode 13 or later
- Flutter SDK with macOS support enabled

## Building for macOS

```bash
flutter build macos
```

## Running on macOS

```bash
flutter run -d macos
```

## Platform-Specific Features

### Implemented
- File system access for photo storage
- Local database with SQLite

### Planned
- Touch ID / Face ID integration
- Native notifications
- macOS-specific UI adaptations
- Keychain integration for secure storage

## Known Limitations

- Camera support requires camera permissions
- Some Firebase features may have limited functionality on desktop
- App sandboxing may restrict certain file operations
