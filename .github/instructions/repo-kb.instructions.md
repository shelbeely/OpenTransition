---
applyTo: "docs/repo-kb/**"
---

# Repo Knowledge Base Editing Instructions

These files are the OpenTransition repository knowledge base, built with the [`repo-knowledge-base`](../skills/curated/repo-knowledge-base/SKILL.md) agent skill.

Keep them factual, grounded, and useful for future Copilot Cloud Agent, Copilot CLI, and IDE agent sessions.

## Rules

- Do not invent implementation details.
- Prefer links to exact repo paths.
- Keep pages skimmable.
- Update `docs/repo-kb/_state/coverage.md` when coverage changes.
- Update `docs/repo-kb/_state/session-log.md` after meaningful documentation work.
- Put uncertain findings in `docs/repo-kb/_state/unknowns.md` or under `docs/repo-kb/questions/`.
- If code and docs disagree, update the docs to match the code.
- Do not paste large source files into KB pages.
- Summarize large files by purpose, key symbols, inputs, outputs, dependencies, tests, and risks.
- Preserve command results, failures, and workarounds in `docs/repo-kb/_state/command-log.md`.
- Never expose real secrets — document variable names and purpose only.

## Page Quality Checklist

A good KB page answers:

- What is this?
- Why does it matter?
- Where is the source?
- What depends on it?
- What does it depend on?
- How do you test or validate it?
- What should future agents avoid breaking?
- What is still unknown?

## When in doubt

Open `docs/repo-kb/maintenance-guide.md` for the refresh process and `.github/skills/curated/repo-knowledge-base/SKILL.md` for the canonical templates and pass structure.
