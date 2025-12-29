#!/bin/bash

# OpenTransition Printable Journal - PDF Generator
# This script generates a PDF from the HTML journal using a headless browser

set -e

echo "==================================="
echo "OpenTransition Printable Journal"
echo "PDF Generator"
echo "==================================="
echo ""

# Check if we're in the printable directory
if [ ! -f "index.html" ]; then
    echo "Error: index.html not found."
    echo "Please run this script from the printable/ directory."
    exit 1
fi

# Function to check if a command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Try to find a suitable browser for PDF generation
BROWSER=""
if command_exists google-chrome || command_exists google-chrome-stable; then
    BROWSER="chrome"
    echo "✓ Found Chrome"
elif command_exists chromium || command_exists chromium-browser; then
    BROWSER="chromium"
    echo "✓ Found Chromium"
elif command_exists firefox; then
    BROWSER="firefox"
    echo "✓ Found Firefox"
else
    echo "⚠ No suitable browser found for automatic PDF generation."
    echo ""
    echo "To generate a PDF:"
    echo "1. Open index.html in your browser"
    echo "2. Press Ctrl+P (Cmd+P on Mac)"
    echo "3. Select 'Save as PDF'"
    echo "4. Set paper size to 5.5\" x 8.5\""
    echo "5. Save as 'OpenTransition-Journal.pdf'"
    echo ""
    exit 0
fi

OUTPUT_FILE="OpenTransition-Printable-Journal.pdf"

echo ""
echo "Generating PDF..."
echo "Output: $OUTPUT_FILE"
echo ""

if [ "$BROWSER" = "chrome" ]; then
    # Use Chrome/Chromium headless mode
    CHROME_BIN=$(command -v google-chrome || command -v google-chrome-stable || command -v chromium || command -v chromium-browser)
    
    "$CHROME_BIN" --headless --disable-gpu \
        --print-to-pdf="$OUTPUT_FILE" \
        --print-to-pdf-no-header \
        --no-margins \
        "file://$(pwd)/index.html"
    
    echo "✓ PDF generated successfully!"
    
elif [ "$BROWSER" = "firefox" ]; then
    echo "Note: Firefox PDF generation requires manual steps."
    echo "Opening Firefox... Please use Print > Save as PDF"
    firefox "file://$(pwd)/index.html"
fi

echo ""
echo "==================================="
echo "PDF Generation Complete!"
echo "==================================="
echo ""
echo "File: $OUTPUT_FILE"
echo ""
echo "Next steps:"
echo "1. Open the PDF to verify formatting"
echo "2. Print on 5.5\" x 8.5\" paper"
echo "3. Bind or organize pages as desired"
echo ""
echo "For detailed printing instructions, see README.md"
echo ""
