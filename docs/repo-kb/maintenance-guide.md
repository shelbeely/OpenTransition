# Maintenance Guide

How to keep `docs/repo-kb/` accurate as the repo evolves. The skill that built it lives at [`.github/skills/curated/repo-knowledge-base/SKILL.md`](../../.github/skills/curated/repo-knowledge-base/SKILL.md).

## Refresh Triggers

Update the matching KB page **in the same pull request** when you change:

| You changed… | Update… |
|---|---|
| Module boundaries, deps between modules, or a new module | [`architecture.md`](./architecture.md), [`repo-map.md`](./repo-map.md) |
| `compileSdk` / `minSdk` / `targetSdk` / Kotlin / AGP / a major library version | [`architecture.md`](./architecture.md) → "Build & SDK Targets" + [`build-and-release.md`](./build-and-release.md) |
| A `./gradlew` command that agents should run | [`quickstart-for-agents.md`](./quickstart-for-agents.md), [`build-and-release.md`](./build-and-release.md), [`testing.md`](./testing.md) |
| A `.github/workflows/*.yml` file (added, removed, or changed trigger) | [`workflows/index.md`](./workflows/index.md) and the per-workflow page under `workflows/` |
| Environment variables, secrets, or signing config | [`configuration.md`](./configuration.md) |
| ProGuard/R8, Crashlytics on/off, or any security-sensitive default | [`security-and-risk.md`](./security-and-risk.md) |
| A new feature visible to users | Add a page under `features/` and link it from `features/index.md` |
| A new public API, route, or wear message path | Add a page under `apis/` and link it from `apis/index.md` |
| A new Room entity, schema migration, or shared model | [`data/index.md`](./data/index.md) (and a per-table page when useful) |

## Refresh Process

1. Make the code change.
2. Update the relevant KB page(s) above.
3. Update [`_state/coverage.md`](./_state/coverage.md) if coverage status changed (Missing → Partial → Complete or back).
4. Append to [`_state/session-log.md`](./_state/session-log.md) with what changed and what was validated.
5. If a documented command's behavior changed, update [`_state/command-log.md`](./_state/command-log.md).
6. If you discover something the KB asserts but the source contradicts: **trust the source, fix the KB**, and note it in the session log.

## When to Run a Full Refresh Pass

Re-run the [`repo-knowledge-base`](../../.github/skills/curated/repo-knowledge-base/SKILL.md) skill end-to-end when:

- The KB has not been touched in many PRs.
- A large refactor (new module, framework swap, major upgrade) lands.
- An audit (`audit-report/`) is refreshed.

Use this prompt:

```txt
Use the repo-knowledge-base skill. Refresh docs/repo-kb against the current source code. Check for stale architecture notes, commands, config, tests, CI/CD workflows, APIs, and feature docs. Update .github/copilot-instructions.md, .github/instructions/repo-kb.instructions.md, and AGENTS.md only if needed. Record changes in coverage and session-log.
```

## Anti-Stale Discipline

- Never paste large source files into KB pages — summarize and link.
- Never mark coverage Complete unless it actually is.
- Never invent behavior — file an entry in [`_state/unknowns.md`](./_state/unknowns.md) instead.
- Prefer many small focused commits over a single huge KB rewrite.
- Preserve existing rules in [`.github/copilot-instructions.md`](../../.github/copilot-instructions.md) and [`AGENTS.md`](../../AGENTS.md) when merging KB pointers in.

## Editing Inside `docs/repo-kb/`

A path-scoped editing instruction file lives at [`.github/instructions/repo-kb.instructions.md`](../../.github/instructions/repo-kb.instructions.md). VS Code agent mode and the Copilot CLI will pick it up automatically when editing files under `docs/repo-kb/**`.
