## A paged level map in the style of the classic puzzle games: big numbered tiles with their stars
## in a tab underneath, locked levels greyed with a padlock, arrows and swipes between pages, and a
## back button. Three columns upright, five sideways; tiles grow to fill the screen.
class_name LilloLevelMap
extends Control

signal picked(number: int)
signal back_pressed

var count := 30
var per_page := 15
## (number) -> int: stars earned on a level.
var stars_of: Callable
## (number) -> bool: whether a level can be played.
var open_of: Callable
## {"tile", "tile_lip", "tab", "star", "arrow", "arrow_lip"}.
var colours := {}
var page := 0
var _grid: GridContainer
var _title: Label
var _dots: Label
var _swipe := 0.0


func _init(level_count: int, stars_of_level: Callable, open_of_level: Callable, map_colours: Dictionary,
		levels_per_page := 15) -> void:
	count = level_count
	stars_of = stars_of_level
	open_of = open_of_level
	colours = map_colours
	per_page = levels_per_page
	set_anchors_preset(Control.PRESET_FULL_RECT)
	gui_input.connect(_on_input)


func _ready() -> void:
	var column := VBoxContainer.new()
	column.set_anchors_preset(Control.PRESET_FULL_RECT)
	column.add_theme_constant_override("separation", 12)
	column.mouse_filter = Control.MOUSE_FILTER_PASS
	add_child(column)
	_title = LilloTheme.title("", 48)
	column.add_child(_title)
	var middle := HBoxContainer.new()
	middle.size_flags_vertical = Control.SIZE_EXPAND_FILL
	middle.mouse_filter = Control.MOUSE_FILTER_PASS
	column.add_child(middle)
	middle.add_child(LilloTheme.round_button("<", turn.bind(-1), colours.arrow, colours.arrow_lip))
	var centre := CenterContainer.new()
	centre.size_flags_horizontal = Control.SIZE_EXPAND_FILL
	centre.mouse_filter = Control.MOUSE_FILTER_PASS
	middle.add_child(centre)
	_grid = GridContainer.new()
	_grid.add_theme_constant_override("h_separation", 14)
	_grid.add_theme_constant_override("v_separation", 14)
	centre.add_child(_grid)
	middle.add_child(LilloTheme.round_button(">", turn.bind(1), colours.arrow, colours.arrow_lip))
	var footer := HBoxContainer.new()
	column.add_child(footer)
	var back := LilloTheme.button("<", func() -> void: back_pressed.emit(), 44, 84.0)
	back.custom_minimum_size.x = 84
	footer.add_child(back)
	_dots = LilloTheme.title("", 34)
	_dots.size_flags_horizontal = Control.SIZE_EXPAND_FILL
	footer.add_child(_dots)
	var spacer := Control.new()
	spacer.custom_minimum_size = Vector2(84, 0)
	footer.add_child(spacer)
	get_viewport().size_changed.connect(func() -> void:
		if visible:
			refresh())


func pages() -> int:
	return ceili(count / float(per_page))


## The first level not yet open's predecessor: the one to play next.
func next_to_play() -> int:
	var next := 1
	while next < count and open_of.call(next + 1):
		next += 1
	return next


## Shows `title` and the page holding the next level to play.
func open(title: String) -> void:
	page = (next_to_play() - 1) / per_page
	_title.text = title
	refresh()


func turn(step: int) -> void:
	page = clampi(page + step, 0, pages() - 1)
	refresh()


func refresh() -> void:
	if _grid == null:
		return
	var area := get_viewport().get_visible_rect().size
	_grid.columns = 3 if area.y > area.x else 5
	var rows := ceili(per_page / float(_grid.columns))
	var side := minf((area.x - 220.0) / _grid.columns, (area.y - 190.0) / rows / 1.3) - 14.0
	side = clampf(side, 72.0, 170.0)
	for child in _grid.get_children():
		child.queue_free()
	var first := page * per_page + 1
	for number in range(first, mini(first + per_page, count + 1)):
		var tile := _tile(number, side)
		_grid.add_child(tile)
		LilloFx.pop_in(tile, (number - first) * 0.03)
	var dots := PackedStringArray()
	for i in pages():
		dots.append("●" if i == page else "○")
	_dots.text = " ".join(dots)


func _tile(number: int, side: float) -> Control:
	var open_level: bool = open_of.call(number)
	var tile := VBoxContainer.new()
	tile.add_theme_constant_override("separation", 0)
	var button := LilloTheme.button(str(number) if open_level else "", func() -> void: picked.emit(number),
		int(side * 0.5), side * 0.96)
	button.custom_minimum_size.x = side
	button.disabled = not open_level
	var face := LilloTheme.box(colours.tile, colours.tile_lip)
	face.border_width_top = 5
	button.add_theme_stylebox_override("normal", face)
	if not open_level:
		button.add_child(_padlock())
	tile.add_child(button)
	var tab := PanelContainer.new()
	var style := StyleBoxFlat.new()
	style.bg_color = colours.tab
	style.corner_radius_bottom_left = 14
	style.corner_radius_bottom_right = 14
	style.content_margin_top = 4
	style.content_margin_bottom = 6
	tab.add_theme_stylebox_override("panel", style)
	tab.custom_minimum_size = Vector2(0, side * 0.32)
	tab.add_child(LilloStars.new(stars_of.call(number), side * 0.11, colours.star))
	tile.add_child(tab)
	return tile


func _padlock() -> Control:
	var lock := Control.new()
	lock.set_anchors_preset(Control.PRESET_FULL_RECT)
	lock.mouse_filter = Control.MOUSE_FILTER_IGNORE
	lock.draw.connect(func() -> void:
		var k := lock.size.x / 96.0
		var c := lock.size * 0.5 + Vector2(0, 6) * k
		var colour := Color(1, 1, 1, 0.85)
		lock.draw_arc(c + Vector2(0, -12) * k, 10.0 * k, PI, TAU, 16, colour, 5.0 * k, true)
		lock.draw_rect(Rect2(c + Vector2(-16, -12) * k, Vector2(32, 26) * k), colour)
		lock.draw_circle(c, 3.5 * k, Color(0.35, 0.35, 0.4)))
	return lock


func _on_input(event: InputEvent) -> void:
	if event is InputEventScreenDrag or (event is InputEventMouseMotion and event.button_mask != 0):
		_swipe += event.relative.x
	elif event is InputEventScreenTouch or event is InputEventMouseButton:
		if not event.pressed and absf(_swipe) > 80.0:
			turn(-1 if _swipe > 0.0 else 1)
		_swipe = 0.0
