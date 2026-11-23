extends GutTest


## Every text a generated level puts on a gate (the operation, the equation under it, a weapon's
## name) fits inside its panel at a readable size, in both languages.
func test_every_gate_text_fits_its_gate() -> void:
	var too_wide := []
	for language in ["es", "en"]:
		var texts := SummandosTexts.for_language(language)
		for mode in SummandosLevel.Mode.values():
			for number in range(1, SummandosLevel.level_count(mode) + 1):
				for row in SummandosLevel.generate(mode, number).rows:
					for side in [row.left, row.right]:
						var lines: Array[String] = []
						if side.has("gate"):
							lines.append((side.gate as LilloGate).label())
							if side.gate.note != "":
								lines.append(side.gate.note)
						else:
							lines.append("%s ×%d" % [texts.weapons[int(side.weapon)], int(side.weapon)])
						for line in lines:
							var size := SummandosWorld.fit_font_size(line, 200, SummandosWorld.GATE_TEXT_WIDTH)
							if SummandosWorld.text_width(line, size) > SummandosWorld.GATE_TEXT_WIDTH:
								too_wide.append("%s (mode %d level %d)" % [line, mode, number])
	assert_eq(too_wide, [], "gate texts wider than their gate")


func test_a_long_equation_shrinks_and_a_short_one_stays_big() -> void:
	var width := SummandosWorld.GATE_TEXT_WIDTH
	assert_lt(SummandosWorld.fit_font_size("+x  3x+4=x+18", 200, width), SummandosWorld.fit_font_size("+5", 200, width))
	assert_eq(SummandosWorld.fit_font_size("+5", 200, width), 200)
