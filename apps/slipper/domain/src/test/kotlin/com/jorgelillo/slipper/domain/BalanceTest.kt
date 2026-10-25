package com.jorgelillo.slipper.domain

import com.jorgelillo.slipper.domain.GameConfig as C
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * How long runs last, played by bots over many seeds: the numbers that decide whether the game is
 * fun can't be felt in a unit test, but a run that is over in five seconds, or never ends, shows up
 * here first. Printed with `./gradlew :apps:slipper:domain:test -i | grep balance:`. Tune on a phone
 * (GameConfig), then check these still hold.
 */
class BalanceTest {
    /** Plays one run with [bot] deciding each step's input; returns how long it lasted (capped). */
    private fun survive(seed: Long, cap: Double = 180.0, bot: (World) -> Input): World {
        var w = Engine.start(seed)
        while (!w.over && w.time < cap) w = Engine.step(w, bot(w))
        return w
    }

    /**
     * A decent player: hops low slippers and whatever is ahead on the floor, holds for tall
     * furniture, and stays down when a high slipper is about to pass. Reacts a little late, like a
     * person, and sometimes not at all.
     */
    private fun decent(seed: Long): (World) -> Input {
        val sloppy = Random(seed)
        var holdUntil = 0.0
        return { w ->
            val speed = Engine.runSpeed(w.time)
            val x = w.distance
            fun arrivesIn(s: Slipper) = (x - (s.x + C.SLIPPER_W)) / (s.speed - speed)
            val highSoon = w.slippers.any { !it.passed && it.kind == SlipperKind.HIGH && arrivesIn(it) in 0.0..0.45 }
            val lowSoon = w.slippers.any { !it.passed && it.kind == SlipperKind.LOW && arrivesIn(it) in 0.0..0.10 }
            val obstacle = (
                w.furniture.filter { !it.bumped }.map { it.x to it.top } +
                    w.cats.filter { !it.fled }.map { it.x to C.CAT_H }
                ).filter { (fx, _) -> fx - (x + C.KID_W) in 0.0..(speed * 0.22) }
                .maxByOrNull { it.second }
            val want = (lowSoon || obstacle != null) && !highSoon && w.kid.standing
            val tap = want && sloppy.nextDouble() < 0.9
            if (tap) holdUntil = w.time + if ((obstacle?.second ?: 0.0) >= 7.0) C.MAX_HOLD else 0.05
            Input(tapped = tap, holding = w.time < holdUntil)
        }
    }

    private fun random(seed: Long): (World) -> Input {
        val r = Random(seed)
        return { Input(tapped = r.nextDouble() < 0.015, holding = false) }
    }

    private fun List<Double>.median() = sorted()[size / 2]

    @Test
    fun runsLastLongEnoughToBeFunAndAlwaysEnd() {
        val seeds = 1L..200L
        val decentRuns = seeds.map { survive(it, bot = decent(it)) }
        val randomRuns = seeds.map { survive(it, bot = random(it)) }
        val decentTimes = decentRuns.map { it.time }
        val randomTimes = randomRuns.map { it.time }
        println(
            "balance: decent median %.1fs (p10 %.1f, p90 %.1f, capped %d), random median %.1fs; ".format(
                decentTimes.median(),
                decentTimes.sorted()[20],
                decentTimes.sorted()[180],
                decentRuns.count { !it.over },
                randomTimes.median()
            ) + "decent score median ${decentRuns.map { it.score.toDouble() }.median().toInt()}, " +
                "near misses ${decentRuns.sumOf { it.nearMisses } / decentRuns.size} per run"
        )
        assertTrue(randomTimes.median() < 20, "a careless run ends quickly")
        assertTrue(decentTimes.median() in 20.0..60.0, "a decent run lasts a while, not forever")
        assertTrue(decentRuns.count { !it.over } < 20, "almost every run ends (difficulty keeps rising)")
        assertTrue(decentTimes.median() > randomTimes.median() * 1.6, "skill matters")
    }
}
