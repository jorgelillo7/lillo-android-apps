"""Small synthesiser for game sound effects and simple loops, shared by every engine game.

Plain Python (math, wave, random): no samples from anywhere, so every sound is ours. A game's
tools/generate_sounds.py describes its sounds with these pieces and writes them as 16-bit mono WAV.
"""
import math
import random
import struct
import wave

RATE = 44100


def note(name: str) -> float:
    """Frequency of a note like 'C5' or 'F#4' (A4 = 440 Hz)."""
    names = {"C": -9, "C#": -8, "D": -7, "D#": -6, "E": -5, "F": -4, "F#": -3, "G": -2, "G#": -1,
             "A": 0, "A#": 1, "B": 2}
    pitch, octave = name[:-1], int(name[-1])
    return 440.0 * 2 ** ((names[pitch] + 12 * (octave - 4)) / 12)


def wave_at(shape: str, phase: float) -> float:
    """One sample of a waveform at `phase` (cycles)."""
    p = phase % 1.0
    if shape == "sine":
        return math.sin(2 * math.pi * p)
    if shape == "square":
        return 1.0 if p < 0.5 else -1.0
    if shape == "triangle":
        return 4 * abs(p - 0.5) - 1
    if shape == "saw":
        return 2 * p - 1
    raise ValueError(shape)


def tone(start: float, end: float, length: float, shape="sine", volume=0.5, attack=0.005,
         decay=6.0, vibrato=0.0) -> list:
    """A pitch sweep from `start` to `end` Hz over `length` s with a quick attack and an
    exponential fade (`decay` per second)."""
    out, phase = [], 0.0
    count = int(length * RATE)
    for i in range(count):
        t = i / RATE
        freq = start + (end - start) * (i / max(1, count - 1))
        freq *= 1 + vibrato * math.sin(2 * math.pi * 6 * t)
        phase += freq / RATE
        envelope = min(1.0, t / attack) * math.exp(-decay * t)
        out.append(volume * envelope * wave_at(shape, phase))
    return out


def noise(length: float, volume=0.4, decay=30.0, smooth=0.0, seed=1) -> list:
    """A burst of noise; `smooth` (0..1) low-passes it for thuds and crunches."""
    rng = random.Random(seed)
    out, last = [], 0.0
    for i in range(int(length * RATE)):
        t = i / RATE
        value = rng.uniform(-1, 1)
        last = last * smooth + value * (1 - smooth)
        out.append(volume * math.exp(-decay * t) * last)
    return out


def mix(*parts) -> list:
    """Parts played together, padded to the longest."""
    length = max(len(p) for p in parts)
    return [sum(p[i] for p in parts if i < len(p)) for i in range(length)]


def sequence(*parts, gap=0.0) -> list:
    """Parts one after another, `gap` seconds of silence between them."""
    out = []
    for part in parts:
        out.extend(part)
        out.extend([0.0] * int(gap * RATE))
    return out


def silence(length: float) -> list:
    return [0.0] * int(length * RATE)


def write(path, samples: list, peak=0.9) -> None:
    """Normalises to `peak` and writes a 16-bit mono WAV."""
    top = max(1e-9, max(abs(s) for s in samples))
    scale = peak / top if top > peak else 1.0
    with wave.open(str(path), "wb") as out:
        out.setnchannels(1)
        out.setsampwidth(2)
        out.setframerate(RATE)
        out.writeframes(b"".join(struct.pack("<h", int(max(-1, min(1, s * scale)) * 32767)) for s in samples))


def drum(kind: str, seed=7) -> list:
    """One drum hit: "kick", "snare" or "hat"."""
    if kind == "kick":
        return mix(tone(150, 45, 0.25, "sine", 0.9, decay=14), noise(0.02, 0.2, decay=150, seed=seed))
    if kind == "snare":
        return mix(noise(0.18, 0.5, decay=22, smooth=0.15, seed=seed), tone(220, 180, 0.08, "triangle", 0.25, decay=30))
    if kind == "hat":
        return noise(0.04, 0.18, decay=90, smooth=0.0, seed=seed)
    raise ValueError(kind)


def place(track: list, part: list, at: float, gain=1.0) -> None:
    """Adds `part` into `track` starting at `at` seconds, wrapping past the end so loops stay seamless."""
    start = int(at * RATE)
    for i, sample in enumerate(part):
        track[(start + i) % len(track)] += sample * gain


def song(bpm: int, bars: int, chords: list, melody: list, beat: list, lead="square", bass="triangle") -> list:
    """A seamless loop: `chords` one per bar (note names, root first) give the bass and a soft pad,
    `melody` is (note or "-", length in eighths) pairs, `beat` is a bar of 16 steps written with
    k (kick), s (snare), h (hat), r (snare roll) or "." for rest."""
    eighth = 60 / bpm / 2
    bar = eighth * 8
    out = silence(bar * bars)
    for b in range(bars):
        names = chords[b % len(chords)]
        root = names[0]
        for step in range(4):
            place(out, tone(note(root), note(root), eighth * 1.8, bass, 0.32, decay=3), b * bar + step * eighth * 2)
        for n in names[1:]:
            place(out, tone(note(n), note(n), bar * 0.95, "sine", 0.05, attack=0.05, decay=0.8), b * bar)
        for step, hit in enumerate(beat):
            at = b * bar + step * eighth / 2
            if hit == "k":
                place(out, drum("kick"), at)
            elif hit == "s":
                place(out, drum("snare", seed=b + step), at, 0.8)
            elif hit == "r":
                place(out, drum("snare", seed=b + step), at, 0.45)
            elif hit == "h":
                place(out, drum("hat", seed=b * 16 + step), at)
    at = 0.0
    total = bar * bars
    while at < total - 1e-6:
        for name, eighths in melody:
            if at >= total - 1e-6:
                break
            length = eighths * eighth
            if name != "-":
                place(out, tone(note(name), note(name), length * 0.92, lead, 0.13, attack=0.01, decay=2.5, vibrato=0.004), at)
            at += length
    return out
