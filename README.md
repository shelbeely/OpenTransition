# OpenTransition

OpenTransition is a transition tracking application made specifically for transgender people, focusing on photo tracking and milestone management. It helps you document your journey privately and securely.

This is a customized fork of the original TransTracks application, rebranded and repackaged for independent deployment.

## 📱 Multi-Platform Support

This repository is organized as a **monorepo** containing multiple Android applications:

- 📱 **Mobile App** - Full-featured Android app for phones and tablets
- ⌚ **Wear OS App** - Companion app for Android smartwatches
- 🔗 **Shared Module** - Common code and data models for communication

The mobile and Wear OS apps communicate using Google's Wearable Data Layer API, allowing you to:
- Trigger photo capture from your smartwatch
- View recent milestones on your wrist
- Receive notifications on your watch

**See [MONOREPO.md](MONOREPO.md) for detailed information about the monorepo structure and inter-app communication.**

## 📚 Documentation

**Complete documentation is available at:** [**https://shelbeely.github.io/OpenTransition**](https://shelbeely.github.io/OpenTransition)

### For Users

- 📱 [User Guide](https://shelbeely.github.io/OpenTransition/user-guide/getting-started/) - Learn how to use the app
- ❓ [FAQ](https://shelbeely.github.io/OpenTransition/user-guide/faq/) - Frequently asked questions
- 🎨 [Features Overview](https://shelbeely.github.io/OpenTransition/user-guide/features/) - Discover what the app can do
- 🔒 [Privacy & Security](https://shelbeely.github.io/OpenTransition/user-guide/privacy/) - Learn about data protection

### For Developers

- 🚀 [Getting Started](https://shelbeely.github.io/OpenTransition/getting-started/overview/) - Set up your development environment
- 🏗️ [Architecture](https://shelbeely.github.io/OpenTransition/architecture/overview/) - Understand the app structure
- 🧪 [Development Guide](https://shelbeely.github.io/OpenTransition/development/code-style/) - Coding standards and practices
- 🤝 [Contributing](https://shelbeely.github.io/OpenTransition/contributing/guidelines/) - How to contribute to the project

## ✨ Key Features

- 📸 **Photo Tracking** - Document your transition with organized photos (face, body, custom areas)
- 🎯 **Milestone Management** - Record and celebrate important events
- 🖼️ **Gallery View** - Browse and compare your progress photos
- 🔒 **Privacy First** - App lock, disguised mode, and local storage
- 💾 **Data Control** - Export, backup, and sync on your terms
- 🎨 **Customizable** - Multiple themes and personalization options
- ⌚ **Wear OS Companion** - Access key features from your smartwatch

## 🚀 Quick Start for Development

1. Set up a Firebase project for development
2. Download the `google-services.json` file from Firebase console
3. Place the `google-services.json` file in the `mobile/` folder (note: was `app/` previously)
4. Copy `secrets.properties.example` to `secrets.properties`
5. Build and run: `./gradlew build`

**Build specific modules:**
```bash
# Mobile app only
./gradlew :mobile:assembleDebug

# Wear OS app only
./gradlew :wear:assembleDebug

# All modules
./gradlew build
```

**Full setup instructions:** [Development Setup Guide](https://shelbeely.github.io/OpenTransition/getting-started/development-setup/)

## 📦 Package Information

### Mobile App
- **Package Name**: `com.shelbeely.opentransition`
- **App Name**: OpenTransition
- **Minimum SDK**: 21 (Android 5.0)
- **Target SDK**: 36

### Wear OS App
- **Package Name**: `com.shelbeely.opentransition.wear`
- **App Name**: OpenTransition
- **Minimum SDK**: 30 (Wear OS 3.0)
- **Target SDK**: 36

This fork uses a different package name to allow independent deployment

## 🤝 Contributing

We welcome contributions! If you'd like to help improve OpenTransition:

1. Check the [issues](https://github.com/shelbeely/OpenTransition/issues) for ideas
2. Read our [Contributing Guidelines](https://shelbeely.github.io/OpenTransition/contributing/guidelines/)
3. Fork the repository and make your changes
4. Submit a pull request

**Areas we need help with:**
- Bug fixes and testing
- Feature development
- Documentation improvements
- Translations/localization
- UI/UX improvements

## 🔄 Technology Migration

Interested in helping modernize OpenTransition's UI? We've created comprehensive migration guides:

- **[Migration Analysis](MIGRATION_ANALYSIS.md)** - Complete analysis of Flutter vs Jetpack Compose migration
- **[Jetpack Compose Guide](docs/migration/compose-migration-guide.md)** - Practical step-by-step migration guide
- **[Decision Checklist](docs/migration/decision-checklist.md)** - Framework selection criteria

**TL;DR:** We recommend **Jetpack Compose** for this project (3-6 months, incremental migration, keeps Wear OS support)

## 👥 Contributors

We are grateful to everyone who has contributed to OpenTransition! Here are our code contributors:

- copilot-swe-agent[bot]: 4 commits (80.0%)
- Shelbee Johnson: 1 commits (20.0%)

View all contributors on [GitHub](https://github.com/shelbeely/OpenTransition/graphs/contributors).

## License

OpenTransition is a fork of TransTracks and maintains the same GPL v3 license.

```
Original Copyright (C) 2018 - 2021 TransTracks
Fork modifications and rebranding (C) 2025 Shelbeely and OpenTransition contributors

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
```

## Credits

OpenTransition is based on the original **TransTracks** application. We are deeply grateful to the TransTracks developers and contributors for creating this valuable tool for the transgender community. Their work provided the foundation that makes OpenTransition possible.

[Full Credits & Attribution →](https://shelbeely.github.io/OpenTransition/credits/)
