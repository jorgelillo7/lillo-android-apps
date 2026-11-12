"""Composes the raw store screenshot of the home-screen widget (store/raw/<locale>/05-widget.png).

A launcher can't be driven to place a widget reliably on the emulator, so this draws one: a
plain home screen, the app's icon, and the widget as JaleoWidget lays it out (widget_busy.xml's
gradient and 24dp corners, 14dp padding, 16/24/15sp texts in Ink), at the emulator's 1080x2424 and
density 2.625. The texts are the app's own strings for tomorrow's sample match. generate_store_assets.py
then frames it like the other screenshots.

Usage (repo root): .venv/bin/python apps/stadium-events/tools/compose_widget_screenshot.py
Needs Android Studio's Roboto (layoutlib fonts) and the 512px icon in store/.
"""
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parent.parent
FONT = Path("/Applications/Android Studio.app/Contents/plugins/design-tools/resources/layoutlib/data/fonts/Roboto-Regular.ttf")
W, H, DP = 1080, 2424, 2.625

INK = (0x1E, 0x1B, 0x18)
BUSY_START, BUSY_END = (0xF2, 0xB5, 0x44), (0xE0, 0xA1, 0x2E)
WALL_TOP, WALL_BOTTOM = (36, 71, 107), (14, 28, 44)

# The widget's lines and the launcher's label, as the app shows them for a Champions match at 21:00.
TEXTS = {
    "en-US": {"label": "Tomorrow", "headline": "Match · 21:00", "line": "Atlético – Viking FK (Champions)",
              "app": "Busy day?"},
    "es-ES": {"label": "Mañana", "headline": "Partido · 21:00", "line": "Atlético – Viking FK (Champions)",
              "app": "¿Hay jaleo?"},
}


def px(dp: float) -> int:
    return round(dp * DP)


def roboto(sp: float, bold: bool) -> ImageFont.FreeTypeFont:
    font = ImageFont.truetype(str(FONT), px(sp))
    font.set_variation_by_axes([700 if bold else 400])
    return font


def vertical_gradient(size, top, bottom) -> Image.Image:
    w, h = size
    img = Image.new("RGB", size)
    d = ImageDraw.Draw(img)
    for y in range(h):
        t = y / max(1, h - 1)
        d.line([(0, y), (w, y)], fill=tuple(round(a + (b - a) * t) for a, b in zip(top, bottom)))
    return img


def diagonal_gradient(size, start, end) -> Image.Image:
    """widget_busy.xml's 315° gradient: start at the top left, end at the bottom right."""
    w, h = size
    img = Image.new("RGB", size)
    px_ = img.load()
    for y in range(h):
        for x in range(w):
            t = (x / w + y / h) / 2
            px_[x, y] = tuple(round(a + (b - a) * t) for a, b in zip(start, end))
    return img


def rounded(img: Image.Image, radius: int) -> Image.Image:
    mask = Image.new("L", img.size, 0)
    ImageDraw.Draw(mask).rounded_rectangle([0, 0, img.width - 1, img.height - 1], radius=radius, fill=255)
    out = img.convert("RGBA")
    out.putalpha(mask)
    return out


def widget(texts) -> Image.Image:
    w, h = px(300), px(140)
    card = rounded(diagonal_gradient((w, h), BUSY_START, BUSY_END), px(24))
    d = ImageDraw.Draw(card)
    fonts = [(texts["label"], roboto(16, True)), (texts["headline"], roboto(24, True)), (texts["line"], roboto(15, False))]
    heights = [d.textbbox((0, 0), t, font=f)[3] for t, f in fonts]
    gap = px(4)
    y = (h - sum(heights) - gap * (len(fonts) - 1)) // 2
    for (text, font), height in zip(fonts, heights):
        d.text((px(14), y), text, font=font, fill=INK)
        y += height + gap
    return card


def home_screen(locale: str) -> Image.Image:
    texts = TEXTS[locale]
    screen = vertical_gradient((W, H), WALL_TOP, WALL_BOTTOM).convert("RGBA")
    d = ImageDraw.Draw(screen)
    white = (255, 255, 255)
    d.text((px(24), px(14)), "12:25", font=roboto(14, False), fill=white)
    d.text((px(24), px(64)), "12:25", font=roboto(72, False), fill=white)

    card = widget(texts)
    shadow = Image.new("RGBA", (card.width + px(16), card.height + px(16)), (0, 0, 0, 0))
    ImageDraw.Draw(shadow).rounded_rectangle(
        [px(8), px(10), card.width + px(8), card.height + px(10)], radius=px(24), fill=(0, 0, 0, 90)
    )
    x, y = (W - card.width) // 2, px(260)
    screen.alpha_composite(shadow, (x - px(8), y - px(6)))
    screen.alpha_composite(card, (x, y))

    icon = Image.open(ROOT / "store" / "play-icon-512.png").convert("RGBA").resize((px(56), px(56)))
    icon = rounded(icon, px(28))
    ix, iy = (W - icon.width) // 2, H - px(200)
    screen.alpha_composite(icon, (ix, iy))
    label_font = roboto(13, False)
    lw = d.textlength(texts["app"], font=label_font)
    d.text(((W - lw) / 2, iy + icon.height + px(6)), texts["app"], font=label_font, fill=white)
    return screen


if __name__ == "__main__":
    for locale in TEXTS:
        out = ROOT / "store" / "raw" / locale / "05-widget.png"
        home_screen(locale).save(out)
        print(out.relative_to(ROOT.parent.parent))
