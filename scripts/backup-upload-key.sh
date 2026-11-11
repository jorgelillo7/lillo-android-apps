#!/usr/bin/env bash
# Backs up an app's Google Play upload key (the .jks named in apps/<app>/keystore.properties) and
# its credentials to the Google Drive for Desktop folder, one subfolder per app, and verifies the
# copy by checksum. A different key already there is never overwritten. Passwords are never printed.
#
# Usage: scripts/backup-upload-key.sh [--check] [--encrypt] <app-folder|all>
#   --check    report, per app, whether the backup holds the same key; writes nothing
#   --encrypt  store <key>.enc and credentials.txt.enc (AES-256, PBKDF2) instead of the plain files,
#              with the passphrase from the Keychain item "lillo-android-keys-backup"
#
# Backup folder: $LILLO_KEYS_BACKUP_DIR if set, else the one folder matching
#   ~/Library/CloudStorage/GoogleDrive-*/<Mi unidad|My Drive>/Proyectos personales/backups/android-playstore-keys
# The account is part of that path: it is found at run time and never written in this repo.
#
# See docs/signing.md.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
KEYCHAIN_ITEM="lillo-android-keys-backup"
OPENSSL=/usr/bin/openssl
CIPHER=(-aes-256-cbc -pbkdf2 -iter 600000 -md sha256)

usage() { echo "usage: backup-upload-key.sh [--check] [--encrypt] <app-folder|all>" >&2; exit 2; }

check=0
encrypt=0
target=""
for arg in "$@"; do
  case "$arg" in
    --check) check=1 ;;
    --encrypt) encrypt=1 ;;
    -*) usage ;;
    *) [ -z "$target" ] || usage; target="$arg" ;;
  esac
done
[ -n "$target" ] || usage

backup_root() {
  if [ -n "${LILLO_KEYS_BACKUP_DIR:-}" ]; then
    [ -d "$LILLO_KEYS_BACKUP_DIR" ] || { echo "LILLO_KEYS_BACKUP_DIR is not a folder: $LILLO_KEYS_BACKUP_DIR" >&2; return 1; }
    printf '%s\n' "$LILLO_KEYS_BACKUP_DIR"
    return
  fi
  local matches=() candidate
  for candidate in "$HOME"/Library/CloudStorage/GoogleDrive-*/{"Mi unidad","My Drive"}/"Proyectos personales/backups/android-playstore-keys"; do
    [ -d "$candidate" ] && matches+=("$candidate")
  done
  if [ "${#matches[@]}" -ne 1 ]; then
    echo "Expected exactly one Google Drive keys folder, found ${#matches[@]}" >&2
    echo "(…/GoogleDrive-*/Mi unidad/Proyectos personales/backups/android-playstore-keys)." >&2
    echo "Is Google Drive for Desktop running? Set LILLO_KEYS_BACKUP_DIR to choose one." >&2
    return 1
  fi
  printf '%s\n' "${matches[0]}"
}

# The value of key $2 in the properties file $1, trimmed, with \uXXXX escapes decoded.
prop() {
  python3 - "$1" "$2" <<'PY'
import re, sys
path, key = sys.argv[1], sys.argv[2]
for line in open(path, encoding="latin-1"):
    name, sep, value = line.partition("=")
    if sep and name.strip() == key:
        print(re.sub(r"\\u([0-9a-fA-F]{4})", lambda m: chr(int(m.group(1), 16)), value.strip()))
        break
PY
}

sha() { shasum -a 256 | cut -d' ' -f1; }

passphrase=""
load_passphrase() {
  [ -n "$passphrase" ] && return
  if ! passphrase="$(security find-generic-password -s "$KEYCHAIN_ITEM" -w 2>/dev/null)" || [ -z "$passphrase" ]; then
    echo "No passphrase in the Keychain item '$KEYCHAIN_ITEM'. Create it once (it asks for the passphrase):" >&2
    echo "  security add-generic-password -s $KEYCHAIN_ITEM -a \"\$USER\" -w" >&2
    echo "and keep the same passphrase in your password manager: without it the backups can't be opened." >&2
    exit 1
  fi
}

# printf is a builtin: the passphrase reaches openssl through a pipe, never an argument or a file.
encrypt_file() { printf '%s' "$passphrase" | "$OPENSSL" enc "${CIPHER[@]}" -salt -pass stdin -in "$1" -out "$2"; }
decrypt_sha() { printf '%s' "$passphrase" | "$OPENSSL" enc -d "${CIPHER[@]}" -pass stdin -in "$1" 2>/dev/null | sha; }

# What a backed-up file holds, as a checksum ("" when there is no copy). $1: plain path.
stored_sha() {
  local sum
  if [ -f "$1" ]; then
    sha <"$1"
  elif [ -f "$1.enc" ]; then
    load_passphrase
    if sum="$(decrypt_sha "$1.enc")"; then echo "$sum"; else echo "unreadable"; fi
  fi
}

# Writes $1 to $2 (plain, or $2.enc), then reads it back and compares checksums.
store() {
  local source="$1" dest="$2" want got
  want="$(sha <"$source")"
  if [ "$encrypt" = 1 ]; then
    encrypt_file "$source" "$dest.enc"
    got="$(decrypt_sha "$dest.enc")"
  else
    cp "$source" "$dest"
    got="$(sha <"$dest")"
  fi
  [ "$want" = "$got" ] || { echo "  Verification FAILED for $(basename "$dest"): the copy differs" >&2; return 1; }
}

failures=0
backup_app() {
  local app="$1" props jks name dest credentials key_sha cred_sha key_have cred_have
  props="$ROOT/apps/$app/keystore.properties"
  if [ ! -f "$props" ]; then
    echo "$app: no keystore.properties, no key to back up"
    [ "$target" = all ] || failures=$((failures + 1))
    return
  fi
  jks="$(prop "$props" storeFile)"
  [ -f "$jks" ] || { echo "$app: the key named in keystore.properties is missing: $jks" >&2; failures=$((failures + 1)); return; }
  name="$(basename "$jks")"
  dest="$root/$app"

  credentials="$work/$app-credentials.txt"
  {
    echo "# Google Play upload key for $app: $name, in this folder. Secret: keep it private."
    echo "keyAlias=$(prop "$props" keyAlias)"
    echo "storePassword=$(prop "$props" storePassword)"
    echo "keyPassword=$(prop "$props" keyPassword)"
  } >"$credentials"

  key_sha="$(sha <"$jks")"
  cred_sha="$(sha <"$credentials")"
  key_have="$(stored_sha "$dest/$name")"
  cred_have="$(stored_sha "$dest/credentials.txt")"

  if [ "$key_have" = unreadable ] || [ "$cred_have" = unreadable ]; then
    echo "$app: the encrypted backup doesn't open with the Keychain passphrase. Nothing written." >&2
    failures=$((failures + 1)); return
  fi

  if [ "$check" = 1 ]; then
    if [ "$key_have" = "$key_sha" ] && [ "$cred_have" = "$cred_sha" ]; then
      echo "$app: backed up ($name and its credentials match)"
    elif [ -z "$key_have" ]; then
      echo "$app: NOT backed up"; failures=$((failures + 1))
    elif [ "$key_have" != "$key_sha" ]; then
      echo "$app: the backup holds a DIFFERENT key ($name)"; failures=$((failures + 1))
    else
      echo "$app: key backed up, credentials missing or different"; failures=$((failures + 1))
    fi
    return
  fi

  if [ -n "$key_have" ] && [ "$key_have" != "$key_sha" ]; then
    echo "$app: STOP, $dest/$name holds a different key. Nothing written; compare them by hand." >&2
    failures=$((failures + 1)); return
  fi
  if [ -n "$cred_have" ] && [ "$cred_have" != "$cred_sha" ]; then
    echo "$app: STOP, $dest/credentials.txt differs from keystore.properties. Nothing written." >&2
    failures=$((failures + 1)); return
  fi
  if [ "$key_have" = "$key_sha" ] && [ "$cred_have" = "$cred_sha" ]; then
    echo "$app: already backed up"
    return
  fi

  [ "$encrypt" = 1 ] && load_passphrase
  mkdir -p "$dest"
  if [ -z "$key_have" ]; then store "$jks" "$dest/$name" || { failures=$((failures + 1)); return; }; fi
  if [ -z "$cred_have" ]; then store "$credentials" "$dest/credentials.txt" || { failures=$((failures + 1)); return; }; fi
  echo "$app: backed up and verified ($name + credentials$([ "$encrypt" = 1 ] && echo ", encrypted"))"
}

[ "$target" = all ] || [ -d "$ROOT/apps/$target" ] || { echo "No such app: apps/$target" >&2; exit 1; }
root="$(backup_root)" || exit 1
umask 077   # the credentials copy holds the passwords: readable by you only
work="$(mktemp -d "${TMPDIR:-/tmp}/upload-key-backup.XXXXXX")"
trap 'rm -rf "$work"' EXIT
# The account (an email address) is part of Drive's path: keep it out of logs and chats.
echo "Backup folder: $(printf '%s' "$root" | sed -E 's#/GoogleDrive-[^/]+/#/GoogleDrive-<account>/#')"
if [ "$target" = all ]; then
  for dir in "$ROOT"/apps/*/; do backup_app "$(basename "$dir")"; done
else
  backup_app "$target"
fi
[ "$failures" = 0 ] || { echo "$failures problem(s): see above." >&2; exit 1; }
