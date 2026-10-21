"""Builds the Google Play listing graphics of ¿Quién llora esta jornada? from raw app screenshots.

The drawing lives in scripts/store_assets.py (shared by every app); this file only holds the
app's colours and headlines. Inputs and outputs: see that module. The raw screenshots must come
from the sample deck (tools/capture_store_screenshots.sh makes sure), never the private one.

Usage (repo root): .venv/bin/python apps/league-cards/tools/generate_store_assets.py
"""
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
sys.path.insert(0, str(ROOT.parent.parent / "scripts"))
from store_assets import Style, build  # noqa: E402

# The app's chat palette: dark chat background, chat green, gold for the champion, tear blue.
INK_TOP, INK = (22, 37, 46), (11, 20, 26)
GREEN, GOLD, TEAR, RED = (0, 168, 132), (255, 200, 61), (83, 189, 235), (241, 92, 109)

LOCALES = {
    "es-ES": {
        "title": "¿Quién llora esta jornada?",
        "tagline": "Cartas para tu liga fantasy · Sin cuentas · Sin anuncios",
        "headlines": {
            "01-card": ("Decide", "carta a carta"),
            "02-joker": ("Un comodín", "por temporada"),
            "03-chat": ("El chat de la liga", "tras cada jornada"),
            "04-table": ("La clasificación", "jornada a jornada"),
            "05-ending": ("Cada final", "es distinto"),
            "06-trophies": ("Logros, finales", "e historial"),
        },
    },
}

STYLE = Style(
    top=INK_TOP,
    bottom=INK,
    headline=(233, 237, 239),
    highlight=GOLD,
    tagline=(134, 150, 160),
    accents=[GREEN, GOLD, TEAR, RED],
)


if __name__ == "__main__":
    build(ROOT, STYLE, LOCALES)
