extends GutTest

var opened := 7
var colours := {"tile": Color.TEAL, "tile_lip": Color.DARK_SLATE_GRAY, "tab": Color.BLACK,
	"star": Color.GOLD, "arrow": Color.TEAL, "arrow_lip": Color.DARK_SLATE_GRAY}


func before_each() -> void:
	opened = 7


func _map() -> LilloLevelMap:
	var map := LilloLevelMap.new(30, func(n: int) -> int: return 3 if n < opened else 0,
		func(n: int) -> bool: return n <= opened, colours)
	add_child_autofree(map)
	return map


func test_the_next_level_to_play_is_the_last_open_one() -> void:
	assert_eq(_map().next_to_play(), 7)


func test_it_opens_on_the_page_of_the_next_level() -> void:
	opened = 18
	var map := _map()
	map.open("Easy")
	assert_eq(map.page, 1)


func test_pages_stay_in_range() -> void:
	var map := _map()
	map.turn(-1)
	assert_eq(map.page, 0)
	map.turn(5)
	assert_eq(map.page, map.pages() - 1)


func test_a_tile_emits_its_number() -> void:
	var map := _map()
	watch_signals(map)
	map.open("Easy")
	await wait_process_frames(1)
	var grid: GridContainer = map.find_children("*", "GridContainer", true, false)[0]
	var first: Button = grid.get_child(0).get_child(0)
	first.pressed.emit()
	assert_signal_emitted_with_parameters(map, "picked", [1])
	var locked: Button = grid.get_child(10).get_child(0)
	assert_true(locked.disabled, "level 11 is locked while only 7 are open")
