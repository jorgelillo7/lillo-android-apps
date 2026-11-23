extends GutTest


func test_each_operation_changes_the_crowd() -> void:
	assert_eq(LilloGate.new(LilloGate.Op.ADD, 20).apply(10), 30)
	assert_eq(LilloGate.new(LilloGate.Op.SUBTRACT, 4).apply(10), 6)
	assert_eq(LilloGate.new(LilloGate.Op.MULTIPLY, 3).apply(10), 30)
	assert_eq(LilloGate.new(LilloGate.Op.DIVIDE, 4).apply(10), 2)
	assert_eq(LilloGate.new(LilloGate.Op.PERCENT, 50).apply(9), 4)


func test_the_crowd_never_goes_below_zero() -> void:
	assert_eq(LilloGate.new(LilloGate.Op.SUBTRACT, 15).apply(10), 0)


func test_dividing_by_zero_leaves_the_crowd_as_it_was() -> void:
	assert_eq(LilloGate.new(LilloGate.Op.DIVIDE, 0).apply(10), 10)


func test_the_better_gate_depends_on_the_crowd() -> void:
	var times_three := LilloGate.new(LilloGate.Op.MULTIPLY, 3)
	var plus_thirty := LilloGate.new(LilloGate.Op.ADD, 30)
	assert_eq(LilloGate.better(times_three, plus_thirty, 20), 0, "×3 gives 60, +30 gives 50")
	assert_eq(LilloGate.better(times_three, plus_thirty, 10), 1, "×3 gives 30, +30 gives 40")
	assert_eq(LilloGate.better(times_three, plus_thirty, 15), -1, "both give 45")


func test_labels_read_like_the_gates_on_screen() -> void:
	assert_eq(LilloGate.new(LilloGate.Op.MULTIPLY, 2).label(), "×2")
	assert_eq(LilloGate.new(LilloGate.Op.SUBTRACT, 15).label(), "−15")
	assert_eq(LilloGate.new(LilloGate.Op.PERCENT, 50).label(), "50 %")
