extends GutTest


func test_a_figure_stands_on_the_ground_at_its_height() -> void:
	var mesh := LilloFigure.build(1.0, Color.BLUE, Color.BISQUE, Color.NAVY_BLUE)
	var box := mesh.get_aabb()
	assert_almost_eq(box.position.y, 0.0, 0.01, "feet on y = 0")
	assert_between(box.end.y, 0.9, 1.15, "about as tall as asked")


func test_a_figure_carries_its_colours() -> void:
	var mesh := LilloFigure.build(1.0, Color.BLUE, Color.BISQUE)
	var colours: PackedColorArray = mesh.surface_get_arrays(0)[Mesh.ARRAY_COLOR]
	assert_true(colours.has(Color.BLUE), "body colour")
	assert_true(colours.has(Color.BISQUE), "head colour")
	assert_eq(mesh.get_surface_count(), 1, "one surface, one draw call per crowd")


func test_a_monster_is_wider_than_a_figure_of_its_height() -> void:
	var monster := LilloFigure.monster(2.0, Color.PURPLE, Color.PINK, Color.WHITE).get_aabb()
	var figure := LilloFigure.build(2.0, Color.BLUE, Color.BISQUE).get_aabb()
	assert_almost_eq(monster.position.y, 0.0, 0.05, "stands on the ground")
	assert_gt(monster.size.x, figure.size.x * 1.5)


func test_a_streak_points_along_z() -> void:
	var box := LilloFigure.streak(0.8, 0.04, Color.YELLOW).get_aabb()
	assert_almost_eq(box.size.z, 0.8, 0.01)
	assert_lt(box.size.x, 0.1)
