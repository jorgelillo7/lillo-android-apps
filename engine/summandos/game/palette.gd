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
		"weapon": Color("#FFD23F"), "bullet": Color("#FFF3B0"), "outline": Color("#3A1C0E"),
	},
	"night": {
		"sky_top": Color("#070A1A"), "sky": Color("#26305E"), "ambient": Color("#6A74A8"),
		"track": Color("#2A3154"), "track_stripe": Color("#222846"), "edge": Color("#00E0FF"),
		"squad": Color("#22D3EE"), "squad_dark": Color("#0E7490"), "skin": Color("#F2C9A0"),
		"enemy": Color("#FF3D8B"), "enemy_dark": Color("#8A1047"), "enemy_skin": Color("#B8F35C"),
		"boss": Color("#E11D74"), "boss_belly": Color("#FFB3D1"), "boss_horns": Color("#B8F35C"),
		"good_gate": Color(0.13, 0.83, 0.93, 0.4), "bad_gate": Color(1.0, 0.24, 0.55, 0.4),
		"good_floor": Color("#1592A8"), "bad_floor": Color("#B0245E"),
		"weapon": Color("#FFD60A"), "bullet": Color("#FFFFFF"), "outline": Color("#000000"),
	},
}


static func named(name: String) -> Dictionary:
	return THEMES.get(name, THEMES[NAMES[0]])
