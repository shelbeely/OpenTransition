# Getting Started Overview

This guide will help you get started with OpenTransition development.

## Prerequisites

Before you begin, ensure you have the following installed:

### Required Software

- **Android Studio**: Latest stable version (Hedgehog or newer recommended)
- **JDK**: Java Development Kit 17
- **Git**: For version control
- **Firebase Account**: For development testing

### Recommended Tools

- **Android Emulator**: For testing without a physical device
- **ADB (Android Debug Bridge)**: Comes with Android Studio
- **Gradle**: Included with the project

## System Requirements

### For Development

=== "Windows"
    - Windows 10 or later (64-bit)
    - 8 GB RAM minimum (16 GB recommended)
    - 10 GB free disk space
    - Screen resolution: 1280x800 minimum

=== "macOS"
    - macOS 10.14 (Mojave) or later
    - 8 GB RAM minimum (16 GB recommended)
    - 10 GB free disk space
    - Screen resolution: 1280x800 minimum

=== "Linux"
    - 64-bit GNU/Linux distribution
    - 8 GB RAM minimum (16 GB recommended)
    - 10 GB free disk space
    - GNOME or KDE desktop

### For Running the App

- **Android Device or Emulator**: Android 5.0 (API 21) or higher
- **Google Play Services**: Required for Firebase features
- **Camera**: Optional, for taking photos directly in the app
- **Storage Permission**: For importing photos

## What You'll Learn

In this getting started section, you will learn:

1. **[Development Setup](development-setup.md)**: How to set up your development environment, configure Firebase, and clone the repository.

2. **[Building the App](building.md)**: How to build and run the app on an emulator or physical device, including debug and release builds.

## Quick Start

If you're eager to get started quickly:

```bash
# Clone the repository
git clone https://github.com/shelbeely/OpenTransition.git
cd OpenTransition

# Copy the example secrets file
cp secrets.properties.example secrets.properties

# Edit secrets.properties with your configuration
# Download google-services.json from Firebase Console
# Place it in the app/ directory

# Build and run
./gradlew build
```

!!! warning "Firebase Configuration Required"
    The app requires Firebase configuration to build successfully. See the [Development Setup](development-setup.md) guide for detailed instructions.

## Understanding the Project Structure

```
OpenTransition/
├── .github/              # GitHub workflows and configurations
│   └── workflows/        # CI/CD pipeline definitions
├── app/                  # Main application module
│   ├── src/
│   │   ├── main/        # Main source code
│   │   ├── test/        # Unit tests
│   │   └── androidTest/ # Instrumentation tests
│   └── build.gradle     # App-level build configuration
├── docs/                 # Documentation source files
├── gradle/              # Gradle wrapper files
├── keys/                # Keystore files (gitignored)
├── build.gradle         # Project-level build configuration
├── settings.gradle      # Gradle settings
├── mkdocs.yml          # Documentation configuration
└── README.md           # Project README
```

## Next Steps

Ready to set up your development environment? Proceed to:

→ [Development Setup](development-setup.md)

## Need Help?

If you run into issues:

1. Check the [Development Setup](development-setup.md) guide for detailed instructions
2. Search existing [GitHub Issues](https://github.com/shelbeely/OpenTransition/issues)
3. Create a new issue if your problem isn't already reported
4. Join the community discussions

## Additional Resources

- [Android Developer Documentation](https://developer.android.com/)
- [Kotlin Language Documentation](https://kotlinlang.org/docs/)
- [Firebase Documentation](https://firebase.google.com/docs)
- [Material Design Guidelines](https://material.io/design)
