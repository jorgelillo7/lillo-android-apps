## Builds a small stylised character as one mesh with vertex colours, so a LilloCrowdView draws a
## whole two- or three-tone crowd in one call. Stands on y = 0 and faces -z.
class_name LilloFigure
extends RefCounted


## A figure `height` metres tall: a rounded body in `body`, a head in `head` and, when `cap` has
## alpha, a helmet in `cap`. `eyes` adds two dark dots on the face.
static func build(height: float, body: Color, head: Color, cap := Color(0, 0, 0, 0), eyes := true) -> ArrayMesh:
	var tool := SurfaceTool.new()
	tool.begin(Mesh.PRIMITIVE_TRIANGLES)
	var body_radius := height * 0.24
	var body_height := height * 0.62
	var head_radius := height * 0.2
	_append(tool, _capsule(body_radius, body_height), Vector3(0, body_height * 0.5, 0), body)
	var head_centre := Vector3(0, body_height + head_radius * 0.75, 0)
	_append(tool, _sphere(head_radius), head_centre, head)
	if cap.a > 0.0:
		var helmet := _sphere(head_radius * 1.12, true)
		_append(tool, helmet, head_centre + Vector3(0, head_radius * 0.15, 0), cap)
	if eyes:
		for side in [-1.0, 1.0]:
			var eye := _sphere(head_radius * 0.16)
			var at := head_centre + Vector3(side * head_radius * 0.38, 0.0, -head_radius * 0.9)
			_append(tool, eye, at, Color(0.08, 0.08, 0.12))
	tool.generate_normals()
	return tool.commit()


## A big round monster `height` metres tall for bosses: a belly, two horns, wide eyes and a mouth.
static func monster(height: float, body: Color, belly: Color, horns: Color) -> ArrayMesh:
	var tool := SurfaceTool.new()
	tool.begin(Mesh.PRIMITIVE_TRIANGLES)
	var radius := height * 0.42
	var centre := Vector3(0, radius, 0)
	_append(tool, _sphere(radius), centre, body)
	_append(tool, _sphere(radius * 0.7), centre + Vector3(0, -radius * 0.2, -radius * 0.42), belly)
	for side in [-1.0, 1.0]:
		var horn := _cone(radius * 0.22, radius * 0.6)
		_append(tool, horn, centre + Vector3(side * radius * 0.55, radius * 0.95, 0), horns)
		var eye_at := centre + Vector3(side * radius * 0.34, radius * 0.35, -radius * 0.82)
		_append(tool, _sphere(radius * 0.2), eye_at, Color.WHITE)
		_append(tool, _sphere(radius * 0.09), eye_at + Vector3(0, 0, -radius * 0.15), Color(0.05, 0.05, 0.08))
		var foot := _sphere(radius * 0.28)
		_append(tool, foot, Vector3(side * radius * 0.5, radius * 0.28, -radius * 0.1), horns)
	var mouth := _sphere(radius * 0.3, true)
	_append(tool, mouth, centre + Vector3(0, -radius * 0.05, -radius * 0.9), Color(0.25, 0.02, 0.06), true)
	tool.generate_normals()
	return tool.commit()


## A thin streak along +z, `length` long, for bullets and sparks.
static func streak(length: float, radius: float, colour: Color) -> ArrayMesh:
	var tool := SurfaceTool.new()
	tool.begin(Mesh.PRIMITIVE_TRIANGLES)
	var mesh := CapsuleMesh.new()
	mesh.radius = radius
	mesh.height = length
	mesh.radial_segments = 6
	mesh.rings = 2
	_append(tool, mesh.get_mesh_arrays(), Vector3.ZERO, colour, false, Basis(Vector3.RIGHT, PI * 0.5))
	tool.generate_normals()
	return tool.commit()


## A cartoon material for LilloFigure meshes: vertex colours, toon light bands, a rim light and,
## when `outline` has alpha, a dark outline drawn as an inverted shell.
static func material(outline := Color(0, 0, 0, 0)) -> StandardMaterial3D:
	var result := StandardMaterial3D.new()
	result.vertex_color_use_as_albedo = true
	result.diffuse_mode = BaseMaterial3D.DIFFUSE_TOON
	result.specular_mode = BaseMaterial3D.SPECULAR_TOON
	result.roughness = 0.6
	result.rim_enabled = true
	result.rim = 0.35
	result.rim_tint = 0.6
	if outline.a > 0.0:
		var shell := StandardMaterial3D.new()
		shell.shading_mode = BaseMaterial3D.SHADING_MODE_UNSHADED
		shell.albedo_color = outline
		shell.cull_mode = BaseMaterial3D.CULL_FRONT
		shell.grow = true
		shell.grow_amount = 0.025
		result.next_pass = shell
	return result


static func _capsule(radius: float, height: float) -> Array:
	var mesh := CapsuleMesh.new()
	mesh.radius = radius
	mesh.height = height
	mesh.radial_segments = 12
	mesh.rings = 4
	return mesh.get_mesh_arrays()


static func _sphere(radius: float, hemisphere := false) -> Array:
	var mesh := SphereMesh.new()
	mesh.radius = radius
	mesh.height = radius * (1.0 if hemisphere else 2.0)
	mesh.is_hemisphere = hemisphere
	mesh.radial_segments = 12
	mesh.rings = 6
	return mesh.get_mesh_arrays()


static func _cone(radius: float, height: float) -> Array:
	var mesh := CylinderMesh.new()
	mesh.top_radius = 0.0
	mesh.bottom_radius = radius
	mesh.height = height
	mesh.radial_segments = 10
	mesh.rings = 1
	return mesh.get_mesh_arrays()


## Adds a primitive's triangles, rotated by `basis`, squashed flat in z when `flat`.
static func _append(tool: SurfaceTool, arrays: Array, offset: Vector3, colour: Color, flat := false,
		basis := Basis.IDENTITY) -> void:
	var vertices: PackedVector3Array = arrays[Mesh.ARRAY_VERTEX]
	var indices: PackedInt32Array = arrays[Mesh.ARRAY_INDEX]
	var squash := Vector3(1.0, 0.6, 0.35) if flat else Vector3.ONE
	for index in indices:
		tool.set_color(colour)
		tool.add_vertex(basis * (vertices[index] * squash) + offset)
