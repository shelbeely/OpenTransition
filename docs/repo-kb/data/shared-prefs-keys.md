# SharedPreferences Keys

> Default-prefs keys defined by the `SettingsManager.Key` enum at
> [`SettingsManager.kt:485-505`](../../../mobile/src/main/java/com/shelbeely/opentransition/util/settings/SettingsManager.kt),
> plus extra ad-hoc keys in [`PrefUtil.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/util/settings/PrefUtil.kt).
> All access goes through [`PrefUtil`](../../../mobile/src/main/java/com/shelbeely/opentransition/util/settings/PrefUtil.kt).

## Default prefs (`PreferenceManager.getDefaultSharedPreferences`)

| Key | Type | Default | Owner feature | Firestore-mirrored? | Source |
|---|---|---|---|---|---|
| `currentAndroidVersion` | Int? | `null` | App boot version migration | ❌ (excluded) | [`SettingsManager.kt:389-391`](../../../mobile/src/main/java/com/shelbeely/opentransition/util/settings/SettingsManager.kt) |
| `incorrectPasswordCount` | Int? | `null` | [App lock](../features/app-lock.md) | ❌ (excluded) | same |
| `lockCode` | String (hashed) | `""` | [App lock](../features/app-lock.md) | ✅ | [`SettingsManager.kt:375`](../../../mobile/src/main/java/com/shelbeely/opentransition/util/settings/SettingsManager.kt) |
| `lockDelay` | enum `LockDelay` | `instant` | [App lock](../features/app-lock.md) | ✅ | [`SettingsManager.kt:376`](../../../mobile/src/main/java/com/shelbeely/opentransition/util/settings/SettingsManager.kt) |
| `lockType` | enum `LockType` | `off` | [App lock](../features/app-lock.md) / [Disguised mode](../features/disguised-mode.md) | ✅ | [`SettingsManager.kt:377`](../../../mobile/src/main/java/com/shelbeely/opentransition/util/settings/SettingsManager.kt) |
| `saveToFirebase` | Bool | `false` | [Settings](../features/settings.md) | ❌ (excluded) | [`SettingsManager.kt:393`](../../../mobile/src/main/java/com/shelbeely/opentransition/util/settings/SettingsManager.kt) |
| `showAccountWarning` | Bool | `false` | [Settings](../features/settings.md) | ❌ (excluded) | [`SettingsManager.kt:115`](../../../mobile/src/main/java/com/shelbeely/opentransition/util/settings/SettingsManager.kt) |
| `showAds` | Bool | `false` | [Settings](../features/settings.md) | ✅ | [`SettingsManager.kt:127`](../../../mobile/src/main/java/com/shelbeely/opentransition/util/settings/SettingsManager.kt) |
| `showWelcome` | Bool | `true` | First-launch flow | ✅ | [`SettingsManager.kt:141`](../../../mobile/src/main/java/com/shelbeely/opentransition/util/settings/SettingsManager.kt) |
| `startDate` | Long (epochDay) | `LocalDate.now()` on first read | [Home](../features/photo-gallery-and-viewer.md) | ✅ | [`SettingsManager.kt:153-173`](../../../mobile/src/main/java/com/shelbeely/opentransition/util/settings/SettingsManager.kt) |
| `theme` | enum `Theme` | `pink` | Appearance | ✅ | [`SettingsManager.kt:177`](../../../mobile/src/main/java/com/shelbeely/opentransition/util/settings/SettingsManager.kt) |
| `userLastSeen` | Long | now() | [Settings](../features/settings.md) | ❌ (excluded) | [`SettingsManager.kt:226-228`](../../../mobile/src/main/java/com/shelbeely/opentransition/util/settings/SettingsManager.kt) |
| `enableAnalytics` | Bool | `true` | [Settings](../features/settings.md) | ✅ | [`SettingsManager.kt:190`](../../../mobile/src/main/java/com/shelbeely/opentransition/util/settings/SettingsManager.kt) |
| `enableCrashReports` | Bool | `true` | [Settings](../features/settings.md) | ✅ | [`SettingsManager.kt:208`](../../../mobile/src/main/java/com/shelbeely/opentransition/util/settings/SettingsManager.kt) |
| `encryptedDatabaseEnabled` | Bool | `false` | [Encrypted DB](../features/encrypted-database.md) — **toggle hidden, read returns hard-coded false** | ✅ | [`SettingsManager.kt:408, 384`](../../../mobile/src/main/java/com/shelbeely/opentransition/util/settings/SettingsManager.kt) |
| `decoyVaultEnabled` | Bool | `false` | [Decoy vault](../features/decoy-vault.md) | ✅ | [`SettingsManager.kt:423`](../../../mobile/src/main/java/com/shelbeely/opentransition/util/settings/SettingsManager.kt) |
| `decoyLockCode` | String (hashed) | `""` | [Decoy vault](../features/decoy-vault.md) | ❌ (excluded) | [`SettingsManager.kt:434, 390`](../../../mobile/src/main/java/com/shelbeely/opentransition/util/settings/SettingsManager.kt) |
| `quickHideEnabled` | Bool | `false` | [Quick hide](../features/settings.md) (`FLAG_SECURE`) | ✅ | [`SettingsManager.kt:445`](../../../mobile/src/main/java/com/shelbeely/opentransition/util/settings/SettingsManager.kt) |
| `colorVariant` | enum `AppColorVariant` | `dynamic` | Appearance / Material You | ✅ | [`SettingsManager.kt:458`](../../../mobile/src/main/java/com/shelbeely/opentransition/util/settings/SettingsManager.kt) |
| `selectPhotoFirstVisible` | String | `""` (cleared at every app start) | Photo picker scroll restore | ❌ | [`PrefUtil.kt:88-94`](../../../mobile/src/main/java/com/shelbeely/opentransition/util/settings/PrefUtil.kt), [`OpenTransitionApp.kt:93`](../../../mobile/src/main/java/com/shelbeely/opentransition/OpenTransitionApp.kt) |

## Per-album prefs (`Context.MODE_PRIVATE`, file `albumFirstVisible`)

| Key pattern | Value | Owner | Notes |
|---|---|---|---|
| `<bucketId>` | image URI string | Photo picker | Cleared on every app start ([`OpenTransitionApp.kt:94`](../../../mobile/src/main/java/com/shelbeely/opentransition/OpenTransitionApp.kt)) |

## EncryptedSharedPreferences (`opentransition_db_keys`)

Stored in [`KeystoreManager.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/database/KeystoreManager.kt):

| Key | Value | Owner |
|---|---|---|
| `real_db_passphrase` | 64-char hex (32 random bytes) | [Encrypted DB](../features/encrypted-database.md) — reserved; unused while feature is off |
| `decoy_db_passphrase` | 64-char hex | Same |

## Firestore-mirror policy

The mirror is one-way write-through from local prefs to Firestore document `users/{uid}` (see [`apis/firebase-contracts.md`](../apis/firebase-contracts.md)) plus a conflict-resolution path at `SettingsManager.attemptFirebaseAutoSetup` ([`SettingsManager.kt:303-363`](../../../mobile/src/main/java/com/shelbeely/opentransition/util/settings/SettingsManager.kt)).

Excluded keys (never written): `saveToFirebase`, `showAccountWarning`, `userLastSeen`, `currentAndroidVersion`, `incorrectPasswordCount`, `decoyLockCode`.
