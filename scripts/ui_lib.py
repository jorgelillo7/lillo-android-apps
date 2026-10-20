#!/usr/bin/env python3
"""Drives any app in this repo on the running emulator or phone, by what is on screen (text, content
description or Compose test tag) instead of fixed coordinates, so steps survive layout, language
and orientation changes. Compose test tags show up when the app sets testTagsAsResourceId = true.

As a library (UI batteries, e.g. apps/whos-lying/tools/battery_test.py):
    sys.path.insert(0, str(ROOT / "scripts")); import ui_lib as ui
    ui.tap("start", scroll=True); ui.wait_for("result_title"); ui.screenshot(path)

From the shell (tap-text.sh and smoke_lib.sh use these):
    python3 scripts/ui_lib.py tap "<text|tag>" [wait-seconds]
    python3 scripts/ui_lib.py hide-keyboard
    python3 scripts/ui_lib.py screenshot <out.png>
"""
import html
import re
import subprocess
import sys
import time
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ADB = subprocess.run(["bash", "-c", f"source '{ROOT}/scripts/env.sh' >/dev/null; echo $ADB"],
                     capture_output=True, text=True).stdout.strip()


def adb(*args):
    return subprocess.run([ADB, *args], capture_output=True, text=True).stdout


def shell(*args):
    return adb("shell", *args)


class Node:
    def __init__(self, attrs):
        self.text = html.unescape(attrs.get("text", ""))
        self.desc = html.unescape(attrs.get("content-desc", ""))
        self.tag = attrs.get("resource-id", "").split("/")[-1]
        self.checked = attrs.get("checked") == "true"
        x1, y1, x2, y2 = map(int, re.findall(r"\d+", attrs.get("bounds", "[0,0][0,0]")))
        self.center = ((x1 + x2) // 2, (y1 + y2) // 2)

    def matches(self, target):
        return target in (self.text, self.desc, self.tag)


def dump():
    """The nodes on screen; the first one is the whole screen. Retries while the app is busy
    (dumps fail during animations)."""
    for _ in range(10):
        shell("rm", "-f", "/sdcard/ui.xml")
        shell("uiautomator", "dump", "/sdcard/ui.xml")
        xml = adb("exec-out", "cat", "/sdcard/ui.xml")
        if "<hierarchy" in xml:
            return [Node(dict(re.findall(r'([\w-]+)="([^"]*)"', n))) for n in re.findall(r"<node [^>]*>", xml)]
        time.sleep(1.5)
    raise RuntimeError("UI dump keeps failing")


def screen_size(nodes=None):
    return tuple(2 * c for c in (nodes or dump())[0].center)


def find(nodes, tag=None, text=None, prefix=None, any_of=None):
    """The first node with that test tag, that exact text, a tag starting with prefix, or (any_of)
    whose text, content description or tag is that."""
    for n in nodes:
        if (tag and n.tag == tag or text and n.text == text or prefix and n.tag.startswith(prefix)
                or any_of and n.matches(any_of)):
            return n
    return None


def wait_for(tag=None, text=None, timeout=15):
    """Waits until the tag or text is on screen and returns the nodes."""
    end = time.time() + timeout
    while time.time() < end:
        nodes = dump()
        if find(nodes, tag, text):
            return nodes
        time.sleep(1)
    raise AssertionError(f"not on screen: {tag or text}")


def tap(target, wait=1.5, timeout=15, scroll=False):
    """Taps the node whose text, content description or tag is target; with scroll, swipes up until
    it shows (long lists in landscape take many swipes, so it allows more time)."""
    end = time.time() + (max(timeout, 45) if scroll else timeout)
    while time.time() < end:
        node = find(dump(), any_of=target)
        if node:
            shell("input", "tap", *map(str, node.center))
            time.sleep(wait)
            return
        if scroll:
            swipe_up(end=0.41)
        time.sleep(1)
    raise AssertionError(f"can't tap {target}")


def swipe_up(start=0.70, end=0.45, ms=400, wait=True):
    """Drags up the middle of the screen, in fractions of its height (portrait and landscape alike).
    wait=False returns the running swipe, to read the screen while a finger is still down."""
    w, h = screen_size()
    args = [ADB, "shell", "input", "swipe", str(w // 2), str(int(h * start)), str(w // 2), str(int(h * end)), str(ms)]
    return subprocess.run(args, capture_output=True) if wait else subprocess.Popen(args)


def type_text(text):
    shell("input", "text", text.replace(" ", "%s"))


def hide_keyboard():
    """Closes the keyboard only if it is up: ESCAPE doesn't always, and BACK without a keyboard
    leaves the screen."""
    for _ in range(3):
        if "mInputShown=true" not in shell("dumpsys", "input_method"):
            return
        shell("input", "keyevent", "KEYCODE_BACK")
        time.sleep(1)


def rotate(landscape):
    """Turns the phone. Something resets the rotation once when a run starts, so it is checked back."""
    want = "1" if landscape else "0"
    for _ in range(5):
        shell("settings", "put", "system", "accelerometer_rotation", "0")
        shell("settings", "put", "system", "user_rotation", want)
        time.sleep(2)
        if shell("settings", "get", "system", "user_rotation").strip() == want:
            return
    raise AssertionError("the phone won't stay turned")


def screenshot(path):
    """Saves a PNG of the screen, after closing "isn't responding" dialogs (busy emulators show them)."""
    for _ in range(3):
        if "Application Not Responding" not in shell("dumpsys", "window"):
            break
        node = find(dump(), tag="aerr_wait")
        if node:
            shell("input", "tap", *map(str, node.center))
        time.sleep(4)
    path = Path(path)
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_bytes(subprocess.run([ADB, "exec-out", "screencap", "-p"], capture_output=True).stdout)
    return path


def install_release(app, package):
    """Clean install of apps/<app>'s release APK (a debug build on the device has another signature)."""
    adb("uninstall", package)
    print(adb("install", "-r", str(ROOT / f"apps/{app}/app/build/outputs/apk/release/app-release.apk")).strip().splitlines()[-1])
    adb("logcat", "-c")


def crashes(package):
    """Crashes of that app since install_release (not of other processes, e.g. uiautomator itself)."""
    return adb("logcat", "-d", "-b", "crash").count(f"Process: {package}")


if __name__ == "__main__":
    command, *rest = sys.argv[1:] or [""]
    if command == "tap" and rest:
        try:
            tap(rest[0], wait=float(rest[1]) if len(rest) > 1 else 4, timeout=12)
        except AssertionError:
            sys.exit(f'tap: "{rest[0]}" not found on screen')
    elif command == "hide-keyboard":
        hide_keyboard()
    elif command == "screenshot" and rest:
        print(f"Saved {screenshot(rest[0])}")
    else:
        sys.exit(__doc__)
