## One run of a level, without any drawing: the squad moves forward, takes gates and weapons,
## shoots the nearest wave in range and loses a soldier for every enemy that reaches it.
class_name SummandosRun
extends RefCounted

enum State { RUNNING, WON, LOST }

const SPEED := 9.0
const ENEMY_SPEED := 3.0
const RANGE := 22.0
const SHOTS_PER_SECOND := 1.5
const TRACK_HALF_WIDTH := 4.0
## Enemies stand still until the squad is this close, then walk towards it.
const WAKE_DISTANCE := 40.0

var level: SummandosLevel
var state := State.RUNNING
var z := 0.0
var x := 0.0
var soldiers := 0
var damage := 1.0
var kills := 0
## Live waves, nearest first: [{z, count, hp, damage_taken}].
var waves: Array[Dictionary] = []
var next_row := 0
## The last thing the squad took, for the HUD: {"gate": LilloGate} or {"weapon": x}.
var last_pick: Dictionary = {}


func _init(run_level: SummandosLevel) -> void:
	level = run_level
	soldiers = level.start_soldiers
	for wave in level.waves:
		waves.append({"z": wave.z, "count": wave.count, "hp": wave.hp, "damage_taken": 0.0})


## Steers towards a track position; the squad never leaves the track.
func steer(target_x: float) -> void:
	x = clampf(target_x, -TRACK_HALF_WIDTH, TRACK_HALF_WIDTH)


## The wave the squad is shooting at, or an empty dictionary.
func target() -> Dictionary:
	if not waves.is_empty() and waves[0].z - z <= RANGE:
		return waves[0]
	return {}


func step(delta: float) -> void:
	if state != State.RUNNING:
		return
	var blocked: bool = not waves.is_empty() and waves[0].z - z <= 1.0
	if not blocked:
		z = minf(z + SPEED * delta, level.length)
	_take_rows()
	_shoot(delta)
	_advance_waves(delta)
	if soldiers <= 0:
		state = State.LOST
	elif waves.is_empty() and z >= level.length:
		state = State.WON


func _take_rows() -> void:
	while next_row < level.rows.size() and z >= level.rows[next_row].z:
		var row: Dictionary = level.rows[next_row]
		var pick: Dictionary = row.left if x < 0.0 else row.right
		if pick.has("gate"):
			soldiers = (pick.gate as LilloGate).apply(soldiers)
		else:
			damage *= pick.weapon
		last_pick = pick
		next_row += 1


func _shoot(delta: float) -> void:
	var wave := target()
	if wave.is_empty():
		return
	wave.damage_taken += soldiers * SHOTS_PER_SECOND * damage * delta
	var dead := mini(wave.count, int(wave.damage_taken / wave.hp))
	if dead > 0:
		wave.count -= dead
		wave.damage_taken -= dead * wave.hp
		kills += dead
	if wave.count <= 0:
		waves.pop_front()


func _advance_waves(delta: float) -> void:
	for wave in waves:
		if wave.z - z < WAKE_DISTANCE:
			wave.z = maxf(wave.z - ENEMY_SPEED * delta, z + 1.0)
	if not waves.is_empty() and waves[0].z - z <= 1.0:
		var front: Dictionary = waves[0]
		var clash := mini(front.count, maxi(1, int(ceil(soldiers * 2.0 * delta))))
		clash = mini(clash, soldiers)
		front.count -= clash
		soldiers -= clash
		if front.count <= 0:
			waves.pop_front()
