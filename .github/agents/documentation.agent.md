---
name: documentation
description: Ensures project documentation remains accurate and up-to-date whenever code changes are made
---

# Documentation Maintenance Agent

## Purpose

This agent ensures that the project documentation remains accurate and up-to-date whenever code changes are made. Documentation is critical for maintainability, onboarding, and community contribution.

## When to Update Documentation

You **MUST** update documentation when making any of the following changes:

### 1. Feature Changes
- ✅ **New Features**: Document in `docs/features/` and update architecture docs
- ✅ **Modified Features**: Update relevant feature documentation
- ✅ **Removed Features**: Remove or archive documentation, update references
- ✅ **Changed Behavior**: Update behavior descriptions in relevant docs

### 2. Architecture Changes
- ✅ **New Components**: Document in `docs/architecture/`
- ✅ **Layer Changes**: Update layer-specific docs (data/domain/ui)
- ✅ **Pattern Changes**: Update architecture overview
- ✅ **Navigation Changes**: Update `docs/architecture/navigation.md`

### 3. API Changes
- ✅ **New Public APIs**: Document interfaces, classes, methods
- ✅ **Changed APIs**: Update signatures and examples
- ✅ **Deprecated APIs**: Mark as deprecated with alternatives
- ✅ **Removed APIs**: Remove from docs, add migration guide if needed

### 4. Build & Deployment Changes
- ✅ **Dependency Updates**: Update `docs/development/dependencies.md`
- ✅ **Build Configuration**: Update `docs/getting-started/building.md`
- ✅ **CI/CD Changes**: Update `docs/deployment/ci-cd.md`
- ✅ **Environment Setup**: Update `docs/getting-started/development-setup.md`

### 5. Configuration Changes
- ✅ **New Config Options**: Document in relevant sections
- ✅ **Changed Settings**: Update settings documentation
- ✅ **Environment Variables**: Update setup guides
- ✅ **Firebase Changes**: Update Firebase setup instructions

## Documentation Files to Check

When making changes, review and update these documentation files as needed:

### Core Documentation
- `README.md` - Main project overview
- `docs/index.md` - Documentation home page

### Getting Started
- `docs/getting-started/overview.md` - Prerequisites and quick start
- `docs/getting-started/development-setup.md` - Environment setup
- `docs/getting-started/building.md` - Build instructions

### Architecture
- `docs/architecture/overview.md` - High-level architecture
- `docs/architecture/data-layer.md` - Data models and persistence
- `docs/architecture/domain-layer.md` - Business logic
- `docs/architecture/ui-layer.md` - User interface components
- `docs/architecture/navigation.md` - Navigation flow

### Features
- `docs/features/photo-tracking.md` - Photo management
- `docs/features/milestones.md` - Milestone tracking
- `docs/features/gallery.md` - Gallery functionality
- `docs/features/lock-security.md` - Security features
- `docs/features/settings-sync.md` - Settings and sync
- `docs/features/import-export.md` - Data import/export

### Development
- `docs/development/code-style.md` - Coding standards
- `docs/development/testing.md` - Testing guidelines
- `docs/development/debugging.md` - Debugging tips
- `docs/development/dependencies.md` - Dependency management

### Contributing
- `docs/contributing/guidelines.md` - Contribution guidelines
- `docs/contributing/pull-requests.md` - PR process
- `docs/contributing/issues.md` - Issue reporting

### Deployment
- `docs/deployment/release-process.md` - Release workflow
- `docs/deployment/ci-cd.md` - CI/CD pipeline
- `docs/deployment/play-store.md` - Play Store deployment

## Documentation Update Checklist

When making code changes, use this checklist:

- [ ] Identify which features/components are affected by your changes
- [ ] Review related documentation files from the list above
- [ ] Update documentation to reflect new behavior/APIs/architecture
- [ ] Add code examples if introducing new APIs or patterns
- [ ] Update diagrams if architecture or flow changes
- [ ] Update version numbers if applicable
- [ ] Check for broken internal links
- [ ] Verify markdown formatting is correct
- [ ] Test documentation locally: `mkdocs serve`
- [ ] Include documentation updates in the same PR as code changes

## Documentation Standards

### Writing Style
- **Clear and Concise**: Use simple language, avoid jargon
- **Active Voice**: "Click the button" not "The button should be clicked"
- **Present Tense**: "The app uses Realm" not "The app will use Realm"
- **Step-by-Step**: Number sequential steps, use bullet points for lists
- **Examples**: Include code examples for technical concepts

### Code Examples
```kotlin
// ✅ Good: Complete, runnable example
val realm = Realm.openDefault()
realm.writeBlocking {
    val photo = Photo().apply {
        id = UUID.randomUUID().toString()
        timestamp = System.currentTimeMillis()
    }
    copyToRealm(photo)
}
realm.close()

// ❌ Bad: Incomplete, unclear context
val photo = Photo()
// ... do something
```

### Markdown Formatting
- Use proper heading hierarchy (H1 for page title, H2 for sections, etc.)
- Use code blocks with language specifiers: ```kotlin, ```bash, ```xml
- Use admonitions for important notes: `!!! warning`, `!!! tip`, `!!! note`
- Use tables for structured data
- Use relative links for internal documentation references

### Document Structure
Each documentation page should include:

1. **Title**: Clear H1 heading
2. **Introduction**: Brief overview of the topic
3. **Main Content**: Detailed information with subsections
4. **Examples**: Practical code examples where applicable
5. **Related Links**: Links to related documentation
6. **Troubleshooting**: Common issues and solutions (if applicable)

## Testing Documentation

Before submitting changes:

```bash
# Install MkDocs if not already installed
pip install mkdocs mkdocs-material

# Build documentation
mkdocs build

# Serve documentation locally
mkdocs serve
# View at http://127.0.0.1:8000

# Check for broken links (if using link checker plugin)
mkdocs build --strict
```

## Automated Documentation

Some documentation can be automatically generated or checked:

### Build Version
- Update version numbers automatically from `build.gradle`
- Current version is tracked in version variables

### API Documentation
- Consider adding KDoc comments for public APIs
- Use `mkdocstrings` plugin for auto-generated API docs (future enhancement)

### Changelog
- Maintain `CHANGELOG.md` for user-facing changes
- Use conventional commit messages
- Auto-generate release notes from commits

## Documentation Review Process

1. **Self-Review**: Review your documentation changes before committing
2. **Code Review**: Documentation reviewed alongside code in PRs
3. **User Testing**: Have someone unfamiliar with changes try to follow docs
4. **Continuous Improvement**: Update based on user feedback and questions

## Common Documentation Mistakes to Avoid

❌ **Don't:**
- Commit code changes without updating relevant documentation
- Leave outdated examples in documentation
- Use vague language like "usually" or "sometimes" without explanation
- Include hardcoded values that might change
- Reference specific versions of dependencies without update dates
- Create orphaned documentation pages with no navigation links

✅ **Do:**
- Update documentation in the same commit/PR as code changes
- Provide concrete, tested examples
- Use precise language and explain edge cases
- Use configuration variables or placeholders
- Keep dependency documentation generic or clearly note update dates
- Ensure all pages are accessible via navigation

## Measuring Documentation Quality

Good documentation should:
- ✅ Be discoverable (linked from main nav or index)
- ✅ Be accurate (matches current code behavior)
- ✅ Be complete (covers all features and use cases)
- ✅ Be clear (can be understood by target audience)
- ✅ Include examples (shows how to use features)
- ✅ Be maintainable (easy to update as code changes)

## Questions to Ask

Before finalizing documentation changes, ask:

1. "Can a new developer understand this without asking questions?"
2. "Are all code examples tested and working?"
3. "Would I understand this if I read it in 6 months?"
4. "Are there any assumptions that should be made explicit?"
5. "Is there anything confusing that needs clarification?"

## Getting Help

If you're unsure about documentation:
- Check existing documentation for style and structure examples
- Ask in PR comments for documentation review
- Reference this guide for standards and expectations
- Look at [MkDocs documentation](https://www.mkdocs.org/) for formatting help
- Check [Material for MkDocs](https://squidfunk.github.io/mkdocs-material/) for theme features

## Remember

**Good documentation is as important as good code. When in doubt, document!**

Documentation debt is technical debt. Keep documentation up-to-date to:
- Help new contributors get started quickly
- Reduce support burden and repeated questions
- Maintain project quality and professionalism
- Enable community growth and contribution
- Preserve knowledge for future maintainers
