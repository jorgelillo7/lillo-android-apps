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
    /** A run with nothing in the way (no furniture, no croquetas, no throws) unless a test adds it. */
    private fun bare(vararg furniture: Furniture, slippers: List<Slipper> = emptyList(), gap: Double = C.START_GAP) =
        World(
            seed = 1,
            chunks = listOf(Chunk(0, 0.0, 1e9, Room.KITCHEN)),
            furniture = furniture.toList(),
            slippers = slippers,
            nextThrow = 1e9,
            gap = gap
        )

    private fun run(w: World, seconds: Double, input: (World) -> Input = { Input.NONE }): World {
        var world = w
        repeat((seconds / C.STEP).toInt()) { world = Engine.step(world, input(world)) }
        return world
    }

    private fun peak(holdFor: Double): Double {
        var w = Engine.step(bare(), Input(tapped = true, holding = true))
        var top = 0.0
        repeat(240) {
            w = Engine.step(w, Input(holding = w.time < holdFor))
            top = maxOf(top, w.kid.y)
        }
        return top
    }

    @Test
    fun theKidRunsAndSpeedsUp() {
        val w = run(bare(), 10.0)
        assertTrue(w.distance > C.RUN_SPEED * 10)
        assertTrue(Engine.runSpeed(20.0) > Engine.runSpeed(0.0))
        assertEquals(C.MAX_RUN_SPEED, Engine.runSpeed(1000.0))
    }

    @Test
    fun aTapJumpsAndTheKidLandsAgain() {
        val up = Engine.step(bare(), Input(tapped = true))
        assertTrue(up.kid.y > 0)
        assertTrue(run(up, 1.5).kid.standing)
    }

    @Test
    fun holdingJumpsHigher() {
        assertTrue(peak(holdFor = 0.3) > peak(holdFor = 0.0) * 1.3)
    }

    @Test
    fun aTapJustBeforeLandingStillJumps() {
        var w = Engine.step(bare(), Input(tapped = true))
        while (w.kid.vy > 0 || w.kid.y > 2.0) w = Engine.step(w, Input.NONE) // falling, almost down
        w = Engine.step(w, Input(tapped = true))
        assertTrue(run(w, 0.2).kid.y > 2.0, "the buffered tap jumped on landing")
    }

    @Test
    fun runningIntoAChairStumblesOnceAndSheClosesIn() {
        val w = run(bare(Furniture(FurnitureKind.CHAIR, 20.0)), 0.9)
        assertTrue(w.furniture.single().bumped)
        assertTrue(w.gap < C.START_GAP - 1.5, "gap ${w.gap}")
        // Past the chair: no second stumble from the same chair.
        val later = run(w, 1.0)
        assertTrue(later.gap > w.gap)
    }

    @Test
    fun aHighJumpLandsOnTheTableAndRunsAcrossIt() {
        val start = bare(Furniture(FurnitureKind.TABLE, 22.0))
        // Jump early and hold: on top of the table by the time he reaches it.
        val w = run(start, 0.85) {
            if (it.time <
                C.STEP * 2
            ) {
                Input(tapped = true, holding = true)
            } else {
                Input(holding = it.time < 0.25)
            }
        }
        assertFalse(w.furniture.single().bumped)
        assertEquals(FurnitureKind.TABLE.h, w.kid.y, 0.01)
        assertTrue(w.kid.standing)
    }

    @Test
    fun walkingOffTheTableFallsBackToTheFloor() {
        val onTable = bare(Furniture(FurnitureKind.TABLE, -5.0)).copy(kid = Kid(y = FurnitureKind.TABLE.h))
        assertEquals(0.0, run(onTable, 1.5).kid.y)
    }

    @Test
    fun aLowSlipperHitsAKidOnTheFloor() {
        val w = run(bare(slippers = listOf(Slipper(-10.0, SlipperKind.LOW, C.RUN_SPEED + 40))), 0.6)
        assertTrue(w.slippers.isEmpty())
        assertTrue(w.gap < C.START_GAP - C.HIT_LOSS)
    }

    @Test
    fun aHighSlipperFliesOverAKidOnTheFloor() {
        val w = run(bare(slippers = listOf(Slipper(-10.0, SlipperKind.HIGH, C.RUN_SPEED + 40))), 0.6)
        assertTrue(w.gap >= C.START_GAP)
    }

    @Test
    fun runningCleanOpensTheGapUpToTheMax() {
        assertEquals(C.MAX_GAP, run(bare(gap = C.MAX_GAP - 1), 10.0).gap, 0.001)
    }

    @Test
    fun sheCatchesYouWhenTheGapCloses() {
        val w = run(bare(Furniture(FurnitureKind.CHAIR, 10.0), gap = 4.0), 1.5)
        assertTrue(w.caught)
        assertTrue(w.over)
        assertEquals(w, Engine.step(w, Input(tapped = true)), "an over run stays over")
    }

    @Test
    fun angerMakesHerRunHarder() {
        val calm = run(bare(gap = 20.0), 5.0)
        val angry = run(bare(gap = 20.0).copy(eaten = 20), 5.0)
        assertTrue(angry.gap < calm.gap)
    }

    @Test
    fun croquetasAreEatenAndScore() {
        val w = run(bare().copy(croquetas = listOf(Croqueta(8.0, C.LOW_Y))), 0.5)
        assertEquals(1, w.eaten)
        assertEquals((w.distance * C.POINTS_PER_UNIT).toInt() + C.POINTS_PER_CROQUETA, w.score)
    }

    @Test
    fun theHouseIsBuiltAheadAndForgottenBehind() {
        val w = run(Engine.start(9), 20.0) { Input(tapped = true, holding = true) }
        val last = w.chunks.last()
        assertTrue(last.end >= w.distance + C.BUILD_AHEAD)
        assertTrue(w.chunks.first().end > w.distance - C.KEEP_BEHIND)
        assertTrue(w.chunks.zipWithNext().all { (a, b) -> a.end == b.start && b.index == a.index + 1 })
    }

    @Test
    fun theFirstChunksAreBareFloor() {
        val w = Engine.start(4)
        val warmUpEnd = w.chunks[House.WARM_UP_CHUNKS - 1].end
        assertTrue(w.furniture.all { it.x >= warmUpEnd })
    }

    @Test
    fun theSameSeedAndInputsGiveTheSameRun() {
        val tapEverySecond: (World) -> Input = {
            Input(
                tapped = (it.time % 1.0) < C.STEP,
                holding =
                    (it.time % 1.0) < 0.2
            )
        }
        val a = run(Engine.start(42), 20.0, tapEverySecond)
        assertEquals(a, run(Engine.start(42), 20.0, tapEverySecond))
        val other = Engine.start(43)
        assertFalse(Engine.start(42).furniture == other.furniture && Engine.start(42).chunks == other.chunks)
    }

    @Test
    fun eatingCroquetasNeverMovesTheNextThrow() {
        val calm = run(bare().copy(nextThrow = C.FIRST_THROW), 3.0)
        val angry = run(bare().copy(nextThrow = C.FIRST_THROW, eaten = 10), 3.0)
        assertEquals(calm.throws, angry.throws)
        assertEquals(calm.nextThrow, angry.nextThrow)
    }

    @Test
    fun noHighSlippersInTheFirstSeconds() {
        val w = run(bare().copy(nextThrow = 0.0), C.HIGH_FROM - 0.1) { Input.NONE }
        assertTrue(w.throws > 0)
        assertTrue(w.slippers.all { it.kind == SlipperKind.LOW })
    }

    @Test
    fun roomsFollowEachOther() {
        assertEquals(Room.KITCHEN, House.room(0))
        assertEquals(Room.HALL, House.room(House.CHUNKS_PER_ROOM))
        assertEquals(Room.LIVING_ROOM, House.room(House.CHUNKS_PER_ROOM * 2))
        assertEquals(Room.KITCHEN, House.room(House.CHUNKS_PER_ROOM * 3))
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
