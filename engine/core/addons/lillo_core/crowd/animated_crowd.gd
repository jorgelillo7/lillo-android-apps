## A crowd that looks alive: one LilloCrowdView per pose (from LilloPoseBaker), each member
## stepping through the poses with its own offset. Same calls as LilloCrowdView.
class_name LilloAnimatedCrowd
extends Node3D

var poses: Array[LilloCrowdView] = []
var fps := 8.0
var _time := 0.0


## `material` overrides the meshes' own (e.g. LilloFigure.material()); null keeps theirs.
func _init(frames: Array[Mesh], capacity: int, frames_per_second := 8.0, material: Material = null) -> void:
	fps = frames_per_second
	for mesh in frames:
		var view := LilloCrowdView.new(mesh, material, capacity)
		poses.append(view)
		add_child(view)


func _process(delta: float) -> void:
	_time += delta


func capacity() -> int:
	return poses[0].capacity()


## Members at `origin + offsets[i]`, facing -z (or +z when `facing_back`).
func show_at(origin: Vector3, offsets: PackedVector3Array, facing_back := false) -> void:
	var buckets := _buckets()
	var tick := int(_time * fps)
	for i in mini(offsets.size(), capacity()):
		buckets[(i + tick) % poses.size()].append(origin + offsets[i])
	for p in poses.size():
		poses[p].show_at(Vector3.ZERO, buckets[p], facing_back)


## Members at absolute positions, each scaled by `scales[i]` when given.
func show_points(points: PackedVector3Array, scales := PackedFloat32Array()) -> void:
	var buckets := _buckets()
	var bucket_scales: Array[PackedFloat32Array] = []
	for p in poses.size():
		bucket_scales.append(PackedFloat32Array())
	var tick := int(_time * fps)
	for i in mini(points.size(), capacity()):
		var p := (i + tick) % poses.size()
		buckets[p].append(points[i])
		if i < scales.size():
			bucket_scales[p].append(scales[i])
	for p in poses.size():
		poses[p].show_points(buckets[p], bucket_scales[p])


func _buckets() -> Array[PackedVector3Array]:
	var buckets: Array[PackedVector3Array] = []
	for p in poses.size():
		buckets.append(PackedVector3Array())
	return buckets
