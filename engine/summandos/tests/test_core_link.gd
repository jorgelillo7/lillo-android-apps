extends GutTest
## The shared addon reaches this game through addons/lillo_core.


func test_the_shared_gate_is_available() -> void:
	assert_eq(LilloGate.new(LilloGate.Op.MULTIPLY, 2).apply(21), 42)
