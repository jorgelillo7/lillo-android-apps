## Summandos' screens over the 3D world: the menu, the level map, the HUD during a run and the
## result card. Emits what the player chose; main.gd decides what happens.
class_name SummandosUi
extends CanvasLayer

signal mode_picked(mode: int)
signal level_picked(number: int)
signal stress_picked
signal night_toggled
signal next_picked
signal again_picked
signal levels_picked
signal menu_picked

const FONT := preload("res://assets/fonts/LuckiestGuy-Regular.ttf")

var palette: Dictionary
var texts: Dictionary
var level_map: LilloLevelMap
var decor: Control
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


func _init(colours: Dictionary, words: Dictionary, map: LilloLevelMap) -> void:
	palette = colours
	texts = words
	level_map = map


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


## Shows one screen: "menu", "levels", "run" or "result".
func show_screen(screen: String) -> void:
	menu.visible = screen == "menu"
	level_map.visible = screen == "levels"
	hud.visible = screen == "run"
	result.visible = screen == "result"
	decor.visible = screen == "menu" or screen == "levels"


func show_levels(mode: int) -> void:
	var total := 0
	for number in range(1, level_map.count + 1):
		total += level_map.stars_of.call(number)
	level_map.open("%s   ★ %d/%d" % [texts.modes[mode], total, level_map.count * 3])
	show_screen("levels")


func show_result(won: bool, soldiers: int, kills: int, stars: int, has_next: bool) -> void:
	show_screen("result")
	result_title.text = texts.won if won else texts.lost
	result_detail.text = texts.detail % [soldiers, kills]
	for child in result_stars.get_children():
		child.queue_free()
	if won and stars > 0:
		var row := LilloStars.new(stars, 30.0, palette.weapon)
		result_stars.add_child(row)
		LilloFx.pop_in(row, 0.3)
		LilloFx.confetti(result, [palette.weapon, palette.squad, palette.good_floor, Color.WHITE])
	next_button.visible = has_next


func update_hud(top: String, thinking: float) -> void:
	hud_top.text = top
	think_box.visible = thinking > 0.0
	think_label.text = texts.think % ceili(thinking)
	fps_label.text = "%d FPS" % Engine.get_frames_per_second()


func _build_decor() -> void:
	decor = Control.new()
	decor.set_anchors_preset(Control.PRESET_FULL_RECT)
	decor.mouse_filter = Control.MOUSE_FILTER_IGNORE
	add_child(decor)
	decor.add_child(LilloBackdrop.new(palette))
	var hero := LilloSticker.new("soldier", palette.squad, palette.skin, palette.squad_dark, palette.outline)
	var boss := LilloSticker.new("monster", palette.boss, palette.boss_belly, palette.boss_horns, palette.outline)
	for sticker in [hero, boss]:
		sticker.size = Vector2(230, 230)
		decor.add_child(sticker)
	var place := func() -> void:
		var area := decor.get_viewport().get_visible_rect().size
		hero.position = Vector2(-10, area.y - 230)
		boss.position = Vector2(area.x - 200, area.y - 215)
	decor.get_viewport().size_changed.connect(place)
	place.call()


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
		box.add_child(LilloTheme.button(texts.modes[i], func() -> void: mode_picked.emit(i), 44, 96.0))
	var extras := HBoxContainer.new()
	extras.add_theme_constant_override("separation", 10)
	var night: bool = LilloSettings.read("night", false)
	var dark := LilloTheme.box(Color(palette.outline, 0.75), Color(palette.outline, 0.9))
	for button in [LilloTheme.button(texts.night_on if night else texts.night_off, func() -> void: night_toggled.emit(), 22),
			LilloTheme.button(texts.stress, func() -> void: stress_picked.emit(), 22)]:
		button.size_flags_horizontal = Control.SIZE_EXPAND_FILL
		button.autowrap_mode = TextServer.AUTOWRAP_WORD_SMART
		button.add_theme_stylebox_override("normal", dark)
		extras.add_child(button)
	box.add_child(extras)
	return screen


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
