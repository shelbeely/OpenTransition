# Contract: Firebase

> What Firebase services OpenTransition actually wires up today, and how each is
> used. Sourced directly from `FirebaseSettingUtil`, `SettingsManager`,
> `AnalyticsUtil`, `MainActivity`, and `SettingsFragment`.

## Page structure follows the contract template in [`.github/skills/repo-knowledge-base/SKILL.md`](../../../.github/skills/repo-knowledge-base/SKILL.md) §"Eighth Pass: APIs, Routes, and Data Contracts".

## Services in use

| Service | Used? | Notes |
|---|---|---|
| Firebase Auth | ✅ | Optional sign-in for settings sync |
| Firebase Auth UI (FirebaseUI Android) | ✅ | Drives the sign-in screen |
| Firebase Firestore | ✅ | Per-user settings document only |
| Firebase Crashlytics | ✅ | Reports uncaught exceptions in release builds |
| Firebase Analytics | ✅ | Screen-shown events; opt-out via `enableAnalytics` |
| Firebase Storage | ❌ | Not used — no photo or audio is uploaded |
| Firebase Cloud Messaging | ❌ | Not used |
| Firebase Remote Config | ❌ | Not used |

## Auth providers actually wired up

Per [`ui/settings/SettingsFragment.kt:743-746`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/settings/SettingsFragment.kt):

| Provider | Builder |
|---|---|
| Email + password | `AuthUI.IdpConfig.EmailBuilder` |
| Google | `AuthUI.IdpConfig.GoogleBuilder` |
| Twitter / X | `AuthUI.IdpConfig.TwitterBuilder` |
| Apple | `AuthUI.IdpConfig.AppleBuilder` |

Auth state is read via `FirebaseAuth.getInstance().currentUser` throughout the codebase; sign-out goes through `AuthUI.getInstance().signOut(...)` ([`SettingsFragment.kt:130`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/settings/SettingsFragment.kt)).

## Firestore collections used

| Path | Document fields | Reader/writer | Source |
|---|---|---|---|
| `{user.uid}/settings` | Subset of `SettingsManager.Key` values that are **not** in the exclude list (see [`data/shared-prefs-keys.md`](../data/shared-prefs-keys.md)) | `FirebaseSettingUtil.savePreference`, real-time listener `FirebaseSettingUtil.startListening` | [`util/settings/FirebaseSettingUtil.kt:40-110`](../../../mobile/src/main/java/com/shelbeely/opentransition/util/settings/FirebaseSettingUtil.kt) |

> ⚠️ Note: the collection is **named by the user's UID** and contains a single
> document literally named `settings`. There is no top-level `users/` collection,
> no other documents, and **no photo/audio bytes** in Firestore.

### Settings document field set (post-exclusion)

The full `SettingsManager.Key` enum:

```
currentAndroidVersion, incorrectPasswordCount, lockCode, lockDelay, lockType,
saveToFirebase, showAccountWarning, showAds, showWelcome, startDate, theme,
userLastSeen, enableAnalytics, enableCrashReports, encryptedDatabaseEnabled,
decoyVaultEnabled, decoyLockCode, quickHideEnabled, colorVariant
```

**Excluded from the document** ([`SettingsManager.kt:389-396`](../../../mobile/src/main/java/com/shelbeely/opentransition/util/settings/SettingsManager.kt)):

`currentAndroidVersion`, `incorrectPasswordCount`, `userLastSeen`,
`saveToFirebase`, `showAccountWarning`, `decoyLockCode`.

> The `lockCode` field **is** synced, but it's the already-hashed value. The
> hashing salt (`BuildConfig.CODE_SALT`) is global per release build — anyone
> with the Firestore document and the salt can offline-attack the code. See
> [`security-and-risk.md`](../security-and-risk.md).

## Crashlytics keys

`FirebaseCrashlytics.getInstance().recordException(...)` is used in:

- App-level coroutine uncaught handler ([`OpenTransitionApp.kt:54-58`](../../../mobile/src/main/java/com/shelbeely/opentransition/OpenTransitionApp.kt)).
- `MainActivity.onCreate` for early init errors ([`MainActivity.kt:107, 170, 283, 290`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/MainActivity.kt)).
- `LockFragment` on password decryption failures ([`LockFragment.kt:113`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/lock/LockFragment.kt)).
- `SettingsDomain` for Firebase sync conflicts and rollback paths ([`SettingsDomain.kt:240, 258, 275`](../../../mobile/src/main/java/com/shelbeely/opentransition/domain/SettingsDomain.kt)).

No custom Crashlytics keys (`setCustomKey`) are set. Collection enabled flag follows `SettingsManager.getEnableCrashReports()` ([`SettingsManager.kt:215`](../../../mobile/src/main/java/com/shelbeely/opentransition/util/settings/SettingsManager.kt)).

## Analytics events

Defined in [`util/AnalyticsUtil.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/util/AnalyticsUtil.kt) as a sealed `Event` class. Every event is screen-shown only; no behavioural events fire on user interactions. Names sent to `FirebaseAnalytics.logEvent`:

| Event name | Bundle params |
|---|---|
| `AddEditMilestoneControllerShown` | — |
| `AssignPhotoControllerShown` | — |
| `EditPhotoControllerShown` | — |
| `GalleryControllerShown` | `type` ∈ {`Face`, `Body`} |
| `HomeControllerShown` | — |
| `LockControllerShown` | `type` ∈ {`Off`, `Normal`, `Trains`, `Biometric`} |
| `MilestonesControllerShown` | — |
| `SelectPhotoControllerShown` | — |
| `SelectAlbumControllerShown` | — |
| `SingleAlbumControllerShown` | — |
| `SettingsControllerShown` | — |
| `SinglePhotoControllerShown` | — |
| `CameraFragmentShown` | — |

> The voice/audio fragments (`RecordAudio`, `VoiceProgress`, `VoiceSessionDetail`) do **not** currently fire analytics events — a gap relative to the rest of the app.

Collection enabled flag follows `SettingsManager.getEnableAnalytics()`.

## Initialisation timing

- `MobileAds.initialize` and `FirebaseSettingUtil.startListening` (when logged in) are launched off the main thread from `OpenTransitionApp.onCreate` ([`OpenTransitionApp.kt:79-91`](../../../mobile/src/main/java/com/shelbeely/opentransition/OpenTransitionApp.kt)) — historical issue ISSUE-023 fix.

## Known issues

- 🟡 [`SettingsFragment.kt:432`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/settings/SettingsFragment.kt) TODO: password recovery from Firebase.
- 🟡 [`SettingsManager.kt:392-396`](../../../mobile/src/main/java/com/shelbeely/opentransition/util/settings/SettingsManager.kt) TODO: encrypt settings JSON before sending to Firestore.
- 🟠 Firestore root collection naming `{uid}/settings` is unusual — the standard convention is `users/{uid}` with `settings` as a subdoc. Any future field beyond `settings` will sit oddly under the same UID-named collection.

## Invariants (rewrite must preserve)

- Settings sync is **opt-in** (`saveToFirebase` defaults to `false`).
- `decoyLockCode` **must remain** in the exclude list. Mirroring it would defeat the duress-mode property.
- No photo or audio bytes may be written to Firebase Storage / Firestore in any future rewrite without a deliberate user-facing toggle, separate from `saveToFirebase`.
