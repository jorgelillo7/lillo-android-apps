## Turns an animated character (a glTF scene with a skeleton) into a few still meshes along one
## animation, so a crowd of hundreds can be drawn as instances and still look like it moves.
## Each frame is one mesh, scaled to `height`, facing -z like LilloFigure, and simplified to about
## `detail` of its triangles.
class_name LilloPoseBaker
extends RefCounted


## `frames` meshes evenly spread over `animation`. `skip` names mesh nodes to leave out (spare
## weapons, shields). `host` must be in the scene tree: the character is posed there and removed.
static func bake(host: Node, scene: PackedScene, animation: String, frames: int, height: float,
		skip: PackedStringArray = PackedStringArray(), detail := 0.3) -> Array[Mesh]:
	var root: Node3D = scene.instantiate()
	host.add_child(root)
	var player: AnimationPlayer = root.find_children("*", "AnimationPlayer", true, false)[0]
	var skeleton: Skeleton3D = root.find_children("*", "Skeleton3D", true, false)[0]
	var parts: Array[MeshInstance3D] = []
	for node in root.find_children("*", "MeshInstance3D", true, false):
		if not skip.has(node.name):
			parts.append(node)
	var clip := player.get_animation(animation)
	player.play(animation)
	var raw: Array[ArrayMesh] = []
	for frame in frames:
		player.seek(clip.length * frame / float(frames), true)
		skeleton.force_update_all_bone_transforms()
		raw.append(_merge(root, parts))
	root.queue_free()
	var box := raw[0].get_aabb()
	var k := height / maxf(0.001, box.size.y)
	var fit := Transform3D(Basis(Vector3.UP, PI).scaled(Vector3.ONE * k), Vector3(0, -box.position.y * k, 0))
	var result: Array[Mesh] = []
	for mesh in raw:
		result.append(_simplify(_transformed(mesh, fit), detail))
	return result


## One mesh of every part in its current pose, one surface per material.
static func _merge(root: Node3D, parts: Array[MeshInstance3D]) -> ArrayMesh:
	var tools := {}
	for part in parts:
		var posed: Mesh = part.mesh
		if part.skin != null:
			posed = part.bake_mesh_from_current_skeleton_pose()
		var place := root.global_transform.affine_inverse() * part.global_transform
		for surface in posed.get_surface_count():
			var material := part.get_active_material(surface)
			if not tools.has(material):
				var tool := SurfaceTool.new()
				tool.begin(Mesh.PRIMITIVE_TRIANGLES)
				tools[material] = tool
			tools[material].append_from(posed, surface, place)
	var mesh := ArrayMesh.new()
	for material in tools:
		tools[material].commit(mesh)
		mesh.surface_set_material(mesh.get_surface_count() - 1, material)
	return mesh


static func _transformed(mesh: ArrayMesh, place: Transform3D) -> ArrayMesh:
	var result := ArrayMesh.new()
	for surface in mesh.get_surface_count():
		var tool := SurfaceTool.new()
		tool.begin(Mesh.PRIMITIVE_TRIANGLES)
		tool.append_from(mesh, surface, place)
		tool.commit(result)
		result.surface_set_material(surface, mesh.surface_get_material(surface))
	return result


## The same mesh with fewer triangles, from Godot's own LOD generator; unchanged when `detail` >= 1.
static func _simplify(mesh: ArrayMesh, detail: float) -> ArrayMesh:
	if detail >= 1.0:
		return mesh
	var importer := ImporterMesh.new()
	for surface in mesh.get_surface_count():
		importer.add_surface(Mesh.PRIMITIVE_TRIANGLES, mesh.surface_get_arrays(surface), [], {},
			mesh.surface_get_material(surface))
	importer.generate_lods(25.0, 60.0, [])
	var result := ArrayMesh.new()
	for surface in importer.get_surface_count():
		var arrays := importer.get_surface_arrays(surface)
		var full: int = (arrays[Mesh.ARRAY_INDEX] as PackedInt32Array).size()
		var best: PackedInt32Array = arrays[Mesh.ARRAY_INDEX]
		for lod in importer.get_surface_lod_count(surface):
			var indices := importer.get_surface_lod_indices(surface, lod)
			if indices.size() >= full * detail:
				best = indices
		arrays[Mesh.ARRAY_INDEX] = best
		result.add_surface_from_arrays(Mesh.PRIMITIVE_TRIANGLES, arrays)
		result.surface_set_material(surface, importer.get_surface_material(surface))
	return result
