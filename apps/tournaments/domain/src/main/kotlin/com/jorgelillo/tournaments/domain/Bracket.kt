package com.jorgelillo.tournaments.domain

import kotlin.random.Random

/** Knock-out brackets: any number of players, byes where it doesn't fit a power of two. */
object Bracket {

    /** Bracket size: the smallest power of two that fits [players]. */
    fun size(players: Int): Int {
        var size = 1
        while (size < players) size *= 2
        return size.coerceAtLeast(2)
    }

    fun rounds(players: Int): Int = Integer.numberOfTrailingZeros(size(players))

    /**
     * Seed positions for a bracket of [size] (1-based seeds, top to bottom), the standard way:
     * seed 1 and 2 can only meet in the final, and byes (the missing highest seeds) spread out.
     */
    fun seedOrder(size: Int): List<Int> {
        var order = listOf(1)
        while (order.size < size) {
            val n = order.size * 2 + 1
            order = order.flatMap { listOf(it, n - it) }
        }
        return order
    }

    /**
     * First-round matches plus the empty later rounds. [seeds] is the players in seed order (the
     * random draw, or a Swiss ranking for a top cut). With 6 players, seeds 1 and 2 get a bye.
     */
    fun build(seeds: List<Int>): List<Match> {
        val size = size(seeds.size)
        val order = seedOrder(size)
        val first = order.chunked(2).mapIndexed { slot, (top, bottom) ->
            Match(Stage.ELIMINATION, 1, slot, seeds.getOrNull(top - 1), seeds.getOrNull(bottom - 1))
        }
        val later = (2..rounds(seeds.size)).flatMap { r ->
            (0 until (size shr r)).map { slot -> Match(Stage.ELIMINATION, r, slot) }
        }
        return advance(first + later, bestOf = 1)
    }

    /** Random draw: seeds in a shuffled order. */
    fun draw(players: List<Player>, random: Random): List<Int> = players.map { it.id }.shuffled(random)

    /**
     * Moves every decided winner (and every round-1 bye) into the next round, and clears later
     * slots whose feeder match was undone. Pure: returns the new list.
     */
    fun advance(matches: List<Match>, bestOf: Int): List<Match> {
        val byKey = matches.filter { it.stage == Stage.ELIMINATION }.associateBy { it.round to it.slot }.toMutableMap()
        val last = byKey.keys.maxOfOrNull { it.first } ?: return matches
        for (r in 2..last) {
            for ((key, match) in byKey.filterKeys { it.first == r }) {
                val top = byKey[r - 1 to key.second * 2]?.winner(bestOf)
                val bottom = byKey[r - 1 to key.second * 2 + 1]?.winner(bestOf)
                if (match.a != top || match.b != bottom) {
                    // A feeder result changed: this match starts again.
                    byKey[key] = match.copy(a = top, b = bottom, winsA = 0, winsB = 0, comment = "")
                }
            }
        }
        return matches.filter { it.stage != Stage.ELIMINATION } +
            byKey.values.sortedWith(compareBy({ it.round }, { it.slot }))
    }
}
