package com.jorgelillo.tournaments.domain

/** One player across every tournament, matched by name (player ids are per tournament). */
data class PlayerStats(
    val name: String,
    val tournaments: Int,
    val titles: Int,
    val finals: Int,
    val wins: Int,
    val draws: Int,
    val losses: Int,
    val gamesWon: Int,
    val gamesLost: Int
) {
    val matches: Int get() = wins + draws + losses
    val winRate: Double get() = if (matches == 0) 0.0 else wins.toDouble() / matches
}

object Stats {
    /** "  pepe " and "Pepe" are the same person. */
    fun key(name: String): String = name.trim().lowercase()

    /** Best first: titles, then finals, then match win rate. [game] null = every game. */
    fun of(tournaments: List<Tournament>, game: String? = null): List<PlayerStats> {
        val chosen = tournaments.filter { game == null || it.game.equals(game, ignoreCase = true) }
        data class Acc(var name: String, var tournaments: Int = 0, var titles: Int = 0, var finals: Int = 0, var w: Int = 0, var d: Int = 0, var l: Int = 0, var gw: Int = 0, var gl: Int = 0)
        val byName = LinkedHashMap<String, Acc>()
        for (t in chosen) {
            val champion = t.champion
            val final = t.eliminationRounds.takeIf { it > 0 }?.let { t.round(Stage.ELIMINATION, it).singleOrNull() }
            for (p in t.players) {
                val acc = byName.getOrPut(key(p.name)) { Acc(p.name.trim()) }
                acc.name = p.name.trim()
                acc.tournaments++
                if (champion == p.id) acc.titles++
                if (final != null && final.involves(p.id) && final.a != null && final.b != null) acc.finals++
                t.matches.filter { it.involves(p.id) && !it.isBye && it.isDone(t.bestOf) }.forEach { m ->
                    when {
                        m.draw -> acc.d++
                        m.winner(t.bestOf) == p.id -> acc.w++
                        else -> acc.l++
                    }
                    acc.gw += if (m.a == p.id) m.winsA else m.winsB
                    acc.gl += if (m.a == p.id) m.winsB else m.winsA
                }
            }
        }
        return byName.values
            .map { PlayerStats(it.name, it.tournaments, it.titles, it.finals, it.w, it.d, it.l, it.gw, it.gl) }
            .sortedWith(compareByDescending<PlayerStats> { it.titles }.thenByDescending { it.finals }.thenByDescending { it.winRate }.thenByDescending { it.matches })
    }

    /** Every match as CSV (one row per match), for a spreadsheet. */
    fun csv(t: Tournament, roundName: (Match) -> String): String = buildString {
        appendLine("tournament,game,date_epoch_day,stage,round,player_a,player_b,games_a,games_b,winner,comment")
        t.matches.filter { it.a != null || it.b != null }.forEach { m ->
            val winner = m.winner(t.bestOf)?.let { t.player(it)?.name } ?: if (m.draw) "draw" else ""
            listOf(
                t.name, t.game, t.epochDay.toString(), roundName(m), m.round.toString(),
                t.player(m.a)?.name.orEmpty(), t.player(m.b)?.name.orEmpty(),
                m.winsA.toString(), m.winsB.toString(), winner, m.comment
            ).joinTo(this, ",") { field -> if (field.any { it in ",\"\n" }) "\"" + field.replace("\"", "\"\"") + "\"" else field }
            appendLine()
        }
    }
}
