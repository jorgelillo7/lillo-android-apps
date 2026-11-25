extends GutTest


func test_seven_quick_taps_unlock_and_count_down_on_the_way() -> void:
	var taps := LilloSecretTaps.new(7, 1.5)
	var left: Array[int] = []
	for i in 7:
		left.append(taps.tap(i * 0.3))
	assert_eq(left, [6, 5, 4, 3, 2, 1, 0] as Array[int])
	assert_eq(taps.tap(2.2), 6, "after unlocking it counts from the start again")


func test_a_pause_starts_the_count_again() -> void:
	var taps := LilloSecretTaps.new(7, 1.5)
	for i in 5:
		taps.tap(i * 0.3)
	assert_eq(taps.tap(10.0), 6, "a tap after a long pause is the first of a new run")
