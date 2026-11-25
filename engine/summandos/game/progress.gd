## Stars earned per mode and level, kept between sessions; a level opens once the one before it
## has been won.
class_name SummandosProgress
extends RefCounted


static func stars(mode: SummandosLevel.Mode, number: int, path := LilloSettings.PATH) -> int:
	var earned: Array = LilloSettings.read("stars_%d" % mode, [], path)
	return earned[number - 1] if number - 1 < earned.size() else 0


## Keeps the best result: a worse run never takes stars away.
static func record(mode: SummandosLevel.Mode, number: int, earned: int, path := LilloSettings.PATH) -> void:
	var all: Array = LilloSettings.read("stars_%d" % mode, [], path)
	while all.size() < number:
		all.append(0)
	all[number - 1] = maxi(all[number - 1], earned)
	LilloSettings.write("stars_%d" % mode, all, path)


static func unlocked(mode: SummandosLevel.Mode, number: int, path := LilloSettings.PATH) -> bool:
	return number == 1 or stars(mode, number - 1, path) > 0


## Endless mode opens once every rank's last level has been won.
static func endless_unlocked(path := LilloSettings.PATH) -> bool:
	for mode in SummandosLevel.Mode.values():
		if stars(mode, SummandosLevel.level_count(mode), path) == 0:
			return false
	return true


## The most endless levels won in one run.
static func endless_best(path := LilloSettings.PATH) -> int:
	return LilloSettings.read("endless_best", 0, path)


## Keeps `cleared` as the endless record if it beats it; true when it does.
static func record_endless(cleared: int, path := LilloSettings.PATH) -> bool:
	if cleared <= endless_best(path):
		return false
	LilloSettings.write("endless_best", cleared, path)
	return true
