## Materials every 3D game reuses: flat colours, banded ground and a camera-facing badge.
class_name LilloMaterials
extends RefCounted

const BANDS := preload("bands.gdshader")
const BADGE := preload("badge.gdshader")


## A plain colour; `transparent` for see-through panels, `glowing` for things that shine.
static func flat(colour: Color, transparent := false, glowing := false) -> StandardMaterial3D:
	var material := StandardMaterial3D.new()
	material.albedo_color = colour
	if transparent:
		material.transparency = BaseMaterial3D.TRANSPARENCY_ALPHA
		material.cull_mode = BaseMaterial3D.CULL_DISABLED
		material.shading_mode = BaseMaterial3D.SHADING_MODE_UNSHADED
	if glowing:
		material.emission_enabled = true
		material.emission = colour
	return material


## Ground in bands of `base` and `stripe` across z, `period` metres per pair.
static func bands(base: Color, stripe: Color, period := 4.0) -> ShaderMaterial:
	var material := ShaderMaterial.new()
	material.shader = BANDS
	material.set_shader_parameter("base", base)
	material.set_shader_parameter("stripe", stripe)
	material.set_shader_parameter("period", period)
	return material


## A rounded badge for a QuadMesh of size (2, 0.95), drawn over everything, facing the camera.
static func badge(colour: Color) -> ShaderMaterial:
	var material := ShaderMaterial.new()
	material.shader = BADGE
	material.set_shader_parameter("colour", colour)
	return material
