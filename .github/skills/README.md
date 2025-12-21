# AI Agent Skills for OpenTransition

This directory contains AI agent skills for GitHub Copilot and Claude. Skills are folders of instructions, scripts, and resources that AI agents can discover and use to perform specialized tasks.

## About Agent Skills

Agent Skills follow the [Agent Skills standard](http://agentskills.io) and are automatically available when using GitHub Copilot or Claude with this repository. Each skill is self-contained in its own folder with a `SKILL.md` file containing YAML frontmatter and instructions.

For more information:
- [GitHub Copilot Agent Skills](https://docs.github.com/en/copilot/concepts/agents/about-agent-skills)
- [Anthropic Claude Skills](https://support.claude.com/en/articles/12512176-what-are-skills)
- [Agent Skills Standard](http://agentskills.io)

## Available Skills (26 total)

### GitHub & Development
- **gh-address-comments** - Address PR review comments efficiently
- **gh-fix-ci** - Debug and fix failing GitHub Actions CI/CD
- **create-plan** - Create concise plans for coding tasks
- **linear** - Manage issues and workflows in Linear
- **mcp-builder** - Create MCP (Model Context Protocol) servers
- **webapp-testing** - Test web applications with Playwright

### Notion Integration
- **notion-knowledge-capture** - Capture conversations into structured Notion pages
- **notion-meeting-intelligence** - Prepare meeting materials with context
- **notion-research-documentation** - Research and synthesize documentation
- **notion-spec-to-implementation** - Turn specs into implementation plans

### Document & Office
- **docx** - Document creation, editing, and analysis
- **pdf** - PDF manipulation toolkit
- **pptx** - Presentation creation and editing
- **xlsx** - Spreadsheet creation and analysis

### Creative & Design
- **algorithmic-art** - Create algorithmic art with p5.js
- **canvas-design** - Create beautiful visual art in PNG/PDF
- **frontend-design** - Create production-grade frontend interfaces
- **slack-gif-creator** - Create animated GIFs for Slack
- **theme-factory** - Style artifacts with themes
- **web-artifacts-builder** - Create complex web artifacts with React

### Enterprise & Communication
- **brand-guidelines** - Apply brand colors and typography
- **doc-coauthoring** - Guide for co-authoring documentation
- **internal-comms** - Write internal communications

### Skill Management
- **skill-creator** - Guide for creating new skills (OpenAI version)
- **skill-creator-anthropic** - Guide for creating new skills (Anthropic version)
- **skill-installer** - Install skills from GitHub repositories

## Sources

Skills in this directory come from:
- [OpenAI Skills](https://github.com/openai/skills) - Apache 2.0 / MIT License
- [Anthropic Skills](https://github.com/anthropics/skills) - Apache 2.0 License (most), Source-available (docx/pdf/pptx/xlsx)

See `THIRD_PARTY_NOTICES.md` for complete license information.

## Usage

These skills are automatically discovered by AI assistants when working in this repository. Simply mention the skill or task, and the AI will use the appropriate skill if available.

Example: "Use the gh-fix-ci skill to debug the failing CI pipeline"

## Creating Custom Skills

Skills are simple to create - just a folder with a `SKILL.md` file:

```markdown
---
name: my-skill-name
description: A clear description of what this skill does and when to use it
---

# My Skill Name

[Instructions that the AI will follow when this skill is active]

## Examples
- Example usage 1
- Example usage 2
```

For more details, see the `skill-creator` or `skill-creator-anthropic` skills in this directory.
