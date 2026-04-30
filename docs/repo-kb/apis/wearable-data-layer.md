# Contract: Mobile ↔ Wear (Wearable Data Layer)

The wire contract between `:mobile` and `:wear`. Single source of truth lives in `:shared`. This page summarises and links — do not let it drift from the source.

Page structure follows the contract template in [`.github/skills/curated/repo-knowledge-base/SKILL.md`](../../../.github/skills/curated/repo-knowledge-base/SKILL.md) §"Eighth Pass: APIs, Routes, and Data Contracts".

## Location

- [`shared/src/main/java/com/shelbeely/opentransition/shared/WearableConstants.kt`](../../../shared/src/main/java/com/shelbeely/opentransition/shared/WearableConstants.kt) — message paths, data-item paths, capability names, message keys, photo/flash enumerations.
- [`shared/src/main/java/com/shelbeely/opentransition/shared/models/MilestoneData.kt`](../../../shared/src/main/java/com/shelbeely/opentransition/shared/models/MilestoneData.kt) — `@Parcelize` cross-app DTO.
- [`shared/src/main/java/com/shelbeely/opentransition/shared/util/WearableHelper.kt`](../../../shared/src/main/java/com/shelbeely/opentransition/shared/util/WearableHelper.kt) — wire helpers wrapping `MessageClient` and `DataClient`.
- [`wear/src/main/java/com/shelbeely/opentransition/wear/WearableListenerService.kt`](../../../wear/src/main/java/com/shelbeely/opentransition/wear/WearableListenerService.kt) — wear-side inbound endpoint.
- [`mobile/src/main/java/com/shelbeely/opentransition/wear/MobileWearableListenerService.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/wear/MobileWearableListenerService.kt) — mobile-side inbound endpoint.
- [`wear/src/main/AndroidManifest.xml`](../../../wear/src/main/AndroidManifest.xml) — registers wear-side service intent filter on path prefix `/opentransition`.

## Purpose

Define the protocol the phone app and the Wear OS companion use to:

- Trigger photo capture from the watch.
- Push milestone data from the phone to the watch (with local caching on the watch).
- Notify the watch when an individual milestone changes on the phone.
- Drive remote camera controls (shutter, zoom, flash, switch) from the watch.
- Stream audio recordings from the watch to the phone.

Transport is Google's **Wearable Data Layer API** via `com.google.android.gms:play-services-wearable:18.1.0`, declared in both [`wear/build.gradle`](../../../wear/build.gradle) and [`shared/build.gradle`](../../../shared/build.gradle). Three transports are used:

| Transport | Used for |
|---|---|
| `MessageClient` (one-shot) | Photo trigger, sync request, milestone-update notification, all camera/audio control |
| `DataClient` (replicated key-value items) | Bulk milestone sync, settings sync (reserved), audio data items (reserved) |
| `ChannelClient` (streaming bytes) | Audio file transfer on path `PATH_AUDIO_DATA + "/<filename>"` |

## Inputs

### Capability advertisements

`CapabilityClient` discovery names defined by `WearableConstants`:

| Constant | Value | Advertised by |
|---|---|---|
| `CAPABILITY_MOBILE_APP` | `"opentransition_mobile_app"` | `:mobile` |
| `CAPABILITY_WEAR_APP` | `"opentransition_wear_app"` | `:wear` |

### Message paths (all share prefix `/opentransition`)

| Constant | Path | Direction | Body |
|---|---|---|---|
| `PATH_TRIGGER_PHOTO` | `/opentransition/trigger_photo` | wear → mobile | UTF-8 photo type (`"face"` / `"body"`) |
| `PATH_SYNC_MILESTONES` | `/opentransition/sync_milestones` | mobile → wear | trigger only (typically empty) |
| `PATH_MILESTONE_UPDATE` | `/opentransition/milestone_update` | mobile → wear | empty notification |
| `PATH_REQUEST_SYNC` | `/opentransition/request_sync` | wear → mobile | `ByteArray(0)` |
| `PATH_CAMERA_SHUTTER` | `/opentransition/camera/shutter` | wear → mobile | empty |
| `PATH_CAMERA_ZOOM` | `/opentransition/camera/zoom` | wear → mobile | encodes `KEY_ZOOM_LEVEL` |
| `PATH_CAMERA_FLASH` | `/opentransition/camera/flash` | wear → mobile | encodes `KEY_FLASH_MODE` |
| `PATH_CAMERA_SWITCH` | `/opentransition/camera/switch` | wear → mobile | empty |
| `PATH_AUDIO_START` | `/opentransition/audio/start` | wear → mobile | empty |
| `PATH_AUDIO_STOP` | `/opentransition/audio/stop` | wear → mobile | empty |
| `PATH_AUDIO_DATA` | `/opentransition/audio/data` | wear → mobile (channel) | streamed bytes; channel path is `PATH_AUDIO_DATA + "/<filename>"` |

### DataItem paths

| Constant | Path | Producer | Keys |
|---|---|---|---|
| `DATA_PATH_MILESTONES` | `/opentransition/data/milestones` | `:mobile` | `KEY_MILESTONE_DATA` (Gson JSON), `KEY_MILESTONE_COUNT` (`Int`), `KEY_LAST_SYNC` (`Long`, epoch ms) |
| `DATA_PATH_SETTINGS` | `/opentransition/data/settings` | reserved | wear-side handler currently logs only |
| `DATA_PATH_AUDIO` | `/opentransition/data/audio` | reserved | — |

### Message / DataMap keys

| Constant | Value | Used by |
|---|---|---|
| `KEY_PHOTO_TYPE` | `"photo_type"` | photo trigger payloads |
| `KEY_MILESTONE_DATA` | `"milestone_data"` | `DATA_PATH_MILESTONES` |
| `KEY_MILESTONE_COUNT` | `"milestone_count"` | `DATA_PATH_MILESTONES` |
| `KEY_LAST_SYNC` | `"last_sync"` | `DATA_PATH_MILESTONES` |
| `KEY_ZOOM_LEVEL` | `"zoom_level"` | `PATH_CAMERA_ZOOM` |
| `KEY_FLASH_MODE` | `"flash_mode"` | `PATH_CAMERA_FLASH` |
| `KEY_AUDIO_DATA` | `"audio_data"` | audio data item / channel |
| `KEY_AUDIO_FILENAME` | `"audio_filename"` | audio data item / channel |

### Enumerations

Photo types accepted by `PATH_TRIGGER_PHOTO`: `PHOTO_TYPE_FACE = "face"`, `PHOTO_TYPE_BODY = "body"`.

Flash modes carried under `KEY_FLASH_MODE`: `FLASH_MODE_AUTO = "auto"`, `FLASH_MODE_ON = "on"`, `FLASH_MODE_OFF = "off"`.

### Shared DTO

`MilestoneData` (`@Parcelize`):

| Field | Type | Notes |
|---|---|---|
| `id` | `String` | Stable id from Room |
| `title` | `String` | |
| `description` | `String?` | |
| `date` | `Long` | Epoch milliseconds |
| `type` | `String` | Application-defined; not enumerated in `:shared` |

Serialised as a JSON list via Gson (`com.google.code.gson:gson:2.10.1`, declared in both `:wear` and `:shared`).

## Outputs

The contract itself produces no outputs — its outputs are the **side effects** observed at each side's listener (see [Side Effects](#side-effects) below). The helper API in `WearableHelper` is documented under [Callers](#callers).

## Validation

There is **no schema validation** at the wire layer:

- Payload bytes for fire-and-forget messages are interpreted positionally by the receiving handler. Unknown paths are simply ignored by the listener service (the `when` in `MobileWearableListenerService.onMessageReceived` falls through with no `else`).
- DataMap reads use defensive defaults (`getInt(key, 0)`, `getString(key) ?: return`) inside `WearableListenerService.handleMilestoneSync`.
- `WearableHelper.parseMilestones` wraps Gson deserialization in `try/catch` and returns `emptyList()` on **any** exception — a malformed payload is indistinguishable from an empty list at the call site.
- Payload size is bounded by the platform: the Wearable Data Layer caps individual `DataItem` assets at ~100 KB; large milestone JSON payloads should be reviewed against this ceiling.

If the schema needs to evolve, add an explicit version key (e.g. `KEY_SCHEMA_VERSION`) rather than relying on Gson's lenient parsing.

## Auth / Security

- Pairing/identity is delegated to the platform Wearable Data Layer; only paired companion devices can deliver messages.
- The wear `WearableListenerService` is `android:exported="true"` (required by the Wearable runtime). The mobile counterpart is registered the same way. The `tools:ignore="ExportedService"` annotation in [`wear/src/main/AndroidManifest.xml`](../../../wear/src/main/AndroidManifest.xml) is intentional.
- The wear app declares `<meta-data android:name="com.google.android.wearable.standalone" android:value="false" />` — it **requires** a phone companion and will not function standalone.
- Required permissions on the wear side ([`wear/src/main/AndroidManifest.xml`](../../../wear/src/main/AndroidManifest.xml)):
  - `android.permission.WAKE_LOCK` — required by Wearable Data Layer.
  - `android.permission.RECORD_AUDIO` — for the audio recording flow.
- No additional cryptographic protections are layered on top of the Data Layer; sensitive milestone content travels in plaintext over the platform channel. See [`security-and-risk.md`](../security-and-risk.md) "Wearable Data Layer payloads" row and [`audit-report/05-mobile-wear-integration.md`](../../../audit-report/05-mobile-wear-integration.md).

## Callers

The shared helper [`util/WearableHelper.kt`](../../../shared/src/main/java/com/shelbeely/opentransition/shared/util/WearableHelper.kt) is the recommended entry point. Callers should prefer it over assembling raw `PutDataMapRequest` instances:

| Function | Sends | Notes |
|---|---|---|
| `sendPhotoTrigger(messageClient, nodeId, photoType)` | `MessageClient` → `PATH_TRIGGER_PHOTO` | Body is `photoType.toByteArray()` (UTF-8) |
| `sendSyncRequest(messageClient, nodeId)` | `MessageClient` → `PATH_REQUEST_SYNC` | Empty body |
| `syncMilestones(dataClient, milestones)` | `DataClient.putDataItem` → `DATA_PATH_MILESTONES` | Sets `KEY_MILESTONE_DATA` (Gson JSON), `KEY_MILESTONE_COUNT`, `KEY_LAST_SYNC = System.currentTimeMillis()`; calls `setUrgent()` on the request |
| `parseMilestones(jsonString)` | n/a (parser) | Returns `emptyList()` on any deserialization exception |

Other callers found in the tree:

- Wear: `MainActivity`, `CameraControlActivity`, `AudioRecordActivity` (UI sources of camera and audio messages).
- Mobile: `MobileWearableListenerService` (inbound dispatch).

## Side Effects

### Wear side ([`WearableListenerService`](../../../wear/src/main/java/com/shelbeely/opentransition/wear/WearableListenerService.kt))

- `DATA_PATH_MILESTONES` arrives → JSON is persisted to `SharedPreferences("wear_cache").edit().putString("milestones", …)` so a freshly opened `MainActivity` shows fresh data; then a local broadcast `ACTION_MILESTONE_UPDATED` (`"com.shelbeely.opentransition.wear.MILESTONE_UPDATED"`) is sent so any foreground UI can refresh.
- `DATA_PATH_SETTINGS` arrives → log only (handler reserved).
- `PATH_MILESTONE_UPDATE` arrives → broadcasts `ACTION_MILESTONE_UPDATED`.
- `onCapabilityChanged` → log only.

### Mobile side ([`MobileWearableListenerService`](../../../mobile/src/main/java/com/shelbeely/opentransition/wear/MobileWearableListenerService.kt))

Handles by path:

- `PATH_TRIGGER_PHOTO` → `handlePhotoTrigger(messageEvent.data)`
- `PATH_REQUEST_SYNC` → `handleSyncRequest(messageEvent.sourceNodeId)`
- `PATH_CAMERA_SHUTTER` / `PATH_CAMERA_ZOOM` / `PATH_CAMERA_FLASH` / `PATH_CAMERA_SWITCH` → camera control handlers
- Channel paths starting with `PATH_AUDIO_DATA` → audio file ingest (filename appended after the `/`)

## Related Tests

- [`shared/src/test/java/com/shelbeely/opentransition/shared/util/WearableHelperTest.kt`](../../../shared/src/test/java/com/shelbeely/opentransition/shared/util/WearableHelperTest.kt) — JVM unit test for the helper.
- Run with: `./gradlew :shared:testDebugUnitTest`.
- No instrumented tests for the Listener services exist as of this pass; if you add one, document it here.

## Evidence

- [`shared/src/main/java/com/shelbeely/opentransition/shared/WearableConstants.kt`](../../../shared/src/main/java/com/shelbeely/opentransition/shared/WearableConstants.kt)
- [`shared/src/main/java/com/shelbeely/opentransition/shared/models/MilestoneData.kt`](../../../shared/src/main/java/com/shelbeely/opentransition/shared/models/MilestoneData.kt)
- [`shared/src/main/java/com/shelbeely/opentransition/shared/util/WearableHelper.kt`](../../../shared/src/main/java/com/shelbeely/opentransition/shared/util/WearableHelper.kt)
- [`shared/src/test/java/com/shelbeely/opentransition/shared/util/WearableHelperTest.kt`](../../../shared/src/test/java/com/shelbeely/opentransition/shared/util/WearableHelperTest.kt)
- [`wear/src/main/java/com/shelbeely/opentransition/wear/WearableListenerService.kt`](../../../wear/src/main/java/com/shelbeely/opentransition/wear/WearableListenerService.kt)
- [`mobile/src/main/java/com/shelbeely/opentransition/wear/MobileWearableListenerService.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/wear/MobileWearableListenerService.kt)
- [`wear/src/main/AndroidManifest.xml`](../../../wear/src/main/AndroidManifest.xml)
- [`shared/build.gradle`](../../../shared/build.gradle), [`wear/build.gradle`](../../../wear/build.gradle)

## Change Notes for Agents

- **Never rename a `PATH_*` or `KEY_*` constant in isolation.** Both apps use the same constants from `:shared`, but a stale build of either side in the field will silently mismatch. Treat the values as a wire format.
- **Adding a new path under `/opentransition` is safe** — the wear `AndroidManifest.xml` intent filter uses a path-prefix match. Adding a path **outside** `/opentransition` requires a manifest change.
- **Audio channel path scheme** is `PATH_AUDIO_DATA + "/<filename>"`. Keep the leading slash and avoid filename characters that could confuse path parsing.
- **`parseMilestones` swallows exceptions.** If you change the schema, introduce a versioning key.
- Update this page **and** the source in the same PR. Then refresh [`_state/coverage.md`](../_state/coverage.md).
