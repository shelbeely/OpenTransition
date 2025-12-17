# Architecture: Monorepo & Inter-App Communication

This document describes the technical architecture of the OpenTransition monorepo and how the mobile and Wear OS apps communicate.

## Repository Structure

```
OpenTransition/
├── mobile/                    # Mobile Android app (phones & tablets)
│   ├── src/main/
│   │   ├── java/com/shelbeely/opentransition/
│   │   │   ├── ui/           # UI components
│   │   │   ├── data/         # Data models
│   │   │   ├── domain/       # Business logic
│   │   │   ├── background/   # Background tasks
│   │   │   └── wear/         # Wear communication services
│   │   └── res/              # Resources
│   └── build.gradle          # Mobile build configuration
│
├── wear/                      # Wear OS app (smartwatches)
│   ├── src/main/
│   │   ├── java/com/shelbeely/opentransition/wear/
│   │   │   ├── MainActivity.kt               # Main Wear activity
│   │   │   └── WearableListenerService.kt    # Message receiver
│   │   └── res/              # Wear-specific resources
│   └── build.gradle          # Wear build configuration
│
├── shared/                    # Shared code & models
│   ├── src/main/java/com/shelbeely/opentransition/shared/
│   │   ├── WearableConstants.kt   # Communication constants
│   │   └── models/                # Shared data models
│   │       └── MilestoneData.kt   # Milestone sync model
│   └── build.gradle          # Shared library configuration
│
├── build.gradle              # Root build configuration
├── settings.gradle           # Module declarations
└── MONOREPO.md              # Monorepo guide
```

## Module Dependencies

```
┌─────────────┐
│   mobile    │─────┐
└─────────────┘     │
                    ├──> ┌─────────────┐
                    │    │   shared    │
┌─────────────┐     │    └─────────────┘
│    wear     │─────┘
└─────────────┘
```

Both `mobile` and `wear` modules depend on the `shared` module, which contains common code and data models.

## Communication Architecture

### Wearable Data Layer API

The apps communicate using Google's **Wearable Data Layer API**, which provides three main communication mechanisms:

#### 1. **Messages** (Instant, Ephemeral)
Used for one-time commands and triggers that don't need to persist.

**Example Flow: Photo Trigger from Watch**
```
┌──────────┐                                    ┌──────────┐
│   Wear   │                                    │  Mobile  │
│   App    │                                    │   App    │
└────┬─────┘                                    └─────┬────┘
     │                                                │
     │  1. User taps "Take Photo" button              │
     │                                                │
     │  2. Send message via Wearable Data Layer      │
     │    Path: /opentransition/trigger_photo        │
     │    Data: "face" (photo type)                  │
     │───────────────────────────────────────────────>│
     │                                                │
     │                                                │  3. MobileWearableListenerService
     │                                                │     receives message
     │                                                │
     │                                                │  4. Opens camera with
     │                                                │     specified photo type
     │                                                │
     │  5. Success confirmation (optional)            │
     │<───────────────────────────────────────────────│
     │                                                │
```

#### 2. **Data Items** (Synchronized, Persistent)
Used for data that should be kept in sync across devices.

**Example: Milestone Sync**
```
┌──────────┐                                    ┌──────────┐
│  Mobile  │                                    │   Wear   │
│   App    │                                    │   App    │
└────┬─────┘                                    └─────┬────┘
     │                                                │
     │  1. User creates/updates milestone            │
     │                                                │
     │  2. Serialize milestone data                  │
     │                                                │
     │  3. Put DataItem with path:                   │
     │     /opentransition/data/milestones           │
     │───────────────────────────────────────────────>│
     │                                                │
     │                                                │  4. WearableListenerService
     │                                                │     receives data change
     │                                                │
     │                                                │  5. Update local cache
     │                                                │     and refresh UI
     │                                                │
```

#### 3. **Capabilities** (Device Detection)
Used to detect when companion apps are installed and reachable.

**Capabilities Registered:**
- `opentransition_mobile_app` - Registered by mobile app
- `opentransition_wear_app` - Registered by wear app

## Communication Paths

### From Wear to Mobile

| Path | Type | Purpose | Data |
|------|------|---------|------|
| `/opentransition/trigger_photo` | Message | Trigger photo capture | Photo type (face/body) |
| `/opentransition/request_sync` | Message | Request data sync | None |

### From Mobile to Wear

| Path | Type | Purpose | Data |
|------|------|---------|------|
| `/opentransition/data/milestones` | DataItem | Sync milestones | Serialized milestone list |
| `/opentransition/milestone_update` | Message | Notify of change | Milestone ID |
| `/opentransition/data/settings` | DataItem | Sync settings | Serialized settings |

## Data Models

### MilestoneData (Shared)

A simplified version of the full Milestone model for efficient sync:

```kotlin
@Parcelize
data class MilestoneData(
    val id: String,
    val title: String,
    val description: String?,
    val date: Long,  // Timestamp in milliseconds
    val type: String
) : Parcelable
```

This model is defined in the `shared` module and used by both apps.

## Services

### MobileWearableListenerService (Mobile)

Extends `WearableListenerService` to handle incoming messages from Wear:

**Responsibilities:**
- Receive photo trigger messages from watch
- Receive sync requests
- Process and route messages to appropriate handlers

**Implementation Location:** `mobile/src/main/java/com/shelbeely/opentransition/wear/`

### WearableListenerService (Wear)

Extends `WearableListenerService` to handle incoming data from Mobile:

**Responsibilities:**
- Receive milestone data updates
- Receive settings updates
- Update local UI when data changes
- Handle capability changes (connection/disconnection)

**Implementation Location:** `wear/src/main/java/com/shelbeely/opentransition/wear/`

## Build Configuration

### Shared Dependencies

Common dependencies shared across modules:

```gradle
// Wearable Data Layer
implementation 'com.google.android.gms:play-services-wearable:18.1.0'

// Gson for serialization
implementation 'com.google.code.gson:gson:2.10.1'
```

### Mobile-Specific

```gradle
// Standard Android dependencies
implementation 'androidx.appcompat:appcompat:1.6.1'
implementation 'com.google.android.material:material:1.13.0'

// Firebase (for existing features)
implementation platform('com.google.firebase:firebase-bom:32.3.1')

// Shared module
implementation project(':shared')
```

### Wear-Specific

```gradle
// Wear OS libraries
implementation 'androidx.wear:wear:1.3.0'
implementation 'com.google.android.support:wearable:2.9.0'
compileOnly 'com.google.android.wearable:wearable:2.9.0'

// Shared module
implementation project(':shared')
```

## Testing Communication

### Prerequisites
1. Physical Wear OS device paired with phone (emulators have limited Wearable Data Layer support)
2. Both apps installed on their respective devices
3. Devices connected via Bluetooth

### Testing Steps

1. **Install both apps:**
   ```bash
   ./gradlew :mobile:installDebug
   ./gradlew :wear:installDebug
   ```

2. **Verify connection:**
   - Open Wear app
   - Check connection status (should show "Connected")

3. **Test photo trigger:**
   - On watch, tap "Take Photo" button
   - Mobile app should open camera

4. **Test data sync:**
   - Create a milestone on mobile
   - Verify it appears on watch (when implemented)

## Future Enhancements

### Planned Features
- [ ] Full milestone viewing on Wear
- [ ] Quick voice notes from watch
- [ ] Notification mirroring
- [ ] Photo preview on watch after capture
- [ ] Daily reminder notifications
- [ ] Quick stats display on watch face (complications)

### Technical Improvements
- [ ] Implement proper data synchronization with conflict resolution
- [ ] Add offline support with queue-based message sending
- [ ] Implement battery-efficient background sync
- [ ] Add data compression for large payloads
- [ ] Implement incremental sync for large datasets

## Security Considerations

### Data in Transit
- Wearable Data Layer uses Google Play Services secure transport
- Data is encrypted in transit between devices
- No additional encryption needed for basic use

### Sensitive Data
- Photo data is NOT sent to watch (only metadata)
- Personal information should be filtered before sync
- Consider implementing additional encryption for highly sensitive data

## Troubleshooting

### Common Issues

**"Phone disconnected" on Wear app:**
- Ensure Bluetooth is enabled
- Check devices are paired in Wear OS app on phone
- Verify both apps are installed
- Restart both apps

**Messages not received:**
- Check AndroidManifest service declarations
- Verify intent filters are correct
- Check Logcat for errors
- Ensure Google Play Services is up to date

**Build failures:**
- Run `./gradlew clean`
- Check all modules have correct dependencies
- Verify `settings.gradle` includes all modules
- Check for conflicting library versions

## Resources

- [Wearable Data Layer API Guide](https://developer.android.com/training/wearables/data/data-layer)
- [Wear OS Development](https://developer.android.com/training/wearables)
- [Google Play Services](https://developers.google.com/android/guides/overview)
