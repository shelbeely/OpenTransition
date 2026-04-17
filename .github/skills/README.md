# AI Agent Skills

This directory contains AI agent skills for GitHub Copilot and Claude. Skills are folders of instructions, scripts, and resources that AI agents can discover and use to perform specialized tasks.

Skills in this directory are at `.github/skills/`, which is one of the [supported project skill locations](https://docs.github.com/en/copilot/concepts/agents/about-agent-skills) for GitHub Copilot cloud agent, GitHub Copilot CLI, and agent mode in VS Code.

## Directory Structure

Skills are organized by source and category:

- **`android/`** - Official Android skills from Google (build, navigation, system, performance, Jetpack Compose, Play)
- **`curated/`** - Curated skills from OpenAI (GitHub automation, Notion integration)
- **`experimental/`** - Experimental skills from OpenAI (planning, Linear)
- **`system/`** - System skills from OpenAI (skill creation and installation)
- **`anthropic/`** - Skills from Anthropic (documents, creative, development, enterprise)

## Android Skills (`android/`)

Official skills from the [Android skills GitHub repository](https://github.com/android/skills) — licensed under Apache 2.0. See `android/LICENSE.txt`.

### Build
- **agp-9-upgrade** (`build/agp/agp-9-upgrade`) - Upgrade or migrate an Android project to Android Gradle Plugin 9

### Navigation
- **navigation-3** (`navigation/navigation-3`) - Install and migrate to Jetpack Navigation 3, including deep links, multiple backstacks, scenes, and architecture patterns

### System
- **edge-to-edge** (`system/edge-to-edge`) - Migrate a Jetpack Compose app to add adaptive edge-to-edge support

### Performance
- **r8-analyzer** (`performance/r8-analyzer`) - Analyze R8/ProGuard keep rules to identify redundancies and optimize app size

### Jetpack Compose
- **migrate-xml-views-to-jetpack-compose** (`jetpack-compose/migration/migrate-xml-views-to-jetpack-compose`) - Structured workflow for migrating Android XML Views to Jetpack Compose

### Play
- **play-billing-library-version-upgrade** (`play/play-billing-library-version-upgrade`) - Upgrade an Android project to the latest Google Play Billing Library version

## OpenAI Skills

### Curated (`curated/`)
- **gh-address-comments** - Address PR review comments efficiently
- **gh-fix-ci** - Debug and fix failing GitHub Actions CI/CD
- **notion-knowledge-capture** - Capture conversations into structured Notion pages
- **notion-meeting-intelligence** - Prepare meeting materials with context
- **notion-research-documentation** - Research and synthesize documentation
- **notion-spec-to-implementation** - Turn specs into implementation plans

### Experimental (`experimental/`)
- **create-plan** - Create concise plans for coding tasks
- **linear** - Manage issues and workflows in Linear

### System (`system/`)
- **skill-creator** - Guide for creating new skills
- **skill-installer** - Install skills from GitHub repositories

## Anthropic Skills (`anthropic/`)

### Creative & Design
- **algorithmic-art** - Create algorithmic art with p5.js
- **canvas-design** - Create beautiful visual art in PNG/PDF
- **frontend-design** - Create production-grade frontend interfaces
- **slack-gif-creator** - Create animated GIFs for Slack
- **theme-factory** - Style artifacts with themes

### Development & Technical
- **mcp-builder** - Create MCP (Model Context Protocol) servers
- **web-artifacts-builder** - Create complex web artifacts with React
- **webapp-testing** - Test web applications with Playwright

### Document Skills
- **docx** - Document creation, editing, and analysis
- **pdf** - PDF manipulation toolkit
- **pptx** - Presentation creation and editing
- **xlsx** - Spreadsheet creation and analysis

### Enterprise & Communication
- **brand-guidelines** - Apply brand colors and typography
- **doc-coauthoring** - Guide for co-authoring documentation
- **internal-comms** - Write internal communications
- **skill-creator** - Guide for creating effective skills

## Usage

Skills are automatically discovered by AI assistants when working in this repository. Simply mention the skill or task, and the AI will use the appropriate skill if available.

Examples:
- "Use the gh-fix-ci skill to debug the failing CI pipeline"
- "Use the agp-9-upgrade skill to migrate this project to AGP 9"
- "Make the app UI edge-to-edge"

## Sources

- [Android Skills](https://github.com/android/skills) - Apache 2.0 License
- [OpenAI Skills](https://github.com/openai/skills) - Apache 2.0 / MIT License
- [Anthropic Skills](https://github.com/anthropics/skills) - Apache 2.0 License (most), Source-available (docx/pdf/pptx/xlsx)

See `THIRD_PARTY_NOTICES.md` for complete license information.
