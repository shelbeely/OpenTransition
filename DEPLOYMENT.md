# Deployment Guide - GitHub Pages

This repository contains two web app implementations that can be deployed to GitHub Pages:

1. **JavaScript Web App** (`/webapp`) - Production-ready, zero build process
2. **Flutter Web App** (`/flutter_webapp`) - Production-ready, requires Flutter build

---

## 🚀 Quick Start - JavaScript Web App (Recommended)

The JavaScript web app is the **easiest to deploy** since it requires no build process.

### Option 1: Automatic Deployment (GitHub Actions)

We've included a GitHub Actions workflow that automatically deploys the JavaScript web app.

**Steps:**

1. Go to your repository **Settings** → **Pages**
2. Under **Source**, select **GitHub Actions**
3. Push changes to the `main` branch or manually trigger the workflow
4. Your app will be deployed to: `https://<username>.github.io/OpenTransition/`

The workflow file is at: `.github/workflows/deploy-webapp.yml`

### Option 2: Manual Deployment

1. Go to repository **Settings** → **Pages**
2. Under **Source**, select **Deploy from a branch**
3. Select branch: `main` (or your PR branch)
4. Select folder: `/webapp`
5. Click **Save**
6. Visit: `https://<username>.github.io/OpenTransition/`

---

## 🎯 Flutter Web App Deployment

The Flutter web app requires a build step before deployment.

### Option 1: Automatic Deployment (GitHub Actions)

We've included a GitHub Actions workflow for Flutter deployment.

**Steps:**

1. Go to repository **Settings** → **Pages**
2. Under **Source**, select **GitHub Actions**
3. The workflow will automatically build and deploy when you push to `main`
4. Your app will be deployed to: `https://<username>.github.io/OpenTransition-flutter/`

The workflow file is at: `.github/workflows/deploy-flutter.yml`

### Option 2: Manual Build & Deploy

**Prerequisites:**
- Flutter SDK installed locally
- GitHub CLI or git configured

**Steps:**

1. **Build the Flutter app:**
   ```bash
   cd flutter_webapp
   
   # First time setup
   flutter create . --platforms=web
   flutter pub get
   
   # Build for GitHub Pages
   flutter build web --release --base-href="/OpenTransition/"
   ```

2. **Deploy the build output:**
   - Copy `flutter_webapp/build/web/` contents to `gh-pages` branch
   - Or use the GitHub Actions workflow (recommended)

---

## 📝 Configuration Notes

### JavaScript Web App

**No configuration needed!** The app works out of the box on GitHub Pages.

- ✅ Works with any base path
- ✅ Service Worker for PWA functionality
- ✅ IndexedDB for data storage
- ✅ All assets are relative paths

### Flutter Web App

**Base Href Configuration:**

If deploying to a subdirectory, update the base href:

```bash
# If your repo is https://github.com/username/OpenTransition
flutter build web --release --base-href="/OpenTransition/"

# If deploying to root domain
flutter build web --release --base-href="/"
```

**Update in `flutter_webapp/web/index.html`:**
```html
<base href="/OpenTransition/">
```

---

## 🔧 GitHub Pages Settings

### Enable GitHub Pages

1. Go to **Settings** → **Pages**
2. Choose deployment method:
   - **GitHub Actions** (recommended for automatic deployment)
   - **Deploy from a branch** (simple, manual updates)

### Custom Domain (Optional)

1. Go to **Settings** → **Pages** → **Custom domain**
2. Enter your domain (e.g., `opentransition.example.com`)
3. Add a `CNAME` file to the deployment directory with your domain

---

## 🎯 Which App Should You Deploy?

### Deploy JavaScript Web App if:
- ✅ You want the **simplest deployment** (no build process)
- ✅ You only need **web support**
- ✅ You want **instant updates** (just edit files and push)
- ✅ You prefer **minimal dependencies**

### Deploy Flutter Web App if:
- ✅ You want to **share code with mobile apps** later
- ✅ You need **native-like performance**
- ✅ You prefer **strongly-typed Dart** over JavaScript
- ✅ You plan to build **Android/iOS versions**

### Deploy Both! 🎉

You can deploy both versions:
- JavaScript: `https://yourusername.github.io/OpenTransition/`
- Flutter: `https://yourusername.github.io/OpenTransition-flutter/`

Both use the same `.ttbackup` format, so users can switch between them seamlessly!

---

## 🔄 Continuous Deployment

Both workflows are configured to automatically deploy when you push changes:

- **JavaScript**: Deploys when files in `/webapp` change
- **Flutter**: Deploys when files in `/flutter_webapp` change

You can also manually trigger deployments:
1. Go to **Actions** tab
2. Select the workflow
3. Click **Run workflow**

---

## 🐛 Troubleshooting

### JavaScript App

**Issue:** App doesn't load assets
- **Solution:** Check that all paths in HTML/CSS/JS are relative (no leading `/`)

**Issue:** Service Worker not working
- **Solution:** GitHub Pages requires HTTPS, which is automatically provided

### Flutter App

**Issue:** Blank page or 404 errors
- **Solution:** Check `base href` matches your deployment path
- **Solution:** Ensure you ran `flutter create . --platforms=web` first

**Issue:** Images/assets not loading
- **Solution:** Check `pubspec.yaml` has correct asset paths
- **Solution:** Use `AssetImage` or relative paths in Flutter code

**Issue:** Build fails in GitHub Actions
- **Solution:** Check Flutter version in workflow matches your local version
- **Solution:** Verify all dependencies in `pubspec.yaml` are valid

---

## 📱 Testing Locally

### JavaScript Web App

```bash
# Simple HTTP server
cd webapp
python -m http.server 8000
# Visit: http://localhost:8000

# Or use Node.js
npx http-server webapp -p 8000
```

### Flutter Web App

```bash
cd flutter_webapp
flutter run -d chrome
# Or for production build test:
flutter build web --release
cd build/web
python -m http.server 8000
```

---

## ✅ Deployment Checklist

### Before Deploying JavaScript App:
- [ ] Test locally in a web browser
- [ ] Verify all links work with relative paths
- [ ] Test service worker (PWA) functionality
- [ ] Test .ttbackup import/export
- [ ] Check console for errors

### Before Deploying Flutter App:
- [ ] Run `flutter create . --platforms=web`
- [ ] Run `flutter pub get`
- [ ] Set correct `base-href` in build command
- [ ] Test locally with `flutter run -d chrome`
- [ ] Build with `flutter build web --release`
- [ ] Test built version locally
- [ ] Check console for errors

---

## 🎉 Success!

Once deployed, your app will be live at:
- **JavaScript**: `https://<username>.github.io/OpenTransition/`
- **Flutter**: `https://<username>.github.io/OpenTransition-flutter/`

Share the link with users, and they can start tracking their transition journey!

Both apps support:
- ✅ Photo tracking (face, body)
- ✅ Milestone management
- ✅ .ttbackup import/export (Android compatible)
- ✅ Light/Dark themes
- ✅ Privacy-focused (all data stored locally)
- ✅ Material 3 design

---

## 📚 Additional Resources

- [GitHub Pages Documentation](https://docs.github.com/en/pages)
- [GitHub Actions for Pages](https://github.com/actions/deploy-pages)
- [Flutter Web Deployment](https://docs.flutter.dev/deployment/web)
- [Material 3 Guidelines](https://m3.material.io/)

---

## 🆘 Need Help?

If you encounter issues:
1. Check the **Actions** tab for build logs
2. Review the troubleshooting section above
3. Check browser console for errors
4. Ensure GitHub Pages is enabled in repository settings

For Flutter-specific issues:
- Run `flutter doctor` to check your Flutter installation
- Check Flutter version compatibility
- Review `pubspec.yaml` for dependency issues
