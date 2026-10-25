"""Generates the game's short sound effects into app/src/main/res/raw/ (committed, like the images).

Plain Python (wave + math), no samples from anywhere: chiptune-style blips that fit the pixel art.
Run from the repo root:  .venv/bin/python apps/slipper/tools/generate_sounds.py
"""

import math
import os
import random
import struct
import wave

RATE = 22050
OUT = os.path.join(os.path.dirname(__file__), "..", "app", "src", "main", "res", "raw")


def square(freq, t):
    return 1.0 if math.sin(2 * math.pi * freq * t) >= 0 else -1.0


def tone(freqs, length, shape=square, volume=0.35, decay=4.0):
    """A sweep through `freqs` (start, end) over `length` seconds, fading out."""
    start, end = freqs
    samples = []
    phase = 0.0
    n = int(RATE * length)
    for i in range(n):
        t = i / RATE
        f = start + (end - start) * i / n
        phase += f / RATE
        env = math.exp(-decay * t / length) * min(1.0, i / 60)
        samples.append(shape(1, phase) * env * volume)
    return samples


def noise(length, volume=0.4, decay=6.0, seed=1):
    rng = random.Random(seed)
    n = int(RATE * length)
    return [rng.uniform(-1, 1) * math.exp(-decay * i / n) * volume for i in range(n)]


def notes(seq, each, volume=0.3):
    out = []
    for f in seq:
        out += tone((f, f), each, volume=volume, decay=2.5)
    return out


def mix(*tracks):
    n = max(len(t) for t in tracks)
    return [max(-1.0, min(1.0, sum(t[i] for t in tracks if i < len(t)))) for i in range(n)]


def write(name, samples):
    path = os.path.join(OUT, name + ".wav")
    with wave.open(path, "wb") as w:
        w.setnchannels(1)
        w.setsampwidth(2)
        w.setframerate(RATE)
        w.writeframes(b"".join(struct.pack("<h", int(s * 32767)) for s in samples))
    print("wrote", os.path.relpath(path))


def main():
    os.makedirs(OUT, exist_ok=True)
    write("sfx_jump", tone((330, 660), 0.12, volume=0.25))
    write("sfx_croqueta", notes([988, 1319], 0.05, volume=0.25))
    write("sfx_near_miss", mix(noise(0.18, volume=0.25, decay=3), tone((1200, 500), 0.18, volume=0.12)))
    write("sfx_hit", mix(noise(0.16, volume=0.5, decay=8, seed=2), tone((180, 80), 0.16, volume=0.35)))
    write("sfx_blocked", mix(noise(0.1, volume=0.3, decay=10, seed=3), notes([523, 784], 0.06, volume=0.2)))
    write("sfx_power", notes([523, 659, 784, 1047], 0.06, volume=0.25))
    write("sfx_crash", mix(noise(0.3, volume=0.45, decay=5, seed=4), tone((220, 90), 0.3, volume=0.25)))
    write("sfx_caught", mix(noise(0.08, volume=0.6, decay=12, seed=5), notes([392, 330, 262, 196], 0.12, volume=0.3)))


if __name__ == "__main__":
    main()
