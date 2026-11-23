#!/usr/bin/env bash
# Fetches a game's third-party models listed in engine/<game>/assets/models/models.txt
# ("<file> <sha256> <source> [animations]" per line), each pinned by checksum; skips files already there
# with the right checksum. A source starting with ~/ is a local copy of a bought pack, which never goes
# in the public repo: it is copied when this machine has it and skipped (the game falls back) when not.
# Usage: scripts/engine-assets.sh engine/<game>
set -euo pipefail

project="${1:?usage: scripts/engine-assets.sh engine/<game>}"
dir="$project/assets/models"
manifest="$dir/models.txt"
[ -f "$manifest" ] || { echo "no $manifest: nothing to fetch"; exit 0; }
while read -r file sum url _; do
  case "$file" in ''|\#*) continue ;; esac
  target="$dir/$file"
  if [ -f "$target" ] && [ "$(shasum -a 256 "$target" | cut -d' ' -f1)" = "$sum" ]; then
    continue
  fi
  case "$url" in
    "~/"*)
      local_copy="$HOME/${url#\~/}"
      if [ ! -f "$local_copy" ]; then
        echo "skipped $file: bought pack not on this machine ($url)"
        continue
      fi
      cp "$local_copy" "$target.part" ;;
    *) curl -fsSL "$url" -o "$target.part" ;;
  esac
  got="$(shasum -a 256 "$target.part" | cut -d' ' -f1)"
  if [ "$got" != "$sum" ]; then
    rm -f "$target.part"
    echo "checksum mismatch for $file: expected $sum, got $got" >&2
    exit 1
  fi
  mv "$target.part" "$target"
  echo "fetched $file"
done < "$manifest"
