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
const LABEL_PIXEL := 0.01
## How wide a gate's text may draw, in metres: the panel less a margin each side.
const GATE_TEXT_WIDTH := TRACK_WIDTH * 0.5 - 0.6
## The smallest font a gate's text shrinks to; the tests check every generated gate fits above it.
const MIN_GATE_FONT := 60
## How long the weapon on a crate is, and how high its name floats above it, in metres.
const WEAPON_LENGTH := 2.4
const WEAPON_LABEL_HEIGHT := 2.7
const FIGURE_HEIGHT := 0.85
const RUN := "Running_A"
const SHOOT := "2H_Ranged_Shooting"
## The boss's blow; models without it (the bought Golem and Necromancer) only lunge.
const BOSS_ATTACK := "2H_Melee_Attack_Chop"
## How long a boss's lunge lasts and how far it reaches, and how long the camera shakes after it.
const LUNGE_SECONDS := 0.4
const LUNGE_REACH := 1.2
const SHAKE_SECONDS := 0.3

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
## The mage's fireballs in flight, and a red ring where each will land.
var fireballs_view: LilloCrowdView
var landing_view: LilloCrowdView
var boss_view: LilloCrowdView
## The boss as one fully animated model, when the models are there; else boss_view draws it.
var boss_model: Node3D
var boss_player: AnimationPlayer
var boss_walk := ""
var seen_strikes := 0
## Seconds left of the boss's lunge and of the camera shake after a blow.
var lunge := 0.0
var shake := 0.0
var row_nodes: Array[Node3D] = []
var formation_cache := {}
var bullets := PackedVector3Array()
var bullet_ends := PackedFloat32Array()
## Sideways distance each bolt covers on its way, so it flies to the wave's lane.
var bullet_drift := PackedFloat32Array()
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
	_dress_boss(level)
	seen_strikes = 0
	lunge = 0.0
	shake = 0.0
	bullets.clear()
	bullet_ends.clear()
	bullet_drift.clear()
	formation_cache.clear()
	shown_row = 0
	for node in row_nodes:
		node.queue_free()
	row_nodes.clear()
	for row in level.rows:
		var node := Node3D.new()
		node.position = Vector3(0, 0, row.z)
		var left := _choice(row.left, -1.0)
		var right := _choice(row.right, 1.0)
		node.add_child(left)
		node.add_child(right)
		node.set_meta("sides", [left, right])
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
	var archer := SummandosModels.scene("Rogue_Hooded.glb")
	var minion := SummandosModels.scene("Skeleton_Minion.glb")
	var spares := SummandosModels.ARCHER_SPARES
	if archer and minion:
		var running := LilloPoseBaker.bake(self, archer, RUN, 4, FIGURE_HEIGHT, spares)
		var tint := SummandosModels.team(running[0], palette.squad)
		soldiers_view = LilloAnimatedCrowd.new(running, SOLDIER_CAPACITY, 10.0, tint)
		soldiers_view.add_clip(SHOOT, LilloPoseBaker.bake(self, archer, SHOOT, 4, FIGURE_HEIGHT, spares))
		var walking := LilloPoseBaker.bake(self, minion, "Walking_A", 4, FIGURE_HEIGHT)
		enemies_view = LilloAnimatedCrowd.new(walking, ENEMY_CAPACITY, 6.0, SummandosModels.team(walking[0], palette.enemy))
		leader = SummandosModels.character("Rogue_Hooded.glb", RUN, 1.2, palette.squad, false, spares)
		leader_player = leader.get_meta("player")
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
	var bullet_material := LilloMaterials.flat(palette.bullet, false, true)
	bullet_material.vertex_color_use_as_albedo = true
	bullets_view = LilloCrowdView.new(LilloFigure.streak(0.9, 0.035, palette.bullet), bullet_material, BULLET_CAPACITY)
	add_child(bullets_view)
	var ball := SphereMesh.new()
	ball.radius = 0.7
	ball.height = 1.4
	fireballs_view = LilloCrowdView.new(ball, LilloMaterials.flat(Color("#FF7A1A"), false, true), 8)
	add_child(fireballs_view)
	var ring := CylinderMesh.new()
	ring.top_radius = SummandosRun.FIREBALL_RADIUS
	ring.bottom_radius = SummandosRun.FIREBALL_RADIUS
	ring.height = 0.02
	landing_view = LilloCrowdView.new(ring, LilloMaterials.flat(Color(0.95, 0.15, 0.05, 0.55), true), 8)
	add_child(landing_view)

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


## Puts this level's boss on the track: its skeleton model, colour and size (SummandosLevel.boss),
## or the default look for a level that does not say. Without models boss_view draws the monster.
func _dress_boss(level: SummandosLevel) -> void:
	if boss_model:
		boss_model.queue_free()
		boss_model = null
	var look := level.boss if not level.boss.is_empty() else {"model": "Skeleton_Warrior.glb", "colour": 0, "height": BOSS_HEIGHT}
	var colour: Color = SummandosPalette.BOSS_COLOURS[look.colour]
	boss_model = SummandosModels.character(look.model, "Walking_B", look.height, colour, true)
	boss_player = null
	if boss_model:
		boss_model.visible = false
		add_child(boss_model)
		boss_player = boss_model.get_meta("player")
		boss_walk = boss_player.current_animation
		if boss_player.has_animation(BOSS_ATTACK):
			boss_player.get_animation(BOSS_ATTACK).loop_mode = Animation.LOOP_NONE


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
	label.pixel_size = LABEL_PIXEL
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
		node.set_meta("panel", panel)
		node.add_child(panel)
		var floor := MeshInstance3D.new()
		var floor_plane := PlaneMesh.new()
		floor_plane.size = Vector2(TRACK_WIDTH * 0.5 - 0.15, GATE_FLOOR_LENGTH)
		floor.mesh = floor_plane
		floor.position = Vector3(0, 0.01, -GATE_FLOOR_LENGTH * 0.5)
		floor.material_override = LilloMaterials.flat(floor_colour)
		node.add_child(floor)
		var text := gate.label()
		var fitted := fit_font_size(text, 200, GATE_TEXT_WIDTH)
		node.add_child(_label(text, fitted, int(fitted * 0.2), 1.3))
		if gate.note != "":
			var note_size := fit_font_size(gate.note, 110, GATE_TEXT_WIDTH)
			node.add_child(_label(gate.note, note_size, int(note_size * 0.2), 0.35))
	else:
		var crate := MeshInstance3D.new()
		var box := BoxMesh.new()
		box.size = Vector3(2.2, 0.9, 1.0)
		crate.mesh = box
		crate.position.y = 0.4
		crate.material_override = LilloMaterials.flat(palette.weapon, false, true)
		node.add_child(crate)
		var shown := SummandosModels.weapon(pick.weapon, WEAPON_LENGTH)
		if shown:
			shown.position.y = 0.95
			node.add_child(shown)
		var weapon_size := fit_font_size(weapon_text(pick.weapon), 110, GATE_TEXT_WIDTH)
		var label := _label(weapon_text(pick.weapon), weapon_size, int(weapon_size * 0.2),
			WEAPON_LABEL_HEIGHT if shown else 1.3)
		label.modulate = palette.weapon
		node.add_child(label)
	return node


## The weapon a multiplier stands for, by name: pistols ×2, a bazooka ×3.
func weapon_text(multiplier: float) -> String:
	var name: String = texts.weapons.get(int(multiplier), texts.weapon)
	return "%s ×%s" % [name, str(multiplier).trim_suffix(".0")]


## The largest font size, up to `largest`, at which `text` (outline included) is at most `width`
## metres wide on a gate's Label3D; never below MIN_GATE_FONT.
static func fit_font_size(text: String, largest: int, width: float) -> int:
	var size := largest
	while size > MIN_GATE_FONT and text_width(text, size) > width:
		size -= 2
	return size


## How wide `text` draws on a gate's Label3D at `font_size`, outline included, in metres.
static func text_width(text: String, font_size: int) -> float:
	var outline := font_size * 0.2
	var drawn := ThemeDB.fallback_font.get_string_size(text, HORIZONTAL_ALIGNMENT_LEFT, -1, font_size).x
	return (drawn + outline) * LABEL_PIXEL


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
	fireballs_view.show_points(PackedVector3Array())
	landing_view.show_points(PackedVector3Array())
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
	_highlight(run)

	var enemy_points := PackedVector3Array()
	var boss_points := PackedVector3Array()
	for wave in run.waves:
		if wave.z - run.z > VISIBLE_AHEAD or enemy_points.size() >= ENEMY_CAPACITY:
			break
		if wave.boss:
			boss_points.append(Vector3(world_x(wave.x), 0.0, wave.z))
			continue
		for o in _enemy_offsets(wave.count):
			if enemy_points.size() >= ENEMY_CAPACITY:
				break
			var ex := clampf(world_x(wave.x) + o.x * 0.6, -TRACK_WIDTH * 0.5 + 0.3, TRACK_WIDTH * 0.5 - 0.3)
			enemy_points.append(Vector3(ex, 0.0, wave.z + o.z))
	enemies_view.show_points(enemy_points)
	if run.strikes > seen_strikes:
		seen_strikes = run.strikes
		_boss_strikes(run.last_strike)
	lunge = maxf(0.0, lunge - delta)
	shake = maxf(0.0, shake - delta)
	var reach := Vector3(0, 0, -LUNGE_REACH * sin(PI * (1.0 - lunge / LUNGE_SECONDS))) if lunge > 0.0 else Vector3.ZERO
	if boss_model:
		boss_model.visible = not boss_points.is_empty()
		if boss_model.visible:
			boss_model.position = boss_points[0] + reach
	else:
		if not boss_points.is_empty():
			boss_points[0] += reach
		boss_view.show_points(boss_points)
	_draw_bullets(run, delta)
	_draw_fireballs(run)
	var jolt := Vector3(randf_range(-1.0, 1.0), randf_range(-1.0, 1.0), 0.0) * 0.15 * shake / SHAKE_SECONDS
	camera.position = Vector3(x * 0.35, rig.x, run.z - rig.y) + jolt
	camera.look_at(Vector3(x * 0.35, 0, run.z + rig.z) + jolt)


## A boss's blow: it lunges (swinging its weapon when the model has the clip), the camera shakes
## and what it ate rises over the squad.
func _boss_strikes(eaten: int) -> void:
	lunge = LUNGE_SECONDS
	shake = SHAKE_SECONDS
	if boss_player and boss_player.has_animation(BOSS_ATTACK):
		boss_player.play(BOSS_ATTACK, 0.1, 1.6)
		if boss_walk != "":
			boss_player.queue(boss_walk)
	if eaten > 0:
		LilloFx.rising_text(count_label, Vector3(0, 0.7, 0), "−%d" % eaten, palette.hurt, palette.outline)
		LilloFx.pulse(count_label)


## Lights the gate the squad is heading through and dims the other, so the choice is never a
## surprise (a squad dead centre takes the right gate).
func _highlight(run: SummandosRun) -> void:
	if run.next_row >= row_nodes.size():
		return
	var node := row_nodes[run.next_row]
	var near := clampf(1.0 - (node.position.z - run.z) / 25.0, 0.0, 1.0)
	var taken := 0 if run.x < 0.0 else 1
	var sides: Array = node.get_meta("sides")
	for i in 2:
		var side: Node3D = sides[i]
		var chosen := i == taken
		side.scale = Vector3.ONE * (1.0 + (0.12 * near if chosen else 0.0))
		if side.has_meta("panel"):
			var material: StandardMaterial3D = (side.get_meta("panel") as MeshInstance3D).material_override
			material.albedo_color.a = lerpf(0.45, 0.8 if chosen else 0.2, near)


## Each fireball arcs from the boss to where the squad stood when it was thrown, over a red ring
## marking the spot, so the player sees where to move out of.
func _draw_fireballs(run: SummandosRun) -> void:
	var balls := PackedVector3Array()
	var rings := PackedVector3Array()
	for ball in run.fireballs:
		var t: float = 1.0 - ball.left / SummandosRun.FIREBALL_FLIGHT
		var start := Vector3(world_x(ball.from_x), 3.0, ball.from_z)
		var end := Vector3(world_x(ball.x), 0.3, run.z)
		var at := start.lerp(end, t)
		at.y += sin(t * PI) * 3.0
		balls.append(at)
		rings.append(Vector3(end.x, 0.03, run.z))
	fireballs_view.show_points(balls)
	landing_view.show_points(rings)


func _draw_bullets(run: SummandosRun, delta: float) -> void:
	var wave := run.target()
	if not wave.is_empty() and run.soldiers > 0:
		var lane := world_x(wave.x) - world_x(run.x)
		for i in mini(8, run.soldiers):
			if bullets.size() >= BULLET_CAPACITY:
				break
			var spread := Vector3(randf_range(-1.5, 1.5), 0.6, randf_range(-1.0, 0.5))
			bullets.append(Vector3(world_x(run.x), 0.0, run.z) + spread)
			bullet_ends.append(wave.z)
			bullet_drift.append(lane)
	var i := bullets.size() - 1
	while i >= 0:
		var flight := maxf(1.0, bullet_ends[i] - run.z)
		bullets[i].z += BULLET_SPEED * delta
		bullets[i].x += bullet_drift[i] * BULLET_SPEED * delta / flight
		if bullets[i].z >= bullet_ends[i]:
			bullets.remove_at(i)
			bullet_ends.remove_at(i)
			bullet_drift.remove_at(i)
		i -= 1
	# Pistols fire thicker bolts and a bazooka thicker still, so the weapon shows.
	var thickness := PackedFloat32Array()
	thickness.resize(bullets.size())
	thickness.fill(1.0 + (run.damage - 1.0) * 0.6)
	bullets_view.show_points(bullets, thickness)
