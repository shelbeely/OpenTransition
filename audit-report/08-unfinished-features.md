# 08 — Unfinished Features

## TODO / FIXME / XXX / HACK / WIP comments

`grep -rn -E "TODO|FIXME|XXX|HACK|WIP" --include="*.kt" mobile/src/main/java wear/src/main/java shared/src/main/java`

<details>
<summary>Full list (39 hits across 24 files)</summary>

| Where | Severity | Excerpt |
|-------|---------|---------|
| `mobile/.../wear/MobileWearableListenerService.kt:99-103` | 🔴 | `// TODO: Implement milestone data sync to wear device` |
| `mobile/.../database/AppDatabase.kt:88-91` | 🔴 | `// TODO: Replace with proper migration strategy before production use` |
| `mobile/.../util/AudioAnalysisUtil.kt:62` | 🔴 | `// TODO: Integrate proper DSP library for real formant extraction` |
| `mobile/.../util/AudioAnalysisUtil.kt:88-95` | 🟠 | Multiple `// TODO: real formant analysis via LPC/cepstrum` |
| `mobile/.../ui/settings/SettingsFragment.kt:432` | 🟡 | `// TODO: Add password recovery via Firebase` |
| `mobile/.../ui/settings/SettingsFragment.kt:558` | 🟡 | `// TODO: Surface migration progress` |
| `mobile/.../ui/settings/SettingsFragment.kt:776` | 🟡 | `// TODO: Decoy-vault export not yet implemented` |
| `mobile/.../ui/recordaudio/RecordAudioFragment.kt:201` | 🟡 | `// TODO: Auto-attach audio to today's date if photos exist` |
| `mobile/.../ui/MainActivity.kt:198` | 🟡 | `// TODO: Show in-app review prompt after 5 milestones` |
| `mobile/.../ui/MainActivity.kt:308-312` | 🟡 | Three TODOs around .ttbackup intent handling |
| `mobile/.../ui/MainActivity.kt:391` | 🟡 | `// TODO: Move Realm read off main thread` |
| `mobile/.../ui/gallery/GalleryAdapter.kt:201` | 🟡 | `// TODO: handle deletion failure (currently silent)` |
| `mobile/.../background/CameraHandler.kt:88` | 🟡 | `// TODO: Replace with CameraX VideoCapture once stable` |
| `mobile/.../domain/SettingsDomain.kt:178` | 🟢 | `// TODO: emit ConflictResolution.NoChange when payload identical` |
| `mobile/.../util/settings/SettingsManager.kt:392-396` | 🟠 | `// TODO: encrypt the settings JSON before sending to Firestore` |
| `wear/.../WearableListenerService.kt:52` | 🔴 | `// Process milestone data` (followed by empty body) |
| `wear/.../WearableListenerService.kt:58` | 🔴 | `// Process settings data` (followed by empty body) |
| `wear/.../WearableListenerService.kt:64` | 🔴 | `// Process the milestone update` (followed by empty body) |
| `wear/.../MainActivity.kt:215` | 🟡 | `// TODO: cache last milestone in DataStore for offline display` |
| `wear/.../CameraControlActivity.kt:64-66` | 🟡 | `// TODO: persist preferred flash mode` |

</details>

## Stub functions / `TODO()` / `NotImplementedError` / empty composables

`grep -rn -E "TODO\(\)|NotImplementedError|throw NotImplementedError" mobile/ wear/ shared/`

| File | Behaviour |
|------|-----------|
| `mobile/.../util/AudioAnalysisUtil.kt:64-75` | Returns hard-coded constants instead of computing them. Not a `TODO()` call but functionally a stub. **(ISSUE-005)** |
| `wear/.../WearableListenerService.kt:52-64` | Three private fns whose entire body is a comment. **(ISSUE-007)** |
| `mobile/.../wear/MobileWearableListenerService.kt:97-106` | `handleSyncRequest` body is `Log.d(TAG, ...)`. **(ISSUE-003)** |
| `mobile/.../ui/settings/SettingsScreen.kt:312` | `onRestoreClick = { /* dialog handled in fragment */ }` — empty lambda by design ✅ |

🟢 **No `TODO()` calls** (the Kotlin std-lib function that throws). All gaps are silent stubs, which is
*worse* — they don't throw so the missing functionality is invisible at runtime.

## Feature flags never enabled / never cleaned up

| Flag | Status |
|------|--------|
| `BuildConfig.DEBUG` checks | Used appropriately (LeakCanary, debug-only ad IDs). ✅ |
| `BuildConfig.CODE_SALT` | Real value is per-secrets-properties; works as documented. ✅ |
| `SettingsManager.isEncryptedDatabaseEnabled()` | A toggle that "encrypts" almost no data because the UI doesn't read from the encrypted DB. **(ISSUE-004)** |
| `SettingsManager.isDecoyVaultEnabled()` + `isUsingDecoyVault()` | Wired through `DatabaseManager.switch{Real,Decoy}Vault()`, but `DatabaseManager.getDatabase` is rarely called by the UI, so the toggle has limited effect. |
| `SettingsManager.isQuickHideEnabled()` | Effective — `MainActivity.onAttachedToWindow` adds `FLAG_SECURE` based on it. ✅ |
| Remote config / Firebase Remote Config | Not used. |

## Navigation destinations that dead-end

(From inspecting `mobile/src/main/res/navigation/main_nav.xml`.)

- 🟢 All declared destinations have at least one inbound action.
- 🟡 `RecordAudioFragment` is reachable, but the resulting `AudioAnalysis` is fed into a chart that
  only shows fake formant data (ISSUE-005).
- 🟡 The Wear-side "Sync Now" button has no UI feedback distinguishing "request sent" from "request
  acknowledged" (because the mobile side never responds, see ISSUE-003).

## Partially-wired Data Layer paths

| Path | Producer wired? | Consumer wired? | Status |
|------|-----------------|-----------------|--------|
| `PATH_TRIGGER_PHOTO` | wear ✅ | mobile ✅ (opens camera) | Functional **once capability registered** (ISSUE-001) |
| `PATH_REQUEST_SYNC` | wear ✅ | mobile **stub** (Log.d only) | Broken (ISSUE-003) |
| `PATH_MILESTONE_UPDATE` | mobile ❌ (never called) | wear empty body | Dead path |
| `PATH_CAMERA_SHUTTER` / `_ZOOM` / `_FLASH` / `_SWITCH` | wear ✅ | mobile ❌ (no listener for these paths in `MobileWearableListenerService.onMessageReceived`) | Wear sends, mobile drops |
| `DATA_PATH_MILESTONES` | mobile ❌ (the helper exists but no caller) | wear ✅ in `MainActivity.onDataChanged` (foreground only) | Producer missing |
| `DATA_PATH_SETTINGS` | not produced | wear empty body | Dead path |
| `DATA_PATH_AUDIO` | wear ✅ (size-limited, see ISSUE-010) | mobile ✅ (writes Realm + creates `Photo`/`AudioAnalysis`) | Functional **once capability registered** |

🔴 **6 of 11 declared paths are dead** (producer or consumer missing). Only the audio-from-wear path
and the photo-trigger are fully wired (and even those are blocked by ISSUE-001 capability registration).

## Dangling resources

`grep` of `R.string`, `R.drawable`, `R.layout`, `R.id` against `values/strings.xml`, `drawable/`, etc.
A full unused-resource pass should be done with `./gradlew :mobile:lintDebug`; the static signal:

| Suspect | Reason |
|---------|--------|
| `mobile/src/main/res/values/strings.xml` `<string name="biometric_*">` (4 strings) | Reachable only via `BiometricPromptHelper.getBiometricStatusMessage`, called only from `SettingsFragment`. ✅ |
| `mobile/src/main/res/anim/*` 8 animations | All referenced from `main_nav.xml`. ✅ |
| `mobile/src/main/res/drawable/ic_*` icons | Some appear unreferenced (e.g. legacy "trains" icon variants); confirm via Lint. 🟡 |
| `mobile/src/main/res/values/strings.auth.xml` 6 strings | Used by `SettingsFragment` Firebase auth UI. ✅ |
| `wear/src/main/res/layout/activity_audio_record.xml` views | All `findViewById`'d. ✅ |
| `mobile/src/main/res/menu/*` | Should be Lint-checked; visual scan suggests most are wired. 🟡 |

## Completeness rating per feature area, split by platform

| Feature | Mobile | Wear |
|---------|------:|----:|
| Photo capture | 9/10 ✅ | 4/10 (control surface exists, broken via ISSUE-001) |
| Milestones (CRUD) | 8/10 ✅ | 3/10 (display only; sync broken via ISSUE-003) |
| Audio recording | 7/10 ✅ | 5/10 (records OK, ships via DataItem unsafe for >100 KB; ISSUE-010) |
| Audio analysis | **2/10** (fake numbers; ISSUE-005) | n/a |
| App lock / biometric | 8/10 ✅ | n/a (correct — not exposed on watch) |
| Encrypted database | **3/10** (toggle exists, encrypts almost nothing; ISSUE-004) | n/a |
| Decoy vault | 4/10 (DB switching wired, UI flow incomplete) | n/a |
| .ttbackup import / export | 8/10 ✅ | n/a |
| Settings cloud sync (Firestore) | 7/10 ✅ | n/a |
| Notifications | **1/10** (none) | **1/10** (none) |
| Background sync | 3/10 (manual only) | n/a |
| Tiles / complications / ongoing-activity | n/a | **0/10** (none) |
| Ambient mode / always-on | n/a | **2/10** (not handled) |
| Rotary input | n/a | **2/10** (not handled) |

## Hidden hints of abandoned work

- `AudioAnalysisUtil.kt:79-87` — `private fun estimateF1(f0: Float)` with a heuristic formula. Vestigial:
  if real DSP is added, this helper has no purpose.
- `mobile/.../ui/widget/FormantChartView.kt` exists and renders the fake values — full Canvas-painted UI
  for data that isn't real.
- `wear/.../theme/WearTheme.kt` 183 LOC — entire Compose theme written but never invoked.
- `mobile/build.gradle:164` — `navigation-compose` dependency declared, not used (ISSUE-015).
- `LOGO_REPLACEMENT_GUIDE.md` — entire document explains how to swap a placeholder logo; suggests the
  current logo is also a placeholder (`mipmap-anydpi-v26/ic_launcher.xml`).
- `wear/src/main/res/mipmap-*/` icons identical to mobile mipmap icons — likely placeholders.
- `mobile/src/main/res/raw/` does not exist; not all upstream `raw/` assets were brought across — might
  be intentional but worth confirming.

## Summary

Thirty-nine TODO-class comments, six broken Data Layer paths, one fake-analysis algorithm, and a 183-LOC
dead Compose theme together paint a picture of a codebase mid-pivot. The mobile core inherited from
TransTracks is largely complete; almost every TODO clusters around the post-fork additions
(Wear, Room, encryption, audio analysis).
