#!/usr/bin/env bash
# Copies an upstream Mekanism checkout into port/ (mirroring its layout) and normalizes it.
# Commit the result as a standalone "vendor:" commit before any manual edits (NOTES.md D10).
#
# Usage: scripts/import_upstream.sh <path-to-upstream-checkout>
set -euo pipefail
UP="$(cd "$1" && pwd)"
PORT="$(cd "$(dirname "$0")/.." && pwd)"

for d in api main generators additions tools datagen gameTest test; do
  rm -rf "$PORT/src/$d"
  cp -r "$UP/src/$d" "$PORT/src/$d"
done
rm -rf "$PORT/annotation-processor"
cp -r "$UP/annotation-processor" "$PORT/annotation-processor"
cp "$UP/LICENSE" "$PORT/LICENSE"
cp "$UP/logo.png" "$PORT/logo.png"
(cd "$UP" && git rev-parse HEAD) > "$PORT/UPSTREAM_COMMIT"

python "$PORT/scripts/normalize_upstream.py" "$PORT/src" "$PORT/annotation-processor"
echo "Imported upstream $(cat "$PORT/UPSTREAM_COMMIT")"
