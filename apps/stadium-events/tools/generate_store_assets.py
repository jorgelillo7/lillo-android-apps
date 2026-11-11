"""Builds the Google Play listing graphics of Busy day? Metropolitano from raw app screenshots.

The drawing lives in scripts/store_assets.py (shared by every app); this file only holds the
app's colours and headlines. Inputs and outputs: see that module.

Usage (repo root): .venv/bin/python apps/stadium-events/tools/generate_store_assets.py
"""
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
sys.path.insert(0, str(ROOT.parent.parent / "scripts"))
from store_assets import Style, build  # noqa: E402

# The icon's night blue behind the light app, with the answer cards' amber, coral and green.
NAVY_TOP, NAVY = (36, 71, 107), (19, 38, 58)
CREAM, AMBER = (251, 247, 241), (242, 181, 68)
ACCENTS = [(242, 181, 68), (232, 112, 95), (140, 199, 164), (143, 184, 242)]

LOCALES = {
    "en-US": {
        "title": "Busy day? Metropolitano",
        "tagline": "Matches · Concerts · Finals · No setup · No ads",
        "headlines": {
            "01-busy": ("A match tomorrow?", "Know it the day before"),
            "02-calm": ("A quiet day?", "Just as clear"),
            "03-maybe": ("Day not fixed yet?", "It says so"),
            "04-final": ("Concerts and finals", "the biggest days"),
        },
    },
    "es-ES": {
        "title": "¿Hay jaleo? Metropolitano",
        "tagline": "Partidos · Conciertos · Finales · Sin configurar · Sin anuncios",
        "headlines": {
            "01-busy": ("¿Mañana hay partido?", "Entérate la víspera"),
            "02-calm": ("¿Día tranquilo?", "También te lo dice"),
            "03-maybe": ("¿Día sin confirmar?", "Te lo cuenta tal cual"),
            "04-final": ("Conciertos y finales", "los días más gordos"),
        },
    },
}

STYLE = Style(
    top=NAVY_TOP,
    bottom=NAVY,
    headline=CREAM,
    highlight=AMBER,
    tagline=(200, 214, 230),
    accents=ACCENTS,
)


if __name__ == "__main__":
    build(ROOT, STYLE, LOCALES)
