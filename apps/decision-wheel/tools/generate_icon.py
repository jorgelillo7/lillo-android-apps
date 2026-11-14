"""Generates every launcher/store icon asset of the decision wheel from one geometry definition.

Outputs: see scripts/icon_lib.py (adaptive, monochrome and legacy icons, Play Store icon).

Usage (repo root): .venv/bin/python apps/decision-wheel/tools/generate_icon.py
"""
import math
import sys
from pathlib import Path

from PIL import ImageDraw

ROOT = Path(__file__).resolve().parent.parent
sys.path.insert(0, str(ROOT.parent.parent / "scripts"))
from icon_lib import circle_path, f, fill, poly_path, write_icon  # noqa: E402

NIGHT_TOP, NIGHT_BOTTOM = "#2A0F25", "#1E0A1B"  # the dark look's wine (DESIGN.md)
CREAM, NIGHT = "#FBEAF2", "#1E0A1B"
SEGMENTS = ["#FF6B6B", "#FFB347", "#FFD93D", "#4ECDC4", "#6C8EF5", "#C792EA"]

# Geometry in the 108x108 adaptive viewport (safe zone: radius 33 around the centre).
CX, CY, R = 54.0, 57.0, 27.0
GAP = 1.4                       # night-coloured gap between slices
HUB_OUT, HUB_IN = 6.5, 4.0
POINTER = [(54, 36.5), (48.5, 26.5), (59.5, 26.5)]   # tip touches the wheel at the top
ROTATION = -15.0                 # slight tilt: a wheel caught mid-spin


def slice_points(i, n=6, steps=24):
    a0 = math.radians(ROTATION - 90 + i * 360 / n)
    a1 = math.radians(ROTATION - 90 + (i + 1) * 360 / n)
    pts = [(CX, CY)]
    for s in range(steps + 1):
        a = a0 + (a1 - a0) * s / steps
        pts.append((CX + R * math.cos(a), CY + R * math.sin(a)))
    return pts


def stroke(d, color, width):
    return (f'    <path\n        android:strokeColor="{color}"\n        android:strokeWidth="{width}"\n'
            f'        android:strokeLineCap="round"\n        android:pathData="{d}" />')


def spokes():
    lines = []
    for i in range(6):
        a = math.radians(ROTATION - 90 + i * 60)
        lines.append(f"M{f(CX)},{f(CY)} L{f(CX + R * math.cos(a))},{f(CY + R * math.sin(a))}")
    return " ".join(lines)


def foreground():
    return ([fill(circle_path(CX, CY, R + 2.2), CREAM)]
            + [fill(poly_path(slice_points(i)), c) for i, c in enumerate(SEGMENTS)]
            + [stroke(spokes(), NIGHT, GAP),
               fill(circle_path(CX, CY, HUB_OUT), NIGHT),
               fill(circle_path(CX, CY, HUB_IN), CREAM),
               fill(poly_path(POINTER), CREAM),
               stroke(poly_path(POINTER), NIGHT, 1.2)])


def monochrome():
    return [
        stroke(circle_path(CX, CY, R), "#FF000000", 3),
        stroke(spokes(), "#FF000000", 2.4),
        fill(circle_path(CX, CY, HUB_OUT), "#FF000000"),
        fill(poly_path(POINTER), "#FF000000"),
    ]


def draw(img, P, k):
    """The foreground with Pillow, matching the vector layers."""
    d = ImageDraw.Draw(img)
    rr = (R + 2.2) * k
    cx, cy = P(CX, CY)
    d.ellipse([cx - rr, cy - rr, cx + rr, cy + rr], fill=CREAM)
    for i, c in enumerate(SEGMENTS):
        d.polygon([P(*p) for p in slice_points(i)], fill=c)
    for i in range(6):
        a = math.radians(ROTATION - 90 + i * 60)
        d.line([P(CX, CY), P(CX + R * math.cos(a), CY + R * math.sin(a))], fill=NIGHT, width=max(1, int(GAP * k)))
    for r, c in ((HUB_OUT, NIGHT), (HUB_IN, CREAM)):
        d.ellipse([cx - r * k, cy - r * k, cx + r * k, cy + r * k], fill=c)
    d.polygon([P(*p) for p in POINTER], fill=CREAM, outline=NIGHT, width=max(1, int(1.2 * k)))


if __name__ == "__main__":
    write_icon(ROOT, NIGHT_TOP, NIGHT_BOTTOM, foreground(), monochrome(), draw)
