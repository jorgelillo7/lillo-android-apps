"""Builds the Google Play listing graphics of ¿Te pilla la chancla? from raw app screenshots.

The drawing lives in scripts/store_assets.py (shared by every app); this file only holds the
app's colours and headlines. Inputs and outputs: see that module.

Usage (repo root): .venv/bin/python apps/slipper/tools/generate_store_assets.py
"""
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
sys.path.insert(0, str(ROOT.parent.parent / "scripts"))
from store_assets import Style, build  # noqa: E402

# Grandma's kitchen: tile blue, cream, her red slipper, croqueta gold.
TILE, TILE_DEEP = (70, 140, 200), (44, 92, 150)
CREAM, SLIPPER, CROQUETA, GREEN = (255, 236, 200), (229, 70, 90), (247, 190, 60), (46, 160, 120)

LOCALES = {
    "es-ES": {
        "title": "¿Te pilla la chancla?",
        "tagline": "Huye de la abuela · Un dedo · Sin anuncios",
        "headlines": {
            "01-home": ("Huye de la abuela", "y de su chancla"),
            "02-run": ("Salta sus chanclas", "y róbale las croquetas"),
            "03-run": ("Combos, bocatas", "y ¡por los pelos!"),
            "04-result": ("¿Te superan?", "Reta con un enlace"),
            "05-daily": ("Reto del día", "igual para todos"),
        },
    },
    "en-US": {
        "title": "Will the slipper get you?",
        "tagline": "Run from grandma · One finger · No ads",
        "headlines": {
            "01-home": ("Run from grandma", "and her slipper"),
            "02-run": ("Jump her slippers", "steal her croquetas"),
            "03-run": ("Combos, sandwiches", "and close calls"),
            "04-result": ("Can they beat you?", "Challenge by link"),
            "05-daily": ("Run of the day", "the same for everyone"),
        },
    },
}

STYLE = Style(
    top=TILE,
    bottom=TILE_DEEP,
    headline=CREAM,
    highlight=CROQUETA,
    tagline=(220, 232, 245),
    accents=[SLIPPER, CROQUETA, GREEN, CREAM],
)


if __name__ == "__main__":
    build(ROOT, STYLE, LOCALES)
