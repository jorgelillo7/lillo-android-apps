## Summandos' colour themes: every colour the scene uses, by name. "sunset" is the game's look,
## "night" the dark mode; `named()` falls back to "sunset" for an unknown name.
class_name SummandosPalette
extends RefCounted

const NAMES := ["sunset", "night"]

const THEMES := {
	"sunset": {
		"sky_top": Color("#FF8F6B"), "sky": Color("#FFD3A8"), "ambient": Color("#FFE2CC"),
		"track": Color("#E2C29C"), "track_stripe": Color("#C9A57C"), "edge": Color("#7A4E3A"),
		"squad": Color("#13A89E"), "squad_dark": Color("#0B5E58"), "skin": Color("#F2C9A0"),
		"enemy": Color("#7B3FD1"), "enemy_dark": Color("#3E1C73"), "enemy_skin": Color("#C8A2F0"),
		"boss": Color("#9B2FD1"), "boss_belly": Color("#F2A6E0"), "boss_horns": Color("#FFF1D6"),
		"good_gate": Color(0.07, 0.66, 0.62, 0.45), "bad_gate": Color(0.48, 0.25, 0.82, 0.45),
		"good_floor": Color("#3CC2B5"), "bad_floor": Color("#9C6BE0"),
		"neutral_gate": Color(1.0, 0.82, 0.25, 0.4), "neutral_floor": Color("#E8B04A"),
		"weapon": Color("#FFD23F"), "bullet": Color("#FFF3B0"), "outline": Color("#3A1C0E"),
		"hurt": Color("#FF3B30"),
		"hill_far": Color("#F29A6E"), "hill_near": Color("#D9785A"), "cloud": Color("#FFF4E6"),
	},
	"night": {
		"sky_top": Color("#070A1A"), "sky": Color("#26305E"), "ambient": Color("#6A74A8"),
		"track": Color("#2A3154"), "track_stripe": Color("#222846"), "edge": Color("#00E0FF"),
		"squad": Color("#22D3EE"), "squad_dark": Color("#0E7490"), "skin": Color("#F2C9A0"),
		"enemy": Color("#FF3D8B"), "enemy_dark": Color("#8A1047"), "enemy_skin": Color("#B8F35C"),
		"boss": Color("#E11D74"), "boss_belly": Color("#FFB3D1"), "boss_horns": Color("#B8F35C"),
		"good_gate": Color(0.13, 0.83, 0.93, 0.4), "bad_gate": Color(1.0, 0.24, 0.55, 0.4),
		"good_floor": Color("#1592A8"), "bad_floor": Color("#B0245E"),
		"neutral_gate": Color(1.0, 0.84, 0.04, 0.35), "neutral_floor": Color("#8A6A12"),
		"weapon": Color("#FFD60A"), "bullet": Color("#FFFFFF"), "outline": Color("#000000"),
		"hurt": Color("#FF5C5C"),
		"hill_far": Color("#2C3566"), "hill_near": Color("#1B2148"), "cloud": Color("#3B4580"),
	},
}


## Squad colours the player can pick: [squad, helmets/badge]. Each one was checked to stay apart
## from both palettes' enemies, boss and track with every kind of colour vision (tests).
const SQUADS := {
	"default": [],
	"sky": [Color("#38BDF8"), Color("#0B6E99")],
	"orange": [Color("#FF8A1F"), Color("#A34E05")],
	"lime": [Color("#A3E635"), Color("#4D7C0F")],
	"white": [Color("#F2F2F7"), Color("#8A8AA0")],
}
## Boss colours, one per boss in turn: each stays apart from every squad colour and both tracks
## with every kind of colour vision (tests).
const BOSS_COLOURS := [Color("#9B2FD1"), Color("#DC2626"), Color("#5B3FE0"), Color("#A21CAF"),
	Color("#9F1239"), Color("#C026D3")]
## The smallest colour difference (ΔE, any vision) a squad keeps from enemies, boss and track.
const MIN_DIFFERENCE := 19.0


## Theme `name` with the squad in `squad` (one of SQUADS; "default" keeps the theme's own).
static func named(name: String, squad := "default") -> Dictionary:
	var theme: Dictionary = THEMES.get(name, THEMES[NAMES[0]]).duplicate()
	var pick: Array = SQUADS.get(squad, [])
	if not pick.is_empty():
		theme.squad = pick[0]
		theme.squad_dark = pick[1]
	return theme


## The swatch shown for each squad choice under theme `name`.
static func swatches(name: String) -> Dictionary:
	var result := {}
	for squad in SQUADS:
		result[squad] = named(name, squad).squad
	return result
