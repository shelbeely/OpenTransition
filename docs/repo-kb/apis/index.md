# APIs & Contracts

No HTTP server lives in this repo. The relevant "contracts" here are:

| Contract | Documented? | Where |
|---|---|---|
| **Mobile ↔ Wear (Wearable Data Layer)** | ✅ [`wearable-data-layer.md`](./wearable-data-layer.md) | [`shared/src/main/java/com/shelbeely/opentransition/shared/`](../../../shared/src/main/java/com/shelbeely/opentransition/shared/) (`WearableConstants.kt`, `models/MilestoneData.kt`, `util/WearableHelper.kt`) |
| **Firebase (Auth providers, Firestore collections, Crashlytics, Analytics events)** | ✅ [`firebase-contracts.md`](./firebase-contracts.md) | [`util/settings/FirebaseSettingUtil.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/util/settings/FirebaseSettingUtil.kt), [`util/AnalyticsUtil.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/util/AnalyticsUtil.kt), [`ui/settings/SettingsFragment.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/settings/SettingsFragment.kt) |
| Room schemas | ✅ [`../data/room-entities.md`](../data/room-entities.md) | `mobile/schemas/` (KSP `room.schemaLocation` arg) |
| TransTracks `.ttbackup` archive layout | ✅ [`../data/ttbackup-format.md`](../data/ttbackup-format.md) | `mobile/src/main/java/com/shelbeely/opentransition/database/migration/` and `data/*.kt` |
| Realm legacy schemas | ✅ [`../data/realm-models.md`](../data/realm-models.md) | `mobile/src/main/java/com/shelbeely/opentransition/data/{Photo,Milestone,AudioAnalysis}.kt` |
| AdMob unit IDs | Deferred | `secrets.properties` (test IDs in `secrets.properties.example`) |

Per-contract pages follow the template in [`.github/skills/repo-knowledge-base/SKILL.md`](../../../.github/skills/repo-knowledge-base/SKILL.md) §"Eighth Pass: APIs, Routes, and Data Contracts".
