## Loads Summandos' third-party models (assets/models, fetched by scripts/engine-assets.sh) and
## builds animated characters from them in a team colour. Every call returns null without the
## models, so callers fall back to the game's own figures.
class_name SummandosModels
extends RefCounted

const DIR := "res://assets/models/"
## Native height of a KayKit character, in metres, before scaling.
const NATIVE_HEIGHT := 2.4
## Models from a bought pack, and the free model shown in their place on a machine without it.
const STAND_INS := {"Skeleton_Golem.glb": "Skeleton_Warrior.glb", "Necromancer.glb": "Skeleton_Mage.glb"}
## The bought pack's rigs name their clips differently; each asked-for clip and its stand-in.
const CLIP_STAND_INS := {"Walking_B": "Walking_A", "Idle_Combat": "Idle_A"}
## Models whose native height is not NATIVE_HEIGHT: the Golem is drawn a third bigger than the rest.
const NATIVE_HEIGHTS := {"Skeleton_Golem.glb": 3.2}
## Spare kit on the crossbowman model that the squad does not carry.
const ARCHER_SPARES := ["Knife_Offhand", "1H_Crossbow", "Knife", "Throwable"]


static func scene(file: String) -> PackedScene:
	return load(DIR + file) if ResourceLoader.exists(DIR + file) else null


## The model to load for `file`: itself, or its free stand-in when the bought pack is missing.
static func available(file: String) -> String:
	if not ResourceLoader.exists(DIR + file) and STAND_INS.has(file):
		return STAND_INS[file]
	return file


## The model's texture repainted in `colour` (LilloMaterials.team).
static func team(mesh: Mesh, colour: Color) -> Material:
	var own: StandardMaterial3D = mesh.surface_get_material(0)
	return LilloMaterials.team(own.albedo_texture, colour)


## One character from `file` playing `animation` in a loop, `height` tall, painted `colour`;
## `facing_us` turns it to look down -z. Null when the model is not there.
static func character(file: String, animation: String, height: float, colour: Color, facing_us := false,
		spares: Array = []) -> Node3D:
	var packed := scene(available(file))
	if packed == null:
		return null
	var holder := Node3D.new()
	var body: Node3D = packed.instantiate()
	holder.add_child(body)
	for name in spares:
		var part := body.find_child(name, true, false)
		if part:
			part.free()
	for part in body.find_children("*", "MeshInstance3D", true, false):
		if part.mesh:
			part.material_override = team(part.mesh, colour)
	var player: AnimationPlayer = body.find_children("*", "AnimationPlayer", true, false)[0]
	for clip in player.get_animation_list():
		player.get_animation(clip).loop_mode = Animation.LOOP_LINEAR
	if not player.has_animation(animation):
		animation = CLIP_STAND_INS.get(animation, animation)
	if player.has_animation(animation):
		player.play(animation)
	body.scale = Vector3.ONE * (height / NATIVE_HEIGHTS.get(available(file), NATIVE_HEIGHT))
	body.rotation.y = PI if facing_us else 0.0
	holder.set_meta("player", player)
	return holder


## Where the weapons live (Kenney Blaster Kit, CC0, committed: it is free).
const WEAPON_DIR := "res://assets/weapons/"
## The model each weapon multiplier shows: pistols for ×2, a launcher (the bazooka) for ×3.
const WEAPON_MODELS := {2: "blaster-a.glb", 3: "blaster-h.glb"}
## Seconds a shown weapon takes to turn round once.
const WEAPON_TURN := 3.0


## The weapon for `multiplier`, `length` metres long, standing on y = 0 and turning slowly:
## a pair of pistols for ×2, one bazooka for ×3 or more. Null when the model is not there.
static func weapon(multiplier: float, length: float) -> Node3D:
	var file: String = WEAPON_MODELS[2 if multiplier < 3.0 else 3]
	if not ResourceLoader.exists(WEAPON_DIR + file):
		return null
	var packed: PackedScene = load(WEAPON_DIR + file)
	var holder := Node3D.new()
	var copies := 2 if multiplier < 3.0 else 1
	for i in copies:
		var body: Node3D = packed.instantiate()
		var box := bounds(body)
		var fit := length / maxf(box.size.x, box.size.z)
		body.scale = Vector3.ONE * fit
		var centre := box.get_center() * fit
		body.position = Vector3(-centre.x, -box.position.y * fit, -centre.z)
		if copies > 1:
			# Side by side and staggered along the barrel, so both show from any side.
			var along := Vector3(box.size.x, 0, 0) if box.size.x >= box.size.z else Vector3(0, 0, box.size.z)
			var across := Vector3(0, 0, box.size.z) if box.size.x >= box.size.z else Vector3(box.size.x, 0, 0)
			body.position += (across * 1.3 + along * 0.3) * fit * (i - 0.5)
		holder.add_child(body)
	holder.ready.connect(func() -> void:
		var spin := holder.create_tween().set_loops()
		spin.tween_property(holder, "rotation:y", TAU, WEAPON_TURN).as_relative())
	return holder


## The box round every mesh under `root`, in `root`'s own space.
static func bounds(root: Node3D) -> AABB:
	var box := AABB()
	var first := true
	for part: MeshInstance3D in root.find_children("*", "MeshInstance3D", true, false):
		var to_root := Transform3D.IDENTITY
		var node: Node = part
		while node != root:
			to_root = (node as Node3D).transform * to_root
			node = node.get_parent()
		var piece := to_root * part.get_aabb()
		box = piece if first else box.merge(piece)
		first = false
	return box
