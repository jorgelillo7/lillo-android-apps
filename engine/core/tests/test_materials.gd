extends GutTest


func test_flat_materials_are_plain_see_through_or_glowing() -> void:
	assert_eq(LilloMaterials.flat(Color.RED).albedo_color, Color.RED)
	assert_eq(LilloMaterials.flat(Color.RED, true).transparency, BaseMaterial3D.TRANSPARENCY_ALPHA)
	assert_true(LilloMaterials.flat(Color.RED, false, true).emission_enabled)


func test_shader_materials_carry_their_colours() -> void:
	var bands := LilloMaterials.bands(Color.WHITE, Color.GRAY, 6.0)
	assert_eq(bands.get_shader_parameter("period"), 6.0)
	assert_eq(LilloMaterials.badge(Color.BLUE).get_shader_parameter("colour"), Color.BLUE)
