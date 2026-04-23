# OpenTransition

OpenTransition is a transition tracking application made specifically for transgender people, focusing on photo tracking and milestone management. It helps you document your journey privately and securely.

OpenTransition began as a fork of the original **TransTracks** application. TransTracks was [retired from the Google Play Store in 2025](https://github.com/TransTracks/TransTracks-Android) and its repository is now archived and read-only. OpenTransition is the active successor — the recommended way to continue tracking your transition journey. If you were a TransTracks user, see [Importing from TransTracks](#-importing-from-transtracks) below.

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

## 📸 Screenshots

<!-- SCREENSHOTS-START -->
> 🤖 **Auto-generated** — refreshed by CI on every push to `main`.  Captured on a `medium_phone` emulator (API 36, debug build).

| | | |
|:---:|:---:|:---:|
| ![Home](screenshots/02_home.png) | ![Settings](screenshots/06_settings_top.png) | ![Milestones](screenshots/13_milestones_empty.png) |
| **Home** | **Settings** | **Milestones** |
| ![Face Gallery](screenshots/16_face_gallery_empty.png) | ![Audio Gallery](screenshots/23_audio_gallery_empty.png) | ![Record Audio](screenshots/24_record_audio_idle.png) |
| **Face Gallery** | **Audio Gallery** | **Record Audio** |

[📂 View all 40 screenshots →](screenshots/README.md)
<!-- SCREENSHOTS-END -->

## ✨ Key Features

- 📸 **Photo Tracking** - Document your transition with organized photos (face, body, custom areas)
- 📸 **Face-Detection Camera** - CameraX-powered camera with ML Kit face detection for perfectly framed face photos
- 🎯 **Milestone Management** - Record and celebrate important events
- 🖼️ **Gallery View** - Browse and compare your progress photos
- 🎙️ **Audio Tracking** (preview) — Record voice samples and revisit them over time. Pitch and formant values displayed alongside recordings are currently typical estimates rather than measurements of your audio; on-device DSP analysis is planned for a future release. See [ISSUE-005](audit-report/07-issues-and-bugs.md#issue-005--correctness-mobile).
- 🔒 **Privacy First** - App lock (PIN, pattern, or biometric), disguised mode, and local storage
- 🔐 **Optional Encryption** - Encrypt your database with SQLCipher (optional)
- 🎭 **Decoy Vault** - Create a separate vault with different passcode for added security
- 💾 **Data Control** - Export, backup, and sync on your terms
- 🔄 **Import Backups** - Import `.ttbackup` data from the original TransTracks app
- 🎨 **Customizable** - Multiple themes and personalization options
- ⌚ **Wear OS Companion** - Trigger photo capture and view milestones from your smartwatch

## 🔄 Importing from TransTracks

**TransTracks was retired from the Google Play Store in 2025.** If you were a TransTracks user, OpenTransition is the recommended way to continue your journey. It is fully compatible with the `.ttbackup` backup format that TransTracks produces.

### How to get your data out of TransTracks

If you still have TransTracks installed on your phone:

1. Open TransTracks
2. Go to **Settings → Export**
3. The app will bundle everything into a `.ttbackup` file and open the Android share sheet
4. Save it somewhere safe (Google Drive, email, Files app, etc.)

The `.ttbackup` file is a standard ZIP archive with a custom extension — you can rename it to `.zip` to inspect its contents on a computer.

If you have already uninstalled TransTracks but used the same Google account, you may still be able to reinstall it from the Play Store under **Manage apps and device → Manage → Not installed**. Android auto-backup usually restores the data.

### Importing into OpenTransition

1. Locate your `.ttbackup` file
2. Tap it to open it with OpenTransition (or go to Settings → Import Backup)
3. Confirm the import
4. Wait for the import to complete — all photos, milestones, and metadata are preserved

### Technical Details

OpenTransition uses **Room database** instead of the Realm database used by the original TransTracks. Backwards-compatible import utilities handle the conversion automatically:

- **Realm data models preserved** — used only for reading TransTracks backups
- **Automatic conversion** from Realm to Room format during import
- **No data loss** — all photos, milestones, and audio analyses are preserved
- **One-time process** — after import, everything runs on the Room database

All Realm-related code is marked with `BACKWARDS COMPATIBILITY` comments.

See [ENCRYPTED_DATABASE.md](ENCRYPTED_DATABASE.md) for details on the database architecture and optional SQLCipher encryption.

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

## 👥 Contributors

We are grateful to everyone who has contributed to OpenTransition!

View the full, up-to-date list on [GitHub contributors](https://github.com/shelbeely/OpenTransition/graphs/contributors).

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

OpenTransition is based on the original **TransTracks** application. TransTracks was retired from the Google Play Store in 2025; its source code is archived at [github.com/TransTracks/TransTracks-Android](https://github.com/TransTracks/TransTracks-Android). We are deeply grateful to the TransTracks developers and contributors for creating this valuable tool for the transgender community. Their work provided the foundation that makes OpenTransition possible.

[Full Credits & Attribution →](https://shelbeely.github.io/OpenTransition/credits/)
