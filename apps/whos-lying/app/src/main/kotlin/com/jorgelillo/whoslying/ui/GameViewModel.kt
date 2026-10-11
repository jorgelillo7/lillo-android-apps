package com.jorgelillo.whoslying.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.jorgelillo.whoslying.data.SavedState
import com.jorgelillo.whoslying.data.StateRepository
import com.jorgelillo.whoslying.domain.Elimination
import com.jorgelillo.whoslying.domain.Game
import com.jorgelillo.whoslying.domain.GameSettings
import com.jorgelillo.whoslying.domain.Match
import com.jorgelillo.whoslying.domain.Rules
import com.jorgelillo.whoslying.domain.Scoring
import com.jorgelillo.whoslying.domain.PackShare
import com.jorgelillo.whoslying.domain.WordPack
import com.jorgelillo.whoslying.domain.WordPacks
import com.jorgelillo.whoslying.domain.WordPicker
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.random.Random

/**
 * Saved setup (players, settings, custom packs) plus the game in progress. The game lives only
 * in memory: it's a few minutes long and played with the phone in hand.
 */
class GameViewModel(private val repository: StateRepository, language: String) : ViewModel() {

    val saved: StateFlow<SavedState?> = repository.state.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val builtInPacks: List<WordPack> = WordPacks.builtIn(language)

    var game: Game? by mutableStateOf(null)
        private set

    /** Game is mutable; bump this after every change so Compose re-reads it. */
    var revision by mutableIntStateOf(0)
        private set

    var lastElimination: Elimination? by mutableStateOf(null)
        private set

    /** Drawing mode: the shared canvas of the game in progress, kept across rounds. */
    val strokes = mutableStateListOf<DrawStroke>()

    /** Points the last finished game gave, to show next to the totals. */
    var lastPoints: Map<String, Int> by mutableStateOf(emptyMap())
        private set

    /** The discussion timer ran out this round: the group must vote. */
    var voteForced by mutableStateOf(false)

    private var scoredGame: Game? = null

    fun allPacks(state: SavedState): List<WordPack> = builtInPacks + state.customPacks

    /** Packs the next game draws from: the selected ones, or all of them. */
    fun selectedPacks(state: SavedState): List<WordPack> =
        allPacks(state).filter { state.settings.packIds.isEmpty() || it.id in state.settings.packIds }.ifEmpty { builtInPacks }

    fun unplayed(state: SavedState): Pair<Int, Int> = WordPicker.unplayed(selectedPacks(state), state.recentWords)

    // --- Players and settings -------------------------------------------------------------------

    fun addPlayer(name: String) = update { s ->
        val clean = name.trim()
        if (clean.isEmpty() || s.players.any { it.equals(clean, ignoreCase = true) } || s.players.size >= Rules.MAX_PLAYERS) s
        else s.copy(players = s.players + clean)
    }

    fun removePlayer(name: String) = update { it.copy(players = it.players - name) }

    fun updateSettings(transform: (GameSettings) -> GameSettings) = update { s ->
        val next = transform(s.settings)
        val max = Rules.maxImpostors(s.players.size, next.mode).coerceAtLeast(1)
        s.copy(settings = next.copy(impostors = next.impostors.coerceIn(1, max)))
    }

    /** Adds or removes a pack from the selection; never leaves it empty-by-accident. */
    fun togglePack(id: String) = update { s ->
        val all = allPacks(s).map { it.id }.toSet()
        val current = s.settings.packIds.ifEmpty { all }
        val next = if (id in current) current - id else current + id
        val ids = when {
            next.isEmpty() -> current // keep at least one pack
            next.containsAll(all) -> emptySet()
            else -> next
        }
        s.copy(settings = s.settings.copy(packIds = ids))
    }

    fun selectAllPacks() = update { it.copy(settings = it.settings.copy(packIds = emptySet())) }

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
        s.copy(customPacks = s.customPacks.filterNot { it.id == id }, settings = s.settings.copy(packIds = s.settings.packIds - id))
    }

    // --- Game -----------------------------------------------------------------------------------

    fun startGame(): Boolean {
        val s = saved.value ?: return false
        if (!Rules.canStart(s.players.size, s.settings)) return false
        if (Match.isOver(s.roundsPlayed, s.settings.rounds)) resetScores()
        val (pack, entry) = WordPicker.pick(selectedPacks(s), s.recentWords, Random.Default)
        game = Game.deal(s.players, s.settings, pack, entry, Random.Default)
        lastElimination = null
        lastPoints = emptyMap()
        voteForced = false
        strokes.clear()
        revision++
        update { it.copy(recentWords = (listOf(entry.word) + (it.recentWords - entry.word)).take(WordPicker.RECENT_MEMORY)) }
        return true
    }

    fun eliminate(player: String) {
        voteForced = false
        lastElimination = game?.eliminate(player)
        changed()
    }

    fun guess(text: String): Boolean {
        val right = game?.guess(text) ?: false
        changed()
        return right
    }

    fun dismissElimination() {
        lastElimination = null
    }

    fun revealAll() {
        game?.reveal()
        changed()
    }

    /** Starts a new match: scores and rounds back to zero. */
    fun resetScores() = update { it.copy(scores = emptyMap(), roundsPlayed = 0) }

    /** Bumps [revision] and, once per game, adds the winners' points to the running scores. */
    private fun changed() {
        revision++
        val current = game ?: return
        if (!current.isOver || scoredGame === current) return
        scoredGame = current
        val points = Scoring.points(current)
        lastPoints = points
        if (!Match.counts(current.outcome)) return
        update { s ->
            s.copy(
                scores = s.scores + points.mapValues { (player, p) -> (s.scores[player] ?: 0) + p },
                roundsPlayed = s.roundsPlayed + 1,
            )
        }
    }

    private fun update(transform: (SavedState) -> SavedState) {
        viewModelScope.launch { repository.update(transform) }
    }

    companion object {
        /** [language] picks the built-in packs: Spanish for "es", English otherwise. */
        fun factory(repository: StateRepository, language: String): ViewModelProvider.Factory = viewModelFactory {
            initializer { GameViewModel(repository, language) }
        }
    }
}
