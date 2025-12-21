# AI Agent Skills

This directory contains AI agent skills for GitHub Copilot and Claude. Skills are folders of instructions, scripts, and resources that AI agents can discover and use to perform specialized tasks.

## Directory Structure

Skills are organized by source and category:

- **`.curated/`** - Curated skills from OpenAI (GitHub automation, Notion integration)
- **`.experimental/`** - Experimental skills from OpenAI (planning, Linear)
- **`.system/`** - System skills from OpenAI (skill creation and installation)
- **`.anthropic/`** - Skills from Anthropic (documents, creative, development, enterprise)

## OpenAI Skills

### Curated (`.curated/`)
- **gh-address-comments** - Address PR review comments efficiently
- **gh-fix-ci** - Debug and fix failing GitHub Actions CI/CD
- **notion-knowledge-capture** - Capture conversations into structured Notion pages
- **notion-meeting-intelligence** - Prepare meeting materials with context
- **notion-research-documentation** - Research and synthesize documentation
- **notion-spec-to-implementation** - Turn specs into implementation plans

### Experimental (`.experimental/`)
- **create-plan** - Create concise plans for coding tasks
- **linear** - Manage issues and workflows in Linear

### System (`.system/`)
- **skill-creator** - Guide for creating new skills
- **skill-installer** - Install skills from GitHub repositories

## Anthropic Skills (`.anthropic/`)

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

Example: "Use the gh-fix-ci skill to debug the failing CI pipeline"

## Sources

- [OpenAI Skills](https://github.com/openai/skills) - Apache 2.0 / MIT License
- [Anthropic Skills](https://github.com/anthropics/skills) - Apache 2.0 License (most), Source-available (docx/pdf/pptx/xlsx)

See `THIRD_PARTY_NOTICES.md` for complete license information.
