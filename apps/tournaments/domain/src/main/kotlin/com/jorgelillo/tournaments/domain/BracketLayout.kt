package com.jorgelillo.tournaments.domain

/**
 * Where each elimination match goes in the two-sided bracket: the first half of round 1 on the
 * left, the second half on the right (mirrored), the final in the centre column. Slot k is fed
 * by slots 2k and 2k+1, so each half stays on its own side by construction.
 */
object BracketLayout {

    /** [column] from 0 (far left) to [Grid.columns] - 1; [y] in rows (0 = top), halves for later rounds. */
    data class Cell(val round: Int, val slot: Int, val column: Int, val y: Float, val side: Side)

    enum class Side { LEFT, RIGHT, CENTER }

    data class Grid(val columns: Int, val rows: Int, val cells: List<Cell>) {
        fun cell(round: Int, slot: Int): Cell? = cells.firstOrNull { it.round == round && it.slot == slot }
    }

    /** [rounds] elimination rounds (1 = just a final). */
    fun of(rounds: Int): Grid {
        require(rounds >= 1)
        if (rounds == 1) return Grid(1, 1, listOf(Cell(1, 0, 0, 0f, Side.CENTER)))
        val firstRoundMatches = 1 shl (rounds - 1)
        val half = firstRoundMatches / 2
        val y = HashMap<Pair<Int, Int>, Float>()
        val cells = mutableListOf<Cell>()
        for (r in 1..rounds) {
            val count = firstRoundMatches shr (r - 1)
            for (s in 0 until count) {
                val value = if (r == 1) (s % half).toFloat() else (y.getValue(r - 1 to 2 * s) + y.getValue(r - 1 to 2 * s + 1)) / 2
                y[r to s] = value
                val (column, side) = when {
                    r == rounds -> rounds - 1 to Side.CENTER
                    s < count / 2 -> r - 1 to Side.LEFT
                    else -> 2 * (rounds - 1) - (r - 1) to Side.RIGHT
                }
                cells += Cell(r, s, column, value, side)
            }
        }
        return Grid(columns = 2 * rounds - 1, rows = half, cells = cells)
    }

    /** The matches a player won on the way, for the champion's highlighted path. */
    fun pathOf(t: Tournament, player: Int): List<Match> = t.matches.filter { it.stage == Stage.ELIMINATION && it.winner(t.bestOf) == player }
}
