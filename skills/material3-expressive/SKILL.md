---
name: material3-expressive
description: Build, review, and refactor Android UIs using Material 3 Expressive with Jetpack Compose. Use for expressive theming, motion, typography, adaptive layout, component selection, and M3E compliance audits.
user-invokable: true
argument-hint: [component|screen|theme|motion|audit] [description or file path]
---

# Material 3 Expressive

Use this skill when the user wants:
- Material 3 Expressive UI
- Jetpack Compose screens that feel modern, emotional, playful, or highly polished
- Material You / dynamic color support
- expressive motion, typography, shapes, and adaptive layout
- a review or audit for M3 Expressive alignment

## What this skill does

This skill helps the agent:
1. Choose the right Material 3 Expressive patterns for Android UI work.
2. Generate Compose code using Material 3 components and theme tokens.
3. Refactor overly flat or generic Material 3 screens into more expressive ones.
4. Audit a screen or codebase for M3 Expressive compliance.
5. Keep accessibility, responsiveness, and implementation realism intact.

## Scope

Primary target:
- Android
- Jetpack Compose
- `androidx.compose.material3`

Secondary target:
- Design specs, token systems, and implementation notes that may later inform other platforms

Do not:
- assume full web parity
- invent components that do not exist
- use XML for newly generated UI unless the user explicitly asks
- hardcode random colors when theme roles should be used
- create visual chaos and call it "expressive"

## Core principles

Material 3 Expressive should feel:
- emotionally engaging
- visually clear
- adaptive across screen sizes
- motion-aware
- still accessible and system-coherent

Expressive does **not** mean:
- cluttered
- noisy
- decorative without hierarchy
- excessive blur, gradients, or glass effects
- random oversized corners with no system logic

## Default implementation stance

Unless the user says otherwise:
- use Jetpack Compose
- use `MaterialTheme`
- prefer theme roles over raw colors
- support dark theme
- support dynamic color when appropriate
- use adaptive layout patterns for larger screens
- preserve strong accessibility defaults
- keep motion tasteful and purposeful

## Decision tree

### If the user asks for a component
- Build a single expressive component
- Use Material 3 tokens and shape roles
- Make state handling explicit
- Keep the composable stateless when possible

### If the user asks for a screen
- Build a complete screen with:
  - app bar or top-level structure
  - content hierarchy
  - states: loading, empty, populated, error
  - responsive spacing
  - expressive type and shape choices
  - preview(s)

### If the user asks for theming
- Define or refine:
  - color scheme
  - typography
  - shapes
  - motion intent
- Prefer token-driven theming over one-off styling

### If the user asks for review or audit
Inspect for:
- token misuse
- weak hierarchy
- flat or generic typography
- missing expressive shape logic
- poor motion choices
- accessibility issues
- adaptive layout gaps
- over-customization that breaks Material coherence

## Required design rules

### 1) Color
- Prefer `MaterialTheme.colorScheme.*`
- Use semantic roles, not arbitrary hex values
- Keep accent usage intentional
- Preserve contrast and readability
- Use dynamic color when the product benefits from system personalization

### 2) Typography
- Use Material typography roles
- Create stronger hierarchy through role choice, spacing, and emphasis
- Prefer expressive scale and emphasis over random font size inflation
- Keep body text readable and stable
- Avoid making every header huge or bold

### 3) Shape
- Use shape intentionally to show hierarchy and personality
- Prefer rounded and expanded shape systems where appropriate
- Use larger shapes for key surfaces and actions
- Keep shape choices systematic across the screen
- Do not mix unrelated corner styles without a reason

### 4) Motion
- Motion should reinforce structure, focus, and delight
- Animate state changes, emphasis shifts, container transitions, and key interactions
- Avoid constant looping motion unless it serves a clear UX role
- Prefer soft, responsive, spring-like behavior
- Keep reduced-motion concerns in mind

### 5) Layout
- Build with responsive Compose layout primitives
- Respect window size changes
- Avoid phone-only layouts when the screen could expand
- Use clear grouping, breathing room, and rhythm
- Make focal actions visually obvious

### 6) Components
Prefer official Material 3 components first:
- buttons
- cards
- app bars
- text fields
- FABs
- navigation components
- bottom sheets
- lists
- tabs
- chips
- progress and feedback surfaces

Only build custom components when:
- the user explicitly needs one
- official components cannot support the pattern
- the result still feels like Material 3 Expressive

## Compose implementation rules

### Structure
- Prefer small composables with clear responsibilities
- Hoist state
- Keep business logic out of UI composables
- Use previews for major states
- Use sealed UI state or similar explicit state models where practical

### Theming
Use:
- `MaterialTheme(colorScheme = ..., typography = ..., shapes = ...)`

Prefer:
- `lightColorScheme(...)`
- `darkColorScheme(...)`
- dynamic color integration when appropriate

### Accessibility
Always check:
- touch target size
- color contrast
- semantic labels where needed
- content descriptions for non-decorative icons/images
- screen reader flow
- not relying on color alone

## Output modes

### `component`
Return:
- composable code
- state parameters
- preview
- brief explanation of expressive choices

### `screen`
Return:
- complete screen composable
- supporting models/state if needed
- previews for at least 2–4 states
- note on theming and layout behavior

### `theme`
Return:
- theme setup
- color/typography/shape recommendations
- dynamic color note
- migration notes if replacing a flatter MD3 theme

### `motion`
Return:
- interaction points worth animating
- Compose animation approach
- why motion helps hierarchy or feedback

### `audit`
Return:
- score out of 100
- findings grouped by severity
- concrete fixes
- code-level recommendations
- "quick wins" and "high impact" sections

## Audit rubric

Score these areas:
1. Theme token usage
2. Typography hierarchy
3. Shape consistency
4. Color semantics
5. Motion quality
6. Component appropriateness
7. Layout rhythm and spacing
8. Adaptive behavior
9. Accessibility
10. Overall expressive coherence

### Severity labels
- Critical
- Important
- Nice-to-have

## Default coding style

Generate Kotlin Compose code that is:
- production-leaning
- easy to read
- previewable
- realistic
- not overloaded with comments
- not dependent on fake libraries

## When the request is vague

If the user says things like:
- "make it more Material"
- "make it more modern"
- "make it feel like Android 16"
- "make it expressive"

Then:
1. infer a reasonable M3 Expressive direction
2. improve hierarchy, shape, spacing, and motion
3. keep the result grounded in Material 3
4. explain the biggest changes briefly

## Anti-patterns

Avoid:
- giant blobs of UI in one composable
- hardcoded spacing everywhere with no system rhythm
- random colors outside the theme
- excessive custom drawing when standard Material components fit
- making everything high-emphasis
- lifeless flat screens with no hierarchy
- fake "expressive" styling that breaks accessibility

## Recommended companion references

Use alongside:
- a Compose best-practices skill
- an accessibility skill
- an adaptive layout skill
- a motion/animation skill
- screenshot testing or design QA tooling

## Example invocations

- `/material3-expressive component Build an expressive settings row with icon, supporting text, and trailing switch`
- `/material3-expressive screen Create a finance dashboard home screen with strong hierarchy and adaptive layout`
- `/material3-expressive theme Refactor this app theme to feel more expressive while keeping brand purple`
- `/material3-expressive motion Suggest motion upgrades for this task completion flow`
- `/material3-expressive audit app/src/main/java/com/example/ui/HomeScreen.kt`

## Response contract

When using this skill, the agent must:
1. Prefer official Material 3 Compose patterns.
2. Keep recommendations implementable.
3. Explain expressive choices briefly.
4. Flag when a request would leave Material territory.
5. Prioritize Android/Compose correctness over trendy styling.
