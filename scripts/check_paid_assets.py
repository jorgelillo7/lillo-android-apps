#!/usr/bin/env python3
"""Fails when a file from a bought asset pack is in git, or about to be, anywhere in the repo.

Two checks, both needing no pack on the machine:
- by name: a model whose models.txt source is a local bought pack (``~/...``) may only appear
  as its ``.import`` file; the model and anything Godot extracts from it (``<stem>_*.png``) may not;
- by content: no file may match a checksum in ``engine/paid-assets.sha256``, the SHA-256 of
  every file in every bought pack (checksums only: they give nothing away).

It looks at tracked files, staged files and untracked files that are not ignored, so it also
catches a ``git add -A`` that has not run yet.

  scripts/check_paid_assets.py            # check (CI, the pre-commit hook)
  scripts/check_paid_assets.py --hook     # Claude Code PreToolUse hook: reads the tool call on stdin
  scripts/check_paid_assets.py --update   # rewrite the checksum list from the packs on this Mac
"""
import hashlib
import json
import os
import re
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
CHECKSUMS = ROOT / "engine" / "paid-assets.sha256"
PACKS = Path.home() / "Projects" / "documentation" / "assets"
# Bought packs under PACKS; free ones (KayKit FREE, Kenney) are not listed.
PAID_PACKS = ["kaykit-skeletons-extra"]
GIT_ACTIONS = re.compile(r"\b(git\s+(commit|push|add)|gh\s+pr\s+(create|merge))\b")


def git(*args: str) -> list[str]:
    out = subprocess.run(["git", *args], cwd=ROOT, capture_output=True, text=True, check=True)
    return [line for line in out.stdout.splitlines() if line]


def candidates() -> set[str]:
    """Every path that is or could soon be committed."""
    return set(git("ls-files")) | set(git("diff", "--cached", "--name-only", "--diff-filter=AMR")) \
        | set(git("ls-files", "--others", "--exclude-standard"))


def paid_stems() -> set[str]:
    stems = set()
    for manifest in ROOT.glob("engine/*/assets/models/models.txt"):
        for line in manifest.read_text().splitlines():
            parts = line.split()
            if len(parts) >= 3 and not parts[0].startswith("#") and parts[2].startswith("~/"):
                stems.add(Path(parts[0]).stem)
    return stems


def sha256(path: Path) -> str:
    digest = hashlib.sha256()
    with open(path, "rb") as handle:
        for block in iter(lambda: handle.read(1 << 20), b""):
            digest.update(block)
    return digest.hexdigest()


def problems() -> list[str]:
    found = []
    stems = paid_stems()
    known = set(CHECKSUMS.read_text().split()[::2]) if CHECKSUMS.exists() else set()
    for path in sorted(candidates()):
        name = Path(path).name
        if not name.endswith(".import") and any(name == s + Path(name).suffix or name.startswith(s + "_")
                                                for s in stems):
            found.append(f"{path}: a bought model (its models.txt source is a local pack)")
            continue
        full = ROOT / path
        if known and full.is_file() and not full.is_symlink() and sha256(full) in known:
            found.append(f"{path}: same content as a file in a bought pack")
    return found


def update() -> None:
    lines = []
    for pack in PAID_PACKS:
        folder = PACKS / pack
        if not folder.is_dir():
            raise SystemExit(f"{folder} is not on this Mac")
        for file in sorted(p for p in folder.rglob("*") if p.is_file() and p.name != ".DS_Store"):
            lines.append(f"{sha256(file)}  {pack}/{file.relative_to(folder)}")
    CHECKSUMS.write_text("".join(sorted(set(line + "\n" for line in lines))))
    print(f"{CHECKSUMS.relative_to(ROOT)}: {len(lines)} files")


def main() -> None:
    if "--update" in sys.argv:
        update()
        return
    if "--hook" in sys.argv:
        call = json.load(sys.stdin)
        if not GIT_ACTIONS.search(call.get("tool_input", {}).get("command", "")):
            return
    found = problems()
    if not found:
        return
    print("Bought assets must never reach this public repo:", file=sys.stderr)
    for line in found:
        print(f"  {line}", file=sys.stderr)
    print("Keep them in ~/Projects/documentation/assets/ (engine/README.md, 'Bought assets').",
          file=sys.stderr)
    sys.exit(2 if "--hook" in sys.argv else 1)


if __name__ == "__main__":
    os.environ.setdefault("GIT_OPTIONAL_LOCKS", "0")
    main()
