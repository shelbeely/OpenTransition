# Contributing Guidelines

Thank you for considering contributing to OpenTransition! 🌈

## How to Contribute

### Reporting Bugs

1. Search [existing issues](https://github.com/shelbeely/OpenTransition/issues) to avoid duplicates
2. Create a new issue using the bug report template
3. Include:
   - Android version and device model
   - Steps to reproduce
   - Expected vs actual behaviour
   - Logcat output if applicable

### Suggesting Features

1. Check [existing issues](https://github.com/shelbeely/OpenTransition/issues) for similar ideas
2. Create a new issue using the feature request template
3. Describe the use case, not just the solution

### Contributing Code

#### 1. Fork and clone

```bash
git clone https://github.com/YOUR_USERNAME/OpenTransition.git
cd OpenTransition
```

#### 2. Set up your development environment

Follow the [Development Setup](../getting-started/development-setup.md) guide.

#### 3. Branch naming

Use descriptive branch names:

| Type | Pattern | Example |
|------|---------|---------|
| Feature | `feature/short-description` | `feature/audio-export` |
| Bug fix | `fix/short-description` | `fix/gallery-crash-on-rotate` |
| Documentation | `docs/short-description` | `docs/update-readme` |
| Chore/refactor | `chore/short-description` | `chore/upgrade-camerax` |

#### 4. Make your changes

- Follow the [Code Style](../development/code-style.md) guidelines
- Keep changes focused — one concern per PR
- Add or update tests where applicable
- Run lint and unit tests before pushing:
  ```bash
  ./gradlew :mobile:lintDebug
  ./gradlew :mobile:testDebugUnitTest
  ```

#### 5. Commit messages

Use the [Conventional Commits](https://www.conventionalcommits.org/) format:

```
<type>: <short summary>

[optional body]
```

Types: `feat`, `fix`, `docs`, `chore`, `refactor`, `test`, `ci`

Examples:
```
feat: add audio export to gallery
fix: prevent crash when gallery loads empty state
docs: expand import-export feature page
```

#### 6. Open a Pull Request

- Target the `main` branch
- Fill in the PR template completely
- Link any related issues with `Closes #123`
- Request a review from a maintainer

## What We're Looking For

- Bug fixes and stability improvements
- Documentation improvements
- Test coverage improvements
- Accessibility improvements
- UI/UX refinements consistent with the existing XML Views approach

## What We're Not Looking For (Right Now)

- Jetpack Compose migration — the app uses XML Views + ViewBinding by design
- New DI frameworks — dependencies are passed manually
- KAPT processors — use KSP for any new annotation processing
- Breaking changes to the `.ttbackup` import format — backwards compatibility must be maintained

## Code Review Process

1. Automated checks run (lint, unit tests, debug build)
2. A maintainer reviews the code
3. Feedback is provided as PR comments
4. Address feedback and push updated commits
5. Maintainer approves and merges

## Note on AI-Assisted Development

This repository uses GitHub Copilot coding agent for some development tasks. AI-generated changes go through the same PR review process. If you see a PR from `copilot-swe-agent[bot]`, treat it like any other contribution and review it carefully.

## Questions?

Open an issue with the `question` label or check the [FAQ](../user-guide/faq.md).

---

**Thank you for helping make transition tracking more accessible and private!** 🏳️‍⚧️
