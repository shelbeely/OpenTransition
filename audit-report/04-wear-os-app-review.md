# 04 — Wear OS App Review

## Per-feature rating table

| Feature area | Status | Rating | Notes |
|--------------|--------|------:|-------|
| **Wear Compose** | Declared, never invoked | **1** | `WearTheme.kt` is dead code. Activities use XML+`findViewById`. |
| **Tiles** | None | **0** | No `TileService` anywhere. |
| **Complications** | None | **0** | No `ComplicationDataSourceService`. |
| **Ongoing Activity** | None | **0** | No `OngoingActivity` API usage. |
| **Watch face integration** | None | **0** | Out of scope by design (see `MONOREPO.md`). |
| **Rotary input / bezel** | Not handled | **2** | No `onRotaryScrollEvent`, no `RotaryEventDispatcher`. |
| **Ambient mode / always-on** | Not supported | **2** | `Activity` does not implement `AmbientModeSupport.AmbientCallbackProvider`; uses `WAKE_LOCK` permission but no actual wake lock acquired. |
| **Health Services / Health Connect** | Not used | **N/A** | Out of scope. |
| **Wear navigation** | None | **2** | Plain `startActivity(Intent)`; no `SwipeDismissableNavHost`. |
| **Standalone vs tethered** | `standalone=false` (tethered) | **5** | Honest declaration matches behaviour. |
| **Permissions (`RECORD_AUDIO`)** | Runtime requested | **5** | `BODY_SENSORS` not used (good — not needed). |
| **Screen size / shape adaptability** | XML layouts only | **3** | No `BoxInsetLayout` / `WearableRecyclerView`; no round/square awareness. |
| **Companion data sync (mobile→wear)** | Listener handlers are empty bodies | **2** | `WearableListenerService.kt:45-65` are placeholders. |

**Overall Wear app rating: 3 / 10.**

## 1. Wear Compose vs legacy Wearable Support Library

**Verdict: this app is essentially a legacy Wear OS 1.x app dressed in modern dependencies.**

`wear/build.gradle` declares:
```
androidx.compose.ui:ui:1.7.8
androidx.wear.compose:compose-material:1.4.1
androidx.wear:wear:1.3.0
com.google.android.support:wearable:2.9.0   // legacy support library
```

But the production code is exclusively:
```kotlin
class MainActivity : Activity(), DataClient.OnDataChangedListener, ...
override fun onCreate(savedInstanceState: Bundle?) {
    setContentView(R.layout.activity_main)         // XML View inflation
    triggerPhotoButton = findViewById(R.id.trigger_photo_button)
}
```
(`wear/src/main/java/com/shelbeely/opentransition/wear/MainActivity.kt:29-57`.)

🔴 **`WearTheme.kt`** (183 LOC, `wear/.../theme/WearTheme.kt`) defines four `Colors` palettes and a
typography scale for `androidx.wear.compose.material.MaterialTheme`. Searching `grep -rn "WearTheme\|setContent"
wear/` confirms there is **no caller** anywhere. It is entirely dead code.

🟡 The legacy `com.google.android.support:wearable:2.9.0` is included alongside `androidx.wear:wear:1.3.0`,
which is the modern equivalent. Pick one.

## 2. Tiles, Complications, Ongoing Activity

❌ **None present.** A "true Wear OS companion" in 2026 should at minimum have:

- A **Tile** showing the milestone count and last milestone date.
- A **Complication** so users can put days-since-start on their watch face.
- An **Ongoing Activity** during audio recording so the recording is reachable from anywhere on the
  device.

The current app forces the user to launch a full-screen activity for everything. This violates
modern Wear OS UX guidelines.

## 3. Rotary input / bezel

❌ Zero handling. Rotary is the primary discoverable input on Wear OS 3+. The seekbar at
`wear/.../CameraControlActivity.kt:83-96` uses `SeekBar.OnSeekBarChangeListener` (touch-only). On a
Pixel Watch or Galaxy Watch with a rotating bezel, the user cannot turn the bezel to change zoom.

🟠 **Recommendation**: at minimum implement `View.setOnGenericMotionListener` for `MotionEvent.AXIS_SCROLL`
on the zoom seekbar (one-block API call).

## 4. Ambient mode / always-on display

`AndroidManifest.xml:18` declares `android.permission.WAKE_LOCK` but no wake lock is acquired anywhere
in source (`grep -rn "WakeLock\|newWakeLock" wear/` → 0). The audio-record screen will sleep mid-record
under default settings, terminating the recording.

🟠 The standard pattern is to:
1. Declare `<uses-permission android:name="android.permission.WAKE_LOCK"/>` ✅ (already done).
2. Implement `AmbientModeSupport.AmbientCallbackProvider` on the recording activity.
3. Acquire a `PowerManager.PARTIAL_WAKE_LOCK` for the duration of `MediaRecorder` recording.

None of these is done.

## 5. Power and battery

| Risk | Status | Notes |
|------|--------|------|
| Wake locks | None acquired (above). 🟠 |
| Sensor / location | ❌ Not used. ✅ (good — wear-specific privacy win) |
| Data sync frequency | Manual only via "Sync" button + capability change → `requestSync()`. 🟢 |
| Tile refresh | N/A — no tile. |
| `MediaRecorder` cleanup | Released in `stopRecording()` and in `onDestroy()` ✅ — correct. (`wear/.../AudioRecordActivity.kt:134-155, 243-247`) |
| Background `Service` | None other than `WearableListenerService` (which GMS lifecycles). 🟢 |
| Polling loop | `durationText.postDelayed({ updateDuration() }, 1000)` recursion is **not cancelled when activity is paused/destroyed**. Could leak a `Handler` callback. 🟠 (`wear/.../AudioRecordActivity.kt:157-162`) |

**Battery profile**: low risk overall because the app does so little autonomously. The main threats
are (a) the recording duration loop, and (b) running `MediaRecorder` without ambient handling.

## 6. Health Services / Health Connect

❌ Not used and **shouldn't be** — the app's purpose (transition tracking) doesn't intersect with
real-time biometric streams. ✅ This is the correct posture.

## 7. Wear-specific navigation

❌ No `SwipeDismissableNavHost`. ❌ No `ScalingLazyColumn`. The activities are all linearly chained
with `startActivity(Intent)`. Users can only navigate "back" via the system back gesture; there is no
edge-swipe-to-dismiss because the activities don't host a `SwipeDismissFrameLayout`.

🟠 This is a Wear OS UX violation. Adding `SwipeDismissFrameLayout` as the root of each XML layout is
a one-tag fix per file.

## 8. Standalone vs tethered

`wear/src/main/AndroidManifest.xml:35-37`:
```xml
<meta-data android:name="com.google.android.wearable.standalone" android:value="false" />
```

✅ Honest. The app is tethered — every action requires the phone. The README and `MONOREPO.md` both
state this explicitly. ✅

🟡 However, even tethered apps must handle the "phone disconnected" case gracefully. The current handling
is `Toast.makeText(this, "Phone disconnected", ...)` and disabling buttons — fine for a prototype, but
no offline queue, no retry, no useful feedback to the user.

## 9. Wear permissions

| Permission | Declared | Used | OK? |
|------------|----------|------|-----|
| `WAKE_LOCK` | ✅ | ❌ (never acquired) | 🟡 declared but unused |
| `RECORD_AUDIO` | ✅ | ✅ runtime requested | ✅ |
| `BODY_SENSORS` | ❌ | n/a | ✅ correct |
| `INTERNET` | ❌ | n/a | ✅ correct (Wear app has no direct network use) |

## 10. Screen size / shape adaptability

The XML layouts under `wear/src/main/res/layout/` are plain `LinearLayout`/`ConstraintLayout`. There
are no qualifiers for `-round`, `-square`, `-w*dp`. On a small round watch, the rectangular buttons
will be clipped at the corners.

🟠 **Recommended fixes**:
- Wrap each layout in `androidx.wear.widget.BoxInsetLayout` so content respects the round inset.
- Provide `layout-round/` variants where button arrangement differs.
- Or migrate to Wear Compose with `ScalingLazyColumn` (the natural answer).

## 11. Concrete bug list (Wear-specific)

| ID (cross-ref to `07-issues-and-bugs.md`) | Severity | One-liner |
|--|--|--|
| `ISSUE-001` | 🔴 | Capability `opentransition_mobile_app` never registered → all `getCapability(...).addOnSuccessListener` callbacks see `nodes.isEmpty()` and the UI permanently shows "Phone Disconnected". |
| `ISSUE-007` | 🟠 | `WearableListenerService.handleMilestoneSync/handleSettingsSync/handleMilestoneUpdate` are **empty function bodies** — incoming data is silently dropped. (`wear/.../WearableListenerService.kt:52-64`) |
| `ISSUE-008` | 🟠 | `MainActivity.onDataChanged` *does* parse milestone data and update the cache (`wear/.../MainActivity.kt:201-220`) — but only while the activity is in the foreground (registered/unregistered in `onResume/onPause`). When not visible, the listener service handles it — but its handler is empty (see `ISSUE-007`). Net: data only updates when the user is staring at the screen. |
| `ISSUE-009` | 🟠 | `durationText.postDelayed { updateDuration() }` recursion not cancelled in `onPause`. (`wear/.../AudioRecordActivity.kt:157-162`) |
| `ISSUE-010` | 🟡 | Audio bytes (potentially MB) sent via `dataMap.putByteArray` which has size limits (~100 KB recommended, hard cap ~200 KB). Recording for more than ~30s with AMR_NB will exceed this and the `putDataItem` will silently fail. (`wear/.../AudioRecordActivity.kt:206`) — should use `ChannelClient` for files. |
| `ISSUE-011` | 🟡 | `WearableListenerService` is `android:exported="true"` (`wear/AndroidManifest.xml:60-73`); GMS requires this, but `tools:ignore="ExportedService"` is correct. ✅ However, no peer-id verification is done — see `ISSUE-014`. |

## Summary

The Wear app is best understood as a **proof-of-concept demo**: it shows the intended UX (tap-to-trigger,
camera remote, audio capture) but is built on a legacy Wear 1.x foundation, has a non-functional
peer-discovery contract, has dead Compose theming, and offers none of the modern Wear OS surfaces (Tiles,
Complications, Ongoing Activity, Rotary, Ambient). To deliver on the promises in `WEAR_APP_FEATURES.md`,
this module needs a substantial rewrite.

**Wear app overall rating: 3 / 10.**
