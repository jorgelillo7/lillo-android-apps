"""Builds the Google Play listing graphics of Christmas Countdown from raw app screenshots.

The drawing lives in scripts/store_assets.py (shared by every app); this file only holds the
app's colours and headlines. Inputs and outputs: see that module.

Usage (repo root): .venv/bin/python apps/christmas-countdown/tools/generate_store_assets.py
"""
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
sys.path.insert(0, str(ROOT.parent.parent / "scripts"))
from store_assets import Style, build  # noqa: E402

WINE, MIDNIGHT = (74, 14, 28), (11, 20, 38)
GOLD, SNOW = (255, 200, 87), (255, 248, 238)

LOCALES = {
    "en-US": {
        "title": "Is it Christmas yet? Countdown",
        "tagline": "Days left · Advent calendar · Widget · Carols",
        "headlines": {
            "01-countdown": ("Every second", "until Christmas"),
            "02-sleeps": ("Count the sleeps,", "just like kids do"),
            "03-advent": ("A surprise every day", "of December"),
            "04-plan": ("A family plan", "every day"),
            "05-music": ("Music days", "with Christmas songs"),
            "06-about": ("No ads.", "No tracking."),
        },
    },
    "es-ES": {
        "title": "¿Ya es Navidad? Cuenta atrás",
        "tagline": "Días que faltan · Adviento · Widget · Villancicos",
        "headlines": {
            "01-countdown": ("Cada segundo", "hasta Navidad"),
            "02-sleeps": ("Cuenta las noches,", "como los peques"),
            "03-advent": ("Una sorpresa cada día", "de diciembre"),
            "04-plan": ("Un plan en familia", "cada día"),
            "05-music": ("Días de música", "con villancicos"),
            "06-about": ("Sin anuncios.", "Sin rastreo."),
        },
    },
}

STYLE = Style(
    top=WINE,
    bottom=MIDNIGHT,
    headline=SNOW,
    highlight=GOLD,
    tagline=(230, 210, 210),
    decoration="snow",
    feature_seed=25,
)


if __name__ == "__main__":
    build(ROOT, STYLE, LOCALES)
