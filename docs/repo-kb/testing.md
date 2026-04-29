# Testing

## Layout

| Source set | Tooling | Purpose |
|---|---|---|
| `mobile/src/test/` | JUnit on the JVM | Pure-Kotlin unit tests for `:mobile` |
| `mobile/src/androidTest/` | AndroidX Test + JUnit + AndroidJUnitRunner | Instrumented tests on a connected device or emulator |
| `wear/src/test/`, `wear/src/androidTest/` | Same | Wear OS tests |
| `shared/src/test/`, `shared/src/androidTest/` | Same | Shared library tests |

`testInstrumentationRunner` is `androidx.test.runner.AndroidJUnitRunner` in all three modules' `build.gradle`.

## Commands

| Command | Purpose | Cloud-Agent Safe? |
|---|---|---|
| `./gradlew test` | Run all unit tests (every module, every variant) | Yes |
| `./gradlew :mobile:testDebugUnitTest` | Mobile unit tests, debug variant only | Yes — fastest |
| `./gradlew :wear:testDebugUnitTest` | Wear unit tests, debug variant | Yes |
| `./gradlew :shared:testDebugUnitTest` | Shared lib unit tests | Yes |
| `./gradlew :mobile:connectedDebugAndroidTest` | Run instrumented mobile tests on connected device/emulator | Yes once `adb devices` shows the `medium_phone` emulator |
| `./gradlew :wear:connectedDebugAndroidTest` | Instrumented Wear tests | Requires a Wear emulator/device |
| `./gradlew :mobile:lintDebug` | Run lint as a quasi-test gate before PR | Yes |

Always add `--stacktrace` for failures.

## Test Reports

Gradle writes HTML reports to `mobile/build/reports/tests/`, `wear/build/reports/tests/`, etc.
[`.github/workflows/ci.yml`](../../.github/workflows/ci.yml) and [`.github/workflows/pr-debug.yml`](../../.github/workflows/pr-debug.yml) upload these as workflow artifacts.

## What CI Runs

- [`pr-debug.yml`](../../.github/workflows/pr-debug.yml) — on pull requests: builds debug APK, boots an emulator, runs instrumented tests.
- [`ci.yml`](../../.github/workflows/ci.yml) — on push to `production`: full build, then release bundling and Play Store deploy.
- [`copilot-debug-build.yml`](../../.github/workflows/copilot-debug-build.yml) — on `copilot/**` branches: debug APK build only.
- [`boycott-check.yml`](../../.github/workflows/boycott-check.yml) — on PRs that touch Gradle files: validates dependencies are not on a boycott list (script: `.github/scripts/boycott_check.py`).

See [Workflows](./workflows/index.md) for more.

## Status / Coverage

Per-area test coverage and known gaps are documented in [`audit-report/12-testing-and-ci.md`](../../audit-report/12-testing-and-ci.md). Treat that file as the authoritative testing audit; this KB page only summarizes commands and entry points.
