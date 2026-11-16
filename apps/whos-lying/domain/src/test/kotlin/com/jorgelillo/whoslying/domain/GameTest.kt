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

    private val mysterious = GameSettings(GameMode.MYSTERIOUS)
    private val withJournalist = GameSettings(GameMode.MYSTERIOUS, journalists = 1)

    @Test
    fun originalTellsEveryoneTheirRoleAndTheHintIsTheSimilarWord() {
        for (hint in listOf(true, false)) {
            val game = deal(GameSettings(GameMode.ORIGINAL, impostors = 2, impostorHint = hint))
            val impostors = game.cards.filter { it.role == Role.IMPOSTOR }
            assertEquals(2, impostors.size)
            assertTrue(impostors.all { it.word == null && it.knowsRole }, "hint=$hint")
            assertTrue(impostors.all { it.hint == if (hint) "Piscina" else null }, "hint=$hint")
            assertTrue(game.cards.filter { it.role == Role.CIVILIAN }.all { it.word == "Playa" && it.knowsRole })
        }
    }

    @Test
    fun mysteriousImpostorGetsTheSimilarWordAndDoesNotKnow() {
        val game = deal(mysterious)
        val impostor = game.cards.single { it.role == Role.IMPOSTOR }
        assertEquals("Piscina", impostor.word)
        assertFalse(impostor.knowsRole)
        assertTrue(game.cards.filter { it.role == Role.CIVILIAN }.all { it.word == "Playa" && !it.knowsRole })
    }

    @Test
    fun drawingImpostorsGetNoWordInAnyMode() {
        for (mode in GameMode.entries) {
            val game = deal(GameSettings(mode, drawing = true), seed = 2)
            val impostor = game.cards.first { it.role == Role.IMPOSTOR }
            assertNull(impostor.word, "$mode")
            assertTrue(impostor.knowsRole, "$mode")
        }
    }

    @Test
    fun journalistsGetNoWordKnowItAndNeverStart() {
        repeat(30) { seed ->
            val game = deal(GameSettings(GameMode.MYSTERIOUS, impostors = 0, journalists = 2), seed)
            val journalists = game.cards.filter { it.role == Role.JOURNALIST }
            assertEquals(2, journalists.size)
            assertTrue(journalists.all { it.word == null && it.hint == null && it.knowsRole })
            assertEquals(0, game.cards.count { it.role == Role.IMPOSTOR })
            assertNotEquals(Role.JOURNALIST, game.cardOf(game.starter).role)
        }
    }

    @Test
    fun civiliansWinWhenEveryInfiltratorIsOut() {
        val dealt = deal(mysterious)
        val game = dealt.eliminate(dealt.cards.single { it.role == Role.IMPOSTOR }.player)
        assertEquals(Outcome.CiviliansWin, game.outcome)
        assertNull(dealt.outcome, "a move never changes the state it started from")
    }

    @Test
    fun impostorsWinWhenOneCivilianIsLeft() {
        val dealt = deal(mysterious, who = listOf("Ana", "Bea", "Carlos"))
        val game = dealt.eliminate(dealt.cards.first { it.role == Role.CIVILIAN }.player)
        assertEquals(Outcome.InfiltratorsWin(setOf(Role.IMPOSTOR)), game.outcome)
    }

    @Test
    fun eliminatedJournalistGuessesWithAccentsAndCaseIgnored() {
        val game = Game.deal(players, withJournalist, pack, Entry("Café", listOf("Té")), Random(3))
        val journalist = game.cards.single { it.role == Role.JOURNALIST }.player
        val out = game.eliminate(journalist)
        assertTrue(out.lastElimination!!.journalistMustGuess)
        val guessed = out.guess("  cafe ")
        assertTrue(guessed.journalistGuessed)
        assertEquals(Outcome.JournalistGuessed, guessed.outcome)
    }

    @Test
    fun wrongJournalistGuessKeepsTheGameGoing() {
        val dealt = deal(withJournalist)
        val game = dealt.eliminate(dealt.cards.single { it.role == Role.JOURNALIST }.player).guess("Montaña")
        assertFalse(game.journalistGuessed)
        assertNull(game.outcome)
        assertNull(game.pendingGuess, "the next vote can go ahead")
        assertEquals(4, game.alive.size)
    }

    @Test
    fun impostorsAndJournalistsAreAtMostHalfTheGroup() {
        assertEquals(1, Rules.maxInfiltrators(3))
        assertEquals(2, Rules.maxInfiltrators(4))
        assertEquals(12, Rules.maxInfiltrators(24))
        assertTrue(Rules.canStart(4, GameSettings(impostors = 2)))
        assertFalse(Rules.canStart(5, GameSettings(impostors = 3)))
        assertTrue(Rules.canStart(3, GameSettings(GameMode.MYSTERIOUS, impostors = 0, journalists = 1)))
        assertFalse(Rules.canStart(3, GameSettings(GameMode.MYSTERIOUS, impostors = 1, journalists = 1)))
        assertFalse(Rules.canStart(5, GameSettings(GameMode.MYSTERIOUS, impostors = 0, journalists = 0)))
        assertFalse(Rules.canStart(2, GameSettings()))
        assertEquals(
            GameSettings(GameMode.MYSTERIOUS, impostors = 0, journalists = 12),
            Rules.fit(GameSettings(GameMode.MYSTERIOUS, impostors = 4, journalists = 20), 24)
        )
        assertEquals(
            GameSettings(GameMode.MYSTERIOUS, impostors = 1, journalists = 0),
            Rules.fit(GameSettings(GameMode.MYSTERIOUS, impostors = 0, journalists = 0), 6)
        )
        assertEquals(GameSettings(impostors = 2), Rules.fit(GameSettings(impostors = 5, journalists = 1), 4))
    }

    @Test
    fun chaosDealsUpToTheCapAndSometimesPastItUpToEveryone() {
        val games = (0 until 400).map { seed -> deal(GameSettings(GameMode.CHAOS, impostors = 2), seed) }
        val counts = games.map { game -> game.cards.count { it.role == Role.IMPOSTOR } }
        val cap = Rules.maxInfiltrators(players.size)
        assertTrue(counts.count { it <= cap } > 200, "within the cap most of the time: ${counts.count { it <= cap }}")
        assertTrue((1..players.size).all { it in counts }, "counts seen: ${counts.toSet()}")
        assertTrue(games.all { game -> game.cards.all { it.knowsRole } && !game.scored })
        val impostorsOf = { past: Boolean ->
            games.filter { game -> (game.cards.count { it.role == Role.IMPOSTOR } > cap) == past }
                .flatMap { game -> game.cards.filter { it.role == Role.IMPOSTOR } }
        }
        assertTrue(impostorsOf(true).any { it.hint == null } && impostorsOf(true).any { it.hint == "Piscina" })
        assertTrue(impostorsOf(false).all { it.hint == "Piscina" }, "no chaos: every impostor gets the hint")
        val allImpostors = games.first { game -> game.cards.all { it.role == Role.IMPOSTOR } }
        val oneOut = allImpostors.eliminate(players.first())
        assertNull(oneOut.outcome)
        assertIs<Outcome.NoCivilians>(oneOut.reveal().outcome)

        val all24 = (0 until 600).any { seed ->
            Game.deal((1..24).map { "P$it" }, GameSettings(GameMode.CHAOS), pack, pack.entries.first(), Random(seed))
                .cards.all { it.role == Role.IMPOSTOR }
        }
        assertTrue(all24, "24 players can all be impostors")
    }

    @Test
    fun endingEarlyIsNotANoCiviliansGame() {
        val dealt = deal(withJournalist)
        val game = dealt.eliminate(dealt.cards.single { it.role == Role.IMPOSTOR }.player)
        assertNull(game.outcome)
        assertEquals(Outcome.EndedEarly, game.reveal().outcome)
    }

    @Test
    fun winnersScoreWhetherEliminatedOrNot() {
        val classic = deal(mysterious)
        val civilians = classic.cards.filter { it.role == Role.CIVILIAN }.map { it.player }
        val civiliansWin = classic.eliminate(classic.cards.single { it.role == Role.IMPOSTOR }.player)
        assertEquals(civilians.associateWith { Scoring.CIVILIAN_WIN }, Scoring.points(civiliansWin))

        val three = deal(mysterious, who = listOf("Ana", "Bea", "Carlos"))
        val impostorsWin = three.eliminate(three.cards.first { it.role == Role.CIVILIAN }.player)
        val impostor = impostorsWin.cards.single { it.role == Role.IMPOSTOR }.player
        assertEquals(mapOf(impostor to Scoring.INFILTRATOR_WIN), Scoring.points(impostorsWin))

        val journalistGame = Game.deal(
            players,
            GameSettings(GameMode.MYSTERIOUS, impostors = 0, journalists = 2),
            pack,
            Entry("Café", listOf("Té")),
            Random(3)
        )
        val journalist = journalistGame.cards.first { it.role == Role.JOURNALIST }.player
        val guessed = journalistGame.eliminate(journalist).guess("café")
        assertEquals(mapOf(journalist to Scoring.INFILTRATOR_WIN), Scoring.points(guessed), "only the one who guessed")

        val chaos = (0 until 50).map { deal(GameSettings(GameMode.CHAOS), it) }.first { game ->
            game.cards.count { it.role == Role.IMPOSTOR } == 1
        }
        assertTrue(Scoring.points(chaos.eliminate(chaos.cards.single { it.role == Role.IMPOSTOR }.player)).isEmpty())

        val ended = deal(mysterious).reveal()
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
            Game.deal(players, mysterious, pack, entry, Random(seed)).cards.single {
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
