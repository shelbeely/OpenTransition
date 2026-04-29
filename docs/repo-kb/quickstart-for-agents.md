# Quickstart for Agents

If you are a Copilot Cloud Agent, Copilot CLI session, or VS Code agent: read this **before** broadly searching the repo. It is the shortest path to the right context.

## Read Order

1. This page.
2. [`docs/repo-kb/index.md`](./index.md) — project summary.
3. [`docs/repo-kb/architecture.md`](./architecture.md) — module + layer overview.
4. [`docs/repo-kb/repo-map.md`](./repo-map.md) — directory map.
5. [`docs/repo-kb/_state/coverage.md`](./_state/coverage.md) — what is documented vs. missing.
6. [`docs/repo-kb/_state/unknowns.md`](./_state/unknowns.md) — open questions; do **not** assume undocumented behavior.
7. The relevant page under `features/`, `components/`, `apis/`, `data/`, `workflows/`, or `files/`.
8. The source code itself for verification — **the source is the final authority.**

## Repo at a Glance

- Gradle multi-module Android monorepo: `:mobile`, `:wear`, `:shared`.
- Kotlin 2.0.20, AGP 8.13.0, Jetpack Navigation 2.8.5.
- UI: XML Views + Jetpack Compose interop (incremental migration).
- DB: Room + optional SQLCipher; Realm read-only for legacy import.
- See [`docs/repo-kb/index.md`](./index.md) for the full tech matrix.

## Environment Bootstrap

These are normally already done by [`.github/workflows/copilot-setup-steps.yml`](../../.github/workflows/copilot-setup-steps.yml). If anything looks missing, run:

```bash
echo "sdk.dir=$ANDROID_SDK_ROOT" > local.properties
cp secrets.properties.example secrets.properties
chmod +x ./gradlew
```

You also need (already provisioned in the agent runner):

- `mobile/google-services.json` (stubbed dummy values for debug builds).
- `keys/debug-keystore.jks` (committed; debug-only).
- Android SDK + emulator (`medium_phone`); confirm with `adb devices`.

## Cloud-Agent-Safe Commands

| Command | Purpose | Notes |
|---|---|---|
| `./gradlew :mobile:assembleDebug` | Build mobile debug APK | Slow on first run |
| `./gradlew :wear:assembleDebug` | Build Wear OS debug APK | |
| `./gradlew test` | Run all unit tests | |
| `./gradlew :mobile:testDebugUnitTest` | Mobile unit tests only | Fastest verification of logic changes |
| `./gradlew :mobile:lintDebug` | Lint the mobile module | Run before opening a PR |
| `./gradlew lint` | Lint everything | |
| `./gradlew clean` | Clean build outputs | Use sparingly — slows next build |
| `./gradlew :mobile:dependencies` | Inspect resolved dependency graph | |
| `adb devices` | Verify emulator is connected | |
| `android docs search '<query>'` | Search official Android docs | Use before guessing about Android APIs |

Add `--stacktrace` to any failing Gradle command to diagnose.

## Commands That Are NOT Safe in Cloud Agent

| Command | Why |
|---|---|
| `./gradlew :mobile:bundleRelease` | Requires `STORE_PASS`, `KEY_ALIAS`, `KEY_PASS` env vars and the real release keystore (`KEYSTORE_64` secret) |
| `git push origin production` / `main` | Direct pushes to protected branches are forbidden by [`.github/copilot-instructions.md`](../../.github/copilot-instructions.md) and policy |
| Anything that mutates Firebase project, Play Store, or AdMob | No real credentials available |

## Validation Workflow Before Finishing

1. `./gradlew :mobile:testDebugUnitTest` — unit tests.
2. `./gradlew :mobile:lintDebug` — lint.
3. If you changed `:wear` or `:shared`, also build `:wear:assembleDebug` and `:shared:assembleDebug`.
4. Update [`_state/session-log.md`](./_state/session-log.md) with what you changed.
5. Update the relevant KB page (architecture, configuration, workflow, etc.) if your change affects it.
6. If a documented command fails, record it in [`_state/command-log.md`](./_state/command-log.md).

## When to Search the Source

Search the source code (don't trust the KB) when:

- The KB is missing the area you need.
- The KB marks the area Partial or Missing in [`_state/coverage.md`](./_state/coverage.md).
- The KB and source disagree — **trust the source** and update the KB in the same PR.
- You are about to change behavior and need exact implementation details.
- A documented command fails.

## Hard Don'ts

From [`.github/copilot-instructions.md`](../../.github/copilot-instructions.md):

- Don't commit `secrets.properties`, `local.properties`, or real `mobile/google-services.json`.
- Don't change `minSdkVersion` below the documented floor or `targetSdkVersion` above the documented ceiling without discussion. The policy in `.github/copilot-instructions.md` says floor 21 / ceiling 36; the current `mobile/build.gradle` actually sets `minSdkVersion 26 / targetSdkVersion 35` — see [`_state/unknowns.md`](./_state/unknowns.md) for the reconciliation question.
- Don't remove or weaken existing ProGuard/R8 rules.
- Don't push directly to `production` or `main`.

From this KB:

- Don't invent undocumented behavior — file an entry in [`_state/unknowns.md`](./_state/unknowns.md).
- Don't mark KB coverage Complete unless the area really is documented.
- Don't paste large source files into KB pages — summarize and link.
