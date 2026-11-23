#!/usr/bin/env bash
# Fetches a game's third-party models listed in engine/<game>/assets/models/models.txt
# ("<file> <sha256> <url> [animations]" per line), each pinned by URL and checksum; skips files already there
# with the right checksum. Usage: scripts/engine-assets.sh engine/<game>
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
  curl -fsSL "$url" -o "$target.part"
  got="$(shasum -a 256 "$target.part" | cut -d' ' -f1)"
  if [ "$got" != "$sum" ]; then
    rm -f "$target.part"
    echo "checksum mismatch for $file: expected $sum, got $got" >&2
    exit 1
  fi
  mv "$target.part" "$target"
  echo "fetched $file"
done < "$manifest"
