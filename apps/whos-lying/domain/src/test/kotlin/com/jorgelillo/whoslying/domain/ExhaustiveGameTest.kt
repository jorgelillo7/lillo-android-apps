package com.jorgelillo.whoslying.domain

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * Every setting combination (mode × drawing × category hint × chaos × players × impostors), dealt
 * from the real built-in packs and played to the end with random votes, guesses and early
 * endings. Checks the deal, that every game ends, and that outcome and points always agree with
 * who is left.
 */
class ExhaustiveGameTest {

    private val packs = WordPacks.builtIn("es") + WordPacks.builtIn("en")
    private val playerCounts = (3..12) + listOf(16, 24)
    private val outcomesSeen = mutableMapOf<String, Int>()

    private fun names(n: Int) = (1..n).map { "P$it" }

    @Test
    fun everyCombinationDealsRightAndEveryGameEndsConsistently() {
        var games = 0
        for (mode in GameMode.entries) for (drawing in listOf(false, true)) for (hint in listOf(false, true)) for (chaos in listOf(false, true)) {
            for (n in playerCounts) {
                if (n < Rules.minPlayers(mode)) {
                    assertFalse(Rules.canStart(n, GameSettings(mode)), "$mode should not start with $n players")
                    continue
                }
                val max = Rules.maxImpostors(n, mode)
                assertFalse(Rules.canStart(n, GameSettings(mode, impostors = max + 1)), "$mode $n players: ${max + 1} impostors")
                for (impostors in 1..max) {
                    val settings = GameSettings(mode, impostors, hint, chaos, drawing = drawing)
                    repeat(SEEDS) { seed ->
                        val random = Random(seed * 7919 + n * 31 + impostors)
                        val (pack, entry) = WordPicker.pick(packs, emptyList(), random)
                        val game = Game.deal(names(n), settings, pack, entry, random)
                        checkDeal(game, settings, n, pack, entry)
                        playOut(game, random)
                        games++
                    }
                }
            }
        }
        println("exhaustive: $games games, outcomes $outcomesSeen")
        // Every kind of ending showed up somewhere in the matrix.
        for (kind in listOf("CiviliansWin", "Impostors", "Drifter", "ImpostorsAndDrifter", "DrifterGuessed", "NoCivilians", "EndedEarly")) {
            assertTrue((outcomesSeen[kind] ?: 0) > 0, "never saw $kind")
        }
    }

    private fun checkDeal(game: Game, settings: GameSettings, n: Int, pack: WordPack, entry: Entry) {
        val ctx = "$settings n=$n word=${entry.word}"
        assertEquals(names(n), game.cards.map { it.player }, ctx)
        val impostors = game.cards.count { it.role == Role.IMPOSTOR }
        val drifters = game.cards.count { it.role == Role.DRIFTER }
        val allImpostors = impostors == n
        if (settings.chaos) {
            assertTrue(impostors in 1..Rules.maxImpostors(n, settings.mode) || allImpostors, "$ctx: chaos gave $impostors")
        } else {
            assertEquals(settings.impostors, impostors, ctx)
        }
        assertEquals(if (settings.mode == GameMode.DRIFTER && !allImpostors) 1 else 0, drifters, ctx)
        if (!allImpostors) assertTrue(game.cards.count { it.role == Role.CIVILIAN } > impostors, "$ctx: civilians must outnumber impostors")

        for (card in game.cards) {
            when (card.role) {
                Role.CIVILIAN -> assertEquals(entry.word, card.word, ctx)
                Role.DRIFTER -> assertNull(card.word, ctx)
                Role.IMPOSTOR -> if (settings.mode == GameMode.BLIND || settings.drawing) {
                    assertNull(card.word, "$ctx: blind / drawing impostors get no word")
                } else {
                    assertNotNull(card.word, ctx)
                    assertNotEquals(entry.word, card.word, "$ctx: the decoy must differ from the word")
                    assertTrue(card.word in entry.decoys || pack.entries.any { it.word == card.word }, ctx)
                }
            }
            assertEquals(if (card.word == null && settings.categoryHint) pack.name else null, card.hint, "$ctx: hint for ${card.role}")
            assertEquals(card.role != Role.CIVILIAN && card.word == null, card.knowsRole, ctx)
        }
        assertNotEquals(Role.DRIFTER, game.cardOf(game.starter).role, "$ctx: the drifter never starts")
        assertEquals(entry.word, game.civilianWord)
        assertEquals(pack.name, game.category)
    }

    /** Votes at random until the game ends; sometimes the group ends it early instead. */
    private fun playOut(game: Game, random: Random) {
        val endEarlyAfter = if (random.nextInt(6) == 0) random.nextInt(game.cards.size) else Int.MAX_VALUE
        var votes = 0
        while (!game.isOver) {
            if (votes >= endEarlyAfter) {
                game.reveal()
                break
            }
            if (votes > game.cards.size || game.alive.isEmpty()) fail("game never ended: ${game.cards.map { it.role }}, alive ${game.alive.size}")
            val out = game.alive.random(random).player
            val elimination = game.eliminate(out)
            votes++
            if (elimination.drifterMustGuess) {
                assertEquals(out, game.pendingGuess)
                val right = random.nextBoolean()
                assertEquals(right, game.guess(if (right) " " + game.civilianWord.uppercase() + " " else "nope"))
                if (right) {
                    assertEquals(Outcome.DrifterGuessed, game.outcome)
                    break
                }
            }
            checkOutcome(game)
        }
        checkOutcome(game)
        checkPoints(game)
        assertTrue(Match.counts(game.outcome) == (game.outcome != Outcome.EndedEarly))
    }

    private fun checkOutcome(game: Game) {
        val civilians = game.alive.count { it.role == Role.CIVILIAN }
        val infiltrators = game.alive.filter { it.role != Role.CIVILIAN }.map { it.role }.toSet()
        val noCivilians = game.cards.none { it.role == Role.CIVILIAN }
        when (val outcome = game.outcome) {
            null -> {
                assertTrue(infiltrators.isNotEmpty() && civilians > 1 || noCivilians && game.alive.size > 1, "unfinished game should be over: ${game.alive}")
            }
            Outcome.CiviliansWin -> assertTrue(infiltrators.isEmpty())
            is Outcome.InfiltratorsWin -> {
                assertTrue(civilians <= 1, "infiltrators won with $civilians civilians left")
                assertEquals(infiltrators, outcome.roles)
            }
            Outcome.DrifterGuessed -> assertTrue(game.cards.any { it.role == Role.DRIFTER })
            Outcome.NoCivilians -> assertTrue(noCivilians && (game.alive.size <= 1 || game.pendingGuess == null))
            Outcome.EndedEarly -> assertTrue(infiltrators.isNotEmpty() && civilians > 1 && !noCivilians)
        }
        game.outcome?.let { outcomesSeen.merge(kind(it), 1, Int::plus) }
    }

    private fun kind(outcome: Outcome): String = when (outcome) {
        is Outcome.InfiltratorsWin -> when (outcome.roles) {
            setOf(Role.IMPOSTOR) -> "Impostors"
            setOf(Role.DRIFTER) -> "Drifter"
            else -> "ImpostorsAndDrifter"
        }
        else -> outcome::class.simpleName!!
    }

    private fun checkPoints(game: Game) {
        val points = Scoring.points(game)
        val expected = when (val outcome = game.outcome) {
            Outcome.CiviliansWin -> game.cards.filter { it.role == Role.CIVILIAN }.associate { it.player to Scoring.CIVILIAN_WIN }
            is Outcome.InfiltratorsWin -> game.cards.filter { it.role in outcome.roles }.associate { it.player to Scoring.INFILTRATOR_WIN }
            Outcome.DrifterGuessed -> game.cards.filter { it.role == Role.DRIFTER }.associate { it.player to Scoring.INFILTRATOR_WIN }
            else -> emptyMap()
        }
        assertEquals(expected, points, "points for ${game.outcome}")
    }

    @Test
    fun impostorAndDrifterWinTogetherWhenOneCivilianIsLeft() {
        // 5 players in DRIFTER mode: 1 impostor, 1 drifter, 3 civilians. Vote two civilians out.
        val game = dealUntil(GameMode.DRIFTER, 5) { true }
        game.cards.filter { it.role == Role.CIVILIAN }.take(2).forEach { game.eliminate(it.player) }
        assertEquals(Outcome.InfiltratorsWin(setOf(Role.IMPOSTOR, Role.DRIFTER)), game.outcome)
        assertEquals(setOf(Scoring.INFILTRATOR_WIN), Scoring.points(game).values.toSet())
        assertEquals(2, Scoring.points(game).size)
    }

    @Test
    fun theDrifterWinsAloneWhenTheImpostorWasCaught() {
        val game = dealUntil(GameMode.DRIFTER, 5) { true }
        game.eliminate(game.cards.single { it.role == Role.IMPOSTOR }.player)
        assertNull(game.outcome, "the drifter is still in")
        game.cards.filter { it.role == Role.CIVILIAN }.take(2).forEach { game.eliminate(it.player) }
        assertEquals(Outcome.InfiltratorsWin(setOf(Role.DRIFTER)), game.outcome)
        assertEquals(mapOf(game.cards.single { it.role == Role.DRIFTER }.player to Scoring.INFILTRATOR_WIN), Scoring.points(game))
    }

    @Test
    fun aWrongGuessByTheLastInfiltratorHandsTheWinToTheCivilians() {
        val game = dealUntil(GameMode.DRIFTER, 5) { true }
        game.eliminate(game.cards.single { it.role == Role.IMPOSTOR }.player)
        game.eliminate(game.cards.single { it.role == Role.DRIFTER }.player)
        assertFalse(game.guess("nope"))
        assertEquals(Outcome.CiviliansWin, game.outcome)
    }

    @Test
    fun anAllImpostorGameEndsWhenTheVotesLeaveOnePlayer() {
        val random = Random(3)
        val (pack, entry) = WordPicker.pick(packs, emptyList(), random)
        // Chaos picks "everyone" now and then: find such a deal.
        val game = generateSequence { Game.deal(names(4), GameSettings(GameMode.CLASSIC, chaos = true), pack, entry, random) }
            .first { g -> g.cards.all { it.role == Role.IMPOSTOR } }
        repeat(2) { game.eliminate(game.alive.first().player); assertNull(game.outcome) }
        game.eliminate(game.alive.first().player)
        assertEquals(Outcome.NoCivilians, game.outcome)
        assertEquals(1, game.alive.size)
    }

    @Test
    fun votingStopsAsSoonAsSomeoneWins() {
        val game = dealUntil(GameMode.DRIFTER, 4) { true }
        game.eliminate(game.cards.first { it.role == Role.CIVILIAN }.player)
        assertEquals(Outcome.InfiltratorsWin(setOf(Role.IMPOSTOR, Role.DRIFTER)), game.outcome)
        assertTrue(runCatching { game.eliminate(game.alive.first().player) }.isFailure, "no votes after the end")
    }

    @Test
    fun aMatchCountsOnlyFinishedGamesAndCrownsTheLeaders() {
        val scores = mutableMapOf<String, Int>()
        var played = 0
        val random = Random(42)
        // A game ended early first: no points, no round used.
        val early = dealUntil(GameMode.DRIFTER, 6, random) { true }.apply { reveal() }
        assertEquals(Outcome.EndedEarly, early.outcome)
        assertFalse(Match.counts(early.outcome))
        assertEquals(emptyMap(), Scoring.points(early))
        while (!Match.isOver(played, 3)) {
            val game = dealUntil(GameMode.DRIFTER, 6, random) { true }
            while (!game.isOver) if (game.eliminate(game.alive.random(random).player).drifterMustGuess) game.guess("nope")
            Scoring.points(game).forEach { (p, pts) -> scores.merge(p, pts, Int::plus) }
            if (Match.counts(game.outcome)) played++
        }
        assertEquals(3, played)
        val best = scores.values.maxOrNull() ?: 0
        assertEquals(scores.filterValues { it == best && it > 0 }.keys.sorted(), Match.leaders(scores))
    }

    private fun dealUntil(mode: GameMode, n: Int, random: Random = Random(1), ok: (Game) -> Boolean): Game {
        repeat(100) {
            val (pack, entry) = WordPicker.pick(packs, emptyList(), random)
            val game = Game.deal(names(n), GameSettings(mode, impostors = 1), pack, entry, random)
            if (ok(game)) return game
        }
        fail("no deal")
    }

    private companion object {
        const val SEEDS = 12
    }
}
