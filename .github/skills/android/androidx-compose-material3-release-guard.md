---
name: androidx-compose-material3-release-guard
description: Verifies current AndroidX Compose Material3 release notes before changing, upgrading, or recommending Material3 APIs, dependencies, components, expressive APIs, icons, or migration code.
---

# AndroidX Compose Material3 Release Guard

Use this skill whenever working on Android projects that use or mention:

- `androidx.compose.material3`
- `androidx.compose.material3:material3`
- `androidx.compose.material3:material3-window-size-class`
- `androidx.compose.material3:material3-adaptive-navigation-suite`
- Material 3
- Material 3 Expressive
- Compose Material components
- Compose themes, typography, color schemes, buttons, sheets, search bars, top app bars, carousels, chips, sliders, dialogs, navigation, or icons

This skill prevents stale Compose Material3 guidance.

Do not rely on model memory for Material3 APIs or versions. Always verify the current release notes before making implementation decisions.

## Canonical source

Primary release notes:

```text
https://developer.android.com/jetpack/androidx/releases/compose-material3
```

Related sources to check when relevant:

```text
https://developer.android.com/jetpack/androidx/releases/compose-material3-adaptive
https://developer.android.com/jetpack/androidx/releases/wear-compose-material3
https://developer.android.com/develop/ui/compose/designsystems/material3
https://developer.android.com/develop/ui/compose/graphics/images/material
https://developer.android.com/jetpack/androidx/releases/compose-kotlin
```

## Required behavior

Before editing code, dependencies, docs, or recommendations involving Compose Material3:

1. Open the canonical AndroidX Compose Material3 release notes.
2. Identify the current:
   - latest update date
   - stable release
   - alpha release
   - beta release, if any
   - release candidate, if any
3. Inspect the project's current dependency versions.
4. Determine whether the requested API is:
   - stable
   - experimental
   - alpha-only
   - deprecated
   - renamed
   - removed
   - moved to another artifact
5. Choose the safest valid implementation for the project's current release channel.
6. Include a short validation note in the final response.

## Current known checkpoint

This checkpoint is not a substitute for live verification.

As of the AndroidX release notes checked on 2026-04-28:

```text
Latest update: 2026-04-22
Stable release: 1.4.0
Alpha release: 1.5.0-alpha18
```

Always re-check the release notes because these values can change.

## Dependency inspection

Inspect these files before changing versions:

```text
settings.gradle
settings.gradle.kts
build.gradle
build.gradle.kts
app/build.gradle
app/build.gradle.kts
gradle/libs.versions.toml
```

Search for:

```text
androidx.compose.material3:material3
androidx.compose.material3:material3-window-size-class
androidx.compose.material3:material3-adaptive-navigation-suite
androidx.compose.material3.adaptive
androidx.wear.compose:compose-material3
androidx.compose:compose-bom
```

If the project uses a Compose BOM, determine whether Material3 is controlled by the BOM or pinned separately.

Do not blindly add a newer Material3 version if the project is intentionally using a BOM.

## Version policy

Prefer stable releases unless the task explicitly requires alpha-only APIs.

Use this decision table:

| Situation | Decision |
|---|---|
| Production app | Prefer latest stable Material3 |
| App already uses stable Material3 | Do not introduce alpha-only APIs without explaining the tradeoff |
| App already uses alpha Material3 | Stay within the same alpha line unless upgrading is requested |
| User asks for Material3 Expressive | Check whether the API requires alpha or a different artifact |
| Wear OS app using Material 3 Expressive | Use Wear Compose Material3 instead of regular Compose Material3 |
| API unavailable in current project version | State the minimum version needed |
| API renamed or removed | Provide a migration path |
| Dependency conflict appears | Check Compose BOM and Compose/Kotlin compatibility before editing |

## Wear OS rule

For Wear OS apps using Material 3 Expressive, use the Wear Compose Material3 library instead of the normal phone/tablet Compose Material3 library.

Do not use regular `androidx.compose.material3:material3` as the main Material3 Expressive library for Wear OS UI work.

## Icon rule

Do not assume Material3 brings in `material-icons-core`.

Check whether the project depends on icons directly.

Prefer Material Symbols vector drawables from the Android tab of Google Fonts Icons when appropriate.

Only add `material-icons-core` or `material-icons-extended` when the project explicitly needs the Compose icon library and the tradeoff is acceptable.

## Experimental API rule

Only add:

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
```

when the current release notes or API reference require it.

Do not remove existing opt-ins unless verifying that the API has been promoted to stable in the project's actual Material3 version.

## High-risk API areas

Verify these especially carefully:

- Material 3 Expressive APIs
- `SearchBar`
- `SearchBarState`
- `TopSearchBar`
- `ExpandedDockedSearchBar`
- `ModalBottomSheet`
- `BottomSheetScaffold`
- `TopAppBarScrollBehavior`
- `PullToRefresh`
- `WideNavigationRail`
- `NavigationSuiteScaffold`
- `Carousel`
- `HorizontalMultiBrowseCarousel`
- `SecureTextField`
- `TextField`
- `Slider`
- `RangeSlider`
- `LinearProgressIndicator`
- `CircularProgressIndicator`
- `MotionScheme`
- `FloatingToolbar`
- `Tooltip`
- Material icons and Material Symbols

## Required final note

When completing a task, include this block:

```text
Material3 release check:
- Project version:
- Latest stable:
- Latest alpha:
- API status:
- Decision:
```

Example:

```text
Material3 release check:
- Project version: 1.4.0
- Latest stable: 1.4.0
- Latest alpha: 1.5.0-alpha18
- API status: Requested API is stable in the project version
- Decision: Use stable Material3 APIs and avoid alpha-only Expressive APIs
```

If the release notes cannot be accessed, say:

```text
I could not verify the current AndroidX Compose Material3 release notes, so this recommendation may be stale.
```

## Migration behavior

When changing code because an API changed:

1. Name the old API or pattern.
2. Name the current replacement.
3. State the minimum Material3 version required.
4. Show the replacement code.
5. Say whether the replacement is stable or experimental.
6. Avoid silent migrations.

## Dependency examples

Kotlin DSL:

```kotlin
dependencies {
    implementation("androidx.compose.material3:material3:<verified-version>")
    implementation("androidx.compose.material3:material3-window-size-class:<verified-version>")
}
```

Version catalog:

```toml
[versions]
composeMaterial3 = "<verified-version>"

[libraries]
androidx-compose-material3 = { module = "androidx.compose.material3:material3", version.ref = "composeMaterial3" }
androidx-compose-material3-window-size-class = { module = "androidx.compose.material3:material3-window-size-class", version.ref = "composeMaterial3" }
```

Do not copy these versions blindly. Replace `<verified-version>` only after checking the release notes and the project's dependency strategy.

## Refusal to guess

If an API cannot be verified against the current release notes, do not invent it.

Instead:

- state what was checked
- state what could not be verified
- recommend the nearest stable alternative
- leave a clear TODO comment if code must be added later
