## The 3D side of Summandos: sky, track, gates, the squad, enemies, boss and bullets, and the
## chase camera. Draws a SummandosRun each frame; knows nothing about menus.
class_name SummandosWorld
extends Node3D

const SPACING := 0.42
const TRACK_WIDTH := 10.0
const SOLDIER_CAPACITY := 600
const ENEMY_CAPACITY := 700
const BULLET_CAPACITY := 300
const BULLET_SPEED := 45.0
const VISIBLE_AHEAD := 70.0
const BOSS_HEIGHT := 4.5
const GATE_FLOOR_LENGTH := 7.0
const FIGURE_HEIGHT := 0.85
const MODELS := "res://assets/models/"
## Spare kit on the crossbowman model that the squad does not carry.
const ARCHER_SPARES := ["Knife_Offhand", "1H_Crossbow", "Knife", "Throwable"]
const RUN := "Running_A"
const SHOOT := "2H_Ranged_Shooting"

var palette: Dictionary
var texts: Dictionary
var camera: Camera3D
var leader: Node3D
## The leader's AnimationPlayer when it is a model, to switch between running and shooting.
var leader_player: AnimationPlayer
var count_label: Label3D
var count_badge: MeshInstance3D
var soldiers_view: LilloAnimatedCrowd
var enemies_view: LilloAnimatedCrowd
var bullets_view: LilloCrowdView
var boss_view: LilloCrowdView
## The boss as one fully animated model, when the models are there; else boss_view draws it.
var boss_model: Node3D
var row_nodes: Array[Node3D] = []
var formation_cache := {}
var bullets := PackedVector3Array()
var bullet_ends := PackedFloat32Array()
var shown_row := 0
## Gates all in one colour, so the colour never hints which one grows the squad.
var neutral_gates := false
## Camera rig for the current orientation: height, distance behind the squad, look-ahead.
var rig := Vector3(7.5, 8.5, 9.0)


func _init(colours: Dictionary, words: Dictionary) -> void:
	palette = colours
	texts = words


func _ready() -> void:
	_build()
	get_viewport().size_changed.connect(_fit_camera)
	_fit_camera()


## Clears what the last run left and lays out `level`'s gates; `neutral` paints every gate alike.
func reset(level: SummandosLevel, neutral := false) -> void:
	neutral_gates = neutral
	bullets.clear()
	bullet_ends.clear()
	formation_cache.clear()
	shown_row = 0
	for node in row_nodes:
		node.queue_free()
	row_nodes.clear()
	for row in level.rows:
		var node := Node3D.new()
		node.position = Vector3(0, 0, row.z)
		node.add_child(_choice(row.left, -1.0))
		node.add_child(_choice(row.right, 1.0))
		add_child(node)
		row_nodes.append(node)


func _build() -> void:
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

	var ground := MeshInstance3D.new()
	var plane := PlaneMesh.new()
	plane.size = Vector2(TRACK_WIDTH, 2000.0)
	ground.mesh = plane
	ground.material_override = LilloMaterials.bands(palette.track, palette.track_stripe)
	add_child(ground)
	for side in [-1.0, 1.0]:
		var edge := MeshInstance3D.new()
		var box := BoxMesh.new()
		box.size = Vector3(0.3, 0.3, 2000.0)
		edge.mesh = box
		edge.position = Vector3(side * (TRACK_WIDTH * 0.5 + 0.15), 0.15, 0.0)
		edge.material_override = LilloMaterials.flat(palette.edge)
		add_child(edge)

	var outline := Color(palette.outline, 0.9)
	var archer := _model("Rogue_Hooded.glb")
	var minion := _model("Skeleton_Minion.glb")
	if archer and minion:
		var running := LilloPoseBaker.bake(self, archer, RUN, 4, FIGURE_HEIGHT, ARCHER_SPARES)
		var tint := _tinted(running[0], palette.squad.lightened(0.25))
		soldiers_view = LilloAnimatedCrowd.new(running, SOLDIER_CAPACITY, 10.0, tint)
		soldiers_view.add_clip(SHOOT, LilloPoseBaker.bake(self, archer, SHOOT, 4, FIGURE_HEIGHT, ARCHER_SPARES))
		var walking := LilloPoseBaker.bake(self, minion, "Walking_A", 4, FIGURE_HEIGHT)
		enemies_view = LilloAnimatedCrowd.new(walking, ENEMY_CAPACITY, 6.0, _tinted(walking[0], palette.enemy))
		leader = _animated(archer, RUN, 1.2, ARCHER_SPARES, false, palette.squad)
		leader_player = leader.find_children("*", "AnimationPlayer", true, false)[0]
		leader_player.get_animation(SHOOT).loop_mode = Animation.LOOP_LINEAR
	else:
		var soldier: Array[Mesh] = [LilloFigure.build(0.75, palette.squad_dark, palette.skin, palette.squad)]
		soldiers_view = LilloAnimatedCrowd.new(soldier, SOLDIER_CAPACITY, 1.0, LilloFigure.material(outline))
		var enemy: Array[Mesh] = [LilloFigure.build(0.75, palette.enemy, palette.enemy_skin)]
		enemies_view = LilloAnimatedCrowd.new(enemy, ENEMY_CAPACITY, 1.0, LilloFigure.material(outline))
		var figure := MeshInstance3D.new()
		figure.mesh = LilloFigure.build(1.1, palette.squad_dark, palette.skin, palette.squad)
		figure.material_override = LilloFigure.material(outline)
		figure.rotation.y = PI
		leader = Node3D.new()
		leader.add_child(figure)
	add_child(soldiers_view)
	add_child(enemies_view)
	add_child(leader)
	var monster := LilloFigure.monster(BOSS_HEIGHT, palette.boss, palette.boss_belly, palette.boss_horns)
	boss_view = LilloCrowdView.new(monster, LilloFigure.material(outline), 4)
	add_child(boss_view)
	var warrior := _model("Skeleton_Warrior.glb")
	if warrior:
		boss_model = _animated(warrior, "Walking_B", BOSS_HEIGHT, [], true, palette.boss)
		boss_model.visible = false
		add_child(boss_model)
	var bullet_material := LilloMaterials.flat(palette.bullet, false, true)
	bullet_material.vertex_color_use_as_albedo = true
	bullets_view = LilloCrowdView.new(LilloFigure.streak(0.9, 0.035, palette.bullet), bullet_material, BULLET_CAPACITY)
	add_child(bullets_view)

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
	count_badge = MeshInstance3D.new()
	var quad := QuadMesh.new()
	quad.size = Vector2(2.0, 0.95)
	count_badge.mesh = quad
	count_badge.material_override = LilloMaterials.badge(palette.squad_dark)
	add_child(count_badge)

	camera = Camera3D.new()
	add_child(camera)


## The model's own textured material washed with `colour`, so the team reads at a glance.
func _tinted(mesh: Mesh, colour: Color) -> Material:
	var material: StandardMaterial3D = mesh.surface_get_material(0).duplicate()
	material.albedo_color = Color.WHITE.lerp(colour, 0.65)
	material.rim_enabled = true
	material.rim = 0.4
	return material


func _model(file: String) -> PackedScene:
	return load(MODELS + file) if ResourceLoader.exists(MODELS + file) else null


## One animated character playing `animation` in a loop, `height` tall, washed with `tint`;
## `facing_us` turns it to look down -z, towards the squad.
func _animated(scene: PackedScene, animation: String, height: float, spares: Array, facing_us: bool,
		tint: Color) -> Node3D:
	var holder := Node3D.new()
	var body: Node3D = scene.instantiate()
	holder.add_child(body)
	for name in spares:
		var part := body.find_child(name, true, false)
		if part:
			part.queue_free()
	for part in body.find_children("*", "MeshInstance3D", true, false):
		if not spares.has(part.name) and part.mesh:
			part.material_override = _tinted(part.mesh, tint)
	var player: AnimationPlayer = body.find_children("*", "AnimationPlayer", true, false)[0]
	player.get_animation(animation).loop_mode = Animation.LOOP_LINEAR
	player.play(animation)
	body.scale = Vector3.ONE * (height / 2.4)
	body.rotation.y = PI if facing_us else 0.0
	return holder


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


func _label(text: String, font_size: int, outline: int, height: float) -> Label3D:
	var label := Label3D.new()
	label.text = text
	label.font_size = font_size
	label.outline_size = outline
	label.pixel_size = 0.01
	label.rotation.y = PI
	label.position.y = height
	return label


func _choice(pick: Dictionary, side: float) -> Node3D:
	var node := Node3D.new()
	node.position.x = world_x(side * TRACK_WIDTH * 0.25)
	if pick.has("gate"):
		var gate: LilloGate = pick.gate
		var good := gate.apply(100) >= 100
		var panel := MeshInstance3D.new()
		var quad := QuadMesh.new()
		quad.size = Vector2(TRACK_WIDTH * 0.5 - 0.3, 2.4)
		panel.mesh = quad
		panel.position.y = 1.2
		var gate_colour: Color = palette.neutral_gate if neutral_gates else (palette.good_gate if good else palette.bad_gate)
		var floor_colour: Color = palette.neutral_floor if neutral_gates else (palette.good_floor if good else palette.bad_floor)
		panel.material_override = LilloMaterials.flat(gate_colour, true)
		node.add_child(panel)
		var floor := MeshInstance3D.new()
		var floor_plane := PlaneMesh.new()
		floor_plane.size = Vector2(TRACK_WIDTH * 0.5 - 0.15, GATE_FLOOR_LENGTH)
		floor.mesh = floor_plane
		floor.position = Vector3(0, 0.01, -GATE_FLOOR_LENGTH * 0.5)
		floor.material_override = LilloMaterials.flat(floor_colour)
		node.add_child(floor)
		var text := gate.label()
		var fitted := clampi(int(850.0 / maxf(1.0, text.length())), 90, 200)
		node.add_child(_label(text, fitted, int(fitted * 0.2), 1.3))
		if gate.note != "":
			node.add_child(_label(gate.note, 110, 28, 0.35))
	else:
		var crate := MeshInstance3D.new()
		var box := BoxMesh.new()
		box.size = Vector3(1.4, 0.8, 0.8)
		crate.mesh = box
		crate.position.y = 0.4
		crate.material_override = LilloMaterials.flat(palette.weapon, false, true)
		node.add_child(crate)
		var label := _label(weapon_text(pick.weapon), 96, 40, 1.3)
		label.modulate = palette.weapon
		node.add_child(label)
	return node


func weapon_text(multiplier: float) -> String:
	return "%s ×%s" % [texts.weapon, str(multiplier).trim_suffix(".0")]


## The camera looks down +z, so the screen's right is the world's -x; the run's x is screen-side.
static func world_x(screen_x: float) -> float:
	return -screen_x


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


## A small squad standing at the start, behind the menus.
func draw_idle() -> void:
	soldiers_view.play("default")
	soldiers_view.show_at(Vector3.ZERO, _offsets(12).duplicate(), true)
	enemies_view.show_points(PackedVector3Array())
	bullets_view.show_points(PackedVector3Array())
	boss_view.show_points(PackedVector3Array())
	if boss_model:
		boss_model.visible = false
	count_badge.visible = false
	leader.position = Vector3(0, 0.0, 0.6)
	count_label.text = ""
	for node in row_nodes:
		node.visible = false
	camera.position = Vector3(0, rig.x, -rig.y)
	camera.look_at(Vector3(0, 0, rig.z))


func draw_run(run: SummandosRun, delta: float) -> void:
	var x := world_x(run.x)
	var squad := Vector3(x, 0.35, run.z)
	var squad_offsets := _offsets(run.soldiers)
	var behind := PackedVector3Array()
	behind.resize(squad_offsets.size())
	for i in squad_offsets.size():
		var o := squad_offsets[i]
		behind[i] = Vector3(clampf(o.x * 0.9 + x, -TRACK_WIDTH * 0.5 + 0.3, TRACK_WIDTH * 0.5 - 0.3), 0.0, -o.z - 0.6)
	var shooting := not run.target().is_empty() and run.soldiers > 0
	soldiers_view.play(SHOOT if shooting else "default")
	if leader_player and leader_player.current_animation != (SHOOT if shooting else RUN):
		leader_player.play(SHOOT if shooting else RUN, 0.2)
	soldiers_view.show_at(Vector3(0, 0.0, run.z), behind, true)
	leader.position = Vector3(x, 0.0, run.z + 0.2)
	var running := run.state == SummandosRun.State.RUNNING
	count_label.position = squad + Vector3(0, 1.9, 0)
	count_badge.position = count_label.position
	count_badge.visible = running
	count_label.text = str(run.soldiers) if running else ""
	if run.next_row > shown_row:
		shown_row = run.next_row
		var pick := run.last_pick
		var text := (pick.gate as LilloGate).label() if pick.has("gate") else weapon_text(pick.weapon)
		LilloFx.rising_text(count_label, Vector3(0, 0.7, 0), text, palette.weapon, palette.outline)
		LilloFx.pulse(count_label)
	for node in row_nodes:
		node.visible = node.position.z > run.z - 0.5

	var enemy_points := PackedVector3Array()
	var boss_points := PackedVector3Array()
	for wave in run.waves:
		if wave.z - run.z > VISIBLE_AHEAD or enemy_points.size() >= ENEMY_CAPACITY:
			break
		if wave.hp >= 50.0:
			boss_points.append(Vector3(0.0, 0.0, wave.z))
			continue
		for o in _enemy_offsets(wave.count):
			if enemy_points.size() >= ENEMY_CAPACITY:
				break
			enemy_points.append(Vector3(o.x, 0.0, wave.z + o.z))
	enemies_view.show_points(enemy_points)
	if boss_model:
		boss_model.visible = not boss_points.is_empty()
		if boss_model.visible:
			boss_model.position = boss_points[0]
	else:
		boss_view.show_points(boss_points)
	_draw_bullets(run, delta)
	camera.position = Vector3(x * 0.35, rig.x, run.z - rig.y)
	camera.look_at(Vector3(x * 0.35, 0, run.z + rig.z))


func _draw_bullets(run: SummandosRun, delta: float) -> void:
	var wave := run.target()
	if not wave.is_empty() and run.soldiers > 0:
		for i in mini(8, run.soldiers):
			if bullets.size() >= BULLET_CAPACITY:
				break
			var spread := Vector3(randf_range(-2.0, 2.0), 0.6, randf_range(-1.0, 0.5))
			bullets.append(Vector3(world_x(run.x), 0.0, run.z) + spread)
			bullet_ends.append(wave.z)
	var i := bullets.size() - 1
	while i >= 0:
		bullets[i].z += BULLET_SPEED * delta
		if bullets[i].z >= bullet_ends[i]:
			bullets.remove_at(i)
			bullet_ends.remove_at(i)
		i -= 1
	bullets_view.show_points(bullets)
