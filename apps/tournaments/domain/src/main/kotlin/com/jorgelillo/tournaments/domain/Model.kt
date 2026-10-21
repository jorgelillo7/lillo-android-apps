package com.jorgelillo.tournaments.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Short field names keep a shared tournament link small (it travels as a URL / QR code). */
@Serializable
data class Player(@SerialName("i") val id: Int, @SerialName("n") val name: String)

@Serializable
enum class Format {
    /** Knock-out bracket from the first round. */
    @SerialName("e")
    ELIMINATION,

    /** Swiss rounds (everyone plays every round), optionally followed by a knock-out top cut. */
    @SerialName("s")
    SWISS
}

@Serializable
enum class Stage {
    @SerialName("s")
    SWISS,

    @SerialName("e")
    ELIMINATION
}

/**
 * One pairing. In elimination [slot] is its position in the round (0 = top), and the winner of
 * slots 2k and 2k+1 meets in slot k of the next round. A null side is a bye (or, later in the
 * bracket, a winner still to come).
 */
@Serializable
data class Match(
    @SerialName("t") val stage: Stage,
    @SerialName("r") val round: Int,
    @SerialName("s") val slot: Int,
    @SerialName("a") val a: Int? = null,
    @SerialName("b") val b: Int? = null,
    @SerialName("x") val winsA: Int = 0,
    @SerialName("y") val winsB: Int = 0,
    /** Swiss only: the round ended level (e.g. 1-1 on time). */
    @SerialName("d") val draw: Boolean = false,
    @SerialName("c") val comment: String = ""
) {
    /**
     * One player and nobody to face. Only in Swiss and in the first elimination round: later in
     * the bracket an empty side means that opponent is still to be decided.
     */
    val isBye: Boolean get() = (a == null) != (b == null) && (stage == Stage.SWISS || round == 1)

    fun winner(bestOf: Int): Int? = when {
        isBye -> a ?: b
        a == null || b == null -> null
        winsA >= needed(bestOf) -> a
        winsB >= needed(bestOf) -> b
        else -> null
    }

    fun isDone(bestOf: Int): Boolean = draw || winner(bestOf) != null

    fun involves(player: Int): Boolean = a == player || b == player

    fun opponentOf(player: Int): Int? = when (player) {
        a -> b
        b -> a
        else -> null
    }

    companion object {
        fun needed(bestOf: Int): Int = bestOf / 2 + 1
    }
}

@Serializable
data class Tournament(
    @SerialName("id") val id: String,
    @SerialName("nm") val name: String,
    /** Free text: "Magic", "Pokémon", "Ping-pong"… Empty when not set. */
    @SerialName("g") val game: String = "",
    /** Day of the tournament, as days since 1970-01-01 (no time zone surprises). */
    @SerialName("dt") val epochDay: Long,
    @SerialName("bo") val bestOf: Int = 3,
    @SerialName("f") val format: Format = Format.ELIMINATION,
    @SerialName("p") val players: List<Player>,
    /** Swiss only: how many rounds. */
    @SerialName("sr") val swissRounds: Int = 0,
    /** Swiss only: players that go on to a knock-out after the Swiss rounds (0, 2, 4 or 8). */
    @SerialName("tc") val topCut: Int = 0,
    @SerialName("m") val matches: List<Match> = emptyList(),
    /** Bumped on every change: when a tournament is imported again, the newest copy wins. */
    @SerialName("v") val revision: Int = 0
) {
    fun player(id: Int?): Player? = players.firstOrNull { it.id == id }

    fun round(stage: Stage, round: Int): List<Match> = matches.filter {
        it.stage == stage && it.round == round
    }.sortedBy { it.slot }

    val eliminationRounds: Int get() = matches.filter { it.stage == Stage.ELIMINATION }.maxOfOrNull { it.round } ?: 0

    /** The champion, once the final is decided. */
    val champion: Int?
        get() {
            val last = eliminationRounds
            if (last ==
                0
            ) {
                return if (format == Format.SWISS && topCut == 0 &&
                    isSwissOver
                ) {
                    Standings.of(this).firstOrNull()?.player
                } else {
                    null
                }
            }
            return round(Stage.ELIMINATION, last).singleOrNull()?.winner(bestOf)
        }

    val isSwissOver: Boolean
        get() = format == Format.SWISS &&
            (1..swissRounds).all { r ->
                round(Stage.SWISS, r).let { it.isNotEmpty() && it.all { m -> m.isDone(bestOf) } }
            }

    val isOver: Boolean get() = champion != null
}
