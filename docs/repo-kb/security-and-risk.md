# Security & Risk

This is a **risk map** to help future agents avoid breaking sensitive areas. It is **not** a full security audit — see [`audit-report/10-security-review.md`](../../audit-report/10-security-review.md) for that.

## Sensitive Surface Areas

| Area | Where it lives | Risk if broken |
|---|---|---|
| App lock (PIN / pattern / biometric) | `mobile/src/main/java/com/shelbeely/opentransition/` (search for `Biometric`, `AppLock`) | Bypasses user privacy guarantee |
| Disguised mode / decoy vault | Same module | Reveals user is using a transition tracker |
| SQLCipher encrypted DB | [`ENCRYPTED_DATABASE.md`](../../ENCRYPTED_DATABASE.md), `mobile/build.gradle` (`net.zetetic:android-database-sqlcipher:4.5.4`) | Leaks all journal entries / photos |
| EncryptedSharedPreferences | `androidx.security:security-crypto:1.1.0-alpha06` | Leaks credentials/keys |
| Firebase Auth | `mobile/google-services.json` + `com.google.gms.google-services` plugin | Account takeover |
| Wearable Data Layer payloads | `:shared` (`WearableConstants.kt`, `models/`) | Cross-device data leak / spoofed messages |
| AdMob configuration | `secrets.properties` | Wrong publisher ID = lost revenue or policy violation |
| Signing keys | `keys/`, `KEYSTORE_64` secret, `prepare-keystore.sh` | App identity hijack on Play Store |
| ProGuard / R8 rules | `mobile/proguard-rules.pro`, `wear/proguard-rules.pro` | Crashes in release; leaked symbols |

## Hard Rules (Enforced by Repo Policy)

From [`.github/copilot-instructions.md`](../../.github/copilot-instructions.md):

- Do **not** commit real `secrets.properties`, `local.properties`, or `mobile/google-services.json`.
- Do **not** lower `minSdkVersion` below 21 or raise `targetSdkVersion` above 36 without discussion.
- Do **not** remove or weaken ProGuard/R8 rules.
- Do **not** push directly to `production` or `main`.

From this knowledge base:

- Do **not** invent undocumented security behavior.
- Do **not** disable Crashlytics, lint, or boycott checks "to make CI green".
- Do **not** print secret values in logs, KB pages, or PR descriptions.

## Dependency Risk Controls

- [`boycott-check.yml`](../../.github/workflows/boycott-check.yml) (script: [`.github/scripts/boycott_check.py`](../../.github/scripts/boycott_check.py), config: `.github/boycott_list.yml`) blocks PRs that introduce dependencies on a maintained boycott list.
- Dependabot is configured: [`.github/dependabot.yml`](../../.github/dependabot.yml).
- New dependencies should be checked against the GitHub Advisory Database before adoption.

## Crashlytics

Disabled in debug builds (`mobile/build.gradle`: `android.buildTypes.debug.ext.enableCrashlytics = false`). Enabled in release. Do not flip these without explicit instruction.

## Things This Page Does Not Cover

- Detailed cryptographic primitives — see [`ENCRYPTED_DATABASE.md`](../../ENCRYPTED_DATABASE.md).
- Threat model — see [`audit-report/10-security-review.md`](../../audit-report/10-security-review.md).
- Privacy policy / data handling user-facing copy — see `docs/user-guide/`.
