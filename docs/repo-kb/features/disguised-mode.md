# Feature: Disguised Mode ("Train Tracks")

## Summary

The app exposes two **launcher activity-aliases** in its manifest. Exactly one is enabled at a time:

- `MainActivityDefault` — OpenTransition icon + label.
- `MainActivityTrain` — disguised "Train Tracks" icon + label.

Toggling Lock Type to `trains` (or back to `normal`) flips which alias is enabled via `PackageManager.setComponentEnabledSetting`. The launcher icon in the system app drawer changes accordingly, and the lock-screen UI swaps to a fake "Train Tracks" theme.

## Entry points

| Surface | Component | Path |
|---|---|---|
| Alias declarations | `AndroidManifest.xml` | [`mobile/src/main/AndroidManifest.xml`](../../../mobile/src/main/AndroidManifest.xml) |
| Toggle logic | `MainActivity` | [`ui/MainActivity.kt:211-228`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/MainActivity.kt) |
| Setting surface | Lock-type radio + snackbar | [`ui/settings/SettingsFragment.kt:475, 555`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/settings/SettingsFragment.kt) |
| Lock UI rebranding | `LockScreen.kt` swaps the title via `R.string.train_tracks_title` for `LockType.trains` | [`ui/lock/LockScreen.kt:94`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/lock/LockScreen.kt) |

## Trigger

The switch happens when the user picks a new `LockType` in Settings. After saving, `MainActivity` (next time it sees `LockType` change) calls `setComponentEnabledSetting` on the two alias `ComponentName`s ([`MainActivity.kt:218-229`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/MainActivity.kt)).

## Persistence touched

| Pref | Where |
|---|---|
| `lockType` (`SettingsManager.Key.lockType`) | `PrefUtil` default prefs |

## External APIs

None (purely `PackageManager`).

## Known issues (audit cross-reference)

- 🟡 If the user denies the snackbar / kills the app before alias toggling runs to completion, the icon and the `LockType` setting can desync. There is no rescue path.

## Invariants

- Both aliases must always be declared in the manifest.
- Component name strings are hard-coded ([`MainActivity.kt:221, 228`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/MainActivity.kt)). Any refactor that moves `MainActivity` packages must update these or the icon swap silently fails.

## Tests

None.
