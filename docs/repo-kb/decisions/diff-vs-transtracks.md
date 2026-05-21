# Diff vs Upstream TransTracks-Android

> Comparison between OpenTransition (current working tree, branch
> `copilot/compare-original-repo`) and upstream
> [TransTracks/TransTracks-Android](https://github.com/TransTracks/TransTracks-Android),
> **archived 2026-04-11**, pinned to SHA `f8560a1aa643e06fa9cfb9dd7c8b4bcbf802f5c9`.
>
> Every upstream row links to that pinned SHA. The upstream is **archived**;
> the comparison is therefore against a frozen point in time.

## Repository shape

| Aspect | Upstream (`@f8560a1`) | OpenTransition |
|---|---|---|
| Modules | Single `:app` (`settings.gradle` is one line: `include ':app'`) | `:mobile`, `:wear`, `:shared` ([`settings.gradle`](../../../settings.gradle)) |
| App namespace | `com.drspaceboo.transtracks` ([`app/build.gradle:104`](https://github.com/TransTracks/TransTracks-Android/blob/f8560a1aa643e06fa9cfb9dd7c8b4bcbf802f5c9/app/build.gradle)) | `com.shelbeely.opentransition` ([`mobile/build.gradle`](../../../mobile/build.gradle)) |
| License header | `Copyright (C) 2018 - 2021 TransTracks` (GPL-3.0-or-later) | Same license; headers updated where files are substantively modified ([`NOTICE`](../../../NOTICE), [`AUTHORS`](../../../AUTHORS)) |

## Toolchain

| Aspect | Upstream `@f8560a1` | OpenTransition |
|---|---|---|
| AGP | `com.android.tools.build:gradle:8.1.2` ([upstream `build.gradle:11`](https://github.com/TransTracks/TransTracks-Android/blob/f8560a1aa643e06fa9cfb9dd7c8b4bcbf802f5c9/build.gradle)) | AGP 8 series (kept current — see [`build.gradle`](../../../build.gradle)) |
| Kotlin | `1.9.10` | `2.0.20` (KSP-enabled) |
| Realm Kotlin | `1.11.1` | `2.3.0` (used only for the import path — see [`data/realm-models.md`](../data/realm-models.md)) |
| Navigation | `2.7.4` | `2.8.5` |
| Annotation processing | `kotlin-kapt` | **Replaced** with `com.google.devtools.ksp` |
| `compileSdk` / `targetSdk` | 34 / 33 | 36 / 36 ([`.github/copilot-instructions.md`](../../copilot-instructions.md)) |
| `minSdk` | 21 | 21 (preserved invariant) |

## Dependencies present in upstream, **kept** in OpenTransition

Realm Kotlin SDK (legacy), RxJava 3, RxAndroid 3, RxBinding 3, RxRelay 3, Gson 2.10.1, FirebaseUI Auth 8.0.2 (Email + Google), `firebase-bom`, Firestore, Crashlytics, Analytics, Picasso 2.8, kotlinx-coroutines-rx3, Material 1.10.x, LeakCanary 2.12 (debug).

## Dependencies **added** by OpenTransition

| Dependency | Why | Citation |
|---|---|---|
| **AndroidX Room** (`room-runtime`, `room-ktx`, `room-compiler` via KSP) | New Room schema replacing Realm | [`mobile/build.gradle`](../../../mobile/build.gradle); see [`data/room-entities.md`](../data/room-entities.md) |
| **SQLCipher for Android** + **`androidx.sqlite`** | Encrypted-DB toggle (currently off) | [`mobile/build.gradle`](../../../mobile/build.gradle); see [`features/encrypted-database.md`](../features/encrypted-database.md) |
| **AndroidX Security** (`MasterKey`, `EncryptedSharedPreferences`) | DB passphrase storage | [`KeystoreManager.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/database/KeystoreManager.kt) |
| **CameraX** (`camera-core`, `camera2`, `camera-lifecycle`, `camera-view`) | Modern camera surface alongside legacy `android.hardware.Camera` | [`background/CameraXHandler.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/background/CameraXHandler.kt) |
| **ML Kit Face Detection** | Live face overlay in `CameraFragment` | [`background/FaceDetectionAnalyzer.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/background/FaceDetectionAnalyzer.kt) |
| **Jetpack Compose** (`compose-bom`, Material 3, Foundation, UI, runtime) | Mixed UI — many fragments host a `ComposeView` | per-feature Compose files under `ui/<feature>/*Screen.kt` |
| **AndroidX Biometric** | App lock via `BiometricPrompt` | [`util/BiometricPromptHelper.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/util/BiometricPromptHelper.kt) |
| **AndroidX Dynamic Animation** | Camera focus-ring spring animations | [`ui/camera/CameraFragment.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/camera/CameraFragment.kt) |
| **Play services Wearable** (`play-services-wearable:18.1.0`) | Cross-app Data Layer | [`shared/build.gradle`](../../../shared/build.gradle), [`wear/build.gradle`](../../../wear/build.gradle); see [`apis/wearable-data-layer.md`](../apis/wearable-data-layer.md) |

## Dependencies **removed** by OpenTransition

None confirmed by source comparison. All upstream-declared dependencies (Realm, Rx*, FirebaseUI, Picasso, Material, etc.) remain referenced in OpenTransition's current tree — the migration is **additive** rather than replacing.

## Auth providers

| Upstream `@f8560a1` ([app/build.gradle](https://github.com/TransTracks/TransTracks-Android/blob/f8560a1aa643e06fa9cfb9dd7c8b4bcbf802f5c9/app/build.gradle)) | OpenTransition ([`SettingsFragment.kt:743-746`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/settings/SettingsFragment.kt)) |
|---|---|
| `firebaseui:firebase-ui-auth` declared; providers wired up in `SettingsController` (not re-fetched here — flagged as a follow-up) | Email + Google + **Twitter** + **Apple** |

## User-facing features in OpenTransition that are **not** in upstream

| Feature | Evidence in OpenTransition | Evidence of absence upstream |
|---|---|---|
| **Wear OS companion** (capture, milestones, audio) | [`wear/`](../../../wear/), [`features/wear-companion.md`](../features/wear-companion.md) | Upstream `settings.gradle` includes only `:app` — no `wear/` module exists. |
| **Audio / voice tracking** (record, pitch, goals) | [`features/audio-tracking.md`](../features/audio-tracking.md) | Upstream `data/` directory contains only `Milestone.kt`, `Photo.kt`, `TransTracksFileProvider.kt` — no `AudioAnalysis.kt`. |
| **Encrypted-database toggle** (UI hidden today) | [`features/encrypted-database.md`](../features/encrypted-database.md), [`ENCRYPTED_DATABASE.md`](../../../ENCRYPTED_DATABASE.md) | No SQLCipher dependency in upstream `app/build.gradle`. |
| **Decoy vault** | [`features/decoy-vault.md`](../features/decoy-vault.md) | No equivalent code path; upstream uses one Realm DB. |
| **Disguised mode** (Train Tracks icon swap) | [`features/disguised-mode.md`](../features/disguised-mode.md) | Upstream lock-type still exists, but **alias activities** + `setComponentEnabledSetting` flow do not — needs follow-up verification of upstream `Manifest.xml`. |
| **CameraX + ML Kit face detection** | [`features/photo-capture.md`](../features/photo-capture.md) | Upstream `app/build.gradle` declares no CameraX or ML Kit. |
| **Jetpack Compose UI** | Multiple `*Screen.kt` files | Upstream uses ViewBinding (`buildFeatures { viewBinding true }` at [app/build.gradle:99](https://github.com/TransTracks/TransTracks-Android/blob/f8560a1aa643e06fa9cfb9dd7c8b4bcbf802f5c9/app/build.gradle)); no Compose deps. |
| **Quick-hide via `FLAG_SECURE`** | [`features/settings.md`](../features/settings.md), [`SettingsManager.kt:445`](../../../mobile/src/main/java/com/shelbeely/opentransition/util/settings/SettingsManager.kt) | Not declared in upstream `Manifest.xml` per the `app/build.gradle` survey. |
| **Material You / color variants** | `colorVariant` pref ([`SettingsManager.kt:458`](../../../mobile/src/main/java/com/shelbeely/opentransition/util/settings/SettingsManager.kt)) | No `AppColorVariant` in upstream Kotlin tree. |

## Features in upstream that OpenTransition **also has**, with significant change

| Feature | Upstream `@f8560a1` | OpenTransition |
|---|---|---|
| Photos | `Realm Photo` only ([upstream `data/Photo.kt`](https://github.com/TransTracks/TransTracks-Android/blob/f8560a1aa643e06fa9cfb9dd7c8b4bcbf802f5c9/app/src/main/java/com/drspaceboo/transtracks/data/Photo.kt)) | Realm `Photo` **and** Room `PhotoEntity` — see [`data/room-entities.md`](../data/room-entities.md) |
| Milestones | `Realm Milestone` only | Realm `Milestone` **and** Room `MilestoneEntity` |
| Lock | PIN code only (`lockCode`) | PIN + biometric + train-tracks disguise + decoy vault |
| `.ttbackup` | Single `Export` button writes ZIP | Import side fully wired; export side **partial** (TODO at [`SettingsFragment.kt:776`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/settings/SettingsFragment.kt)) |
| Settings sync to Firestore | Same shape (`{uid}/settings`) | Same shape; field set expanded by all the new toggles (see [`data/shared-prefs-keys.md`](../data/shared-prefs-keys.md)) |
| Build module structure | One module | Three modules + Wear companion |

## Features in upstream that may be **removed or transformed** in OpenTransition

This row needs follow-up reading of the upstream source. Filed in
[`_state/unknowns.md`](../_state/unknowns.md) — specifically:

- Does upstream have its own (different) "decoy" pattern that OpenTransition has replaced?
- Are upstream's RxJava `Controller` types preserved in OpenTransition, or have any been replaced by Compose+Domain pairs?

## Invariants the rewrite must preserve

- **`.ttbackup` round-trip** with upstream archives — see [`data/ttbackup-format.md`](../data/ttbackup-format.md). A user holding an exported `.ttbackup` from the archived upstream app must be able to import it into any future OpenTransition rewrite.
- **`com.shelbeely.opentransition` namespace** — the rewrite must not regress to `com.drspaceboo.transtracks` (a separate Play Store app) without explicit user opt-in.
- **GPL-3.0-or-later license** — upstream and OpenTransition are both GPL-3.0-or-later; the rewrite cannot relicense.
- **Wear path strings** (`/opentransition/...`) — already a runtime contract; see [`apis/wearable-data-layer.md`](../apis/wearable-data-layer.md).

## Citation index

- Upstream `README.md` @ `c51c7f648355a857a77e3848decc793deebef75b` blob, repo SHA `f8560a1`.
- Upstream `build.gradle` @ blob `06717d36e38ceca2ec3f2984401b96f9ff5f7004`.
- Upstream `app/build.gradle` @ blob `d90df56f426379f2228d75c9315cdfdc5014629e`.
- Upstream `settings.gradle` @ blob `e7b4def49cb53d9aa04228dd3edb14c9e635e003`.
- Upstream `app/src/main/java/com/drspaceboo/transtracks/data/` listing — three files only: `Milestone.kt`, `Photo.kt`, `TransTracksFileProvider.kt`.
