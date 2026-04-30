# Components

> **Status: pass-1 placeholder.** Per-component pages are not yet written.

The component graph (Compose composables, Activities/Fragments, ViewModels, services) lives under:

- `mobile/src/main/java/com/shelbeely/opentransition/ui/`
- `mobile/src/main/java/com/shelbeely/opentransition/background/`
- `mobile/src/main/java/com/shelbeely/opentransition/wear/`
- `wear/src/main/java/com/shelbeely/opentransition/wear/`

For per-package documentation guidance see the directory-level template in [`.github/skills/curated/repo-knowledge-base/SKILL.md`](../../../.github/skills/curated/repo-knowledge-base/SKILL.md) §"Fourth Pass: File and Module Documentation".

## Suggested first targets

1. The Activity/Fragment hosting Compose content (entry point of the UI migration).
2. ViewModels for milestone list and gallery.
3. The CameraX + ML Kit pipeline.
4. The Wear `MainActivity` and `WearableListenerService`.
