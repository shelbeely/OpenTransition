# Feature: Settings

## Summary

Single `SettingsFragment` covering identity / Firebase, lock & privacy toggles, appearance, ads, analytics & crash reports, decoy vault, quick-hide, backup import, app info, and account warning. State lives in `SharedPreferences` (default) wrapped by `SettingsManager`, with optional bidirectional Firestore mirroring per logged-in user.

## Entry points

| Surface | Component | Path |
|---|---|---|
| Fragment | `SettingsFragment` | [`ui/settings/SettingsFragment.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/settings/SettingsFragment.kt) |
| Compose surface | `SettingsScreen` (used inside the fragment for some sections) | [`ui/settings/SettingsScreen.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/settings/SettingsScreen.kt) |
| Conflict dialog | `SettingsConflictDialog` | [`ui/settings/SettingsConflictDialog.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/settings/SettingsConflictDialog.kt) |

## State holders

- `SettingsDomain` ([`domain/SettingsDomain.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/domain/SettingsDomain.kt)) — emits a stream of effects (`Event`/`Result`) consumed by the fragment.
- `SettingsManager` — sealed object façade over `PrefUtil` + Firestore mirror ([`util/settings/SettingsManager.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/util/settings/SettingsManager.kt)).
- `FirebaseSettingUtil` — write-through to Firestore + listener for incoming updates ([`util/settings/FirebaseSettingUtil.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/util/settings/FirebaseSettingUtil.kt)).

## Persistence touched

All prefs live in `PreferenceManager.getDefaultSharedPreferences(...)`. The full
key list is the `SettingsManager.Key` enum at [`SettingsManager.kt:485-505`](../../../mobile/src/main/java/com/shelbeely/opentransition/util/settings/SettingsManager.kt):

`currentAndroidVersion`, `incorrectPasswordCount`, `lockCode`, `lockDelay`,
`lockType`, `saveToFirebase`, `showAccountWarning`, `showAds`, `showWelcome`,
`startDate`, `theme`, `userLastSeen`, `enableAnalytics`, `enableCrashReports`,
`encryptedDatabaseEnabled`, `decoyVaultEnabled`, `decoyLockCode`,
`quickHideEnabled`, `colorVariant`.

See [`data/shared-prefs-keys.md`](../data/shared-prefs-keys.md) for which feature owns each key, default values, and Firestore-mirror status.

Additional prefs files:
- `albumFirstVisible` (`Context.MODE_PRIVATE`) — last-seen scroll position per album ([`PrefUtil.kt:72-84`](../../../mobile/src/main/java/com/shelbeely/opentransition/util/settings/PrefUtil.kt)). Cleared on each app start.
- Default prefs key `selectPhotoFirstVisible` — same purpose for the picker ([`PrefUtil.kt:88-94`](../../../mobile/src/main/java/com/shelbeely/opentransition/util/settings/PrefUtil.kt)).
- `EncryptedSharedPreferences "opentransition_db_keys"` — DB passphrases.

## External APIs

- **Firebase Auth** — sign-in for sync.
- **Firebase Firestore** — per-user settings document. See [`apis/firebase-contracts.md`](../apis/firebase-contracts.md).
- **Firebase Analytics** — flag toggled by `enableAnalytics`.
- **Firebase Crashlytics** — flag toggled by `enableCrashReports`.
- **AdMob / UMP** — `MobileAds.initialize` in `Application.onCreate`.

## Backup / restore UI

- Export of settings to JSON: `SettingsManager.getSettingsAsJson` ([`SettingsManager.kt:232-237`](../../../mobile/src/main/java/com/shelbeely/opentransition/util/settings/SettingsManager.kt)) — only 4 keys currently round-trip: `currentAndroidVersion`, `startDate`, `theme`, `colorVariant`.
- Import path: `SettingsFragment` launches a file picker for Realm/`.ttbackup` files → `RealmBackupImporter` or the ZIP unpacker. See [`features/transtracks-import.md`](./transtracks-import.md).

## Known issues (audit cross-reference)

- 🟡 `SettingsFragment.kt:432` TODO: password recovery via Firebase.
- 🟡 `SettingsFragment.kt:558` TODO: surface migration progress.
- 🟡 `SettingsFragment.kt:776` TODO: decoy-vault export.
- 🟡 `SettingsManager.kt:392-396` TODO: encrypt the settings JSON before sending to Firestore.
- 🔴 [`audit-report/07-issues-and-bugs.md`](../../../audit-report/07-issues-and-bugs.md) ISSUE-014 — `lifecycleScope.launch(Dispatchers.IO)` used to mutate UI in `SettingsFragment`.

## Invariants

- New keys must be appended to the `SettingsManager.Key` enum *and* handled in `attemptFirebaseAutoSetup` and `firebaseValueForKey`, or Firebase sync silently ignores them ([`SettingsManager.kt:303-391`](../../../mobile/src/main/java/com/shelbeely/opentransition/util/settings/SettingsManager.kt)).
- `decoyLockCode` is **explicitly excluded** from the Firestore mirror — preserve this.

## Tests

None.
