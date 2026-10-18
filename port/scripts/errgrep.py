#!/usr/bin/env python3
"""Dev helper: prints javac errors whose message matches a pattern, with the source line and caret context.
Usage: errgrep.py <log> <regex> [max]
"""
import re
import sys

log = open(sys.argv[1], encoding='utf-8', errors='replace').read().split('\n')
pattern = re.compile(sys.argv[2])
limit = int(sys.argv[3]) if len(sys.argv) > 3 else 10
shown = 0
for i, line in enumerate(log):
    if 'error:' in line and pattern.search(line):
        m = re.search(r'src[\\/](\w+)[\\/]java[\\/](.*?\.java):(\d+): error: (.*)', line)
        if m:
            print(f"{m.group(2).replace(chr(92), '/')}:{m.group(3)}: {m.group(4)}")
        else:
            print(line.strip())
        for j in range(i + 1, min(i + 6, len(log))):
            if 'error:' in log[j] or 'warning:' in log[j]:
                break
            print('    ' + log[j].rstrip())
        shown += 1
        if shown >= limit:
            break
