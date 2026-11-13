"""Builds the Google Play listing graphics of Who's lying? from raw app screenshots.

The drawing lives in scripts/store_assets.py (shared by every app); this file only holds the
app's colours and headlines. Inputs and outputs: see that module.

Usage (repo root): .venv/bin/python apps/whos-lying/tools/generate_store_assets.py
"""
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
sys.path.insert(0, str(ROOT.parent.parent / "scripts"))
from store_assets import Style, build  # noqa: E402

# The app's "Neon" palette.
NIGHT_TOP, NIGHT = (43, 27, 107), (20, 5, 43)
GOLD, SNOW = (255, 201, 77), (238, 224, 255)
ACCENTS = [(124, 92, 255), (0, 224, 184), (255, 77, 148), (255, 201, 77)]

LOCALES = {
    "en-US": {
        "title": "Who's lying? Impostor game",
        "tagline": "3 modes · Drawing · 21 packs · No ads",
        "headlines": {
            "01-reveal": ("Everyone gets the word…", "except the impostor"),
            "02-debate": ("Give clues", "against the clock"),
            "03-vote": ("Vote out", "who's lying"),
            "04-result": ("Caught them…", "or got fooled?"),
            "05-drawing": ("Drawing mode:", "no talking allowed"),
            "06-packs": ("21 packs, 600+ words", "all free"),
            "06b-night": ("Lavender by day,", "neon by night"),
            "07-home": ("Free. No ads.", "No catch."),
        },
    },
    "es-ES": {
        "title": "¿Quién miente? Impostor",
        "tagline": "3 modos · Dibujo · 21 paquetes · Sin anuncios",
        "headlines": {
            "01-reveal": ("Todos tienen la palabra…", "menos el impostor"),
            "02-debate": ("Dad pistas", "contra el reloj"),
            "03-vote": ("Votad a quién", "creéis que miente"),
            "04-result": ("¿Pilláis al impostor…", "u os la cuela?"),
            "05-drawing": ("Modo dibujo:", "prohibido hablar"),
            "06-packs": ("21 paquetes, +600 palabras", "todos gratis"),
            "06b-night": ("Lavanda de día,", "neón de noche"),
            "07-home": ("Gratis. Sin anuncios.", "Sin trampas."),
        },
    },
}

STYLE = Style(
    top=NIGHT_TOP,
    bottom=NIGHT,
    headline=SNOW,
    highlight=GOLD,
    tagline=(182, 163, 209),
    accents=ACCENTS,
)


if __name__ == "__main__":
    build(ROOT, STYLE, LOCALES)
