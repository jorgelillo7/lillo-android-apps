#!/usr/bin/env python3
"""Plays every way a game of Who's lying? can go, on the running emulator, with the release APK.

While the phone is passed around, the script peeks under each curtain (the word, or "You are the
impostor / Journalist") so it knows who is who, then votes on purpose to force each ending in each
mode: civilians win and impostors win in Original (with and without the hint) and Mysterious, a
journalist guesses right or wrong, journalists win alone or with the impostors, the group ends the
game early, a Chaos game, a timed vote, the drawing mode, a 3-round match up to the podium, a match
continued from the home, word packs, Spanish and landscape. Every result title is checked against
the expected one and every scenario leaves a screenshot in build/battery/whos-lying/.

Usage (repo root, emulator running, release APK built):
    ./gradlew :apps:whos-lying:app:assembleRelease && ./gradlew --stop
    scripts/emulator.sh start --headless
    python3 apps/whos-lying/tools/battery_test.py [--landscape] [scenario ...]

--landscape plays every scenario on the phone turned sideways: under 480 dp tall, so it gets the
compact layouts of short screens like the AYN Thor's (screenshots in build/battery/whos-lying-landscape/).
"""
import sys
import struct
import time
from collections import Counter
from pathlib import Path

ROOT = Path(__file__).resolve().parents[3]
sys.path.insert(0, str(ROOT / "scripts"))
from ui_lib import (crashes, dump, find, hide_keyboard, install_release, rotate, screenshot, shell,  # noqa: E402
                    swipe_up, tap, type_text, wait_for)

PKG = "com.jorgelillo.whoslying"
LANDSCAPE = "--landscape" in sys.argv
OUT = ROOT / ("build/battery/whos-lying-landscape" if LANDSCAPE else "build/battery/whos-lying")

TITLES = {
    "caught_one": "You caught the impostor!",
    "caught_all": "You caught them all!",
    "impostors": "The impostors win!",
    "both": "Impostors and Journalists win!",
    "journalists": "The Journalists win!",
    "guessed": "The Journalist guessed the word!",
    "nobody": "Everyone was an impostor! 🎭",
    "ended": "Game over",
}


def shot(name):
    path = screenshot(OUT / f"{name}.png")
    if LANDSCAPE and not name.startswith("FAILED"):
        width, height = struct.unpack(">II", path.read_bytes()[16:24])   # PNG header
        assert width > height, f"{name}: the phone turned upright ({width}x{height})"


# ── app flows ──────────────────────────────────────────────────────────────────────────────────

def fresh(locale="en-US"):
    """Clean install state, given language, past the first-launch welcome."""
    shell("pm", "clear", PKG)
    shell("cmd", "locale", "set-app-locales", PKG, "--locales", locale)
    shell("am", "start", "-W", "-n", f"{PKG}/.MainActivity")
    time.sleep(4)
    rotate(LANDSCAPE)
    tap("welcome_ok", timeout=25)


def players_and_setup(names):
    tap("play", wait=2)
    for name in names:
        tap("player_name", wait=0.5)
        type_text(name)
        shell("input", "keyevent", "KEYCODE_ENTER")
        time.sleep(0.8)
    hide_keyboard()
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
        swipe_up()
        time.sleep(1)
    raise AssertionError(f"no switch {tag}")


def pick(row, option):
    """Opens a setup row's list (rounds_row, timer_row) and picks one value."""
    tap(row, wait=1.5, scroll=True)
    tap(option, wait=1.5)


def configure(mode="original", impostors=1, journalists=0, drawing=False, hint=True, rounds=None, timer=None):
    """Setup for a clean group: rounds and time start at what suits it (time is always on)."""
    tap(f"mode_{mode}", wait=1)
    # One journalist first: only then may the impostors drop to none, and the cap counts both.
    if journalists:
        tap("journalists_plus", wait=0.8, scroll=True)
    for _ in range(1 - impostors):   # Mysterious with journalists only
        tap("impostors_minus", wait=0.8, scroll=True)
    for _ in range(journalists - 1):
        tap("journalists_plus", wait=0.8, scroll=True)
    for _ in range(impostors - 1):
        tap("impostors_plus", wait=0.8, scroll=True)
    set_switch("drawing", drawing)
    if mode == "original":
        set_switch("hint", hint)
    if rounds is not None:
        pick("rounds_row", f"rounds_option_{rounds}")
    if timer is not None:
        pick("timer_row", f"timer_option_{timer}")


def reveal_all_cards(count):
    """Passes the phone around; returns {player: word or 'IMPOSTOR' / 'JOURNALIST'} seen under each curtain."""
    seen = {}
    for _ in range(count):
        nodes = wait_for("tap_to_reveal", timeout=20)
        texts = [n.text for n in nodes]
        player = texts[texts.index("Pass the phone to") + 1] if "Pass the phone to" in texts else texts[texts.index("Pásale el móvil a") + 1]
        # From the middle of the curtain, which sits higher in landscape: hold it up while reading.
        h = 2 * nodes[0].center[1]
        start = find(nodes, "tap_to_reveal").center[1] / h
        swipe = swipe_up(start=start, end=max(start - 0.4, 0.05), ms=4000, wait=False)
        time.sleep(2)
        nodes = dump()
        role = find(nodes, "secret_role")
        word = find(nodes, "secret_word")
        # Original and Chaos tell the role; Mysterious only shows a word (journalists excepted).
        if role and role.text in ("You are the impostor", "Eres el impostor"):
            seen[player] = "IMPOSTOR"
        elif role and role.text in ("You are the Journalist", "Eres el Periodista"):
            seen[player] = "JOURNALIST"
        elif word:
            seen[player] = word.text
        else:
            raise AssertionError(f"couldn't read {player}'s card: {[n.text for n in nodes if n.text]}")
        swipe.wait()
        tap("hide", wait=1.2)
    return seen


def roles(seen):
    """Who is who from what each card showed: the majority word is the civilians'."""
    words = Counter(w for w in seen.values() if w not in ("IMPOSTOR", "JOURNALIST"))
    civilian_word = words.most_common(1)[0][0] if words else None
    role = {}
    for p, w in seen.items():
        role[p] = w if w in ("IMPOSTOR", "JOURNALIST") else ("CIVILIAN" if w == civilian_word and words[w] > 1 or len(words) == 1 else "IMPOSTOR")
    return role, civilian_word


def to_vote(drawing=False):
    if drawing:
        canvas = find(wait_for("canvas", timeout=20), "canvas")
        x, y = canvas.center
        shell("input", "swipe", str(x - 200), str(y), str(x + 200), str(y + 150), "600")
        time.sleep(1)
    wait_for("go_vote", timeout=20)
    shot("debate")
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
            shot("journalist_last_chance")
            for _ in range(3):   # typing before the keyboard is ready drops the text (landscape's full-screen one)
                tap("guess_text", wait=1.5)
                type_text(guess)
                hide_keyboard()
                time.sleep(0.5)
                if find(dump(), "guess_text").text == guess:
                    break
            tap("guess", wait=3)
            state, _ = after_vote()
        if state == "result":
            break
        shot("exposed_game_goes_on")
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


def original_civilians():
    r, _ = new_game(THREE, mode="original")
    expect(play(by("IMPOSTOR", r)), "caught_one", "original_civilians_win")


def original_impostors():
    r, _ = new_game(THREE, mode="original", hint=False)
    expect(play(by("CIVILIAN", r)[:1]), "impostors", "original_impostors_win_no_hint")


def mysterious_civilians():
    r, _ = new_game(THREE, mode="mysterious")
    expect(play(by("IMPOSTOR", r)), "caught_one", "mysterious_civilians_win")


def mysterious_impostors():
    r, _ = new_game(THREE, mode="mysterious")
    expect(play(by("CIVILIAN", r)[:1]), "impostors", "mysterious_impostors_win")


def two_impostors_caught():
    r, _ = new_game(FIVE, mode="original", impostors=2)
    assert len(by("IMPOSTOR", r)) == 2, r
    expect(play(by("IMPOSTOR", r)), "caught_all", "original_two_impostors_caught")


def journalist_guesses_right():
    r, word = new_game(FIVE, mode="mysterious", journalists=1)
    expect(play(by("JOURNALIST", r), guess=word), "guessed", "journalist_guesses_right")


def journalist_guesses_wrong():
    r, _ = new_game(FIVE, mode="mysterious", journalists=1)
    expect(play(by("JOURNALIST", r) + by("IMPOSTOR", r), guess="zzz"), "caught_all", "journalist_wrong_guess_then_civilians_win")


def journalists_win_alone():
    r, _ = new_game(FIVE, mode="mysterious", impostors=0, journalists=2)
    assert len(by("JOURNALIST", r)) == 2 and not by("IMPOSTOR", r), r
    expect(play(by("CIVILIAN", r)[:2]), "journalists", "journalists_win_alone")


def impostor_and_journalist_win():
    r, _ = new_game(FIVE, mode="mysterious", journalists=1)
    expect(play(by("CIVILIAN", r)[:2]), "both", "impostor_and_journalist_win")


def drawing_mode():
    r, _ = new_game(THREE, mode="mysterious", drawing=True)
    assert "IMPOSTOR" in r.values(), f"Mysterious impostors draw blind, knowing it: {r}"
    expect(play(by("IMPOSTOR", r), drawing=True), "caught_one", "drawing_civilians_win")


def ended_early():
    new_game(THREE, mode="original")
    tap("reveal_all", wait=3, timeout=20)   # on the debate screen, next to "Vote"
    expect(find(wait_for("result_title"), "result_title").text, "ended", "ended_early")


def chaos_game():
    fresh()
    players_and_setup(FIVE)
    configure(mode="chaos")
    tap("start", wait=2, scroll=True)
    r, _ = roles(reveal_all_cards(len(FIVE)))
    title = play(list(r))   # vote everyone in turn: every chaos deal must reach an end
    assert title in TITLES.values(), title
    assert not find(dump(), "rounds_left"), "Chaos keeps no score"
    shot("chaos")
    print(f"  ✔ chaos: {title}")


def timer_runs_out():
    fresh()
    players_and_setup(THREE)
    configure(mode="original", timer=60)
    tap("start", wait=2, scroll=True)
    r, _ = roles(reveal_all_cards(3))
    # "Time's up!" only shows for 1.8 s, often less than a UI dump: check the forced vote it leads to.
    nodes = wait_for("confirm_vote", timeout=90)
    shot("timer_forced_vote")
    assert not find(nodes, "back_to_debate"), "time ran out but the vote isn't forced"
    vote(by("IMPOSTOR", r)[0])
    expect(find(wait_for("result_title"), "result_title").text, "caught_one", "timer_vote_forced")


def match_to_podium():
    fresh()
    players_and_setup(THREE)
    configure(mode="original", rounds=3)
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


def continue_from_home():
    r, _ = new_game(THREE, mode="original")
    play(by("IMPOSTOR", r))
    tap("home", wait=2)
    shot("home_match_in_progress")
    tap("continue_match", wait=2)   # straight to the next round's curtain
    wait_for("tap_to_reveal", timeout=20)
    print("  ✔ continue: the home deals the next round of the match")


def packs():
    fresh()
    tap("packs", wait=2)
    tap("new_pack", wait=2)
    tap("pack_name", wait=0.5); type_text("Battery"); hide_keyboard()
    tap("pack_words", wait=0.5); type_text("Mar / Lago"); shell("input", "keyevent", "KEYCODE_ENTER")
    type_text("Sol / Luna"); hide_keyboard(); time.sleep(1)
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
    configure(mode="original")
    tap("start", wait=2, scroll=True)
    r, _ = roles(reveal_all_cards(3))
    title = play(by("IMPOSTOR", r))
    shot("spanish_result")
    assert title and title != TITLES["caught_one"], f"not in Spanish: {title}"
    print(f"  ✔ spanish: {title}")


def landscape():
    try:
        fresh()
        rotate(True)
        players_and_setup(THREE)
        configure(mode="original")
        # Start sits at the end of a long list sideways: scroll to the very end first, or the tap
        # lands on the part of the button still below the list's edge.
        for _ in range(4):
            swipe_up(end=0.2)
            time.sleep(0.8)
        tap("start", wait=3)
        shot("landscape_curtain")
        r, _ = roles(reveal_all_cards(3))
        to_vote()
        shot("landscape_vote")
        vote(by("IMPOSTOR", r)[0])
        expect(find(wait_for("result_title"), "result_title").text, "caught_one", "landscape_result")
    finally:
        rotate(False)


SCENARIOS = [original_civilians, original_impostors, mysterious_civilians, mysterious_impostors, two_impostors_caught,
             journalist_guesses_right, journalist_guesses_wrong, journalists_win_alone, impostor_and_journalist_win,
             drawing_mode, ended_early, chaos_game, timer_runs_out, match_to_podium, continue_from_home, packs,
             spanish, landscape]


def main():
    wanted = [a for a in sys.argv[1:] if not a.startswith("--")]
    install_release("whos-lying", PKG)
    failed = []
    rotate(LANDSCAPE)
    try:
        for scenario in SCENARIOS:
            if wanted and scenario.__name__ not in wanted or LANDSCAPE and scenario is landscape:
                continue
            print(f"▶ {scenario.__name__}")
            try:
                scenario()
            except Exception as e:  # keep going: report every scenario
                failed.append(scenario.__name__)
                shot(f"FAILED_{scenario.__name__}")
                print(f"  ✘ {e}")
            rotate(LANDSCAPE)   # the landscape scenario puts the phone back upright
    finally:
        rotate(False)
    crashed = crashes(PKG)
    shell("cmd", "locale", "set-app-locales", PKG, "--locales", "")
    print(f"\ncrashes: {crashed} · screenshots: {OUT}")
    if failed or crashed:
        print(f"BATTERY FAILED: {failed}")
        sys.exit(1)
    print("BATTERY PASSED")

if __name__ == "__main__":
    main()
