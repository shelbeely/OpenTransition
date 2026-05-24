# OpenTransition Web App

This is the web version of OpenTransition, a transition tracking application made specifically for transgender people.

## Features

- 📸 **Photo Tracking** - Document your journey with face and body photos
- 🎯 **Milestone Management** - Record and celebrate important events
- 🖼️ **Gallery View** - Browse and filter your photos
- 🔒 **Privacy First** - App lock with PIN protection
- 💾 **Data Control** - Export, import, and backup your data
- 🌙 **Theme Support** - Light, dark, and auto (system) themes
- 📱 **Progressive Web App** - Install on your device and use offline

## Privacy & Security

All your photos and data are stored **locally on your device** using IndexedDB. Nothing is uploaded to any server unless you explicitly choose to export your data. Your privacy is our priority.

## Installation

### As a Web App

1. Open `index.html` in a modern web browser
2. The app works immediately - no installation required

### As a PWA (Progressive Web App)

1. Visit the app in Chrome, Edge, or Safari
2. Look for the "Install" button in the address bar
3. Click "Install" to add it to your home screen
4. Use it like a native app with offline support

### Hosting

To host this web app:

1. Upload the entire `webapp` folder to your web server
2. Ensure your server serves the files with proper MIME types
3. For best PWA experience, serve over HTTPS

## Usage

### Adding Photos

1. Click "Add Photo" from the home screen
2. Choose or take a photo
3. Select the photo type (Face or Body)
4. Choose the date
5. Save

### Adding Milestones

1. Click "Add Milestone" from the home screen or milestones view
2. Enter a title and description
3. Choose the date
4. Save

### Viewing Gallery

1. Navigate to "Gallery" from the menu
2. Use filter buttons to show all photos, face photos, or body photos
3. Click any photo to view it full size

### Managing Data

#### Export Data
1. Go to Settings
2. Click "Export Data (.ttbackup format)"
3. Save the `.ttbackup` file to a safe location

**The web app now exports in the same .ttbackup format used by the Android app!**

#### Import Data

**From Web App or Android App Backup:**
1. Go to Settings
2. Click "Import Data"
3. Select your `.ttbackup` file (from either web or Android)

The web app automatically detects and imports `.ttbackup` files from either platform.

**Backup Format Compatibility:**
- Both web and Android apps use the same `.ttbackup` format (ZIP file)
- Contains `data.json` with metadata and image files
- Fully compatible in both directions
- Easy migration between platforms

#### Clear Data
1. Go to Settings
2. Click "Clear All Data"
3. Confirm (this cannot be undone)

### App Lock

1. Go to Settings
2. Enable "Enable App Lock"
3. Enter a 4-digit PIN
4. The app will require this PIN on next launch

## Technology Stack

- **Frontend**: Vanilla JavaScript, HTML5, CSS3
- **Storage**: IndexedDB for local data persistence
- **UI**: Material Design principles
- **PWA**: Service Worker for offline functionality
- **Icons**: Material Icons from Google Fonts

## Browser Compatibility

- Chrome/Edge 90+
- Firefox 88+
- Safari 14+
- Opera 76+

Modern browsers with support for:
- IndexedDB
- Service Workers
- ES6 JavaScript
- CSS Grid
- Flexbox

## Development

### File Structure

```
webapp/
├── index.html          # Main HTML file
├── manifest.json       # PWA manifest
├── service-worker.js   # Service worker for offline support
├── css/
│   └── styles.css      # All styles
├── js/
│   ├── app.js          # Main application logic
│   ├── db.js           # IndexedDB wrapper
│   └── sw-register.js  # Service worker registration
├── images/
│   ├── icon-192.png    # App icon 192x192
│   └── icon-512.png    # App icon 512x512
└── README.md           # This file
```

### Running Locally

For local development, you need to serve the files over HTTP(S) due to service worker requirements:

```bash
# Using Python 3
cd webapp
python3 -m http.server 8000

# Using Node.js (with http-server)
npm install -g http-server
cd webapp
http-server -p 8000

# Using PHP
cd webapp
php -S localhost:8000
```

Then open `http://localhost:8000` in your browser.

## License

OpenTransition is free and open source software licensed under GPL v3.

```
Original Copyright (C) 2018 - 2021 TransTracks
Fork modifications and rebranding by the OpenTransition contributors

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
```

## Credits

OpenTransition is based on the original **TransTracks** Android application. We are deeply grateful to the TransTracks developers and contributors for creating this valuable tool for the transgender community.

The web version was created to provide a cross-platform alternative that works on any device with a modern web browser.

## Support

For issues, questions, or contributions:
- GitHub: https://github.com/shelbeely/OpenTransition
- Documentation: https://shelbeely.github.io/OpenTransition

## Privacy Notice

This web application:
- ✅ Stores all data locally on your device
- ✅ Works completely offline after first load
- ✅ Does not collect any analytics or telemetry
- ✅ Does not send any data to external servers
- ✅ Respects your privacy and anonymity

Your data is yours. Always.
