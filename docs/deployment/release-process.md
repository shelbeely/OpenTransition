# Release Process

## Overview

OpenTransition releases are published to the Google Play Store. Production builds are triggered automatically by pushing to the `production` branch via the `ci.yml` GitHub Actions workflow.

## Prerequisites

Before releasing, ensure:

- [ ] All changes are merged to `main`
- [ ] `main` is passing CI (green on `pr-debug.yml`)
- [ ] Version numbers are updated in `mobile/build.gradle`
- [ ] Release notes / changelog prepared
- [ ] Release keystore available (CI injects it via `KEYSTORE_64` secret)

## Step 1: Update Version Numbers

In `mobile/build.gradle`:

```gradle
def major = 1   // Increment for major feature releases
def minor = 4   // Increment for minor updates and bug fixes
```

The `versionCode` is calculated automatically:
```
versionCode = baseBuildNumber(25) + (major × 100) + minor + BUILD_NUMBER(CI)
```

## Step 2: Create a Release Branch / PR

```bash
# From main
git checkout -b release/1.4.0
# Make version bump commit
git commit -m "chore: bump version to 1.4.0"
git push origin release/1.4.0
```

Open a PR into `main`, get it reviewed and merged.

## Step 3: Trigger Production Build

Push to the `production` branch to trigger the full CI/CD pipeline:

```bash
git checkout production
git merge main
git push origin production
```

The `ci.yml` workflow will:
1. Build the release AAB (`./gradlew :mobile:bundleRelease`)
2. Sign it with the release keystore (from `KEYSTORE_64` secret)
3. Upload to Google Play Internal track automatically
4. Create a GitHub Release with the AAB attached

## Step 4: Promote on Play Console

After the CI upload:

1. Open [Google Play Console](https://play.google.com/console)
2. Navigate to OpenTransition → Release
3. Promote from **Internal testing** → **Production** (or your rollout track)
4. Write release notes for each supported language
5. Set rollout percentage (start at 10–20% for stability)
6. Review and publish

## Step 5: Create GitHub Release Tag

Tag the commit that was released:

```bash
git tag v1.4.0
git push origin v1.4.0
```

Alternatively, the CI workflow creates this automatically when run from `production`.

## Build Secrets Required for Release

| Secret | Description |
|--------|-------------|
| `KEYSTORE_64` | Base64-encoded release keystore file |
| `STORE_PASS` | Keystore store password |
| `KEY_ALIAS` | Key alias in the keystore |
| `KEY_PASS` | Key password |
| `FIREBASE_TOKEN` | (Optional) Firebase service account for Play publishing |

These are configured in the repository's **Settings → Secrets and variables → Actions**.

## Hotfix Process

For urgent fixes:

1. Branch from the production tag: `git checkout -b hotfix/1.4.1 v1.4.0`
2. Apply fix, increment `minor` version
3. Open PR into `main` **and** `production`
4. After merge to `production`, CI builds and publishes automatically

## Related

- [CI/CD](ci-cd.md) — Workflow details
- [Building the App](../getting-started/building.md) — Local release build instructions
