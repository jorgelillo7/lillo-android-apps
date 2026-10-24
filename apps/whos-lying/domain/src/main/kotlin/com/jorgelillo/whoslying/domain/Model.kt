package com.jorgelillo.whoslying.domain

import kotlinx.serialization.Serializable

enum class Role { CIVILIAN, IMPOSTOR, DRIFTER }

enum class GameMode {
    /** The impostor gets a similar but different word and doesn't know they are the impostor. */
    CLASSIC,

    /** The impostor gets no word, not even the category, and knows it. */
    BLIND,

    /** Like CLASSIC, plus one "drifter" with no word who wins by guessing the civilians' word. */
    DRIFTER
}

/**
 * One secret: the civilians' word and, for CLASSIC/DRIFTER, similar words for the impostor (one is
 * picked per game, so a repeated word still plays differently).
 */
@Serializable
data class Entry(val word: String, val decoys: List<String> = emptyList())

@Serializable
data class WordPack(
    val id: String,
    val name: String,
    val emoji: String,
    val entries: List<Entry>,
    val custom: Boolean = false
)

@Serializable
data class GameSettings(
    val mode: GameMode = GameMode.CLASSIC,
    val impostors: Int = 1,
    /** Show the pack name to drawing impostors and the drifter (never in BLIND, see [Rules.hintAvailable]). */
    val categoryHint: Boolean = true,
    /** Some games get a random number of impostors, sometimes everyone. */
    val chaos: Boolean = false,
    /** Empty means "all packs". */
    val packIds: Set<String> = emptySet(),
    /** Discussion countdown before each vote; 0 means no timer. */
    val discussionSeconds: Int = 0,
    /**
     * Clues are drawn together on one canvas instead of said out loud. Works with every mode;
     * impostors get no word (like BLIND), only the category hint if enabled and not BLIND.
     */
    val drawing: Boolean = false,
    /** Games in a match; scores decide a winner after the last one. 0 means no limit. */
    val rounds: Int = Match.DEFAULT_ROUNDS
)

/** What one player sees when it's their turn to look at the phone. */
data class Card(
    val player: String,
    val role: Role,
    /** Word shown to the player; null when they get none (BLIND impostor, drifter). */
    val word: String?,
    /** Category hint for players without the civilians' word, when enabled. */
    val hint: String?
) {
    /** CLASSIC/DRIFTER impostors see a word and are not told they are impostors. */
    val knowsRole: Boolean get() = role != Role.CIVILIAN && word == null
}
