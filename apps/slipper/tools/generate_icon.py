"""Generates every launcher/store icon asset of ¿Te pilla la chancla? from the game's own sprites.

The grandma, shouting, her red slipper raised, over the kitchen's tile blue: the `icon_grandma`
pixel art in app/src/main/assets/sprites.txt. Outputs: see scripts/icon_lib.py.

Usage (repo root): .venv/bin/python apps/slipper/tools/generate_icon.py
"""
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
sys.path.insert(0, str(ROOT.parent.parent / "scripts"))
sys.path.insert(0, str(Path(__file__).resolve().parent))
from icon_lib import fill, poly_path, write_icon  # noqa: E402
from sprites import load  # noqa: E402

BG_TOP, BG_BOTTOM = "#4F94D0", "#2E6AA6"   # the kitchen's tile blue

SPRITES = load(ROOT / "app/src/main/assets/sprites.txt")


def pixel_rects(name, left, top, size):
    """A sprite as rectangles (one per run of same-colour pixels in a row), in viewport units."""
    sprite = SPRITES[name]
    out = []
    for y, row in enumerate(sprite):
        x = 0
        while x < len(row):
            colour = row[x]
            end = x
            while end < len(row) and row[end] == colour:
                end += 1
            if colour:
                x0, y0, x1, y1 = left + x * size, top + y * size, left + end * size, top + (y + 1) * size
                out.append(([(x0, y0), (x1, y0), (x1, y1), (x0, y1)], colour))
            x = end
    return out


def shapes():
    """(polygon, colour) in drawing order: the grandma, sized so the round masks only trim the slipper's tip."""
    return pixel_rects("icon_grandma", left=26, top=28, size=1.85)


def foreground():
    return [fill(poly_path(points), colour) for points, colour in shapes()]


def monochrome():
    return [fill(poly_path(points), "#FF000000") for points, _ in shapes()]


def draw(img, P, k):
    from PIL import ImageDraw
    d = ImageDraw.Draw(img)
    for points, colour in shapes():
        d.polygon([P(*p) for p in points], fill=colour)


if __name__ == "__main__":
    write_icon(ROOT, BG_TOP, BG_BOTTOM, foreground(), monochrome(), draw)
