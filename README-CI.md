# TransTracks Android CI/CD Bundle

This bundle is designed to drop directly into the **root** of your `TransTracks-Android` repo.

It provides:

- A **Build & Release** workflow that:
  - Builds **debug** or **release** APKs via `workflow_dispatch`
  - Builds a **release** APK for tags like `v1.2.3`
  - Uploads APKs as **artifacts**
  - Creates a **GitHub Release** and attaches the APK for tagged builds
- A **PR Debug** workflow that:
  - Runs on pull requests
  - Builds a **debug** APK
  - Uploads it as an artifact for quick download
- CI helper scripts to:
  - Decode `google-services.json` from a Base64-encoded GitHub secret
  - Decode a release keystore from a Base64-encoded GitHub secret
  - Prepare `secrets.properties` from a Base64-encoded secret or fallback to example file

## Files

- `README-CI.md` – this file
- `.github/workflows/build-release.yml` – main CI/CD workflow
- `.github/workflows/pr-debug.yml` – PR debug build workflow
- `.github/ci-scripts/prepare-google-services.sh` – recreates `mobile/google-services.json`
- `.github/ci-scripts/prepare-keystore.sh` – recreates `keys/release-keystore.jks`
- `.github/ci-scripts/prepare-secrets.sh` – prepares `secrets.properties`

## Required GitHub Secrets

Go to **Settings → Secrets and variables → Actions** in your repo and create:

### Firebase

- `GOOGLE_SERVICES_JSON`  
  Base64-encoded contents of your `mobile/google-services.json`.

  From your local machine:

  ```bash
  base64 -w 0 mobile/google-services.json
  ```

  Copy the single-line output and paste it as the secret value.

### Application secrets (optional)

- `SECRETS_PROPERTIES_B64`  
  Base64-encoded contents of your `secrets.properties`:

  ```bash
  base64 -w 0 secrets.properties
  ```

  If not set, the build will use `secrets.properties.example` as a fallback.

### Release keystore (optional, but recommended for signed releases)

- `ANDROID_KEYSTORE_B64`  
  Base64-encoded contents of your `keys/release-keystore.jks`:

  ```bash
  base64 -w 0 keys/release-keystore.jks
  ```

- `ANDROID_KEYSTORE_PASSWORD`  
  The keystore password (mapped to `STORE_PASS` in Gradle).

- `ANDROID_KEY_ALIAS`  
  The key alias (mapped to `KEY_ALIAS`).

- `ANDROID_KEY_ALIAS_PASSWORD`  
  The key password (mapped to `KEY_PASS`).

These env vars are exported in the workflow as:

- `STORE_PASS` → `ANDROID_KEYSTORE_PASSWORD`
- `KEY_ALIAS` → `ANDROID_KEY_ALIAS`
- `KEY_PASS` → `ANDROID_KEY_ALIAS_PASSWORD`

Make sure your `signingConfigs` in `mobile/build.gradle` use those names, e.g.:

```kotlin
signingConfigs {
    release {
        storeFile = file("${rootDir}/keys/release-keystore.jks")
        storePassword = System.getenv("STORE_PASS") ?: "debugkey"
        keyAlias = System.getenv("KEY_ALIAS") ?: "transtracks"
        keyPassword = System.getenv("KEY_PASS") ?: "debugkey"
    }
}
```

## Using the Build & Release workflow

### Manual run (Actions tab)

1. Commit & push this bundle into the root of your repo.
2. Set the secrets listed above.
3. Go to **Actions → Build & Release Android APK → Run workflow**.
4. Choose:
   - `debug` to build only a debug APK
   - `release` to build a release APK (using your keystore secrets if configured)
5. After it finishes:
   - Download the APK from the **Artifacts** section of the run, or
   - For `release` type on a tag, from the **Releases** page.

### Tag-based releases

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

## Using the PR Debug workflow

- Runs on every `pull_request` targeting any branch.
- Builds a **debug** APK.
- Uploads it as an artifact named `transtracks-pr-debug-apk`.

You can grab the PR’s APK from the **Artifacts** section of that workflow run.

## Notes

- These workflows are designed for a **monorepo** structure with `mobile/`, `wear/`, and `shared/` modules.
- The main mobile app is in the `mobile/` module and the Wear OS app is in the `wear/` module.
- `local.properties` is generated automatically in CI using the `ANDROID_SDK_ROOT` provided by `android-actions/setup-android`.
- `google-services.json` is **not** checked into your repo; it is generated at build time from the `GOOGLE_SERVICES_JSON` secret.
- The workflows use `actions/upload-artifact@v4` and `actions/download-artifact@v4` (no deprecated v3 usage).
- Both mobile and wear APKs are built and uploaded as separate artifacts.
- For tagged releases, both mobile and wear APKs are included in the GitHub release.
