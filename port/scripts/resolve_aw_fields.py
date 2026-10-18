#!/usr/bin/env python3
"""Second pass of at_to_aw.py: resolves 'FIELD <cls> <name> [mutable]' placeholder lines to proper access widener entries
by reading field descriptors from the Mojang-named Minecraft jar with javap.
Usage: resolve_aw_fields.py <in.aw> <out.aw> <named-minecraft.jar>
"""
import re
import subprocess
import sys
from collections import defaultdict

src, dst, jar = sys.argv[1:4]
lines = open(src, encoding="utf-8").read().splitlines()
wanted = defaultdict(set)
for l in lines:
    if l.startswith("FIELD "):
        wanted[l.split()[1]].add(l.split()[2])
desc = {}
for cls in wanted:
    out = subprocess.run(["javap", "-p", "-s", "-cp", jar, cls.replace("/", ".")], capture_output=True, text=True).stdout.splitlines()
    for i, l in enumerate(out):
        m = re.match(r"^\s+.*?\b([A-Za-z0-9_$]+);$", l)
        if m and "(" not in l and i + 1 < len(out) and "descriptor:" in out[i + 1]:
            desc[(cls, m.group(1))] = out[i + 1].split("descriptor:")[1].strip()
res = []
for l in lines:
    if not l.startswith("FIELD "):
        res.append(l)
        continue
    p = l.split()
    cls, name = p[1], p[2]
    d = desc.get((cls, name))
    if d is None:
        print(f"WARNING: field {cls}.{name} does not exist in vanilla, skipped")
        res.append(f"# skipped (not in vanilla): {cls} {name}")
        continue
    res.append(f"accessible field {cls} {name} {d}")
    if len(p) > 3 and p[3] == "mutable":
        res.append(f"mutable field {cls} {name} {d}")
open(dst, "w", encoding="utf-8", newline="\n").write("\n".join(res) + "\n")
print(f"resolved {sum(len(v) for v in wanted.values())} fields")
