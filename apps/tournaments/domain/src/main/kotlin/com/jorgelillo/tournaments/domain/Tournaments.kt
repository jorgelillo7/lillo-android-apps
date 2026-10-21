package com.jorgelillo.tournaments.domain

import kotlin.random.Random

/** Everything the app does to a tournament. Pure: each call returns a new copy. */
object Tournaments {
    const val MIN_PLAYERS = 2
    const val MAX_PLAYERS = 64
    val BEST_OF = listOf(1, 3, 5)
    val TOP_CUTS = listOf(0, 2, 4, 8)

    fun create(
        id: String,
        name: String,
        epochDay: Long,
        names: List<String>,
        format: Format,
        bestOf: Int = 3,
        game: String = "",
        swissRounds: Int = Swiss.recommendedRounds(names.size),
        topCut: Int = 0,
        random: Random = Random.Default
    ): Tournament {
        require(names.size in MIN_PLAYERS..MAX_PLAYERS) { "players: ${names.size}" }
        require(bestOf in BEST_OF) { "bestOf: $bestOf" }
        val players = names.mapIndexed { i, n -> Player(i + 1, n.trim()) }
        val base = Tournament(
            id = id, name = name.trim(), game = game.trim(), epochDay = epochDay, bestOf = bestOf,
            format = format, players = players,
            swissRounds = if (format == Format.SWISS) swissRounds.coerceIn(1, names.size - 1 + names.size % 2) else 0,
            topCut = if (format == Format.SWISS) topCut.takeIf { it < names.size } ?: 0 else 0
        )
        return draw(base, random)
    }

    /** The random draw, again. Only before any result is in. */
    fun draw(t: Tournament, random: Random): Tournament {
        check(canRedraw(t)) { "results already in" }
        val matches = when (t.format) {
            Format.ELIMINATION -> Bracket.build(Bracket.draw(t.players, random))
            Format.SWISS -> Swiss.pairRound(t.copy(matches = emptyList()), 1, random)
        }
        return t.copy(matches = matches, revision = t.revision + 1)
    }

    fun canRedraw(t: Tournament): Boolean = t.matches.none { it.winsA + it.winsB > 0 || it.draw }

    /**
     * Records a result ("Pepe beat Luis 2-1", optional comment). A draw only exists in Swiss.
     * Later elimination rounds are refilled; if a changed result knocks a player out, the matches
     * they had already played further on are cleared.
     */
    fun record(
        t: Tournament,
        match: Match,
        winsA: Int,
        winsB: Int,
        comment: String = "",
        draw: Boolean = false
    ): Tournament {
        val needed = Match.needed(t.bestOf)
        require(winsA in 0..needed && winsB in 0..needed && !(winsA == needed && winsB == needed)) {
            "score $winsA-$winsB"
        }
        require(!draw || match.stage == Stage.SWISS) { "no draws in elimination" }
        val updated = match.copy(winsA = winsA, winsB = winsB, draw = draw && winsA == winsB, comment = comment.trim())
        val matches = t.matches.map { if (it.sameAs(match)) updated else it }
        return t.copy(matches = Bracket.advance(matches, t.bestOf), revision = t.revision + 1)
    }

    /** Wipes a result (the match is played again). */
    fun clear(t: Tournament, match: Match): Tournament = record(t, match, 0, 0)

    /** The next Swiss round can be paired once the current one is complete. */
    fun canPairNextRound(t: Tournament): Boolean {
        if (t.format != Format.SWISS) return false
        val current = currentSwissRound(t)
        return current < t.swissRounds && t.round(Stage.SWISS, current).all { it.isDone(t.bestOf) }
    }

    fun currentSwissRound(t: Tournament): Int =
        t.matches.filter { it.stage == Stage.SWISS }.maxOfOrNull { it.round } ?: 0

    fun pairNextRound(t: Tournament, random: Random = Random.Default): Tournament {
        check(canPairNextRound(t)) { "round not finished" }
        val next = Swiss.pairRound(t, currentSwissRound(t) + 1, random)
        return t.copy(matches = t.matches + next, revision = t.revision + 1)
    }

    /** Undoes the last Swiss round's pairings while none of its results is in. */
    fun canUnpairLastRound(t: Tournament): Boolean {
        val current = currentSwissRound(t)
        return current > 1 && t.eliminationRounds == 0 &&
            t.round(Stage.SWISS, current).none { !it.isBye && (it.winsA + it.winsB > 0 || it.draw) }
    }

    fun unpairLastRound(t: Tournament): Tournament {
        check(canUnpairLastRound(t))
        val current = currentSwissRound(t)
        return t.copy(
            matches = t.matches.filterNot { it.stage == Stage.SWISS && it.round == current },
            revision =
                t.revision + 1
        )
    }

    fun canStartTopCut(t: Tournament): Boolean = t.topCut > 0 && t.isSwissOver && t.eliminationRounds == 0

    fun startTopCut(t: Tournament): Tournament {
        check(canStartTopCut(t))
        return t.copy(matches = t.matches + Bracket.build(Swiss.topCut(t, t.topCut)), revision = t.revision + 1)
    }

    /** Matches that still need a result, in playing order. */
    fun pending(t: Tournament): List<Match> = t.matches
        .filter { it.a != null && it.b != null && !it.isDone(t.bestOf) }
        .sortedWith(compareBy({ it.stage != Stage.SWISS }, { it.round }, { it.slot }))

    private fun Match.sameAs(other: Match) = stage == other.stage && round == other.round && slot == other.slot
}
