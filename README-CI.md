# TransTracks Android CI / CD Setup

This bundle contains example GitHub Actions workflows and helper scripts to build and release the TransTracks Android app using GitHub Actions.

It is designed to:

- Build **debug** and **release** APKs on GitHub's x86_64 runners
- Decode `google-services.json` from a GitHub secret
- Optionally decode a release keystore from a GitHub secret
- Upload APKs as build artifacts
- Create GitHub Releases and attach the release APK

## Files

- `.github/workflows/android-build-and-release.yml`  
  Main workflow to build debug/release APKs and optionally publish a GitHub Release.

- `.github/workflows/android-pr-check.yml`  
  Lightweight workflow that runs on pull requests and builds a debug APK.

- `.github/ci-scripts/prepare-google-services.sh`  
  Decodes the `GOOGLE_SERVICES_JSON` secret (Base64) into `app/google-services.json`.

- `.github/ci-scripts/prepare-keystore.sh`  
  Decodes the `ANDROID_KEYSTORE_B64` secret (Base64) into `keys/release-keystore.jks` if present.

## Required Secrets

In your repository settings, go to **Settings → Secrets and variables → Actions** and add:

### Firebase

- `GOOGLE_SERVICES_JSON`  
  Base64-encoded contents of your `google-services.json` file.

  To generate on a local machine:

  ```bash
  base64 -w 0 app/google-services.json
  ```

  Copy the single-line output and paste it as the secret value.

### Release keystore (optional but recommended for signed releases)

- `ANDROID_KEYSTORE_B64`  
  Base64-encoded contents of your `release-keystore.jks`.

  ```bash
  base64 -w 0 keys/release-keystore.jks
  ```

- `ANDROID_KEYSTORE_PASSWORD`  
  Keystore password (STORE_PASS).

- `ANDROID_KEY_ALIAS`  
  Key alias (KEY_ALIAS).

- `ANDROID_KEY_ALIAS_PASSWORD`  
  Key password (KEY_PASS).

These names assume your Gradle configuration uses:

```bash
STORE_PASS
KEY_ALIAS
KEY_PASS
```

as environment variables. If your Gradle files use different names, update the workflow `env:` block accordingly.

## Using the Build & Release workflow

### 1. Manual run (Actions tab)

1. Push this folder structure into your repo.
2. Go to **Actions → Build & Release Android APK → Run workflow**.
3. Choose `debug` or `release` build type.
4. Wait for the workflow to finish:
   - Download the APK from the **Artifacts** section, or
   - If you ran a `release` build with a Git tag, download it from the **Releases** page.

### 2. Tag-based releases

When you push a tag like:

```bash
git tag v1.0.0
git push --tags
```

The workflow will:

- Build a **release** APK
- Upload it as an artifact
- Create a GitHub Release named `TransTracks v1.0.0`
- Attach the APK as a release asset

## Notes

- These workflows expect your Android project (with `app/` module) at the repo root.
- They use the `android-actions/setup-android` action to install the Android SDK and tools.
- `local.properties` is generated dynamically with `sdk.dir` pointing to the SDK installed by the action.
- Firebase is always enabled; `google-services.json` is provided at build time from the secret.

Adjust paths and environment variables as needed to match your exact Gradle configuration.
