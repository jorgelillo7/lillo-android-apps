extends GutTest


func test_a_colour_does_not_differ_from_itself() -> void:
	assert_almost_eq(LilloColours.difference(Color.TEAL, Color.TEAL), 0.0, 0.001)


func test_black_and_white_are_far_apart_for_everyone() -> void:
	assert_gt(LilloColours.worst_difference(Color.BLACK, Color.WHITE), 90.0)


func test_red_and_green_merge_for_red_green_blindness() -> void:
	var red := Color("#D0312D")
	var green := Color("#5E9E2C")
	assert_gt(LilloColours.difference(red, green), 50.0, "clearly apart with normal vision")
	assert_lt(LilloColours.difference(red, green, "deutan"), LilloColours.difference(red, green) * 0.5,
		"much closer for deuteranopia")
