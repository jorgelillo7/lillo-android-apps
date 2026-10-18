package com.jorgelillo.leaguecards.domain

import java.io.File
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class GameTest {

    private val sample = League.parse(File("../app/src/main/assets/league.json").readText())

    /** A league's own deck, kept outside this repository (see the app's README). */
    private val privateDeck = File("../../../../lloros-league/content/league-private.json")

    private fun card(id: String, right: Choice = Choice("Sí"), left: Choice = Choice("No"), followUpOnly: Boolean = false) =
        Card(id, "a", "texto $id", left, right, followUpOnly = followUpOnly)

    private fun league(cards: List<Card>, events: List<MatchdayEvent> = emptyList(), joker: Joker? = null) = League(
        name = "test",
        matchdays = 3,
        cardsPerMatchday = 2,
        characters = listOf(Character("a", "A", "#FF000000"), Character("b", "B", "#FF000000")),
        rivals = listOf(Rival("b")),
        cards = cards,
        events = events,
        joker = joker,
        endings = Endings("ml", "mh", "sl", "sh", "pl", "ph", "tl", "th", "champion", "{position}"),
    )

    /** Plays until the season ends, choosing with [pick]. */
    private fun play(l: League, seed: Long, pick: (Season) -> Boolean): Season {
        var s = Game.newSeason(l, seed)
        repeat(10_000) {
            if (s.over) return s
            s = if (s.reportPending) Game.dismissReport(s) else Game.choose(l, s, pick(s))
        }
        error("season never ended")
    }

    @Test
    fun `the sample deck is valid`() {
        assertEquals(emptyList(), sample.problems())
    }

    @Test
    fun `the private deck is valid when it is on this machine`() {
        if (!privateDeck.exists()) return
        val deck = League.parse(privateDeck.readText())
        assertEquals(emptyList(), deck.problems())
        val careful = finished(deck) { careful(deck) }
        val random = finished(deck) { seed -> Random(seed).let { r -> { r.nextBoolean() } } }
        println("private deck: careful $careful / 200, random $random / 200 reach the last matchday")
        assertTrue(careful >= 120, "careful play finished only $careful of 200 seasons")
    }

    @Test
    fun `problems catch broken references`() {
        val broken = league(listOf(card("x", right = Choice("Sí", followUp = "missing")).copy(character = "nobody")))
        val problems = broken.problems()
        assertTrue(problems.any { "unknown character 'nobody'" in it }, problems.toString())
        assertTrue(problems.any { "unknown follow-up 'missing'" in it }, problems.toString())
    }

    /** Picks the side that leaves the meters nearest the middle: how a careful player swipes. */
    private fun careful(l: League): (Season) -> Boolean = { s ->
        val card = l.card(s.currentCard!!)!!
        fun distance(c: Choice) = (s.meters + c.effects).let { m -> Meter.entries.sumOf { kotlin.math.abs(m[it] - 50) } }
        distance(card.right) < distance(card.left)
    }

    private fun finished(l: League, pick: (Long) -> (Season) -> Boolean): Int = (0 until 200).count { seed ->
        play(l, seed.toLong(), pick(seed.toLong())).ending!!.position != null
    }

    @Test
    fun `a careful player usually finishes the season, a random one sometimes`() {
        val careful = finished(sample) { careful(sample) }
        val random = finished(sample) { seed -> Random(seed).let { r -> { r.nextBoolean() } } }
        println("sample deck: careful $careful / 200, random $random / 200 reach the last matchday")
        assertTrue(careful >= 120, "careful play finished only $careful of 200 seasons")
        assertTrue(random in 5..120, "random play finished $random of 200 seasons")
    }

    @Test
    fun `same seed and choices give the same season`() {
        val a = play(sample, 42) { it.handIndex % 2 == 0 }
        val b = play(sample, 42) { it.handIndex % 2 == 0 }
        assertEquals(a, b)
    }

    @Test
    fun `a follow-up is dealt first next matchday`() {
        val l = league(listOf(card("x", right = Choice("Sí", followUp = "f")), card("y"), card("f", followUpOnly = true)))
        var s = Game.newSeason(l, 1)
        assertFalse("f" in s.hand)
        s = Game.choose(l, s, right = s.hand[0] == "x")
        s = Game.choose(l, s, right = s.hand[1] == "x")
        assertTrue(s.reportPending)
        assertEquals("f", Game.dismissReport(s).currentCard)
    }

    @Test
    fun `a gamble that always goes wrong applies its own effects and outcome`() {
        val risky = Choice("Corazonada", Effects(points = 20), risk = Risk(1.0, Effects(points = -16), "‑16"))
        val l = league(listOf(card("x", right = risky), card("y")))
        var s = Game.newSeason(l, 3)
        val first = s.hand[0]
        s = Game.choose(l, s, right = true)
        if (first == "x") {
            assertEquals(-16, s.matchdayPoints)
            assertEquals("‑16", s.lastOutcome?.text)
        }
    }

    @Test
    fun `an invalid line-up scores nothing`() {
        val l = league(listOf(card("x"), card("y")), events = listOf(MatchdayEvent("i", "indebida", 1.0, noPoints = true)))
        var s = Game.newSeason(l, 5)
        s = Game.choose(l, Game.choose(l, s, true), true)
        assertEquals(0, s.report!!.yourPoints)
        assertEquals("indebida", s.report!!.events.single().text)
    }

    @Test
    fun `the joker skips a card once a season`() {
        val l = league(listOf(card("x"), card("y"), card("z")), joker = Joker("a", "Borrarme", "Me borro", Effects(patience = -10)))
        var s = Game.newSeason(l, 7)
        assertTrue(Game.canUseJoker(l, s))
        s = Game.useJoker(l, s)
        assertEquals(40, s.meters.patience)
        assertEquals(1, s.handIndex)
        assertFalse(Game.canUseJoker(l, s))
    }

    @Test
    fun `a meter at zero ends the season with its ending`() {
        val l = league(listOf(card("x", right = Choice("Todo", Effects(money = -100))), card("y", right = Choice("Todo", Effects(money = -100)))))
        val s = Game.choose(l, Game.newSeason(l, 9), right = true)
        assertEquals(Ending("ml", Meter.MONEY, high = false), s.ending)
    }

    @Test
    fun `the last matchday ends with the final position`() {
        val l = league(listOf(card("x"), card("y")))
        val end = play(l, 11) { false }
        val position = end.ending!!.position!!
        assertTrue(position in 1..2)
        assertEquals(if (position == 1) "champion" else "2", end.ending!!.text)
        assertEquals(4, end.matchday)
    }
}
