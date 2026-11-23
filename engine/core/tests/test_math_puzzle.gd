extends GutTest


func _random(seed: int) -> RandomNumberGenerator:
	var random := RandomNumberGenerator.new()
	random.seed = seed
	return random


## Evaluates what the puzzle writes, independently of how it was written.
func _evaluate(puzzle: Dictionary) -> int:
	var text: String = puzzle.text
	if text == "x":
		var sides: PackedStringArray = puzzle.note.split("=")
		var left := sides[0].split("x+")
		return (int(sides[1]) - int(left[1])) / int(left[0])
	if text.begins_with("√"):
		return int(sqrt(float(text.trim_prefix("√"))))
	for exponent in LilloMathPuzzle.SUPERSCRIPTS:
		var mark: String = LilloMathPuzzle.SUPERSCRIPTS[exponent]
		if text.ends_with(mark):
			return int(pow(int(text.trim_suffix(mark)), exponent))
	for symbol in ["+", "−", "×", "÷"]:
		if text.contains(symbol):
			var parts := text.split(symbol)
			var a := int(parts[0])
			var b := int(parts[1])
			match symbol:
				"+": return a + b
				"−": return a - b
				"×": return a * b
				"÷": return a / b
	return int(text)


func test_tier_zero_is_the_plain_number() -> void:
	assert_eq(LilloMathPuzzle.express(25, 0, _random(1)).text, "25")


func test_every_puzzle_equals_its_value() -> void:
	var random := _random(42)
	for tier in [1, 2]:
		for value in range(2, 61):
			var puzzle := LilloMathPuzzle.express(value, tier, random)
			assert_eq(_evaluate(puzzle), value, "tier %d: %s %s" % [tier, puzzle.text, puzzle.note])


func test_adult_puzzles_are_worth_a_pause_and_easier_ones_are_not() -> void:
	assert_true(LilloMathPuzzle.express(32, 2, _random(3)).hard)
	assert_false(LilloMathPuzzle.express(32, 1, _random(3)).hard)


func test_powers_are_found() -> void:
	assert_eq(LilloMathPuzzle._power(32), "2⁵")
	assert_eq(LilloMathPuzzle._power(49), "7²")
	assert_eq(LilloMathPuzzle._power(30), "")


func test_a_gate_shows_its_puzzle() -> void:
	var gate := LilloGate.new(LilloGate.Op.MULTIPLY, 7)
	gate.shown = "√49"
	assert_eq(gate.label(), "×√49")
	gate.shown = "3+4"
	assert_eq(gate.label(), "×(3+4)")
	gate.shown = "x"
	assert_eq(gate.label(), "×x")
