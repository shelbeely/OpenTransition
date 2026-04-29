# Features

> **Status: pass-1 placeholder.** No per-feature pages exist yet. The user-facing feature inventory is in [`README.md`](../../../README.md) §"Key Features" and [`WEAR_APP_FEATURES.md`](../../../WEAR_APP_FEATURES.md). See also [`audit-report/08-unfinished-features.md`](../../../audit-report/08-unfinished-features.md).

## Known features (linkable summary)

| Feature | Owner module | User-facing source |
|---|---|---|
| Photo tracking + face/body/custom albums | `:mobile` | `README.md` |
| Face-detection camera (CameraX + ML Kit) | `:mobile` | `README.md` |
| Milestone management | `:mobile` (+ `:wear` view) | `README.md`, `WEAR_APP_FEATURES.md` |
| Audio tracking (preview) | `:mobile` | `README.md`; caveat in [`audit-report/07-issues-and-bugs.md`](../../../audit-report/07-issues-and-bugs.md) ISSUE-005 |
| App lock (PIN / pattern / biometric) | `:mobile` | `README.md` |
| Disguised mode | `:mobile` | `README.md` |
| Decoy vault | `:mobile` | `README.md` |
| Optional encrypted DB (SQLCipher) | `:mobile` | [`ENCRYPTED_DATABASE.md`](../../../ENCRYPTED_DATABASE.md) |
| TransTracks `.ttbackup` import | `:mobile` | `README.md` |
| Wear OS companion (capture + milestones) | `:wear` | [`WEAR_APP_FEATURES.md`](../../../WEAR_APP_FEATURES.md) |

## Next pass

Create one page per feature using the template in [`.github/skills/curated/repo-knowledge-base/SKILL.md`](../../../.github/skills/curated/repo-knowledge-base/SKILL.md) §"Fifth Pass: Features", grounded in code under `mobile/src/main/java/com/shelbeely/opentransition/`.
