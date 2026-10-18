package com.jorgelillo.leaguecards.domain

import kotlinx.serialization.Serializable

/** One finished season, for the history. */
@Serializable
data class SeasonRecord(
    /** Day it ended (epoch day). */
    val day: Long,
    val matchdaysPlayed: Int,
    val position: Int,
    val points: Int,
    val ending: String,
    val achievements: List<String> = emptyList(),
)

/** Everything saved on the phone: the season in progress and the player's record and trophies. */
@Serializable
data class Saved(
    val season: Season? = null,
    /** The deck [season] was dealt from: a season of another deck can't be resumed. */
    val deck: String? = null,
    val seasonsPlayed: Int = 0,
    val titles: Int = 0,
    val bestPosition: Int? = null,
    /** Ending keys seen at least once ([Ending.key]). */
    val endingsSeen: Set<String> = emptySet(),
    val achievements: Set<String> = emptySet(),
    /** Newest first, at most [HISTORY]. */
    val history: List<SeasonRecord> = emptyList(),
) {
    /** The season to resume with [league], if any. */
    fun seasonFor(league: League): Season? = season?.takeIf { deck == league.name && !it.over }

    /** Stores [next]; the moment a season ends ([today] as epoch day), it counts in the record. */
    fun with(league: League, next: Season, today: Long): Saved {
        val ended = next.ending?.takeIf { season?.ending == null || season.seed != next.seed }
        val position = Game.position(next)
        return copy(
            season = next,
            deck = league.name,
            achievements = achievements + next.unlocked,
            seasonsPlayed = seasonsPlayed + if (ended != null) 1 else 0,
            titles = titles + if (ended?.champion == true) 1 else 0,
            bestPosition = listOfNotNull(bestPosition, ended?.position).minOrNull(),
            endingsSeen = endingsSeen + listOfNotNull(ended?.key),
            history = if (ended == null) history else (listOf(
                SeasonRecord(
                    day = today,
                    matchdaysPlayed = next.matchday - 1,
                    position = position,
                    points = next.table[YOU] ?: 0,
                    ending = ended.key,
                    achievements = next.unlocked,
                ),
            ) + history).take(HISTORY),
        )
    }

    companion object {
        const val HISTORY = 30
    }
}
