## Summandos' scene: builds the track, the squad, the enemies and the HUD in code, and draws a
## SummandosRun every frame. Command-line options after `--`: `--mode=play|stress` starts a run
## at once, `--shot=<png>` saves a screenshot after `--after=<seconds>` (default 3) and quits.
extends Node3D

const SPACING := 0.42
const TRACK_WIDTH := 10.0
const SOLDIER_CAPACITY := 600
const ENEMY_CAPACITY := 700
const BULLET_CAPACITY := 300
const BULLET_SPEED := 45.0
const VISIBLE_AHEAD := 70.0

const BLUE := Color(0.22, 0.5, 1.0)
const RED := Color(0.95, 0.27, 0.25)
const GOOD_GATE := Color(0.2, 0.55, 1.0, 0.45)
const BAD_GATE := Color(1.0, 0.3, 0.3, 0.45)
const WEAPON_COLOUR := Color(1.0, 0.75, 0.15)

var texts := {}
var run: SummandosRun
var mode := "play"
var target_x := 0.0
var dragging := false

var camera: Camera3D
var ground: MeshInstance3D
var leader: MeshInstance3D
var count_label: Label3D
var soldiers_view: LilloCrowdView
var enemies_view: LilloCrowdView
var bullets_view: LilloCrowdView
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

var shot_path := ""
## Camera rig for the current orientation: height, distance behind the squad, look-ahead.
var rig := Vector3(7.5, 8.5, 9.0)
var shot_after := 3.0
var elapsed := 0.0


func _ready() -> void:
	texts = _texts(OS.get_locale_language())
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
		elif arg.begins_with("--after="):
			shot_after = float(arg.trim_prefix("--after="))
	if start_now:
		_start(mode)
	else:
		_show_menu()


func _texts(language: String) -> Dictionary:
	if language == "es":
		return {
			"title": "Sumandos", "play": "Jugar", "stress": "Prueba de rendimiento (1.000)",
			"won": "¡Victoria!", "lost": "Derrota", "again": "Otra vez", "menu": "Menú",
			"kills": "Bajas", "weapon": "ARMA", "soldiers": "Soldados",
			"detail": "%d soldados en pie · %d bajas enemigas",
		}
	return {
		"title": "Summandos", "play": "Play", "stress": "Performance test (1,000)",
		"won": "Victory!", "lost": "Defeat", "again": "Again", "menu": "Menu",
		"kills": "Kills", "weapon": "WEAPON", "soldiers": "Soldiers",
		"detail": "%d soldiers standing · %d enemies down",
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


func _build_world() -> void:
	var environment := WorldEnvironment.new()
	var env := Environment.new()
	env.background_mode = Environment.BG_COLOR
	env.background_color = Color(0.53, 0.75, 0.95)
	env.ambient_light_source = Environment.AMBIENT_SOURCE_COLOR
	env.ambient_light_color = Color(0.85, 0.88, 0.95)
	env.ambient_light_energy = 0.6
	environment.environment = env
	add_child(environment)

	var sun := DirectionalLight3D.new()
	sun.rotation_degrees = Vector3(-55, -30, 0)
	sun.light_energy = 1.1
	add_child(sun)

	ground = MeshInstance3D.new()
	var plane := PlaneMesh.new()
	plane.size = Vector2(TRACK_WIDTH, 2000.0)
	ground.mesh = plane
	ground.material_override = _material(Color(0.78, 0.8, 0.84))
	add_child(ground)
	for side in [-1.0, 1.0]:
		var edge := MeshInstance3D.new()
		var box := BoxMesh.new()
		box.size = Vector3(0.3, 0.3, 2000.0)
		edge.mesh = box
		edge.position = Vector3(side * (TRACK_WIDTH * 0.5 + 0.15), 0.15, 0.0)
		edge.material_override = _material(Color(0.35, 0.38, 0.45))
		add_child(edge)

	var soldier := CapsuleMesh.new()
	soldier.radius = 0.16
	soldier.height = 0.7
	soldiers_view = LilloCrowdView.new(soldier, _material(BLUE), SOLDIER_CAPACITY)
	add_child(soldiers_view)
	enemies_view = LilloCrowdView.new(soldier, _material(RED), ENEMY_CAPACITY)
	add_child(enemies_view)
	var bullet := SphereMesh.new()
	bullet.radius = 0.06
	bullet.height = 0.12
	bullets_view = LilloCrowdView.new(bullet, _material(Color(1.0, 0.9, 0.3), false, true), BULLET_CAPACITY)
	add_child(bullets_view)

	leader = MeshInstance3D.new()
	var leader_mesh := CapsuleMesh.new()
	leader_mesh.radius = 0.24
	leader_mesh.height = 1.0
	leader.mesh = leader_mesh
	leader.material_override = _material(Color(0.1, 0.3, 0.85))
	add_child(leader)

	count_label = Label3D.new()
	count_label.billboard = BaseMaterial3D.BILLBOARD_ENABLED
	count_label.font_size = 96
	count_label.outline_size = 24
	count_label.pixel_size = 0.01
	count_label.no_depth_test = true
	add_child(count_label)

	camera = Camera3D.new()
	add_child(camera)


func _fit_camera() -> void:
	var size := get_viewport().get_visible_rect().size
	if size.y > size.x:
		camera.keep_aspect = Camera3D.KEEP_WIDTH
		camera.fov = 62.0
		rig = Vector3(8.5, 7.0, 12.0)
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
	label.font_size = 140
	label.outline_size = 28
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
		panel.material_override = _material(GOOD_GATE if good else BAD_GATE, true)
		node.add_child(panel)
		label.text = gate.label()
	else:
		var crate := MeshInstance3D.new()
		var box := BoxMesh.new()
		box.size = Vector3(1.4, 0.8, 0.8)
		crate.mesh = box
		crate.position.y = 0.4
		crate.material_override = _material(WEAPON_COLOUR, false, true)
		node.add_child(crate)
		label.text = "%s ×%s" % [texts.weapon, str(pick.weapon).trim_suffix(".0")]
		label.font_size = 96
		label.modulate = WEAPON_COLOUR
	node.add_child(label)
	return node


# --- UI ----------------------------------------------------------------------------------------

func _button(text: String, action: Callable) -> Button:
	var button := Button.new()
	button.text = text
	button.custom_minimum_size = Vector2(0, 72)
	button.add_theme_font_size_override("font_size", 30)
	button.pressed.connect(action)
	return button


func _centre_box(parent: Control) -> VBoxContainer:
	var centre := CenterContainer.new()
	centre.set_anchors_preset(Control.PRESET_FULL_RECT)
	parent.add_child(centre)
	var box := VBoxContainer.new()
	box.custom_minimum_size = Vector2(minf(420.0, get_viewport().get_visible_rect().size.x - 48.0), 0)
	box.add_theme_constant_override("separation", 18)
	centre.add_child(box)
	return box


func _title(text: String, size: int) -> Label:
	var label := Label.new()
	label.text = text
	label.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	label.add_theme_font_size_override("font_size", size)
	label.add_theme_constant_override("outline_size", 12)
	label.add_theme_color_override("font_outline_color", Color(0.05, 0.1, 0.25))
	return label


func _build_ui() -> void:
	var layer := CanvasLayer.new()
	add_child(layer)
	var root := MarginContainer.new()
	root.set_anchors_preset(Control.PRESET_FULL_RECT)
	layer.add_child(root)
	root.set_meta("safe", true)
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

	menu = Control.new()
	menu.set_anchors_preset(Control.PRESET_FULL_RECT)
	root.add_child(menu)
	var menu_box := _centre_box(menu)
	menu_box.add_child(_title(texts.title, 84))
	menu_box.add_child(_button(texts.play, _start.bind("play")))
	menu_box.add_child(_button(texts.stress, _start.bind("stress")))

	result = Control.new()
	result.set_anchors_preset(Control.PRESET_FULL_RECT)
	root.add_child(result)
	var result_box := _centre_box(result)
	result_title = _title("", 72)
	result_box.add_child(result_title)
	result_detail = _title("", 28)
	result_detail.autowrap_mode = TextServer.AUTOWRAP_WORD_SMART
	result_box.add_child(result_detail)
	result_box.add_child(_button(texts.again, func() -> void: _start(mode)))
	result_box.add_child(_button(texts.menu, _show_menu))


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


func _show_menu() -> void:
	run = null
	menu.visible = true
	result.visible = false
	hud.visible = false
	_draw_idle()


func _start(run_mode: String) -> void:
	mode = run_mode
	run = SummandosRun.new(SummandosLevel.stress() if mode == "stress" else SummandosLevel.first())
	target_x = 0.0
	bullets.clear()
	bullet_ends.clear()
	formation_cache.clear()
	_build_rows()
	menu.visible = false
	result.visible = false
	hud.visible = true


func _finish() -> void:
	result.visible = true
	hud.visible = false
	var won := run.state == SummandosRun.State.WON
	result_title.text = texts.won if won else texts.lost
	result_detail.text = texts.detail % [run.soldiers, run.kills]


# --- Input -------------------------------------------------------------------------------------

func _unhandled_input(event: InputEvent) -> void:
	if run == null:
		return
	var width := get_viewport().get_visible_rect().size.x
	if event is InputEventScreenTouch or event is InputEventMouseButton:
		dragging = event.pressed
	elif (event is InputEventScreenDrag) or (event is InputEventMouseMotion and dragging):
		target_x += event.relative.x / width * TRACK_WIDTH * 1.4
		target_x = clampf(target_x, -SummandosRun.TRACK_HALF_WIDTH, SummandosRun.TRACK_HALF_WIDTH)


func _notification(what: int) -> void:
	if what == NOTIFICATION_WM_GO_BACK_REQUEST:
		if run != null:
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


func _draw_idle() -> void:
	soldiers_view.show_at(Vector3.ZERO, _offsets(12).duplicate())
	enemies_view.show_points(PackedVector3Array())
	bullets_view.show_points(PackedVector3Array())
	leader.position = Vector3(0, 0.5, 0.6)
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
	soldiers_view.show_at(Vector3(0, 0.35, run.z), _shift(behind, _world_x(run.x)))
	leader.position = squad + Vector3(0, 0.15, 0.2)
	count_label.position = squad + Vector3(0, 1.6, 0)
	count_label.text = str(run.soldiers) if run.state == SummandosRun.State.RUNNING else ""
	for node in row_nodes:
		node.visible = node.position.z > run.z - 0.5
	count_label.modulate = Color.WHITE

	var enemy_points := PackedVector3Array()
	for wave in run.waves:
		if wave.z - run.z > VISIBLE_AHEAD or enemy_points.size() >= ENEMY_CAPACITY:
			break
		var wave_offsets := _offsets(wave.count)
		var big: bool = wave.hp >= 50.0
		for o in wave_offsets:
			if enemy_points.size() >= ENEMY_CAPACITY:
				break
			enemy_points.append(Vector3(o.x, 0.35 if not big else 1.2, wave.z + o.z))
	enemies_view.show_points(enemy_points)
	_draw_bullets(delta)

	hud_top.text = "%s %d   ·   %s %d%s" % [
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
