"""Generates Summandos' icons: a white plus and a gold times over the squad's blue.

Placeholder until the game's design pass; drawn with the apps' shared scripts/icon_lib.py so the
final one follows the same pipeline. Outputs (engine/summandos/):
  icons/main_192.png                       legacy launcher icon (Godot's main_192x192)
  icons/adaptive_{foreground,background,monochrome}_432.png   adaptive icon layers
  store/play-icon-512.png                  Play Store icon

Usage (repo root): .venv/bin/python engine/summandos/tools/generate_icon.py
"""
import math
import sys
from pathlib import Path

from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parent.parent
sys.path.insert(0, str(ROOT.parent.parent / "scripts"))
from icon_lib import LEGACY_CROP, STORE_CROP, canvas  # noqa: E402

BG_TOP, BG_BOTTOM = "#3A7BFF", "#1B3FA8"   # the squad's blue
PLUS, TIMES = "#FFFFFF", "#FFC233"


def cross(cx, cy, half, thickness, angle=0.0):
    """A plus sign's 12 corners in the 108x108 viewport, rotated by `angle` radians."""
    t = thickness / 2
    points = [(-t, -half), (t, -half), (t, -t), (half, -t), (half, t), (t, t),
              (t, half), (-t, half), (-t, t), (-half, t), (-half, -t), (-t, -t)]
    c, s = math.cos(angle), math.sin(angle)
    return [(cx + x * c - y * s, cy + x * s + y * c) for x, y in points]


def shapes():
    """(polygon, colour) in drawing order, inside the adaptive safe zone (radius 33)."""
    return [
        (cross(46, 47, 18, 11), PLUS),
        (cross(69, 69, 10, 7, math.pi / 4), TIMES),
    ]


def paint(img, P, colour_of=lambda c: c):
    d = ImageDraw.Draw(img)
    for points, colour in shapes():
        d.polygon([P(*p) for p in points], fill=colour_of(colour))


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
    layer(432, (0, 108), lambda _: "#FFFFFF").save(icons / "adaptive_monochrome_432.png", optimize=True)
    full(512, STORE_CROP).save(ROOT / "store/play-icon-512.png", optimize=True)
    print("Icons generated.")
