"""Builds the Google Play listing graphics of Last one standing? from raw app screenshots.

The drawing lives in scripts/store_assets.py (shared by every app); this file only holds the
app's colours and headlines. Inputs and outputs: see that module.

Usage (repo root): .venv/bin/python apps/tournaments/tools/generate_store_assets.py
"""
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
sys.path.insert(0, str(ROOT.parent.parent / "scripts"))
from store_assets import Style, build  # noqa: E402

# The app's "Arena" palette: deep blue night, cream text, gold for winners.
INK_TOP, INK = (30, 24, 74), (12, 7, 46)
GOLD, CREAM = (255, 200, 61), (231, 226, 255)
ACCENTS = [(255, 200, 61), (157, 167, 255), (255, 107, 107), (108, 142, 245)]

LOCALES = {
    "en-US": {
        "title": "Last one standing? Tournaments",
        "tagline": "Knockout · Swiss · No accounts · No ads",
        "headlines": {
            "01-champion": ("Only one", "takes the crown"),
            "02-result": ("Results in", "one tap"),
            "03-bracket": ("A two-sided bracket", "with the victory line"),
            "04-standings": ("Swiss with", "proper tie-breakers"),
            "05-share": ("To another phone", "with a QR code"),
            "06-stats": ("Your league's", "hall of fame"),
        },
    },
    "es-ES": {
        "title": "¿Solo quedará uno? Torneos",
        "tagline": "Eliminatoria · Suizo · Sin cuentas · Sin anuncios",
        "headlines": {
            "01-champion": ("Solo uno", "se lleva la corona"),
            "02-result": ("Resultados", "en un toque"),
            "03-bracket": ("Cuadro a dos lados", "con la línea de victoria"),
            "04-standings": ("Suizo con", "desempates de verdad"),
            "05-share": ("A otro móvil", "con un QR"),
            "06-stats": ("El salón de la fama", "de vuestra liga"),
        },
    },
}

STYLE = Style(
    top=INK_TOP,
    bottom=INK,
    headline=CREAM,
    highlight=GOLD,
    tagline=(171, 166, 213),
    accents=ACCENTS,
)


if __name__ == "__main__":
    build(ROOT, STYLE, LOCALES)
