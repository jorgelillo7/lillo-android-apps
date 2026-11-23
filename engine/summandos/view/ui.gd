## Summandos' screens over the 3D world: the menu, the level map, the HUD during a run and the
## result card. Emits what the player chose; main.gd decides what happens.
class_name SummandosUi
extends CanvasLayer

signal mode_picked(mode: int)
signal level_picked(number: int)
signal night_toggled
signal squad_picked(name: String)
signal next_picked
signal again_picked
signal levels_picked
signal menu_picked

const FONT := preload("res://assets/fonts/LuckiestGuy-Regular.ttf")
const READING_FONT := preload("res://assets/fonts/Fredoka.ttf")

var palette: Dictionary
var texts: Dictionary
var level_map: LilloLevelMap
var about: LilloAbout
var swatches: LilloSwatchPicker
var settings: Control
var corner: Control
var mute_button: LilloIconButton
var help: LilloInfoCard
## {name -> colour} for the squad picker, and the current choice.
var squad_choices := {}
var squad_choice := "default"
var audio: LilloAudio
var decor: Control
var boss_corner: Control
var menu: Control
var hud: Control
var result: Control
var hud_top: Label
var fps_label: Label
var think_box: Control
var think_label: Label
var result_title: Label
var result_detail: Label
var result_stars: CenterContainer
var next_button: Button


func _init(colours: Dictionary, words: Dictionary, map: LilloLevelMap, squads: Dictionary, squad: String,
		sounds: LilloAudio) -> void:
	audio = sounds
	palette = colours
	texts = words
	level_map = map
	squad_choices = squads
	squad_choice = squad


func _ready() -> void:
	_build_decor()
	var root := MarginContainer.new()
	root.set_anchors_preset(Control.PRESET_FULL_RECT)
	root.theme = LilloTheme.build(FONT, palette.weapon, palette.outline)
	add_child(root)
	LilloSafeArea.attach(root)
	hud = _build_hud()
	root.add_child(hud)
	menu = _build_menu()
	root.add_child(menu)
	root.add_child(level_map)
	level_map.picked.connect(func(number: int) -> void: level_picked.emit(number))
	level_map.back_pressed.connect(func() -> void: menu_picked.emit())
	result = _build_result()
	root.add_child(result)
	var version := str(ProjectSettings.get_setting("application/config/version", ""))
	var credits: Array = texts.credits.duplicate()
	if SummandosModels.scene("Skeleton_Golem.glb"):
		credits.insert(1, texts.golem_credit)
	about = LilloAbout.new(texts.title, texts.version % version, credits, texts.about_note, texts.close,
		palette.outline, palette.weapon, _reading())
	about.closed.connect(show_screen.bind("settings"))
	root.add_child(about)
	swatches = LilloSwatchPicker.new(texts.colour_title, squad_choices, squad_choice, texts.close, palette.outline)
	swatches.picked.connect(func(name: String) -> void: squad_picked.emit(name))
	swatches.closed.connect(show_screen.bind("settings"))
	root.add_child(swatches)
	settings = _build_settings()
	root.add_child(settings)
	help = LilloInfoCard.new(texts.help, texts.help_sections, texts.close, palette.outline, palette.weapon, _reading())
	help.closed.connect(show_screen.bind("menu"))
	root.add_child(help)
	corner = _build_corner()
	root.add_child(corner)


## Fredoka at a medium weight, for text meant to be read rather than shouted.
static func _reading() -> Font:
	var font := FontVariation.new()
	font.base_font = READING_FONT
	font.variation_opentype = {TextServerManager.get_primary_interface().name_to_tag("wght"): 560}
	return font


## Shows one screen: "menu", "levels", "run", "result" or "about".
func show_screen(screen: String) -> void:
	about.visible = screen == "about"
	swatches.visible = screen == "colours"
	settings.visible = screen == "settings"
	help.visible = screen == "help"
	corner.visible = screen == "menu"
	menu.visible = screen == "menu"
	level_map.visible = screen == "levels"
	hud.visible = screen == "run"
	result.visible = screen == "result"
	decor.visible = screen in ["menu", "levels", "about", "colours", "settings", "help"]


func show_levels(mode: int) -> void:
	level_map.count = SummandosLevel.level_count(mode)
	var total := 0
	for number in range(1, level_map.count + 1):
		total += level_map.stars_of.call(number)
	level_map.open("%s   ★ %d/%d" % [texts.modes[mode], total, level_map.count * 3])
	show_screen("levels")


## `failed_gate` (SummandosRun.failed_gate) explains an expert level lost on a gate.
func show_result(won: bool, soldiers: int, kills: int, stars: int, has_next: bool, failed_gate := {}) -> void:
	show_screen("result")
	result_title.text = texts.won if won else texts.lost
	result_detail.text = texts.detail % [soldiers, kills]
	if not failed_gate.is_empty():
		result_detail.text = texts.wrong_gate % [(failed_gate.taken as LilloGate).label(), failed_gate.taken_result,
			(failed_gate.other as LilloGate).label(), failed_gate.other_result]
	for child in result_stars.get_children():
		child.queue_free()
	if won and stars > 0:
		var row := LilloStars.new(stars, 30.0, palette.weapon)
		result_stars.add_child(row)
		LilloFx.pop_in(row, 0.3)
		for i in stars:
			get_tree().create_timer(0.35 + i * 0.25).timeout.connect(audio.play.bind("star", 1.0 + i * 0.12))
		LilloFx.confetti(result, [palette.weapon, palette.squad, palette.good_floor, Color.WHITE])
	next_button.visible = has_next


## `show_fps` puts the frame rate in the corner (the performance test only).
func update_hud(top: String, thinking: float, show_fps := false) -> void:
	hud_top.text = top
	fps_label.visible = show_fps
	think_box.visible = thinking > 0.0
	think_label.text = texts.think % ceili(thinking)
	fps_label.text = "%d FPS" % Engine.get_frames_per_second()


func _build_decor() -> void:
	decor = Control.new()
	decor.set_anchors_preset(Control.PRESET_FULL_RECT)
	decor.mouse_filter = Control.MOUSE_FILTER_IGNORE
	add_child(decor)
	decor.add_child(LilloBackdrop.new(palette))
	var hero: Control = null
	var archer := SummandosModels.character("Rogue_Hooded.glb", "Cheer", 1.0, palette.squad, false,
		SummandosModels.ARCHER_SPARES)
	if archer:
		hero = LilloModelStage.new(archer, 1.0, Vector2i(240, 280), 0.35)
	else:
		hero = LilloSticker.new("soldier", palette.squad, palette.skin, palette.squad_dark, palette.outline)
		hero.size = Vector2(230, 230)
	decor.add_child(hero)
	boss_corner = Control.new()
	boss_corner.size = Vector2(240, 280)
	boss_corner.mouse_filter = Control.MOUSE_FILTER_IGNORE
	decor.add_child(boss_corner)
	show_boss({})
	var place := func() -> void:
		var area := decor.get_viewport().get_visible_rect().size
		hero.position = Vector2(-10, area.y - hero.size.y)
		boss_corner.position = Vector2(area.x - boss_corner.size.x + 10, area.y - boss_corner.size.y)
	decor.get_viewport().size_changed.connect(place)
	place.call()


## Puts `look` (SummandosLevel.boss_look) in the bottom-right corner: the next boss to beat. An
## empty look, or no models, shows the drawn monster.
func show_boss(look: Dictionary) -> void:
	for child in boss_corner.get_children():
		child.queue_free()
	var boss: Node3D = null
	if not look.is_empty():
		boss = SummandosModels.character(look.model, "Idle_Combat", 1.6, SummandosPalette.BOSS_COLOURS[look.colour])
	if boss:
		boss_corner.add_child(LilloModelStage.new(boss, 1.6, Vector2i(240, 280), -0.35))
	else:
		var monster := LilloSticker.new("monster", palette.boss, palette.boss_belly, palette.boss_horns, palette.outline)
		monster.size = Vector2(230, 230)
		monster.position = Vector2(10, 50)
		boss_corner.add_child(monster)


func _column(parent: Control, card := false) -> VBoxContainer:
	var centre := CenterContainer.new()
	centre.set_anchors_preset(Control.PRESET_FULL_RECT)
	parent.add_child(centre)
	var holder: Control = centre
	if card:
		holder = LilloTheme.card(palette.outline)
		centre.add_child(holder)
	var box := VBoxContainer.new()
	box.custom_minimum_size = Vector2(minf(420.0, get_viewport().get_visible_rect().size.x - 104.0), 0)
	box.add_theme_constant_override("separation", 18)
	holder.add_child(box)
	return box


func _screen() -> Control:
	var screen := Control.new()
	screen.set_anchors_preset(Control.PRESET_FULL_RECT)
	return screen


func _build_menu() -> Control:
	var screen := _screen()
	var box := _column(screen)
	var title := LilloTheme.title(texts.title, 96)
	title.add_theme_color_override("font_color", palette.weapon)
	box.add_child(title)
	title.ready.connect(func() -> void:
		title.pivot_offset = title.size * 0.5
		var tween := title.create_tween().set_loops()
		tween.tween_property(title, "scale", Vector2(1.06, 1.06), 0.9).set_trans(Tween.TRANS_SINE)
		tween.tween_property(title, "scale", Vector2.ONE, 0.9).set_trans(Tween.TRANS_SINE))
	for i in texts.modes.size():
		box.add_child(_mode_button(i))

	return screen


## A menu button that flips a setting on and off, its text saying which (texts.<key>_on/_off).
func _switch(key: String, on: bool, apply: Callable) -> Button:
	var button := LilloTheme.button(texts[key + ("_on" if on else "_off")], func() -> void: pass, 26, 64.0)
	button.set_meta("on", on)
	button.pressed.connect(func() -> void:
		var now: bool = not button.get_meta("on")
		button.set_meta("on", now)
		button.text = texts[key + ("_on" if now else "_off")]
		apply.call(now)
		if mute_button:
			mute_button.set_kind("sound" if audio.sound_on or audio.music_on else "muted"))
	return button


## The two quiet buttons in the top-right corner: mute everything, and the settings card.
func _build_corner() -> Control:
	var layer := Control.new()
	layer.set_anchors_preset(Control.PRESET_FULL_RECT)
	layer.mouse_filter = Control.MOUSE_FILTER_IGNORE
	var row := HBoxContainer.new()
	row.set_anchors_and_offsets_preset(Control.PRESET_TOP_RIGHT)
	row.grow_horizontal = Control.GROW_DIRECTION_BEGIN
	row.add_theme_constant_override("separation", 10)
	layer.add_child(row)
	var quiet := Color(palette.outline, 0.55)
	mute_button = LilloIconButton.new("sound" if audio.sound_on or audio.music_on else "muted", quiet)
	mute_button.pressed.connect(func() -> void:
		var on := not (audio.sound_on or audio.music_on)
		audio.set_sound(on)
		audio.set_music(on)
		mute_button.set_kind("sound" if on else "muted")
		_refresh_settings())
	var question := LilloIconButton.new("help", quiet)
	question.pressed.connect(show_screen.bind("help"))
	row.add_child(question)
	row.add_child(mute_button)
	var gear := LilloIconButton.new("gear", quiet)
	gear.pressed.connect(show_screen.bind("settings"))
	row.add_child(gear)
	return layer


## A rank button: its name large, and what its gates hold underneath in the reading font.
func _mode_button(i: int) -> Button:
	var button := LilloTheme.button("", func() -> void: mode_picked.emit(i), 40, 100.0)
	var lines := VBoxContainer.new()
	lines.set_anchors_preset(Control.PRESET_FULL_RECT)
	lines.alignment = BoxContainer.ALIGNMENT_CENTER
	lines.mouse_filter = Control.MOUSE_FILTER_IGNORE
	lines.add_theme_constant_override("separation", -2)
	button.add_child(lines)
	var name := LilloTheme.title(texts.modes[i], 40)
	name.add_theme_color_override("font_color", Color.WHITE)
	name.mouse_filter = Control.MOUSE_FILTER_IGNORE
	lines.add_child(name)
	var hint := LilloAbout._text(texts.mode_hints[i], _reading(), 22, Color(palette.outline, 0.9))
	hint.mouse_filter = Control.MOUSE_FILTER_IGNORE
	lines.add_child(hint)
	return button


## The settings card: sound, music and vibration switches, night mode, squad colour, About.
func _build_settings() -> Control:
	var screen := _screen()
	var box := _column(screen, true)
	box.add_child(LilloTheme.title(texts.settings, 48))
	for entry in [["sound", audio.sound_on, audio.set_sound], ["music", audio.music_on, audio.set_music],
			["vibration", audio.vibration_on, audio.set_vibration]]:
		var button := _switch(entry[0], entry[1], entry[2])
		button.name = entry[0]
		box.add_child(button)
	var night: bool = LilloSettings.read("night", false)
	box.add_child(LilloTheme.button(texts.night_on if night else texts.night_off, func() -> void: night_toggled.emit(), 26, 64.0))
	box.add_child(LilloTheme.button(texts.colour, show_screen.bind("colours"), 26, 64.0))
	box.add_child(LilloTheme.button(texts.about, show_screen.bind("about"), 26, 64.0))
	box.add_child(LilloTheme.button(texts.close, show_screen.bind("menu"), 26, 64.0))
	return screen


## Puts the switches back in line with the settings after the mute button changed them.
func _refresh_settings() -> void:
	for key in ["sound", "music"]:
		var button: Button = settings.find_child(key, true, false)
		var on: bool = audio.sound_on if key == "sound" else audio.music_on
		button.set_meta("on", on)
		button.text = texts[key + ("_on" if on else "_off")]


func _build_hud() -> Control:
	var screen := _screen()
	screen.mouse_filter = Control.MOUSE_FILTER_IGNORE
	hud_top = LilloTheme.title("", 30)
	hud_top.set_anchors_preset(Control.PRESET_TOP_WIDE)
	screen.add_child(hud_top)
	fps_label = Label.new()
	fps_label.add_theme_font_size_override("font_size", 22)
	fps_label.set_anchors_preset(Control.PRESET_BOTTOM_LEFT)
	fps_label.position.y = -40
	screen.add_child(fps_label)
	think_box = VBoxContainer.new()
	think_box.set_anchors_preset(Control.PRESET_TOP_WIDE)
	think_box.position.y = 90
	think_box.mouse_filter = Control.MOUSE_FILTER_IGNORE
	screen.add_child(think_box)
	think_label = LilloTheme.title("", 64)
	think_label.add_theme_color_override("font_color", palette.weapon)
	think_box.add_child(think_label)
	think_box.add_child(LilloTheme.title(texts.think_hint, 26))
	think_box.visible = false
	return screen


func _build_result() -> Control:
	var screen := _screen()
	var box := _column(screen, true)
	result_title = LilloTheme.title("", 72)
	box.add_child(result_title)
	result_detail = LilloTheme.title("", 28)
	result_detail.autowrap_mode = TextServer.AUTOWRAP_WORD_SMART
	box.add_child(result_detail)
	result_stars = CenterContainer.new()
	result_stars.custom_minimum_size = Vector2(0, 64)
	box.add_child(result_stars)
	next_button = LilloTheme.button(texts.next, func() -> void: next_picked.emit())
	box.add_child(next_button)
	box.add_child(LilloTheme.button(texts.again, func() -> void: again_picked.emit()))
	box.add_child(LilloTheme.button(texts.menu, func() -> void: levels_picked.emit()))
	return screen
