# Windows Platform Support

This directory contains Windows-specific configuration and code for OpenTransition.

## Prerequisites

- Windows 10 or later
- Visual Studio 2022 with C++ development tools
- Flutter SDK with Windows support enabled

## Building for Windows

```bash
flutter build windows
```

## Running on Windows

```bash
flutter run -d windows
```

## Platform-Specific Features

### Implemented
- File system access for photo storage
- Local database with SQLite

### Planned
- Windows Hello integration for biometric authentication
- Native notifications
- Windows-specific UI adaptations

## Known Limitations

- Camera support requires webcam access
- Some Firebase features may have limited functionality on desktop
- Biometric authentication depends on Windows Hello availability
