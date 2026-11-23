"""Checks that a game's code uses nothing its engine profile leaves out, before building or
exporting with that profile: a removed module would only fail at run time, on the phone.

Usage: python3 scripts/engine_profile_check.py engine/<game>   (exit 1 and a list when it finds any)
"""
import re
import sys
from pathlib import Path

# What each switch in an engine profile takes away, as names the game's scripts and scenes would use.
REMOVES = {
    "disable_physics_2d": r"RigidBody2D|CharacterBody2D|StaticBody2D|Area2D|CollisionShape2D|CollisionPolygon2D|PhysicsServer2D|RayCast2D|ShapeCast2D",
    "disable_physics_3d": r"RigidBody3D|CharacterBody3D|StaticBody3D|Area3D|CollisionShape3D|CollisionPolygon3D|PhysicsServer3D|RayCast3D|ShapeCast3D|VehicleBody3D|SoftBody3D",
    "disable_navigation_2d": r"NavigationAgent2D|NavigationRegion2D|NavigationServer2D|NavigationObstacle2D",
    "disable_navigation_3d": r"NavigationAgent3D|NavigationRegion3D|NavigationServer3D|NavigationObstacle3D",
    "disable_xr": r"\bXR[A-Z]\w*|XRServer",
    "module_csg_enabled": r"\bCSG\w+",
    "module_gridmap_enabled": r"\bGridMap\b",
    "module_noise_enabled": r"\b\w*Noise(Texture2D|Texture3D)?\b|FastNoiseLite",
    "module_ogg_enabled": r"AudioStreamOggVorbis|\.ogg\b",
    "module_vorbis_enabled": r"AudioStreamOggVorbis|\.ogg\b",
    "module_minimp3_enabled": r"AudioStreamMP3|\.mp3\b",
    "module_theora_enabled": r"VideoStreamTheora|\.ogv\b",
    "module_regex_enabled": r"\bRegEx\b",
    "module_websocket_enabled": r"WebSocket\w*",
    "module_webrtc_enabled": r"WebRTC\w*",
    "module_enet_enabled": r"ENet\w*",
    "module_multiplayer_enabled": r"Multiplayer\w*|@rpc",
    "module_upnp_enabled": r"\bUPNP\w*",
    "module_gltf_enabled": r"GLTF\w+",
    "module_jsonrpc_enabled": r"\bJSONRPC\b",
    "module_zip_enabled": r"\bZIP(Reader|Packer)\b",
    "module_camera_enabled": r"\bCameraFeed\b|CameraServer",
    "module_lightmapper_rd_enabled": r"\bLightmapGI\b",
    "module_raycast_enabled": r"OccluderInstance3D",
}
OFF = {"yes": True, "no": False}


def removed(profile: Path) -> list:
    """The switches in `profile` that take something away."""
    out = []
    for line in profile.read_text().splitlines():
        m = re.match(r'\s*(\w+)\s*=\s*"(yes|no)"', line)
        if not m or m.group(1) not in REMOVES:
            continue
        name, value = m.groups()
        if name.startswith("disable_") and value == "yes" or name.startswith("module_") and value == "no":
            out.append(name)
    return out


def check(game: Path) -> list:
    """(file, line, switch, text) for every use of something the profile removes."""
    switches = removed(game / "engine-profile.py")
    hits = []
    sources = [p for p in game.rglob("*") if p.suffix in (".gd", ".tscn", ".tres", ".godot")
               and "/addons/gut/" not in str(p) and "/tests/" not in str(p) and "/tools/" not in str(p)]
    for core in (game.parent / "core" / "addons" / "lillo_core").rglob("*.gd"):
        sources.append(core)
    for path in sources:
        for number, text in enumerate(path.read_text(errors="ignore").splitlines(), start=1):
            for switch in switches:
                if re.search(REMOVES[switch], text):
                    hits.append((path, number, switch, text.strip()))
    return hits


if __name__ == "__main__":
    game = Path(sys.argv[1])
    if not (game / "engine-profile.py").exists():
        print(f"{game}: no engine-profile.py, nothing to check")
        sys.exit(0)
    found = check(game)
    for path, number, switch, text in found:
        print(f"{path}:{number}: needs what {switch} removes: {text}")
    if found:
        print("Turn the switch back on in engine-profile.py, or stop using it.")
        sys.exit(1)
    print(f"{game}: uses nothing its engine profile removes")
