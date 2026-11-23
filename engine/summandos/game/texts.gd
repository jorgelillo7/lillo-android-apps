## Every word the game shows, in English and Spanish; the phone's language picks.
class_name SummandosTexts
extends RefCounted


static func for_language(language: String) -> Dictionary:
	if language == "es":
		return {
			"title": "Sumandos", "stress": "Prueba de rendimiento (1.000)",
			"night_on": "Modo noche: sí", "night_off": "Modo noche: no",
			"won": "¡Victoria!", "lost": "Derrota", "again": "Otra vez", "menu": "Niveles",
			"kills": "Bajas", "weapon": "ARMA", "soldiers": "Soldados",
			"detail": "%d soldados en pie · %d bajas enemigas",
			"modes": ["Fácil", "Normal", "Adulto"], "next": "Siguiente nivel", "level": "Nivel %d",
			"think": "¡Piensa! %d", "think_hint": "Elige puerta y suelta el dedo",
			"about": "Acerca de", "close": "Cerrar", "version": "Versión %s",
			"colour": "Color del pelotón", "colour_title": "Elige el color de tu pelotón",
			"credits": [
				"Personajes 3D: KayKit de Kay Lousberg (kaylousberg.com), CC0",
				"Tipografía: Luckiest Guy de Astigmatic, licencia Apache 2.0",
				"Hecho con Godot Engine (godotengine.org), licencia MIT",
				"Sin anuncios, sin compras, sin datos que salgan del móvil",
			],
		}
	return {
		"title": "Summandos", "stress": "Performance test (1,000)",
		"night_on": "Night mode: on", "night_off": "Night mode: off",
		"won": "Victory!", "lost": "Defeat", "again": "Again", "menu": "Levels",
		"kills": "Kills", "weapon": "WEAPON", "soldiers": "Soldiers",
		"detail": "%d soldiers standing · %d enemies down",
		"modes": ["Easy", "Normal", "Grown-up"], "next": "Next level", "level": "Level %d",
		"think": "Think! %d", "think_hint": "Pick a gate and lift your finger",
		"about": "About", "close": "Close", "version": "Version %s",
		"colour": "Squad colour", "colour_title": "Pick your squad's colour",
		"credits": [
			"3D characters: KayKit by Kay Lousberg (kaylousberg.com), CC0",
			"Type: Luckiest Guy by Astigmatic, Apache License 2.0",
			"Made with Godot Engine (godotengine.org), MIT licence",
			"No ads, no purchases, no data leaves the phone",
		],
	}
