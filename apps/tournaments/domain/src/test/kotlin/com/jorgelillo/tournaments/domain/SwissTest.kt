package com.jorgelillo.tournaments.domain

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SwissTest {

    private fun swiss(n: Int, topCut: Int = 0, seed: Int = 1) = Tournaments.create(
        "t",
        "League",
        0,
        (1..n).map { "P$it" },
        Format.SWISS,
        topCut = topCut,
        random = Random(seed)
    )

    /** Plays every pending match: the player with the lower id wins 2-0. */
    private fun playRound(t: Tournament): Tournament = Tournaments.pending(t).fold(t) { acc, m ->
        if (m.a!! < m.b!!) Tournaments.record(acc, m, 2, 0) else Tournaments.record(acc, m, 0, 2)
    }

    @Test
    fun recommendedRounds() {
        assertEquals(listOf(1, 2, 3, 3, 4, 5), listOf(2, 4, 6, 8, 11, 32).map(Swiss::recommendedRounds))
    }

    @Test
    fun noRematchesAndOneByeEach() {
        for (n in 3..12) {
            for (seed in 1..5) {
                var t = swiss(n, seed = seed)
                while (true) {
                    t = playRound(t)
                    if (!Tournaments.canPairNextRound(t)) break
                    t = Tournaments.pairNextRound(t, Random(seed))
                }
                assertTrue(t.isSwissOver, "n=$n")
                val real = t.matches.filter { !it.isBye }.map { setOf(it.a, it.b) }
                assertEquals(real.size, real.toSet().size, "rematch n=$n seed=$seed")
                val byes = t.matches.filter { it.isBye }.map { it.a }
                assertEquals(byes.size, byes.toSet().size, "double bye n=$n seed=$seed")
                assertEquals(if (n % 2 == 1) t.swissRounds else 0, byes.size)
                assertEquals(t.players.size, Standings.of(t).size)
            }
        }
    }

    @Test
    fun byeCountsAsAWin() {
        val t = playRound(swiss(5))
        val byePlayer = t.round(Stage.SWISS, 1).single { it.isBye }.a
        val row = Standings.of(t).single { it.player == byePlayer }
        assertEquals(Standings.WIN, row.points)
    }

    @Test
    fun drawsAndTieBreakers() {
        var t = swiss(4, seed = 3)
        val (m0, m1) = t.round(Stage.SWISS, 1)
        t = Tournaments.record(t, m0, 1, 1, draw = true)
        t = Tournaments.record(t, m1, 2, 1)
        val table = Standings.of(t)
        assertEquals(m1.a, table.first().player)
        assertEquals(listOf(3, 1, 1, 0), table.map { it.points })
        assertTrue(table.first().gameWinRate > 0.6)
    }

    @Test
    fun swissWithoutCutEndsWithTheLeader() {
        var t = swiss(4)
        t = playRound(t)
        assertNull(t.champion)
        t = playRound(Tournaments.pairNextRound(t))
        assertFalse(Tournaments.canPairNextRound(t))
        assertEquals(1, t.champion)
    }

    @Test
    fun topCutSeedsTheBestIntoABracket() {
        var t = swiss(8, topCut = 4)
        repeat(t.swissRounds) { i ->
            if (i > 0) t = Tournaments.pairNextRound(t)
            t = playRound(t)
        }
        assertTrue(Tournaments.canStartTopCut(t))
        t = Tournaments.startTopCut(t)
        val seeds = Swiss.topCut(t, 4)
        val semis = t.round(Stage.ELIMINATION, 1)
        assertEquals(setOf(seeds[0], seeds[3]), setOf(semis[0].a, semis[0].b))
        while (Tournaments.pending(t).isNotEmpty()) t = playRound(t)
        assertEquals(1, t.champion)
    }

    @Test
    fun unpairWhileTheRoundIsUntouched() {
        var t = playRound(swiss(6))
        t = Tournaments.pairNextRound(t)
        assertTrue(Tournaments.canUnpairLastRound(t))
        assertEquals(1, Tournaments.currentSwissRound(Tournaments.unpairLastRound(t)))
        t = Tournaments.record(t, Tournaments.pending(t).first(), 1, 0)
        assertFalse(Tournaments.canUnpairLastRound(t))
    }
}
