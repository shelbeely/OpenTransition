# Feature: Wear OS Companion

## Summary

A Wear OS app for hands-free photo capture, remote camera control, voice
recording, and a read-only milestone view. Communicates with the phone via the
Google Play services Wearable Data Layer (Message + Data + Channel clients) and
declares the capability `wear_app` so the phone can discover it.

## Entry points

| Surface | Component | Path |
|---|---|---|
| Home tile | `MainActivity` | [`wear/MainActivity.kt`](../../../wear/src/main/java/com/shelbeely/opentransition/wear/MainActivity.kt) |
| Camera remote-control | `CameraControlActivity` | [`wear/CameraControlActivity.kt`](../../../wear/src/main/java/com/shelbeely/opentransition/wear/CameraControlActivity.kt) |
| Audio recording | `AudioRecordActivity` | [`wear/AudioRecordActivity.kt`](../../../wear/src/main/java/com/shelbeely/opentransition/wear/AudioRecordActivity.kt) |
| Incoming events | `WearableListenerService` | [`wear/WearableListenerService.kt`](../../../wear/src/main/java/com/shelbeely/opentransition/wear/WearableListenerService.kt) |
| Capability declaration | `wear/src/main/res/values/wear.xml` (capability `wear_app`) | [`wear.xml`](../../../wear/src/main/res/values/wear.xml) |

## Cross-app contract

Mobile/wear share constants and DTOs via the `:shared` module — paths, keys,
capability names, photo-type / flash-mode enums, and the `MilestoneData`
`@Parcelize` class:

- [`shared/WearableConstants.kt`](../../../shared/src/main/java/com/shelbeely/opentransition/shared/WearableConstants.kt)
- [`shared/models/MilestoneData.kt`](../../../shared/src/main/java/com/shelbeely/opentransition/shared/models/MilestoneData.kt)
- [`shared/util/WearableHelper.kt`](../../../shared/src/main/java/com/shelbeely/opentransition/shared/util/WearableHelper.kt)

For the path-by-path functional vs dead status, see
[`apis/wearable-data-layer.md`](../apis/wearable-data-layer.md).

## State holders

None on Wear yet; activities own their state directly. There is no Wear-side Domain layer mirroring the phone's `DomainManager`.

## Persistence touched

- **Phone side**: incoming watch audio is saved to `mobile/.../files/audio/<filename>`, then a Realm `Photo` row is inserted with `type = TYPE_AUDIO` ([`MobileWearableListenerService.kt:127-249`](../../../mobile/src/main/java/com/shelbeely/opentransition/wear/MobileWearableListenerService.kt)).
- **Watch side**: a single `MediaRecorder` output file in `cacheDir`; cleared on next record.
- **Watch DataStore**: not used (audit TODO at `wear/MainActivity.kt:215` suggests caching last milestone).

## External APIs

- **Wearable `MessageClient`** for camera triggers (`PATH_TRIGGER_PHOTO`, `PATH_CAMERA_*`, `PATH_REQUEST_SYNC`).
- **Wearable `DataClient`** for sync payloads (milestones via `DATA_PATH_MILESTONES`).
- **Wearable `ChannelClient`** for audio (`PATH_AUDIO_DATA + "/<filename>"`).
- **`CapabilityClient`** to discover the phone and gate inbound messages by source-node (`CAPABILITY_WEAR_APP`).

## Known issues (audit cross-reference)

- 🔴 [`audit-report/07-issues-and-bugs.md`](../../../audit-report/07-issues-and-bugs.md) ISSUE-001 — capability registration must be confirmed at runtime; if not registered, **mobile drops every wear message** ([`MobileWearableListenerService.kt:91-97`](../../../mobile/src/main/java/com/shelbeely/opentransition/wear/MobileWearableListenerService.kt)).
- 🔴 ISSUE-007 — three empty function bodies in `wear/WearableListenerService.kt:52-64` (`process milestone data`, `process settings data`, `process milestone update`).
- 🟡 ISSUE-009 — `AudioRecordActivity` recursive `postDelayed` may leak references when navigated away during recording.
- 🟡 `wear/CameraControlActivity.kt:64-66` TODO: persist preferred flash mode.

## Invariants (rewrite must preserve)

- **Path strings** in `WearableConstants` are part of the wire contract. Renaming them silently breaks watches still on an older mobile app, or vice versa. Any rewrite must keep them stable, with adapters for renames.
- Capability name `wear_app` must remain declared on **both** sides.
- Watch milestone view is read-only — there is no `PATH_MILESTONE_*_WRITE` path.

## Tests

Only `shared` has unit tests for the helper: [`shared/src/test/.../WearableHelperTest.kt`](../../../shared/src/test/java/com/shelbeely/opentransition/shared/util/WearableHelperTest.kt) (6 tests pass per [`audit-report/18-refresh-2026-04-29.md`](../../../audit-report/18-refresh-2026-04-29.md)).
