# Agent Instructions

This repository uses [`docs/repo-kb/`](docs/repo-kb/index.md) as its durable knowledge base, built and maintained with the [`repo-knowledge-base`](.github/skills/curated/repo-knowledge-base/SKILL.md) agent skill.

Before making non-trivial changes, read in this order:

1. [`docs/repo-kb/index.md`](docs/repo-kb/index.md)
2. [`docs/repo-kb/quickstart-for-agents.md`](docs/repo-kb/quickstart-for-agents.md)
3. [`docs/repo-kb/architecture.md`](docs/repo-kb/architecture.md)
4. [`docs/repo-kb/_state/coverage.md`](docs/repo-kb/_state/coverage.md)
5. [`docs/repo-kb/_state/unknowns.md`](docs/repo-kb/_state/unknowns.md)

Then read any relevant page under:

- [`docs/repo-kb/features/`](docs/repo-kb/features/index.md)
- [`docs/repo-kb/components/`](docs/repo-kb/components/index.md)
- [`docs/repo-kb/apis/`](docs/repo-kb/apis/index.md)
- [`docs/repo-kb/data/`](docs/repo-kb/data/index.md)
- [`docs/repo-kb/workflows/`](docs/repo-kb/workflows/index.md)
- [`docs/repo-kb/files/`](docs/repo-kb/files/index.md)

Use the knowledge base to find the right files, commands, risks, and validation steps before searching broadly.

The source code is the final authority. If the knowledge base is stale or incomplete, update it in the same change. See [`docs/repo-kb/maintenance-guide.md`](docs/repo-kb/maintenance-guide.md) for the refresh checklist.

At the end of a meaningful agent session, append a short note to:

```
docs/repo-kb/_state/session-log.md
```

Include what changed, what was validated, what failed, and what future agents should do next.

For full repo-wide rules (environment bootstrap, hard don'ts, build/test commands), also read [`.github/copilot-instructions.md`](.github/copilot-instructions.md).
