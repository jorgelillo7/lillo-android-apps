"""Generates Summandos' sound effects and its placeholder music into assets/sounds/ (committed).

Every sound is synthesised with engine/core/tools/lillo_sfx.py: nothing is sampled from anywhere.
Usage (repo root): .venv/bin/python engine/summandos/tools/generate_sounds.py
"""
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
sys.path.insert(0, str(ROOT.parent / "core" / "tools"))
from lillo_sfx import mix, note, noise, sequence, silence, song, tone, write  # noqa: E402

OUT = ROOT / "assets" / "sounds"


def chord(names, length, shape="triangle", volume=0.25, decay=2.5):
    return mix(*[tone(note(n), note(n), length, shape, volume, decay=decay) for n in names])


SOUNDS = {
    "click": lambda: tone(900, 500, 0.07, "sine", 0.6, decay=40),
    "gate_up": lambda: sequence(*[tone(note(n), note(n), 0.07, "square", 0.25, decay=18)
                                  for n in ("C5", "E5", "G5", "C6")]),
    "gate_down": lambda: sequence(tone(note("E4"), note("C4"), 0.12, "square", 0.25, decay=10),
                                  tone(note("C4"), note("G3"), 0.16, "square", 0.25, decay=10)),
    "shot": lambda: mix(noise(0.05, 0.25, decay=90, smooth=0.3, seed=2),
                        tone(1400, 700, 0.04, "triangle", 0.15, decay=80)),
    "crunch": lambda: mix(noise(0.09, 0.45, decay=45, smooth=0.55, seed=3),
                          tone(300, 120, 0.08, "square", 0.12, decay=40)),
    "tick": lambda: mix(tone(1800, 1600, 0.03, "sine", 0.5, decay=120),
                        noise(0.02, 0.15, decay=200, seed=4)),
    "boss": lambda: mix(tone(90, 45, 0.9, "sine", 0.8, decay=3),
                        noise(0.6, 0.35, decay=5, smooth=0.92, seed=5)),
    "star": lambda: mix(tone(note("E6"), note("E6"), 0.5, "sine", 0.35, decay=7),
                        tone(note("B6"), note("B6"), 0.5, "sine", 0.15, decay=9)),
    "win": lambda: sequence(*[tone(note(n), note(n), 0.11, "square", 0.22, decay=10)
                              for n in ("G4", "C5", "E5", "G5")],
                            chord(("C5", "E5", "G5", "C6"), 0.7, "square", 0.12, decay=3)),
    "lose": lambda: sequence(tone(note("G4"), note("F#4"), 0.25, "triangle", 0.35, decay=3),
                             tone(note("F4"), note("E4"), 0.25, "triangle", 0.35, decay=3),
                             tone(note("D#4"), note("C4"), 0.6, "triangle", 0.35, decay=2, vibrato=0.01)),
}


MUSIC = {
    # The menus: a bouncy little march in C, light on drums.
    "music_menu": lambda: song(
        112, 8,
        [("C3", "E4", "G4"), ("A2", "C4", "E4"), ("F2", "A3", "C4"), ("G2", "B3", "D4")],
        [("E5", 2), ("G5", 1), ("E5", 1), ("C5", 2), ("D5", 2), ("E5", 2), ("C5", 2), ("A4", 4),
         ("F5", 2), ("E5", 1), ("D5", 1), ("C5", 2), ("A4", 2), ("B4", 2), ("D5", 2), ("G5", 4),
         ("E5", 2), ("G5", 1), ("A5", 1), ("G5", 2), ("E5", 2), ("C5", 2), ("E5", 2), ("A4", 4),
         ("F5", 2), ("A5", 2), ("G5", 2), ("F5", 2), ("E5", 2), ("D5", 2), ("C5", 4)],
        list("k...h...s...h..."), lead="triangle"),
    # The run: a driving march in A minor with a snare roll, a bugle-like lead.
    "music_run": lambda: song(
        144, 8,
        [("A2", "C4", "E4"), ("A2", "C4", "E4"), ("F2", "A3", "C4"), ("G2", "B3", "D4"),
         ("A2", "C4", "E4"), ("C3", "E4", "G4"), ("D3", "F4", "A4"), ("E2", "G#3", "B3")],
        [("A4", 2), ("A4", 1), ("A4", 1), ("C5", 2), ("E5", 2), ("D5", 2), ("C5", 2), ("B4", 2), ("A4", 2),
         ("F4", 2), ("A4", 2), ("C5", 4), ("D5", 2), ("B4", 2), ("G4", 4),
         ("A4", 2), ("C5", 1), ("E5", 1), ("A5", 4), ("G5", 2), ("E5", 2), ("C5", 2), ("E5", 2),
         ("F5", 2), ("E5", 2), ("D5", 2), ("C5", 2), ("B4", 4), ("G#4", 4)],
        list("k.h.s.h.k.kks.rr")),
}


if __name__ == "__main__":
    OUT.mkdir(parents=True, exist_ok=True)
    for name, make in {**SOUNDS, **MUSIC}.items():
        write(OUT / f"{name}.wav", make())
        print(name)
