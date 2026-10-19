#!/usr/bin/env python3
"""Prints access widener lines for members of a Minecraft class, with descriptors read from the compile classpath via javap.
Usage: aw_entry.py <class> <member> [access]   (access: accessible (default), extendable, mutable; class: dots or slashes)
       aw_entry.py <class>                     (prints an 'accessible class' line)
Needs /tmp/mek_cp.txt (./gradlew -q -I scripts/cp.gradle dumpCp). Every overload of a method name is printed.
"""
import re
import subprocess
import sys

cls = sys.argv[1].replace(".", "/")
if len(sys.argv) == 2:
    print(f"accessible class {cls}")
    sys.exit()
member, access = sys.argv[2], sys.argv[3] if len(sys.argv) > 3 else "accessible"
cp = ":".join(l.strip() for l in open("/tmp/mek_cp.txt") if l.strip())
out = subprocess.run(["javap", "-p", "-s", "-cp", cp, cls.replace("/", ".")], capture_output=True, text=True).stdout.splitlines()
found = False
for i, l in enumerate(out):
    if i + 1 >= len(out) or "descriptor:" not in out[i + 1]:
        continue
    d = out[i + 1].split("descriptor:")[1].strip()
    simple = cls.rsplit("/", 1)[-1].split("$")[-1]
    m = re.match(r"^\s+.*?\b([A-Za-z0-9_$]+)\(", l)
    if m:
        name = m.group(1)
        if name == cls.replace("/", ".") or name == simple:
            name = "<init>"
        if name == member:
            print(f"{'accessible' if access == 'mutable' else access} method {cls} {name} {d}")
            found = True
        continue
    m = re.match(r"^\s+.*?\b([A-Za-z0-9_$]+);$", l)
    if m and m.group(1) == member:
        print(f"{'accessible' if access == 'extendable' else access} field {cls} {member} {d}")
        found = True
if not found:
    sys.exit(f"no member {member} in {cls}")
