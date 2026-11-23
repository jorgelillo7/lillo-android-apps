## A crowd that looks alive: for each clip (running, shooting...) one LilloCrowdView per pose
## from LilloPoseBaker, each member stepping through the poses with its own offset. Draws the clip
## last chosen with play(). Same drawing calls as LilloCrowdView.
class_name LilloAnimatedCrowd
extends Node3D

## clip name -> Array of LilloCrowdView, one per pose.
var clips := {}
var current := "default"
var fps := 8.0
var _capacity := 0
var _material: Material
var _time := 0.0


## `material` overrides the meshes' own (e.g. LilloFigure.material()); null keeps theirs.
func _init(frames: Array[Mesh], capacity: int, frames_per_second := 8.0, material: Material = null) -> void:
	fps = frames_per_second
	_capacity = capacity
	_material = material
	add_clip("default", frames)


## Another set of poses, drawn after play(`name`).
func add_clip(name: String, frames: Array[Mesh], material: Material = null) -> void:
	var views: Array[LilloCrowdView] = []
	for mesh in frames:
		var view := LilloCrowdView.new(mesh, material if material else _material, _capacity)
		views.append(view)
		add_child(view)
	clips[name] = views


## Switches every member to clip `name` (ignored when there is no such clip).
func play(name: String) -> void:
	if clips.has(name) and name != current:
		for view in clips[current]:
			view.multimesh.visible_instance_count = 0
		current = name


func poses() -> Array:
	return clips[current]


func _process(delta: float) -> void:
	_time += delta


func capacity() -> int:
	return _capacity


## Members at `origin + offsets[i]`, facing -z (or +z when `facing_back`).
func show_at(origin: Vector3, offsets: PackedVector3Array, facing_back := false) -> void:
	var views := poses()
	var buckets := _buckets(views.size())
	var tick := int(_time * fps)
	for i in mini(offsets.size(), _capacity):
		buckets[(i + tick) % views.size()].append(origin + offsets[i])
	for p in views.size():
		views[p].show_at(Vector3.ZERO, buckets[p], facing_back)


## Members at absolute positions, each scaled by `scales[i]` when given.
func show_points(points: PackedVector3Array, scales := PackedFloat32Array()) -> void:
	var views := poses()
	var buckets := _buckets(views.size())
	var bucket_scales: Array[PackedFloat32Array] = []
	for p in views.size():
		bucket_scales.append(PackedFloat32Array())
	var tick := int(_time * fps)
	for i in mini(points.size(), _capacity):
		var p := (i + tick) % views.size()
		buckets[p].append(points[i])
		if i < scales.size():
			bucket_scales[p].append(scales[i])
	for p in views.size():
		views[p].show_points(buckets[p], bucket_scales[p])


func _buckets(count: int) -> Array[PackedVector3Array]:
	var buckets: Array[PackedVector3Array] = []
	for p in count:
		buckets.append(PackedVector3Array())
	return buckets
