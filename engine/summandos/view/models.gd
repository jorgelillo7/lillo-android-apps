## Loads Summandos' third-party models (assets/models, fetched by scripts/engine-assets.sh) and
## builds animated characters from them in a team colour. Every call returns null without the
## models, so callers fall back to the game's own figures.
class_name SummandosModels
extends RefCounted

const DIR := "res://assets/models/"
## Native height of a KayKit character, in metres, before scaling.
const NATIVE_HEIGHT := 2.4
## Spare kit on the crossbowman model that the squad does not carry.
const ARCHER_SPARES := ["Knife_Offhand", "1H_Crossbow", "Knife", "Throwable"]


static func scene(file: String) -> PackedScene:
	return load(DIR + file) if ResourceLoader.exists(DIR + file) else null


## The model's texture repainted in `colour` (LilloMaterials.team).
static func team(mesh: Mesh, colour: Color) -> Material:
	var own: StandardMaterial3D = mesh.surface_get_material(0)
	return LilloMaterials.team(own.albedo_texture, colour)


## One character from `file` playing `animation` in a loop, `height` tall, painted `colour`;
## `facing_us` turns it to look down -z. Null when the model is not there.
static func character(file: String, animation: String, height: float, colour: Color, facing_us := false,
		spares: Array = []) -> Node3D:
	var packed := scene(file)
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
	if player.has_animation(animation):
		player.play(animation)
	body.scale = Vector3.ONE * (height / NATIVE_HEIGHT)
	body.rotation.y = PI if facing_us else 0.0
	holder.set_meta("player", player)
	return holder
