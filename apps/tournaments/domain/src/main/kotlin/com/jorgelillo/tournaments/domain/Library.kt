package com.jorgelillo.tournaments.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Everything the app keeps on the phone. */
@Serializable
data class Library(@SerialName("tournaments") val tournaments: List<Tournament> = emptyList()) {
    fun tournament(id: String): Tournament? = tournaments.firstOrNull { it.id == id }

    fun save(t: Tournament): Library = copy(
        tournaments = if (tournament(t.id) ==
            null
        ) {
            tournaments + t
        } else {
            tournaments.map { if (it.id == t.id) t else it }
        }
    )

    fun delete(id: String): Library = copy(tournaments = tournaments.filterNot { it.id == id })

    enum class ImportResult { ADDED, UPDATED, ALREADY_UP_TO_DATE, NEWER_HERE }

    /** A tournament from a link: new ones are added, a newer copy of one we have replaces it. */
    fun import(t: Tournament): Pair<Library, ImportResult> {
        val existing = tournament(t.id) ?: return save(t) to ImportResult.ADDED
        return when {
            t.revision > existing.revision -> save(t) to ImportResult.UPDATED
            t == existing -> this to ImportResult.ALREADY_UP_TO_DATE
            else -> this to ImportResult.NEWER_HERE
        }
    }

    /** In progress first, then the most recent. */
    val sorted: List<Tournament>
        get() = tournaments.sortedWith(
            compareBy<Tournament> {
                it.isOver
            }.thenByDescending { it.epochDay }.thenByDescending { it.revision }
        )

    /** Names used before, most frequent first: offered as chips when creating a tournament. */
    val knownNames: List<String>
        get() = tournaments.flatMap { t -> t.players.map { it.name } }
            .groupBy { Stats.key(it) }.values
            .sortedByDescending { it.size }
            .map { it.last() }

    val knownGames: List<String>
        get() = tournaments.map { it.game }.filter { it.isNotBlank() }.groupBy { it.lowercase() }.values
            .sortedByDescending { it.size }.map { it.last() }
}
