package com.jorgelillo.tournaments.domain

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BracketTest {

    private fun names(n: Int) = (1..n).map { "P$it" }

    private fun elimination(n: Int, seed: Int = 1) =
        Tournaments.create("t", "Cup", 20_000, names(n), Format.ELIMINATION, random = Random(seed))

    @Test
    fun sizesAndRounds() {
        assertEquals(listOf(2, 2, 4, 8, 8, 16, 16), listOf(1, 2, 3, 5, 8, 11, 16).map(Bracket::size))
        assertEquals(3, Bracket.rounds(6))
        assertEquals(4, Bracket.rounds(11))
    }

    @Test
    fun standardSeedOrder() {
        assertEquals(listOf(1, 8, 4, 5, 2, 7, 3, 6), Bracket.seedOrder(8))
    }

    @Test
    fun everyPlayerOnceAndNoByeAgainstBye() {
        for (n in 2..33) {
            val t = elimination(n)
            val first = t.round(Stage.ELIMINATION, 1)
            assertEquals(Bracket.size(n) / 2, first.size, "n=$n")
            assertTrue(first.none { it.a == null && it.b == null }, "n=$n")
            assertEquals(t.players.map { it.id }.toSet(), first.flatMap { listOfNotNull(it.a, it.b) }.toSet())
            assertEquals(Bracket.size(n) - n, first.count { it.isBye })
            assertEquals(Bracket.size(n) - 1, t.matches.size)
        }
    }

    @Test
    fun sixPlayersByesGoStraightToSemis() {
        val t = elimination(6)
        val semis = t.round(Stage.ELIMINATION, 2)
        val byes = t.round(Stage.ELIMINATION, 1).filter { it.isBye }.map { it.a ?: it.b }
        assertEquals(2, byes.size)
        assertEquals(byes.toSet(), semis.flatMap { listOfNotNull(it.a, it.b) }.toSet())
        // The two players with a bye are on opposite halves, waiting for an opponent (not a bye).
        assertTrue(semis.all { (it.a == null) != (it.b == null) && !it.isBye && it.winner(3) == null })
    }

    @Test
    fun playingItOutCrownsAChampion() {
        var t = elimination(11)
        while (Tournaments.pending(t).isNotEmpty()) {
            t = Tournaments.record(t, Tournaments.pending(t).first(), 2, 1, "20 vidas a 0")
        }
        val final = t.round(Stage.ELIMINATION, 4).single()
        assertEquals(final.a, t.champion)
        assertTrue(t.isOver)
    }

    @Test
    fun changingAnEarlierResultClearsWhatFollowed() {
        var t = elimination(4)
        val (m0, m1) = t.round(Stage.ELIMINATION, 1)
        t = Tournaments.record(t, m0, 2, 0)
        t = Tournaments.record(t, m1, 2, 0)
        t = Tournaments.record(t, t.round(Stage.ELIMINATION, 2).single(), 2, 1, "close")
        assertEquals(m0.a, t.champion)

        t = Tournaments.record(t, t.round(Stage.ELIMINATION, 1)[0], 1, 2)
        val final = t.round(Stage.ELIMINATION, 2).single()
        assertEquals(m0.b, final.a)
        assertEquals(0, final.winsA + final.winsB)
        assertEquals("", final.comment)
        assertNull(t.champion)
    }

    @Test
    fun redrawOnlyBeforeResults() {
        val t = elimination(8)
        assertTrue(Tournaments.canRedraw(t))
        val redrawn = Tournaments.draw(t, Random(99))
        assertTrue(redrawn.matches != t.matches)
        val played = Tournaments.record(t, t.round(Stage.ELIMINATION, 1)[0], 1, 0)
        assertTrue(!Tournaments.canRedraw(played))
    }

    @Test
    fun bestOfOneAndFive() {
        val bo1 = Tournaments.create("t", "Cup", 0, names(2), Format.ELIMINATION, bestOf = 1)
        assertEquals(bo1.matches.single().a, Tournaments.record(bo1, bo1.matches.single(), 1, 0).champion)
        val bo5 = Tournaments.create("t", "Cup", 0, names(2), Format.ELIMINATION, bestOf = 5)
        assertNull(Tournaments.record(bo5, bo5.matches.single(), 2, 1).champion)
        assertEquals(bo5.matches.single().b, Tournaments.record(bo5, bo5.matches.single(), 2, 3).champion)
    }
}
