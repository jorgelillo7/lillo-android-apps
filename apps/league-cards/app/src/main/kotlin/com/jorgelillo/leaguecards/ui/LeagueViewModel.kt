package com.jorgelillo.leaguecards.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.jorgelillo.core.platform.JsonStore
import com.jorgelillo.leaguecards.domain.Game
import com.jorgelillo.leaguecards.domain.League
import com.jorgelillo.leaguecards.domain.Saved
import com.jorgelillo.leaguecards.domain.Season
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.random.Random

/** Single ViewModel for the whole (small) app: the saved state and every move. */
class LeagueViewModel(val league: League, private val store: JsonStore<Saved>) : ViewModel() {

    /** Null until the first read from disk, so screens don't flash empty. */
    val saved: StateFlow<Saved?> = store.state.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun newSeason() = update { Game.newSeason(league, Random.nextLong()) }

    fun choose(right: Boolean) = updateSeason { Game.choose(league, it, right) }

    fun useJoker() = updateSeason { Game.useJoker(league, it) }

    fun dismissReport() = updateSeason(Game::dismissReport)

    private fun updateSeason(change: (Season) -> Season) = viewModelScope.launch {
        store.update { saved -> saved.season?.let { saved.with(league, change(it)) } ?: saved }
    }

    private fun update(next: () -> Season) = viewModelScope.launch {
        store.update { it.with(league, next()) }
    }

    companion object {
        fun factory(league: League, store: JsonStore<Saved>): ViewModelProvider.Factory = viewModelFactory {
            initializer { LeagueViewModel(league, store) }
        }
    }
}
