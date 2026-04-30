# Features

> **Status: partial.** Three feature pages now exist (camera, import, app lock); the rest of the inventory still points to the canonical user-facing docs. The full feature inventory is in [`README.md`](../../../README.md) §"Key Features" and [`WEAR_APP_FEATURES.md`](../../../WEAR_APP_FEATURES.md). See also [`audit-report/08-unfinished-features.md`](../../../audit-report/08-unfinished-features.md).

## Known features (linkable summary)

| Feature | Owner module | User-facing source | KB page |
|---|---|---|---|
| Photo tracking + face/body/custom albums | `:mobile` | `README.md` | _deferred_ |
| Face-detection camera (CameraX + ML Kit) | `:mobile` | `README.md` | ✅ [`photo-capture.md`](./photo-capture.md) — Documented |
| Milestone management | `:mobile` (+ `:wear` view) | `README.md`, `WEAR_APP_FEATURES.md` | _deferred_ |
| Audio tracking (preview) | `:mobile` | `README.md`; caveat in [`audit-report/07-issues-and-bugs.md`](../../../audit-report/07-issues-and-bugs.md) ISSUE-005 | _deferred_ |
| App lock (PIN / trains-disguise / biometric) + decoy vault | `:mobile` | `README.md` | ✅ [`app-lock.md`](./app-lock.md) — Documented |
| Disguised launcher icon | `:mobile` | `README.md` | _deferred_ |
| Optional encrypted DB (SQLCipher) | `:mobile` | [`ENCRYPTED_DATABASE.md`](../../../ENCRYPTED_DATABASE.md) | partially covered by [`app-lock.md`](./app-lock.md) §"Vault switching mechanics"; standalone page _deferred_ |
| TransTracks `.ttbackup` import + Realm→Room migration | `:mobile` | `README.md` | ✅ [`import.md`](./import.md) — Documented |
| Wear OS companion (capture + milestones) | `:wear` | [`WEAR_APP_FEATURES.md`](../../../WEAR_APP_FEATURES.md) | _deferred_ — wire format covered in [`apis/wearable-data-layer.md`](../apis/wearable-data-layer.md) |

## Documented pages

- [`photo-capture.md`](./photo-capture.md) — CameraX preview + ML Kit face-detection analyzer + temp-file capture pipeline.
- [`import.md`](./import.md) — Two import paths (`.ttbackup` zip → legacy Realm via `MainActivity.processImport`; in-app `.realm` picker → Room via `RealmBackupImporter`) plus the in-place `RealmToRoomMigration`.
- [`app-lock.md`](./app-lock.md) — Lock types (`off`/`normal`/`trains`/`biometric`), lock delay, decoy vault, biometric prompt, vault switching, and SQLCipher passphrase storage.

## Next pass

Continue per the skill template in [`.github/skills/repo-knowledge-base/SKILL.md`](../../../.github/skills/repo-knowledge-base/SKILL.md) §"Fifth Pass: Features". Highest-value next: milestone management (cross-cuts `:mobile` and `:wear`), and the disguised-launcher feature (currently only documented prose).
