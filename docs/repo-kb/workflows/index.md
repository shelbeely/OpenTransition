# Workflows

One page per file under [`.github/workflows/`](../../../.github/workflows/). Triggers extracted from the YAML on the current branch.

| Workflow | File | Trigger | Purpose |
|---|---|---|---|
| Continuous Integration | [`ci.yml`](../../../.github/workflows/ci.yml) | `push` to `production` | Full build, then `bundleRelease` for `:mobile` and `:wear`, then Play Store deploy via Fastlane |
| PR Debug Build | [`pr-debug.yml`](../../../.github/workflows/pr-debug.yml) | `workflow_dispatch`, `pull_request` | Debug APK build + boot emulator + run instrumented tests |
| Build & Release Android APK | [`build-release.yml`](../../../.github/workflows/build-release.yml) | `workflow_dispatch` | Manual release build that attaches APK/AAB to a GitHub Release (matrix `mobile` / `wear`) |
| Copilot Debug Build | [`copilot-debug-build.yml`](../../../.github/workflows/copilot-debug-build.yml) | `workflow_dispatch`, push to `copilot/**` | Debug APK build for Copilot agent branches |
| Copilot Setup Steps | [`copilot-setup-steps.yml`](../../../.github/workflows/copilot-setup-steps.yml) | `workflow_dispatch`, push/PR touching the file or `.github/actions/android-setup/action.yml` | Provisions the Copilot agent runner: Android SDK, emulator, secrets stubs |
| Boycott Check | [`boycott-check.yml`](../../../.github/workflows/boycott-check.yml) | `pull_request` touching Gradle files, workflow files, or boycott config | Runs `.github/scripts/boycott_check.py` against `.github/boycott_list.yml` |
| Cleanup Caches | [`cleanup-caches.yml`](../../../.github/workflows/cleanup-caches.yml) | `pull_request` (closed) | Drops PR-scoped Actions caches when the PR closes |
| Deploy Documentation | [`deploy-docs.yml`](../../../.github/workflows/deploy-docs.yml) | Push to `production` or to `docs/**` / `mkdocs.yml` / itself; `workflow_dispatch` | MkDocs site → GitHub Pages |
| UI Screenshots | [`ui-screenshots.yml`](../../../.github/workflows/ui-screenshots.yml) | Push to `production` touching `mobile/src/main/**`; `workflow_dispatch` | Boots `medium_phone` emulator, captures screenshots, refreshes [`screenshots/`](../../../screenshots/) and the `<!-- SCREENSHOTS-START -->` block in `README.md` |

## Reusable building blocks

- [`.github/actions/android-setup/action.yml`](../../../.github/actions/android-setup/) — composite action used by every Android workflow.
- [`.github/ci-scripts/prepare-secrets.sh`](../../../.github/ci-scripts/prepare-secrets.sh), `prepare-google-services.sh`, `prepare-keystore.sh` — secret materialization scripts.
- [`.github/scripts/boycott_check.py`](../../../.github/scripts/boycott_check.py) — boycott check.

## Agent rules for changing workflows

From [`.github/copilot-instructions.md`](../../../.github/copilot-instructions.md):

- **Do not** modify `ci.yml` deploy steps without explicit instruction.
- **Do not** push directly to `production` or `main`.
- Any change that affects build/test commands must also update [`build-and-release.md`](../build-and-release.md) and [`testing.md`](../testing.md) (see [Maintenance Guide](../maintenance-guide.md)).
