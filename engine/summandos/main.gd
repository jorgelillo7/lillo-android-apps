## Summandos' entry point: wires the run (game/), the 3D world and the screens (view/) together and
## handles input and the back button. Options after `--`: `--mode=play|stress` starts a run at
## once (stress is the 1,000-unit performance test, reachable only from here;
## `--difficulty=easy|normal|adult|expert --level=<n>` pick a level, `--autosteer` takes the better gate),
## `--paused` pauses the run a moment after it starts, `--levels` opens the level map, `--cast` lines up the characters (store), `--about` the About card, `--palette=sunset|night` and `--squad=<colour>` override the saved choices, `--colours` opens the
## squad colour picker, and
## `--lang=es|en` overrides the phone's language, and LilloCapture's `--shot=<png> --after=<s>
## [--shot-size=WxH]` saves a screenshot and quits.
extends Node3D

const MODES := ["easy", "normal", "adult", "expert"]
## How long `--paused` lets the run go before pausing it, in seconds.
const PAUSED_AFTER := 2.0

var texts := SummandosTexts.for_language(OS.get_locale_language())
var theme_name := "night" if LilloSettings.read("night", false) else "sunset"
var squad_choice: String = LilloSettings.read("squad", "default")
var palette := SummandosPalette.named(theme_name, squad_choice)
var difficulty: SummandosLevel.Mode = LilloSettings.read("difficulty", SummandosLevel.Mode.EASY)
var level_number := 1
var mode := "play"
var run: SummandosRun
var target_x := 0.0
var dragging := false
## True when run to take a screenshot: the frame rate stays off the picture.
var capturing := " ".join(OS.get_cmdline_user_args()).contains("--shot=")
var world: SummandosWorld
var ui: SummandosUi
var audio: LilloAudio
var levels := SummandosLevelsAhead.new()
## What the last frame saw, to sound what changed: rows taken, soldiers, kills, thinking seconds,
## whether the boss has shown up.
var heard := {}


func _exit_tree() -> void:
	levels.finish()


func _ready() -> void:
	if LilloCapture.rehost(self):
		return
	var args := OS.get_cmdline_user_args()
	var start := ""
	for arg in args:
		if arg.begins_with("--palette="):
			theme_name = arg.trim_prefix("--palette=")
			palette = SummandosPalette.named(theme_name, squad_choice)
		elif arg.begins_with("--squad="):
			squad_choice = arg.trim_prefix("--squad=")
			palette = SummandosPalette.named(theme_name, squad_choice)
		elif arg.begins_with("--mode="):
			start = arg.trim_prefix("--mode=")
		elif arg.begins_with("--difficulty="):
			difficulty = maxi(0, MODES.find(arg.trim_prefix("--difficulty="))) as SummandosLevel.Mode
		elif arg.begins_with("--lang="):
			texts = SummandosTexts.for_language(arg.trim_prefix("--lang="))
		elif arg.begins_with("--level="):
			level_number = int(arg.trim_prefix("--level="))
	audio = LilloAudio.new(_sounds())
	audio.process_mode = Node.PROCESS_MODE_ALWAYS
	add_child(audio)
	audio.click_on_buttons("click")
	world = SummandosWorld.new(palette, texts)
	add_child(world)
	var map := LilloLevelMap.new(SummandosLevel.LEVELS_PER_MODE,
		func(n: int) -> int: return SummandosProgress.stars(difficulty, n),
		func(n: int) -> bool: return SummandosProgress.unlocked(difficulty, n),
		{"tile": palette.squad, "tile_lip": palette.squad_dark, "tab": Color(palette.outline, 0.7),
			"star": palette.weapon, "arrow": palette.squad, "arrow_lip": palette.squad_dark})
	ui = SummandosUi.new(palette, texts, map, SummandosPalette.swatches(theme_name), squad_choice, audio)
	add_child(ui)
	add_child(LilloCapture.new())
	ui.mode_picked.connect(_open_levels)
	ui.level_picked.connect(func(number: int) -> void: _start("play", number))
	ui.night_toggled.connect(_toggle_night)
	ui.squad_picked.connect(_pick_squad)
	ui.next_picked.connect(func() -> void: _start("play", level_number + 1))
	ui.again_picked.connect(func() -> void: _start(mode, level_number))
	ui.levels_picked.connect(_open_levels.bind(-1))
	ui.menu_picked.connect(_show_menu)
	ui.pause_picked.connect(_pause)
	ui.resume_picked.connect(_resume)
	ui.show_boss(_next_boss())
	if start != "":
		_start(start, level_number)
		if args.has("--paused"):
			get_tree().create_timer(PAUSED_AFTER).timeout.connect(_pause)
	elif args.has("--levels"):
		_open_levels(-1)
	elif args.has("--about"):
		ui.show_screen("about")
	elif args.has("--colours"):
		ui.show_screen("colours")
	elif args.has("--settings"):
		ui.show_screen("settings")
	elif args.has("--help-card"):
		ui.show_screen("help")
	elif args.has("--cast"):
		var bosses := []
		for number in [5, 10, 15, 20]:
			bosses.append(SummandosLevel.boss_look(SummandosLevel.Mode.EASY, number))
		ui.show_cast([palette.squad, SummandosPalette.named(theme_name, "orange").squad,
			SummandosPalette.named(theme_name, "sky").squad], bosses)
	else:
		_show_menu()


## Every effect in assets/sounds, and the two music loops.
func _sounds() -> Dictionary:
	var effects := {}
	for name in ["click", "gate_up", "gate_down", "shot", "crunch", "tick", "boss", "star", "win", "lose"]:
		effects[name] = load("res://assets/sounds/%s.wav" % name)
	return effects


func _music(name: String) -> void:
	audio.music(LilloAudio.looping(load("res://assets/sounds/music_%s.wav" % name)))


func _show_menu() -> void:
	get_tree().paused = false
	run = null
	_music("menu")
	world.draw_idle()
	ui.show_boss(_next_boss())
	ui.show_screen("menu")


## The look of the next boss the player will meet in the current mode.
func _next_boss() -> Dictionary:
	var next := ui.level_map.next_to_play()
	return SummandosLevel.boss_look(difficulty, ceili(next / 5.0) * 5)


## Opens the level map of `picked` mode (-1 keeps the current one).
func _open_levels(picked: int) -> void:
	if picked >= 0:
		difficulty = picked as SummandosLevel.Mode
		LilloSettings.write("difficulty", picked)
	get_tree().paused = false
	run = null
	_music("menu")
	world.draw_idle()
	ui.show_boss(_next_boss())
	ui.show_levels(difficulty)
	levels.prepare(difficulty, ui.level_map.next_to_play())


func _pick_squad(name: String) -> void:
	LilloSettings.write("squad", name)
	get_tree().reload_current_scene()


func _toggle_night() -> void:
	LilloSettings.write("night", not LilloSettings.read("night", false))
	get_tree().reload_current_scene()


func _start(run_mode: String, number := level_number) -> void:
	get_tree().paused = false
	mode = run_mode
	level_number = clampi(number, 1, SummandosLevel.level_count(difficulty))
	var level := SummandosLevel.stress() if mode == "stress" else levels.take(difficulty, level_number)
	run = SummandosRun.new(level)
	target_x = 0.0
	world.reset(level, mode == "play" and difficulty >= SummandosLevel.Mode.ADULT)
	heard = {"row": 0, "soldiers": run.soldiers, "kills": 0, "think": 0, "boss": false}
	_music("run")
	ui.show_screen("run")


func _finish() -> void:
	var won := run.state == SummandosRun.State.WON
	var playing := mode == "play"
	var stars := run.stars() if playing else 0
	if playing:
		SummandosProgress.record(difficulty, level_number, stars)
		if won:
			levels.prepare(difficulty, level_number + 1)
	audio.play("win" if won else "lose")
	audio.buzz(80 if won else 200)
	ui.show_result(_heading(), won, run.soldiers, run.kills, stars,
		won and playing and level_number < SummandosLevel.level_count(difficulty), run.failed_gate)


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


## Stops a run in progress where it stands and shows the pause card; nothing moves until
## `_resume`. Not once the run is over.
func _pause() -> void:
	if run == null or run.state != SummandosRun.State.RUNNING or get_tree().paused:
		return
	dragging = false
	get_tree().paused = true
	ui.show_screen("paused")


func _resume() -> void:
	get_tree().paused = false
	ui.show_screen("run")


## Leaving the game (a call, the home button, the notification shade) pauses the run; the back
## button pauses it too, and on the pause card carries on.
func _notification(what: int) -> void:
	# Only on the phone: a desktop window losing focus would pause the runs screenshots are taken of.
	if what == NOTIFICATION_APPLICATION_PAUSED or (what == NOTIFICATION_APPLICATION_FOCUS_OUT and OS.has_feature("mobile")):
		_pause()
	if what != NOTIFICATION_WM_GO_BACK_REQUEST:
		return
	if run != null and get_tree().paused:
		_resume()
	elif run != null and run.state == SummandosRun.State.RUNNING:
		_pause()
	elif run != null:
		_open_levels(-1)
	elif ui.about.visible or ui.swatches.visible:
		ui.show_screen("settings")
	elif ui.level_map.visible or ui.settings.visible or ui.help.visible:
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
	_sound_run()
	ui.update_hud(_heading(), _hud_text(), run.thinking, run.boss_health(), mode == "stress" and not capturing)
	var armed := run.damage if mode == "play" else 1.0
	ui.show_weapon(armed, world.weapon_text(armed))
	if run.state != SummandosRun.State.RUNNING and not ui.result.visible:
		_finish()


## Sounds what changed in the run since last frame: a gate (higher for a bigger gain), shots
## while a wave is in range, hits, the thinking countdown, the boss arriving.
func _sound_run() -> void:
	if run.next_row > heard.row:
		var before: int = heard.soldiers
		if run.soldiers >= before:
			audio.play("gate_up", clampf(1.0 + (run.soldiers - before) / maxf(1.0, before) * 0.25, 1.0, 1.6))
		else:
			audio.play("gate_down")
		audio.buzz(20)
	if not run.target().is_empty() and run.soldiers > 0:
		audio.play("shot", randf_range(0.9, 1.15), -16.0)
	if run.kills > heard.kills:
		audio.play("crunch", randf_range(0.85, 1.2), -8.0)
	var seconds := ceili(run.thinking)
	if seconds != heard.think and seconds > 0:
		audio.play("tick", 1.3 if seconds <= 3 else 1.0)
	if not heard.boss:
		for wave in run.waves:
			if wave.boss and wave.z - run.z < SummandosRun.WAKE_DISTANCE:
				heard.boss = true
				audio.play("boss")
				audio.buzz(120)
	if run.burnt > 0:
		run.burnt = 0
		audio.play("lose", 1.6, -6.0)
		audio.buzz(60)
	if run.struck > 0:
		run.struck = 0
		audio.play("lose", 0.8, -4.0)
		audio.buzz(90)
	heard.row = run.next_row
	heard.soldiers = run.soldiers
	heard.kills = run.kills
	heard.think = seconds


## The mode and level being played, e.g. "Captain · Level 5"; empty in the performance test.
func _heading() -> String:
	if mode != "play":
		return ""
	return "%s  ·  %s" % [texts.modes[difficulty], texts.level % level_number]


func _hud_text() -> String:
	return "%s %d   ·   %s %d" % [texts.soldiers, run.soldiers, texts.kills, run.kills]


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
