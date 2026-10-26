#!/usr/bin/env python3
"""Every app's version in this repo next to what each Google Play track holds.

For each apps/<app>: the versionCode/versionName in its build.gradle.kts, and per Play track
(production, internal…) the releases there with their versionCodes and status (completed, draft,
inProgress for a staged rollout, halted). Google's publishing API doesn't say whether a release is
"in review": that only shows in Play Console.

Reads the Play service account key from $ANDROID_PUBLISHER_CREDENTIALS (the JSON itself, as the
Release workflow has it) or from ~/.config/lillo-android-apps/play-service-account.json. Prints a
Markdown table. Run by the "Play status" workflow (Actions → Play status), or locally:
  pip3 install google-auth requests && python3 scripts/play_status.py
"""
import json
import os
import re
import sys
from pathlib import Path

import requests
from google.oauth2 import service_account
from google.auth.transport.requests import Request

ROOT = Path(__file__).resolve().parent.parent
API = "https://androidpublisher.googleapis.com/androidpublisher/v3/applications"
SCOPE = ["https://www.googleapis.com/auth/androidpublisher"]
KEY_FILE = Path.home() / ".config/lillo-android-apps/play-service-account.json"


def credentials():
    raw = os.environ.get("ANDROID_PUBLISHER_CREDENTIALS")
    if not raw and KEY_FILE.is_file():
        raw = KEY_FILE.read_text()
    if not raw:
        sys.exit("No Play service account key: set ANDROID_PUBLISHER_CREDENTIALS or create " + str(KEY_FILE))
    creds = service_account.Credentials.from_service_account_info(json.loads(raw), scopes=SCOPE)
    creds.refresh(Request())
    return creds


def repo_apps():
    """(folder, applicationId, versionCode, versionName) of every app in the repo."""
    for build in sorted(ROOT.glob("apps/*/app/build.gradle.kts")):
        text = build.read_text()
        app_id = re.search(r'applicationId\s*=\s*"([^"]+)"', text)
        code = re.search(r"versionCode\s*=\s*(\d+)", text)
        name = re.search(r'versionName\s*=\s*"([^"]+)"', text)
        if app_id:
            yield build.parent.parent.name, app_id.group(1), code and int(code.group(1)), name and name.group(1)


def play_tracks(session, package):
    """{track: [(versionCodes, versionName, status)]}, or None if Play doesn't know the app."""
    edit = session.post(f"{API}/{package}/edits")
    if edit.status_code in (403, 404):
        return None
    edit.raise_for_status()
    edit_id = edit.json()["id"]
    try:
        tracks = session.get(f"{API}/{package}/edits/{edit_id}/tracks")
        tracks.raise_for_status()
        out = {}
        for track in tracks.json().get("tracks", []):
            releases = [
                (r.get("versionCodes", []), r.get("name", ""), r.get("status", ""))
                for r in track.get("releases", [])
            ]
            if releases:
                out[track["track"]] = releases
        return out
    finally:
        session.delete(f"{API}/{package}/edits/{edit_id}")  # read only: nothing is ever committed


def main():
    session = requests.Session()
    session.headers["Authorization"] = "Bearer " + credentials().token
    rows = ["| App | Repo | Production | Internal | Other tracks |", "|---|---|---|---|---|"]
    for folder, package, code, name in repo_apps():
        tracks = play_tracks(session, package)
        if tracks is None:
            rows.append(f"| {folder} | {code} ({name}) | not on Play | | |")
            continue

        def show(releases):
            return "<br>".join(f"{','.join(c) or '—'} ({n}) · {s}" for c, n, s in releases) if releases else "—"

        others = "<br>".join(f"{t}: {show(r)}" for t, r in tracks.items() if t not in ("production", "internal"))
        live = max((int(v) for codes, _, s in tracks.get("production", []) if s == "completed" for v in codes), default=0)
        ahead = " ⬆️" if code and code > live else ""
        rows.append(
            f"| {folder} | {code} ({name}){ahead} | {show(tracks.get('production'))} | "
            f"{show(tracks.get('internal'))} | {others} |"
        )
    print("\n".join(rows))
    print("\n⬆️ the repo is ahead of what production serves. Status: completed = live (or in review if just "
          "sent), draft = not sent yet, inProgress = staged rollout. \"In review\" only shows in Play Console.")


if __name__ == "__main__":
    main()
