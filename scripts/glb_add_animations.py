#!/usr/bin/env python3
"""Copies named animations from rig .glb files into a character .glb that ships without them.

KayKit 1.1 packs keep a character's mesh and its rig's animations in separate files; Godot
plays animations only from the scene that holds the skeleton. Channels are matched to the
character's nodes by name, so the rig and the character must share bone names.

Usage: glb_add_animations.py <character.glb> <out.glb> <rig.glb:Anim,Anim> [<rig.glb:Anim> ...]
"""
import json
import struct
import sys

GLB_MAGIC = 0x46546C67
JSON_CHUNK = 0x4E4F534A
BIN_CHUNK = 0x004E4942


def read_glb(path: str) -> tuple[dict, bytearray]:
    data = open(path, "rb").read()
    magic, _, _ = struct.unpack_from("<III", data, 0)
    if magic != GLB_MAGIC:
        raise SystemExit(f"{path}: not a .glb")
    offset, doc, binary = 12, None, bytearray()
    while offset < len(data):
        length, kind = struct.unpack_from("<II", data, offset)
        chunk = data[offset + 8:offset + 8 + length]
        if kind == JSON_CHUNK:
            doc = json.loads(chunk)
        elif kind == BIN_CHUNK:
            binary = bytearray(chunk)
        offset += 8 + length
    return doc, binary


def write_glb(path: str, doc: dict, binary: bytearray) -> None:
    text = json.dumps(doc, separators=(",", ":")).encode()
    text += b" " * (-len(text) % 4)
    binary += b"\0" * (-len(binary) % 4)
    total = 12 + 8 + len(text) + 8 + len(binary)
    with open(path, "wb") as out:
        out.write(struct.pack("<III", GLB_MAGIC, 2, total))
        out.write(struct.pack("<II", len(text), JSON_CHUNK) + text)
        out.write(struct.pack("<II", len(binary), BIN_CHUNK) + binary)


def copy_accessor(index: int, rig: dict, rig_bin: bytearray, doc: dict, binary: bytearray) -> int:
    """Appends the rig's accessor `index` (and its bytes) to the character; returns its new index."""
    accessor = dict(rig["accessors"][index])
    view = rig["bufferViews"][accessor["bufferView"]]
    start = view.get("byteOffset", 0)
    chunk = rig_bin[start:start + view["byteLength"]]
    binary += b"\0" * (-len(binary) % 4)
    new_view = {"buffer": 0, "byteOffset": len(binary), "byteLength": len(chunk)}
    if "byteStride" in view:
        new_view["byteStride"] = view["byteStride"]
    binary += chunk
    doc.setdefault("bufferViews", []).append(new_view)
    accessor["bufferView"] = len(doc["bufferViews"]) - 1
    doc.setdefault("accessors", []).append(accessor)
    return len(doc["accessors"]) - 1


def add_animations(doc: dict, binary: bytearray, rig_path: str, names: list[str]) -> None:
    rig, rig_bin = read_glb(rig_path)
    nodes = {node.get("name"): i for i, node in enumerate(doc["nodes"])}
    found = {a["name"]: a for a in rig.get("animations", [])}
    for name in names:
        if name not in found:
            raise SystemExit(f"{rig_path}: no animation {name} (has {', '.join(found)})")
        source = found[name]
        samplers = []
        for sampler in source["samplers"]:
            samplers.append({
                "input": copy_accessor(sampler["input"], rig, rig_bin, doc, binary),
                "output": copy_accessor(sampler["output"], rig, rig_bin, doc, binary),
                "interpolation": sampler.get("interpolation", "LINEAR"),
            })
        channels = []
        for channel in source["channels"]:
            bone = rig["nodes"][channel["target"]["node"]].get("name")
            if bone not in nodes:
                raise SystemExit(f"{rig_path}: bone {bone} is not in the character")
            channels.append({"sampler": channel["sampler"],
                             "target": {"node": nodes[bone], "path": channel["target"]["path"]}})
        doc.setdefault("animations", []).append({"name": name, "samplers": samplers, "channels": channels})
    doc["buffers"][0]["byteLength"] = len(binary) + (-len(binary) % 4)


def main() -> None:
    if len(sys.argv) < 4:
        raise SystemExit(__doc__)
    doc, binary = read_glb(sys.argv[1])
    for spec in sys.argv[3:]:
        rig_path, _, names = spec.rpartition(":")
        add_animations(doc, binary, rig_path, names.split(","))
    write_glb(sys.argv[2], doc, binary)


if __name__ == "__main__":
    main()
