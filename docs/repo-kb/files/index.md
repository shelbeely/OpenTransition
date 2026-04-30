# Files

> **Status: pass-1 placeholder.** No per-file pages exist yet.

When you next deep-dive a particular file, create a page here using the file template in [`.github/skills/curated/repo-knowledge-base/SKILL.md`](../../../.github/skills/curated/repo-knowledge-base/SKILL.md) §"Fourth Pass: File and Module Documentation".

Suggested first targets (unblock other agents):

| Path | Why |
|---|---|
| `mobile/src/main/AndroidManifest.xml` | Declared activities, services, permissions, intents |
| `mobile/src/main/java/com/shelbeely/opentransition/Application.kt` (or equivalent) | App entry point and DI graph (such as it is — no DI framework) |
| `wear/src/main/java/com/shelbeely/opentransition/wear/MainActivity.kt` | Wear UI entry point |
| `wear/src/main/java/com/shelbeely/opentransition/wear/AudioRecordActivity.kt` | Wear-side audio recording UI; producer of `PATH_AUDIO_*` traffic |
| `wear/src/main/java/com/shelbeely/opentransition/wear/CameraControlActivity.kt` | Wear-side remote camera UI; producer of `PATH_CAMERA_*` traffic |
| `wear/src/main/java/com/shelbeely/opentransition/wear/WearableListenerService.kt` | Inbound side of mobile→wear communication. Already summarised in [`apis/wearable-data-layer.md`](../apis/wearable-data-layer.md) |
| `mobile/src/main/java/com/shelbeely/opentransition/wear/MobileWearableListenerService.kt` | Inbound side of wear→mobile communication. Already summarised in [`apis/wearable-data-layer.md`](../apis/wearable-data-layer.md) |
| `shared/src/main/java/com/shelbeely/opentransition/shared/util/WearableHelper.kt` | Wire helper API. Already summarised in [`apis/wearable-data-layer.md`](../apis/wearable-data-layer.md) |
