package com.jorgelillo.whoslying.domain

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GameTest {

    private val players = listOf("Ana", "Bea", "Carlos", "Dani", "Eva")
    private val pack =
        WordPack("p", "Lugares", "🗺️", listOf(Entry("Playa", listOf("Piscina")), Entry("Cine", listOf("Teatro"))))

    private fun deal(settings: GameSettings, seed: Int = 1, who: List<String> = players) =
        Game.deal(who, settings, pack, pack.entries.first(), Random(seed))

    @Test
    fun classicImpostorGetsTheDecoyAndDoesNotKnow() {
        val game = deal(GameSettings(GameMode.CLASSIC, impostors = 1))
        val impostor = game.cards.single { it.role == Role.IMPOSTOR }
        assertEquals("Piscina", impostor.word)
        assertFalse(impostor.knowsRole)
        assertTrue(game.cards.filter { it.role == Role.CIVILIAN }.all { it.word == "Playa" })
    }

    @Test
    fun blindImpostorGetsNoWordAndNoHintEvenWithTheHintOn() {
        for (drawing in listOf(false, true)) {
            val game = deal(GameSettings(GameMode.BLIND, impostors = 2, categoryHint = true, drawing = drawing))
            val impostors = game.cards.filter { it.role == Role.IMPOSTOR }
            assertEquals(2, impostors.size)
            assertTrue(impostors.all { it.word == null && it.hint == null && it.knowsRole }, "drawing=$drawing")
        }
    }

    @Test
    fun drawingImpostorsGetNoWordInAnyMode() {
        for (mode in GameMode.entries) {
            val game = deal(GameSettings(mode, drawing = true))
            val impostor = game.cards.first { it.role == Role.IMPOSTOR }
            assertNull(impostor.word, "$mode")
            assertTrue(impostor.knowsRole)
            assertEquals(if (mode == GameMode.BLIND) null else "Lugares", impostor.hint, "$mode")
        }
    }

    @Test
    fun drifterModeAddsOneDrifterWhoNeverStarts() {
        repeat(30) { seed ->
            val game = deal(GameSettings(GameMode.DRIFTER), seed)
            assertEquals(1, game.cards.count { it.role == Role.DRIFTER })
            assertNotEquals(Role.DRIFTER, game.cardOf(game.starter).role)
        }
    }

    @Test
    fun civiliansWinWhenEveryInfiltratorIsOut() {
        val dealt = deal(GameSettings(GameMode.CLASSIC))
        val game = dealt.eliminate(dealt.cards.single { it.role == Role.IMPOSTOR }.player)
        assertEquals(Outcome.CiviliansWin, game.outcome)
        assertNull(dealt.outcome, "a move never changes the state it started from")
    }

    @Test
    fun impostorsWinWhenOneCivilianIsLeft() {
        val dealt = deal(GameSettings(GameMode.CLASSIC), who = listOf("Ana", "Bea", "Carlos"))
        val game = dealt.eliminate(dealt.cards.first { it.role == Role.CIVILIAN }.player)
        assertEquals(Outcome.InfiltratorsWin(setOf(Role.IMPOSTOR)), game.outcome)
    }

    @Test
    fun eliminatedDrifterGuessesWithAccentsAndCaseIgnored() {
        val game = Game.deal(players, GameSettings(GameMode.DRIFTER), pack, Entry("Café", listOf("Té")), Random(3))
        val drifter = game.cards.single { it.role == Role.DRIFTER }.player
        val out = game.eliminate(drifter)
        assertTrue(out.lastElimination!!.drifterMustGuess)
        val guessed = out.guess("  cafe ")
        assertTrue(guessed.drifterGuessed)
        assertEquals(Outcome.DrifterGuessed, guessed.outcome)
    }

    @Test
    fun wrongDrifterGuessKeepsTheGameGoing() {
        val dealt = deal(GameSettings(GameMode.DRIFTER))
        val game = dealt.eliminate(dealt.cards.single { it.role == Role.DRIFTER }.player).guess("Montaña")
        assertFalse(game.drifterGuessed)
        assertNull(game.outcome)
        assertNull(game.pendingGuess, "the next vote can go ahead")
        assertEquals(4, game.alive.size)
    }

    @Test
    fun impostorLimitsKeepCiviliansAhead() {
        assertEquals(1, Rules.maxImpostors(3, GameMode.CLASSIC))
        assertEquals(2, Rules.maxImpostors(5, GameMode.CLASSIC))
        assertEquals(1, Rules.maxImpostors(4, GameMode.DRIFTER))
        assertFalse(Rules.canStart(3, GameSettings(GameMode.DRIFTER)))
        assertFalse(Rules.canStart(5, GameSettings(impostors = 3)))
    }

    @Test
    fun chaosSometimesMakesEveryoneAnImpostor() {
        val counts = (0 until 400).map { seed ->
            deal(GameSettings(GameMode.BLIND, chaos = true), seed).cards.count { it.role == Role.IMPOSTOR }
        }.toSet()
        assertTrue(1 in counts && players.size in counts, "counts seen: $counts")
        val allImpostors = (0 until 400).map { deal(GameSettings(GameMode.BLIND, chaos = true), it) }
            .first { game -> game.cards.all { it.role == Role.IMPOSTOR } }
        val oneOut = allImpostors.eliminate(players.first())
        assertNull(oneOut.outcome)
        assertIs<Outcome.NoCivilians>(oneOut.reveal().outcome)
    }

    @Test
    fun endingEarlyIsNotANoCiviliansGame() {
        val dealt = deal(GameSettings(GameMode.DRIFTER))
        val game = dealt.eliminate(dealt.cards.single { it.role == Role.IMPOSTOR }.player)
        assertNull(game.outcome)
        assertEquals(Outcome.EndedEarly, game.reveal().outcome)
    }

    @Test
    fun winnersScoreWhetherEliminatedOrNot() {
        val classic = deal(GameSettings(GameMode.CLASSIC))
        val civilians = classic.cards.filter { it.role == Role.CIVILIAN }.map { it.player }
        val civiliansWin = classic.eliminate(classic.cards.single { it.role == Role.IMPOSTOR }.player)
        assertEquals(civilians.associateWith { Scoring.CIVILIAN_WIN }, Scoring.points(civiliansWin))

        val three = deal(GameSettings(GameMode.CLASSIC), who = listOf("Ana", "Bea", "Carlos"))
        val impostorsWin = three.eliminate(three.cards.first { it.role == Role.CIVILIAN }.player)
        val impostor = impostorsWin.cards.single { it.role == Role.IMPOSTOR }.player
        assertEquals(mapOf(impostor to Scoring.INFILTRATOR_WIN), Scoring.points(impostorsWin))

        val drifterGame = Game.deal(
            players,
            GameSettings(GameMode.DRIFTER),
            pack,
            Entry("Café", listOf("Té")),
            Random(3)
        )
        val drifter = drifterGame.cards.single { it.role == Role.DRIFTER }.player
        val guessed = drifterGame.eliminate(drifter).guess("café")
        assertEquals(mapOf(drifter to Scoring.INFILTRATOR_WIN), Scoring.points(guessed))

        val ended = deal(GameSettings(GameMode.CLASSIC)).reveal()
        assertTrue(Scoring.points(ended).isEmpty())
    }

    @Test
    fun matchEndsAfterTheChosenRoundsAndTiesShareTheWin() {
        assertFalse(Match.isOver(9, 10))
        assertTrue(Match.isOver(10, 10))
        assertFalse(Match.isOver(99, 0))
        assertEquals(3, Match.remaining(7, 10))
        assertFalse(Match.counts(Outcome.EndedEarly))
        assertTrue(Match.counts(Outcome.NoCivilians))
        assertEquals(listOf("Ana", "Bea"), Match.leaders(mapOf("Bea" to 20, "Ana" to 20, "Carlos" to 5)))
        assertTrue(Match.leaders(mapOf("Ana" to 0)).isEmpty())
        assertEquals(10, Match.recommendedRounds(4))
        assertEquals(5, Match.recommendedRounds(10))
        assertTrue(Match.recommendedRounds(24) in Match.ROUND_OPTIONS)
        assertEquals(240, Rules.recommendedTimer(4))
        assertTrue((3..24).all { Rules.recommendedTimer(it) in Rules.TIMER_OPTIONS })
    }

    @Test
    fun pickerAvoidsRecentWords() {
        val (_, entry) = WordPicker.pick(listOf(pack), recent = listOf("Playa"), random = Random(1))
        assertEquals("Cine", entry.word)
    }

    @Test
    fun noWordRepeatsUntilAlmostAllWerePlayed() {
        val big = WordPack("big", "Big", "🔢", (1..100).map { Entry("w$it", listOf("d$it")) })
        val recent = mutableListOf<String>()
        repeat(90) { round ->
            val (_, entry) = WordPicker.pick(listOf(big), recent, Random(round))
            assertFalse(entry.word in recent, "repeated ${entry.word} after $round games")
            recent.add(0, entry.word)
        }
        assertEquals(10 to 100, WordPicker.unplayed(listOf(big), recent))
        // From here on, the oldest words come back first, never the ones just played.
        repeat(50) { round ->
            val (_, entry) = WordPicker.pick(listOf(big), recent, Random(1_000 + round))
            assertFalse(entry.word in recent.take(89), "${entry.word} came back too soon")
            recent.remove(entry.word)
            recent.add(0, entry.word)
        }
    }

    @Test
    fun impostorGetsOneOfSeveralSimilarWords() {
        val entry = Entry("Playa", listOf("Piscina", "Lago", "Río"))
        val decoys = (0 until 60).map { seed ->
            Game.deal(players, GameSettings(GameMode.CLASSIC), pack, entry, Random(seed)).cards.single {
                it.role ==
                    Role.IMPOSTOR
            }.word
        }.toSet()
        assertEquals(setOf("Piscina", "Lago", "Río"), decoys)
    }

    @Test
    fun parsesSeveralSimilarWordsPerLine() {
        val entries = WordPacks.parseEntries("Playa / Piscina / lago / Playa\n\n  Cine  \nplaya / Mar")
        assertEquals(listOf(Entry("Playa", listOf("Piscina", "lago")), Entry("Cine")), entries)
        assertEquals("Playa / Piscina / lago", WordPacks.toLine(entries.first()))
    }

    @Test
    fun builtInPacksAreWellFormed() {
        for (language in listOf("es", "en")) {
            val packs = WordPacks.builtIn(language)
            assertEquals(21, packs.size)
            assertEquals(packs.size, packs.map { it.id }.toSet().size)
            for (p in packs) {
                assertTrue(p.entries.size >= 30, "${p.name} has ${p.entries.size} words")
                p.entries.forEach { e ->
                    assertEquals(2, e.decoys.size, "similar words for ${e.word}")
                    e.decoys.forEach { assertNotEquals(Rules.normalize(e.word), Rules.normalize(it)) }
                }
            }
            // A word is played once per language, whatever pack it comes from.
            val words = packs.flatMap { p -> p.entries.map { Rules.normalize(it.word) } }
            assertEquals(
                words.groupingBy {
                    it
                }.eachCount().filterValues { it > 1 },
                emptyMap(),
                "repeated words in $language"
            )
        }
        // Both languages offer the same packs, in the same order.
        assertEquals(WordPacks.builtIn("es").map { it.id }, WordPacks.builtIn("en").map { it.id })
    }
}
