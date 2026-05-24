# Icon Generation Instructions

The webapp requires two icon files:
- `icon-192.png` (192x192 pixels)
- `icon-512.png` (512x512 pixels)

## Option 1: Use the Android App Icon

Copy the existing Android app icons from:
- `app/src/main/res/mipmap-xxxhdpi/ic_launcher.png` (192x192) → `icon-192.png`
- Or create a 512x512 version → `icon-512.png`

## Option 2: Generate from SVG

Use the provided `icon.svg` file and convert it using:

### Using ImageMagick
```bash
convert icon.svg -resize 192x192 icon-192.png
convert icon.svg -resize 512x512 icon-512.png
```

### Using Inkscape
```bash
inkscape icon.svg --export-png=icon-192.png --export-width=192 --export-height=192
inkscape icon.svg --export-png=icon-512.png --export-width=512 --export-height=512
```

### Using Online Tools
1. Go to https://cloudconvert.com/svg-to-png
2. Upload `icon.svg`
3. Set dimensions to 192x192 and 512x512
4. Download the PNG files

## Option 3: Create Custom Icons

Create custom PNG icons with your preferred design tool ensuring:
- Transparent or solid background
- Clear, recognizable design
- Follows Material Design icon guidelines
- Square format (192x192 and 512x512)

## Temporary Placeholder

Until proper icons are created, the app will work but won't have a custom icon when installed as a PWA. The browser will use a default icon instead.
