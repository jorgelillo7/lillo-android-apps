## Summandos' screens over the 3D world: the menu, the level map, the HUD during a run, the pause
## card and the result card. Emits what the player chose; main.gd decides what happens. It keeps
## working while the game is paused.
class_name SummandosUi
extends CanvasLayer

signal mode_picked(mode: int)
signal endless_picked
signal level_picked(number: int)
signal night_toggled
signal squad_picked(name: String)
signal next_picked
signal again_picked
signal levels_picked
signal menu_picked
signal pause_picked
signal resume_picked

const FONT := preload("res://assets/fonts/LuckiestGuy-Regular.ttf")
const READING_FONT := preload("res://assets/fonts/Fredoka.ttf")
const PRIVACY_POLICY := "jorgelillo7.github.io/privacy/summandos/"

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
var paused_card: Control
var pause_button: LilloIconButton
var hud_heading: Label
var hud_top: Label
var boss_bar: ProgressBar
var fps_label: Label
var think_box: Control
## Top right of the run: the weapon the squad carries, turning, with its name.
var weapon_badge: VBoxContainer
var weapon_label: Label
var weapon_shown := 1.0
const WEAPON_BADGE_TOP := 120.0
const WEAPON_BADGE_SIZE := Vector2i(150, 110)
var think_label: Label
var result_heading: Label
var result_title: Label
var result_detail: Label
var result_stars: CenterContainer
var next_button: Button
## The menu's Endless button, shown once every rank's last level is won.
var endless_button: Button
var endless_hint: Label
var menu_box: VBoxContainer
var menu_buttons: Array[Button] = []


func _init(colours: Dictionary, words: Dictionary, map: LilloLevelMap, squads: Dictionary, squad: String,
		sounds: LilloAudio) -> void:
	audio = sounds
	palette = colours
	texts = words
	level_map = map
	squad_choices = squads
	squad_choice = squad


func _ready() -> void:
	process_mode = Node.PROCESS_MODE_ALWAYS
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
	paused_card = _build_paused()
	root.add_child(paused_card)
	var version := str(ProjectSettings.get_setting("application/config/version", ""))
	var credits: Array = texts.credits.duplicate()
	if SummandosModels.scene("Skeleton_Golem.glb"):
		credits.insert(1, texts.golem_credit)
	about = LilloAbout.new(texts.title, texts.version % version, credits, texts.about_note, texts.close,
		palette.outline, palette.weapon, _reading(), texts.name_story, [texts.privacy, PRIVACY_POLICY])
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


## Shows one screen: "menu", "levels", "run", "paused", "result", "about", "colours", "settings"
## or "help".
func show_screen(screen: String) -> void:
	about.visible = screen == "about"
	swatches.visible = screen == "colours"
	settings.visible = screen == "settings"
	help.visible = screen == "help"
	corner.visible = screen == "menu"
	menu.visible = screen == "menu"
	level_map.visible = screen == "levels"
	hud.visible = screen in ["run", "paused"]
	pause_button.visible = screen == "run"
	paused_card.visible = screen == "paused"
	result.visible = screen == "result"
	decor.visible = screen in ["menu", "levels", "about", "colours", "settings", "help", "cast"]


func show_levels(mode: int) -> void:
	level_map.count = SummandosLevel.level_count(mode)
	var total := 0
	for number in range(1, level_map.count + 1):
		total += level_map.stars_of.call(number)
	level_map.open("%s   ★ %d/%d" % [texts.modes[mode], total, level_map.count * 3])
	show_screen("levels")


## `heading` names the mode and level; `failed_gate` (SummandosRun.failed_gate) explains an expert
## level lost on a gate.
func show_result(heading: String, won: bool, soldiers: int, kills: int, stars: int, has_next: bool,
		failed_gate := {}) -> void:
	show_screen("result")
	result_heading.text = heading
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


## `heading` names the mode and level above the counts in `top`; `boss` is the boss's health
## (0 to 1) while it is near, else below 0; `show_fps` puts the frame rate in the corner (the
## performance test only).
func update_hud(heading: String, top: String, thinking: float, boss := -1.0, show_fps := false) -> void:
	hud_heading.text = heading
	hud_heading.visible = heading != ""
	hud_top.text = top
	boss_bar.visible = boss >= 0.0
	boss_bar.value = maxf(boss, 0.0)
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


## The cast, for the store: crossbowmen in three squad colours cheering, and the four bosses in
## their combat stance below. Shown over the backdrop with nothing else; empty without models.
func show_cast(squads: Array, bosses: Array) -> void:
	show_screen("cast")
	var column := VBoxContainer.new()
	column.set_anchors_preset(Control.PRESET_FULL_RECT)
	column.alignment = BoxContainer.ALIGNMENT_CENTER
	column.add_theme_constant_override("separation", 0)
	decor.add_child(column)
	for row in [[squads, "Rogue_Hooded.glb", "Cheer", 1.0, Vector2i(230, 330)], [bosses, "", "Idle_Combat", 1.6, Vector2i(175, 300)]]:
		var line := HBoxContainer.new()
		line.alignment = BoxContainer.ALIGNMENT_CENTER
		line.add_theme_constant_override("separation", -10)
		column.add_child(line)
		for entry in row[0]:
			var file: String = row[1] if row[1] != "" else entry.model
			var colour: Color = entry if entry is Color else SummandosPalette.BOSS_COLOURS[entry.colour]
			var body := SummandosModels.character(file, row[2], row[3], colour, false,
				SummandosModels.ARCHER_SPARES if row[1] != "" else [])
			if body:
				line.add_child(LilloModelStage.new(body, row[3], row[4], 0.25))
	for child in decor.get_children():
		if child != column and not child is LilloBackdrop:
			child.visible = false


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
		menu_buttons.append(_mode_button(i))
		box.add_child(menu_buttons[-1])
	endless_button = _mode_button(-1)
	endless_button.visible = false
	menu_buttons.append(endless_button)
	box.add_child(endless_button)
	menu_box = box
	get_viewport().size_changed.connect(_fit_menu)

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


## A rank button: its name large, and what its gates hold underneath in the reading font. -1 is
## the Endless button, its record underneath.
func _mode_button(i: int) -> Button:
	var pick := func() -> void:
		if i < 0:
			endless_picked.emit()
		else:
			mode_picked.emit(i)
	var button := LilloTheme.button("", pick, 40, 100.0)
	var lines := VBoxContainer.new()
	lines.set_anchors_preset(Control.PRESET_FULL_RECT)
	lines.alignment = BoxContainer.ALIGNMENT_CENTER
	lines.mouse_filter = Control.MOUSE_FILTER_IGNORE
	lines.add_theme_constant_override("separation", -2)
	button.add_child(lines)
	var name := LilloTheme.title(texts.endless if i < 0 else texts.modes[i], 40)
	name.add_theme_color_override("font_color", Color.WHITE)
	name.mouse_filter = Control.MOUSE_FILTER_IGNORE
	lines.add_child(name)
	var hint := LilloAbout._text("" if i < 0 else texts.mode_hints[i], _reading(), 22, Color(palette.outline, 0.9))
	hint.mouse_filter = Control.MOUSE_FILTER_IGNORE
	lines.add_child(hint)
	if i < 0:
		endless_hint = hint
	return button


## Shows the menu's Endless button when it is open, with the record.
func show_endless(open: bool, best: int) -> void:
	endless_button.visible = open
	endless_hint.text = texts.endless_hint % best
	_fit_menu()


## Five buttons do not fit a landscape phone at full height: shorter buttons, closer together.
func _fit_menu() -> void:
	var short := endless_button.visible and get_viewport().get_visible_rect().size.y < 900.0
	menu_box.add_theme_constant_override("separation", 8 if short else 18)
	for button in menu_buttons:
		button.custom_minimum_size.y = 84.0 if short else 100.0


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
	var top := VBoxContainer.new()
	top.set_anchors_and_offsets_preset(Control.PRESET_TOP_WIDE)
	top.add_theme_constant_override("separation", 0)
	top.mouse_filter = Control.MOUSE_FILTER_IGNORE
	screen.add_child(top)
	hud_heading = LilloTheme.title("", 24)
	hud_heading.add_theme_color_override("font_color", palette.weapon)
	top.add_child(hud_heading)
	hud_top = LilloTheme.title("", 30)
	top.add_child(hud_top)
	boss_bar = _boss_bar()
	top.add_child(boss_bar)
	fps_label = Label.new()
	fps_label.add_theme_font_size_override("font_size", 22)
	fps_label.set_anchors_preset(Control.PRESET_BOTTOM_LEFT)
	fps_label.position.y = -40
	screen.add_child(fps_label)
	# In the top-left corner, clear of the gates in the middle of the track.
	think_box = VBoxContainer.new()
	think_box.position = Vector2(20, 96)
	think_box.custom_minimum_size.x = 320
	think_box.add_theme_constant_override("separation", 0)
	think_box.mouse_filter = Control.MOUSE_FILTER_IGNORE
	screen.add_child(think_box)
	think_label = LilloTheme.title("", 52)
	think_label.horizontal_alignment = HORIZONTAL_ALIGNMENT_LEFT
	think_label.add_theme_color_override("font_color", palette.weapon)
	think_box.add_child(think_label)
	var hint := LilloTheme.title(texts.think_hint, 22)
	hint.horizontal_alignment = HORIZONTAL_ALIGNMENT_LEFT
	hint.autowrap_mode = TextServer.AUTOWRAP_WORD_SMART
	think_box.add_child(hint)
	think_box.visible = false
	weapon_badge = VBoxContainer.new()
	weapon_badge.set_anchors_and_offsets_preset(Control.PRESET_TOP_RIGHT)
	weapon_badge.grow_horizontal = Control.GROW_DIRECTION_BEGIN
	weapon_badge.position.y = WEAPON_BADGE_TOP
	weapon_badge.add_theme_constant_override("separation", 0)
	weapon_badge.mouse_filter = Control.MOUSE_FILTER_IGNORE
	weapon_badge.visible = false
	screen.add_child(weapon_badge)
	weapon_label = LilloTheme.title("", 26)
	weapon_label.add_theme_color_override("font_color", palette.weapon)
	weapon_badge.add_child(weapon_label)
	pause_button = LilloIconButton.new("pause", Color(palette.outline, 0.55))
	pause_button.position = Vector2(16, 16)
	pause_button.pressed.connect(func() -> void: pause_picked.emit())
	screen.add_child(pause_button)
	return screen


## Shows the weapon for damage `multiplier` in the badge, or hides it at 1 (bare crossbows).
func show_weapon(multiplier: float, name: String) -> void:
	if multiplier == weapon_shown:
		return
	weapon_shown = multiplier
	for child in weapon_badge.get_children():
		if child is LilloModelStage:
			child.queue_free()
	weapon_badge.visible = multiplier > 1.0
	weapon_label.text = name
	var model := SummandosModels.weapon(multiplier, 1.0) if multiplier > 1.0 else null
	if model:
		var stage := LilloModelStage.new(model, 0.8, WEAPON_BADGE_SIZE, -0.5)
		stage.size_flags_horizontal = Control.SIZE_SHRINK_CENTER
		weapon_badge.add_child(stage)
		weapon_badge.move_child(stage, 0)


## The boss's health under the counts: a thick bar in the boss's colour on a dark track.
func _boss_bar() -> ProgressBar:
	var bar := ProgressBar.new()
	bar.max_value = 1.0
	bar.show_percentage = false
	bar.custom_minimum_size = Vector2(320, 22)
	bar.size_flags_horizontal = Control.SIZE_SHRINK_CENTER
	bar.mouse_filter = Control.MOUSE_FILTER_IGNORE
	var back := StyleBoxFlat.new()
	back.bg_color = Color(palette.outline, 0.6)
	back.set_corner_radius_all(11)
	back.set_border_width_all(3)
	back.border_color = palette.outline
	var fill := StyleBoxFlat.new()
	fill.bg_color = palette.boss
	fill.set_corner_radius_all(11)
	fill.set_border_width_all(3)
	fill.border_color = palette.outline
	bar.add_theme_stylebox_override("background", back)
	bar.add_theme_stylebox_override("fill", fill)
	bar.visible = false
	return bar


func _build_result() -> Control:
	var screen := _screen()
	var box := _column(screen, true)
	result_heading = LilloTheme.title("", 24)
	result_heading.add_theme_color_override("font_color", palette.weapon)
	box.add_child(result_heading)
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


## The card over a paused run: carry on, start the level again, or back to the level map.
func _build_paused() -> Control:
	var screen := _screen()
	var box := _column(screen, true)
	box.add_child(LilloTheme.title(texts.paused, 72))
	box.add_child(LilloTheme.button(texts.resume, func() -> void: resume_picked.emit()))
	box.add_child(LilloTheme.button(texts.again, func() -> void: again_picked.emit()))
	box.add_child(LilloTheme.button(texts.menu, func() -> void: levels_picked.emit()))
	return screen
