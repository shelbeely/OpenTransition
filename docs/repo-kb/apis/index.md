# APIs & Contracts

> **Status: pass-1 placeholder.** No HTTP server lives in this repo. The relevant "contracts" here are:

| Contract | Where |
|---|---|
| Wearable Data Layer message paths + payloads | [`shared/src/main/java/com/shelbeely/opentransition/shared/WearableConstants.kt`](../../../shared/src/main/java/com/shelbeely/opentransition/shared/WearableConstants.kt), [`shared/src/main/java/com/shelbeely/opentransition/shared/models/MilestoneData.kt`](../../../shared/src/main/java/com/shelbeely/opentransition/shared/models/MilestoneData.kt) |
| Room schemas | `mobile/schemas/` (KSP `room.schemaLocation` arg) |
| TransTracks `.ttbackup` Realm schema | Realm classes used in read-only import path inside `:mobile` |
| Firebase data model (Firestore) | Defined inline in `:mobile` Firestore calls; not centrally schematized |
| AdMob unit IDs | `secrets.properties` (test IDs in `secrets.properties.example`) |

Per-contract pages are deferred. Use the template in [`.github/skills/curated/repo-knowledge-base/SKILL.md`](../../../.github/skills/curated/repo-knowledge-base/SKILL.md) §"Eighth Pass: APIs, Routes, and Data Contracts".
