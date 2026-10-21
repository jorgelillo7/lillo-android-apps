package com.jorgelillo.tournaments.domain

import kotlin.math.ceil
import kotlin.math.ln
import kotlin.random.Random

/**
 * One row of the Swiss table, ranked like Magic / Pokémon / Lorcana organised play: points (win 3,
 * draw 1, loss 0; a bye counts as a win), then OMW, GW and OGW, each floored at 33 %.
 */
data class Standing(
    val player: Int,
    val points: Int,
    val wins: Int,
    val draws: Int,
    val losses: Int,
    /** Opponents' match-win percentage (each floored at 33 %), the usual first tie-breaker. */
    val opponentsWinRate: Double,
    /** Games won / games played (floored at 33 %), the second tie-breaker. */
    val gameWinRate: Double,
    /** Opponents' game-win percentage, the third. */
    val opponentsGameWinRate: Double
)

object Standings {
    const val WIN = 3
    const val DRAW = 1
    private const val FLOOR = 1.0 / 3

    /** Ranking after the Swiss rounds played so far (or of all matches in an elimination). */
    fun of(t: Tournament): List<Standing> {
        val stage = if (t.format == Format.SWISS) Stage.SWISS else Stage.ELIMINATION
        val played = t.matches.filter { it.stage == stage && it.isDone(t.bestOf) }
        fun record(p: Int): Triple<Int, Int, Int> {
            var w = 0
            var d = 0
            var l = 0
            played.filter { it.involves(p) }.forEach { m ->
                when {
                    m.draw -> d++
                    m.winner(t.bestOf) == p -> w++
                    else -> l++
                }
            }
            return Triple(w, d, l)
        }
        fun matchWinRate(p: Int): Double {
            val (w, d, l) = record(p)
            val n = w + d + l
            return if (n == 0) FLOOR else ((w * WIN + d * DRAW).toDouble() / (n * WIN)).coerceAtLeast(FLOOR)
        }
        fun gameWinRate(p: Int): Double {
            val games = played.filter { it.involves(p) && !it.isBye }
            val won = games.sumOf { if (it.a == p) it.winsA else it.winsB }
            val total = games.sumOf { it.winsA + it.winsB }
            return if (total == 0) FLOOR else (won.toDouble() / total).coerceAtLeast(FLOOR)
        }
        return t.players.map { player ->
            val p = player.id
            val (w, d, l) = record(p)
            val opponents = played.filter { it.involves(p) && !it.isBye }.mapNotNull { it.opponentOf(p) }
            Standing(
                player = p,
                points = w * WIN + d * DRAW,
                wins = w,
                draws = d,
                losses = l,
                opponentsWinRate = if (opponents.isEmpty()) 0.0 else opponents.map(::matchWinRate).average(),
                gameWinRate = gameWinRate(p),
                opponentsGameWinRate = if (opponents.isEmpty()) 0.0 else opponents.map(::gameWinRate).average()
            )
        }.sortedWith(
            compareByDescending<Standing> { it.points }
                .thenByDescending { it.opponentsWinRate }
                .thenByDescending { it.gameWinRate }
                .thenByDescending { it.opponentsGameWinRate }
                .thenBy { t.players.indexOfFirst { p -> p.id == it.player } }
        )
    }
}

object Swiss {

    /** The usual number of rounds: enough for one undefeated player (log2 of the field). */
    fun recommendedRounds(players: Int): Int = ceil(ln(players.coerceAtLeast(2).toDouble()) / ln(2.0)).toInt()

    /**
     * Pairings for the next round: players by points, each paired with the closest one they
     * haven't played yet; with an odd number, the lowest-ranked player without a bye gets one.
     * Round 1 is a random draw.
     */
    fun pairRound(t: Tournament, round: Int, random: Random): List<Match> {
        val previous = t.matches.filter { it.stage == Stage.SWISS }
        val ranking = if (round == 1) t.players.map { it.id }.shuffled(random) else Standings.of(t).map { it.player }
        val pool = ranking.toMutableList()
        val pairs = mutableListOf<Pair<Int, Int?>>()
        if (pool.size % 2 == 1) {
            val hadBye = previous.filter { it.isBye }.mapNotNull { it.a ?: it.b }.toSet()
            val byePlayer = pool.lastOrNull { it !in hadBye } ?: pool.last()
            pool.remove(byePlayer)
            pairs += byePlayer to null
        }
        fun played(x: Int, y: Int) = previous.any { it.involves(x) && it.involves(y) }
        val paired = pairWithoutRematches(pool, ::played) ?: pool.chunked(2).map { it[0] to it[1] }
        val ordered = paired.map { (x, y) -> x to y as Int? } + pairs
        return ordered.mapIndexed { slot, (x, y) -> Match(Stage.SWISS, round, slot, x, y) }
    }

    /** Backtracking: top-down, each player with the nearest-ranked opponent not met before. */
    private fun pairWithoutRematches(pool: List<Int>, played: (Int, Int) -> Boolean): List<Pair<Int, Int>>? {
        if (pool.isEmpty()) return emptyList()
        val first = pool.first()
        for (i in 1 until pool.size) {
            val other = pool[i]
            if (played(first, other)) continue
            val rest = pool.filterIndexed { index, _ -> index != 0 && index != i }
            pairWithoutRematches(rest, played)?.let { return listOf(first to other) + it }
        }
        return null
    }

    /** Top-cut seeds: the best [cut] players of the Swiss table, in order. */
    fun topCut(t: Tournament, cut: Int): List<Int> = Standings.of(t).take(cut).map { it.player }
}
