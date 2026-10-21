package com.jorgelillo.decisionwheel.domain

import kotlinx.serialization.Serializable

@Serializable
data class Wheel(
    val id: String,
    val name: String,
    val options: List<String>,
    /** Starred: listed first and offered as a launcher shortcut. */
    val favorite: Boolean = false
)

/** A result the user accepted ("Let's go!"). Only accepted results count as history. */
@Serializable
data class Decision(val wheelId: String, val option: String, val atEpochMillis: Long)

@Serializable
data class AppState(
    val wheels: List<Wheel> = emptyList(),
    val history: List<Decision> = emptyList(),
    val avoidRepeats: Boolean = true,
    val soundOn: Boolean = true
) {
    fun wheel(id: String): Wheel? = wheels.firstOrNull { it.id == id }

    fun historyOf(wheelId: String): List<Decision> = history.filter {
        it.wheelId == wheelId
    }.sortedByDescending { it.atEpochMillis }

    fun withDecision(decision: Decision): AppState = copy(history = (listOf(decision) + history).take(MAX_HISTORY))

    /** Home order: favourites first, each group in the user's order. */
    val sortedWheels: List<Wheel> get() = wheels.filter { it.favorite } + wheels.filterNot { it.favorite }

    fun toggleFavorite(wheelId: String): AppState = copy(
        wheels = wheels.map {
            if (it.id ==
                wheelId
            ) {
                it.copy(favorite = !it.favorite)
            } else {
                it
            }
        }
    )

    /**
     * Launcher shortcuts: favourites first, then the most recently used wheels, up to [max]
     * (Android shows about four).
     */
    fun shortcutWheels(max: Int = MAX_SHORTCUTS): List<Wheel> {
        val recent = history.sortedByDescending { it.atEpochMillis }.map { it.wheelId }.distinct().mapNotNull(::wheel)
        return (wheels.filter { it.favorite } + recent).distinctBy { it.id }.take(max)
    }

    /** Brings back the example wheels that were deleted; edited ones and the user's own are untouched. */
    fun restorePresets(presets: List<Wheel>): AppState {
        val missing = presets.filter { preset -> wheels.none { it.id == preset.id } }
        return copy(wheels = wheels + missing)
    }

    companion object {
        const val MAX_HISTORY = 300
        const val MAX_SHORTCUTS = 3
    }
}
