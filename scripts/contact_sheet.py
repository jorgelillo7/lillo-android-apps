"""Builds an app's design contact sheet: its old screens beside the new light and dark ones.

For each screen named, the sheet shows the screenshot test image as it was at --old-ref, then the
light (`<screen>_light.png`) and dark (`<screen>.png`) images now committed in
apps/<app>/app/src/test/screenshots/, with the palette swatches read from the app's DESIGN.md
(the hex values under its `light:` and `dark:` keys) and the display face named.

Usage (repo root):
  .venv/bin/python scripts/contact_sheet.py decision-wheel home wheel --out /tmp/sheet.png
  .venv/bin/python scripts/contact_sheet.py tournaments home tournament --old-ref origin/master~3 --out …
"""
import argparse
import io
import re
import subprocess
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parent.parent
FONT = "/System/Library/Fonts/Avenir Next.ttc"
BOLD, REGULAR = 8, 7  # faces in Avenir Next.ttc: Heavy, Medium
DISPLAY_FACE = "Figtree (titles and numbers) · Roboto (body)"

SHOT_W = 330
GAP = 28
MARGIN = 48
SWATCH = 64
INK, PAPER, GREY = (30, 22, 30), (250, 248, 245), (110, 100, 110)


def font(size: int, face: int) -> ImageFont.FreeTypeFont:
    return ImageFont.truetype(FONT, size, index=face)


def shot_at(path: str, ref: str | None) -> Image.Image | None:
    """The image at `path`, from git `ref` or from the working tree; None when it is missing."""
    if ref is None:
        file = ROOT / path
        return Image.open(file).convert("RGB") if file.exists() else None
    done = subprocess.run(["git", "show", f"{ref}:{path}"], cwd=ROOT, capture_output=True)
    return Image.open(io.BytesIO(done.stdout)).convert("RGB") if done.returncode == 0 else None


def scaled(img: Image.Image) -> Image.Image:
    return img.resize((SHOT_W, round(img.height * SHOT_W / img.width)), Image.LANCZOS)


def swatches(design: str) -> dict[str, list[tuple[str, str]]]:
    """`{"light": [(name, "#hex"), …], "dark": […]}` from the DESIGN.md front matter."""
    found: dict[str, list[tuple[str, str]]] = {"light": [], "dark": []}
    block = None
    for line in design.split("\n---", 1)[0].splitlines():
        top = re.match(r"^(\w+):", line)
        if top:
            block = top.group(1) if top.group(1) in found else None
            continue
        entry = re.match(r'^\s+(\w+):\s*"(#[0-9A-Fa-f]{6})"', line)
        if block and entry:
            found[block].append((entry.group(1), entry.group(2)))
    return found


def draw_swatches(draw: ImageDraw.ImageDraw, x: int, y: int, label: str, colours: list[tuple[str, str]]) -> int:
    draw.text((x, y), label, font=font(26, BOLD), fill=INK)
    y += 40
    for i, (name, hexa) in enumerate(colours):
        cx = x + (i % 4) * 260
        cy = y + (i // 4) * (SWATCH + 44)
        draw.rounded_rectangle([cx, cy, cx + SWATCH, cy + SWATCH], radius=14, fill=hexa, outline=(0, 0, 0, 40))
        draw.text((cx + SWATCH + 10, cy + 6), name, font=font(20, BOLD), fill=INK)
        draw.text((cx + SWATCH + 10, cy + 32), hexa.upper(), font=font(18, REGULAR), fill=GREY)
    rows = (len(colours) + 3) // 4
    return y + rows * (SWATCH + 44)


def sheet(app: str, screens: list[str], old_ref: str) -> Image.Image:
    shots = f"apps/{app}/app/src/test/screenshots"
    rows = []
    for screen in screens:
        row = [
            ("Before", shot_at(f"{shots}/{screen}.png", old_ref)),
            ("Light", shot_at(f"{shots}/{screen}_light.png", None)),
            ("Dark", shot_at(f"{shots}/{screen}.png", None)),
        ]
        rows.append((screen, [(label, scaled(img)) for label, img in row if img is not None]))
    design = (ROOT / "apps" / app / "DESIGN.md").read_text(encoding="utf-8")
    title = re.search(r"^name:\s*(.+)$", design, re.M)
    colours = swatches(design)

    width = MARGIN * 2 + 3 * SHOT_W + 2 * GAP
    width = max(width, MARGIN * 2 + 4 * 260)
    heights = [max(img.height for _, img in row) for _, row in rows]
    height = MARGIN + 110 + sum(h + 70 for h in heights) + 2 * (40 + 3 * (SWATCH + 44)) + 140
    canvas = Image.new("RGB", (width, height), PAPER)
    draw = ImageDraw.Draw(canvas)
    draw.text((MARGIN, MARGIN), title.group(1) if title else app, font=font(44, BOLD), fill=INK)
    draw.text((MARGIN, MARGIN + 60), f"Display face: {DISPLAY_FACE}", font=font(24, REGULAR), fill=GREY)
    y = MARGIN + 110
    for (screen, row), h in zip(rows, heights):
        for i, (label, img) in enumerate(row):
            x = MARGIN + i * (SHOT_W + GAP)
            draw.text((x, y), f"{label} · {screen}", font=font(22, BOLD), fill=INK)
            canvas.paste(img, (x, y + 36))
            draw.rounded_rectangle([x, y + 36, x + img.width, y + 36 + img.height], radius=6, outline=(0, 0, 0))
        y += h + 70
    for look in ("light", "dark"):
        if colours[look]:
            y = draw_swatches(draw, MARGIN, y, f"{look.capitalize()} palette", colours[look]) + 20
    return canvas.crop((0, 0, width, y + MARGIN))


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__.split("\n\n")[0])
    parser.add_argument("app")
    parser.add_argument("screens", nargs="+", help="screenshot names without .png, e.g. home wheel")
    parser.add_argument("--old-ref", default="origin/master", help="git ref the 'Before' images come from")
    parser.add_argument("--out", required=True, type=Path)
    args = parser.parse_args()
    sheet(args.app, args.screens, args.old_ref).save(args.out)
    print(args.out)
