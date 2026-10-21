"""Groups javac errors in a Gradle log by source area. Usage: errareas.py <log> [depth]"""
import collections
import re
import sys

log = sys.argv[1]
depth = int(sys.argv[2]) if len(sys.argv) > 2 else 3
areas = collections.Counter()
roots = collections.Counter()
files = collections.Counter()
for line in open(log, encoding="utf-8", errors="ignore"):
    m = re.search(r"port[\\/]src[\\/]([a-z]+)[\\/]java[\\/](.+?)\.java:\d+: error", line)
    if not m:
        continue
    parts = re.split(r"[\\/]", m.group(2))
    roots[m.group(1)] += 1
    areas[m.group(1) + " " + "/".join(parts[:depth])] += 1
    files[m.group(1) + " " + "/".join(parts)] += 1
print(dict(roots))
for k, v in areas.most_common(40):
    print(v, k)
print("--- files with most errors")
for k, v in files.most_common(25):
    print(v, k)
