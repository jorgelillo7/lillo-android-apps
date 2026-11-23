"""Builds Summandos' Google Play listing graphics from its raw store screenshots.

The drawing lives in scripts/store_assets.py (shared with the apps); this file only holds the
game's colours and headlines. Raw screenshots: tools/capture_store_screenshots.sh. Output goes to
play/src/main/play/listings/, where the game's Gradle Play Publisher module will read it (S4).

Usage (repo root): .venv/bin/python engine/summandos/tools/generate_store_assets.py
"""
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
sys.path.insert(0, str(ROOT.parent.parent / "scripts"))
from store_assets import Style, build  # noqa: E402

# The sunset palette (game/palette.gd).
SKY_TOP, SKY = (255, 143, 107), (214, 96, 72)
CREAM, GOLD, TEAL, PURPLE = (255, 244, 230), (255, 210, 63), (19, 168, 158), (123, 63, 209)

LOCALES = {
    "es-ES": {
        "title": "Sumandos",
        "tagline": "Mates de cabeza · Un pelotón enorme · Sin anuncios",
        "headlines": {
            "01-menu": ("Haz crecer tu pelotón", "con mates de cabeza"),
            "02-gates": ("¿Qué puerta da más?", "Elige bien y suma"),
            "02b-cast": ("Ballesteros contra esqueletos", "tu pelotón, de tu color"),
            "03-battle": ("Cientos de ballesteros", "contra la horda"),
            "04-boss": ("Jefes enormes", "cada cinco niveles"),
            "05-think": ("Modo Leyenda:", "dos pasos, sin fallar"),
            "06-levels": ("Cuatro rangos", "y 100 niveles con estrellas"),
        },
    },
    "en-US": {
        "title": "Summandos",
        "tagline": "Mental maths · A huge squad · No ads",
        "headlines": {
            "01-menu": ("Grow your squad", "with mental maths"),
            "02-gates": ("Which gate gives more?", "Pick well and add up"),
            "02b-cast": ("Crossbows versus skeletons", "your squad, your colour"),
            "03-battle": ("Hundreds of crossbows", "against the horde"),
            "04-boss": ("Giant bosses", "every five levels"),
            "05-think": ("Legend mode:", "two steps, no mistakes"),
            "06-levels": ("Four ranks", "and 100 levels with stars"),
        },
    },
}

STYLE = Style(
    top=SKY_TOP,
    bottom=SKY,
    headline=CREAM,
    highlight=GOLD,
    tagline=(255, 230, 210),
    accents=[GOLD, TEAL, PURPLE, CREAM],
)


if __name__ == "__main__":
    build(ROOT, STYLE, LOCALES, ROOT / "play" / "src" / "main" / "play" / "listings")
