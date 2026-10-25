package com.jorgelillo.slipper.domain

import com.jorgelillo.slipper.domain.GameConfig as C
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/** What makes a run more than jumping: near misses, the combo, power-ups, the cat, the warnings. */
class FeaturesTest {
    private fun bare(
        furniture: List<Furniture> = emptyList(),
        slippers: List<Slipper> = emptyList(),
        croquetas: List<Croqueta> = emptyList(),
        cats: List<Cat> = emptyList(),
        powerUps: List<PowerUp> = emptyList()
    ) = World(
        seed = 1,
        chunks = listOf(Chunk(0, -100.0, 1e9, Room.KITCHEN)),
        furniture = furniture,
        slippers = slippers,
        croquetas = croquetas,
        cats = cats,
        powerUps = powerUps,
        nextThrow = 1e9
    )

    private fun run(w: World, seconds: Double, input: (World) -> Input = { Input.NONE }): World {
        var world = w
        repeat((seconds / C.STEP).toInt()) { world = Engine.step(world, input(world)) }
        return world
    }

    @Test
    fun aHighSlipperOverAStandingKidIsANearMissAndOpensTheGap() {
        val w0 = bare(slippers = listOf(Slipper(-4.0, SlipperKind.HIGH, speed = 70.0)))
        val w = run(w0, 0.5)
        assertEquals(1, w.nearMisses)
        assertEquals(C.NEAR_MISS_POINTS, w.bonus)
        assertTrue(w.gap > C.START_GAP - 1, "the near miss gained ground (${w.gap})")
    }

    @Test
    fun croquetasInARowBuildACombo() {
        // Three croquetas a few units apart: eaten well within the combo window.
        val w = run(bare(croquetas = listOf(Croqueta(10.0, 0.5), Croqueta(16.0, 0.5), Croqueta(22.0, 0.5))), 0.9)
        assertEquals(3, w.eaten)
        assertEquals(3, w.combo)
        assertEquals(10 + 20 + 30, w.bonus)
        assertEquals(listOf(10, 20, 30), w.events.filter { it.kind == EventKind.CROQUETA }.map { it.value })
    }

    @Test
    fun theComboResetsAfterAPause() {
        val far = C.RUN_SPEED * (C.COMBO_WINDOW + 1)
        val w = run(bare(croquetas = listOf(Croqueta(10.0, 0.5), Croqueta(10.0 + far, 0.5))), 4.0)
        assertEquals(2, w.eaten)
        assertEquals(20, w.bonus)
    }

    @Test
    fun theSandwichBlocksOneHit() {
        val w0 = bare(
            powerUps = listOf(PowerUp(PowerKind.SANDWICH, 6.0, 0.0)),
            furniture = listOf(Furniture(FurnitureKind.CHAIR, 30.0), Furniture(FurnitureKind.CHAIR, 60.0))
        )
        val afterFirst = run(w0, 1.2)
        assertNotNull(afterFirst.lastEvent(EventKind.BLOCKED))
        assertFalse(afterFirst.shield)
        assertEquals(null, afterFirst.lastEvent(EventKind.STUMBLE))
        val afterSecond = run(afterFirst, 1.5)
        assertNotNull(afterSecond.lastEvent(EventKind.STUMBLE), "the second chair trips him")
    }

    @Test
    fun skatesAreFasterAndCrashThroughFurniture() {
        val w0 = bare(
            powerUps = listOf(PowerUp(PowerKind.SKATES, 6.0, 0.0)),
            furniture = listOf(Furniture(FurnitureKind.CHAIR, 30.0))
        )
        val w = run(w0, 1.5)
        assertNotNull(w.lastEvent(EventKind.CRASH))
        assertEquals(0.0, w.kid.stumble)
        assertTrue(w.gap > C.START_GAP, "she falls behind while he skates (${w.gap})")
    }

    @Test
    fun theCatWalksTowardsTheKidAndTripsHim() {
        val w = run(bare(cats = listOf(Cat(40.0))), 1.5)
        assertTrue(w.cats.single().fled)
        assertNotNull(w.events.lastOrNull { it.kind == EventKind.STUMBLE })
    }

    @Test
    fun sheWindsUpBeforeEachThrowAndTheNextKindIsKnown() {
        var w = Engine.start(seed = 5)
        var windUpSteps = 0
        var lastThrow = 0.0
        var throwsSeen = 0
        repeat((40 / C.STEP).toInt()) {
            val next = Engine.step(w.copy(gap = C.MAX_GAP), Input.NONE)
            if (next.throws > w.throws) {
                assertEquals(w.nextKind, next.slippers.last().kind, "the warned kind is the thrown kind")
                // The second of a pair comes sooner than a wind-up; every other throw is warned in full.
                if (next.time - lastThrow > C.WIND_UP + 0.05) {
                    assertTrue(windUpSteps * C.STEP >= C.WIND_UP - 2 * C.STEP, "warned ${windUpSteps * C.STEP}s")
                }
                lastThrow = next.time
                throwsSeen++
                windUpSteps = 0
            } else if (next.windingUp) {
                windUpSteps++
            }
            w = next
        }
        assertTrue(throwsSeen > 10)
    }

    @Test
    fun laterSomeThrowsComeInPairs() {
        // Throw a long run without getting caught: keep the gap open by hand.
        var w = Engine.start(seed = 9)
        val gaps = mutableListOf<Double>()
        var lastThrow = 0.0
        repeat((90 / C.STEP).toInt()) {
            val next = Engine.step(w.copy(gap = C.MAX_GAP), Input.NONE)
            if (next.throws > w.throws) {
                gaps += next.time - lastThrow
                lastThrow = next.time
            }
            w = next
        }
        assertTrue(gaps.any { it < C.DOUBLE_GAP + 0.05 }, "a pair came: $gaps")
    }

    @Test
    fun theStartIsNeverEmpty() {
        val w = Engine.start(seed = 3)
        assertTrue(w.chunks.first().start < -20, "the house reaches behind the kid at the start")
    }

    @Test
    fun scoreIsDistanceAndBonus() {
        val w = bare().copy(distance = 100.0, bonus = 25)
        assertEquals((100 * C.POINTS_PER_UNIT).toInt() + 25, w.score)
    }
}
