#!/usr/bin/env python3
"""Deterministic, idempotent normalization applied to every upstream Mekanism snapshot
imported into port/ (see NOTES.md D1/D10).

Relocates references to NeoForge API classes into the port's clean-room compatibility layer
(`mekanism.neoshim.*`), except for the classes that Forge Config API Port provides under their
real NeoForge names.

Usage: normalize_upstream.py <dir> [<dir> ...]
"""
import re
import sys
from pathlib import Path

# Prefixes that must keep their real net.neoforged names (provided by Forge Config API Port).
KEEP = (
    "net.neoforged.neoforge.common.ModConfigSpec",
    "net.neoforged.neoforge.common.TranslatableEnum",
    "net.neoforged.fml.config.",
    "net.neoforged.neoforge.client.gui.ConfigurationScreen",
)

PATTERN = re.compile(r"\bnet\.neoforged\.([A-Za-z0-9_.]*[A-Za-z0-9_*])")


def relocate(match: re.Match) -> str:
    full = match.group(0)
    for keep in KEEP:
        if full == keep or full.startswith(keep if keep.endswith(".") else keep + ".") or full == keep.rstrip("."):
            return full
    return "mekanism.neoshim." + match.group(1)


def normalize_file(path: Path) -> bool:
    text = path.read_text(encoding="utf-8")
    new = PATTERN.sub(relocate, text)
    if new != text:
        path.write_text(new, encoding="utf-8", newline="")
        return True
    return False


def main(argv):
    changed = 0
    for root in argv[1:]:
        for path in sorted(Path(root).rglob("*.java")):
            if normalize_file(path):
                changed += 1
    print(f"normalize_upstream: rewrote {changed} files")


if __name__ == "__main__":
    main(sys.argv)
