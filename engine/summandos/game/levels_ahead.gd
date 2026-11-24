## Generates levels on a worker thread before they are played and keeps them, so starting a level
## (or playing it again) never waits on SummandosLevel.generate, which simulates the level many
## times over. Call finish() before letting go of it.
class_name SummandosLevelsAhead
extends RefCounted

var _levels := {}
var _tasks := {}


## Starts generating level `number` of `mode` in the background, unless it is already there.
func prepare(mode: SummandosLevel.Mode, number: int) -> void:
	if number < 1 or number > SummandosLevel.level_count(mode):
		return
	var key := _key(mode, number)
	if _levels.has(key):
		return
	var box: Array[SummandosLevel] = []
	_levels[key] = box
	_tasks[key] = WorkerThreadPool.add_task(func() -> void: box.append(SummandosLevel.generate(mode, number)))


## Level `number` of `mode`, waiting for it if it is still being generated.
func take(mode: SummandosLevel.Mode, number: int) -> SummandosLevel:
	prepare(mode, number)
	var key := _key(mode, number)
	if _tasks.has(key):
		WorkerThreadPool.wait_for_task_completion(_tasks[key])
		_tasks.erase(key)
	return _levels[key][0]


## Waits for every level still being generated.
func finish() -> void:
	for key in _tasks:
		WorkerThreadPool.wait_for_task_completion(_tasks[key])
	_tasks.clear()


func _key(mode: SummandosLevel.Mode, number: int) -> String:
	return "%d-%d" % [mode, number]
