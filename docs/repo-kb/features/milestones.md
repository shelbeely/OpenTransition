# Feature: Milestones

## Summary

A milestone is a free-text labelled date marker shown on Home, in the Milestones list,
and (via the Wear companion) on the watch. The user can add, edit, or delete them.

## Entry points

| Surface | Component | Path |
|---|---|---|
| List | `MilestonesFragment` + `MilestonesScreen` | [`ui/milestones/`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/milestones/) |
| Add / Edit | `AddEditMilestoneFragment` + `AddEditMilestoneScreen` | [`ui/addeditmilestone/`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/addeditmilestone/) |
| Home count button | `HomeMilestonesButton` (Compose) | [`ui/home/HomeMilestonesButton.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/home/HomeMilestonesButton.kt) |
| Wear view | `wear/MainActivity.kt` | [`wear/MainActivity.kt`](../../../wear/src/main/java/com/shelbeely/opentransition/wear/MainActivity.kt) |

## State holders

- `AddEditMilestoneDomain` ([`domain/AddEditMilestoneDomain.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/domain/AddEditMilestoneDomain.kt)) — edit state.

## Persistence touched

| Data | Sink |
|---|---|
| `MilestoneEntity { id, epochDay, timestamp, title, description }` | Room `milestones` table via [`MilestoneDao`](../../../mobile/src/main/java/com/shelbeely/opentransition/database/room/dao/MilestoneDao.kt) |
| Legacy `Milestone` (`RealmObject`) | Read by `MobileWearableListenerService.handleSyncRequest` ([`MobileWearableListenerService.kt:172-200`](../../../mobile/src/main/java/com/shelbeely/opentransition/wear/MobileWearableListenerService.kt)) |

## Wear sync

When the watch sends `PATH_REQUEST_SYNC`, the mobile listener queries the **Realm** `Milestone` class, sorts by `epochDay` descending, takes the most-recent 100, converts to `MilestoneData` (with `type = "general"`, no per-milestone type yet), and writes via `WearableHelper.syncMilestones` — see [`apis/wearable-data-layer.md`](../apis/wearable-data-layer.md) and the source at [`MobileWearableListenerService.kt:172-200`](../../../mobile/src/main/java/com/shelbeely/opentransition/wear/MobileWearableListenerService.kt).

> ⚠️ The 100-row cap is `MAX_MILESTONES_TO_SYNC` ([`MobileWearableListenerService.kt:52`](../../../mobile/src/main/java/com/shelbeely/opentransition/wear/MobileWearableListenerService.kt)).

## External APIs

- **Wearable Data Layer** (`DATA_PATH_MILESTONES`, `KEY_MILESTONE_DATA` Gson JSON).
- **Firebase Firestore** for settings only; milestone bodies are **not** synced to Firestore (see [`apis/firebase-contracts.md`](../apis/firebase-contracts.md)).

## Known issues (audit cross-reference)

- 🔴 ISSUE-003 — historical, **now fixed** by `handleSyncRequest` actually pulling from Realm ([`audit-report/18-refresh-2026-04-29.md`](../../../audit-report/18-refresh-2026-04-29.md)).
- 🔴 ISSUE-001 — the underlying capability registration that lets Wear discover Mobile is still being shipped via `wear.xml`; verify before relying on watch reachability.

## Invariants

- `id` is UUID-string.
- `epochDay` UTC; `timestamp` epoch-ms.
- Watch can display at most `MAX_MILESTONES_TO_SYNC` (100) at a time.

## Tests

None.
