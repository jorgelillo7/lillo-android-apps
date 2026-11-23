## Every word the game shows, in English and Spanish; the phone's language picks.
class_name SummandosTexts
extends RefCounted


static func for_language(language: String) -> Dictionary:
	if language == "es":
		return {
			"title": "Sumandos", "stress": "Prueba de rendimiento",
			"night_on": "Modo noche: sí", "night_off": "Modo noche: no",
			"won": "¡Victoria!", "lost": "Derrota", "again": "Otra vez", "menu": "Niveles",
			"kills": "Bajas", "weapon": "ARMA", "weapons": {2: "PISTOLAS", 3: "BAZUCA"}, "soldiers": "Soldados",
			"detail": "%d soldados en pie · %d bajas enemigas",
			"modes": ["Recluta", "Soldado", "Capitán", "Leyenda"],
			"mode_hints": ["+  −  ×2  ÷2", "%  y  sumas sencillas", "potencias, raíces y x", "dos pasos, sin fallar"],
			"help": "Cómo se juega",
			"help_sections": [
				{"heading": "Elige puerta", "body": "Desliza el dedo para mover el pelotón y pasa por la puerta que deje más soldados. La que vas a coger se ilumina."},
				{"heading": "Apunta", "body": "Ponte delante de cada oleada: solo aciertas a lo que tienes enfrente. Los enemigos aguantan varios disparos; un arma los tumba antes."},
				{"heading": "Jefes", "body": "Cada 5 niveles, un jefe. No cae al chocar: se come soldados hasta que vacías su barra de vida. El mago y el nigromante lanzan bolas de fuego: apártate del círculo rojo."},
				{"heading": "Estrellas", "body": "Ganas de 1 a 3 estrellas según los soldados que lleguen al final. Cada victoria abre el siguiente nivel."},
				{"heading": "Recluta", "body": "Sumas, restas, ×2 y ÷2 con números a la vista. Puertas de colores: turquesa suma, morado resta."},
				{"heading": "Soldado", "body": "Porcentajes y operandos escritos como sumas pequeñas: ×(3+4), +21÷3."},
				{"heading": "Capitán", "body": "Potencias (2⁵), raíces (√49) y ecuaciones (+x con 3x+5=26). Todas las puertas doradas y 5 segundos para pensar antes de las difíciles."},
				{"heading": "Leyenda", "body": "10 niveles de dos pasos: ×(2³−5), +(√144÷3). 5 segundos en cada puerta y, si eliges la peor, pierdes."},
			], "next": "Siguiente nivel", "level": "Nivel %d",
			"wrong_gate": "¡Puerta equivocada! %s dejaba %d soldados y %s dejaba %d.",
			"think": "¡Piensa! %d", "think_hint": "Elige puerta y suelta el dedo",
			"settings": "Ajustes", "about": "Acerca de", "close": "Cerrar", "version": "Versión %s",
			"sound_on": "Sonido: sí", "sound_off": "Sonido: no", "music_on": "Música: sí",
			"music_off": "Música: no", "vibration_on": "Vibración: sí", "vibration_off": "Vibración: no",
			"colour": "Color del pelotón", "colour_title": "Elige el color de tu pelotón",
			"credits": [
				{"what": "Personajes 3D", "who": "KayKit, de Kay Lousberg", "licence": "CC0", "link": "kaylousberg.com"},
				{"what": "Armas", "who": "Blaster Kit, de Kenney", "licence": "CC0", "link": "kenney.nl"},
				{"what": "Tipografía", "who": "Luckiest Guy (Astigmatic) y Fredoka", "licence": "Apache 2.0 y OFL", "link": "fonts.google.com"},
				{"what": "Motor", "who": "Godot Engine", "licence": "Licencia MIT", "link": "godotengine.org"},
			],
			"golem_credit": {"what": "Golem y Nigromante", "who": "KayKit Skeletons EXTRA, comprado a su autor",
				"licence": "CC0", "link": "kaylousberg.itch.io/kaykit-skeletons"},
			"name_story": "¿Por qué Sumandos? Por los sumandos, los números que se suman, y por los comandos, el pelotón que los convierte en soldados.",
			"about_note": "Sin anuncios, sin compras y sin datos que salgan del móvil.",
		}
	return {
		"title": "Summandos", "stress": "Performance test",
		"night_on": "Night mode: on", "night_off": "Night mode: off",
		"won": "Victory!", "lost": "Defeat", "again": "Again", "menu": "Levels",
		"kills": "Kills", "weapon": "WEAPON", "weapons": {2: "PISTOLS", 3: "BAZOOKA"}, "soldiers": "Soldiers",
		"detail": "%d soldiers standing · %d enemies down",
		"modes": ["Recruit", "Soldier", "Captain", "Legend"],
		"mode_hints": ["+  −  ×2  ÷2", "%  and small sums", "powers, roots and x", "two steps, no mistakes"],
		"help": "How to play",
		"help_sections": [
			{"heading": "Pick a gate", "body": "Slide your finger to move the squad and go through the gate that leaves more soldiers. The one you will take lights up."},
			{"heading": "Aim", "body": "Stand in front of each wave: you only hit what is ahead of you. Enemies take several shots; a weapon brings them down sooner."},
			{"heading": "Bosses", "body": "A boss every 5 levels. It does not fall on contact: it eats soldiers until you empty its health bar. The mage and the necromancer throw fireballs: step out of the red ring."},
			{"heading": "Stars", "body": "Win 1 to 3 stars by the soldiers who make it to the end. Each win opens the next level."},
			{"heading": "Recruit", "body": "Adding, taking away, ×2 and ÷2 with plain numbers. Coloured gates: teal adds, purple takes."},
			{"heading": "Soldier", "body": "Percentages, and operands written as small sums: ×(3+4), +21÷3."},
			{"heading": "Captain", "body": "Powers (2⁵), roots (√49) and equations (+x with 3x+5=26). Every gate gold, and 5 seconds to think before the hard ones."},
			{"heading": "Legend", "body": "10 levels of two steps: ×(2³−5), +(√144÷3). 5 seconds at every gate, and the worse gate loses."},
		], "next": "Next level", "level": "Level %d",
		"wrong_gate": "Wrong gate! %s left %d soldiers and %s left %d.",
		"think": "Think! %d", "think_hint": "Pick a gate and lift your finger",
		"settings": "Settings", "about": "About", "close": "Close", "version": "Version %s",
		"sound_on": "Sound: on", "sound_off": "Sound: off", "music_on": "Music: on",
		"music_off": "Music: off", "vibration_on": "Vibration: on", "vibration_off": "Vibration: off",
		"colour": "Squad colour", "colour_title": "Pick your squad's colour",
		"credits": [
			{"what": "3D characters", "who": "KayKit by Kay Lousberg", "licence": "CC0", "link": "kaylousberg.com"},
			{"what": "Weapons", "who": "Blaster Kit by Kenney", "licence": "CC0", "link": "kenney.nl"},
			{"what": "Type", "who": "Luckiest Guy (Astigmatic) and Fredoka", "licence": "Apache 2.0 and OFL", "link": "fonts.google.com"},
			{"what": "Engine", "who": "Godot Engine", "licence": "MIT licence", "link": "godotengine.org"},
		],
		"golem_credit": {"what": "Golem and Necromancer", "who": "KayKit Skeletons EXTRA, bought from its author",
			"licence": "CC0", "link": "kaylousberg.itch.io/kaykit-skeletons"},
		"name_story": "Why Summandos? Summands, the numbers you add, and commandos, the squad that turns them into soldiers.",
		"about_note": "No ads, no purchases, no data leaves the phone.",
	}
