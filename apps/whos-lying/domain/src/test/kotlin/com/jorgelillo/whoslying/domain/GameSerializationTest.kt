package com.jorgelillo.whoslying.domain

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.serialization.json.Json

/** A game written down and read back plays on exactly as the original would. */
class GameSerializationTest {
    private val players = listOf("Ana", "Bea", "Carlos", "Dani", "Eva")
    private val pack = WordPack("p", "Lugares", "🗺️", listOf(Entry("Café", listOf("Té"))))

    private fun roundTrip(game: Game): Game =
        Json.decodeFromString(Game.serializer(), Json.encodeToString(Game.serializer(), game))

    private fun assertSameGame(expected: Game, actual: Game) {
        assertEquals(expected.cards, actual.cards)
        assertEquals(expected.civilianWord, actual.civilianWord)
        assertEquals(expected.category, actual.category)
        assertEquals(expected.starter, actual.starter)
        assertEquals(expected.alive, actual.alive)
        assertEquals(expected.outcome, actual.outcome)
        assertEquals(expected.pendingGuess, actual.pendingGuess)
        assertEquals(expected.lastElimination, actual.lastElimination)
    }

    @Test
    fun aDrifterWaitingToGuessComesBackWaiting() {
        val dealt = Game.deal(players, GameSettings(GameMode.DRIFTER), pack, pack.entries.first(), Random(3))
        val waiting = dealt.eliminate(dealt.cards.single { it.role == Role.DRIFTER }.player)

        val restored = roundTrip(waiting)

        assertSameGame(waiting, restored)
        assertEquals(Outcome.DrifterGuessed, restored.guess("cafe").outcome)
    }

    @Test
    fun aGameMidVoteComesBackWithTheSameSurvivors() {
        val dealt = Game.deal(players, GameSettings(GameMode.CLASSIC), pack, pack.entries.first(), Random(1))
        val civilian = dealt.cards.first { it.role == Role.CIVILIAN }.player
        val midGame = dealt.eliminate(civilian)
        assertNull(midGame.outcome)

        val restored = roundTrip(midGame)

        assertSameGame(midGame, restored)
        val impostor = restored.cards.single { it.role == Role.IMPOSTOR }.player
        assertEquals(Outcome.CiviliansWin, restored.eliminate(impostor).outcome)
    }

    @Test
    fun everyOutcomeReadsBack() {
        val outcomes = listOf(
            Outcome.CiviliansWin,
            Outcome.InfiltratorsWin(setOf(Role.IMPOSTOR, Role.DRIFTER)),
            Outcome.DrifterGuessed,
            Outcome.NoCivilians,
            Outcome.EndedEarly
        )
        outcomes.forEach { outcome ->
            assertEquals(
                outcome,
                Json.decodeFromString(Outcome.serializer(), Json.encodeToString(Outcome.serializer(), outcome))
            )
        }
    }
}
