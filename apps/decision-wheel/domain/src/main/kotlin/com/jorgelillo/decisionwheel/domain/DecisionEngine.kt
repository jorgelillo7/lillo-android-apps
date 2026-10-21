package com.jorgelillo.decisionwheel.domain

import kotlin.math.floor

/** One slice of the wheel: its share of the circle is its real probability of winning. */
data class Segment(val option: String, val startFraction: Double, val sweepFraction: Double)

object DecisionEngine {

    /** Chance multiplier for the most recent accepted results, newest first. */
    val RECENT_PENALTIES = listOf(0.25, 0.5, 0.75)

    /** History older than this no longer lowers an option's chance. */
    const val RECENT_WINDOW_MILLIS = 30L * 24 * 60 * 60 * 1000

    /**
     * Relative weight of every option still in play. Vetoed options are left out; with
     * [avoidRepeats], options accepted recently get a lower weight (see [RECENT_PENALTIES]).
     */
    fun weights(
        options: List<String>,
        vetoed: Set<String>,
        history: List<Decision>,
        nowEpochMillis: Long,
        avoidRepeats: Boolean
    ): Map<String, Double> {
        val eligible = options.distinct().filterNot { it in vetoed }
        if (!avoidRepeats) return eligible.associateWith { 1.0 }
        val recent = history
            .filter { nowEpochMillis - it.atEpochMillis in 0..RECENT_WINDOW_MILLIS }
            .sortedByDescending { it.atEpochMillis }
            .map { it.option }
            .distinct()
            .take(RECENT_PENALTIES.size)
        return eligible.associateWith { option ->
            val rank = recent.indexOf(option)
            if (rank >= 0) RECENT_PENALTIES[rank] else 1.0
        }
    }

    /** Lays the weighted options around the circle, keeping the user's order. */
    fun segments(weights: Map<String, Double>): List<Segment> {
        val total = weights.values.sum()
        if (total <= 0.0) return emptyList()
        var start = 0.0
        return weights.map { (option, weight) ->
            val sweep = weight / total
            Segment(option, start, sweep).also { start += sweep }
        }
    }

    /**
     * Which segment sits under the pointer. The pointer is fixed at the top; [rotationDegrees]
     * is how far the wheel has turned clockwise from its rest position.
     */
    fun segmentAtPointer(segments: List<Segment>, rotationDegrees: Double): Segment? {
        if (segments.isEmpty()) return null
        val turned = ((rotationDegrees % 360.0) + 360.0) % 360.0 / 360.0
        // Turning the wheel clockwise brings earlier angles (counter-clockwise) under the pointer.
        val fraction = (1.0 - turned) % 1.0
        return segments.lastOrNull { fraction >= it.startFraction } ?: segments.first()
    }

    /**
     * Colour slot for each segment from a palette of [paletteSize] colours, cycling through it
     * and never giving the same colour to two neighbours, including last and first.
     */
    fun colorSlots(count: Int, paletteSize: Int): List<Int> {
        require(paletteSize >= 3) { "Need at least three colours" }
        if (count == 0) return emptyList()
        val slots = MutableList(count) { it % paletteSize }
        if (count > 1 && slots.last() == slots.first()) {
            slots[count - 1] = (0 until paletteSize).first { it != slots[count - 2] && it != slots[0] }
        }
        return slots
    }

    /** Number of segment boundaries crossed between two rotations (drives the "click"). */
    fun boundariesCrossed(segments: List<Segment>, fromDegrees: Double, toDegrees: Double): Int {
        if (segments.size < 2 || toDegrees <= fromDegrees) return 0
        var crossings = 0
        for (segment in segments) {
            val boundary = (1.0 - segment.startFraction) * 360.0
            crossings += (floor((toDegrees - boundary) / 360.0) - floor((fromDegrees - boundary) / 360.0)).toInt()
        }
        return crossings
    }
}
