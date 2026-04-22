# 03 — Mobile App Review

## Per-feature rating table

| Feature area | Rating | Notes |
|--------------|------:|-------|
| Photo capture (CameraX + face detection) | **7** | CameraX wrapper is mature; relies on legacy `CameraHandler.install`. |
| Gallery (RecyclerView + Realm) | **6** | Works, but adapters open Realm directly. |
| Milestones (CRUD + sync) | **6** | Mobile→Wear sync documented but unimplemented (`07/ISSUE-003`). |
| Audio recording / formant analysis | **3** | 🔴 fake formant numbers (`AudioAnalysisUtil.kt:64-75`). |
| App lock (PIN / biometric / decoy vault) | **6** | Solid `BiometricPromptHelper`; "decoy vault" is a pref toggle but only switches DB instances. |
| `.ttbackup` import / export | **8** | Mature; backwards-compatible with TransTracks. |
| Settings (Firebase sync) | **5** | God-object Fragment + Manager. Real conflict-resolution dialog is a strength. |
| Theming (M3 Expressive + Material You) | **7** | Pink/Blue/Purple/Green palettes + dynamic colour fallback. |
| AdMob + UMP consent | **7** | Test IDs in repo (intentional); UMP wired in `MainActivity:161-175`. |
| Deep link / `.ttbackup` open from share sheet | **7** | Three carefully-crafted intent-filters for MIME-vs-no-MIME edge cases. |
| Notifications | **N/A** | No notification channels created anywhere (`grep -rn "NotificationChannel"` → 0 hits). |
| WorkManager / background work | **N/A** | None used. |
| Accessibility | **5** | Material components inherit base TalkBack support; no `contentDescription` audit done. |
| i18n / RTL | **5** | `supportsRtl="true"` in manifest; only `values/strings.xml` (no localised variants). |

## 1. UI stack — Compose, XML Views, hybrid

**Hybrid, mid-migration.** See `02-architecture-assessment.md §7` for details. Summary:

- **Primary navigation host:** XML Views via Jetpack Navigation (`activity_main.xml` + `main_nav.xml`).
- **Composable surfaces:** ~31 `.kt` files containing `@Composable`. They are mostly hosted by:
  - `ComposeView` set inside an `XxxUi.kt` glue class (e.g. `recordaudio/RecordAudioView.kt:41-52`,
    `selectphoto/selectalbum/SelectAlbumUi.kt:54-97`).
  - `ComposeView` inside RecyclerView item views (e.g. `selectphoto/SelectPhotoAdapter.kt:69-72`).
- **No Compose Navigation, no `setContent` at the Activity level.**
- **Material 3** Compose libraries pulled in via `mobile/build.gradle:170-178` (`material3`,
  `material3-window-size-class`, `material-icons-extended`). Material 3 XML theme styles also exist
  (`values/styles.xml`, `values-v31/themes.xml`).

**Rating: 6 / 10** — works, but the mid-migration creates two parallel UI paradigms in the same screen
sometimes.

## 2. Material 3 / theming

- Compose: `OpenTransitionTheme` in `ui/theme/OpenTransitionTheme.kt` selects between Material You dynamic
  colour (API 31+) and four hand-crafted static palettes resolved via
  `SettingsManager.getResolvedComposeColorVariant()` (`util/settings/SettingsManager.kt:454-466`).
- XML: four parallel themes (`PinkAppTheme`, `BlueAppTheme`, ...) selected via
  `setTheme(SettingsManager.getTheme().styleRes())` *before* `setContentView`
  (`ui/MainActivity.kt:98`).
- Motion tokens isolated in `ui/theme/MotionTokens.kt` and `Shape.kt` — strong isolation. ✅
- Edge-to-edge enabled via `WindowCompat.setDecorFitsSystemWindows(window, false)`
  (`ui/MainActivity.kt:96`) — but no `enableEdgeToEdge()` from `androidx.activity` and no `WindowInsets`
  consumers visible. 🟡

## 3. Activity / Fragment / Composable structure

- **Single Activity:** `ui/MainActivity` (`AppCompatActivity`, 552 LOC).
- **18 Fragments** under `ui/{home,gallery,settings,assignphoto,addeditmilestone,milestones,
  selectphoto,singlephoto,recordaudio,lock,...}` — Navigation drives them all.
- **2 activity-aliases** (`MainActivityDefault`, `MainActivityTrain`) toggled at runtime to swap the
  launcher icon when "trains" lock is enabled (`ui/MainActivity.kt:218-231`).
- **Single `Application`:** `TransTracksApp` constructs a `DomainManager` eagerly (`TransTracksApp.kt:30`).

🟠 **Concerns**:
- `MainActivity` does too much: navigation, intent processing, photo picker, sign-in launcher, Realm
  imports of `.ttbackup`, AdMob consent, theme/lock observers. Splitting it would reduce the
  god-object footprint. (See `01-monorepo-overview §7`.)

## 4. Lifecycle / configuration changes / process death

| Concern | Status |
|--------|--------|
| Rotation handling | **Disabled.** `screenOrientation="portrait"` and `configChanges="..."` (`AndroidManifest.xml:51-55`). Hides the lack of `ViewModel`. |
| Process death | No `onSaveInstanceState` overrides found in any Fragment (`grep -rn "onSaveInstanceState"` → only the AppCompat default). State is reconstructed from Realm + SharedPreferences after recreate. 🟡 |
| Background photo capture state | `AssignPhotosDomain` keeps a cached `CompositeDisposable` and re-emits via Rx; survives recreate by virtue of the singleton domain. 🟢 |
| `onPause`/`onResume` symmetry on listeners | Mobile: ✅ Wear: 🟠 (`onCapabilityChanged` logic in `WearableListenerService` is empty bodies). |

## 5. Background work

- **No `WorkManager`** — `grep -rn "androidx.work"` returns 0 in production code.
- **No `JobScheduler` / `ForegroundService`** — `MobileWearableListenerService` is the only `Service`,
  and it extends Play Services' `WearableListenerService` (already a started/bound pattern managed by
  GMS).
- **Audio playback / recording** uses `MediaRecorder` and an in-process `AudioRecorderUtil`.
- **AdMob consent + Firebase Auth + Firestore writes** all happen on the Activity scope.

🟡 The lack of `WorkManager` is a notable miss — Firebase Firestore sync (`SettingsManager.startFirbaseSync...`),
`.ttbackup` import (which can be MB-scale ZIP extraction), and Realm→Room migration are all done on
ad-hoc coroutine launches inside Fragments. These would be much safer as `WorkManager` jobs with retry
and constraints.

## 6. Permissions

| Permission | Where requested | Notes |
|------------|----------------|-------|
| `CAMERA` | `mobile/.../background/CameraHandler.kt` | Runtime request via `ActivityResultContracts.RequestPermission` |
| `RECORD_AUDIO` | `RecordAudioFragment` | Runtime request |
| `READ_MEDIA_IMAGES` / `READ_MEDIA_AUDIO` | Manifest only | API 33+ media permissions; falls back to `READ_EXTERNAL_STORAGE maxSdkVersion=32`. ✅ |
| `WRITE_EXTERNAL_STORAGE` | Manifest with `maxSdkVersion="28"` | Correct scoping. ✅ |
| `INTERNET` | Required for Firebase/AdMob. ✅ |

🟢 No `BODY_SENSORS`, no `ACCESS_FINE_LOCATION`. Privacy-respecting baseline.
🟢 Photo Picker uses `ActivityResultContracts.PickMultipleVisualMedia` (`MainActivity.kt:114`) which is
the modern, permission-less API. Excellent.
🟡 No "rationale" UI flow visible — if the user denies camera/audio, the user sees a `Toast` and the
flow stops. Better than nothing, but a `shouldShowRequestPermissionRationale` branch with a clear
explanation dialog would be standard.

## 7. Deep links / app links / share targets

- **`.ttbackup` open from Files / Drive / Gmail**: 3 intent-filters on `MainActivity` (`AndroidManifest.xml:60-147`)
  cover MIME-set, MIME-null, and zero-length-MIME cases (latter is for ES File Explorer). The comments
  are excellent. ✅
- **No HTTPS App Links / Digital Asset Links** — the app does not register any web URL handlers, which
  is appropriate for a private, local-data app.
- **No share-target XML** — the app does not appear in the Android share sheet as a target.

## 8. Notifications

❌ **Zero notification channels are created anywhere in the app.** `grep -rn "NotificationManager\|NotificationChannel\|NotificationCompat"` returns 0 results in `mobile/src/main/java`.

The Wear-side `WearableListenerService.onMessageReceived` calls `requestSync()` and shows a Toast
(`wear/.../MainActivity.kt:222-230`), but there is no notification when the watch sends a photo trigger
to the phone — the user has no idea anything happened until they open the app.

🟠 **Recommendation**: at minimum, post a low-importance notification on the phone when the watch
triggers a photo capture; it's a discoverability and trust issue otherwise.

## 9. Storage

| Store | Used for | File |
|-------|----------|------|
| **Realm Kotlin 2.3.0** (default file) | All UI reads/writes for Photo / Milestone / AudioAnalysis. **Primary persistence.** | `util/Realms.kt`, `util/RealmConfigurations.kt` |
| **Room 2.6.1 + SQLCipher** | Declared, has DAOs and entities, only written to during `RealmToRoomMigration` and `RealmBackupImporter`. **Not actually read by the UI.** | `database/AppDatabase.kt`, `database/DatabaseManager.kt` |
| **Decoy Room database** | Same Room class, separate `.db` filename | `database/AppDatabase.kt:43-49` |
| **`SharedPreferences`** | Settings: lock code, theme, etc. via `PrefUtil` | `util/settings/PrefUtil.kt` |
| **`EncryptedSharedPreferences`** | DB passphrase for SQLCipher | `database/KeystoreManager.kt:103-123` |
| **Files** | Audio recordings under `filesDir/audio/`; cached photos under `cacheDir`; backup ZIPs under `cacheDir` | `MobileWearableListenerService.kt:158-188`, `util/FileUtil.kt` |
| **Cloud Firestore** | User settings (when logged in via Firebase Auth) | `util/settings/FirebaseSettingUtil.kt` |
| **Realm migration safety** | `migrate()` reads default Realm and writes to Room; `RealmBackupImporter` reads `.ttbackup` ZIP into a temp Realm. | `database/migration/RealmToRoomMigration.kt`, `database/migration/RealmBackupImporter.kt` |
| **Room schema export** | ❌ `exportSchema = false` (`AppDatabase.kt:34`). Combined with `fallbackToDestructiveMigration()` this means **any Room schema change destroys all encrypted-DB data**. 🔴 |

🔴 **Migration safety**: see `07-issues-and-bugs.md/ISSUE-006`. `fallbackToDestructiveMigration()` plus
`exportSchema=false` plus the existing TODO at `AppDatabase.kt:89` is a data-loss footgun.

## 10. Networking

- **Firebase Auth** + **Firebase Firestore** for cloud settings sync (`util/settings/FirebaseSettingUtil.kt`).
- **AdMob** + **UMP** for ads/consent.
- **No Retrofit / Ktor / OkHttp** in production code (`grep -rn "retrofit2\|okhttp3" mobile/src/main` → 0).
- **No certificate pinning, no Network Security Config XML, no `usesCleartextTraffic` override.**
- **No interceptors, no caching layer.**

This is appropriate — the app is largely offline-first. Firebase handles its own transport security.

🟡 If Crashlytics or Analytics ever need to support strict pinning (some enterprise users), there is no
foundation to do so today.

## 11. Accessibility

A full audit was not performed (would require running TalkBack on a built APK), but static signals:

- ✅ All Material components inherit a baseline `contentDescription` from their `text`/`hint`.
- 🟡 Custom widgets in `ui/widget/` (waveform display, formant chart, etc.) are `View` subclasses doing
  raw `Canvas` drawing — **no `accessibilityNodeInfo` wiring observed**. TalkBack will read these as
  generic "View".
- 🟡 The wear UI has no a11y considerations (small touch targets, no `contentDescription` on
  emoji-text buttons such as `recordButton.text = "🎤 Record"` at `wear/.../AudioRecordActivity.kt:122`).
- 🟢 Touch target sizes look adequate where Material widgets are used (defaults are 48dp).

**Rating: 5 / 10** — passable for Material widgets; weak where custom views render audio data.

## 12. Internationalization / RTL

- ✅ `android:supportsRtl="true"` in manifest.
- 🔴 **Only `values/strings.xml`** — no `values-es/`, `values-fr/`, `values-de/`, etc.
  The README says "Translations/localization" is an area where help is needed
  (`README.md:159`), so this is a known gap.
- 🟡 `strings.auth.xml` exists but is also English-only.

**Rating: 4 / 10**.

## Summary

The mobile app's strength is its mature inherited core (photo, milestone, backup) and its thoughtful
privacy posture (lock, decoy, biometric, optional encryption). Its weaknesses are the three concurrent
migrations that have left two persistence stacks live, two UI paradigms live, and a god-object
`SettingsFragment`. **Mobile rating: 6 / 10.**
