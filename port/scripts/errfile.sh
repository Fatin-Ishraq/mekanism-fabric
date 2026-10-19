#!/bin/sh
# Prints the unique javac errors (with their detail lines) for files whose path contains a pattern.
# Usage: errfile.sh <log> <path-substring>
tr '\' '/' < "$1" | awk -v pat="$2" '
/: error:|: warning:/ { show = index($0, pat) > 0; if (show) { key = $0; sub(/^[ \t]+/, "", key); if (seen[key]++) show = 0; else print; } next }
show && /^[ \t]/ { print; next }
{ show = 0 }
' | sed 's#.*/src/#src/#'
