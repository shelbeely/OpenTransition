# OpenTransition

OpenTransition is a transition tracking application made specifically for transgender people, focusing on photo tracking and milestone management. It helps you document your journey privately and securely.

This is a customized fork of the original TransTracks application, rebranded and repackaged for independent deployment.

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
- 🤖 **AI Assistance** - On-device AI helps improve milestone text (powered by Gemini Nano)
- 🖼️ **Gallery View** - Browse and compare your progress photos
- 🔒 **Privacy First** - App lock, disguised mode, and local storage
- 💾 **Data Control** - Export, backup, and sync on your terms
- 🎨 **Customizable** - Multiple themes and personalization options

### 🆕 AI Features (New!)

OpenTransition now includes on-device AI powered by Google's Gemini Nano:
- ✓ **Proofread** milestone text for grammar and spelling
- ✨ **Improve** text with different styles (formal, casual, shorter, longer)
- 🔒 **100% Private** - all AI processing happens on your device

[Learn more about AI features →](GEMINI_NANO_AI_FEATURES.md)

## 🚀 Quick Start for Development

1. Set up a Firebase project for development
2. Download the `google-services.json` file from Firebase console
3. Place the `google-services.json` file in the `app/` folder
4. Copy `secrets.properties.example` to `secrets.properties`
5. Build and run: `./gradlew build`

**Full setup instructions:** [Development Setup Guide](https://shelbeely.github.io/OpenTransition/getting-started/development-setup/)

## 📦 Package Information

- **Package Name**: `com.shelbeely.opentransition`
- **App Name**: OpenTransition
- **Minimum SDK**: 26 (Android 8.0) - Required for AI features
- **Target SDK**: 36
- This fork uses a different package name to allow independent deployment

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

## License

OpenTransition is a fork of TransTracks and maintains the same GPL v3 license.

```
Original Copyright (C) 2018 - 2021 TransTracks
Fork modifications and rebranding by the OpenTransition contributors

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
