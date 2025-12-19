# Encrypted Database Features

OpenTransition now includes advanced security features for protecting sensitive data:

## Features

### 1. **Encrypted Database (Room + SQLCipher)**
- **Full database encryption at rest** using SQLCipher
- All sensitive data (photos, milestones, audio analyses) are encrypted on disk
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
│  (SQLCipher)     │  │  (SQLCipher)     │
│                  │  │                  │
│  - Milestones    │  │  - Empty or      │
│  - Photos        │  │    Generic Data  │
│  - Audio         │  │                  │
└──────────────────┘  └──────────────────┘
```

### Key Components

1. **EncryptedDatabase.kt**: Room database with SQLCipher integration
2. **KeystoreManager.kt**: Manages encryption keys in Android Keystore
3. **DatabaseManager.kt**: Handles switching between real and decoy vaults
4. **QuickHideManager.kt**: Implements quick hide functionality

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

## Migration from Realm

The encrypted database infrastructure is **optional** and can coexist with the existing Realm database. To migrate:

1. Enable encrypted database in settings
2. Copy data from Realm to Room (migration utility needed)
3. Switch app to use Room DAOs instead of Realm queries
4. Optionally remove Realm dependency

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
// Room + SQLCipher
def room_version = "2.6.1"
implementation "androidx.room:room-runtime:$room_version"
implementation "androidx.room:room-ktx:$room_version"
ksp "androidx.room:room-compiler:$room_version"

// SQLCipher for database encryption
implementation "net.zetetic:android-database-sqlcipher:4.5.4"
implementation "androidx.sqlite:sqlite:2.4.0"

// Security library
implementation 'androidx.security:security-crypto:1.1.0-alpha06'
```

## Future Enhancements

- [ ] Migration utility from Realm to Room
- [ ] Biometric-gated key access (require biometric for every DB access)
- [ ] Gesture-based quick hide triggers
- [ ] Custom decoy vault content
- [ ] Database backup/restore with encryption
- [ ] Key rotation mechanism
- [ ] Multi-user support with separate vaults
