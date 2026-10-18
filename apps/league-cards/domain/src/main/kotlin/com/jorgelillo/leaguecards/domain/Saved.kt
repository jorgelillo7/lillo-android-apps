package com.jorgelillo.leaguecards.domain

import kotlinx.serialization.Serializable

/** Everything saved on the phone: the season in progress and the player's record. */
@Serializable
data class Saved(
    val season: Season? = null,
    /** The deck [season] was dealt from: a season of another deck can't be resumed. */
    val deck: String? = null,
    val seasonsPlayed: Int = 0,
    val titles: Int = 0,
    val bestPosition: Int? = null,
) {
    /** The season to resume with [league], if any. */
    fun seasonFor(league: League): Season? = season?.takeIf { deck == league.name && !it.over }

    /** Stores [next]; the moment a season ends, it counts in the record. */
    fun with(league: League, next: Season): Saved {
        val ended = next.ending?.takeIf { season?.ending == null }
        return copy(
            season = next,
            deck = league.name,
            seasonsPlayed = seasonsPlayed + if (ended != null) 1 else 0,
            titles = titles + if (ended?.champion == true) 1 else 0,
            bestPosition = listOfNotNull(bestPosition, ended?.position).minOrNull(),
        )
    }
}
