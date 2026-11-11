package com.jorgelillo.whoslying.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.jorgelillo.whoslying.data.SavedState
import com.jorgelillo.whoslying.data.StateRepository
import com.jorgelillo.whoslying.domain.Elimination
import com.jorgelillo.whoslying.domain.Game
import com.jorgelillo.whoslying.domain.GameSettings
import com.jorgelillo.whoslying.domain.Match
import com.jorgelillo.whoslying.domain.PackShare
import com.jorgelillo.whoslying.domain.Rules
import com.jorgelillo.whoslying.domain.Scoring
import com.jorgelillo.whoslying.domain.WordPack
import com.jorgelillo.whoslying.domain.WordPacks
import com.jorgelillo.whoslying.domain.WordPicker
import java.util.UUID
import kotlin.random.Random
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * The game in progress and what the screens show about it, as one document for [SavedStateHandle].
 * The drawing canvas is not in it: strokes can be large, and a redrawn clue is no loss.
 */
@Serializable
private data class GameInProgress(
    val game: Game,
    val lastElimination: Elimination? = null,
    val lastPoints: Map<String, Int> = emptyMap(),
    val voteForced: Boolean = false,
    val scored: Boolean = false
)

/**
 * Saved setup (players, settings, custom packs) plus the game in progress. The game is kept in
 * [savedState], so it comes back when Android stops the app in the background mid-round (a call,
 * the camera); it ends with the task, never goes to disk or backups, and no other app can read it.
 */
class GameViewModel(
    private val repository: StateRepository,
    language: String,
    private val savedState: SavedStateHandle = SavedStateHandle()
) : ViewModel() {
    private val restored: GameInProgress? = savedState.get<String>(GAME_KEY)?.let { raw ->
        try {
            json.decodeFromString(GameInProgress.serializer(), raw)
        } catch (_: IllegalArgumentException) {
            null // a game saved by another version: start from the menu instead
        }
    }

    val saved: StateFlow<SavedState?> = repository.state.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val builtInPacks: List<WordPack> = WordPacks.builtIn(language)

    private val _game = MutableStateFlow(restored?.game)

    /** The game in progress (null before the first one); a new immutable [Game] after every move. */
    val game: StateFlow<Game?> = _game.asStateFlow()

    private val _lastElimination = MutableStateFlow(restored?.lastElimination)

    /** Who the last vote sent out; null once the group moves on to the next vote. */
    val lastElimination: StateFlow<Elimination?> = _lastElimination.asStateFlow()

    /**
     * Drawing mode: the shared canvas of the game in progress, kept across rounds. The one piece of
     * state kept as a Compose list rather than a StateFlow: the canvas adds a point on every touch
     * event (~60 a second), which a snapshot list takes in place and a flow would copy each time.
     */
    val strokes = mutableStateListOf<DrawStroke>()

    private val _lastPoints = MutableStateFlow(restored?.lastPoints.orEmpty())

    /** Points the last finished game gave, to show next to the totals. */
    val lastPoints: StateFlow<Map<String, Int>> = _lastPoints.asStateFlow()

    private val _voteForced = MutableStateFlow(restored?.voteForced ?: false)

    /** The discussion timer ran out this round: the group must vote. */
    val voteForced: StateFlow<Boolean> = _voteForced.asStateFlow()

    fun forceVote() {
        _voteForced.value = true
        keep()
    }

    /** The game in progress has already added its points to the scores. */
    private var scored = restored?.scored ?: false

    fun allPacks(state: SavedState): List<WordPack> = builtInPacks + state.customPacks

    /** Packs the next game draws from: the selected ones, or all of them. */
    fun selectedPacks(state: SavedState): List<WordPack> = allPacks(state).filter {
        state.settings.packIds.isEmpty() ||
            it.id in state.settings.packIds
    }.ifEmpty { builtInPacks }

    fun unplayed(state: SavedState): Pair<Int, Int> = WordPicker.unplayed(selectedPacks(state), state.recentWords)

    // --- Players and settings -------------------------------------------------------------------

    fun addPlayer(name: String) = update { s ->
        val clean = name.trim()
        if (clean.isEmpty() || s.players.any { it.equals(clean, ignoreCase = true) } ||
            s.players.size >= Rules.MAX_PLAYERS
        ) {
            s
        } else {
            s.copy(players = s.players + clean)
        }
    }

    fun removePlayer(name: String) = update { it.copy(players = it.players - name) }

    fun updateSettings(transform: (GameSettings) -> GameSettings) = update { s ->
        val next = transform(s.settings)
        val max = Rules.maxImpostors(s.players.size, next.mode).coerceAtLeast(1)
        s.copy(settings = next.copy(impostors = next.impostors.coerceIn(1, max)))
    }

    /**
     * The packs for the next games: empty, or every pack, means all of them (packs made later
     * included); unknown ids are dropped.
     */
    fun choosePacks(ids: Set<String>) = update { s ->
        val all = allPacks(s).map { it.id }.toSet()
        val chosen = ids intersect all
        s.copy(settings = s.settings.copy(packIds = if (chosen.isEmpty() || chosen == all) emptySet() else chosen))
    }

    fun markWelcomeSeen() = update { it.copy(welcomeSeen = true) }

    // --- Custom packs ---------------------------------------------------------------------------

    fun newPackId(): String = "custom-" + UUID.randomUUID()

    fun savePack(pack: WordPack) = update { s ->
        val others = s.customPacks.filterNot { it.id == pack.id }
        s.copy(customPacks = others + pack.copy(custom = true))
    }

    /** A pack shared from another phone (link, chat message or code); null if the text has none. */
    fun importPack(text: String): WordPack? {
        val pack = PackShare.fromText(text) ?: return null
        savePack(pack)
        return pack
    }

    fun deletePack(id: String) = update { s ->
        s.copy(
            customPacks = s.customPacks.filterNot { it.id == id },
            settings = s.settings.copy(
                packIds =
                    s.settings.packIds - id
            )
        )
    }

    // --- Game -----------------------------------------------------------------------------------

    fun startGame(): Boolean {
        val s = saved.value ?: return false
        if (!Rules.canStart(s.players.size, s.settings)) return false
        if (Match.isOver(s.roundsPlayed, s.settings.rounds)) resetScores()
        val (pack, entry) = WordPicker.pick(selectedPacks(s), s.recentWords, Random.Default)
        _game.value = Game.deal(s.players, s.settings, pack, entry, Random.Default)
        _lastElimination.value = null
        _lastPoints.value = emptyMap()
        _voteForced.value = false
        scored = false
        strokes.clear()
        keep()
        update {
            it.copy(recentWords = (listOf(entry.word) + (it.recentWords - entry.word)).take(WordPicker.RECENT_MEMORY))
        }
        return true
    }

    fun eliminate(player: String) {
        _voteForced.value = false
        val next = move { it.eliminate(player) } ?: return
        _lastElimination.value = next.lastElimination
        keep()
    }

    /** True if the drifter guessed the word. */
    fun guess(text: String): Boolean = move { it.guess(text) }?.drifterGuessed ?: false

    fun dismissElimination() {
        _lastElimination.value = null
        keep()
    }

    fun revealAll() {
        move { it.reveal() }
    }

    /** Starts a new match: scores and rounds back to zero. */
    fun resetScores() = update { it.copy(scores = emptyMap(), roundsPlayed = 0) }

    /** Applies a move to the game in progress and, once per game, adds the winners' points to the scores. */
    private fun move(change: (Game) -> Game): Game? {
        val current = _game.value?.let(change) ?: return null
        _game.value = current
        score(current)
        keep()
        return current
    }

    private fun score(current: Game) {
        if (!current.isOver || scored) return
        scored = true
        val points = Scoring.points(current)
        _lastPoints.value = points
        if (!Match.counts(current.outcome)) return
        update { s ->
            s.copy(
                scores = s.scores + points.mapValues { (player, p) -> (s.scores[player] ?: 0) + p },
                roundsPlayed = s.roundsPlayed + 1
            )
        }
    }

    private fun keep() {
        val game = _game.value ?: return
        savedState[GAME_KEY] = json.encodeToString(
            GameInProgress.serializer(),
            GameInProgress(game, _lastElimination.value, _lastPoints.value, _voteForced.value, scored)
        )
    }

    private fun update(transform: (SavedState) -> SavedState) {
        viewModelScope.launch { repository.update(transform) }
    }

    companion object {
        private const val GAME_KEY = "game_in_progress"
        private val json = Json { ignoreUnknownKeys = true }

        /** [language] picks the built-in packs: Spanish for "es", English otherwise. */
        fun factory(repository: StateRepository, language: String): ViewModelProvider.Factory = viewModelFactory {
            initializer { GameViewModel(repository, language, createSavedStateHandle()) }
        }
    }
}
