# Feature: App Lock (PIN / Train-Disguise / Biometric) and Decoy Vault

## User-Facing Behavior

OpenTransition can require an unlock challenge before the app's content is shown. Three lock styles are available:

- **Normal password** (`LockType.normal`) — a free-text password field.
- **Trains-disguised password** (`LockType.trains`) — the unlock screen is themed as a "train tracks" lookup. The password field is labelled "Enter train reporting number" and the unlock button reads "Search". Entering an incorrect code shows a generic "incorrect" snackbar phrased as a search-result message rather than a lock failure.
- **Biometric** (`LockType.biometric`) — `BiometricPrompt` is shown automatically on first display; the user can fall back to the normal password field if they choose "Use Password" on the system prompt.

A configurable **lock delay** controls how long the app may be backgrounded before the lock re-engages: `instant`, `oneMinute`, `twoMinutes`, `fiveMinutes`, `fifteenMinutes`.

A separate **Decoy Vault** can be enabled. When active, the user has *two* valid codes:

- The **real** code unlocks the real Room database (`opentransition.db`).
- The **decoy** code unlocks an entirely separate Room database (`opentransition_decoy.db`) that is indistinguishable in the UI — milestones, photos, and audio shown after a decoy unlock come from the decoy DB. The decoy is only checked if `decoyVaultEnabled` is true and a decoy code is set.

After 25 consecutive incorrect attempts, with the "show account warning" preference still on, the user is offered a one-tap option to **disable the lock entirely** (the "one chance" dialog) — intended for users who have lost both codes and would otherwise be locked out forever.

The back button is intercepted on the lock screen: pressing back finishes the activity rather than dismissing the lock.

## Implementation Map

| Layer | Files |
|---|---|
| Lock screen fragment | [`mobile/src/main/java/com/shelbeely/opentransition/ui/lock/LockFragment.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/lock/LockFragment.kt) |
| Lock UI host (XML+Compose interop) | [`mobile/src/main/java/com/shelbeely/opentransition/ui/lock/LockUi.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/lock/LockUi.kt) (`LockView`, `LockUiEvent`) |
| Lock screen Composable | [`mobile/src/main/java/com/shelbeely/opentransition/ui/lock/LockScreen.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/lock/LockScreen.kt) |
| Layout | [`mobile/src/main/res/layout/lock.xml`](../../../mobile/src/main/res/layout/lock.xml) (referenced as `R.layout.lock`) |
| Background train graphic | `R.drawable.train_track_background` (referenced from `LockScreen`) |
| Lock trigger / lifecycle | [`mobile/src/main/java/com/shelbeely/opentransition/ui/MainActivity.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/MainActivity.kt) (`onStart` lock-delay check, `showLockControllerIfNotAlreadyShowing`) |
| Biometric helper | [`mobile/src/main/java/com/shelbeely/opentransition/util/BiometricPromptHelper.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/util/BiometricPromptHelper.kt) |
| Settings (lock type, delay, decoy) | [`mobile/src/main/java/com/shelbeely/opentransition/util/settings/SettingsManager.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/util/settings/SettingsManager.kt) (`LockType`, `LockDelay` enums; `getLockCode/setLockCode`; `isDecoyVaultEnabled/getDecoyLockCode/setDecoyLockCode`; `getIncorrectPasswordCount`/`incrementIncorrectPasswordCount`/`resetIncorrectPasswordCount`) |
| Code encryption / salt | [`mobile/src/main/java/com/shelbeely/opentransition/util/EncryptionUtil.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/util/EncryptionUtil.kt), [`util/settings/PrefUtil.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/util/settings/PrefUtil.kt) (`PrefUtil.CODE_SALT`) |
| Vault switching | [`mobile/src/main/java/com/shelbeely/opentransition/database/DatabaseManager.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/database/DatabaseManager.kt) (`switchToRealVault`, `switchToDecoyVault`, `isDecoyPasscode`, `isUsingDecoyVault`) |
| Database files | [`mobile/src/main/java/com/shelbeely/opentransition/database/AppDatabase.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/database/AppDatabase.kt) (`DATABASE_NAME = "opentransition.db"`, `DECOY_DATABASE_NAME = "opentransition_decoy.db"`) |
| SQLCipher passphrase storage | [`mobile/src/main/java/com/shelbeely/opentransition/database/KeystoreManager.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/database/KeystoreManager.kt) |
| Strings | [`mobile/src/main/res/values/strings.xml`](../../../mobile/src/main/res/values/strings.xml) (`enter_password`, `enter_train_reporting_number`, `incorrect_password`, `train_incorrect`, `unlock_with_*`, `biometric_*`, `one_chance_*`) |
| Tests | None located. See [`_state/unknowns.md`](../_state/unknowns.md). |

## Flow

### Lock engage

1. **App resume.** `MainActivity.onStart()` computes `timeToLock = userLastSeen + lockDelay.getMilli() + 1000ms`.
2. **Decision.** If `LockType.off`, any existing lock fragment is popped from the back stack. Otherwise, if `timeToLock <= now()`, `showLockControllerIfNotAlreadyShowing()` navigates to `R.id.lockFragment` via `R.id.action_global_lockFragment`.
3. **Backgrounding.** `MainActivity.onStop()` calls `SettingsManager.updateUserLastSeen()` so the next foregrounding can compute the elapsed delay.

### Lock screen lifecycle

1. **View setup.** `LockFragment` uses `R.layout.lock`, whose root is `LockView` (`LockUi.kt`). `LockView.onAttachedToWindow` mounts a `ComposeView` that renders `LockScreen(lockType, onUnlock, onUseBiometric)` inside `OpenTransitionTheme`. Events are surfaced through a RxRelay `PublishRelay<LockUiEvent>` (`Unlock(code)` / `UseBiometric`).
2. **Back-button intercept.** `LockFragment` registers an `OnBackPressedCallback` that calls `requireActivity().finish()` instead of popping.
3. **Auto-prompt biometric.** If `LockType.biometric`, `LockFragment.onStart()` calls `showBiometricPrompt(view)` once per view-lifecycle (guarded by `biometricPromptShown`) so the system overlay's `onStop`/`onStart` cycle does not stack prompts.

### Biometric path

1. `BiometricPromptHelper.isBiometricAvailable(context)` checks `BiometricManager.canAuthenticate(BIOMETRIC_STRONG) == BIOMETRIC_SUCCESS` (covers fingerprint, face, iris).
2. `showBiometricPrompt` builds `BiometricPrompt.PromptInfo` with `BIOMETRIC_STRONG`-implied credentials and a "Use Password" negative button.
3. On `onAuthenticationSucceeded`, `LockFragment` defers navigation to `viewLifecycleOwner.lifecycleScope.launch { lifecycle.withStarted { popBackStack(); resetIncorrectPasswordCount } }` — the comment explicitly documents that `view.post {}` is insufficient because the system overlay can pause the host activity, leaving `FragmentManager.isStateSaved == true` and silently dropping `popBackStack()` in Navigation 2.8.x.
4. `ERROR_NEGATIVE_BUTTON` falls back to the password field already visible in the lock layout. Other errors surface a Snackbar.

### Password / train path

On `LockUiEvent.Unlock(code)`:

1. `EncryptionUtil.encryptAndEncode(enteredCode, PrefUtil.CODE_SALT)` produces the comparison string.
2. **Real code check:** equal to `SettingsManager.getLockCode()` → `DatabaseManager.switchToRealVault(context)`, hide keyboard, `popBackStack`, reset incorrect-count.
3. **Decoy code check:** `DatabaseManager.isDecoyPasscode(enteredCode)` (which itself checks `isDecoyVaultEnabled()` and re-encrypts with `CODE_SALT` to compare against `getDecoyLockCode()`) → `switchToDecoyVault(context)`, same unlock cleanup.
4. **Legacy-salt fallback:** if the entered code matches `getLockCode()` when re-encrypted with the hard-coded historical salt `"tzDEzR6dHptPbKwgkvdCIsY1NPT9YZ6c"`, the user is unlocked into the real vault and a non-fatal `Exception("Using the example salt")` is recorded to Crashlytics. This is a one-time recovery path for users whose codes were saved with the example salt that was accidentally shipped.
5. **Incorrect:** Snackbar (`R.string.incorrect_password` for `normal`/`biometric`, `R.string.train_incorrect` for `trains`); `incrementIncorrectPasswordCount(activity)`. After 25 wrong attempts and if `showAccountWarning()` is still true, the **one-chance dialog** is shown — confirming disables the warning, sets `LockType.off`, clears the lock code, and pops the lock screen.

### Vault switching mechanics

- `DatabaseManager` holds a single `currentVaultIsDecoy: Boolean` flag in process. `switchToDecoyVault` only flips the flag if `SettingsManager.isDecoyVaultEnabled()` returns true.
- `AppDatabase.getInstance(context, isDecoy)` returns one of two singletons (`INSTANCE` for real, `DECOY_INSTANCE` for decoy). Both are built by `buildDatabase`, which:
  - Picks the file name (`opentransition.db` vs `opentransition_decoy.db`).
  - If `SettingsManager.isEncryptedDatabaseEnabled()`, wraps the open with SQLCipher's `SupportFactory(passphrase)`. The passphrase comes from `KeystoreManager.getOrCreateDatabaseKey(context, isDecoy)` — a 256-bit `SecureRandom`-generated value stored in `EncryptedSharedPreferences` ("opentransition_db_keys") under either `real_db_passphrase` or `decoy_db_passphrase`. **The passphrase is *not* derived from the user's PIN/password** — the user code only gates which passphrase the SQLCipher layer is asked to use.

## Config and Environment

- **Settings keys** (in `SettingsManager.Key`): `lockCode`, `lockType`, `lockDelay`, `incorrectPasswordCount`, `decoyVaultEnabled`, `decoyLockCode`, `quickHideEnabled`, `encryptedDatabaseEnabled`, `showAccountWarning`. Persisted via `PrefUtil` SharedPreferences.
- **`LockType` enum:** `off`, `normal`, `trains`, `biometric` (default `off`).
- **`LockDelay` enum:** `instant`, `oneMinute`, `twoMinutes`, `fiveMinutes`, `fifteenMinutes` (default `instant`). `getMilli()` returns `1000L * 60L * minutes` (or `0` for `instant`).
- **Code salt:** `PrefUtil.CODE_SALT`. Legacy fallback salt: `"tzDEzR6dHptPbKwgkvdCIsY1NPT9YZ6c"`.
- **Encrypted-DB toggle:** `SettingsManager.isEncryptedDatabaseEnabled()`. When off, both vaults are plain Room DBs at the same file names.
- **Encrypted prefs file:** `opentransition_db_keys` (AES256_SIV key + AES256_GCM value, hardware-backed `MasterKey` when available).
- **One-chance threshold:** hard-coded `25` in `LockFragment.onStart`.
- **Lock lockout interaction with Firebase:** if `saveToFirebase()` is on, `setLockCode`, `setLockType`, `setLockDelay`, `setDecoyLockCode`, `setDecoyVaultEnabled`, and `incorrectPasswordCount` updates are mirrored to Firestore via `FirebaseSettingUtil` (see `SettingsManager` references).
- **Disguised mode (separate from lock):** Per `README.md` §"Key Features", a launcher-icon disguise is also offered. The lock-screen disguise (`LockType.trains`) is independent of that. See [`audit-report/`](../../../audit-report/) for the launcher-alias details and [`_state/unknowns.md`](../_state/unknowns.md).

## Failure Modes

- **Biometric overlay lifecycle race.** Documented in code comments — old `view.post {}` approach lost the navigation when the host activity was paused. Fix is `lifecycle.withStarted { ... }`. Do not regress.
- **Duplicate biometric prompts.** Mitigated by `biometricPromptShown` and by clearing `viewDisposables` at the start of `onStart`. Repeated `BiometricPrompt` instances on overlapping `onStop`/`onStart` cycles caused multiple prompts on some devices.
- **Decoy disabled mid-session.** `DatabaseManager.switchToDecoyVault` silently no-ops if `isDecoyVaultEnabled()` is false at the moment of unlock; the user would then keep using whichever vault was last active.
- **Forgotten code.** Without the one-chance dialog, no recovery exists — there is no email-based reset and (when encrypted DB is on) no plaintext copy of the data. After 25 wrong attempts the user must accept disabling the lock to regain access.
- **Encrypted SharedPreferences reset.** If `EncryptedSharedPreferences` is wiped (e.g. user clears app data), `KeystoreManager` will mint a *new* random passphrase, leaving the existing SQLCipher DB unreadable. The `KeystoreManager.deleteDatabaseKey` path catches `GeneralSecurityException` / `IOException` only.
- **Legacy-salt branch.** Logs a non-fatal Crashlytics exception every time it triggers — high-volume users on legacy installs may see many such reports.
- **Lock state vs. process death.** `DatabaseManager.currentVaultIsDecoy` is per-process; if the OS kills the app while in the decoy vault, the next launch starts in the real vault until the user unlocks again.

## How to Modify Safely

- Treat `LockType` and `LockDelay` enum **names** as wire format — they are stored as strings via `PrefUtil.getEnum` / `setEnum` and synced to Firebase. Renaming a value will silently fall back to defaults on existing devices.
- Do not derive the SQLCipher passphrase from the user's PIN. The current design intentionally separates the two so that PIN changes do not require re-encrypting the DB. If you change this, plan a migration in `KeystoreManager` and re-key the DB with `PRAGMA rekey`.
- Keep the `popBackStack()` calls inside `lifecycle.withStarted { ... }` (or an equivalent state-aware primitive) — see the comment in `LockFragment.showBiometricPrompt`.
- Always call `DatabaseManager.switchToRealVault` *before* `popBackStack` on a real-code unlock (and `switchToDecoyVault` on a decoy unlock) so the next data access reads from the right DB.
- Maintain the `OnBackPressedCallback` that calls `finish()` — losing it lets the user dismiss the lock with a back gesture.
- The legacy-salt fallback can be removed once telemetry shows zero hits in the wild; until then, leave it in place.
- If you raise the `25`-attempt threshold, also revisit the localization of `one_chance_*` strings.
- Update [`features/index.md`](./index.md), [`security-and-risk.md`](../security-and-risk.md), and [`_state/coverage.md`](../_state/coverage.md) when the lock surface changes.

## Evidence

- [`mobile/src/main/java/com/shelbeely/opentransition/ui/lock/LockFragment.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/lock/LockFragment.kt)
- [`mobile/src/main/java/com/shelbeely/opentransition/ui/lock/LockUi.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/lock/LockUi.kt)
- [`mobile/src/main/java/com/shelbeely/opentransition/ui/lock/LockScreen.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/lock/LockScreen.kt)
- [`mobile/src/main/java/com/shelbeely/opentransition/ui/MainActivity.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/MainActivity.kt) (`onStart`/`onStop`, `showLockControllerIfNotAlreadyShowing`)
- [`mobile/src/main/java/com/shelbeely/opentransition/util/BiometricPromptHelper.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/util/BiometricPromptHelper.kt)
- [`mobile/src/main/java/com/shelbeely/opentransition/util/settings/SettingsManager.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/util/settings/SettingsManager.kt) (`LockType`, `LockDelay`, decoy + lock-code accessors)
- [`mobile/src/main/java/com/shelbeely/opentransition/database/DatabaseManager.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/database/DatabaseManager.kt)
- [`mobile/src/main/java/com/shelbeely/opentransition/database/AppDatabase.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/database/AppDatabase.kt)
- [`mobile/src/main/java/com/shelbeely/opentransition/database/KeystoreManager.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/database/KeystoreManager.kt)
- [`ENCRYPTED_DATABASE.md`](../../../ENCRYPTED_DATABASE.md) (canonical SQLCipher design notes)
