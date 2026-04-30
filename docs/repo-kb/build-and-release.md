# Build & Release

## Build System

- Gradle wrapper: `./gradlew` (`gradlew.bat` on Windows). Always use the wrapper, never a system `gradle`.
- Android Gradle Plugin **8.13.0** (root [`build.gradle`](../../build.gradle)).
- Kotlin **2.0.20**, KSP **2.0.20-1.0.25** (mobile only).
- Realm Kotlin plugin **2.3.0** declared at root, applied where needed.
- Build features in [`gradle.properties`](../../gradle.properties): AndroidX on, Jetifier on, parallel builds on, Gradle build cache on, BuildConfig generation on.

## Build Variants

| Variant | AdMob | Firebase | Crashlytics | Keystore |
|---|---|---|---|---|
| `debug` | Test IDs from `secrets.properties.example` | Dummy `mobile/google-services.json` | **Disabled** (`mobile/build.gradle`: `debug.ext.enableCrashlytics = false`) | `keys/debug-keystore.jks` (committed, password `debugkey`, alias `transtracks`) |
| `release` | Real IDs from `secrets.properties` | Real `mobile/google-services.json` | Enabled | `keys/release-keystore.jks` (injected from `KEYSTORE_64`); requires env `STORE_PASS`, `KEY_ALIAS`, `KEY_PASS` |

The mobile build [fails loud](../../audit-report/15-release-and-distribution.md) (`isReleaseBuildRequested` check in `mobile/build.gradle` and `wear/build.gradle`) if a release task is requested without the release keystore — instead of silently signing with the debug key.

## Common Build Commands

| Command | Purpose | Cloud-Agent Safe? |
|---|---|---|
| `./gradlew :mobile:assembleDebug` | Build mobile debug APK | Yes |
| `./gradlew :wear:assembleDebug` | Build Wear OS debug APK | Yes |
| `./gradlew :mobile:bundleRelease` | Build mobile release AAB | **No** — needs release secrets |
| `./gradlew :wear:bundleRelease` | Build Wear OS release AAB | **No** — needs release secrets |
| `./gradlew clean` | Delete build outputs (also `mobile/release/`, `wear/release/`) | Yes |
| `./gradlew :mobile:dependencies` | Print resolved dependency graph | Yes |
| `./gradlew :mobile:lintDebug` | Lint mobile debug | Yes |
| `./gradlew lint` | Lint all modules | Yes |
| `./gradlew test` | Run unit tests across all modules | Yes |

Always add `--stacktrace` when diagnosing failures.

## Versioning

Defined **once** in root [`build.gradle`](../../build.gradle) so `:mobile` and `:wear` cannot drift:

```
ext.appMajor              = 1
ext.appMinor              = 3
ext.appBaseBuildNumber    = 25
ext.getAppVersionCode()   // = appBaseBuildNumber + (appMajor * 100) + appMinor + BUILD_NUMBER
ext.getAppVersionName()   // = "${appMajor}.${appMinor}.${getAppVersionCode()}"
```

In CI, `BUILD_NUMBER` is set to `${{ github.run_number }}` (see [`.github/workflows/ci.yml`](../../.github/workflows/ci.yml)).

See [`audit-report/15-release-and-distribution.md`](../../audit-report/15-release-and-distribution.md) §6 for the rationale.

## Release Pipeline

`push` to `production` triggers [`.github/workflows/ci.yml`](../../.github/workflows/ci.yml):

1. Checkout PR.
2. Validate Gradle wrapper.
3. `Android setup` (composite action [`.github/actions/android-setup/action.yml`](../../.github/actions/android-setup/action.yml)).
4. Materialize secrets via `.github/ci-scripts/prepare-secrets.sh`, `prepare-google-services.sh`, `prepare-keystore.sh`.
5. `./gradlew bundleRelease` for `:mobile` and `:wear`.
6. Upload AAB to Play Store via Fastlane supply (metadata under [`fastlane/`](../../fastlane/)).

A separate manual workflow [`.github/workflows/build-release.yml`](../../.github/workflows/build-release.yml) builds and attaches APK/AAB artifacts to a GitHub Release (matrix over `mobile` and `wear`).

> **Do not** modify `ci.yml` deploy steps without explicit instruction (see [`.github/copilot-instructions.md`](../../.github/copilot-instructions.md)).

## Documentation Site

[`mkdocs.yml`](../../mkdocs.yml) defines an MkDocs site sourced from [`docs/`](../../docs/). Built and deployed by [`.github/workflows/deploy-docs.yml`](../../.github/workflows/deploy-docs.yml) on push to `production` or changes under `docs/**`.

## Screenshots

[`.github/workflows/ui-screenshots.yml`](../../.github/workflows/ui-screenshots.yml) runs an emulator (`medium_phone`, API 36, debug build), captures screenshots, and commits them under [`screenshots/`](../../screenshots/). The `<!-- SCREENSHOTS-START -->` block in `README.md` is regenerated automatically.
