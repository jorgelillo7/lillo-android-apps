package com.jorgelillo.whoslying.data

import android.content.Context
import com.jorgelillo.core.platform.JsonStore
import com.jorgelillo.whoslying.domain.GameSettings
import com.jorgelillo.whoslying.domain.WordPack
import kotlinx.serialization.Serializable


/** What survives between games: the group, the last setup, your packs, recently used words and scores. */
@Serializable
data class SavedState(
    val players: List<String> = emptyList(),
    val settings: GameSettings = GameSettings(),
    val customPacks: List<WordPack> = emptyList(),
    val recentWords: List<String> = emptyList(),
    /** Running points per player across games, until the group resets them. */
    val scores: Map<String, Int> = emptyMap(),
    /** Games finished in the current match. */
    val roundsPlayed: Int = 0,
    /** The welcome note was dismissed with "don't show again". */
    val welcomeSeen: Boolean = false,
)

/** The whole saved state as one JSON document. */
typealias StateRepository = JsonStore<SavedState>

/** The app's single store: DataStore file "whos_lying", as it has always been (users keep their data). */
fun stateRepository(context: Context): StateRepository =
    JsonStore(context, "whos_lying", SavedState.serializer()) { SavedState() }
