extends GutTest


func test_a_level_made_ahead_is_the_generated_one_and_is_kept() -> void:
	var levels := SummandosLevelsAhead.new()
	levels.prepare(SummandosLevel.Mode.NORMAL, 4)
	var ahead := levels.take(SummandosLevel.Mode.NORMAL, 4)
	var made := SummandosLevel.generate(SummandosLevel.Mode.NORMAL, 4)
	assert_eq(ahead.reference, made.reference)
	assert_eq(ahead.rows.size(), made.rows.size())
	assert_same(levels.take(SummandosLevel.Mode.NORMAL, 4), ahead, "played again without generating")
	levels.prepare(SummandosLevel.Mode.EASY, SummandosLevel.LEVELS_PER_MODE + 1)
	levels.finish()
