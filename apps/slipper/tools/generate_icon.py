"""Generates every launcher/store icon asset of ¿Te pilla la chancla? from the game's own sprites.

The red slipper flying over the kitchen's tile blue, with speed lines and a croqueta: pixel art from
app/src/main/assets/sprites.txt (the same drawing the game uses). Outputs: see scripts/icon_lib.py.

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
WHITE = "#F4EFE6"

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
    """(polygon, colour) in drawing order: speed lines, the slipper, a croqueta."""
    lines = []
    for i, (x0, x1, y) in enumerate([(22, 30, 42), (20, 30, 48), (23, 30, 54)]):
        lines.append(([(x0, y), (x1, y), (x1, y + 2.6), (x0, y + 2.6)], WHITE))
    slipper = pixel_rects("slipper", left=31, top=37, size=4.2)          # 12x5 px -> 50x21
    croqueta = pixel_rects("croqueta", left=57, top=63, size=2.6)        # 8x5 px
    return lines + slipper + croqueta


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
