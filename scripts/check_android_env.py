#!/usr/bin/env python3
"""Health of the local Android setup and of what the repo says about it, as one Markdown table.

Covers what Renovate can't see: SDK packages, AVDs, Android Studio, the API levels scattered over
the repo, Google Play's targetSdk deadline, free disk and Renovate itself. Read only.

  source scripts/env.sh && python3 scripts/check_android_env.py

Exit code 1 if anything is red.
"""

import json
import os
import re
import shutil
import subprocess
import sys
from datetime import date, datetime, timedelta, timezone
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
HOME = Path.home()
SDK = Path(os.environ.get("ANDROID_HOME", HOME / "Library/Android/sdk"))
AVD_DIR = Path(os.environ.get("ANDROID_AVD_HOME", HOME / ".android/avd"))
STUDIO = Path("/Applications/Android Studio.app")

RED, YELLOW, GREEN = "🔴", "🟡", "🟢"

# Older tools can't read an API level with a minor version: AVDs come out as target=android-0.
MIN_CMDLINE_TOOLS = 20
# (deadline, API level apps must target). Google confirms each year's; 37 is the expected date.
PLAY_DEADLINES = [(date(2026, 8, 31), 36), (date(2027, 8, 31), 37)]
PLAY_WARNING_DAYS = 120
DISK_RED_GB, DISK_YELLOW_GB = 6, 15
RENOVATE_STALE_DAYS = 14

rows = []


def report(status, area, state, fix=""):
    rows.append((status, area, state, fix))


def run(*command):
    """Stdout of a command, or None if it can't run or fails."""
    try:
        result = subprocess.run(command, capture_output=True, text=True, timeout=120, check=False)
    except (OSError, subprocess.TimeoutExpired):
        return None
    return result.stdout if result.returncode == 0 else None


def read(relative):
    path = ROOT / relative
    return path.read_text(encoding="utf-8") if path.exists() else ""


def search(pattern, text):
    match = re.search(pattern, text, re.MULTILINE)
    return match.group(1) if match else None


def properties(path):
    """key=value file, tolerating spaces around the =."""
    if not path.exists():
        return {}
    pairs = (line.split("=", 1) for line in path.read_text(encoding="utf-8").splitlines() if "=" in line)
    return {key.strip(): value.strip() for key, value in pairs}


def major(api):
    """37 from '37', '37.0' or 'android-37.0'; None if there is no level."""
    level = search(r"(\d+)(?:\.\d+)?$", str(api or ""))
    return int(level) if level else None


def default_avd():
    return search(r'avd="\$\{1:-([^}]+)\}"', read("scripts/emulator.sh"))


def check_cmdline_tools():
    revision = properties(SDK / "cmdline-tools/latest/source.properties").get("Pkg.Revision")
    if not revision:
        report(RED, "Command-line Tools", "not installed", "Install them from Android Studio's SDK Manager")
    elif int(revision.split(".")[0]) < MIN_CMDLINE_TOOLS:
        report(
            RED,
            "Command-line Tools",
            f"{revision}: creates broken API 37 AVDs",
            'sdkmanager "cmdline-tools;latest"',
        )
    else:
        report(GREEN, "Command-line Tools", revision)


def check_sdk_updates():
    listing = run(str(SDK / "cmdline-tools/latest/bin/sdkmanager"), "--list")
    if listing is None:
        report(YELLOW, "SDK packages", "couldn't run sdkmanager", "source scripts/env.sh, then retry")
        return
    updates = re.findall(r"^\s+(\S+)\s+(\S+)\s+->\s+(\S+)", listing, re.MULTILINE)
    if updates:
        names = ", ".join(f"{name} {old} → {new}" for name, old, new in updates)
        report(YELLOW, "SDK packages", names, "sdkmanager --update")
    else:
        report(GREEN, "SDK packages", "no updates")


def check_avd(ini):
    name = ini.stem
    area = f"AVD {name}"
    fields = properties(ini)
    target = fields.get("target", "")
    config = properties(Path(fields.get("path", AVD_DIR / f"{name}.avd")) / "config.ini")
    sysdir = config.get("image.sysdir.1", "")
    image_api = search(r"android-([\d.]+)", sysdir)
    if not sysdir:
        report(RED, area, "no system image in config.ini", "Recreate the AVD")
    elif not (SDK / sysdir / "system.img").exists():
        report(RED, area, f"image missing: {sysdir}", f'sdkmanager "{sysdir.strip("/").replace("/", ";")}"')
    elif not major(target) or major(target) != major(image_api):
        report(
            RED,
            area,
            f"target={target or 'missing'}, image is android-{image_api}: boots without the hypervisor",
            f"Set target=android-{image_api} in {ini}",
        )
    else:
        report(GREEN, area, target)


def check_avds():
    inis = sorted(AVD_DIR.glob("*.ini"))
    for ini in inis:
        check_avd(ini)
    wanted = default_avd()
    if wanted and not (AVD_DIR / f"{wanted}.ini").exists():
        report(RED, "Default AVD", f"{wanted} (scripts/emulator.sh) doesn't exist", "Create it with avdmanager")


def check_repo_levels():
    """Every place that states an API level must agree with the version catalog."""
    catalog = read("gradle/libs.versions.toml")
    target = major(search(r'^targetSdk = "([^"]+)"', catalog))
    compile_sdk = major(search(r'^compileSdk = "([^"]+)"', catalog))
    avd = default_avd()
    toolchain = re.search(r"compileSdk / targetSdk / minSdk \| (\d+) / (\d+) /", read("docs/setup/toolchain.md"))
    stated = {
        "smoke.yml api-level": major(search(r'api-level: "([^"]+)"', read(".github/workflows/smoke.yml"))),
        "emulator.sh default AVD": major(search(r"API_(\d+)$", avd or "")),
        "docs/operations.md AVD": major(search(r"default `Pixel_\d+_API_(\d+)`", read("docs/operations.md"))),
        "README badge": major(search(r"badge/targetSdk-(\d+)", read("README.md"))),
        "CLAUDE.md": major(search(r"the apps target (\d+)", read("CLAUDE.md"))),
        "toolchain.md": int(toolchain.group(2)) if toolchain else None,
    }
    wrong = [f"{where}: {level}" for where, level in stated.items() if level != target]
    if wrong:
        report(YELLOW, "API levels in the repo", f"targetSdk is {target}; " + ", ".join(wrong), "Align them")
    else:
        report(GREEN, "API levels in the repo", f"all at {target}")
    if not list((SDK / "platforms").glob(f"android-{compile_sdk}*")):
        report(RED, "compileSdk platform", f"android-{compile_sdk} not installed", f'sdkmanager "platforms;android-{compile_sdk}.0"')
    return target


def check_readme_badges():
    catalog, readme = read("gradle/libs.versions.toml"), read("README.md")
    wrong = []
    for badge, entry in (("AGP", "androidGradlePlugin"), ("Kotlin", "kotlin")):
        shown = search(rf"badge/{badge}-([\d.]+)-", readme)
        version = search(rf'^{entry} = "(\d+\.\d+)', catalog)
        if shown != version:
            wrong.append(f"{badge}: README {shown}, catalog {version}")
    if wrong:
        report(YELLOW, "README badges", ", ".join(wrong), "Update the badges in README.md")
    else:
        report(GREEN, "README badges", "match the catalog")


def check_play_deadline(target, today=None):
    today = today or date.today()
    passed = [item for item in PLAY_DEADLINES if item[0] <= today]
    upcoming = [item for item in PLAY_DEADLINES if item[0] > today]
    if not upcoming:
        report(YELLOW, "Play targetSdk deadline", "no future deadline known", "Update PLAY_DEADLINES in this script")
    required = passed[-1][1] if passed else 0
    if target < required:
        report(RED, "Play targetSdk deadline", f"targets {target}, Play requires {required}", "Raise targetSdk")
        return
    if upcoming:
        deadline, level = upcoming[0]
        days = (deadline - today).days
        if target < level and days <= PLAY_WARNING_DAYS:
            report(YELLOW, "Play targetSdk deadline", f"{level} required in {days} days ({deadline})", "Raise targetSdk")
            return
        report(GREEN, "Play targetSdk deadline", f"targets {target}; next is {level} by {deadline}")
    elif passed:
        report(GREEN, "Play targetSdk deadline", f"targets {target}; {required} required")


def numbers(build):
    return tuple(int(part) for part in re.findall(r"\d+", build))


def check_studio():
    plist = STUDIO / "Contents/Info.plist"
    if not plist.exists():
        report(YELLOW, "Android Studio", "not found in /Applications")
        return
    installed = run("/usr/libexec/PlistBuddy", "-c", "Print CFBundleVersion", str(plist))
    feed = run("curl", "-sL", "--max-time", "60", "https://jb.gg/android-studio-releases-list.json")
    try:
        releases = json.loads(feed)["content"]["item"]
        stable = next(item for item in releases if item.get("channel") in ("Release", "Patch"))
    except (TypeError, ValueError, KeyError, StopIteration):
        report(YELLOW, "Android Studio", "couldn't read Google's release list", "Check it by hand")
        return
    current = next((item["name"] for item in releases if item.get("build") == installed.strip()), installed.strip())
    if numbers(installed) < numbers(stable["build"]):
        report(YELLOW, "Android Studio", f"{current} → {stable['name']}", "scripts/update_android_studio.sh")
    else:
        report(GREEN, "Android Studio", current)


def check_jdk():
    java_home = os.environ.get("JAVA_HOME", "")
    if STUDIO.exists() and not java_home.startswith(str(STUDIO)):
        report(RED, "JDK", f"JAVA_HOME={java_home or 'unset'}, not Studio's", "source scripts/env.sh")
    else:
        report(GREEN, "JDK", "Android Studio's JBR" if STUDIO.exists() else java_home)


def check_disk():
    free = shutil.disk_usage(HOME).free / 1024**3
    fix = "Free space: a system image takes over 5 GB"
    if free < DISK_RED_GB:
        report(RED, "Disk", f"{free:.0f} GB free", fix)
    elif free < DISK_YELLOW_GB:
        report(YELLOW, "Disk", f"{free:.0f} GB free", fix)
    else:
        report(GREEN, "Disk", f"{free:.0f} GB free")


def section_count(body, title):
    """Checkbox lines under one '## title' of the Renovate dashboard, without its 'all at once' box."""
    match = re.search(rf"^## {title}\b(.*?)(?=^## |\Z)", body, re.MULTILINE | re.DOTALL)
    if not match:
        return 0
    lines = re.findall(r"^ - \[.\] .*$", match.group(1), re.MULTILINE)
    return len([line for line in lines if "-all-" not in line and "🔐" not in line])


def check_renovate(now=None):
    now = now or datetime.now(timezone.utc)
    issues = run("gh", "issue", "list", "--state", "open", "--search", "Dependency Dashboard in:title", "--json", "body,title")
    prs = run("gh", "pr", "list", "--state", "all", "--limit", "1000", "--json", "number,author,state,createdAt,title")
    if issues is None or prs is None:
        report(YELLOW, "Renovate", "couldn't query GitHub", "gh auth status")
        return
    dashboard = next((item for item in json.loads(issues) if item["title"] == "Dependency Dashboard"), None)
    if not dashboard:
        report(YELLOW, "Renovate", "no Dependency Dashboard issue", "Check the Renovate app on the repo")
        return
    body = dashboard["body"]
    awaiting = section_count(body, "Awaiting Schedule")
    errored = section_count(body, "Errored")
    own = [pr for pr in json.loads(prs) if "renovate" in pr["author"]["login"]]
    open_prs = [pr for pr in own if pr["state"] == "OPEN"]
    limit = now - timedelta(days=RENOVATE_STALE_DAYS)
    stale = [pr for pr in open_prs if datetime.fromisoformat(pr["createdAt"].replace("Z", "+00:00")) < limit]
    state = f"{awaiting} awaiting schedule, {len(open_prs)} open PRs, {errored} errored"
    if errored:
        report(YELLOW, "Renovate", state, "Read the errored updates in the Dependency Dashboard issue")
    elif stale:
        report(YELLOW, "Renovate", f"{state}; {len(stale)} open over {RENOVATE_STALE_DAYS} days", "Merge or close them")
    elif not own and awaiting:
        report(YELLOW, "Renovate", f"{state}; it has never opened a PR", "Tick 'Create all awaiting schedule PRs' or widen the schedule in renovate.json")
    else:
        report(GREEN, "Renovate", state)


def main():
    check_cmdline_tools()
    check_sdk_updates()
    check_avds()
    check_play_deadline(check_repo_levels())
    check_readme_badges()
    check_studio()
    check_jdk()
    check_disk()
    check_renovate()
    order = {RED: 0, YELLOW: 1, GREEN: 2}
    print("| | Check | State | Fix |\n|---|---|---|---|")
    for status, area, state, fix in sorted(rows, key=lambda row: order[row[0]]):
        shown_fix = f"`{fix}`" if fix.startswith(("sdkmanager", "source", "gh ", "scripts/")) else fix
        print(f"| {status} | {area} | {state.replace('|', '·')} | {shown_fix} |")
    return 1 if any(row[0] == RED for row in rows) else 0


if __name__ == "__main__":
    sys.exit(main())
