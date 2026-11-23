"""Generates Summandos' icons: a teal soldier leading two more of his squad beside a gold calculator,
on the sunset palette's sky.

Drawn with the apps' shared scripts/icon_lib.py. Outputs (engine/summandos/):
  icons/main_192.png                       legacy launcher icon (Godot's main_192x192)
  icons/adaptive_{foreground,background,monochrome}_432.png   adaptive icon layers
  store/play-icon-512.png                  Play Store icon

Usage (repo root): .venv/bin/python engine/summandos/tools/generate_icon.py
"""
import sys
from pathlib import Path

from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parent.parent
sys.path.insert(0, str(ROOT.parent.parent / "scripts"))
from icon_lib import LEGACY_CROP, STORE_CROP, canvas  # noqa: E402

# Colours from game/palette.gd ("sunset").
BG_TOP, BG_BOTTOM = "#FF8F6B", "#FFC58F"
SQUAD, SQUAD_DARK, BACK_ROW = "#13A89E", "#0B5E58", "#0E7F77"
SKIN, EYES, OUTLINE, GOLD = "#F2C9A0", "#14141F", "#3A1C0E", "#FFD23F"


def soldier(cx, base, k, body, helmet):
    """Shapes of one soldier facing the viewer, feet at `base`, scaled by `k`."""
    def box(x0, y0, x1, y1):
        return (cx + x0 * k, base + y0 * k, cx + x1 * k, base + y1 * k)
    return [
        ("round", box(-14, -30, 14, 0), 11 * k, body),
        ("ellipse", box(-10, -46, 10, -26), 0, SKIN),
        ("chord", box(-12, -50, 12, -28), 0, helmet),
        ("rect", box(-13, -40, 13, -37), 0, helmet),
        ("ellipse", box(-5.5, -35, -2.5, -32), 0, EYES),
        ("ellipse", box(2.5, -35, 5.5, -32), 0, EYES),
    ]


def shapes():
    """(kind, box, radius, colour) in drawing order, inside the adaptive safe zone (radius 33)."""
    calculator = [
        ("round", (37, 21, 71, 62), 5, GOLD),
        ("round", (40.5, 24.5, 67.5, 35.5), 2, OUTLINE),
        ("rect", (52.5, 26.5, 55.5, 33.5), 0, SQUAD),
        ("rect", (50.5, 28.5, 57.5, 31.5), 0, SQUAD),
    ] + [("round", (41 + i * 9, 38.5, 48 + i * 9, 44.5), 1.5, OUTLINE) for i in range(3)]
    return (calculator + soldier(37, 90, 0.66, BACK_ROW, SQUAD_DARK)
            + soldier(71, 90, 0.66, BACK_ROW, SQUAD_DARK) + soldier(54, 93, 0.9, SQUAD, SQUAD_DARK))


def paint(img, P, colour_of=lambda c: c):
    d = ImageDraw.Draw(img)
    scale = P(1, 0)[0] - P(0, 0)[0]
    for kind, (x0, y0, x1, y1), radius, colour in shapes():
        box = [*P(x0, y0), *P(x1, y1)]
        fill = colour_of(colour)
        if kind == "round":
            d.rounded_rectangle(box, radius=radius * scale, fill=fill)
        elif kind == "ellipse":
            d.ellipse(box, fill=fill)
        elif kind == "chord":
            d.chord(box, 180, 360, fill=fill)
        else:
            d.rectangle(box, fill=fill)


def monochrome(colour):
    """White shapes; faces and the calculator's keys are cut out so the soldiers still read in one colour."""
    if colour in (SKIN, OUTLINE):
        return (0, 0, 0, 0)
    return "#FFFFFF"


def layer(size, crop, colour_of=lambda c: c):
    """A transparent layer with the shapes, supersampled."""
    s = size * 4
    lo, hi = crop
    k = s / (hi - lo)
    img = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    paint(img, lambda x, y: ((x - lo) * k, (y - lo) * k), colour_of)
    return img.resize((size, size), Image.LANCZOS)


def full(size, crop):
    img, P, _ = canvas(size, crop, BG_TOP, BG_BOTTOM)
    paint(img, P)
    return img.resize((size, size), Image.LANCZOS)


if __name__ == "__main__":
    icons = ROOT / "icons"
    icons.mkdir(exist_ok=True)
    (ROOT / "store").mkdir(exist_ok=True)
    legacy = full(192, LEGACY_CROP).convert("RGBA")
    mask = Image.new("L", (768, 768), 0)
    ImageDraw.Draw(mask).rounded_rectangle([0, 0, 767, 767], radius=154, fill=255)
    legacy.putalpha(mask.resize((192, 192), Image.LANCZOS))
    legacy.save(icons / "main_192.png", optimize=True)
    layer(432, (0, 108)).save(icons / "adaptive_foreground_432.png", optimize=True)
    canvas(432, (0, 108), BG_TOP, BG_BOTTOM)[0].resize((432, 432), Image.LANCZOS).save(
        icons / "adaptive_background_432.png", optimize=True)
    layer(432, (0, 108), monochrome).save(icons / "adaptive_monochrome_432.png", optimize=True)
    full(512, STORE_CROP).save(ROOT / "store/play-icon-512.png", optimize=True)
    print("Icons generated.")
