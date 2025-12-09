# OpenTransition Web App

This directory contains the web version of OpenTransition - a Progressive Web App (PWA) that brings the core functionality of the Android app to any device with a modern web browser.

## Quick Start

### Using the Web App

1. **Direct Use**: Open `index.html` in any modern web browser
2. **Install as PWA**: Visit the app in Chrome/Edge/Safari and click "Install" 

### Local Development Server

```bash
# Using Python 3
cd webapp
python3 -m http.server 8000

# Then open http://localhost:8000 in your browser
```

## Features

✅ **All Core Features from Android App:**
- 📸 Photo tracking (face & body)
- 🎯 Milestone management
- 🖼️ Gallery with filtering
- 🔒 PIN-based app lock
- 💾 Data export/import
- 🌙 Light/Dark/Auto themes
- 📱 **Import Android app backups** (.ttbackup files)

✅ **Web-Specific Benefits:**
- Works on any device (Windows, Mac, Linux, iOS, Android)
- No installation required (or install as PWA)
- Offline support via Service Worker
- 100% local data storage (privacy-first)
- Automatic backups via export
- **Seamless migration from Android app**

## Architecture

```
webapp/
├── index.html          # Main app interface
├── manifest.json       # PWA configuration
├── service-worker.js   # Offline functionality
├── css/
│   └── styles.css      # Material Design styles
├── js/
│   ├── app.js          # Main application logic
│   ├── db.js           # IndexedDB wrapper
│   └── sw-register.js  # Service worker registration
└── images/
    ├── icon-192.png    # App icon (192x192)
    └── icon-512.png    # App icon (512x512)
```

## Technology Stack

- **Frontend**: Vanilla JavaScript, HTML5, CSS3
- **Storage**: IndexedDB (local browser database)
- **UI Framework**: Material Design (custom CSS)
- **PWA**: Service Worker for offline support
- **Dependencies**: None (zero external dependencies except Google Fonts for icons)

## Privacy & Security

The web app follows the same privacy-first principles as the Android app:

- ✅ All data stored locally in browser (IndexedDB)
- ✅ No server communication
- ✅ No analytics or tracking
- ✅ PIN lock protection
- ✅ Export/import for data portability

## Browser Support

Requires a modern browser with:
- IndexedDB support
- Service Worker support
- ES6 JavaScript
- CSS Grid & Flexbox

**Tested on:**
- Chrome/Edge 90+
- Firefox 88+
- Safari 14+
- Opera 76+

## Deployment

### GitHub Pages

```bash
# Add webapp to GitHub Pages
git add webapp/
git commit -m "Add web app version"
git push

# Configure GitHub Pages to serve from /webapp directory
```

### Static Hosting

Upload the `webapp/` directory to any static hosting service:
- Netlify
- Vercel
- Firebase Hosting
- AWS S3
- Your own web server

**Important**: For full PWA functionality, serve over HTTPS.

## Comparison with Android App

| Feature | Android App | Web App |
|---------|-------------|---------|
| Photo Tracking | ✅ | ✅ |
| Milestones | ✅ | ✅ |
| Gallery View | ✅ | ✅ |
| App Lock | ✅ | ✅ (PIN only) |
| Data Export | ✅ | ✅ |
| **Import Android Backups** | N/A | ✅ |
| Offline Support | ✅ | ✅ |
| Firebase Sync | ✅ | ❌ |
| Camera Integration | ✅ Native | ✅ Web API |
| Photo Comparison | ✅ | 📋 Future |
| Cloud Backup | ✅ | ❌ (Manual export) |

**Migration Path:** Users can easily migrate from the Android app to the web app by:
1. Creating a backup in the Android app (Settings → Export)
2. Transferring the `.ttbackup` file to their device
3. Importing it in the web app (Settings → Import Data)

## Development

### Adding New Features

1. **Data Model Changes**: Update `js/db.js`
2. **UI Changes**: Update `index.html` and `css/styles.css`
3. **Logic Changes**: Update `js/app.js`
4. **PWA Updates**: Update `service-worker.js` cache version

### Testing

```bash
# Start local server
python3 -m http.server 8000

# Open browser console to check for errors
# Test in multiple browsers
# Test offline mode (disconnect network)
# Test PWA installation
```

## Contributing

Contributions to the web app are welcome! Please ensure:
- Code follows existing style
- No external dependencies added without discussion
- Privacy-first approach maintained
- Cross-browser compatibility tested

## License

GPL v3 - Same as the Android app

---

For full documentation, see [README.md](README.md) in this directory.
