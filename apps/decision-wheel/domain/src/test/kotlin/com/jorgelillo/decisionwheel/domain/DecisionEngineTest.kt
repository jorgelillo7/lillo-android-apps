package com.jorgelillo.decisionwheel.domain

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class DecisionEngineTest {

    private val day = 24L * 60 * 60 * 1000
    private val now = 1_000 * day
    private val options = listOf("VIPS", "Goiko", "Ginos", "Sushi")

    private fun decided(option: String, daysAgo: Long) = Decision("w", option, now - daysAgo * day)

    @Test
    fun everyOptionHasTheSameWeightWithoutHistory() {
        val weights = DecisionEngine.weights(options, emptySet(), emptyList(), now, avoidRepeats = true)
        assertEquals(options.associateWith { 1.0 }, weights)
    }

    @Test
    fun recentResultsGetLessChanceNewestFirst() {
        val history = listOf(decided("Goiko", 3), decided("VIPS", 1), decided("Ginos", 6))
        val weights = DecisionEngine.weights(options, emptySet(), history, now, avoidRepeats = true)
        assertEquals(mapOf("VIPS" to 0.25, "Goiko" to 0.5, "Ginos" to 0.75, "Sushi" to 1.0), weights)
    }

    /** Only the three newest distinct results are penalised: a fourth, older one weighs 1.0 again. */
    @Test
    fun onlyTheThreeNewestDistinctResultsArePenalised() {
        val history = listOf(
            decided("Sushi", 9),
            decided("VIPS", 1),
            decided("VIPS", 2),
            decided("Goiko", 3),
            decided("Ginos", 6)
        )
        val weights = DecisionEngine.weights(options, emptySet(), history, now, avoidRepeats = true)
        assertEquals(mapOf("VIPS" to 0.25, "Goiko" to 0.5, "Ginos" to 0.75, "Sushi" to 1.0), weights)
    }

    @Test
    fun oldHistoryAndDisabledSettingDoNotPenalise() {
        val old = listOf(decided("VIPS", 40))
        assertEquals(1.0, DecisionEngine.weights(options, emptySet(), old, now, avoidRepeats = true)["VIPS"])
        val recent = listOf(decided("VIPS", 1))
        assertEquals(1.0, DecisionEngine.weights(options, emptySet(), recent, now, avoidRepeats = false)["VIPS"])
    }

    @Test
    fun vetoedOptionsAreOutAndOrderIsKept() {
        val weights = DecisionEngine.weights(options, setOf("Ginos"), emptyList(), now, avoidRepeats = true)
        assertEquals(listOf("VIPS", "Goiko", "Sushi"), weights.keys.toList())
    }

    @Test
    fun segmentsShareTheCircleByWeight() {
        val segments = DecisionEngine.segments(mapOf("a" to 1.0, "b" to 0.25, "c" to 0.75))
        assertEquals(listOf(0.5, 0.125, 0.375), segments.map { it.sweepFraction })
        assertEquals(listOf(0.0, 0.5, 0.625), segments.map { it.startFraction })
    }

    @Test
    fun pointerReadsTheSegmentUnderTheTop() {
        val segments = DecisionEngine.segments(mapOf("a" to 1.0, "b" to 1.0, "c" to 1.0, "d" to 1.0))
        assertEquals("a", DecisionEngine.segmentAtPointer(segments, 0.0)?.option)
        // Turning clockwise by 100° brings what was at 260° (c: 180°–270°) under the pointer.
        assertEquals("c", DecisionEngine.segmentAtPointer(segments, 100.0)?.option)
        assertEquals("b", DecisionEngine.segmentAtPointer(segments, 190.0)?.option)
        assertEquals("d", DecisionEngine.segmentAtPointer(segments, 10.0)?.option)
        assertEquals("a", DecisionEngine.segmentAtPointer(segments, 360.0 * 5 + 10.0 - 10.0)?.option)
    }

    @Test
    fun uniformLandingMatchesTheWeights() {
        val segments = DecisionEngine.segments(mapOf("big" to 1.0, "small" to 0.25))
        val random = Random(42)
        val wins = List(20_000) {
            DecisionEngine.segmentAtPointer(segments, Spin.plan(0.0, random).targetDegrees)!!.option
        }.groupingBy { it }.eachCount()
        val share = wins.getValue("small") / 20_000.0
        assertTrue(share in 0.18..0.22, "small won $share of spins, expected 0.2")
    }

    @Test
    fun neighboursNeverShareAColour() {
        for (count in 1..20) {
            val slots = DecisionEngine.colorSlots(count, paletteSize = 6)
            assertEquals(count, slots.size)
            if (count > 1) {
                (slots + slots.first()).zipWithNext().forEach { (a, b) -> assertNotEquals(a, b, "count=$count $slots") }
            }
        }
    }

    @Test
    fun countsBoundariesForTheClick() {
        val segments = DecisionEngine.segments(mapOf("a" to 1.0, "b" to 1.0, "c" to 1.0, "d" to 1.0))
        assertEquals(0, DecisionEngine.boundariesCrossed(segments, 10.0, 80.0))
        assertEquals(1, DecisionEngine.boundariesCrossed(segments, 80.0, 100.0))
        assertEquals(8, DecisionEngine.boundariesCrossed(segments, 0.5, 720.5))
    }
}
