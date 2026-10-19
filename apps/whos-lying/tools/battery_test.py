#!/usr/bin/env python3
"""Plays every way a game of Who's lying? can go, on the running emulator, with the release APK.

While the phone is passed around, the script peeks under each curtain (the word, or "You are the
impostor / Drifter") so it knows who is who, then votes on purpose to force each ending: civilians
win, impostors win, the Drifter guesses right or wrong, the Drifter wins alone or with the
impostors, the group ends the game early, a chaos game, a timed vote, the drawing mode, a 3-round
match up to the podium, word packs, Spanish and landscape. Every result title is checked against
the expected one and every scenario leaves a screenshot in build/battery/whos-lying/.

Usage (repo root, emulator running, release APK built):
    ./gradlew :apps:whos-lying:app:assembleRelease && ./gradlew --stop
    scripts/emulator.sh start --headless
    python3 apps/whos-lying/tools/battery_test.py [scenario ...]
"""
import html
import re
import subprocess
import sys
import time
from collections import Counter
from pathlib import Path

ROOT = Path(__file__).resolve().parents[3]
PKG = "com.jorgelillo.whoslying"
APK = ROOT / "apps/whos-lying/app/build/outputs/apk/release/app-release.apk"
OUT = ROOT / "build/battery/whos-lying"
ADB = subprocess.run(["bash", "-c", f"source {ROOT}/scripts/env.sh >/dev/null; echo $ADB"], capture_output=True, text=True).stdout.strip()

TITLES = {
    "caught_one": "You caught the impostor!",
    "caught_all": "You caught them all!",
    "impostors": "The impostors win!",
    "both": "Impostors and Drifter win!",
    "drifter": "The Drifter wins!",
    "guessed": "The Drifter guessed the word!",
    "nobody": "Everyone was an impostor! 🎭",
    "ended": "Game over",
}


# ── adb helpers ────────────────────────────────────────────────────────────────────────────────

def adb(*args, check=False):
    return subprocess.run([ADB, *args], capture_output=True, text=True, check=check).stdout


def shell(*args):
    return adb("shell", *args)


class Node:
    def __init__(self, attrs):
        self.text = html.unescape(attrs.get("text", ""))
        self.tag = attrs.get("resource-id", "").split("/")[-1]
        self.checked = attrs.get("checked") == "true"
        x1, y1, x2, y2 = map(int, re.findall(r"\d+", attrs.get("bounds", "[0,0][0,0]")))
        self.center = ((x1 + x2) // 2, (y1 + y2) // 2)


def dump():
    """The nodes on screen; retries while the app is busy (dumps fail during animations)."""
    for _ in range(10):
        shell("rm", "-f", "/sdcard/ui.xml")
        shell("uiautomator", "dump", "/sdcard/ui.xml")
        xml = adb("exec-out", "cat", "/sdcard/ui.xml")
        if "<hierarchy" in xml:
            return [Node(dict(re.findall(r'([\w-]+)="([^"]*)"', n))) for n in re.findall(r"<node [^>]*>", xml)]
        time.sleep(1.5)
    raise RuntimeError("UI dump keeps failing")


def find(nodes, tag=None, text=None, prefix=None):
    for n in nodes:
        if tag and n.tag == tag or text and n.text == text or prefix and n.tag.startswith(prefix):
            return n
    return None


def wait_for(tag=None, text=None, timeout=15):
    end = time.time() + timeout
    while time.time() < end:
        nodes = dump()
        if find(nodes, tag, text):
            return nodes
        time.sleep(1)
    raise AssertionError(f"not on screen: {tag or text}")


def tap(tag, wait=1.5, timeout=15, scroll=False):
    end = time.time() + timeout
    while time.time() < end:
        node = find(dump(), tag)
        if node:
            shell("input", "tap", *map(str, node.center))
            time.sleep(wait)
            return
        if scroll:
            shell("input", "swipe", "540", "1700", "540", "1000", "400")
        time.sleep(1)
    raise AssertionError(f"can't tap {tag}")


def type_text(text):
    shell("input", "text", text.replace(" ", "%s"))


def shot(name):
    OUT.mkdir(parents=True, exist_ok=True)
    subprocess.run([str(ROOT / "scripts/screenshot.sh"), str(OUT / f"{name}.png")], capture_output=True)


# ── app flows ──────────────────────────────────────────────────────────────────────────────────

def fresh(locale="en-US"):
    """Clean install state, given language, past the first-launch welcome."""
    shell("pm", "clear", PKG)
    shell("cmd", "locale", "set-app-locales", PKG, "--locales", locale)
    shell("am", "start", "-W", "-n", f"{PKG}/.MainActivity")
    time.sleep(4)
    tap("welcome_ok", timeout=25)


def players_and_setup(names):
    tap("play", wait=2)
    for name in names:
        tap("player_name", wait=0.5)
        type_text(name)
        shell("input", "keyevent", "KEYCODE_ENTER")
        time.sleep(0.8)
    shell("input", "keyevent", "KEYCODE_ESCAPE")
    time.sleep(1)
    tap("continue", wait=2)


def set_switch(tag, on):
    for _ in range(6):
        node = find(dump(), tag)
        if node:
            if node.checked != on:
                shell("input", "tap", *map(str, node.center))
                time.sleep(1)
            return
        shell("input", "swipe", "540", "1700", "540", "1100", "400")
        time.sleep(1)
    raise AssertionError(f"no switch {tag}")


def configure(mode="classic", impostors=1, drawing=False, chaos=False, hint=True, rounds=None, timer_steps=0):
    tap(f"mode_{mode}", wait=1)
    for _ in range(impostors - 1):
        tap("impostors_plus", wait=0.8, scroll=True)
    set_switch("chaos", chaos)
    set_switch("drawing", drawing)
    if mode != "classic":
        set_switch("hint", hint)
    if rounds is not None:
        for _ in range(8):
            node = find(dump(), "rounds")
            if node is None:
                shell("input", "swipe", "540", "1700", "540", "1100", "400"); time.sleep(1); continue
            if node.text == str(rounds):
                break
            tap("rounds_minus", wait=0.8)
    for _ in range(timer_steps):
        tap("timer_plus", wait=0.8, scroll=True)


def reveal_all_cards(count):
    """Passes the phone around; returns {player: word or 'IMPOSTOR' / 'DRIFTER'} seen under each curtain."""
    seen = {}
    for _ in range(count):
        nodes = wait_for("tap_to_reveal", timeout=20)
        texts = [n.text for n in nodes]
        player = texts[texts.index("Pass the phone to") + 1] if "Pass the phone to" in texts else texts[texts.index("Pásale el móvil a") + 1]
        swipe = subprocess.Popen([ADB, "shell", "input", "swipe", "540", "1700", "540", "700", "4000"])
        time.sleep(2)
        nodes = dump()
        word = find(nodes, "secret_word")
        if word:
            seen[player] = word.text
        elif find(nodes, text="You are the impostor") or find(nodes, text="Eres el impostor"):
            seen[player] = "IMPOSTOR"
        elif find(nodes, text="You are the Drifter") or find(nodes, text="Eres el Despistado"):
            seen[player] = "DRIFTER"
        else:
            raise AssertionError(f"couldn't read {player}'s card: {[n.text for n in nodes if n.text]}")
        swipe.wait()
        tap("hide", wait=1.2)
    return seen


def roles(seen):
    """Who is who from what each card showed: the majority word is the civilians'."""
    words = Counter(w for w in seen.values() if w not in ("IMPOSTOR", "DRIFTER"))
    civilian_word = words.most_common(1)[0][0] if words else None
    role = {}
    for p, w in seen.items():
        role[p] = w if w in ("IMPOSTOR", "DRIFTER") else ("CIVILIAN" if w == civilian_word and words[w] > 1 or len(words) == 1 else "IMPOSTOR")
    return role, civilian_word


def to_vote(drawing=False):
    if drawing:
        canvas = find(wait_for("canvas", timeout=20), "canvas")
        x, y = canvas.center
        shell("input", "swipe", str(x - 200), str(y), str(x + 200), str(y + 150), "600")
        time.sleep(1)
    tap("go_vote", wait=2, timeout=20)


def vote(player):
    tap(f"vote_{player}", wait=0.8)
    tap("confirm_vote", wait=4)


def after_vote():
    """'result', 'guess' or 'continue' once the exposing countdown is over."""
    end = time.time() + 20
    while time.time() < end:
        nodes = dump()
        if find(nodes, "result_title"):
            return "result", nodes
        if find(nodes, "guess_text"):
            return "guess", nodes
        if find(nodes, "keep_playing"):
            return "continue", nodes
        time.sleep(1)
    raise AssertionError("stuck after the vote")


def play(order, drawing=False, guess=None):
    """Votes the players in [order] out one by one until the game ends; returns the result title."""
    to_vote(drawing)
    for player in order:
        vote(player)
        state, _ = after_vote()
        if state == "guess":
            tap("guess_text", wait=0.5)
            type_text(guess)
            shell("input", "keyevent", "KEYCODE_ESCAPE")
            time.sleep(0.5)
            tap("guess", wait=3)
            state, _ = after_vote()
        if state == "result":
            break
        tap("keep_playing", wait=2)
        to_vote(drawing)
    nodes = wait_for("result_title", timeout=20)
    return find(nodes, "result_title").text


def expect(title, key, name):
    shot(name)
    if title != TITLES[key]:
        raise AssertionError(f"{name}: expected «{TITLES[key]}», got «{title}»")
    print(f"  ✔ {name}: {title}")


def new_game(names, **config):
    """From home with a clean app: players, setup, deal. Returns roles and the civilians' word."""
    fresh()
    players_and_setup(names)
    configure(**config)
    tap("start", wait=2, scroll=True)
    seen = reveal_all_cards(len(names))
    return roles(seen)


# ── scenarios ──────────────────────────────────────────────────────────────────────────────────

THREE = ["Ana", "Bea", "Carlos"]
FIVE = ["Ana", "Bea", "Carlos", "Dani", "Eva"]


def by(role, r):
    return [p for p, x in r.items() if x == role]


def classic_civilians():
    r, _ = new_game(THREE, mode="classic")
    expect(play(by("IMPOSTOR", r)), "caught_one", "classic_civilians_win")


def classic_impostors():
    r, _ = new_game(THREE, mode="classic")
    expect(play(by("CIVILIAN", r)[:1]), "impostors", "classic_impostors_win")


def blind_civilians():
    r, _ = new_game(THREE, mode="blind")
    expect(play(by("IMPOSTOR", r)), "caught_one", "blind_civilians_win")


def blind_impostors():
    r, _ = new_game(THREE, mode="blind", hint=False)
    expect(play(by("CIVILIAN", r)[:1]), "impostors", "blind_impostors_win_no_hint")


def two_impostors_caught():
    r, _ = new_game(FIVE, mode="classic", impostors=2)
    assert len(by("IMPOSTOR", r)) == 2, r
    expect(play(by("IMPOSTOR", r)), "caught_all", "classic_two_impostors_caught")


def drifter_guesses_right():
    r, word = new_game(FIVE, mode="drifter")
    expect(play(by("DRIFTER", r), guess=word), "guessed", "drifter_guesses_right")


def drifter_guesses_wrong():
    r, _ = new_game(FIVE, mode="drifter")
    expect(play(by("DRIFTER", r) + by("IMPOSTOR", r), guess="zzz"), "caught_all", "drifter_wrong_guess_then_civilians_win")


def drifter_wins_alone():
    r, _ = new_game(FIVE, mode="drifter")
    expect(play(by("IMPOSTOR", r) + by("CIVILIAN", r)[:2]), "drifter", "drifter_wins_alone")


def impostor_and_drifter_win():
    r, _ = new_game(FIVE, mode="drifter")
    expect(play(by("CIVILIAN", r)[:2]), "both", "impostor_and_drifter_win")


def drawing_mode():
    r, _ = new_game(THREE, mode="classic", drawing=True)
    assert "IMPOSTOR" in r.values(), f"drawing impostors draw blind: {r}"
    expect(play(by("IMPOSTOR", r), drawing=True), "caught_one", "drawing_civilians_win")


def ended_early():
    new_game(THREE, mode="classic")
    tap("go_vote", wait=2, timeout=20)
    tap("reveal_all", wait=3)
    expect(find(wait_for("result_title"), "result_title").text, "ended", "ended_early")


def chaos_game():
    fresh()
    players_and_setup(FIVE)
    configure(mode="classic", chaos=True)
    tap("start", wait=2, scroll=True)
    r, _ = roles(reveal_all_cards(len(FIVE)))
    title = play(list(r))   # vote everyone in turn: every chaos deal must reach an end
    assert title in TITLES.values(), title
    shot("chaos")
    print(f"  ✔ chaos: {title}")


def timer_runs_out():
    fresh()
    players_and_setup(THREE)
    configure(mode="classic", timer_steps=1)  # 1 minute
    tap("start", wait=2, scroll=True)
    r, _ = roles(reveal_all_cards(3))
    wait_for("time_up", timeout=90)
    shot("timer_time_up")
    vote(by("IMPOSTOR", r)[0])
    expect(find(wait_for("result_title"), "result_title").text, "caught_one", "timer_vote_forced")


def match_to_podium():
    fresh()
    players_and_setup(THREE)
    configure(mode="classic", rounds=3)
    tap("start", wait=2, scroll=True)
    for round_ in range(1, 4):
        r, _ = roles(reveal_all_cards(3))
        play(by("IMPOSTOR", r))
        if round_ < 3:
            tap("play_again", wait=2)
            wait_for("next_round")
            shot(f"match_ranking_{round_}")
            tap("next_round", wait=2)
    tap("see_winner", wait=3)
    wait_for("podium_title")
    shot("match_podium")
    print("  ✔ match: 3 rounds, ranking after each, podium")


def packs():
    fresh()
    tap("packs", wait=2)
    tap("new_pack", wait=2)
    tap("pack_name", wait=0.5); type_text("Battery"); shell("input", "keyevent", "KEYCODE_ESCAPE")
    tap("pack_words", wait=0.5); type_text("Mar / Lago"); shell("input", "keyevent", "KEYCODE_ENTER")
    type_text("Sol / Luna"); shell("input", "keyevent", "KEYCODE_ESCAPE"); time.sleep(1)
    tap("save_pack", wait=2)
    nodes = wait_for(text="Battery")
    shot("packs_custom")
    # Built-in packs: edit makes a copy.
    builtin = next(n for n in nodes if n.tag.startswith("pack_") and n.text != "Battery" and not n.tag.startswith("pack_custom"))
    shell("input", "tap", *map(str, builtin.center)); time.sleep(2)
    if find(dump(), "copy_pack_ok"):
        tap("copy_pack_ok", wait=2)
        shot("packs_copy")
    print("  ✔ packs: custom pack saved, built-in pack copy offered")


def spanish():
    fresh("es-ES")
    players_and_setup(THREE)
    configure(mode="classic")
    tap("start", wait=2, scroll=True)
    r, _ = roles(reveal_all_cards(3))
    title = play(by("IMPOSTOR", r))
    shot("spanish_result")
    assert title and title != TITLES["caught_one"], f"not in Spanish: {title}"
    print(f"  ✔ spanish: {title}")


def landscape():
    shell("settings", "put", "system", "accelerometer_rotation", "0")
    shell("settings", "put", "system", "user_rotation", "1")
    try:
        fresh()
        players_and_setup(THREE)
        configure(mode="classic")
        tap("start", wait=2, scroll=True)
        shot("landscape_curtain")
        r, _ = roles(reveal_all_cards(3))
        to_vote()
        shot("landscape_vote")
        vote(by("IMPOSTOR", r)[0])
        expect(find(wait_for("result_title"), "result_title").text, "caught_one", "landscape_result")
    finally:
        shell("settings", "put", "system", "user_rotation", "0")


SCENARIOS = [classic_civilians, classic_impostors, blind_civilians, blind_impostors, two_impostors_caught,
             drifter_guesses_right, drifter_guesses_wrong, drifter_wins_alone, impostor_and_drifter_win,
             drawing_mode, ended_early, chaos_game, timer_runs_out, match_to_podium, packs, spanish, landscape]


def main():
    wanted = sys.argv[1:]
    adb("uninstall", PKG)
    print(adb("install", "-r", str(APK)).strip().splitlines()[-1])
    adb("logcat", "-c")
    failed = []
    for scenario in SCENARIOS:
        if wanted and scenario.__name__ not in wanted:
            continue
        print(f"▶ {scenario.__name__}")
        try:
            scenario()
        except Exception as e:  # keep going: report every scenario
            failed.append(scenario.__name__)
            shot(f"FAILED_{scenario.__name__}")
            print(f"  ✘ {e}")
    crashes = adb("logcat", "-d", "-b", "crash").count("FATAL")
    shell("cmd", "locale", "set-app-locales", PKG, "--locales", "")
    print(f"\ncrashes: {crashes} · screenshots: {OUT}")
    if failed or crashes:
        print(f"BATTERY FAILED: {failed}")
        sys.exit(1)
    print("BATTERY PASSED")


if __name__ == "__main__":
    main()
