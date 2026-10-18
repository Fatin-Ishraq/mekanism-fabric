#!/usr/bin/env python3
"""Dev helper: splits a bundle file into source files.

Bundle format: lines of the form '=== relative/path/File.java ===' start a new file; everything until the next header is its content.
Usage: unbundle.py <bundle> <base-dir>
"""
import re
import sys
from pathlib import Path

bundle, base = sys.argv[1], Path(sys.argv[2])
current, buf, count = None, [], 0


def flush():
    global count
    if current is not None:
        path = base / current
        path.parent.mkdir(parents=True, exist_ok=True)
        text = "\n".join(buf).strip("\n") + "\n"
        path.write_text(text, encoding="utf-8", newline="\n")
        count += 1


for line in open(bundle, encoding="utf-8").read().split("\n"):
    m = re.match(r"^=== (.+?) ===$", line)
    if m:
        flush()
        current, buf = m.group(1).strip(), []
    else:
        buf.append(line)
flush()
print(f"unbundle: wrote {count} files")
