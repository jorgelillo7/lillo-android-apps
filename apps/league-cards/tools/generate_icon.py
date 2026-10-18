"""Generates every launcher/store icon asset of the league card game from one geometry definition.

A classic black-and-white football crying a big blue tear, on the group-chat night green.

Outputs: see scripts/icon_lib.py (adaptive, monochrome and legacy icons, Play Store icon).

Usage (repo root): .venv/bin/python apps/league-cards/tools/generate_icon.py
"""
import math
import sys
from pathlib import Path

from PIL import ImageDraw

ROOT = Path(__file__).resolve().parent.parent
sys.path.insert(0, str(ROOT.parent.parent / "scripts"))
from icon_lib import fill, poly_path, write_icon  # noqa: E402

BG_TOP, BG_BOTTOM = "#1F3B33", "#0B141A"
BALL, PATCH, TEAR = "#F4F1EA", "#0B141A", "#53BDEB"

# Geometry in the 108x108 adaptive viewport (safe zone: radius 33 around the centre).
CX, CY, R = 52.0, 52.0, 25.0


def circle(cx, cy, r, n=72):
    return [(cx + r * math.cos(2 * math.pi * i / n), cy + r * math.sin(2 * math.pi * i / n)) for i in range(n)]


def pentagon(cx, cy, r, turn=-90):
    return [(cx + r * math.cos(math.radians(turn + 72 * i)), cy + r * math.sin(math.radians(turn + 72 * i))) for i in range(5)]


def tear():
    """A drop hanging from under the right eye: round bottom, pointed top."""
    cx, cy, r = 72.0, 72.0, 8.0
    bottom = [(cx + r * math.cos(math.radians(a)), cy + r * math.sin(math.radians(a))) for a in range(-20, 201, 10)]
    return bottom + [(cx - 1.0, cy - 18.0)]


def clip(poly, window):
    """Sutherland-Hodgman: [poly] cut to the convex [window] (counter-clockwise)."""
    def inside(p, a, b):
        return (b[0] - a[0]) * (p[1] - a[1]) - (b[1] - a[1]) * (p[0] - a[0]) >= 0

    def cross(p, q, a, b):
        x1, y1, x2, y2 = p[0], p[1], q[0], q[1]
        x3, y3, x4, y4 = a[0], a[1], b[0], b[1]
        den = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4)
        t = ((x1 - x3) * (y3 - y4) - (y1 - y3) * (x3 - x4)) / den
        return x1 + t * (x2 - x1), y1 + t * (y2 - y1)

    out = poly
    for a, b in zip(window, window[1:] + window[:1]):
        src, out = out, []
        for p, q in zip(src, src[1:] + src[:1]):
            if inside(q, a, b):
                if not inside(p, a, b):
                    out.append(cross(p, q, a, b))
                out.append(q)
            elif inside(p, a, b):
                out.append(cross(p, q, a, b))
        if not out:
            return []
    return out


def seam(p, q, w=1.5):
    """A straight seam from p to q as a thin quad."""
    dx, dy = q[0] - p[0], q[1] - p[1]
    n = math.hypot(dx, dy)
    ox, oy = -dy / n * w / 2, dx / n * w / 2
    # Run a little into the pentagons at both ends so the seams join them without gaps.
    ex, ey = dx / n * 1.2, dy / n * 1.2
    p, q = (p[0] - ex, p[1] - ey), (q[0] + ex, q[1] + ey)
    return [(p[0] + ox, p[1] + oy), (q[0] + ox, q[1] + oy), (q[0] - ox, q[1] - oy), (p[0] - ox, p[1] - oy)]


def ball():
    """The classic black-and-white ball seen from above a pentagon: the centre pentagon, five more
    towards the rim (cut by it), and the seams that draw the white hexagons between them."""
    centre = pentagon(CX, CY, R * 0.33)
    angles = [-90 + 72 * i for i in range(5)]
    outer = [pentagon(CX + R * 0.88 * math.cos(math.radians(a)), CY + R * 0.88 * math.sin(math.radians(a)), R * 0.33, a + 180)
             for a in angles]
    seams = []
    for i in range(5):
        seams.append(seam(centre[i], outer[i][0]))             # centre vertex → the pentagon facing it
        seams.append(seam(outer[i][4], outer[(i + 1) % 5][1]))  # between neighbouring outer pentagons
    rim = circle(CX, CY, R)
    return [centre] + [clip(p, rim) for p in outer], [clip(s, rim) for s in seams]


def shapes():
    """(polygon, colour) in drawing order, in viewport coordinates."""
    patches, seams = ball()
    return ([(circle(CX, CY, R), BALL)] + [(s, PATCH) for s in seams if s] + [(p, PATCH) for p in patches if p] +
            [(tear(), TEAR)])


def foreground():
    return [fill(poly_path(points), colour) for points, colour in shapes()]


def monochrome():
    # The patches cut out of the disc would need even-odd paths; disc + tear reads fine themed.
    return [fill(poly_path(circle(CX, CY, R)), "#FF000000"), fill(poly_path(tear()), "#FF000000")]


def draw(img, P, k):
    """The foreground with Pillow, matching the vector layers."""
    d = ImageDraw.Draw(img)
    for points, colour in shapes():
        d.polygon([P(*p) for p in points], fill=colour)


if __name__ == "__main__":
    write_icon(ROOT, BG_TOP, BG_BOTTOM, foreground(), monochrome(), draw)
