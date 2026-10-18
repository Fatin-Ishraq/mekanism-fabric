#!/usr/bin/env python3
"""Dev helper: summarizes javac errors from a Gradle log.

Modes: symbols (cannot-find-symbol grouped by symbol/location), override (non-overriding @Override sites), files (errors per file).
Usage: errsummary.py <log> [mode] [limit]
"""
import collections
import re
import sys

log = open(sys.argv[1], encoding='utf-8', errors='replace').read().split('\n')
mode = sys.argv[2] if len(sys.argv) > 2 else 'symbols'
limit = int(sys.argv[3]) if len(sys.argv) > 3 else 150
cnt = collections.Counter()
for i, line in enumerate(log):
    if mode == 'symbols' and 'error: cannot find symbol' in line:
        sym = loc = ''
        for j in range(i + 1, min(i + 8, len(log))):
            m = re.search(r'symbol:\s+(.*)', log[j])
            if m:
                sym = m.group(1).strip()
            m = re.search(r'location:\s+(.*)', log[j])
            if m:
                loc = m.group(1).strip()
                break
        loc = re.sub(r'variable \w+ of type ', '', loc)
        cnt[(sym, loc)] += 1
    elif mode == 'override' and 'does not override or implement' in line:
        m = re.search(r'([A-Za-z0-9_]+\.java):(\d+)', line)
        path = re.search(r'(src[\\/].*?\.java):(\d+)', line)
        nxt = ''
        if path:
            try:
                src = open(path.group(1).replace('\\', '/'), encoding='utf-8').read().split('\n')
                ln = int(path.group(2))
                for k in range(ln, min(ln + 4, len(src))):
                    if '(' in src[k]:
                        nxt = src[k].strip()
                        break
            except OSError:
                pass
        cnt[(nxt, m.group(1) if m else '?')] += 1
    elif mode == 'files' and 'error:' in line:
        m = re.search(r'src[\\/](\w+)[\\/]java[\\/](.*?\.java)', line)
        if m:
            cnt[(m.group(2).replace('\\', '/'), '')] += 1
    elif mode == 'messages' and 'error:' in line:
        cnt[(line.split('error:', 1)[1].strip(), '')] += 1
for (a, b), c in cnt.most_common(limit):
    print(c, a, '@', b)
