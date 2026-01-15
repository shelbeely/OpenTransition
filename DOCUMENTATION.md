# OpenTransition Documentation

This file provides an overview of the documentation structure for the OpenTransition project.

## 📚 Viewing the Documentation

**Live Documentation**: Once deployed, the documentation will be available at:
https://shelbeely.github.io/OpenTransition

**Local Preview**: To view documentation locally:
```bash
pip install mkdocs mkdocs-material
mkdocs serve
# Visit http://127.0.0.1:8000
```

## 📁 Documentation Structure

The documentation is organized into several main sections:

### 🎨 Customization Guides (For Rebranding)
Located in the root directory

- **IMAGE_ASSETS_TO_REPLACE.md** - Complete checklist of all image files that need replacing
- **LOGO_REPLACEMENT_GUIDE.md** - Step-by-step instructions for replacing logos, icons, and branding

**Audience**: Developers who want to rebrand/customize the app with their own visual identity

### 🎯 User Guide (For End Users)
Located in `docs/user-guide/`

- **Getting Started** - Setup and first steps
- **Features Overview** - What the app can do
- **FAQ** - Frequently asked questions (40+ Q&A)
- **Photos, Milestones, Privacy, Backup** - Topic-specific guides

**Audience**: Transgender individuals using the app to track their transition

### 🛒 Play Store Content
Located in `docs/play-store/`

- **App Description** - Complete Play Store listing (ready to copy)
- **Feature Highlights** - Marketing content
- **Screenshot Guidelines** - Instructions for store assets

**Audience**: App store listing, marketing materials

### 👨‍💻 Developer Guide
Located in `docs/getting-started/`, `docs/architecture/`, `docs/development/`

- **Getting Started** - Development environment setup
- **Architecture** - App structure and design patterns
- **Development** - Code standards, testing, debugging
- **Features** - Technical feature documentation
- **Contributing** - How to contribute
- **Deployment** - Release and CI/CD processes

**Audience**: Developers contributing to the project

## 🔄 Keeping Documentation Updated

### Automated Deployment

Documentation automatically deploys to GitHub Pages when:
- Changes are pushed to `main` or `production` branches
- Changes are made to `docs/` or `mkdocs.yml`
- Workflow: `.github/workflows/deploy-docs.yml`

### Documentation Agent

The `.github/agents/documentation.md` file provides guidelines for AI agents and contributors to ensure documentation stays up-to-date when code changes.

**Key principle**: Update documentation in the same PR as code changes.

## 📝 Documentation Guidelines

### For User-Facing Content
- Use plain, simple language
- Avoid technical jargon
- Include step-by-step instructions
- Add screenshots where helpful
- Be empathetic and supportive

### For Developer Content
- Include code examples
- Explain architecture decisions
- Document APIs and interfaces
- Provide troubleshooting guides
- Link related documentation

### General Standards
- Use Markdown formatting
- Include relative links between docs
- Add admonitions for important notes (`!!! warning`, `!!! tip`)
- Use code blocks with language specifiers
- Keep pages focused on single topics

## 🛠️ MkDocs Configuration

**File**: `mkdocs.yml`

Key features enabled:
- Material theme with pink/purple color scheme
- Dark/light mode toggle
- Search functionality
- Code syntax highlighting
- Tabbed content
- Navigation with sections
- Emoji support

## 📊 Documentation Stats

- **Total Pages**: 35+ markdown files
- **Sections**: 7 main categories
- **User Content**: ~20,000 words
- **Developer Content**: ~15,000 words
- **Play Store Ready**: Yes, complete app description

## 🎨 Customization

### Themes
The documentation theme colors (pink/purple) match the app's default theme.

### Navigation
Navigation structure is defined in `mkdocs.yml` under the `nav:` section.

### Content
All documentation content is in `docs/` directory as Markdown files.

## 🚀 Quick Commands

```bash
# Install dependencies
pip install mkdocs mkdocs-material mkdocstrings[python]

# Serve locally
mkdocs serve

# Build documentation
mkdocs build

# Deploy to GitHub Pages (manual)
mkdocs gh-deploy

# Check for errors
mkdocs build --strict
```

## 📦 What's Included

### Complete Documentation
- ✅ User getting started guide
- ✅ Comprehensive FAQ (40+ questions)
- ✅ Play Store description (ready to use)
- ✅ Developer setup instructions
- ✅ Architecture overview
- ✅ Data layer documentation
- ✅ Build and deployment guides

### Placeholder Pages (To Be Expanded)
Some pages are stubs with basic information that can be expanded:
- UI Layer details
- Domain Layer details
- Navigation flow diagrams
- Feature implementation details
- Testing procedures
- Contributing workflows

## 🤝 Contributing to Documentation

1. Edit markdown files in `docs/` directory
2. Test locally with `mkdocs serve`
3. Submit PR with documentation changes
4. Documentation updates automatically deploy when merged

## 📞 Support

- **Documentation Issues**: Create GitHub issue with `documentation` label
- **Content Questions**: Check FAQ first, then create issue
- **Suggestions**: Pull requests welcome!

---

**Remember**: Good documentation is as important as good code! Keep it updated! 📚
