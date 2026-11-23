## Summandos' entry point: wires the run (game/), the 3D world and the screens (view/) together and
## handles input and the back button. Options after `--`: `--mode=play|stress` starts a run at
## once (`--difficulty=easy|normal|adult --level=<n>` pick it, `--autosteer` takes the better gate),
## `--levels` opens the level map, `--about` the About card, `--palette=sunset|night` overrides the saved choice, and
## LilloCapture's `--shot=<png> --after=<s>` saves a screenshot and quits.
extends Node3D

const MODES := ["easy", "normal", "adult"]

var texts := SummandosTexts.for_language(OS.get_locale_language())
var palette := SummandosPalette.named("night" if LilloSettings.read("night", false) else "sunset")
var difficulty: SummandosLevel.Mode = LilloSettings.read("difficulty", SummandosLevel.Mode.EASY)
var level_number := 1
var mode := "play"
var run: SummandosRun
var target_x := 0.0
var dragging := false
var world: SummandosWorld
var ui: SummandosUi


func _ready() -> void:
	var args := OS.get_cmdline_user_args()
	var start := ""
	for arg in args:
		if arg.begins_with("--palette="):
			palette = SummandosPalette.named(arg.trim_prefix("--palette="))
		elif arg.begins_with("--mode="):
			start = arg.trim_prefix("--mode=")
		elif arg.begins_with("--difficulty="):
			difficulty = maxi(0, MODES.find(arg.trim_prefix("--difficulty="))) as SummandosLevel.Mode
		elif arg.begins_with("--level="):
			level_number = int(arg.trim_prefix("--level="))
	world = SummandosWorld.new(palette, texts)
	add_child(world)
	var map := LilloLevelMap.new(SummandosLevel.LEVELS_PER_MODE,
		func(n: int) -> int: return SummandosProgress.stars(difficulty, n),
		func(n: int) -> bool: return SummandosProgress.unlocked(difficulty, n),
		{"tile": palette.squad, "tile_lip": palette.squad_dark, "tab": Color(palette.outline, 0.7),
			"star": palette.weapon, "arrow": palette.squad, "arrow_lip": palette.squad_dark})
	ui = SummandosUi.new(palette, texts, map)
	add_child(ui)
	add_child(LilloCapture.new())
	ui.mode_picked.connect(_open_levels)
	ui.level_picked.connect(func(number: int) -> void: _start("play", number))
	ui.stress_picked.connect(_start.bind("stress"))
	ui.night_toggled.connect(_toggle_night)
	ui.next_picked.connect(func() -> void: _start("play", level_number + 1))
	ui.again_picked.connect(func() -> void: _start(mode, level_number))
	ui.levels_picked.connect(_open_levels.bind(-1))
	ui.menu_picked.connect(_show_menu)
	if start != "":
		_start(start, level_number)
	elif args.has("--levels"):
		_open_levels(-1)
	elif args.has("--about"):
		ui.show_screen("about")
	else:
		_show_menu()


func _show_menu() -> void:
	run = null
	world.draw_idle()
	ui.show_screen("menu")


## Opens the level map of `picked` mode (-1 keeps the current one).
func _open_levels(picked: int) -> void:
	if picked >= 0:
		difficulty = picked as SummandosLevel.Mode
		LilloSettings.write("difficulty", picked)
	run = null
	world.draw_idle()
	ui.show_levels(difficulty)


func _toggle_night() -> void:
	LilloSettings.write("night", not LilloSettings.read("night", false))
	get_tree().reload_current_scene()


func _start(run_mode: String, number := level_number) -> void:
	mode = run_mode
	level_number = clampi(number, 1, SummandosLevel.LEVELS_PER_MODE)
	var level := SummandosLevel.stress() if mode == "stress" else SummandosLevel.generate(difficulty, level_number)
	run = SummandosRun.new(level)
	target_x = 0.0
	world.reset(level, mode == "play" and difficulty == SummandosLevel.Mode.ADULT)
	ui.show_screen("run")


func _finish() -> void:
	var won := run.state == SummandosRun.State.WON
	var playing := mode == "play"
	var stars := run.stars() if playing else 0
	if playing:
		SummandosProgress.record(difficulty, level_number, stars)
	ui.show_result(won, run.soldiers, run.kills, stars,
		won and playing and level_number < SummandosLevel.LEVELS_PER_MODE)


func _unhandled_input(event: InputEvent) -> void:
	if run == null:
		return
	var width := get_viewport().get_visible_rect().size.x
	if event is InputEventScreenTouch or event is InputEventMouseButton:
		dragging = event.pressed
		if not event.pressed and run.thinking > 0.0:
			run.done_thinking()
	elif (event is InputEventScreenDrag) or (event is InputEventMouseMotion and dragging):
		target_x = _clamp_x(target_x + event.relative.x / width * SummandosWorld.TRACK_WIDTH * 1.4)


func _notification(what: int) -> void:
	if what != NOTIFICATION_WM_GO_BACK_REQUEST:
		return
	if run != null:
		_open_levels(-1)
	elif ui.level_map.visible or ui.about.visible:
		_show_menu()
	else:
		get_tree().quit()


func _process(delta: float) -> void:
	if run == null:
		return
	var keys := Input.get_axis("ui_left", "ui_right")
	if keys != 0.0:
		target_x = _clamp_x(target_x + keys * 8.0 * delta)
	if mode == "stress" or OS.get_cmdline_user_args().has("--autosteer"):
		target_x = _autosteer()
	run.steer(target_x)
	run.step(delta)
	world.draw_run(run, delta)
	ui.update_hud(_hud_text(), run.thinking)
	if run.state != SummandosRun.State.RUNNING and not ui.result.visible:
		_finish()


func _hud_text() -> String:
	return "%s   ·   %s %d   ·   %s %d%s" % [
		texts.level % level_number if mode == "play" else texts.stress,
		texts.soldiers, run.soldiers, texts.kills, run.kills,
		("   ·   " + world.weapon_text(run.damage)) if run.damage > 1.0 else "",
	]


func _clamp_x(x: float) -> float:
	return clampf(x, -SummandosRun.TRACK_HALF_WIDTH, SummandosRun.TRACK_HALF_WIDTH)


## Steers like a good player: in front of the wave in range, else to the gate leaving more
## soldiers (performance test, --autosteer).
func _autosteer() -> float:
	var gate_x := 0.0
	if run.next_row < run.level.rows.size():
		var row: Dictionary = run.level.rows[run.next_row]
		gate_x = -2.5 if _value(row.left) > _value(row.right) else 2.5
	return run.steer_hint(gate_x)


func _value(pick: Dictionary) -> int:
	if pick.has("gate"):
		return (pick.gate as LilloGate).apply(run.soldiers)
	return run.soldiers
