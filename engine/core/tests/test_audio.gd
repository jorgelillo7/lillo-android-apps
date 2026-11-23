extends GutTest

var settings_backup := {}


func before_each() -> void:
	for key in ["sound", "music", "vibration"]:
		settings_backup[key] = LilloSettings.read(key, true)


func after_each() -> void:
	for key in settings_backup:
		LilloSettings.write(key, settings_backup[key])


func _audio() -> LilloAudio:
	var generator := AudioStreamGenerator.new()
	var audio := LilloAudio.new({"click": generator})
	add_child_autofree(audio)
	return audio


func test_switches_are_saved() -> void:
	var audio := _audio()
	audio.set_sound(false)
	audio.set_music(false)
	audio.set_vibration(false)
	assert_false(LilloSettings.read("sound", true))
	assert_false(LilloSettings.read("music", true))
	assert_false(LilloSettings.read("vibration", true))


func test_an_effect_is_throttled() -> void:
	var audio := _audio()
	audio.set_sound(true)
	audio.play("click")
	var first: float = audio._last["click"]
	audio.play("click")
	assert_eq(audio._last["click"], first, "a second play inside MIN_GAP is skipped")


func test_nothing_plays_with_sound_off() -> void:
	var audio := _audio()
	audio.set_sound(false)
	audio.play("click")
	assert_false(audio._last.has("click"))
