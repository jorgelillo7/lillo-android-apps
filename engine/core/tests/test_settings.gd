extends GutTest

const PATH := "user://test_settings.cfg"


func after_each() -> void:
	DirAccess.remove_absolute(PATH)


func test_an_unset_key_reads_its_fallback() -> void:
	assert_eq(LilloSettings.read("night", false, PATH), false)


func test_a_written_key_reads_back() -> void:
	LilloSettings.write("night", true, PATH)
	LilloSettings.write("volume", 3, PATH)
	assert_eq(LilloSettings.read("night", false, PATH), true)
	assert_eq(LilloSettings.read("volume", 0, PATH), 3)
