# OpenTransition — Copilot Agent Instructions

You are an expert Android development agent working on **OpenTransition**, a Kotlin/Android
photo-journal app for tracking gender transition journeys.

---

## Repository layout

| Module | Description |
|--------|-------------|
| `:mobile` | Main Android phone app (minSdk 21, targetSdk 36) |
| `:wear` | Wear OS companion app |
| `:shared` | Shared Kotlin logic used by both `:mobile` and `:wear` |

Key files:
- `build.gradle` — root build script (AGP 8, Kotlin 2.0.20, Realm 2.3.0, Nav 2.8.5)
- `secrets.properties` — generated from `secrets.properties.example`; **never commit real secrets**
- `mobile/google-services.json` — generated in CI; **never commit real credentials**
- `local.properties` — generated in CI (`sdk.dir=$ANDROID_SDK_ROOT`); **never commit**

---

## Environment bootstrap (already done by copilot-setup-steps.yml)

Before you start any build:
1. `local.properties` exists with `sdk.dir` set.
2. `secrets.properties` is stubbed from `secrets.properties.example`.
3. `mobile/google-services.json` contains dummy values safe for debug builds.
4. Gradle wrapper is executable (`chmod +x ./gradlew`).
5. **Android CLI** is installed at `/usr/local/bin/android` and initialised (`android init`).
6. **Android emulator** (`medium_phone`) is created and fully booted — use `adb devices` to confirm it is listed.

If any of these are missing, re-run the setup steps or execute:
```bash
echo "sdk.dir=$ANDROID_SDK_ROOT" > local.properties
cp secrets.properties.example secrets.properties
chmod +x ./gradlew
```

---

## Common commands

| Task | Command |
|------|---------|
| Full debug build | `./gradlew :mobile:assembleDebug` |
| Full release build | `./gradlew :mobile:bundleRelease` |
| Wear debug build | `./gradlew :wear:assembleDebug` |
| Run all unit tests | `./gradlew test` |
| Run mobile unit tests | `./gradlew :mobile:testDebugUnitTest` |
| Lint | `./gradlew lint` |
| Mobile lint only | `./gradlew :mobile:lintDebug` |
| Clean | `./gradlew clean` |
| Check dependencies | `./gradlew :mobile:dependencies` |
| Install & run on emulator | `android run` |
| List connected devices | `adb devices` |
| Check emulator status | `android emulator list` |
| Search Android docs | `android docs search '<query>'` |
| Fetch a docs page | `android docs fetch kb://<path>` |
| Install an Android skill | `android skills add <skill-name>` |

Always add `--stacktrace` when diagnosing build failures.

---

## Android agentic workflow — best practices

### 1. Grounding in official knowledge

- Use `android docs search '<query>'` and `android docs fetch kb://<path>` to pull
  the latest Android developer guidance directly into context before answering
  questions about APIs, Jetpack libraries, or AGP migrations.
- Prefer the official [Android Developers blog](https://android-developers.googleblog.com),
  [d.android.com](https://developer.android.com), and Kotlin/Firebase docs over pre-trained
  knowledge for anything released after 2024.

### 2. Apply official Android Skills (SKILL.md pattern)

When working on complex or fast-moving areas, consult the relevant Android Skill.
Skills ship in `.github/skills/android/` and `skills/` (installed), or install via `android skills add`:
- **Navigation 3 setup** — use `android skills add --skill=navigation-3`
- **Edge-to-edge / insets** — use `android skills add --skill=edge-to-edge`
- **AGP 9 migration** — use `android skills add --skill=agp-9-upgrade`
- **XML → Compose migration** — use `android skills add --skill=migrate-xml-views-to-jetpack-compose`
- **R8 / ProGuard config** — use `android skills add --skill=r8-analyzer`
- **Play Billing upgrade** — use `android skills add --skill=play-billing-library-version-upgrade`

To install all skills at once: `android skills add --all --project=<repo-root>`

### 3. Dependency management

- Add dependencies using the version variables already defined in `build.gradle`
  (`$nav_version`, `$lifecycle_version`, `$kotlin_version`, etc.).
- Check for vulnerabilities before adding new dependencies.
- Use KSP (already configured) rather than KAPT for new annotation processors.

### 4. Architecture patterns in this project

- **UI layer**: Migrating from XML Views + ViewBinding → Jetpack Compose (incremental;
  use `ComposeView`/`AndroidView` interop during transition).
- **Reactive layer**: RxJava 3 + RxRelay + RxBinding.
- **Local persistence**: Realm Kotlin SDK (`:shared` module) + Room + SQLCipher (`:mobile`).
- **Navigation**: Jetpack Navigation with SafeArgs.
- **Async**: Kotlin Coroutines + `kotlinx-coroutines-rx3` bridge.
- **DI**: No DI framework; dependencies are passed manually.

### 5. Build variants

- `debug` — uses test AdMob IDs from `secrets.properties.example`, dummy Firebase config,
  debug keystore at `keys/debug-keystore.jks`. Crashlytics disabled.
- `release` — uses real AdMob IDs from `secrets.properties`, real Firebase config, release
  keystore. Requires `STORE_PASS`, `KEY_ALIAS`, `KEY_PASS` env vars.

### 6. Testing

- Unit tests live in `src/test/`; instrumented tests in `src/androidTest/`.
- Always run `:mobile:testDebugUnitTest` after any logic change.
- Run `:mobile:lintDebug` before opening a PR.

### 7. CI/CD

- PRs trigger `pr-debug.yml` (debug APK build + upload).
- Pushes to `production` branch trigger `ci.yml` (full build + Play Store deploy).
- Copilot branches (`copilot/**`) trigger `copilot-debug-build.yml`.
- Do **not** modify `ci.yml` deploy steps without explicit instruction.

---

## Secrets convention

| Secret / file | How to obtain in agent environment |
|---|---|
| `secrets.properties` | Auto-stubbed; contains safe test AdMob IDs |
| `mobile/google-services.json` | Auto-stubbed with dummy project info |
| `keys/debug-keystore.jks` | Already committed to repo (safe for debug) |
| `keys/release-keystore.jks` | Injected via `KEYSTORE_64` secret; not available in agent |

---

## What NOT to do

- Do not commit `secrets.properties`, `local.properties`, or `google-services.json` with real values.
- Do not change `minSdkVersion` below 21 or `targetSdkVersion` above 36 without discussion.
- Do not remove or weaken existing ProGuard/R8 rules.
- Do not push directly to `production` or `main` branches.
