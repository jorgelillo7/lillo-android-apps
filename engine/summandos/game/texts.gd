## Every word the game shows, in English and Spanish; the phone's language picks.
class_name SummandosTexts
extends RefCounted


static func for_language(language: String) -> Dictionary:
	if language == "es":
		return {
			"title": "Sumandos", "stress": "Prueba de rendimiento",
			"night_on": "Modo noche: sí", "night_off": "Modo noche: no",
			"won": "¡Victoria!", "lost": "Derrota", "again": "Otra vez", "menu": "Niveles",
			"kills": "Bajas", "weapon": "ARMA", "soldiers": "Soldados",
			"detail": "%d soldados en pie · %d bajas enemigas",
			"modes": ["Fácil", "Normal", "Adulto", "Experto"], "next": "Siguiente nivel", "level": "Nivel %d",
			"wrong_gate": "¡Puerta equivocada! %s dejaba %d soldados y %s dejaba %d.",
			"think": "¡Piensa! %d", "think_hint": "Elige puerta y suelta el dedo",
			"settings": "Ajustes", "about": "Acerca de", "close": "Cerrar", "version": "Versión %s",
			"sound_on": "Sonido: sí", "sound_off": "Sonido: no", "music_on": "Música: sí",
			"music_off": "Música: no", "vibration_on": "Vibración: sí", "vibration_off": "Vibración: no",
			"colour": "Color del pelotón", "colour_title": "Elige el color de tu pelotón",
			"credits": [
				{"what": "Personajes 3D", "who": "KayKit, de Kay Lousberg", "licence": "CC0", "link": "kaylousberg.com"},
				{"what": "Tipografía", "who": "Luckiest Guy (Astigmatic) y Fredoka", "licence": "Apache 2.0 y OFL", "link": "fonts.google.com"},
				{"what": "Motor", "who": "Godot Engine", "licence": "Licencia MIT", "link": "godotengine.org"},
			],
			"golem_credit": {"what": "Golem y Nigromante", "who": "KayKit Skeletons EXTRA, comprado a su autor",
				"licence": "CC0", "link": "kaylousberg.itch.io/kaykit-skeletons"},
			"about_note": "Sin anuncios, sin compras y sin datos que salgan del móvil.",
		}
	return {
		"title": "Summandos", "stress": "Performance test",
		"night_on": "Night mode: on", "night_off": "Night mode: off",
		"won": "Victory!", "lost": "Defeat", "again": "Again", "menu": "Levels",
		"kills": "Kills", "weapon": "WEAPON", "soldiers": "Soldiers",
		"detail": "%d soldiers standing · %d enemies down",
		"modes": ["Easy", "Normal", "Grown-up", "Expert"], "next": "Next level", "level": "Level %d",
		"wrong_gate": "Wrong gate! %s left %d soldiers and %s left %d.",
		"think": "Think! %d", "think_hint": "Pick a gate and lift your finger",
		"settings": "Settings", "about": "About", "close": "Close", "version": "Version %s",
		"sound_on": "Sound: on", "sound_off": "Sound: off", "music_on": "Music: on",
		"music_off": "Music: off", "vibration_on": "Vibration: on", "vibration_off": "Vibration: off",
		"colour": "Squad colour", "colour_title": "Pick your squad's colour",
		"credits": [
			{"what": "3D characters", "who": "KayKit by Kay Lousberg", "licence": "CC0", "link": "kaylousberg.com"},
			{"what": "Type", "who": "Luckiest Guy (Astigmatic) and Fredoka", "licence": "Apache 2.0 and OFL", "link": "fonts.google.com"},
			{"what": "Engine", "who": "Godot Engine", "licence": "MIT licence", "link": "godotengine.org"},
		],
		"golem_credit": {"what": "Golem and Necromancer", "who": "KayKit Skeletons EXTRA, bought from its author",
			"licence": "CC0", "link": "kaylousberg.itch.io/kaykit-skeletons"},
		"about_note": "No ads, no purchases, no data leaves the phone.",
	}
