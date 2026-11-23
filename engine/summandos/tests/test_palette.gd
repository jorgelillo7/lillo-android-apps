extends GutTest


func test_every_squad_colour_stays_apart_from_enemies_and_track_for_everyone() -> void:
	for theme in SummandosPalette.NAMES:
		for squad in SummandosPalette.SQUADS:
			var colours := SummandosPalette.named(theme, squad)
			for other in ["enemy", "boss", "track"]:
				var worst := LilloColours.worst_difference(colours.squad, colours[other])
				assert_gt(worst, SummandosPalette.MIN_DIFFERENCE, "%s squad in %s against %s: ΔE %.1f" % [
					squad, theme, other, worst])


func test_picking_a_squad_colour_changes_only_the_squad() -> void:
	var plain := SummandosPalette.named("sunset")
	var orange := SummandosPalette.named("sunset", "orange")
	assert_ne(orange.squad, plain.squad)
	assert_eq(orange.enemy, plain.enemy)
	assert_eq(SummandosPalette.named("sunset", "nonsense").squad, plain.squad)


func test_boss_colours_stay_apart_from_every_squad_and_track() -> void:
	for theme in SummandosPalette.NAMES:
		for squad in SummandosPalette.SQUADS:
			var colours := SummandosPalette.named(theme, squad)
			for boss in SummandosPalette.BOSS_COLOURS:
				for other in [colours.squad, colours.track]:
					var worst := LilloColours.worst_difference(boss, other)
					assert_gt(worst, SummandosPalette.MIN_DIFFERENCE, "boss %s against %s in %s: ΔE %.1f" % [
						boss.to_html(false), other.to_html(false), theme, worst])


func test_no_two_bosses_in_a_mode_look_alike_and_each_is_bigger() -> void:
	for mode in SummandosLevel.Mode.values():
		var seen := {}
		var last_height := 0.0
		for number in range(5, SummandosLevel.LEVELS_PER_MODE + 1, 5):
			var look := SummandosLevel.boss_look(mode, number)
			var key := "%s/%d" % [look.model, look.colour]
			assert_false(seen.has(key), "mode %d level %d repeats %s" % [mode, number, key])
			seen[key] = true
			assert_gt(look.height, last_height)
			last_height = look.height
			assert_eq(SummandosLevel.generate(mode, number).boss, look, "the level carries its boss")
