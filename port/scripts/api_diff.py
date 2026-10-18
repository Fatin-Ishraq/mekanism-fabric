"""Compares a shim class's methods with the NeoForge class of the same name (from the NeoForge jar, via javap).
Prints NeoForge methods missing from the shim and shim methods NeoForge no longer has (by name + simple parameter types).
Usage: api_diff.py <neoforge.jar> <shim .java file> <fully qualified NeoForge class name>"""
import re
import subprocess
import sys

jar, shim_file, neo_class = sys.argv[1:4]


def simple(t):
    t = re.sub(r"<.*>", "", t.strip())
    t = t.split(".")[-1].replace("$", ".")
    return t.split(".")[-1]


out = subprocess.run(["javap", "-protected", "-classpath", jar, neo_class], capture_output=True, text=True).stdout
neo = {}
for line in out.splitlines():
    m = re.search(r"\s([\w$<>]+)\((.*)\);$", line)
    if not m or "static {}" in line:
        continue
    name, params = m.group(1), m.group(2)
    if name.startswith("lambda$"):
        continue
    sig = name + "(" + ",".join(simple(p) for p in re.split(r",(?![^<]*>)", params) if p.strip()) + ")"
    neo[sig] = line.strip()

src = open(shim_file, encoding="utf-8").read()
shim = {}
for m in re.finditer(r"^\s+(?:public |protected |static |default |abstract |final |synchronized |<[^>]+>\s)*[\w<>\[\]?, .]+\s+(\w+)\(([^)]*)\)\s*(?:\{|;|throws)",
                     src, re.M):
    name, params = m.group(1), m.group(2)
    types = []
    for p in re.split(r",(?![^<]*>)", params):
        p = re.sub(r"@\w+(\([^)]*\))?\s*", "", p).strip()
        if p:
            types.append(simple(p.rsplit(" ", 1)[0]))
    shim[name + "(" + ",".join(types) + ")"] = True

missing = sorted(s for s in neo if s not in shim)
extra = sorted(s for s in shim if s not in neo)
print(f"== {neo_class}: {len(neo)} NeoForge methods, {len(missing)} missing in shim, {len(extra)} shim-only")
for s in missing:
    print("  MISSING", neo[s])
for s in extra:
    print("  SHIM-ONLY", s)
