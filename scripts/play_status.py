#!/usr/bin/env python3
"""Every app's version in this repo next to what each Google Play track holds.

For each apps/<app>: the versionCode/versionName in its build.gradle.kts, and per Play track
(production, internal…) the releases there with their versionCodes and status (completed, draft,
inProgress for a staged rollout, halted). Google's publishing API doesn't say whether a release is
"in review": that only shows in Play Console. When `gh` works (the workflow gives it a token), each
app also gets its last Release workflow run (when, what, how it ended), the best hint for "just
sent, probably in review".

Reads the Play service account key from $ANDROID_PUBLISHER_CREDENTIALS (the JSON itself, as the
Release workflow has it) or from ~/.config/lillo-android-apps/play-service-account.json. Prints a
Markdown table. Run by the "Play status" workflow (Actions → Play status), or locally:
  pip3 install google-auth requests && python3 scripts/play_status.py
"""
import json
import os
import re
import subprocess
import sys
from datetime import datetime, timezone
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
                (r.get("versionCodes", []), r.get("name", ""), r.get("status", ""), r.get("userFraction"))
                for r in track.get("releases", [])
                if r.get("versionCodes")  # an empty draft (no bundle) says nothing
            ]
            if releases:
                out[track["track"]] = releases
        return out
    finally:
        session.delete(f"{API}/{package}/edits/{edit_id}")  # read only: nothing is ever committed


def gh(*args):
    return subprocess.run(["gh", *args], capture_output=True, text=True, timeout=60, check=True).stdout


def last_releases():
    """{app folder: "dd/mm (n d) action → track: conclusion"} from the Release workflow's runs; {} without gh."""
    try:
        runs = json.loads(gh("run", "list", "--workflow", "release.yml", "--limit", "40",
                             "--json", "databaseId,displayTitle,createdAt,conclusion,status"))
    except (OSError, subprocess.SubprocessError, ValueError):
        return {}
    last = {}
    for run in runs:  # newest first
        # Titles read "<app> · <action> → <track>"; older runs are just "Release": their job is named
        # "Publish <app> (<action> → <track>)".
        title = run["displayTitle"]
        if " · " in title:
            app, what = title.split(" · ", 1)
        else:
            try:
                jobs = json.loads(gh("run", "view", str(run["databaseId"]), "--json", "jobs"))["jobs"]
            except (OSError, subprocess.SubprocessError, ValueError):
                continue
            job = next((re.match(r"(?:Publish|Dry run) (\S+) \((.+)\)", j["name"]) for j in jobs
                        if j["name"].startswith(("Publish", "Dry run"))), None)
            if not job:
                continue
            app, what = job.group(1), job.group(2) + (" (dry run)" if "Dry run" in jobs[0]["name"] else "")
        if app in last:
            continue
        when = datetime.fromisoformat(run["createdAt"].replace("Z", "+00:00"))
        days = (datetime.now(timezone.utc) - when).days
        last[app] = f"{when:%d/%m} ({days} d) {what}: {run['conclusion'] or run['status']}"
    return last


def show(releases):
    """One line per release: versionCodes, status (rollout share), and Play's release name if it says more."""
    if not releases:
        return "—"
    lines = []
    for codes, name, status, fraction in releases:
        line = f"{','.join(codes) or '—'} · {status}"
        if fraction:
            line += f" {float(fraction):.0%}"
        if name and not name.startswith(",".join(codes)):
            line += f" (named \"{name}\")"
        lines.append(line)
    return "<br>".join(lines)


def main():
    session = requests.Session()
    session.headers["Authorization"] = "Bearer " + credentials().token
    runs = last_releases()
    rows = ["| App | Repo | Production | Internal | Other tracks | Last Release run |", "|---|---|---|---|---|---|"]
    for folder, package, code, name in repo_apps():
        tracks = play_tracks(session, package)
        if tracks is None:
            rows.append(f"| {folder} | {code} ({name}) | not on Play | | | {runs.get(folder, '—')} |")
            continue
        others = "<br>".join(f"{t}: {show(r)}" for t, r in tracks.items() if t not in ("production", "internal"))
        live = max((int(v) for codes, _, s, _ in tracks.get("production", []) if s == "completed" for v in codes), default=0)
        ahead = " ⬆️" if code and code > live else ""
        rows.append(
            f"| {folder} | {code} ({name}){ahead} | {show(tracks.get('production'))} | "
            f"{show(tracks.get('internal'))} | {others} | {runs.get(folder, '—')} |"
        )
    print("\n".join(rows))
    print("\n⬆️ the repo is ahead of what production serves. Status: completed = live, or in review if the last "
          "run sent it in the last few days; draft = not sent for review yet; inProgress = staged rollout (share of "
          "users); halted = rollout stopped. \"In review\" only shows in Play Console.")


if __name__ == "__main__":
    main()
