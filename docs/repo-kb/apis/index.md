# APIs & Contracts

No HTTP server lives in this repo. The relevant "contracts" here are:

| Contract | Documented? | Where |
|---|---|---|
| **Mobile ↔ Wear (Wearable Data Layer)** | ✅ [`wearable-data-layer.md`](./wearable-data-layer.md) | [`shared/src/main/java/com/shelbeely/opentransition/shared/`](../../../shared/src/main/java/com/shelbeely/opentransition/shared/) (`WearableConstants.kt`, `models/MilestoneData.kt`, `util/WearableHelper.kt`) |
| Room schemas | Deferred | `mobile/schemas/` (KSP `room.schemaLocation` arg) |
| TransTracks `.ttbackup` Realm schema | Deferred | Realm classes used in read-only import path inside `:mobile` |
| Firebase data model (Firestore) | Deferred | Defined inline in `:mobile` Firestore calls; not centrally schematized |
| AdMob unit IDs | Deferred | `secrets.properties` (test IDs in `secrets.properties.example`) |

Per-contract pages follow the template in [`.github/skills/curated/repo-knowledge-base/SKILL.md`](../../../.github/skills/curated/repo-knowledge-base/SKILL.md) §"Eighth Pass: APIs, Routes, and Data Contracts".
