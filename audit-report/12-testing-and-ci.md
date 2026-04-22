# 12 — Testing and CI

## Test inventory

| Module | Unit tests | Instrumented tests | Test LOC | Coverage |
|--------|-----------:|------------------:|--------:|---------|
| `:mobile` | **10 files / 114 `@Test` methods** | 1 file (`ExampleInstrumentedTest.kt` — boilerplate) | ~600 | not measured (no Jacoco config) |
| `:wear` | 0 | 0 | 0 | 0% |
| `:shared` | 0 | 0 | 0 | 0% |

`@Ignore` count: **0** ✅. No commented-out tests detected via `grep`.

### `:mobile` unit tests (what is actually covered)

| File | What it tests |
|------|---------------|
| `ArraysTest.kt` | Tiny array-extension helpers |
| `BoxedLongTest.kt` | A wrapper class around `Long` for nullable storage |
| `LocalDatesTest.kt` | Date formatting helpers in `LocalDates.kt` |
| `PhotoSerializationTest.kt` | `Photo` Realm-model JSON round-trip (used by `.ttbackup`) |
| `MilestoneSerializationTest.kt` | Same for `Milestone` |
| `AudioAnalysisSerializationTest.kt` | Same for `AudioAnalysis` |
| `QuadrupleTest.kt` | A 4-tuple data class |
| `SettingsEnumsTest.kt` | Enum value mapping (`LockType`, `Theme`, etc.) |
| `StringExtTest.kt` | String extension functions |
| `ExampleUnitTest.kt` | Boilerplate from Android Studio template (1 assertion) |

🟢 **What's good**: the serialization round-trip tests are valuable — they protect the .ttbackup
backwards-compatibility contract.

🔴 **What's missing**:
- **No tests for any `domain/`** (the entire RxRelay-driven business logic layer).
- **No tests for `SettingsManager`** — the 588-LOC singleton.
- **No tests for `MobileWearableListenerService`** — the cross-device contract.
- **No tests for `WearableHelper`** in `:shared`.
- **No tests for the Realm→Room migration**, which is destructive if it goes wrong.
- **No UI tests** beyond the boilerplate `ExampleInstrumentedTest.kt`.
- **No Compose UI tests** even though `ui-test-junit4` is on the classpath.
- **Zero tests in `:wear`** — this is the highest-risk module by ratio.

## Frameworks in use

| Framework | Where | Notes |
|-----------|-------|-------|
| **JUnit 4.13.2** | `mobile/build.gradle:219` | All unit tests use it. |
| **Espresso 3.5.1** | `mobile/build.gradle:222` | Pulled in but only used by the example instrumented test. |
| **Compose UI Test** (`ui-test-junit4`) | `mobile/build.gradle:123` | Pulled in, never used. |
| MockK / Mockito | ❌ | Not present. |
| Turbine | ❌ | Not present (would be useful for Flow tests). |
| Kotest | ❌ | Not present. |
| Truth / AssertJ | ❌ | Not present; tests use stdlib `assertEquals`. |
| Robolectric | ❌ | Not present. Combined with the singleton-heavy architecture, this is why so little can be unit-tested. |
| Paparazzi / Roborazzi / Shot | ❌ | No screenshot tests. |

## Wear-specific test coverage

❌ **Zero.** No tests for:
- Tile / Complication code (because none exists, but the foundation is also untestable).
- Data Layer mocks (`WearableListenerService`).
- Activity lifecycle (`MainActivity.onResume` data sync).

## CI/CD pipeline

8 workflow files in `.github/workflows/`:

| Workflow | Trigger | Builds | Tests | Notes |
|----------|---------|--------|-------|-------|
| `ci.yml` | `push: production`, `pull_request` (excluding `copilot/**`) | `./gradlew build` (all modules) | unit (transitively) | 🔴 deploys to **wrong package name** (`com.drspaceboo.transtracks`) — see ISSUE-002 |
| `pr-debug.yml` | `pull_request` (excluding `copilot/**`) | `:mobile:assembleDebug` | `:mobile:connectedDebugAndroidTest` (boots emulator) | 🟠 wear not built; secret name mismatch (ISSUE-020) |
| `build-release.yml` | `workflow_dispatch` | both | unit | manual only |
| `copilot-debug-build.yml` | `push: copilot/**` | mobile only | unit | runs on Copilot branches |
| `copilot-setup-steps.yml` | n/a — composite | n/a | n/a | sets up Android SDK + emulator + secret stubs for the copilot agent |
| `cleanup-caches.yml` | scheduled | n/a | n/a | GH Actions cache hygiene |
| `deploy-docs.yml` | docs changes | mkdocs | n/a | publishes shelbeely.github.io |
| `ui-screenshots.yml` | manual / scheduled | mobile debug + emulator | runs UiAutomator script | refreshes `screenshots/` |
| `boycott-check.yml` | scheduled | n/a | n/a | checks dependencies against a boycott list |

### Critical CI findings

🔴 **`ISSUE-002`** — `ci.yml:86 packageName: com.drspaceboo.transtracks` while the app builds as
`com.shelbeely.opentransition`. The Play Store deploy will fail or push to the wrong listing.

🟠 **`ISSUE-019`** — Wear app is built (transitively via `./gradlew build`) but never:
- Built explicitly in the PR-debug workflow.
- Bundled and deployed to Play.
- Tested with any wear-specific instrumented harness.

🟠 **`ISSUE-020`** — secret name drift:
- `pr-debug.yml:19` reads `SECRETS_PROPERTIES_B64`; `ci.yml:66` reads `SECRETS_PROPERTIES_64`.
- `pr-debug.yml:20` reads `GOOGLE_SERVICES_JSON`; `ci.yml:30` reads `GOOGLE_SERVICES_JSON_64`.

These are real bugs — one of the workflows is silently building with stub secrets. From the
`copilot-setup-steps.yml` and `actions/android-setup/action.yml` behavior, missing secrets fall back to
the example file, so builds succeed but use test ad IDs.

🟢 **Strong points:**
- Gradle wrapper validation step (`gradle/wrapper-validation-action@v3`) — protects against
  wrapper-jar tampering.
- Lint reports uploaded as artifacts.
- Concurrency control (`cancel-in-progress` on PRs).
- Tag-on-deploy step — automatically tags `vX.Y.Z` after a successful Play deploy.
- KVM emulator runs in PR-debug for instrumented tests.

### Build matrix coverage

| Variant | mobile build | wear build | mobile tests | wear tests |
|---------|:-----------:|:----------:|:-----------:|:---------:|
| PR (debug) | ✅ | ❌ (only via `./gradlew build` in `ci.yml`) | ✅ instr. | ❌ |
| Push to `production` (release) | ✅ (bundle) | ❌ (not bundled, not deployed) | ✅ unit | ❌ |
| `workflow_dispatch build-release` | ✅ | ✅ | ✅ unit | ❌ |
| Copilot branches | ✅ (debug) | ❌ | ✅ unit | ❌ |

🔴 **The wear app has never been built or tested in any PR's CI.** A breaking change to wear could
land on `production` and ship without anyone noticing.

## Release automation

- `r0adkll/upload-google-play@v1.1.3` — pinned, OK.
- `RELEASE_BUNDLE_PATH` and `RELEASE_MAPPING_PATH` are repo variables (good — no hardcoded paths).
- ProGuard mapping uploaded ✅.
- Track: `alpha` ✅ (safe default).
- ❌ **No Fastlane** present. Not necessarily a gap (the GH Actions step does the job), but means no
  local repro of the deploy flow.

## Test maturity ratings

| Platform | Rating | Why |
|----------|------:|------|
| Mobile unit | **5 / 10** | Decent coverage of pure-Kotlin utilities; nothing covers the actual business logic. |
| Mobile instrumented | **2 / 10** | Boilerplate only; emulator infrastructure exists in CI but isn't used for real tests. |
| Wear unit | **0 / 10** | None. |
| Wear instrumented | **0 / 10** | None. |
| Shared | **0 / 10** | None for `WearableHelper` or `MilestoneData`. |
| **Overall test suite** | **3 / 10** | Foundation exists; coverage of risky areas is absent. |

## Concrete recommendations (in order)

1. 🔴 Fix `ISSUE-002` (Play Store package name).
2. 🔴 Fix `ISSUE-020` (secret name mismatch) so CI builds *actually* use real secrets.
3. 🟠 Add `:wear:assembleDebug` to PR-debug; add `:wear:bundleRelease` to deploy.
4. 🟠 Add at least one `WearableHelper` round-trip test and one `MobileWearableListenerService` mock test.
5. 🟡 Add a Realm-Room migration test using an in-memory Room DB and a fixture Realm file.
6. 🟡 Adopt MockK + Turbine; introduce a single Compose UI test for `HomeScreen`/`SettingsScreen`.
7. 🟢 Delete `ExampleUnitTest.kt` and `ExampleInstrumentedTest.kt` (boilerplate noise).
