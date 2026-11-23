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
