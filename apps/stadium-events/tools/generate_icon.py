"""Generates every launcher/store icon asset of the stadium-events app from one geometry definition.

The stadium seen from above (cream stands, green pitch with its lines) with two floodlights and a
small amber bell between them: something on at the stadium, and the app tells you.

Outputs: see scripts/icon_lib.py (adaptive, monochrome and legacy icons, Play Store icon).

Usage (repo root): .venv/bin/python apps/stadium-events/tools/generate_icon.py
"""
import sys
from pathlib import Path

from PIL import ImageDraw

ROOT = Path(__file__).resolve().parent.parent
sys.path.insert(0, str(ROOT.parent.parent / "scripts"))
from icon_lib import f, fill, poly_path, write_icon  # noqa: E402

BG_TOP, BG_BOTTOM = "#24476B", "#13263A"
CREAM, STAND_SHADE = "#FBF7F1", "#D9CFC0"
GRASS, GRASS_DARK, LINE = "#4CAF72", "#3F9862", "#FFFFFF"
LAMP, AMBER, NAVY = "#FFE7A3", "#F2B544", "#13263A"

# Geometry in the 108x108 adaptive viewport (safe zone: radius 33 around the centre).
CX, CY, RX, RY = 54, 62, 26, 16           # the bowl
PW, PH = RX * 0.62, RY * 0.58             # half the pitch
MASTS = [(32, 34, 58), (76, 34, 58)]      # x, lamp top, foot (hidden behind the bowl)
BELL = (54, 38, 1.15)                     # centre x, y, scale


def ellipse_path(cx, cy, rx, ry):
    return f"M{f(cx - rx)},{f(cy)} a{f(rx)},{f(ry)} 0 1 0 {f(2 * rx)},0 a{f(rx)},{f(ry)} 0 1 0 {f(-2 * rx)},0 Z"


def rect(x0, y0, x1, y1):
    return [(x0, y0), (x1, y0), (x1, y1), (x0, y1)]


def bezier(p0, p1, p2, p3, n=16):
    return [tuple((1 - t) ** 3 * a + 3 * (1 - t) ** 2 * t * b + 3 * (1 - t) * t ** 2 * c + t ** 3 * e
                  for a, b, c, e in zip(p0, p1, p2, p3)) for t in (i / n for i in range(n + 1))]


def bell_body():
    cx, cy, s = BELL
    dome = bezier((cx - 4 * s, cy - 1 * s), (cx - 4 * s, cy - 7.5 * s), (cx + 4 * s, cy - 7.5 * s), (cx + 4 * s, cy - 1 * s))
    return [(cx - 5.2 * s, cy + 4 * s)] + dome + [(cx + 5.2 * s, cy + 4 * s)]


def pitch_lines():
    """The touchline box, halfway line and centre circle, as thin filled shapes."""
    w, x0, x1, y0, y1 = 0.8, CX - PW + 2, CX + PW - 2, CY - PH + 1.6, CY + PH - 1.6
    return [rect(x0, y0, x1, y0 + w), rect(x0, y1 - w, x1, y1), rect(x0, y0, x0 + w, y1), rect(x1 - w, y0, x1, y1),
            rect(CX - w / 2, y0, CX + w / 2, y1)]


def shapes():
    """(kind, geometry, colour) in drawing order: 'poly' points or 'ellipse' (cx, cy, rx, ry)."""
    out = []
    for x, top, foot in MASTS:
        out += [("poly", rect(x - 0.75, top + 3, x + 0.75, foot), CREAM),
                ("poly", rect(x - 4.2, top - 1, x + 4.2, top + 4), LAMP)]
        out += [("ellipse", (x + i, top + 1.5, 0.9, 0.9), LINE) for i in (-2.5, 0, 2.5)]
    out += [("ellipse", (CX, CY + 2.5, RX, RY), STAND_SHADE),
            ("ellipse", (CX, CY, RX, RY), CREAM),
            ("poly", rect(CX - PW, CY - PH, CX + PW, CY + PH), GRASS)]
    out += [("poly", rect(CX + i * PW / 3 - PW / 6, CY - PH, CX + i * PW / 3 + PW / 6, CY + PH), GRASS_DARK) for i in (-2, 0, 2)]
    out += [("poly", line, LINE) for line in pitch_lines()]
    out += [("ellipse", (CX, CY, 3.4, 2.4), LINE), ("ellipse", (CX, CY, 2.6, 1.6), GRASS_DARK)]
    cx, cy, s = BELL
    out += [("poly", bell_body(), AMBER),
            ("ellipse", (cx, cy + 5.1 * s, 1.6 * s, 1.5 * s), AMBER),
            ("ellipse", (cx, cy - 7.6 * s, 1 * s, 1 * s), AMBER)]
    return out


def path_of(kind, geometry):
    return poly_path(geometry) if kind == "poly" else ellipse_path(*geometry)


def foreground():
    return [fill(path_of(kind, g), colour) for kind, g, colour in shapes()]


def monochrome():
    """Silhouettes only: masts, bowl and bell (Android tints them)."""
    keep = {CREAM, STAND_SHADE, LAMP, AMBER}
    return [fill(path_of(kind, g), "#FF000000") for kind, g, colour in shapes() if colour in keep]


def draw(img, P, k):
    """The foreground with Pillow, matching the vector layers."""
    d = ImageDraw.Draw(img)
    for kind, g, colour in shapes():
        if kind == "poly":
            d.polygon([P(*p) for p in g], fill=colour)
        else:
            cx, cy, rx, ry = g
            d.ellipse([P(cx - rx, cy - ry), P(cx + rx, cy + ry)], fill=colour)


if __name__ == "__main__":
    write_icon(ROOT, BG_TOP, BG_BOTTOM, foreground(), monochrome(), draw)
