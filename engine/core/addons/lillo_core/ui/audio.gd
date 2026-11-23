## Sound effects, music and vibration for a game, each with its own switch saved in LilloSettings
## ("sound", "music", "vibration", all on by default). Effects that fire many times a second
## (shots, hits) are throttled so a crowd never turns into noise. Music pauses with the app.
class_name LilloAudio
extends Node

const PLAYERS_PER_SOUND := 4
## The shortest time between two plays of one effect, in seconds.
const MIN_GAP := 0.06

var sound_on: bool = LilloSettings.read("sound", true)
var music_on: bool = LilloSettings.read("music", true)
var vibration_on: bool = LilloSettings.read("vibration", true)
var _effects := {}
var _players := {}
var _last := {}
var _music: AudioStreamPlayer
var _track: AudioStream


## `effects`: name -> AudioStream.
func _init(effects: Dictionary) -> void:
	_effects = effects


func _ready() -> void:
	process_mode = Node.PROCESS_MODE_ALWAYS
	_music = AudioStreamPlayer.new()
	_music.volume_db = -8.0
	add_child(_music)
	for name in _effects:
		var player := AudioStreamPlayer.new()
		player.stream = _effects[name]
		player.max_polyphony = PLAYERS_PER_SOUND
		add_child(player)
		_players[name] = player


## Plays effect `name`, a little higher with `pitch` > 1; skipped when sound is off or the same
## effect played less than MIN_GAP ago.
func play(name: String, pitch := 1.0, volume_db := 0.0) -> void:
	if not sound_on or not _players.has(name):
		return
	var now := Time.get_ticks_msec() / 1000.0
	if now - float(_last.get(name, -1.0)) < MIN_GAP:
		return
	_last[name] = now
	var player: AudioStreamPlayer = _players[name]
	player.pitch_scale = pitch
	player.volume_db = volume_db
	player.play()


## Loops `track` as background music (unchanged if it is already playing).
func music(track: AudioStream) -> void:
	if track == _track and (_music.playing or not music_on):
		return
	_track = track
	_music.stream = track
	if music_on:
		_music.play()


## Plays effect `name` whenever any button in the scene tree is pressed, now or later.
func click_on_buttons(name: String) -> void:
	var hook := func(node: Node) -> void:
		if node is BaseButton:
			(node as BaseButton).pressed.connect(play.bind(name))
	get_tree().node_added.connect(hook)
	for node in get_tree().root.find_children("*", "BaseButton", true, false):
		hook.call(node)


## A short buzz on phones that have it, when vibration is on.
func buzz(milliseconds := 25) -> void:
	if vibration_on:
		Input.vibrate_handheld(milliseconds)


func set_sound(on: bool) -> void:
	sound_on = on
	LilloSettings.write("sound", on)


func set_music(on: bool) -> void:
	music_on = on
	LilloSettings.write("music", on)
	if on and _track:
		_music.stream = _track
		_music.play()
	else:
		_music.stop()


func set_vibration(on: bool) -> void:
	vibration_on = on
	LilloSettings.write("vibration", on)


func _notification(what: int) -> void:
	if not is_node_ready():
		return
	if what == NOTIFICATION_APPLICATION_PAUSED or what == NOTIFICATION_APPLICATION_FOCUS_OUT:
		_music.stream_paused = true
	elif what == NOTIFICATION_APPLICATION_RESUMED or what == NOTIFICATION_APPLICATION_FOCUS_IN:
		_music.stream_paused = false


## A WAV that loops end to end, for music written without loop points.
static func looping(stream: AudioStreamWAV) -> AudioStreamWAV:
	var copy: AudioStreamWAV = stream.duplicate()
	copy.loop_mode = AudioStreamWAV.LOOP_FORWARD
	copy.loop_begin = 0
	var bytes_per_frame := (2 if copy.format == AudioStreamWAV.FORMAT_16_BITS else 1) * (2 if copy.stereo else 1)
	copy.loop_end = copy.data.size() / bytes_per_frame if copy.format != AudioStreamWAV.FORMAT_QOA else copy.data.size()
	return copy
