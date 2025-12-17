# OpenTransition Monorepo Structure

This repository is organized as a monorepo containing multiple Android applications:

## Modules

### 📱 mobile
The main OpenTransition Android application for phones and tablets. This is the primary app that users interact with for tracking their transition journey.

**Package**: `com.shelbeely.opentransition`
**Min SDK**: 21 (Android 5.0)
**Target SDK**: 36

### ⌚ wear
The Wear OS companion app for Android smartwatches. This is a **remote control and quick-view companion** that works with the mobile app.

**What it does:**
- 📸 **Quick Photo Trigger** - Tap your watch to open the camera on your phone
- 📊 **Milestone Summary** - See total milestones and latest achievement at a glance
- 🔄 **Manual Sync** - Request fresh data from your phone
- 🔗 **Connection Status** - Know when your phone app is reachable

**What it doesn't do:**
- ❌ Store photos (too large, kept on phone)
- ❌ Edit milestones (use phone for detailed work)
- ❌ Work without phone (companion app, not standalone)

**See [WEAR_APP_FEATURES.md](WEAR_APP_FEATURES.md) for detailed feature documentation.**

**Package**: `com.shelbeely.opentransition.wear`
**Min SDK**: 30 (Wear OS 3.0)
**Target SDK**: 36

### 🔗 shared
Shared library containing common code and data models used by both mobile and wear apps. This includes:
- Data models for communication
- Wearable Data Layer constants
- Shared utilities

**Package**: `com.shelbeely.opentransition.shared`

## Communication Between Apps

The mobile and wear apps communicate using the **Wearable Data Layer API** from Google Play Services. This enables:

- **Messaging**: Send commands and triggers (e.g., take a photo)
- **Data Sync**: Synchronize milestones and settings
- **Capability Detection**: Detect when companion app is installed and reachable

### Key Communication Paths

- `/opentransition/trigger_photo` - Trigger photo capture on mobile
- `/opentransition/sync_milestones` - Sync milestone data
- `/opentransition/milestone_update` - Notify of milestone changes
- `/opentransition/request_sync` - Request data sync

## Building the Apps

### Build All Modules
```bash
./gradlew build
```

### Build Individual Modules
```bash
# Mobile app only
./gradlew :mobile:assembleDebug

# Wear app only
./gradlew :wear:assembleDebug

# Shared library
./gradlew :shared:build
```

### Install on Devices
```bash
# Install mobile app
./gradlew :mobile:installDebug

# Install wear app (requires paired Wear OS device)
./gradlew :wear:installDebug
```

## Development Setup

1. **Set up Firebase** (required for mobile app)
   - Download `google-services.json` from Firebase Console
   - Place it in the `mobile/` folder

2. **Configure Secrets**
   - Copy `secrets.properties.example` to `secrets.properties`
   - Fill in required values

3. **Build the project**
   ```bash
   ./gradlew build
   ```

## Testing Communication

To test communication between the mobile and wear apps:

1. Install both apps on their respective devices
2. Ensure the devices are paired via Bluetooth
3. Launch the Wear app - it should detect the mobile app
4. Use the "Take Photo" button on the watch to trigger photo capture on the phone

## Adding the Wear App to an Existing Install

If you already have the mobile app installed and want to add the Wear companion:

1. Build and install the wear app: `./gradlew :wear:installDebug`
2. The apps will automatically detect each other when both are running
3. Open the Wear app to verify connection status

## Module Dependencies

```
mobile -> shared
wear -> shared
```

Both mobile and wear apps depend on the shared module for common code and communication constants.

## ProGuard / R8

Each module has its own ProGuard/R8 configuration:
- `mobile/proguard-rules.pro`
- `wear/proguard-rules.pro`

The shared module does not require ProGuard as it's a library module.

## Documentation

For more detailed documentation, see:
- [Mobile App Documentation](https://shelbeely.github.io/OpenTransition)
- [Development Guide](https://shelbeely.github.io/OpenTransition/getting-started/development-setup/)
