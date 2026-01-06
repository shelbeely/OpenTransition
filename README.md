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
- 🔐 **Optional Encryption** - Encrypt your database with SQLCipher (optional)
- 🎭 **Decoy Vault** - Create a separate vault with different passcode for added security
- 💾 **Data Control** - Export, backup, and sync on your terms
- 🔄 **Import Backups** - Import data from the original TransTracks app
- 🎨 **Customizable** - Multiple themes and personalization options
- ⌚ **Wear OS Companion** - Access key features from your smartwatch

## 🔄 Importing from TransTracks

OpenTransition maintains **backwards compatibility** with TransTracks. You can easily import your existing data:

### Import Process

1. **Export from TransTracks**: In TransTracks, go to Settings → Export Data
2. **Save the backup file**: This creates a `.realm` backup file
3. **Import to OpenTransition**: 
   - Open OpenTransition
   - Go to Settings
   - Tap "Import Backup"
   - Select your `.realm` backup file
   - Wait for import to complete

### Technical Details

OpenTransition uses **Room database** (modern Android database) instead of Realm. However, we maintain full backwards compatibility with TransTracks:

- **Realm data models preserved**: Used only for importing TransTracks backups
- **Migration utilities included**: Automatic conversion from Realm to Room format
- **No data loss**: All photos, milestones, and audio analyses are preserved
- **One-time process**: After import, everything runs on the new Room database

All Realm-related code is marked with `BACKWARDS COMPATIBILITY` comments and is maintained solely for importing data from TransTracks.

See [ENCRYPTED_DATABASE.md](ENCRYPTED_DATABASE.md) for more details on the database architecture.

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

## 🤖 AI Agent Skills

This repository includes **AI agent skills** in the `.github/skills/` directory from both OpenAI and Anthropic. These skills enhance AI-assisted development by providing specialized capabilities that are automatically available when using GitHub Copilot or Claude with this repository.

### OpenAI Skills (from [openai/skills](https://github.com/openai/skills))

**Curated Skills:**
- **gh-address-comments** - Address PR review comments efficiently
- **gh-fix-ci** - Debug and fix failing GitHub Actions CI/CD
- **notion-knowledge-capture** - Capture conversations into structured Notion pages
- **notion-meeting-intelligence** - Prepare meeting materials with context
- **notion-research-documentation** - Research and synthesize documentation
- **notion-spec-to-implementation** - Turn specs into implementation plans

**Experimental Skills:**
- **create-plan** - Create concise plans for coding tasks
- **linear** - Manage issues and workflows in Linear

**System Skills:**
- **skill-creator** - Guide for creating new skills
- **skill-installer** - Install skills from GitHub repositories

### Anthropic Skills (from [anthropics/skills](https://github.com/anthropics/skills))

**Creative & Design:**
- **algorithmic-art** - Create algorithmic art with p5.js
- **canvas-design** - Create beautiful visual art in PNG/PDF
- **frontend-design** - Create production-grade frontend interfaces
- **slack-gif-creator** - Create animated GIFs for Slack
- **theme-factory** - Style artifacts with themes

**Development & Technical:**
- **mcp-builder** - Create MCP (Model Context Protocol) servers
- **web-artifacts-builder** - Create complex web artifacts with React
- **webapp-testing** - Test web applications with Playwright

**Document Skills:**
- **docx** - Document creation, editing, and analysis
- **pdf** - PDF manipulation toolkit
- **pptx** - Presentation creation and editing
- **xlsx** - Spreadsheet creation and analysis

**Enterprise & Communication:**
- **brand-guidelines** - Apply brand colors and typography
- **doc-coauthoring** - Guide for co-authoring documentation
- **internal-comms** - Write internal communications

Learn more: [GitHub Copilot agent skills](https://docs.github.com/en/copilot/concepts/agents/about-agent-skills) | [Agent Skills Standard](http://agentskills.io)

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

We are grateful to everyone who has contributed to OpenTransition! Here are our code contributors:

- Cassie Wilson: 223 commits (47.2%)
- copilot-swe-agent[bot]: 172 commits (36.4%)
- TransTracks: 39 commits (8.3%)
- dependabot[bot]: 28 commits (5.9%)
- shelbeely: 6 commits (1.3%)
- Shelbee Johnson: 3 commits (0.6%)
- codefactor-io: 1 commit (0.2%)

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
