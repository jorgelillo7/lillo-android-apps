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
 * Every setting combination (mode × drawing × impostor hint × players × impostors × journalists),
 * dealt from the real built-in packs and played to the end with random votes, guesses and early
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
        for (mode in GameMode.entries) {
            for (drawing in listOf(false, true)) {
                for (hint in listOf(false, true)) {
                    for (n in playerCounts) {
                        val cap = Rules.maxInfiltrators(n)
                        val overCap = Rules.canStart(n, GameSettings(mode, impostors = cap + 1))
                        assertEquals(mode == GameMode.CHAOS, overCap, "$mode $n players: chaos ignores the count")
                        assertFalse(Rules.canStart(2, GameSettings(mode)), "$mode with 2 players")
                        val journalistCounts = if (mode == GameMode.MYSTERIOUS) 0..cap else 0..0
                        for (journalists in journalistCounts) {
                            for (impostors in 0..cap - journalists) {
                                val settings = GameSettings(mode, impostors, hint, journalists, drawing = drawing)
                                val valid = mode == GameMode.CHAOS ||
                                    (impostors + journalists >= 1 && (impostors >= 1 || mode == GameMode.MYSTERIOUS))
                                assertEquals(valid, Rules.canStart(n, settings), "$settings n=$n")
                                if (!valid) continue
                                repeat(SEEDS) { seed ->
                                    val random = Random(seed * 7919 + n * 31 + impostors * 7 + journalists)
                                    val (pack, entry) = WordPicker.pick(packs, emptyList(), random)
                                    val game = Game.deal(names(n), settings, pack, entry, random)
                                    checkDeal(game, settings, n, pack, entry)
                                    playOut(game, random)
                                    games++
                                }
                            }
                        }
                    }
                }
            }
        }
        println("exhaustive: $games games, outcomes $outcomesSeen")
        // Every kind of ending showed up somewhere in the matrix.
        for (kind in listOf(
            "CiviliansWin",
            "Impostors",
            "Journalists",
            "ImpostorsAndJournalists",
            "JournalistGuessed",
            "NoCivilians",
            "EndedEarly"
        )) {
            assertTrue((outcomesSeen[kind] ?: 0) > 0, "never saw $kind")
        }
    }

    private fun checkDeal(game: Game, settings: GameSettings, n: Int, pack: WordPack, entry: Entry) {
        val ctx = "$settings n=$n word=${entry.word}"
        assertEquals(names(n), game.cards.map { it.player }, ctx)
        val impostors = game.cards.count { it.role == Role.IMPOSTOR }
        val journalists = game.cards.count { it.role == Role.JOURNALIST }
        if (settings.mode == GameMode.CHAOS) {
            assertTrue(impostors in Rules.recommendedImpostors(n)..n, "$ctx: chaos gave $impostors")
            assertFalse(game.scored, ctx)
        } else {
            assertEquals(settings.impostors, impostors, ctx)
            assertTrue(game.scored, ctx)
        }
        assertEquals(if (settings.mode == GameMode.MYSTERIOUS) settings.journalists else 0, journalists, ctx)

        for (card in game.cards) {
            assertEquals(settings.mode != GameMode.MYSTERIOUS || card.word == null, card.knowsRole, "$ctx: $card")
            when (card.role) {
                Role.CIVILIAN -> assertEquals(entry.word, card.word, ctx)

                Role.JOURNALIST -> assertTrue(card.word == null && card.hint == null, ctx)

                Role.IMPOSTOR -> {
                    val similar = if (settings.mode == GameMode.MYSTERIOUS) card.word else card.hint
                    if (settings.mode == GameMode.MYSTERIOUS) {
                        assertNull(card.hint, ctx)
                        if (settings.drawing) assertNull(card.word, "$ctx: drawing impostors draw blind")
                    } else {
                        assertNull(card.word, ctx)
                        if (settings.mode == GameMode.ORIGINAL) {
                            assertEquals(settings.impostorHint, card.hint != null, ctx)
                        }
                    }
                    if (similar != null) {
                        assertNotEquals(entry.word, similar, "$ctx: the similar word must differ from the word")
                        assertTrue(similar in entry.decoys || pack.entries.any { it.word == similar }, ctx)
                    }
                }
            }
        }
        assertNotEquals(Role.JOURNALIST, game.cardOf(game.starter).role, "$ctx: a journalist never starts")
        assertEquals(entry.word, game.civilianWord)
        assertEquals(pack.name, game.category)
    }

    /** Votes at random until the game ends; sometimes the group ends it early instead. */
    private fun playOut(dealt: Game, random: Random) {
        var game = dealt
        val endEarlyAfter = if (random.nextInt(6) == 0) random.nextInt(game.cards.size) else Int.MAX_VALUE
        var votes = 0
        while (!game.isOver) {
            if (votes >= endEarlyAfter) {
                game = game.reveal()
                break
            }
            if (votes > game.cards.size ||
                game.alive.isEmpty()
            ) {
                fail("game never ended: ${game.cards.map { it.role }}, alive ${game.alive.size}")
            }
            val out = game.alive.random(random).player
            val before = game
            game = game.eliminate(out)
            assertEquals(out, game.lastElimination?.player)
            assertEquals(before.alive.size - 1, game.alive.size, "the vote doesn't change the previous state")
            votes++
            if (game.lastElimination!!.journalistMustGuess) {
                assertEquals(out, game.pendingGuess)
                val right = random.nextBoolean()
                game = game.guess(if (right) " " + game.civilianWord.uppercase() + " " else "nope")
                assertEquals(right, game.journalistGuessed)
                if (right) {
                    assertEquals(Outcome.JournalistGuessed, game.outcome)
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
                assertTrue(
                    (infiltrators.isNotEmpty() && civilians > 1) || (noCivilians && game.alive.size > 1),
                    "unfinished game should be over: ${game.alive}"
                )
            }

            Outcome.CiviliansWin -> assertTrue(infiltrators.isEmpty())

            is Outcome.InfiltratorsWin -> {
                assertTrue(civilians <= 1, "infiltrators won with $civilians civilians left")
                assertEquals(infiltrators, outcome.roles)
            }

            Outcome.JournalistGuessed -> assertEquals(Role.JOURNALIST, game.lastElimination?.role)

            Outcome.NoCivilians -> assertTrue(noCivilians && (game.alive.size <= 1 || game.pendingGuess == null))

            Outcome.EndedEarly -> assertTrue(infiltrators.isNotEmpty() && civilians > 1 && !noCivilians)
        }
        game.outcome?.let { outcomesSeen.merge(kind(it), 1, Int::plus) }
    }

    private fun kind(outcome: Outcome): String = when (outcome) {
        is Outcome.InfiltratorsWin -> when (outcome.roles) {
            setOf(Role.IMPOSTOR) -> "Impostors"
            setOf(Role.JOURNALIST) -> "Journalists"
            else -> "ImpostorsAndJournalists"
        }

        else -> outcome::class.simpleName!!
    }

    private fun checkPoints(game: Game) {
        val points = Scoring.points(game)
        val expected = if (!game.scored) {
            emptyMap()
        } else {
            when (val outcome = game.outcome) {
                Outcome.CiviliansWin -> game.cards.filter { it.role == Role.CIVILIAN }.associate {
                    it.player to
                        Scoring.CIVILIAN_WIN
                }

                is Outcome.InfiltratorsWin -> game.cards.filter { it.role in outcome.roles }.associate {
                    it.player to
                        Scoring.INFILTRATOR_WIN
                }

                Outcome.JournalistGuessed -> mapOf(game.lastElimination!!.player to Scoring.INFILTRATOR_WIN)

                else -> emptyMap()
            }
        }
        assertEquals(expected, points, "points for ${game.outcome}")
    }

    @Test
    fun impostorAndJournalistWinTogetherWhenOneCivilianIsLeft() {
        // 5 players in Mysterious with a journalist: 1 impostor, 1 journalist, 3 civilians. Vote two civilians out.
        val dealt = dealUntil(withJournalist, 5) { true }
        val game = dealt.cards.filter {
            it.role == Role.CIVILIAN
        }.take(2).fold(dealt) { g, card -> g.eliminate(card.player) }
        assertEquals(Outcome.InfiltratorsWin(setOf(Role.IMPOSTOR, Role.JOURNALIST)), game.outcome)
        assertEquals(setOf(Scoring.INFILTRATOR_WIN), Scoring.points(game).values.toSet())
        assertEquals(2, Scoring.points(game).size)
    }

    @Test
    fun theJournalistWinsAloneWhenTheImpostorWasCaught() {
        val dealt = dealUntil(withJournalist, 5) { true }
        val impostorOut = dealt.eliminate(dealt.cards.single { it.role == Role.IMPOSTOR }.player)
        assertNull(impostorOut.outcome, "the journalist is still in")
        val game = dealt.cards.filter {
            it.role == Role.CIVILIAN
        }.take(2).fold(impostorOut) { g, card -> g.eliminate(card.player) }
        assertEquals(Outcome.InfiltratorsWin(setOf(Role.JOURNALIST)), game.outcome)
        assertEquals(
            mapOf(
                game.cards.single {
                    it.role == Role.JOURNALIST
                }.player to Scoring.INFILTRATOR_WIN
            ),
            Scoring.points(game)
        )
    }

    @Test
    fun aWrongGuessByTheLastInfiltratorHandsTheWinToTheCivilians() {
        val dealt = dealUntil(withJournalist, 5) { true }
        val game = dealt.eliminate(dealt.cards.single { it.role == Role.IMPOSTOR }.player)
            .eliminate(dealt.cards.single { it.role == Role.JOURNALIST }.player)
            .guess("nope")
        assertFalse(game.journalistGuessed)
        assertEquals(Outcome.CiviliansWin, game.outcome)
    }

    @Test
    fun anAllImpostorGameEndsWhenTheVotesLeaveOnePlayer() {
        val random = Random(3)
        val (pack, entry) = WordPicker.pick(packs, emptyList(), random)
        // Chaos picks "everyone" now and then: find such a deal.
        var game = generateSequence {
            Game.deal(names(4), GameSettings(GameMode.CHAOS), pack, entry, random)
        }
            .first { g -> g.cards.all { it.role == Role.IMPOSTOR } }
        repeat(2) {
            game = game.eliminate(game.alive.first().player)
            assertNull(game.outcome)
        }
        game = game.eliminate(game.alive.first().player)
        assertEquals(Outcome.NoCivilians, game.outcome)
        assertEquals(1, game.alive.size)
    }

    @Test
    fun votingStopsAsSoonAsSomeoneWins() {
        val dealt = dealUntil(withJournalist, 4) { true }
        val game = dealt.eliminate(dealt.cards.first { it.role == Role.CIVILIAN }.player)
        assertEquals(Outcome.InfiltratorsWin(setOf(Role.IMPOSTOR, Role.JOURNALIST)), game.outcome)
        assertTrue(runCatching { game.eliminate(game.alive.first().player) }.isFailure, "no votes after the end")
    }

    @Test
    fun aMatchCountsOnlyFinishedGamesAndCrownsTheLeaders() {
        val scores = mutableMapOf<String, Int>()
        var played = 0
        val random = Random(42)
        // A game ended early first: no points, no round used.
        val early = dealUntil(withJournalist, 6, random) { true }.reveal()
        assertEquals(Outcome.EndedEarly, early.outcome)
        assertFalse(Match.counts(early.outcome))
        assertEquals(emptyMap(), Scoring.points(early))
        while (!Match.isOver(played, 3)) {
            var game = dealUntil(withJournalist, 6, random) { true }
            while (!game.isOver) {
                game = game.eliminate(game.alive.random(random).player)
                if (game.pendingGuess != null) game = game.guess("nope")
            }
            Scoring.points(game).forEach { (p, pts) -> scores.merge(p, pts, Int::plus) }
            if (Match.counts(game.outcome)) played++
        }
        assertEquals(3, played)
        val best = scores.values.maxOrNull() ?: 0
        assertEquals(scores.filterValues { it == best && it > 0 }.keys.sorted(), Match.leaders(scores))
    }

    private val withJournalist = GameSettings(GameMode.MYSTERIOUS, impostors = 1, journalists = 1)

    private fun dealUntil(settings: GameSettings, n: Int, random: Random = Random(1), ok: (Game) -> Boolean): Game {
        repeat(100) {
            val (pack, entry) = WordPicker.pick(packs, emptyList(), random)
            val game = Game.deal(names(n), settings, pack, entry, random)
            if (ok(game)) return game
        }
        fail("no deal")
    }

    private companion object {
        const val SEEDS = 12
    }
}
