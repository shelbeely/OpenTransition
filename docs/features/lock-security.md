# Lock & Security

Developer documentation for lock and security features.

## Overview

OpenTransition provides multiple layers of security to protect sensitive transition data:

1. **App lock** — PIN, pattern, or biometric authentication required to open the app
2. **Disguised icon (Train Mode)** — App icon changes to look like a train/railway app
3. **Optional database encryption** — SQLCipher AES-256 encryption for data at rest
4. **Decoy vault** — Separate encrypted database accessible with a different passcode
5. **Quick hide** — Instantly blanks app content and optionally jumps to home screen

## App Lock

### Lock Types

| `LockType` | Description |
|------------|-------------|
| `OFF` | No lock — app opens immediately |
| `NORMAL` | Real app icon + PIN/pattern lock |
| `TRAINS` | Train icon (disguised) + PIN/pattern lock |

Biometric authentication is available as an additional option on top of `NORMAL` or `TRAINS` when the device supports it.

### Biometric Authentication

Uses Android's `BiometricPrompt` API with `BIOMETRIC_STRONG` security level:

- Supports fingerprint, face recognition, and iris scanning
- Requires a backup passcode for recovery when biometric fails
- Falls back to passcode automatically after too many biometric failures
- Implementation class: `BiometricAuthManager`

```kotlin
val biometricPrompt = BiometricPrompt(activity, executor, callback)
val promptInfo = BiometricPrompt.PromptInfo.Builder()
    .setTitle("Unlock OpenTransition")
    .setAllowedAuthenticators(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
    .build()
biometricPrompt.authenticate(promptInfo)
```

### PIN/Pattern Hashing

Passcodes are never stored in plaintext. They are hashed with a random salt:

```kotlin
val hashedCode = EncryptionUtil.encryptAndEncode(userInput, PrefUtil.CODE_SALT)
```

`CODE_SALT` is injected as a `BuildConfig` field from `secrets.properties` at build time, ensuring each installation uses a unique salt.

## Encrypted Database

See [ENCRYPTED_DATABASE.md](https://github.com/shelbeely/OpenTransition/blob/main/ENCRYPTED_DATABASE.md) for full details.

### Summary

- **Room + SQLCipher**: Full database encryption at rest using AES-256 GCM
- **Android Keystore**: Encryption keys stored in hardware-backed keystore when available
- **Optional**: Can be disabled; Room works without encryption for lower-end devices

### Enabling Encryption

```kotlin
SettingsManager.setEncryptedDatabaseEnabled(true, context)
val db = DatabaseManager.getDatabase(context) // Returns encrypted DB
```

## Decoy Vault

When the decoy vault is enabled, two separate encrypted databases exist:

- **Real vault** — opened with the main passcode, contains actual data
- **Decoy vault** — opened with the decoy passcode, empty or containing generic content

Authentication flow:
1. User enters passcode
2. If it matches main passcode → real vault opens
3. If it matches decoy passcode → decoy vault opens
4. Otherwise → error

This protects against coercion scenarios where someone is forced to unlock the app.

### Configuring Decoy Vault

```kotlin
SettingsManager.setDecoyVaultEnabled(true, context)
SettingsManager.setDecoyLockCode(encryptedDecoyCode, activity)
```

## Quick Hide

Quickly blanks app content when needed:

```kotlin
QuickHideManager.enableSecureMode(activity)  // Sets FLAG_SECURE
QuickHideManager.applyQuickHide(activity)    // Jumps to home screen
```

`FLAG_SECURE` also prevents screenshots and screen recording when enabled.

## Key Files

- `mobile/src/main/java/com/shelbeely/opentransition/database/AppDatabase.kt` — Room + SQLCipher
- `mobile/src/main/java/com/shelbeely/opentransition/database/DatabaseManager.kt` — Real/decoy vault switching
- `mobile/src/main/java/com/shelbeely/opentransition/database/KeystoreManager.kt` — Keystore operations
- `mobile/src/main/java/com/shelbeely/opentransition/util/settings/SettingsManager.kt` — Security settings
- `mobile/src/main/java/com/shelbeely/opentransition/ui/lock/` — Lock screen UI

## Related

- [Architecture Overview](../architecture/overview.md) — Security architecture section
- [ENCRYPTED_DATABASE.md](https://github.com/shelbeely/OpenTransition/blob/main/ENCRYPTED_DATABASE.md) — Full encrypted database documentation
