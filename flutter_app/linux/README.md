# Linux Platform Support

This directory contains Linux-specific configuration and code for OpenTransition.

## Prerequisites

- Linux (Ubuntu 20.04 or later recommended)
- CMake
- Ninja build system
- GTK development libraries
- Flutter SDK with Linux support enabled

## Building for Linux

```bash
flutter build linux
```

## Running on Linux

```bash
flutter run -d linux
```

## Platform-Specific Features

### Implemented
- File system access for photo storage
- Local database with SQLite

### Planned
- Native notifications
- Linux-specific UI adaptations
- Secret Service integration for secure storage

## Known Limitations

- Camera support depends on V4L2 availability
- Some Firebase features may have limited functionality on desktop
- UI may need adjustments for different desktop environments (GNOME, KDE, etc.)
