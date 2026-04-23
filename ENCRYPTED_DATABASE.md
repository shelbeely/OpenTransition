# Encrypted Database Features

> **⚠️ Status: In development.** The end-to-end "Encrypt my database" flow is **not currently
> shipped to users**. The user-facing toggle in Settings is hidden as of this release because the
> Realm → Room migration is still in progress: enabling the toggle today would encrypt a Room
> database that holds essentially nothing while the bulk of user-visible data (photos, milestones,
> audio analyses) continues to live in the unencrypted Realm. Re-enabling this surface requires
> finishing the migration described in `audit-report/07-issues-and-bugs.md` ISSUE-004.
>
> This document describes the **target architecture** once that migration is complete.

OpenTransition includes the following advanced security features for protecting sensitive data:

## Features

### 1. **Encrypted Database (Room + SQLCipher)** — *Not yet user-enabled*
- **Target:** Full database encryption at rest using SQLCipher
- All sensitive data (photos, milestones, audio analyses) will be encrypted on disk once the
  Realm → Room migration is complete
- Encryption keys stored securely in **Android Keystore**
- Hardware-backed security when available
- Zero plaintext data exposure

### 2. **Decoy Vault (Two-Passcode Model)**
- Create a separate, harmless vault accessible with a different passcode
- **Passcode A**: Opens the decoy vault (empty or with generic content)
- **Passcode B**: Opens the real vault with actual data
- Powerful protection against coercion scenarios
- Each vault has its own encrypted database

### 3. **Quick Hide UX**
- Instantly hide app content when needed
- Prevents screenshots and screen recording when enabled
- Quick exit to home screen
- Can be triggered manually or via gestures (extensible)

### 4. **Enhanced Biometric Support**
- Supports **fingerprint, face recognition, and iris scanning**
- Uses Android's `BIOMETRIC_STRONG` authentication
- Compatible with all modern biometric sensors
- Fallback to passcode authentication

## Implementation Details

### Database Architecture

OpenTransition uses **Room database** with optional SQLCipher encryption. This is a modern replacement for the Realm database used in TransTracks.

```
┌─────────────────────────────────────┐
│     Android Keystore (Secure)       │
│  ┌──────────────┐  ┌──────────────┐ │
│  │  Real DB Key │  │ Decoy DB Key │ │
│  └──────────────┘  └──────────────┘ │
└─────────────────────────────────────┘
           │                 │
           ▼                 ▼
┌──────────────────┐  ┌──────────────────┐
│  Real Database   │  │  Decoy Database  │
│  (Room/SQLite)   │  │  (Room/SQLite)   │
│  Optional:       │  │  Optional:       │
│  + SQLCipher     │  │  + SQLCipher     │
│                  │  │                  │
│  - Milestones    │  │  - Empty or      │
│  - Photos        │  │    Generic Data  │
│  - Audio         │  │                  │
└──────────────────┘  └──────────────────┘
```

**Backwards Compatibility**: OpenTransition maintains Realm data models and utilities to import backups from TransTracks. See the "Importing from TransTracks" section below.

### Key Components

1. **AppDatabase.kt**: Room database with optional SQLCipher encryption
2. **KeystoreManager.kt**: Manages encryption keys in EncryptedSharedPreferences
3. **DatabaseManager.kt**: Handles switching between real and decoy vaults
4. **QuickHideManager.kt**: Implements quick hide functionality
5. **RealmBackupImporter.kt**: Imports TransTracks backups (backwards compatibility)

### Security Model

- **Encryption**: AES-256 GCM via SQLCipher
- **Key Storage**: Android Keystore System (hardware-backed when available)
- **Key Derivation**: Separate keys for real and decoy databases
- **Authentication**: Biometric (STRONG) or passcode
- **Screen Security**: FLAG_SECURE when quick hide is enabled

## Usage

### Enable Encrypted Database

```kotlin
// In settings or during setup
SettingsManager.setEncryptedDatabaseEnabled(true, context)

// Get encrypted database instance
val db = DatabaseManager.getDatabase(context)
val milestones = db.milestoneDao().getAllMilestones()
```

### Configure Decoy Vault

```kotlin
// Enable decoy vault
SettingsManager.setDecoyVaultEnabled(true, context)

// Set decoy passcode (encrypted)
val encryptedDecoyCode = EncryptionUtil.encryptAndEncode(
    decoyPasscode, 
    PrefUtil.CODE_SALT
)
SettingsManager.setDecoyLockCode(encryptedDecoyCode, activity)
```

### Authentication Flow

When user enters passcode:
1. Check if it matches real passcode → Open real vault
2. Check if it matches decoy passcode → Open decoy vault
3. Otherwise → Show error

### Quick Hide

```kotlin
// Enable quick hide
SettingsManager.setQuickHideEnabled(true, context)

// Apply secure mode
QuickHideManager.enableSecureMode(activity)

// Trigger quick hide
QuickHideManager.applyQuickHide(activity)
```

## Importing from TransTracks

OpenTransition supports importing backups from TransTracks. The app maintains backwards compatibility with Realm database format.

### How to Import

1. **In TransTracks**: Export your data (Settings → Export)
2. **Save the backup file**: A `.ttbackup` file will be created and offered via the Android share sheet
3. **In OpenTransition**: 
   - Go to Settings
   - Tap "Import Backup"
   - Select your `.ttbackup` backup file
   - Wait for the import to complete

### What Gets Imported

✅ All milestones  
✅ All photos (face, body, audio)  
✅ Audio analysis data  
✅ Timestamps and metadata  

### Technical Details

- **Realm → Room conversion**: Automatic conversion during import
- **No data loss**: All fields are mapped correctly
- **One-time process**: After import, data lives in Room database
- **Backwards compatibility code**: All Realm code is marked with `BACKWARDS COMPATIBILITY` comments
- **Source files**:
  - `RealmBackupImporter.kt` - Imports `.realm` backup files
  - `RealmToRoomMigration.kt` - Migrates existing Realm database on device
  - Data models (`Milestone.kt`, `Photo.kt`, `AudioAnalysis.kt`) - Retained for import only

### Optional: Enable Encryption After Import

After importing your data, you can optionally enable encryption:

```kotlin
// Enable encrypted database
SettingsManager.setEncryptedDatabaseEnabled(true, context)

// Optionally enable decoy vault
SettingsManager.setDecoyVaultEnabled(true, context)
```

Encryption is **optional** - the Room database works fine without it.

## Security Considerations

### What's Protected
✅ Database files encrypted at rest  
✅ Encryption keys in hardware-backed keystore  
✅ Screenshot/screen recording prevention  
✅ Decoy vault for coercion scenarios  
✅ Biometric authentication

### What's NOT Protected
❌ Data in memory while app is running  
❌ Root/jailbroken devices (can bypass some protections)  
❌ Physical device access with debugging enabled  
❌ Photo/audio files on disk (separate from database)

### Best Practices
1. Always use strong, unique passcodes
2. Enable device encryption
3. Keep device OS updated
4. Use biometric authentication when available
5. Set up decoy vault for high-risk scenarios

## Configuration Options

All settings are stored in `SettingsManager`:

```kotlin
// Encrypted database
SettingsManager.isEncryptedDatabaseEnabled(): Boolean
SettingsManager.setEncryptedDatabaseEnabled(enabled: Boolean, context: Context?)

// Decoy vault
SettingsManager.isDecoyVaultEnabled(): Boolean
SettingsManager.setDecoyVaultEnabled(enabled: Boolean, context: Context?)
SettingsManager.getDecoyLockCode(): String
SettingsManager.setDecoyLockCode(code: String, activity: Activity)

// Quick hide
SettingsManager.isQuickHideEnabled(): Boolean
SettingsManager.setQuickHideEnabled(enabled: Boolean, context: Context?)
```

## Testing

To test the encrypted database:

1. Enable in settings
2. Create test data (milestones, photos)
3. Close and reopen app
4. Verify data persists
5. Check database file is encrypted (not readable as plaintext)

To test decoy vault:

1. Enable decoy vault
2. Set decoy passcode (different from main)
3. Lock the app
4. Enter decoy passcode → Should open empty vault
5. Lock again, enter real passcode → Should open real vault

## Dependencies

Added to `mobile/build.gradle`:

```gradle
// Room database (replaces Realm)
def room_version = "2.6.1"
implementation "androidx.room:room-runtime:$room_version"
implementation "androidx.room:room-ktx:$room_version"
ksp "androidx.room:room-compiler:$room_version"

// SQLCipher for optional database encryption
implementation "net.zetetic:android-database-sqlcipher:4.5.4"
implementation "androidx.sqlite:sqlite:2.4.0"

// Security library for key management
implementation 'androidx.security:security-crypto:1.1.0-alpha06'

// Realm (backwards compatibility only - for importing TransTracks backups)
id 'io.realm.kotlin'
// Realm data models maintained in com.shelbeely.opentransition.data.*
```

## Future Enhancements

- [x] ~~Migration utility from Realm to Room~~ ✅ Implemented as RealmBackupImporter
- [ ] Biometric-gated key access (require biometric for every DB access)
- [ ] Gesture-based quick hide triggers
- [ ] Custom decoy vault content
- [ ] Database backup/restore with encryption
- [ ] Key rotation mechanism
- [ ] Multi-user support with separate vaults
