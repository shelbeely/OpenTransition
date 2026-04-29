# Contributing to OpenTransition

Thanks for taking the time to contribute! OpenTransition is a Kotlin/Android
photo-journal app for tracking gender transition journeys, and we welcome
patches, bug reports, translations, and ideas.

## Build prerequisites

| Requirement | Notes |
|---|---|
| **JDK 17** | Required by AGP 8 / Kotlin 2.0.20. |
| **Android Studio** *Ladybug* (or newer) | Optional but recommended for the IDE experience. |
| **Android SDK** | `compileSdk = 36`, `targetSdk = 36`, `minSdk = 21`. The Gradle wrapper picks the right SDK from `local.properties` or the `ANDROID_SDK_ROOT` env var. |
| **Gradle** | Use the bundled wrapper (`./gradlew …`); do not install Gradle globally. |

### One-time setup

```bash
# 1. Point Gradle at your SDK
echo "sdk.dir=$ANDROID_SDK_ROOT" > local.properties

# 2. Stub secrets so debug builds work without real Firebase / AdMob keys
cp secrets.properties.example secrets.properties

# 3. Confirm the wrapper is executable
chmod +x ./gradlew

# 4. Sanity-check
./gradlew :mobile:assembleDebug
```

> ⚠️ **Never commit** `secrets.properties`, `local.properties`, or a real
> `mobile/google-services.json`. They are listed in `.gitignore`; the CI
> pipeline injects production values from GitHub Secrets.

## Module layout

| Module | Description |
|---|---|
| `:mobile` | Phone/tablet app — most application logic lives here. |
| `:wear`   | Wear OS companion app (camera remote, audio recording, milestone display). |
| `:shared` | Pure-Kotlin code shared by `:mobile` and `:wear` (data models, Wearable helpers, util). |

When you add code that's used by both apps, put it in `:shared` rather than
duplicating it. `:shared` has **no Android UI dependencies** and minimal Google
Play Services scope (Wearable Data Layer only) so it stays lightweight on the
watch.

## Common commands

| Task | Command |
|---|---|
| Debug APK | `./gradlew :mobile:assembleDebug` |
| Release bundle | `./gradlew :mobile:bundleRelease` |
| Wear debug APK | `./gradlew :wear:assembleDebug` |
| All unit tests | `./gradlew test` |
| Mobile unit tests | `./gradlew :mobile:testDebugUnitTest` |
| Shared unit tests | `./gradlew :shared:testDebugUnitTest` |
| Lint | `./gradlew :mobile:lintDebug :wear:lintDebug` |
| Clean | `./gradlew clean` |

Append `--stacktrace` whenever you're diagnosing a build failure.

## Wear emulator pairing

To test Wear OS changes locally:

1. Start the **mobile** emulator first (`Pixel_*` AVD recommended).
2. Start a **Wear OS** emulator (`Wear_*` AVD).
3. In the mobile emulator, install the Wear OS by Google app from the Play
   Store and follow the in-app pairing flow; choose the running Wear emulator
   as the device.
4. Install both halves of OpenTransition:

   ```bash
   ./gradlew :mobile:installDebug :wear:installDebug
   ```

5. Launch the watch app — the data layer will discover the phone via
   `CapabilityClient` and milestone sync should start within a few seconds.

> 💡 If the watch can't see the phone, restart Google Play Services on the
> Wear emulator (`adb -s <wear-serial> shell am force-stop com.google.android.gms`).

## Instrumented tests

Instrumented tests live under `src/androidTest/`. They require an emulator
or attached device:

```bash
./gradlew :mobile:connectedDebugAndroidTest
```

CI does not currently run instrumented tests; please run them locally for any
change that touches Realm/Room schemas, ViewModels, or fragment transactions.

## Coding conventions

- **Language**: Kotlin first. Add new Java files only when there's a strong reason.
- **Style**: Default Kotlin/Android style; 4-space indent, no wildcard imports
  except inside the `*Ui*.kt` Compose previews.
- **UI**: New screens **must** be Jetpack Compose. Existing XML/View screens
  are being migrated incrementally; if you touch one, prefer migrating it
  rather than extending the legacy XML.
- **Async**: Coroutines + `Flow` for new code. The remaining RxJava call sites
  are being phased out — please don't add new `Observable` chains.
- **DI**: No DI framework yet (Hilt is on the roadmap). Pass dependencies
  through constructors / fragment factory args.
- **Persistence**:
  - The app is mid-migration from Realm → Room/SQLCipher.
  - For any new persistent state, use Room (`mobile/src/main/java/.../database/`).
  - Do not introduce new direct `Realm.openDefault()` call sites.
- **Image loading**: Coil 3 (`coil3.compose.AsyncImage` for Compose,
  `ImageView.load(…)` extension for legacy Views). Picasso has been removed.
- **Error handling**: Background work uses
  `OpenTransitionApp.appScope`, which has a project-wide `CoroutineExceptionHandler`
  that forwards to Crashlytics. If you create a new long-lived `CoroutineScope`,
  attach the same handler.
- **Strings**: Always go through `res/values/strings.xml`; never hard-code
  user-visible text in Kotlin.

## Pull-request checklist

Before opening a PR, please tick the following:

- [ ] Code compiles: `./gradlew :mobile:assembleDebug :wear:assembleDebug`
- [ ] Lint clean: `./gradlew :mobile:lintDebug :wear:lintDebug`
- [ ] Unit tests pass: `./gradlew test`
- [ ] No `secrets.properties` / `google-services.json` / `local.properties` files added
- [ ] No new Realm call sites; new persistent state uses Room
- [ ] User-facing strings live in `strings.xml`
- [ ] If you added a dependency, verified it has no GitHub Advisory DB warnings
- [ ] Public APIs have KDoc; complex flows have inline comments
- [ ] PR description explains *what* and *why*; links any related issue

## Working on `:shared`

Because `:shared` is consumed by both apps:

- Do **not** import `androidx.appcompat`, `androidx.fragment`, or any Compose
  artifacts from `:shared`. Keep it Android-library-Kotlin only.
- Add unit tests under `shared/src/test/` (run with
  `./gradlew :shared:testDebugUnitTest`). The `WearableHelperTest` is a good
  template — it round-trips the JSON payload that crosses the data layer.
- After changing data classes that flow over the Wearable Data Layer, bump the
  related path constant in `WearableConstants.kt` so the watch knows to discard
  the cached payload.

## Reporting bugs

Please include:

- Device / emulator (model, Android version, Wear OS version if applicable).
- App build variant (`debug` vs `release`, `mobile` vs `wear`).
- Steps to reproduce.
- A `logcat` excerpt — filter on `OpenTransition`, `TransTracks`, or
  `MobileWearableListener` to keep it readable.

Sensitive screenshots should be redacted before posting; we don't need real
photos to triage UI bugs.

## Licensing of contributions

OpenTransition is licensed under the **GNU General Public License,
version 3 or (at your option) any later version** — the same license
used by the original [TransTracks](https://github.com/TransTracks/TransTracks-Android)
project that this app is forked from.

By submitting a contribution (a pull request, patch, translation, or
any other change) to this repository, you agree that:

- Your contribution is licensed under **GPL-3.0-or-later**, the same
  license as the rest of the project (this is the standard *inbound =
  outbound* convention used by GPL projects).
- You have the right to submit it under that license — i.e. it is your
  own work, or you have permission from the copyright holder(s), and it
  does not knowingly include code from incompatibly-licensed sources.
- The project may distribute and sublicense your contribution under
  GPL-3.0-or-later as part of OpenTransition.

This keeps the license chain back to TransTracks intact and ensures the
project stays free and open for the trans community.

For the full license text see [`LICENSE`](LICENSE); for the attribution
summary see [`NOTICE`](NOTICE) and [`AUTHORS`](AUTHORS).
