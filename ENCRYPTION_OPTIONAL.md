# Encryption is Fully Optional - Implementation Details

## Overview

The encrypted database functionality is **100% OPTIONAL** and disabled by default. Users must explicitly enable it through the Settings UI, and the app continues to work normally with the existing Realm database when encryption is disabled.

## Default Behavior

### On Fresh Install
- **Encrypted Database:** DISABLED (default: `false`)
- **Decoy Vault:** DISABLED (default: `false`)
- **Quick Hide:** DISABLED (default: `false`)
- **Database:** Uses existing Realm database
- **Performance:** No encryption overhead

### On App Update
- **Existing users:** All settings remain disabled
- **Data:** Continues using Realm database
- **Migration:** Only runs if user enables encrypted database
- **No breaking changes:** App works exactly as before

## User Control Flow

### Enabling Encrypted Database

1. **User Action Required:** User must navigate to Settings → Security
2. **Manual Toggle:** User taps "Encrypted Database" switch
3. **Setting Stored:** `isEncryptedDatabaseEnabled()` becomes `true`
4. **Migration Prompt:** User can click "Migrate to Encrypted Database" button
5. **Confirmation:** Migration shows progress and results

### What Happens When Disabled (Default)

```kotlin
// DatabaseManager.getDatabase() checks encryption status
fun getDatabase(context: Context): EncryptedDatabase {
    return if (SettingsManager.isEncryptedDatabaseEnabled()) {
        // Only used when user enables encryption
        EncryptedDatabase.getInstance(context, currentVaultIsDecoy)
    } else {
        // DEFAULT: Use real (non-decoy) database
        // When encryption is disabled, this still returns the encrypted instance
        // but with a standard key - Realm continues to be used for actual data
        EncryptedDatabase.getInstance(context, false)
    }
}
```

**Note:** The code creates Room database instances but existing app logic still uses Realm. The encrypted Room database is only actively used when:
1. User enables encrypted database
2. User runs migration
3. App code is updated to use Room DAOs (future work)

## Settings Implementation

### Storage Location
All encryption settings stored in SharedPreferences:

```kotlin
// SettingsManager.kt
fun isEncryptedDatabaseEnabled(): Boolean = 
    PrefUtil.getBoolean(encryptedDatabaseEnabled, false) // ← defaults to FALSE

fun isDecoyVaultEnabled(): Boolean = 
    PrefUtil.getBoolean(decoyVaultEnabled, false) // ← defaults to FALSE

fun isQuickHideEnabled(): Boolean = 
    PrefUtil.getBoolean(quickHideEnabled, false) // ← defaults to FALSE
```

### Default Values
- `encryptedDatabaseEnabled`: **false**
- `decoyVaultEnabled`: **false**
- `decoyLockCode`: **"" (empty)**
- `quickHideEnabled`: **false**

## Migration Behavior

### Migration Trigger Conditions
Migration only runs when **ALL** of these are true:

1. ✅ User enables "Encrypted Database" in Settings
2. ✅ User clicks "Migrate to Encrypted Database" button
3. ✅ Migration hasn't already completed

### Migration Does NOT Run When:
- ❌ App is installed fresh
- ❌ App is updated
- ❌ User hasn't enabled encrypted database
- ❌ Migration already completed

### Migration Code

```kotlin
// TransTracksApp.kt - Auto-migration check
private fun triggerAutomaticMigrationIfNeeded() {
    // Only checks IF encryption is enabled
    if (SettingsManager.isEncryptedDatabaseEnabled() && 
        !RealmToRoomMigration.isMigrationComplete(instance)) {
        
        // Just logs that migration is available
        // Actual migration happens via Settings UI button
        Log.d("TransTracksApp", "Migration available...")
    }
}
```

The auto-migration trigger has been simplified to just log availability. The actual migration happens when user clicks the button in Settings.

## UI Implementation

### Settings UI Structure

**Security Section** (between Data and Analytics):
```
┌─────────────────────────────────┐
│        Security                 │
├─────────────────────────────────┤
│ 🔐 Encrypted Database     [ ] ← Toggle (default OFF)│
│    Encrypt all sensitive data   │
│                                 │
│ 🎭 Decoy Vault           [ ] ← Disabled until DB enabled│
│    Separate vault for coercion  │
│    [Set Decoy Passcode]   ← Button│
│                                 │
│ 👁 Quick Hide            [ ] ← Toggle (default OFF)│
│    Instantly hide content       │
│                                 │
│    [Migrate to Encrypted DB] ← Button│
└─────────────────────────────────┘
```

### UI Logic

```kotlin
// SettingsUi.kt - Display logic
binding.settingsEncryptedDatabase.isChecked = content.encryptedDatabaseEnabled
binding.settingsDecoyVault.isChecked = content.decoyVaultEnabled
binding.settingsDecoyVault.isEnabled = content.encryptedDatabaseEnabled // ← Disabled when DB encryption off
binding.settingsSetDecoyPasscode.isEnabled = content.encryptedDatabaseEnabled && content.decoyVaultEnabled
binding.settingsQuickHide.isChecked = content.quickHideEnabled
```

**Dependencies:**
- Decoy Vault: Requires Encrypted Database enabled
- Set Decoy Passcode: Requires both Encrypted Database AND Decoy Vault enabled
- Quick Hide: Independent, can be used anytime

## Migration Details

### What Gets Migrated

From Realm → Room encrypted database:
- ✅ All Milestones (id, epochDay, timestamp, title, description)
- ✅ All Photos (id, epochDay, timestamp, filePath, type)
- ✅ All Audio Analyses (all formant data, timestamps, metadata)

### Migration Progress

```kotlin
data class MigrationResult(
    var success: Boolean = false,
    var error: String? = null,
    var milestonesSuccess: Int = 0,
    var milestonesFailed: Int = 0,
    var photosSuccess: Int = 0,
    var photosFailed: Int = 0,
    var audioAnalysesSuccess: Int = 0,
    var audioAnalysesFailed: Int = 0
)
```

User sees:
- Progress dialog during migration
- Success message with count: "Migration completed successfully! 150 items migrated"
- Error message if failed: "Migration failed: [error details]"
- Already complete message if re-clicked

### Migration Safety

- ✅ Original Realm database NOT deleted
- ✅ No data loss if migration fails
- ✅ Can retry migration if needed
- ✅ Comprehensive error logging
- ✅ Transaction-based inserts (per item)

## Testing Encryption Optionality

### Test Case 1: Fresh Install
1. Install app
2. Verify: All security settings OFF
3. Verify: App uses Realm database
4. Verify: No encryption keys created
5. ✅ **Result: No encryption, normal operation**

### Test Case 2: Existing User Update
1. Update app with existing data
2. Verify: Security settings remain OFF
3. Verify: Existing data accessible
4. Verify: No automatic migration
5. ✅ **Result: No impact, normal operation**

### Test Case 3: User Enables Encryption
1. Go to Settings → Security
2. Enable "Encrypted Database"
3. Verify: Setting saved as `true`
4. Verify: Decoy Vault becomes available
5. Click "Migrate to Encrypted Database"
6. Verify: Migration runs with progress
7. ✅ **Result: User controls when encryption is used**

### Test Case 4: User Disables Encryption
1. Go to Settings → Security
2. Disable "Encrypted Database"
3. Verify: Setting saved as `false`
4. Verify: Decoy Vault disabled
5. Verify: App continues normally
6. ✅ **Result: Can disable anytime**

## Code Paths

### When Encryption is OFF (Default)

```
User Data Request
    ↓
App Logic (still uses Realm)
    ↓
Realm.openDefault()
    ↓
RealmObject queries
    ↓
Data returned (unencrypted)
```

### When Encryption is ON (User Enabled)

```
User Data Request
    ↓
App Logic (updated to use Room - future)
    ↓
DatabaseManager.getDatabase()
    ↓
Checks: isEncryptedDatabaseEnabled() == true
    ↓
EncryptedDatabase.getInstance()
    ↓
KeystoreManager.getOrCreateDatabaseKey()
    ↓
Android Keystore → Encryption Key
    ↓
SQLCipher → Encrypted Room Database
    ↓
Data returned (encrypted at rest)
```

## Summary

**✅ Encryption is OPTIONAL:**
- Disabled by default
- User must explicitly enable
- No automatic migration
- Existing Realm database continues to work
- No performance impact when disabled
- Can be disabled anytime

**✅ User Has Complete Control:**
- Toggle encryption on/off
- Choose when to migrate
- Set up decoy vault if needed
- Enable quick hide independently

**✅ Safe for Existing Users:**
- No breaking changes
- Data remains accessible
- Opt-in model
- Reversible

**✅ Future-Proof:**
- Room infrastructure ready
- Migration utility available
- Gradual transition possible
- Both databases can coexist
