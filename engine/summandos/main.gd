## Summandos' scene: builds the track, the squad, the enemies and the HUD in code, and draws a
## SummandosRun every frame. Command-line options after `--`: `--mode=play|stress` starts a run
## (`--difficulty=easy|normal|adult` and `--level=<n>` pick which),
## at once, `--levels` opens the level map, `--palette=<name>` picks a SummandosPalette theme, `--shot=<png>` saves a screenshot
## after `--after=<seconds>` (default 3) and quits.
extends Node3D

const SPACING := 0.42
const TRACK_WIDTH := 10.0
const SOLDIER_CAPACITY := 600
const ENEMY_CAPACITY := 700
const BULLET_CAPACITY := 300
const BULLET_SPEED := 45.0
const VISIBLE_AHEAD := 70.0

const LEVELS_PER_PAGE := 15
const FONT := preload("res://fonts/LuckiestGuy-Regular.ttf")
const BOSS_HEIGHT := 4.5
const GATE_FLOOR_LENGTH := 7.0

var texts := {}
var palette := SummandosPalette.named("night" if LilloSettings.read("night", false) else "sunset")
var run: SummandosRun
var mode := "play"
var difficulty: SummandosLevel.Mode = LilloSettings.read("difficulty", SummandosLevel.Mode.EASY)
var level_number := 1
var target_x := 0.0
var dragging := false

var camera: Camera3D
var ground: MeshInstance3D
var leader: MeshInstance3D
var count_label: Label3D
var soldiers_view: LilloCrowdView
var enemies_view: LilloCrowdView
var bullets_view: LilloCrowdView
var boss_view: LilloCrowdView
var count_pill: MeshInstance3D
var row_nodes: Array[Node3D] = []
var formation_cache := {}

var bullets := PackedVector3Array()
var bullet_ends := PackedFloat32Array()

var hud: Control
var hud_top: Label
var fps_label: Label
var menu: Control
var result: Control
var result_title: Label
var result_detail: Label
var result_stars: Label
var next_button: Button
var think_box: Control
var think_label: Label
var levels: Control
## The menus' backdrop and the characters in its corners, hidden during a run.
var decor: Control
var shown_row := 0
var level_grid: GridContainer
var levels_title: Label
var page_dots: Label
var page := 0
var swipe := 0.0

var shot_path := ""
## Camera rig for the current orientation: height, distance behind the squad, look-ahead.
var rig := Vector3(7.5, 8.5, 9.0)
var shot_after := 3.0
var elapsed := 0.0


func _ready() -> void:
	texts = _texts(OS.get_locale_language())
	for arg in OS.get_cmdline_user_args():
		if arg.begins_with("--palette="):
			palette = SummandosPalette.named(arg.trim_prefix("--palette="))
	_build_world()
	_build_ui()
	get_viewport().size_changed.connect(_fit_camera)
	_fit_camera()
	var start_now := false
	for arg in OS.get_cmdline_user_args():
		if arg.begins_with("--mode="):
			mode = arg.trim_prefix("--mode=")
			start_now = true
		elif arg.begins_with("--shot="):
			shot_path = arg.trim_prefix("--shot=")
		elif arg.begins_with("--difficulty="):
			difficulty = ["easy", "normal", "adult"].find(arg.trim_prefix("--difficulty=")) as SummandosLevel.Mode
		elif arg.begins_with("--level="):
			level_number = int(arg.trim_prefix("--level="))
		elif arg.begins_with("--after="):
			shot_after = float(arg.trim_prefix("--after="))
	if start_now:
		_start(mode)
	elif OS.get_cmdline_user_args().has("--levels"):
		_open_levels(-1)
	else:
		_show_menu()


func _texts(language: String) -> Dictionary:
	if language == "es":
		return {
			"title": "Sumandos", "play": "Jugar", "stress": "Prueba de rendimiento (1.000)",
			"night_on": "Modo noche: sí", "night_off": "Modo noche: no",
			"won": "¡Victoria!", "lost": "Derrota", "again": "Otra vez", "menu": "Menú",
			"kills": "Bajas", "weapon": "ARMA", "soldiers": "Soldados",
			"detail": "%d soldados en pie · %d bajas enemigas",
			"modes": ["Fácil", "Normal", "Adulto"], "next": "Siguiente nivel", "level": "Nivel %d",
			"think": "¡Piensa! %d", "think_hint": "Elige puerta y suelta el dedo",
		}
	return {
		"title": "Summandos", "play": "Play", "stress": "Performance test (1,000)",
		"night_on": "Night mode: on", "night_off": "Night mode: off",
		"won": "Victory!", "lost": "Defeat", "again": "Again", "menu": "Menu",
		"kills": "Kills", "weapon": "WEAPON", "soldiers": "Soldiers",
		"detail": "%d soldiers standing · %d enemies down",
		"modes": ["Easy", "Normal", "Grown-up"], "next": "Next level", "level": "Level %d",
		"think": "Think! %d", "think_hint": "Pick a gate and lift your finger",
	}


# --- World -------------------------------------------------------------------------------------

func _material(colour: Color, transparent := false, emissive := false) -> StandardMaterial3D:
	var material := StandardMaterial3D.new()
	material.albedo_color = colour
	if transparent:
		material.transparency = BaseMaterial3D.TRANSPARENCY_ALPHA
		material.cull_mode = BaseMaterial3D.CULL_DISABLED
		material.shading_mode = BaseMaterial3D.SHADING_MODE_UNSHADED
	if emissive:
		material.emission_enabled = true
		material.emission = colour
	return material


## The track: bands across it in two tones, so speed reads at a glance.
func _stripes(base: Color, stripe: Color) -> ShaderMaterial:
	var shader := Shader.new()
	shader.code = """
shader_type spatial;
render_mode diffuse_toon;
uniform vec4 base : source_color;
uniform vec4 stripe : source_color;
varying vec3 world;
void vertex() { world = (MODEL_MATRIX * vec4(VERTEX, 1.0)).xyz; }
void fragment() { ALBEDO = mod(world.z, 4.0) < 2.0 ? base.rgb : stripe.rgb; ROUGHNESS = 0.9; }
"""
	var material := ShaderMaterial.new()
	material.shader = shader
	material.set_shader_parameter("base", base)
	material.set_shader_parameter("stripe", stripe)
	return material


## A rounded badge that always faces the camera, drawn behind the squad's count.
func _pill(colour: Color) -> ShaderMaterial:
	var shader := Shader.new()
	shader.code = """
shader_type spatial;
render_mode unshaded, depth_test_disabled, cull_disabled;
uniform vec4 colour : source_color;
void vertex() {
	MODELVIEW_MATRIX = VIEW_MATRIX * mat4(INV_VIEW_MATRIX[0], INV_VIEW_MATRIX[1], INV_VIEW_MATRIX[2], MODEL_MATRIX[3]);
}
void fragment() {
	vec2 p = (UV - 0.5) * vec2(2.0, 0.95);
	vec2 q = abs(p) - vec2(1.0 - 0.475, 0.0);
	float d = length(max(q, 0.0)) - 0.475;
	ALBEDO = colour.rgb;
	ALPHA = colour.a * (1.0 - smoothstep(-0.02, 0.0, d));
}
"""
	var material := ShaderMaterial.new()
	material.shader = shader
	material.set_shader_parameter("colour", colour)
	material.render_priority = 0
	return material


func _build_world() -> void:
	var environment := WorldEnvironment.new()
	var env := Environment.new()
	env.background_mode = Environment.BG_SKY
	var sky_material := ProceduralSkyMaterial.new()
	sky_material.sky_top_color = palette.sky_top
	sky_material.sky_horizon_color = palette.sky
	sky_material.ground_horizon_color = palette.sky
	sky_material.ground_bottom_color = palette.sky
	sky_material.sun_angle_max = 0.0
	var sky := Sky.new()
	sky.sky_material = sky_material
	env.sky = sky
	env.ambient_light_source = Environment.AMBIENT_SOURCE_COLOR
	env.ambient_light_color = palette.ambient
	env.ambient_light_energy = 0.6
	environment.environment = env
	add_child(environment)

	var sun := DirectionalLight3D.new()
	sun.rotation_degrees = Vector3(-55, -30, 0)
	sun.light_energy = 0.85
	add_child(sun)

	ground = MeshInstance3D.new()
	var plane := PlaneMesh.new()
	plane.size = Vector2(TRACK_WIDTH, 2000.0)
	ground.mesh = plane
	ground.material_override = _stripes(palette.track, palette.track_stripe)
	add_child(ground)
	for side in [-1.0, 1.0]:
		var edge := MeshInstance3D.new()
		var box := BoxMesh.new()
		box.size = Vector3(0.3, 0.3, 2000.0)
		edge.mesh = box
		edge.position = Vector3(side * (TRACK_WIDTH * 0.5 + 0.15), 0.15, 0.0)
		edge.material_override = _material(palette.edge)
		add_child(edge)

	var outline := Color(palette.outline, 0.9)
	var soldier := LilloFigure.build(0.75, palette.squad_dark, palette.skin, palette.squad)
	soldiers_view = LilloCrowdView.new(soldier, LilloFigure.material(outline), SOLDIER_CAPACITY)
	add_child(soldiers_view)
	var enemy := LilloFigure.build(0.75, palette.enemy, palette.enemy_skin)
	enemies_view = LilloCrowdView.new(enemy, LilloFigure.material(outline), ENEMY_CAPACITY)
	add_child(enemies_view)
	var monster := LilloFigure.monster(BOSS_HEIGHT, palette.boss, palette.boss_belly, palette.boss_horns)
	boss_view = LilloCrowdView.new(monster, LilloFigure.material(outline), 4)
	add_child(boss_view)
	var bullet := LilloFigure.streak(0.7, 0.05, palette.bullet)
	var bullet_material := _material(palette.bullet, false, true)
	bullet_material.vertex_color_use_as_albedo = true
	bullets_view = LilloCrowdView.new(bullet, bullet_material, BULLET_CAPACITY)
	add_child(bullets_view)

	leader = MeshInstance3D.new()
	leader.mesh = LilloFigure.build(1.1, palette.squad_dark, palette.skin, palette.squad)
	leader.material_override = LilloFigure.material(outline)
	leader.rotation.y = PI
	add_child(leader)

	count_label = Label3D.new()
	count_label.billboard = BaseMaterial3D.BILLBOARD_ENABLED
	count_label.font_size = 96
	count_label.outline_size = 24
	count_label.pixel_size = 0.01
	count_label.no_depth_test = true
	count_label.render_priority = 2
	count_label.outline_render_priority = 1
	count_label.modulate = palette.weapon
	count_label.outline_modulate = palette.outline
	add_child(count_label)
	count_pill = MeshInstance3D.new()
	var pill := QuadMesh.new()
	pill.size = Vector2(2.0, 0.95)
	count_pill.mesh = pill
	count_pill.material_override = _pill(palette.squad_dark)
	add_child(count_pill)

	camera = Camera3D.new()
	add_child(camera)


func _fit_camera() -> void:
	var size := get_viewport().get_visible_rect().size
	if size.y > size.x:
		camera.keep_aspect = Camera3D.KEEP_WIDTH
		camera.fov = 62.0
		rig = Vector3(7.0, 6.5, 12.0)
	else:
		camera.keep_aspect = Camera3D.KEEP_HEIGHT
		camera.fov = 50.0
		rig = Vector3(7.5, 8.0, 6.0)


func _build_rows() -> void:
	for node in row_nodes:
		node.queue_free()
	row_nodes.clear()
	for row in run.level.rows:
		var node := Node3D.new()
		node.position = Vector3(0, 0, row.z)
		node.add_child(_choice(row.left, -1.0))
		node.add_child(_choice(row.right, 1.0))
		add_child(node)
		row_nodes.append(node)


func _choice(pick: Dictionary, side: float) -> Node3D:
	var node := Node3D.new()
	node.position.x = _world_x(side * TRACK_WIDTH * 0.25)
	var label := Label3D.new()
	label.font_size = 200
	label.outline_size = 40
	label.pixel_size = 0.01
	label.rotation.y = PI
	label.position.y = 1.3
	if pick.has("gate"):
		var gate: LilloGate = pick.gate
		var panel := MeshInstance3D.new()
		var quad := QuadMesh.new()
		quad.size = Vector2(TRACK_WIDTH * 0.5 - 0.3, 2.4)
		panel.mesh = quad
		panel.position.y = 1.2
		var good := gate.apply(100) >= 100
		panel.material_override = _material(palette.good_gate if good else palette.bad_gate, true)
		node.add_child(panel)
		var floor := MeshInstance3D.new()
		var floor_plane := PlaneMesh.new()
		floor_plane.size = Vector2(TRACK_WIDTH * 0.5 - 0.15, GATE_FLOOR_LENGTH)
		floor.mesh = floor_plane
		floor.position = Vector3(0, 0.01, -GATE_FLOOR_LENGTH * 0.5)
		floor.material_override = _material(palette.good_floor if good else palette.bad_floor)
		node.add_child(floor)
		label.text = gate.label()
		if gate.note != "":
			var note := Label3D.new()
			note.text = gate.note
			note.font_size = 110
			note.outline_size = 28
			note.pixel_size = 0.01
			note.rotation.y = PI
			note.position.y = 0.35
			node.add_child(note)
	else:
		var crate := MeshInstance3D.new()
		var box := BoxMesh.new()
		box.size = Vector3(1.4, 0.8, 0.8)
		crate.mesh = box
		crate.position.y = 0.4
		crate.material_override = _material(palette.weapon, false, true)
		node.add_child(crate)
		label.text = "%s ×%s" % [texts.weapon, str(pick.weapon).trim_suffix(".0")]
		label.font_size = 96
		label.modulate = palette.weapon
	node.add_child(label)
	return node


# --- UI ----------------------------------------------------------------------------------------

func _button(text: String, action: Callable) -> Button:
	var button := Button.new()
	button.text = text
	button.custom_minimum_size = Vector2(0, 72)
	button.add_theme_font_size_override("font_size", 30)
	button.pressed.connect(action)
	LilloFx.squishy(button)
	return button


## A centred column; `backdrop` puts it on a dark rounded panel so it reads over the 3D scene.
func _centre_box(parent: Control, backdrop := false) -> VBoxContainer:
	var centre := CenterContainer.new()
	centre.set_anchors_preset(Control.PRESET_FULL_RECT)
	parent.add_child(centre)
	var holder: Control = centre
	if backdrop:
		var panel := PanelContainer.new()
		var style := StyleBoxFlat.new()
		style.bg_color = Color(palette.outline, 0.72)
		style.set_corner_radius_all(24)
		style.set_content_margin_all(28)
		panel.add_theme_stylebox_override("panel", style)
		centre.add_child(panel)
		holder = panel
	var box := VBoxContainer.new()
	box.custom_minimum_size = Vector2(minf(420.0, get_viewport().get_visible_rect().size.x - 104.0), 0)
	box.add_theme_constant_override("separation", 18)
	holder.add_child(box)
	return box


func _title(text: String, size: int) -> Label:
	var label := Label.new()
	label.text = text
	label.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	label.add_theme_font_size_override("font_size", size)
	label.add_theme_constant_override("outline_size", 12)
	label.add_theme_color_override("font_outline_color", palette.outline)
	return label


func _build_ui() -> void:
	var layer := CanvasLayer.new()
	add_child(layer)
	decor = Control.new()
	decor.set_anchors_preset(Control.PRESET_FULL_RECT)
	decor.mouse_filter = Control.MOUSE_FILTER_IGNORE
	layer.add_child(decor)
	decor.add_child(LilloBackdrop.new(palette))
	var hero := LilloSticker.new("soldier", palette.squad, palette.skin, palette.squad_dark, palette.outline)
	var boss := LilloSticker.new("monster", palette.boss, palette.boss_belly, palette.boss_horns, palette.outline)
	for pair in [[hero, Control.PRESET_BOTTOM_LEFT, 1.0], [boss, Control.PRESET_BOTTOM_RIGHT, -1.0]]:
		var sticker: Control = pair[0]
		sticker.set_anchors_preset(pair[1])
		sticker.size = Vector2(230, 230)
		decor.add_child(sticker)
	get_viewport().size_changed.connect(func() -> void:
		var area := get_viewport().get_visible_rect().size
		hero.position = Vector2(-10, area.y - 230)
		boss.position = Vector2(area.x - 200, area.y - 215))
	get_viewport().size_changed.emit.call_deferred()
	var root := MarginContainer.new()
	root.set_anchors_preset(Control.PRESET_FULL_RECT)
	layer.add_child(root)
	root.set_meta("safe", true)
	root.theme = _theme()
	get_viewport().size_changed.connect(_fit_safe_area.bind(root))
	_fit_safe_area(root)

	hud = Control.new()
	hud.mouse_filter = Control.MOUSE_FILTER_IGNORE
	root.add_child(hud)
	hud_top = _title("", 30)
	hud_top.set_anchors_preset(Control.PRESET_TOP_WIDE)
	hud.add_child(hud_top)
	fps_label = Label.new()
	fps_label.add_theme_font_size_override("font_size", 22)
	fps_label.set_anchors_preset(Control.PRESET_BOTTOM_LEFT)
	fps_label.position.y = -40
	hud.add_child(fps_label)
	think_box = VBoxContainer.new()
	think_box.set_anchors_preset(Control.PRESET_TOP_WIDE)
	think_box.position.y = 90
	think_box.mouse_filter = Control.MOUSE_FILTER_IGNORE
	hud.add_child(think_box)
	think_label = _title("", 64)
	think_label.add_theme_color_override("font_color", palette.weapon)
	think_box.add_child(think_label)
	think_box.add_child(_title(texts.think_hint, 26))
	think_box.visible = false

	menu = Control.new()
	menu.set_anchors_preset(Control.PRESET_FULL_RECT)
	root.add_child(menu)
	var menu_box := _centre_box(menu)
	var title := _title(texts.title, 96)
	title.add_theme_color_override("font_color", palette.weapon)
	menu_box.add_child(title)
	title.ready.connect(func() -> void:
		title.pivot_offset = title.size * 0.5
		var tween := title.create_tween().set_loops()
		tween.tween_property(title, "scale", Vector2(1.06, 1.06), 0.9).set_trans(Tween.TRANS_SINE)
		tween.tween_property(title, "scale", Vector2.ONE, 0.9).set_trans(Tween.TRANS_SINE))
	for i in texts.modes.size():
		var button := _button(texts.modes[i], _open_levels.bind(i))
		button.custom_minimum_size.y = 96
		button.add_theme_font_size_override("font_size", 44)
		button.set_meta("mode", i)
		menu_box.add_child(button)
	var extras := HBoxContainer.new()
	extras.add_theme_constant_override("separation", 10)
	var night: bool = LilloSettings.read("night", false)
	for button in [_button(texts.night_on if night else texts.night_off, _toggle_night),
			_button(texts.stress, _start.bind("stress"))]:
		button.size_flags_horizontal = Control.SIZE_EXPAND_FILL
		button.add_theme_font_size_override("font_size", 22)
		button.autowrap_mode = TextServer.AUTOWRAP_WORD_SMART
		button.add_theme_stylebox_override("normal", _box(Color(palette.outline, 0.75), Color(palette.outline, 0.9)))
		extras.add_child(button)
	menu_box.add_child(extras)

	levels = Control.new()
	levels.set_anchors_preset(Control.PRESET_FULL_RECT)
	levels.gui_input.connect(_swipe_levels)
	root.add_child(levels)
	var column := VBoxContainer.new()
	column.set_anchors_preset(Control.PRESET_FULL_RECT)
	column.add_theme_constant_override("separation", 12)
	column.mouse_filter = Control.MOUSE_FILTER_PASS
	levels.add_child(column)
	levels_title = _title("", 48)
	column.add_child(levels_title)
	var middle := HBoxContainer.new()
	middle.size_flags_vertical = Control.SIZE_EXPAND_FILL
	middle.alignment = BoxContainer.ALIGNMENT_CENTER
	middle.mouse_filter = Control.MOUSE_FILTER_PASS
	column.add_child(middle)
	middle.add_child(_round_button("<", _turn_page.bind(-1)))
	var centre := CenterContainer.new()
	centre.size_flags_horizontal = Control.SIZE_EXPAND_FILL
	centre.mouse_filter = Control.MOUSE_FILTER_PASS
	middle.add_child(centre)
	level_grid = GridContainer.new()
	level_grid.add_theme_constant_override("h_separation", 14)
	level_grid.add_theme_constant_override("v_separation", 14)
	centre.add_child(level_grid)
	middle.add_child(_round_button(">", _turn_page.bind(1)))
	var footer := HBoxContainer.new()
	column.add_child(footer)
	var back := _button("<", _show_menu)
	back.custom_minimum_size = Vector2(84, 84)
	back.add_theme_font_size_override("font_size", 44)
	footer.add_child(back)
	page_dots = _title("", 34)
	page_dots.size_flags_horizontal = Control.SIZE_EXPAND_FILL
	footer.add_child(page_dots)
	var spacer := Control.new()
	spacer.custom_minimum_size = Vector2(84, 0)
	footer.add_child(spacer)
	get_viewport().size_changed.connect(func() -> void:
		if levels.visible:
			_fill_levels())

	result = Control.new()
	result.set_anchors_preset(Control.PRESET_FULL_RECT)
	root.add_child(result)
	var result_box := _centre_box(result, true)
	result_title = _title("", 72)
	result_box.add_child(result_title)
	result_detail = _title("", 28)
	result_detail.autowrap_mode = TextServer.AUTOWRAP_WORD_SMART
	result_box.add_child(result_detail)
	result_stars = _title("", 64)
	result_stars.add_theme_color_override("font_color", palette.weapon)
	result_box.add_child(result_stars)
	next_button = _button(texts.next, func() -> void: _start("play", level_number + 1))
	result_box.add_child(next_button)
	result_box.add_child(_button(texts.again, func() -> void: _start(mode, level_number)))
	result_box.add_child(_button(texts.menu, _open_levels.bind(-1)))


## Keeps the HUD and buttons clear of the display cutout and the system bars.
func _fit_safe_area(root: MarginContainer) -> void:
	var window := DisplayServer.window_get_size()
	var safe := DisplayServer.get_display_safe_area()
	var scale := get_viewport().get_visible_rect().size.x / maxf(1.0, float(window.x))
	var margins := {
		"margin_left": maxi(16, int(safe.position.x * scale)),
		"margin_top": maxi(16, int(safe.position.y * scale)),
		"margin_right": maxi(16, int((window.x - safe.end.x) * scale)),
		"margin_bottom": maxi(16, int((window.y - safe.end.y) * scale)),
	}
	if safe.size.x <= 0:
		margins = {"margin_left": 16, "margin_top": 16, "margin_right": 16, "margin_bottom": 16}
	for key in margins:
		root.add_theme_constant_override(key, margins[key])


func _toggle_night() -> void:
	LilloSettings.write("night", not LilloSettings.read("night", false))
	get_tree().reload_current_scene()


## The game's look for every control: the chunky font, outlined text, buttons in the palette's
## gold with a darker lip so they read as pressable.
func _theme() -> Theme:
	var theme := Theme.new()
	theme.default_font = FONT
	theme.set_color("font_color", "Button", Color.WHITE)
	theme.set_color("font_outline_color", "Button", palette.outline)
	theme.set_constant("outline_size", "Button", 10)
	var gold := Color(palette.weapon)
	theme.set_stylebox("normal", "Button", _box(gold, gold.darkened(0.35)))
	theme.set_stylebox("hover", "Button", _box(gold.lightened(0.1), gold.darkened(0.35)))
	theme.set_stylebox("pressed", "Button", _box(gold.darkened(0.15), gold.darkened(0.45), true))
	theme.set_stylebox("disabled", "Button", _box(Color(0.55, 0.55, 0.6), Color(0.35, 0.35, 0.4)))
	theme.set_stylebox("focus", "Button", StyleBoxEmpty.new())
	theme.set_color("font_outline_color", "Label", palette.outline)
	return theme


## A rounded box with a darker lip under it; `pressed` flattens the lip.
func _box(fill: Color, lip: Color, pressed := false) -> StyleBoxFlat:
	var box := StyleBoxFlat.new()
	box.bg_color = fill
	box.border_color = lip
	box.border_width_bottom = 2 if pressed else 8
	box.border_width_top = 6 if pressed else 0
	box.border_blend = false
	box.set_corner_radius_all(18)
	box.set_content_margin_all(10)
	box.shadow_color = Color(0, 0, 0, 0.25)
	box.shadow_size = 4
	box.shadow_offset = Vector2(0, 3)
	return box


func _round_button(text: String, action: Callable) -> Button:
	var button := _button(text, action)
	button.custom_minimum_size = Vector2(64, 64)
	button.size_flags_vertical = Control.SIZE_SHRINK_CENTER
	button.add_theme_font_size_override("font_size", 36)
	var style := _box(palette.squad, palette.squad_dark)
	style.set_corner_radius_all(32)
	button.add_theme_stylebox_override("normal", style)
	return button


## Opens the level map of `picked` mode (-1 keeps the current one), on the page of the next level
## to play.
func _open_levels(picked: int) -> void:
	if picked >= 0:
		difficulty = picked as SummandosLevel.Mode
		LilloSettings.write("difficulty", picked)
	run = null
	var next := 1
	while next < SummandosLevel.LEVELS_PER_MODE and SummandosProgress.unlocked(difficulty, next + 1):
		next += 1
	page = (next - 1) / LEVELS_PER_PAGE
	menu.visible = false
	result.visible = false
	hud.visible = false
	levels.visible = true
	decor.visible = true
	_draw_idle()
	_fill_levels()


func _turn_page(step: int) -> void:
	var pages := ceili(SummandosLevel.LEVELS_PER_MODE / float(LEVELS_PER_PAGE))
	page = clampi(page + step, 0, pages - 1)
	_fill_levels()


func _swipe_levels(event: InputEvent) -> void:
	if event is InputEventScreenDrag or (event is InputEventMouseMotion and event.button_mask != 0):
		swipe += event.relative.x
	elif event is InputEventScreenTouch or event is InputEventMouseButton:
		if not event.pressed and absf(swipe) > 80.0:
			_turn_page(-1 if swipe > 0.0 else 1)
		swipe = 0.0


## One page of the level map: a tile per level with its stars under it, locked levels greyed with
## a padlock. Three columns upright, five sideways.
func _fill_levels() -> void:
	var size := get_viewport().get_visible_rect().size
	var upright := size.y > size.x
	level_grid.columns = 3 if upright else 5
	var rows := ceili(LEVELS_PER_PAGE / float(level_grid.columns))
	var side := minf((size.x - 220.0) / level_grid.columns, (size.y - 190.0) / rows / 1.3) - 14.0
	side = clampf(side, 72.0, 170.0)
	for child in level_grid.get_children():
		child.queue_free()
	var total := 0
	for number in range(1, SummandosLevel.LEVELS_PER_MODE + 1):
		total += SummandosProgress.stars(difficulty, number)
	levels_title.text = "%s   ★ %d/%d" % [texts.modes[difficulty], total, SummandosLevel.LEVELS_PER_MODE * 3]
	var first := page * LEVELS_PER_PAGE + 1
	for number in range(first, mini(first + LEVELS_PER_PAGE, SummandosLevel.LEVELS_PER_MODE + 1)):
		var tile := _level_tile(number, side)
		level_grid.add_child(tile)
		LilloFx.pop_in(tile, (number - first) * 0.03)
	var pages := ceili(SummandosLevel.LEVELS_PER_MODE / float(LEVELS_PER_PAGE))
	var dots := PackedStringArray()
	for i in pages:
		dots.append("●" if i == page else "○")
	page_dots.text = " ".join(dots)


func _level_tile(number: int, side: float) -> Control:
	var open := SummandosProgress.unlocked(difficulty, number)
	var earned := SummandosProgress.stars(difficulty, number)
	var tile := VBoxContainer.new()
	tile.add_theme_constant_override("separation", 0)
	var button := _button("" if not open else str(number), _start.bind("play", number))
	button.custom_minimum_size = Vector2(side, side * 0.96)
	button.add_theme_font_size_override("font_size", int(side * 0.5))
	button.disabled = not open
	var face := _box(palette.squad, palette.squad_dark)
	face.border_width_top = 5
	face.border_color = palette.squad_dark
	button.add_theme_stylebox_override("normal", face)
	if not open:
		button.add_child(_padlock())
	tile.add_child(button)
	var tab := PanelContainer.new()
	var tab_style := StyleBoxFlat.new()
	tab_style.bg_color = Color(palette.outline, 0.7)
	tab_style.corner_radius_bottom_left = 14
	tab_style.corner_radius_bottom_right = 14
	tab_style.content_margin_top = 4
	tab_style.content_margin_bottom = 6
	tab.add_theme_stylebox_override("panel", tab_style)
	tab.custom_minimum_size = Vector2(0, side * 0.32)
	tile.add_child(tab)
	var stars := Control.new()
	stars.custom_minimum_size = Vector2(0, side * 0.22)
	stars.draw.connect(func() -> void: _draw_stars(stars, earned, side * 0.11))
	tab.add_child(stars)
	return tile


func _draw_stars(where: Control, earned: int, star: float) -> void:
	var gap := star * 2.4
	var start := where.size.x * 0.5 - gap
	for i in 3:
		var centre := Vector2(start + i * gap, where.size.y * 0.5)
		var points := PackedVector2Array()
		for k in 10:
			var radius := star if k % 2 == 0 else star * 0.44
			var angle := -PI / 2.0 + k * PI / 5.0
			points.append(centre + Vector2(cos(angle), sin(angle)) * radius)
		var fill: Color = palette.weapon if i < earned else Color(1, 1, 1, 0.18)
		where.draw_colored_polygon(points, fill)
		if i < earned:
			points.append(points[0])
			where.draw_polyline(points, palette.weapon.darkened(0.4), 1.5, true)


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


func _stars(count: int) -> String:
	return "★".repeat(count) + "☆".repeat(3 - count)


func _show_menu() -> void:
	run = null
	menu.visible = true
	decor.visible = true
	levels.visible = false
	result.visible = false
	hud.visible = false
	_draw_idle()


func _start(run_mode: String, number := level_number) -> void:
	mode = run_mode
	level_number = clampi(number, 1, SummandosLevel.LEVELS_PER_MODE)
	var level := SummandosLevel.stress() if mode == "stress" else SummandosLevel.generate(difficulty, level_number)
	run = SummandosRun.new(level)
	target_x = 0.0
	bullets.clear()
	bullet_ends.clear()
	formation_cache.clear()
	_build_rows()
	menu.visible = false
	levels.visible = false
	decor.visible = false
	result.visible = false
	hud.visible = true
	shown_row = 0


func _finish() -> void:
	result.visible = true
	hud.visible = false
	var won := run.state == SummandosRun.State.WON
	result_title.text = texts.won if won else texts.lost
	result_detail.text = texts.detail % [run.soldiers, run.kills]
	var stars := run.stars() if mode == "play" else 0
	result_stars.text = _stars(stars) if won and mode == "play" else ""
	LilloFx.pop_in(result_stars, 0.3)
	if won:
		LilloFx.confetti(result, [palette.weapon, palette.squad, palette.good_floor, Color.WHITE])
	if mode == "play":
		SummandosProgress.record(difficulty, level_number, stars)
	next_button.visible = won and mode == "play" and level_number < SummandosLevel.LEVELS_PER_MODE


# --- Input -------------------------------------------------------------------------------------

func _unhandled_input(event: InputEvent) -> void:
	if run == null:
		return
	var width := get_viewport().get_visible_rect().size.x
	if event is InputEventScreenTouch or event is InputEventMouseButton:
		dragging = event.pressed
		if not event.pressed and run.thinking > 0.0:
			run.done_thinking()
	elif (event is InputEventScreenDrag) or (event is InputEventMouseMotion and dragging):
		target_x += event.relative.x / width * TRACK_WIDTH * 1.4
		target_x = clampf(target_x, -SummandosRun.TRACK_HALF_WIDTH, SummandosRun.TRACK_HALF_WIDTH)


func _notification(what: int) -> void:
	if what == NOTIFICATION_WM_GO_BACK_REQUEST:
		if run != null:
			_open_levels(-1)
		elif levels.visible:
			_show_menu()
		else:
			get_tree().quit()


# --- Frame -------------------------------------------------------------------------------------

func _process(delta: float) -> void:
	elapsed += delta
	if run != null:
		var keys := Input.get_axis("ui_left", "ui_right")
		if keys != 0.0:
			target_x = clampf(target_x + keys * 8.0 * delta, -SummandosRun.TRACK_HALF_WIDTH, SummandosRun.TRACK_HALF_WIDTH)
		if mode == "stress" or OS.get_cmdline_user_args().has("--autosteer"):
			target_x = _autosteer()
		run.steer(target_x)
		run.step(delta)
		think_box.visible = run.thinking > 0.0
		think_label.text = texts.think % ceili(run.thinking)
		_draw_run(delta)
		if run.state != SummandosRun.State.RUNNING and not result.visible:
			_finish()
	fps_label.text = "%d FPS" % Engine.get_frames_per_second()
	if shot_path != "" and elapsed >= shot_after:
		get_viewport().get_texture().get_image().save_png(shot_path)
		shot_path = ""
		get_tree().quit()


## In the performance test the squad steers itself: towards the side whose gate gives more.
func _autosteer() -> float:
	if run.next_row >= run.level.rows.size():
		return 0.0
	var row: Dictionary = run.level.rows[run.next_row]
	var left := _value(row.left)
	var right := _value(row.right)
	return -2.5 if left > right else 2.5


func _value(pick: Dictionary) -> int:
	if pick.has("gate"):
		return (pick.gate as LilloGate).apply(run.soldiers)
	return run.soldiers


func _offsets(count: int) -> PackedVector3Array:
	if not formation_cache.has(count):
		if formation_cache.size() > 64:
			formation_cache.clear()
		formation_cache[count] = LilloFormation.offsets(count, SPACING, TRACK_WIDTH - 1.0)
	return formation_cache[count]


## Enemy waves stand as a loose mob rather than a parade block.
func _enemy_offsets(count: int) -> PackedVector3Array:
	var key := -count
	if not formation_cache.has(key):
		var offsets := LilloFormation.offsets(count, SPACING * 1.1, TRACK_WIDTH - 1.5)
		formation_cache[key] = LilloFormation.scatter(offsets, SPACING * 0.35, count)
	return formation_cache[key]


func _draw_idle() -> void:
	soldiers_view.show_at(Vector3.ZERO, _offsets(12).duplicate(), true)
	enemies_view.show_points(PackedVector3Array())
	bullets_view.show_points(PackedVector3Array())
	boss_view.show_points(PackedVector3Array())
	count_pill.visible = false
	leader.position = Vector3(0, 0.0, 0.6)
	count_label.text = ""
	camera.position = Vector3(0, rig.x, -rig.y)
	camera.look_at(Vector3(0, 0, rig.z))


func _draw_run(delta: float) -> void:
	var squad := Vector3(_world_x(run.x), 0.35, run.z)
	var squad_offsets := _offsets(run.soldiers)
	var behind := PackedVector3Array()
	behind.resize(squad_offsets.size())
	for i in squad_offsets.size():
		var o := squad_offsets[i]
		behind[i] = Vector3(o.x * 0.9, 0.0, -o.z - 0.6)
	soldiers_view.show_at(Vector3(0, 0.0, run.z), _shift(behind, _world_x(run.x)), true)
	leader.position = Vector3(squad.x, 0.0, squad.z + 0.2)
	count_label.position = squad + Vector3(0, 1.9, 0)
	count_pill.position = count_label.position
	count_pill.visible = run.state == SummandosRun.State.RUNNING
	count_label.text = str(run.soldiers) if run.state == SummandosRun.State.RUNNING else ""
	if run.next_row > shown_row:
		shown_row = run.next_row
		var pick := run.last_pick
		var text := (pick.gate as LilloGate).label() if pick.has("gate") else "%s ×%s" % [texts.weapon, str(pick.weapon).trim_suffix(".0")]
		LilloFx.rising_text(count_label, Vector3(0, 0.7, 0), text, palette.weapon, palette.outline)
		LilloFx.pulse(count_label)
	for node in row_nodes:
		node.visible = node.position.z > run.z - 0.5

	var enemy_points := PackedVector3Array()
	var boss_points := PackedVector3Array()
	for wave in run.waves:
		if wave.z - run.z > VISIBLE_AHEAD or enemy_points.size() >= ENEMY_CAPACITY:
			break
		var wave_offsets := _enemy_offsets(wave.count)
		var big: bool = wave.hp >= 50.0
		if big:
			boss_points.append(Vector3(0.0, 0.0, wave.z))
			continue
		for o in wave_offsets:
			if enemy_points.size() >= ENEMY_CAPACITY:
				break
			enemy_points.append(Vector3(o.x, 0.0, wave.z + o.z))
	enemies_view.show_points(enemy_points)
	boss_view.show_points(boss_points)
	_draw_bullets(delta)

	hud_top.text = "%s   ·   %s %d   ·   %s %d%s" % [
		texts.level % level_number if mode == "play" else texts.stress,
		texts.soldiers, run.soldiers, texts.kills, run.kills,
		("   ·   %s ×%s" % [texts.weapon, str(run.damage).trim_suffix(".0")]) if run.damage > 1.0 else "",
	]
	camera.position = Vector3(_world_x(run.x) * 0.35, rig.x, run.z - rig.y)
	camera.look_at(Vector3(_world_x(run.x) * 0.35, 0, run.z + rig.z))


## The camera looks down +z, so the screen's right is the world's -x; the run's x is screen-side.
func _world_x(screen_x: float) -> float:
	return -screen_x


func _shift(points: PackedVector3Array, x: float) -> PackedVector3Array:
	for i in points.size():
		points[i].x = clampf(points[i].x + x, -TRACK_WIDTH * 0.5 + 0.3, TRACK_WIDTH * 0.5 - 0.3)
	return points


func _draw_bullets(delta: float) -> void:
	var wave := run.target()
	if not wave.is_empty() and run.soldiers > 0:
		var shots := mini(8, run.soldiers)
		for i in shots:
			if bullets.size() >= BULLET_CAPACITY:
				break
			var spread := Vector3(randf_range(-2.0, 2.0), 0.6, randf_range(-1.0, 0.5))
			bullets.append(Vector3(_world_x(run.x), 0.0, run.z) + spread)
			bullet_ends.append(wave.z)
	var i := bullets.size() - 1
	while i >= 0:
		bullets[i].z += BULLET_SPEED * delta
		if bullets[i].z >= bullet_ends[i]:
			bullets.remove_at(i)
			bullet_ends.remove_at(i)
		i -= 1
	bullets_view.show_points(bullets)
