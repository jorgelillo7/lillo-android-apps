## Draws up to `capacity` copies of one mesh in a single draw call, for crowds, bullets or any
## swarm. Positions are set every frame; only the first `visible` instances are drawn.
class_name LilloCrowdView
extends MultiMeshInstance3D


func _init(mesh: Mesh, material: Material, capacity: int) -> void:
	var instances := MultiMesh.new()
	instances.transform_format = MultiMesh.TRANSFORM_3D
	instances.mesh = mesh
	instances.instance_count = capacity
	instances.visible_instance_count = 0
	multimesh = instances
	material_override = material
	cast_shadow = GeometryInstance3D.SHADOW_CASTING_SETTING_OFF


func capacity() -> int:
	return multimesh.instance_count


## Places members at `origin + offsets[i]` (facing -z, or +z when `facing_back`); extra offsets
## beyond the capacity are not drawn.
func show_at(origin: Vector3, offsets: PackedVector3Array, facing_back := false) -> void:
	var shown := mini(offsets.size(), multimesh.instance_count)
	var basis := Basis(Vector3.UP, PI) if facing_back else Basis.IDENTITY
	for i in shown:
		multimesh.set_instance_transform(i, Transform3D(basis, origin + offsets[i]))
	multimesh.visible_instance_count = shown


## Places members at absolute positions.
func show_points(points: PackedVector3Array) -> void:
	var shown := mini(points.size(), multimesh.instance_count)
	for i in shown:
		multimesh.set_instance_transform(i, Transform3D(Basis.IDENTITY, points[i]))
	multimesh.visible_instance_count = shown
