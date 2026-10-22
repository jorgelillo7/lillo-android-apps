"""Reads app/src/main/assets/sprites.txt (the game's pixel art) for the asset scripts.

load(path) -> {name: [[colour or None per pixel] per row]}, colours as "#RRGGBB". Same format the
app parses (domain/Sprites.kt): a palette block, then sprite blocks, '.' transparent.
"""


def load(path):
    palette, sprites, section, rows = {".": None}, {}, None, []
    for raw in open(path, encoding="utf-8"):
        line = raw.split("#", 1)[0].rstrip()
        if not line.strip():
            continue
        if line == "palette":
            section = "palette"
        elif line.startswith("sprite "):
            section, rows = line[len("sprite "):].strip(), []
        elif line == "end":
            if section not in (None, "palette"):
                sprites[section] = [[palette[c] for c in r] for r in rows]
            section = None
        elif section == "palette":
            key, hexa = line.split()[:2]
            palette[key] = "#" + hexa
        elif section:
            rows.append(line.strip())
    return sprites
