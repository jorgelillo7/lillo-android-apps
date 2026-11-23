"""One image to judge a game-engine project's launcher icon before a release: the legacy icon at
144, 96 and 48 px on a light and a dark wallpaper, the adaptive layers under a round mask, and
the monochrome layer on a themed background.

Usage (repo root): .venv/bin/python scripts/engine_icon_preview.py engine/<game> <out.png>
Reads <game>/assets/icons/{main_192,adaptive_*_432}.png, as tools/generate_icon.py writes them.
"""
import sys
from pathlib import Path

from PIL import Image, ImageDraw


def preview(project: Path, out: Path) -> None:
    icons = project / "assets" / "icons"
    legacy = Image.open(icons / "main_192.png").convert("RGBA")
    adaptive = Image.alpha_composite(
        Image.open(icons / "adaptive_background_432.png").convert("RGBA"),
        Image.open(icons / "adaptive_foreground_432.png").convert("RGBA"))
    mono = Image.open(icons / "adaptive_monochrome_432.png").convert("RGBA")

    board = Image.new("RGB", (880, 260), "#F4F4F4")
    board.paste(Image.new("RGB", (360, 260), "#1C1C22"), (360, 0))
    for left in (0, 360):
        x = left + 10
        for size in (144, 96, 48):
            icon = legacy.resize((size, size), Image.LANCZOS)
            board.paste(icon, (x, 40), icon)
            x += size + 15
    mask = Image.new("L", (432, 432), 0)
    ImageDraw.Draw(mask).ellipse([54, 54, 378, 378], fill=255)
    round_icon = Image.new("RGBA", (432, 432), (0, 0, 0, 0))
    round_icon.paste(adaptive, (0, 0), mask)
    round_icon = round_icon.crop((54, 54, 378, 378)).resize((144, 144), Image.LANCZOS)
    board.paste(round_icon, (725, 10), round_icon)
    themed = mono.resize((96, 96), Image.LANCZOS)
    board.paste(Image.new("RGB", (110, 110), "#3A3A44"), (740, 150))
    board.paste(themed, (747, 157), themed)
    board.save(out)


if __name__ == "__main__":
    if len(sys.argv) != 3:
        sys.exit(__doc__)
    preview(Path(sys.argv[1]), Path(sys.argv[2]))
    print(sys.argv[2])
