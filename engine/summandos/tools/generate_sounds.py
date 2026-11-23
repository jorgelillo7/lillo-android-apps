"""Generates Summandos' sound effects and its placeholder music into assets/sounds/ (committed).

Every sound is synthesised with engine/core/tools/lillo_sfx.py: nothing is sampled from anywhere.
Usage (repo root): .venv/bin/python engine/summandos/tools/generate_sounds.py
"""
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
sys.path.insert(0, str(ROOT.parent / "core" / "tools"))
from lillo_sfx import mix, note, noise, sequence, silence, tone, write  # noqa: E402

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


def loop(bpm: int, bass: list, lead: list, bars: int) -> list:
    """A seamless chiptune loop: one bass note per beat, one lead note per half beat."""
    beat = 60 / bpm
    out = silence(beat * 4 * bars)
    for i, n in enumerate(bass * (bars * 4 // len(bass))):
        start = int(i * beat * 44100)
        part = tone(note(n), note(n), beat * 0.9, "triangle", 0.35, decay=3)
        for j, s in enumerate(part):
            if start + j < len(out):
                out[start + j] += s
    for i, n in enumerate(lead * (bars * 8 // len(lead))):
        if n == "-":
            continue
        start = int(i * beat / 2 * 44100)
        part = tone(note(n), note(n), beat * 0.45, "square", 0.12, decay=6)
        for j, s in enumerate(part):
            if start + j < len(out):
                out[start + j] += s
    return out


MUSIC = {
    "music_menu": lambda: loop(100, ["C3", "G2", "A2", "F2"],
                               ["E5", "G5", "C6", "G5", "A5", "-", "G5", "E5",
                                "D5", "E5", "G5", "-", "C5", "D5", "E5", "-"], 8),
    "music_run": lambda: loop(132, ["A2", "A2", "F2", "G2"],
                              ["A4", "C5", "E5", "A5", "G5", "E5", "C5", "E5",
                               "F5", "A5", "C6", "A5", "G5", "B4", "D5", "G5"], 8),
}


if __name__ == "__main__":
    OUT.mkdir(parents=True, exist_ok=True)
    for name, make in {**SOUNDS, **MUSIC}.items():
        write(OUT / f"{name}.wav", make())
        print(name)
