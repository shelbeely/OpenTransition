# AI Agent Skills

This directory contains AI agent skills for GitHub Copilot and Claude. Skills are folders of instructions, scripts, and resources that AI agents can discover and use to perform specialized tasks.

Skills in this directory are at `.github/skills/`, which is one of the [supported project skill locations](https://docs.github.com/en/copilot/how-tos/copilot-on-github/customize-copilot/customize-cloud-agent/add-skills) for GitHub Copilot cloud agent, GitHub Copilot CLI, and agent mode in VS Code.

## Layout

Per the [Add skills](https://docs.github.com/en/copilot/how-tos/copilot-on-github/customize-copilot/customize-cloud-agent/add-skills) docs, each skill must live as a **direct child** of `.github/skills/`, with a `SKILL.md` file whose `name:` frontmatter matches the directory name:

```
.github/skills/
├── <skill-name>/
│   └── SKILL.md
└── …
```

Category subfolders are not supported by Copilot's discovery, so all skills here are flat.

## Skills

### Android (from [`android/skills`](https://github.com/android/skills), Apache 2.0 — see [`ANDROID_LICENSE.txt`](ANDROID_LICENSE.txt))

- [`edge-to-edge`](edge-to-edge/SKILL.md) — Migrate a Jetpack Compose app to add adaptive edge-to-edge support
- [`navigation-3`](navigation-3/SKILL.md) — Install and migrate to Jetpack Navigation 3 (deep links, multiple backstacks, scenes, architecture)
- [`migrate-xml-views-to-jetpack-compose`](migrate-xml-views-to-jetpack-compose/SKILL.md) — Structured workflow for migrating Android XML Views to Jetpack Compose
- [`r8-analyzer`](r8-analyzer/SKILL.md) — Analyze R8/ProGuard keep rules to identify redundancies and optimize app size
- [`play-billing-library-version-upgrade`](play-billing-library-version-upgrade/SKILL.md) — Upgrade an Android project to the latest Google Play Billing Library version

See also [`androidx-compose-material3-release-guard.md`](androidx-compose-material3-release-guard.md) for Material 3 release guidance.

### Curated (from [`openai/skills`](https://github.com/openai/skills))

- [`gh-address-comments`](gh-address-comments/SKILL.md) — Address PR review comments efficiently
- [`gh-fix-ci`](gh-fix-ci/SKILL.md) — Debug and fix failing GitHub Actions CI/CD
- [`notion-knowledge-capture`](notion-knowledge-capture/SKILL.md) — Capture conversations into structured Notion pages
- [`notion-meeting-intelligence`](notion-meeting-intelligence/SKILL.md) — Prepare meeting materials with context
- [`notion-research-documentation`](notion-research-documentation/SKILL.md) — Research and synthesize documentation
- [`notion-spec-to-implementation`](notion-spec-to-implementation/SKILL.md) — Turn specs into implementation plans
- [`repo-knowledge-base`](repo-knowledge-base/SKILL.md) — Build and maintain `docs/repo-kb/` for this repo

### Experimental

- [`create-plan`](create-plan/SKILL.md) — Create concise plans for coding tasks
- [`linear`](linear/SKILL.md) — Manage issues and workflows in Linear

### System

- [`skill-creator`](skill-creator/SKILL.md) — Guide for creating effective skills
- [`skill-installer`](skill-installer/SKILL.md) — Install skills from GitHub repositories

### Anthropic (from [`anthropics/skills`](https://github.com/anthropics/skills) — see [`ANTHROPIC_README.md`](ANTHROPIC_README.md), [`ANTHROPIC_THIRD_PARTY_NOTICES.md`](ANTHROPIC_THIRD_PARTY_NOTICES.md))

Creative & Design:
- [`algorithmic-art`](algorithmic-art/SKILL.md), [`canvas-design`](canvas-design/SKILL.md), [`frontend-design`](frontend-design/SKILL.md), [`slack-gif-creator`](slack-gif-creator/SKILL.md), [`theme-factory`](theme-factory/SKILL.md)

Development & Technical:
- [`mcp-builder`](mcp-builder/SKILL.md), [`web-artifacts-builder`](web-artifacts-builder/SKILL.md), [`webapp-testing`](webapp-testing/SKILL.md)

Documents:
- [`docx`](docx/SKILL.md), [`pdf`](pdf/SKILL.md), [`pptx`](pptx/SKILL.md), [`xlsx`](xlsx/SKILL.md)

Enterprise & Communication:
- [`brand-guidelines`](brand-guidelines/SKILL.md), [`doc-coauthoring`](doc-coauthoring/SKILL.md), [`internal-comms`](internal-comms/SKILL.md)

### Project skills (this repo)

- [`android-accessibility`](android-accessibility/SKILL.md), [`android-architecture`](android-architecture/SKILL.md), [`android-coroutines`](android-coroutines/SKILL.md), [`android-data-layer`](android-data-layer/SKILL.md), [`android-emulator-skill`](android-emulator-skill/SKILL.md), [`android-gradle-logic`](android-gradle-logic/SKILL.md), [`android-retrofit`](android-retrofit/SKILL.md), [`android-testing`](android-testing/SKILL.md), [`android-viewmodel`](android-viewmodel/SKILL.md)
- [`coil-compose`](coil-compose/SKILL.md), [`compose-navigation`](compose-navigation/SKILL.md), [`compose-performance-audit`](compose-performance-audit/SKILL.md), [`compose-ui`](compose-ui/SKILL.md)
- [`gradle-build-performance`](gradle-build-performance/SKILL.md), [`kotlin-concurrency-expert`](kotlin-concurrency-expert/SKILL.md)
- [`rxjava-to-coroutines-migration`](rxjava-to-coroutines-migration/SKILL.md), [`xml-to-compose-migration`](xml-to-compose-migration/SKILL.md)

## Usage

Skills are automatically discovered by AI assistants when working in this repository. Simply mention the skill or task, and the AI will use the appropriate skill if available.

Examples:
- "Use the gh-fix-ci skill to debug the failing CI pipeline"
- "Use the edge-to-edge skill to make the app UI edge-to-edge"

## Sources & licensing

- [Android Skills](https://github.com/android/skills) — Apache 2.0 (see [`ANDROID_LICENSE.txt`](ANDROID_LICENSE.txt))
- [OpenAI Skills](https://github.com/openai/skills) — Apache 2.0 / MIT
- [Anthropic Skills](https://github.com/anthropics/skills) — Apache 2.0 (most); source-available for `docx`, `pdf`, `pptx`, `xlsx` (see [`ANTHROPIC_THIRD_PARTY_NOTICES.md`](ANTHROPIC_THIRD_PARTY_NOTICES.md))
