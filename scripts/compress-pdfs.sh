#!/bin/bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
ASSETS_DIR="$SCRIPT_DIR/../app/src/main/assets/rulebooks"

if ! command -v gs &>/dev/null; then
    echo "Error: ghostscript (gs) not found. Install with: sudo apt install ghostscript"
    exit 1
fi

echo "Compressing PDFs in $ASSETS_DIR ..."

find "$ASSETS_DIR" -name '*.pdf' -print0 | while IFS= read -r -d '' pdf; do
    before=$(stat -c%s "$pdf" 2>/dev/null || stat -f%z "$pdf" 2>/dev/null || echo 0)
    echo -n "  $(basename "$pdf") ($(numfmt --to=iec "$before" 2>/dev/null || echo "${before}B")) ... "

    tmp="${pdf}.tmp.pdf"
    gs -sDEVICE=pdfwrite -dCompatibilityLevel=1.4 -dPDFSETTINGS=/ebook \
        -dNOPAUSE -dQUIET -dBATCH -sOutputFile="$tmp" "$pdf"

    mv "$tmp" "$pdf"

    after=$(stat -c%s "$pdf" 2>/dev/null || stat -f%z "$pdf" 2>/dev/null || echo 0)
    echo "$(numfmt --to=iec "$after" 2>/dev/null || echo "${after}B")"
done

echo "Done."
