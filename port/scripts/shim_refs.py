"""Cross-references upstream sources against the neoshim tree.
Prints shim classes referenced by upstream (non-fabric) sources that do not exist, and shim classes nobody outside the shim references.
Usage (from port/): shim_refs.py [modules...]  (default: every source set except fabric)"""
import collections
import os
import re
import sys

ROOT = "src"
BASE = os.path.join("src", "fabric", "java")
mods = sys.argv[1:] or [m for m in os.listdir(ROOT) if m != "fabric"]
IMPORT = re.compile(r"import (?:static )?(mekanism\.neoshim\.[\w.]+)")


def java_files(top):
    for dp, _, fs in os.walk(top):
        for f in fs:
            if f.endswith(".java"):
                yield os.path.join(dp, f)


def imports(files):
    found = collections.Counter()
    for path in files:
        for m in IMPORT.finditer(open(path, encoding="utf-8", errors="ignore").read()):
            found[m.group(1)] += 1
    return found


shim = set()
for path in java_files(os.path.join(BASE, "mekanism", "neoshim")):
    rel = os.path.relpath(path, BASE)[:-5]
    shim.add(rel.replace(os.sep, "."))


def owner(name):
    parts = name.split(".")
    for i in range(len(parts), 0, -1):
        cand = ".".join(parts[:i])
        if cand in shim:
            return cand
    return None


upstream = imports(f for m in mods for f in java_files(os.path.join(ROOT, m)))
port = imports(f for f in java_files(BASE) if os.sep + "neoshim" + os.sep not in f)
missing = sorted(c for c in upstream if owner(c) is None)
print("MISSING (referenced upstream, not in shim):", len(missing))
for c in missing:
    print("  ", c, upstream[c])
used = {owner(c) for c in list(upstream) + list(port)}
unused = sorted(shim - used)
print("UNREFERENCED outside the shim:", len(unused))
for c in unused:
    print("  ", c)
