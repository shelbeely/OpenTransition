# Command Log

Append entries here when a documented command's behaviour changes (works in CI, fails in Cloud Agent, requires a new flag, etc.). Pass 1 did not execute build/test commands — there are therefore no execution results yet.

## Format

```md
## Command: `command here`

### Purpose

### Result

Passed / Failed / Skipped / Unknown

### Environment

Copilot Cloud Agent / Copilot CLI / Local / CI

### Notes

### Evidence

- `path/to/file`
```

## Entries

### 2026-04-30 — Cloud Agent re-validation pass

Goal: re-validate the documented agent-safe commands listed in [`quickstart-for-agents.md`](../quickstart-for-agents.md), [`testing.md`](../testing.md), and [`build-and-release.md`](../build-and-release.md). All four ran inside the GitHub Copilot Cloud Agent runner with the bootstrap files (`local.properties`, `secrets.properties`, stubbed `mobile/google-services.json`) already in place. Logs saved under `audit-report/runtime-artifacts/2026-04-30/`.

## Command: `./gradlew :mobile:lintDebug`

### Purpose

Static analysis on `:mobile` debug variant. Documented as the pre-PR check.

### Result

Failed.

### Environment

Copilot Cloud Agent.

### Notes

`BUILD FAILED in 3m 51s`. Lint reported **2 errors and 190 warnings**. First failure:

```
mobile/src/main/java/com/shelbeely/opentransition/ui/home/HomeScreen.kt:133:
  Error: Querying resource values using LocalContext.current
  [LocalContextGetResourceValueCall from androidx.compose.ui]
```

Pre-existing baseline; not introduced by this pass. Recorded so future agents are not surprised. Fix is out of scope for KB work.

### Evidence

- `audit-report/runtime-artifacts/2026-04-30/lintDebug.log`
- `mobile/build/reports/lint-results-debug.html` (generated, not committed)

## Command: `./gradlew :mobile:testDebugUnitTest`

### Purpose

Mobile-app JVM unit tests on the debug variant.

### Result

Passed.

### Environment

Copilot Cloud Agent.

### Notes

`BUILD SUCCESSFUL in 35s`. No test failures reported.

### Evidence

- `audit-report/runtime-artifacts/2026-04-30/testDebugUnitTest.log`

## Command: `./gradlew :mobile:assembleDebug`

### Purpose

Build the debug APK end-to-end.

### Result

Passed.

### Environment

Copilot Cloud Agent.

### Notes

`BUILD SUCCESSFUL in 2m 45s` (well within the 8-minute budget). Required bootstrap (stubbed `secrets.properties` + `mobile/google-services.json`) was already in place from the runner setup steps.

### Evidence

- `audit-report/runtime-artifacts/2026-04-30/assembleDebug.log`

## Command: `./gradlew test`

### Purpose

All-module JVM unit tests (`:mobile`, `:wear`, `:shared`).

### Result

Passed.

### Environment

Copilot Cloud Agent.

### Notes

`BUILD SUCCESSFUL in 1m 37s`. 158 actionable tasks; ran cleanly across all three modules.

### Evidence

- `audit-report/runtime-artifacts/2026-04-30/test.log`

The canonical list of agent-safe commands lives in [`quickstart-for-agents.md`](../quickstart-for-agents.md), [`build-and-release.md`](../build-and-release.md), and [`testing.md`](../testing.md). Use entries here only to record deviations.
