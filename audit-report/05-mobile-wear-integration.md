# 05 — Mobile ↔ Wear Integration

> **Verdict:** the contract documented in `ARCHITECTURE.md` is *not* implemented in code. The Wear app
> appears to work in isolation but cannot find the phone, and the phone cannot push milestone updates
> to the watch. **Integration rating: 2 / 10.**

## 1. Communication channels in use

| Channel | Used? | Where | Purpose |
|---------|-------|-------|---------|
| `MessageClient` | ✅ | wear → mobile commands (photo trigger, camera shutter/zoom/flash/switch, sync request); mobile → wear `PATH_MILESTONE_UPDATE` (declared, never sent in code). | One-shot commands. |
| `DataClient` | ✅ (one direction working) | wear → mobile audio bytes (`DATA_PATH_AUDIO`); mobile → wear milestones (`DATA_PATH_MILESTONES`) — but the producer call is **a `Log.d` line**, see `MobileWearableListenerService.kt:101-106`. | Synced state. |
| `CapabilityClient` | ✅ called, ❌ never registered | Both apps query `CAPABILITY_MOBILE_APP` / `CAPABILITY_WEAR_APP`; **no app declares these capabilities**, so queries return zero nodes. | Peer discovery. |
| `ChannelClient` | ❌ | — | Best-fit for large audio files (currently shoehorned into `DataClient`, see ISSUE-010). |
| `NodeClient` | ❌ | — | Could be a fallback for "is *any* peer reachable" when capabilities aren't honoured. |

## 2. Capability declarations — root-cause of integration failure

🔴 **CRITICAL.** The Data Layer requires capabilities to be declared either in `res/values/wear.xml`
(`<string-array name="android_wear_capabilities">`) or programmatically via
`Wearable.getCapabilityClient(this).addLocalCapability("opentransition_xxx_app")` in `Application.onCreate`.

```
$ grep -rn "android_wear_capabilities\|addLocalCapability" mobile/ wear/ shared/
(no matches)
$ find . -name "wear.xml"
(no matches)
```

Yet the code calls `capabilityClient.getCapability(WearableConstants.CAPABILITY_MOBILE_APP, FILTER_REACHABLE)`
in 5 different locations:

| File | Line | Effect when capability is unregistered |
|------|------:|----------------------------------------|
| `wear/.../MainActivity.kt` | 102, 122, 142 | "Phone Disconnected", buttons disabled |
| `wear/.../CameraControlActivity.kt` | 110, 127 | All camera commands silently fail |
| `wear/.../AudioRecordActivity.kt` | 173 | Audio is recorded then dropped |

This single missing piece is **the largest issue in the entire repo** by impact. Fix is one resource
file (`wear/src/main/res/values/wear.xml` declaring `opentransition_wear_app`, plus `mobile/.../wear.xml`
declaring `opentransition_mobile_app`).

## 3. Data payload schemas — versioning & back-compat

`shared/.../models/MilestoneData.kt` (the only schema):

```kotlin
@Parcelize
data class MilestoneData(
    val id: String,
    val title: String,
    val description: String?,
    val date: Long,
    val type: String
) : Parcelable
```

| Concern | Status |
|---------|--------|
| Schema-version field | ❌ none |
| Forward-compat (drop unknown fields) | ⚠️ Gson default tolerates missing fields, but `parseMilestones` swallows ALL exceptions → silent corruption (`shared/.../WearableHelper.kt:73-79`). |
| Backward-compat | ❌ if mobile sends a renamed field, watch shows empty list (parseMilestones returns `emptyList()`). |
| Schema definition | Single Kotlin file in `:shared` — both sides see the same compile-time schema. ✅ |
| Cross-version pairing (newer mobile, older wear) | 🔴 No story. The watch app could be uninstalled/re-installed after the mobile version moves on. |

🟠 **Recommendation**: add an `int schemaVersion` to every payload's `DataMap`/JSON, and refuse parses
of `schemaVersion > KNOWN_MAX`.

## 4. Serialization

- **Gson 2.13.1** (mobile) / **2.10.1** (wear) — note version drift. (`mobile/build.gradle:222-223`,
  `wear/build.gradle:64`.) 🟡
- **No `kotlinx.serialization`**, no Protobuf, no Moshi.
- The audio data uses raw byte arrays placed in a `DataMap.putByteArray(...)` — efficient but constrained
  by the ~100 KB DataItem soft limit.
- `MilestoneData` is `@Parcelize` (kotlin-parcelize plugin) — but the actual sync uses Gson over a
  `String` field, not Parcelable. The `@Parcelize` annotation is dead. 🟡

## 5. Sync conflict resolution, offline queue, retry

| Concern | Status |
|---------|--------|
| Conflict resolution | ❌ none. `syncMilestones` does a full overwrite via `DataClient.putDataItem(...)`. |
| Offline queue | ❌ none. Failed `sendMessage` (e.g. peer disconnected) is dropped — no retry, no persistence. |
| Retry | ❌ none. `addOnFailureListener { runOnUiThread { Toast(...) } }` is the only failure handling visible. |
| Idempotency | ✅ DataItem updates are inherently idempotent (Play Services handles deltas). |
| Last-write-wins clock | The mobile is implicit producer; no concept of vector clocks or hybrid logical clocks needed at this scale. |

## 6. Pairing / handshake / node discovery edge cases

Documented in `ARCHITECTURE.md:115-121`:

> **Capabilities Registered:** `opentransition_mobile_app` - Registered by mobile app

… but never actually registered anywhere. The mobile app's `MobileWearableListenerService` is declared
exported with the right `<intent-filter>` for GMS (`mobile/AndroidManifest.xml:191-204`), so messages
*sent to it* by node ID would be received — but the wear app cannot get a node ID without capability
discovery.

🟠 **Edge case not handled**: multiple paired wear devices (e.g. user has two watches). The code uses
`nodes.first().id` (`wear/.../MainActivity.kt:106, 126`); the "primary" watch is not picked
deterministically.

## 7. Authentication token sharing

❌ **No tokens are shared between devices.** Firebase Auth state lives on the phone only; the wear app
has no Firebase setup. This is correct — the Wear app does no network — but if wear features ever needed
identity, there's no story for distributing it.

## 8. Install / uninstall lifecycle

| Scenario | Handling |
|----------|----------|
| Phone installed, watch missing | Mobile UI is unaffected; `MobileWearableListenerService` is registered but receives nothing. ✅ |
| Watch installed, phone missing or app uninstalled | Watch shows "Phone Disconnected" toast. 🟢 visible feedback. |
| Phone reinstalled (capability list refreshes) | `onCapabilityChanged` is wired in `wear/.../MainActivity.kt:233-235` and triggers `checkMobileAppConnection()`. ✅ — but moot until capability is registered (ISSUE-001). |
| `WearableListenerService` cold-start when phone sends data while watch app dead | Service should start automatically per GMS; handlers `handleMilestoneSync/handleSettingsSync/handleMilestoneUpdate` are empty — data is silently dropped. 🔴 ISSUE-007. |

## 9. Shared modules — actually shared or duplicated?

(See `02-architecture-assessment.md §3`.) Realistically, **only path constants and one tiny model are
shared**. The audio handling, milestone serialization, and even the Gson instance are duplicated
informally because `WearableHelper.kt` does too little.

| Concept | In `:shared`? | Should be? |
|---------|---------------|------------|
| `MilestoneData` model | ✅ | ✅ |
| Path / capability strings | ✅ | ✅ |
| `WearableHelper` send/parse | ✅ | ✅ |
| Audio data envelope (filename, timestamp, dateKey, autoSent) | ❌ duplicated as ad-hoc strings on both sides | ✅ should be a typed shared model |
| Camera control values (`FLASH_MODE_AUTO/ON/OFF`) | ✅ in constants | ✅ |
| Theme palettes | ❌ duplicated hex codes | ✅ |

## 10. Error handling when peer is disconnected

| Surface | Handling |
|---------|----------|
| Wear `MainActivity.sendPhotoTriggerMessage` | `if (nodes.isNotEmpty()) ... else Toast(R.string.disconnected)` ✅ |
| Wear `CameraControlActivity.sendCameraCommand` | Same pattern, but **no `addOnFailureListener` on the `getCapability` call itself** — if Play Services is unavailable, nothing happens at all, no toast. 🟠 |
| Wear `AudioRecordActivity.sendAudioToPhone` | Same, plus an `addOnFailureListener` on the put itself ✅. |
| Mobile listener handlers | Most are empty bodies, so disconnection at the moment a message lands silently drops it. 🔴 |

## 11. Risks table

| ID | Risk | Severity | Likelihood | Mitigation direction |
|----|------|---------:|-----------:|----------------------|
| R-1 | Capability never registered → entire integration broken | 🔴 | Certain | Add `wear.xml` resource on both sides. |
| R-2 | Mobile→Wear milestone sync is a `Log.d` placeholder | 🔴 | Certain | Wire `WearableHelper.syncMilestones` from `handleSyncRequest` and from milestone CRUD callbacks. |
| R-3 | Audio file > ~100 KB silently fails via DataItem | 🟠 | High (any recording > ~30s) | Use `ChannelClient` for files. |
| R-4 | Schema drift: no version field in payloads | 🟠 | Medium-Low (single dev today) | Add `schemaVersion` int. |
| R-5 | `parseMilestones` swallows all exceptions | 🟡 | Medium | Log + re-throw or fall back to empty cache (currently silent). |
| R-6 | Mobile listener writes audio to **unencrypted** Realm bypassing `DatabaseManager` | 🟠 | Certain (every audio sync) | Route through `DatabaseManager.getDatabase().photoDao()`. |
| R-7 | Mobile listener service is `exported=true` and trusts all incoming `MessageEvent` data without source verification | 🟡 | Low (GMS gates the path) | Validate `messageEvent.sourceNodeId` against known nodes. |
| R-8 | Two-watch scenario picks `nodes.first()` non-deterministically | 🟡 | Low | Track preferred node in SharedPreferences. |
| R-9 | Gson version drift across `:mobile` (2.13.1) and `:wear` (2.10.1) | 🟢 | Low | Unify via `:shared`'s `api` declaration. |

## 12. Integration rating

| Sub-criterion | Score |
|---------------|------:|
| Discoverability (capabilities work) | 1 |
| Bidirectional sync (data flows both ways) | 2 (only wear→mobile audio works; mobile→wear milestones is a TODO) |
| Robustness (retry, offline, errors) | 2 |
| Schema versioning | 2 |
| Documentation accuracy | 3 (docs describe the intended state, not the implemented state) |
| Code sharing between apps | 5 |
| **Overall integration** | **2 / 10** |

The good news is that the *infrastructure* (services, paths, serialization helpers, listener wiring) is
all in place. Roughly one engineer-week of focused work can bring this from 2/10 to 6/10 by addressing
ISSUE-001 (capabilities), ISSUE-003 (mobile→wear sync), ISSUE-007 (empty listener bodies), and ISSUE-010
(audio via ChannelClient).
