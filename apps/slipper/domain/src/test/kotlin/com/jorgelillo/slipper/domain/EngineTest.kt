package com.jorgelillo.slipper.domain

import com.jorgelillo.slipper.domain.GameConfig as C
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class EngineTest {
    private fun run(w: World, seconds: Double, input: (World) -> Input = { Input.NONE }): World {
        var world = w
        repeat((seconds / C.STEP).toInt()) { world = Engine.step(world, input(world)) }
        return world
    }

    private fun peak(holdFor: Double): Double {
        var w = Engine.step(World(seed = 1, nextThrow = 1e9, nextCroqueta = 1e9), Input(tapped = true, holding = true))
        var top = 0.0
        repeat(240) {
            w = Engine.step(w, Input(holding = w.time < holdFor))
            top = maxOf(top, w.kid.y)
        }
        return top
    }

    @Test
    fun aTapJumpsAndTheKidLandsAgain() {
        val w = run(World(seed = 1, nextThrow = 1e9), 0.0)
        val up = Engine.step(w, Input(tapped = true))
        assertTrue(up.kid.y > 0)
        val landed = run(up, 1.5)
        assertTrue(landed.kid.onFloor)
    }

    @Test
    fun holdingJumpsHigher() {
        assertTrue(peak(holdFor = 0.3) > peak(holdFor = 0.0) * 1.3)
    }

    @Test
    fun aTapJustBeforeLandingStillJumps() {
        var w = Engine.step(World(seed = 1, nextThrow = 1e9, nextCroqueta = 1e9), Input(tapped = true))
        while (w.kid.vy > 0 || w.kid.y > 2.0) w = Engine.step(w, Input.NONE) // falling, almost down
        w = Engine.step(w, Input(tapped = true))
        w = run(w, 0.2)
        assertTrue(w.kid.y > 2.0, "the buffered tap jumped on landing")
    }

    @Test
    fun aLowSlipperCatchesAKidOnTheFloor() {
        val w =
            run(
                World(seed = 1, slippers = listOf(Slipper(40.0, SlipperKind.LOW, C.SLIPPER_SPEED)), nextThrow = 1e9),
                1.0
            )
        assertNotNull(w.caughtBy)
        assertTrue(w.over)
    }

    @Test
    fun jumpingClearsALowSlipper() {
        val start = World(seed = 1, slippers = listOf(Slipper(48.0, SlipperKind.LOW, C.SLIPPER_SPEED)), nextThrow = 1e9)
        val w = run(start, 1.0) {
            if (it.time <
                C.STEP * 2
            ) {
                Input(tapped = true, holding = true)
            } else {
                Input(holding = it.time < 0.25)
            }
        }
        assertNull(w.caughtBy)
    }

    @Test
    fun aHighSlipperFliesOverAKidOnTheFloor() {
        val w =
            run(
                World(seed = 1, slippers = listOf(Slipper(40.0, SlipperKind.HIGH, C.SLIPPER_SPEED)), nextThrow = 1e9),
                1.0
            )
        assertNull(w.caughtBy)
    }

    @Test
    fun aCroquetaInReachIsEatenAndScores() {
        val w =
            run(World(seed = 1, croquetas = listOf(Croqueta(70.0, C.LOW_Y)), nextThrow = 1e9, nextCroqueta = 1e9), 0.5)
        assertEquals(1, w.eaten)
        assertEquals((w.time * C.POINTS_PER_SECOND).toInt() + C.POINTS_PER_CROQUETA, w.score)
    }

    @Test
    fun theSameSeedAndInputsGiveTheSameRun() {
        val tapEverySecond: (World) -> Input = { Input(tapped = (it.time % 1.0) < C.STEP) }
        val a = run(Engine.start(42), 20.0, tapEverySecond)
        val b = run(Engine.start(42), 20.0, tapEverySecond)
        assertEquals(a, b)
        val other = run(Engine.start(43), 20.0, tapEverySecond)
        assertFalse(
            a.slippers == other.slippers && a.croquetas == other.croquetas && a.over == other.over &&
                a.time == other.time
        )
    }

    @Test
    fun eatingCroquetasNeverMovesTheNextThrow() {
        // Same seed, one calm grandma and one angry one: the slippers leave her hand at the same
        // times (a challenge link replays the pattern), only faster when she is angry.
        val calm = run(World(seed = 7, nextCroqueta = 1e9), 2.0)
        val angry = run(World(seed = 7, eaten = 10, nextCroqueta = 1e9), 2.0)
        assertEquals(1, calm.throws)
        assertEquals(calm.throws, angry.throws)
        assertEquals(calm.nextThrow, angry.nextThrow)
        assertTrue(angry.slippers.single().speed > calm.slippers.single().speed)
    }

    @Test
    fun angerMakesSlippersFasterUpToTheCap() {
        val w = run(World(seed = 3, eaten = 100, nextThrow = 0.0, nextCroqueta = 1e9), C.STEP)
        assertEquals(C.MAX_SLIPPER_SPEED, w.slippers.single().speed)
    }

    @Test
    fun itGetsHarderOverTime() {
        assertTrue(Engine.throwGap(30.0) < Engine.throwGap(0.0))
        assertEquals(C.THROW_GAP_MIN, Engine.throwGap(1000.0))
        assertTrue(Engine.worldSpeed(20.0) > Engine.worldSpeed(0.0))
    }

    @Test
    fun noHighSlippersInTheFirstSeconds() {
        val w = run(Engine.start(5), C.HIGH_FROM - 0.1) { Input.NONE }
        assertTrue(w.slippers.all { it.kind == SlipperKind.LOW })
    }

    @Test
    fun anOverRunStaysOver() {
        val caught = World(seed = 1, caughtBy = Slipper(60.0, SlipperKind.LOW, 45.0))
        assertEquals(caught, Engine.step(caught, Input(tapped = true)))
    }

    @Test
    fun aChallengeTravelsInALink() {
        val c = Challenge(seed = 123456789L, score = 1240)
        val link = Challenges.link(c)
        assertTrue(link.startsWith("https://jorgelillo7.github.io/c/#"))
        assertEquals(c, Challenges.fromText("¿Me ganas? $link"))
        assertNull(Challenges.fromText("no link here"))
    }

    @Test
    fun theSpritesFileParses() {
        val sprites = Sprites.parse(File("../app/src/main/assets/sprites.txt").readText())
        for (name in listOf(
            "kid_run_1",
            "kid_run_2",
            "kid_jump",
            "grandma_run_1",
            "grandma_run_2",
            "grandma_throw",
            "slipper",
            "croqueta"
        )) {
            assertNotNull(sprites[name], name)
        }
        val kid = sprites.getValue("kid_run_1")
        assertEquals(12, kid.width)
        assertEquals(16, kid.height)
        assertEquals(0, kid[0, 0])
        assertEquals(0xFF2B1D1D.toInt(), kid[4, 0])
    }
}
