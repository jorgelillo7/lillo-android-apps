"""Builds the Google Play listing graphics of What next? Decision Wheel from raw app screenshots.

The drawing lives in scripts/store_assets.py (shared by every app); this file only holds the
app's colours and headlines. Inputs and outputs: see that module.

Usage (repo root): .venv/bin/python apps/decision-wheel/tools/generate_store_assets.py
"""
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
sys.path.insert(0, str(ROOT.parent.parent / "scripts"))
from store_assets import Style, build  # noqa: E402

# The dark look (DESIGN.md): wine from #2A0F25 to #1E0A1B, cream text, sun yellow.
NIGHT_TOP, NIGHT = (42, 15, 37), (30, 10, 27)
GOLD, SNOW = (255, 217, 61), (251, 234, 242)
ACCENTS = [(255, 107, 107), (255, 179, 71), (255, 217, 61), (78, 205, 196), (108, 142, 245), (199, 146, 234)]

LOCALES = {
    "en-US": {
        "title": "What next? Decision Wheel",
        "tagline": "Avoids repeats · Vetoes · History · No ads",
        "headlines": {
            "01-wheel": ("Can't decide?", "Spin it."),
            "01b-night": ("By day or by night,", "as you like"),
            "02-result": ("The wheel decides,", "you just go"),
            "03-repeats-vetoes": ("No repeats.", "Veto what you don't fancy"),
            "04-home": ("Ready-made wheels", "or your own"),
        },
    },
    "es-ES": {
        "title": "¿Qué hacemos? Ruleta",
        "tagline": "No repite · Vetos · Historial · Sin anuncios",
        "headlines": {
            "01-wheel": ("¿No os decidís?", "Gira la ruleta"),
            "01b-night": ("De día o de noche,", "como prefieras"),
            "02-result": ("La ruleta decide,", "vosotros vais"),
            "03-repeats-vetoes": ("No repite lo de ayer", "y puedes vetar"),
            "04-home": ("Ruletas preparadas", "o las tuyas"),
        },
    },
}

STYLE = Style(
    top=NIGHT_TOP,
    bottom=NIGHT,
    headline=SNOW,
    highlight=GOLD,
    tagline=(212, 182, 200),
    accents=ACCENTS,
)


if __name__ == "__main__":
    build(ROOT, STYLE, LOCALES)
