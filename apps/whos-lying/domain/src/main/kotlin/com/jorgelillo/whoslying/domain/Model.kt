package com.jorgelillo.whoslying.domain

import kotlinx.serialization.Serializable

enum class Role { CIVILIAN, IMPOSTOR, JOURNALIST }

enum class GameMode {
    /** Everyone is told their role. Impostors get no word, or the similar word as a hint when [GameSettings.impostorHint] is on. */
    ORIGINAL,

    /** Nobody is told their role: impostors get the similar word. Journalists get no word and guess it when voted out. */
    MYSTERIOUS,

    /**
     * Original with hints and a random number of impostors within [Rules.maxInfiltrators]; one game in five
     * goes past it and one in five is all in (everyone an impostor), some without a hint. Never scored.
     */
    CHAOS
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

@Serializable(with = GameSettingsSerializer::class)
data class GameSettings(
    val mode: GameMode = GameMode.ORIGINAL,
    /** Impostors set for the game; at least one but in [GameMode.MYSTERIOUS] with a journalist. Ignored in [GameMode.CHAOS]. */
    val impostors: Int = 1,
    /** [GameMode.ORIGINAL]: impostors see the similar word as a hint. */
    val impostorHint: Boolean = true,
    /** [GameMode.MYSTERIOUS] only: players without a word who win by guessing it when voted out. */
    val journalists: Int = 0,
    /** Empty means "all packs". */
    val packIds: Set<String> = emptySet(),
    /** Discussion countdown before each vote; 0 means no timer. */
    val discussionSeconds: Int = 0,
    /** Clues are drawn together on one canvas instead of said out loud. Works with every mode. */
    val drawing: Boolean = false,
    /** Games in a match; scores decide a winner after the last one. 0 means no limit. */
    val rounds: Int = Match.DEFAULT_ROUNDS
)

/** What one player sees when it's their turn to look at the phone. */
@Serializable
data class Card(
    val player: String,
    val role: Role,
    /** Word shown to the player; null when they get none. */
    val word: String?,
    /** The similar word shown to an impostor as a hint, when they get one. */
    val hint: String?,
    /** The card tells the player their role; false for everyone in [GameMode.MYSTERIOUS] but journalists. */
    val knowsRole: Boolean
)
