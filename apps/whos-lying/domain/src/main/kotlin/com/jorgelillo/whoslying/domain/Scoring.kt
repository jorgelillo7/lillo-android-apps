package com.jorgelillo.whoslying.domain

/**
 * Points for a finished game, added up across games: every player on the winning side scores,
 * eliminated or not. Impostors and the drifter score more because they are outnumbered.
 */
object Scoring {
    const val CIVILIAN_WIN = 5
    const val INFILTRATOR_WIN = 15

    fun points(game: Game): Map<String, Int> = when (val outcome = game.outcome) {
        Outcome.CiviliansWin -> game.cards.filter { it.role == Role.CIVILIAN }.associate { it.player to CIVILIAN_WIN }

        is Outcome.InfiltratorsWin -> game.cards.filter { it.role in outcome.roles }.associate {
            it.player to
                INFILTRATOR_WIN
        }

        Outcome.DrifterGuessed -> game.cards.filter {
            it.role == Role.DRIFTER
        }.associate { it.player to INFILTRATOR_WIN }

        Outcome.NoCivilians, Outcome.EndedEarly, null -> emptyMap()
    }
}

/** A match is a set number of games; whoever has the most points at the end wins it. */
object Match {
    const val DEFAULT_ROUNDS = 10
    val ROUND_OPTIONS = listOf(3, 5, 7, 10, 0)

    /** Bigger groups take longer per game, so they get fewer rounds. */
    fun recommendedRounds(players: Int): Int = when {
        players <= 5 -> 10
        players <= 8 -> 7
        players <= 12 -> 5
        else -> 3
    }

    /** Games ended early don't use up a round. */
    fun counts(outcome: Outcome?): Boolean = outcome != null && outcome != Outcome.EndedEarly

    fun isOver(played: Int, rounds: Int): Boolean = rounds > 0 && played >= rounds

    fun remaining(played: Int, rounds: Int): Int = (rounds - played).coerceAtLeast(0)

    /** Players tied at the top, or nobody if no one scored. */
    fun leaders(scores: Map<String, Int>): List<String> {
        val best = scores.values.maxOrNull()?.takeIf { it > 0 } ?: return emptyList()
        return scores.filterValues { it == best }.keys.sorted()
    }
}
