# Feature: App Lock

## Summary

Optional code/biometric lock at app launch. Lock type is one of `off`, `normal`, `trains`, `biometric` ([`SettingsManager.kt:539-553`](../../../mobile/src/main/java/com/shelbeely/opentransition/util/settings/SettingsManager.kt)). Lock delay between `instant` and `fifteenMinutes` ([`SettingsManager.kt:509-536`](../../../mobile/src/main/java/com/shelbeely/opentransition/util/settings/SettingsManager.kt)).

## Entry points

| Surface | Component | Path |
|---|---|---|
| Lock screen | `LockFragment` + `LockScreen` | [`ui/lock/`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/lock/) |
| Toggle in Settings | `SettingsFragment` | [`ui/settings/SettingsFragment.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/settings/SettingsFragment.kt) |

## Code hashing

User-entered code → `EncryptionUtil.encryptAndEncode(passcode, PrefUtil.CODE_SALT)` ([`util/EncryptionUtil.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/util/EncryptionUtil.kt), [`DatabaseManager.kt:67-70`](../../../mobile/src/main/java/com/shelbeely/opentransition/database/DatabaseManager.kt)). `CODE_SALT` is injected via `BuildConfig.CODE_SALT` from `secrets.properties`.

## Biometric path

`BiometricPromptHelper` uses `androidx.biometric.BiometricPrompt` with `Authenticators.BIOMETRIC_STRONG` ([`util/BiometricPromptHelper.kt:27-28`](../../../mobile/src/main/java/com/shelbeely/opentransition/util/BiometricPromptHelper.kt)). `LockFragment` guards against double-show via a `biometricPromptShown` flag and lifecycle re-arming ([`LockFragment.kt:41-79`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/lock/LockFragment.kt)).

## Persistence touched

| Pref key (in `SettingsManager.Key`) | Type | Purpose |
|---|---|---|
| `lockCode` | String (hashed) | Real lock code |
| `lockDelay` | enum `LockDelay` | When to re-prompt |
| `lockType` | enum `LockType` | Off / normal / trains / biometric |
| `incorrectPasswordCount` | Int | Telemetry for repeated failures |
| `decoyLockCode` | String (hashed) | See [`decoy-vault.md`](./decoy-vault.md) |

All under `PreferenceManager.getDefaultSharedPreferences` ([`util/settings/PrefUtil.kt:27-28`](../../../mobile/src/main/java/com/shelbeely/opentransition/util/settings/PrefUtil.kt)).

## External APIs

- **`androidx.biometric`** for `BiometricPrompt`.
- **Firebase Firestore** when `saveToFirebase = true`: `lockCode`, `lockDelay`, `lockType` are mirrored ([`SettingsManager.kt:374-388`](../../../mobile/src/main/java/com/shelbeely/opentransition/util/settings/SettingsManager.kt)). See [`apis/firebase-contracts.md`](../apis/firebase-contracts.md). The mirrored value is the **already-hashed** code, but the salt is global per-build, so anyone with the Firestore document and the `CODE_SALT` can offline-attack it (audit ISSUE: see [`security-and-risk.md`](../security-and-risk.md)).

## Known issues (audit cross-reference)

- 🟡 [`audit-report/07-issues-and-bugs.md`](../../../audit-report/07-issues-and-bugs.md) ISSUE-014 — `lifecycleScope.launch(Dispatchers.IO)` in `SettingsFragment` for lock-code changes.

## Invariants

- A new `LockType` enum value requires updating `displayNameRes()` and `firebaseValueForKey` to avoid the `when` defaulting to `null` and dropping the sync.
- `getMilli()` returns `0` for `LockDelay.instant` ([`SettingsManager.kt:527`](../../../mobile/src/main/java/com/shelbeely/opentransition/util/settings/SettingsManager.kt)).

## Tests

None.
