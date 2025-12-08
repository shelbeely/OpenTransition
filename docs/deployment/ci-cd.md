# CI/CD

## GitHub Actions

The project uses GitHub Actions for:
- Building and testing
- Release deployment
- Documentation publishing

See `.github/workflows/` for workflow definitions.

## Documentation Deployment

The documentation is automatically deployed to GitHub Pages using the `deploy-docs.yml` workflow.

### Initial Setup

To enable documentation deployment, you need to configure GitHub Pages in the repository settings:

1. Go to **Settings** → **Pages** in the repository
2. Under **Build and deployment**, set:
   - **Source**: GitHub Actions
3. Save the settings

The workflow will automatically deploy documentation when:
- Changes are pushed to the `main` or `production` branches that affect:
  - Files in the `docs/` directory
  - The `mkdocs.yml` configuration file
  - The workflow file itself
- The workflow is manually triggered via workflow_dispatch

### Workflow Details

The deployment workflow consists of two jobs:

1. **Build Job**:
   - Checks out the repository
   - Sets up Python
   - Installs MkDocs and dependencies
   - Builds the documentation site
   - Uploads the built site as an artifact

2. **Deploy Job**:
   - Downloads the artifact from the build job
   - Deploys to GitHub Pages using the official GitHub Pages action
   - Provides the deployed site URL in the workflow output

The documentation will be available at: `https://shelbeely.github.io/OpenTransition/`
