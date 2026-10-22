package com.jorgelillo.slipper.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.jorgelillo.slipper.data.Saved
import com.jorgelillo.slipper.data.SavedStore
import com.jorgelillo.slipper.domain.Challenge
import com.jorgelillo.slipper.domain.Challenges
import kotlin.random.Random
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** The best score on disk, the challenge being played, and what a finished run changes. */
class SlipperViewModel(private val store: SavedStore, private val newRandomSeed: () -> Long = { Random.nextLong() }) :
    ViewModel() {

    /** Null until the first read from disk. */
    val saved: StateFlow<Saved?> = store.state.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _challenge = MutableStateFlow<Challenge?>(null)

    /** A friend's challenge from a link: runs replay its seed until it is dismissed. */
    val challenge: StateFlow<Challenge?> = _challenge.asStateFlow()

    private val _invalidLink = MutableStateFlow(false)

    /** Text was shared to the app but held no challenge. */
    val invalidLink: StateFlow<Boolean> = _invalidLink.asStateFlow()

    fun importText(text: String) {
        val challenge = Challenges.fromText(text)
        if (challenge == null) _invalidLink.value = true else _challenge.value = challenge
    }

    fun dismissChallenge() {
        _challenge.value = null
    }

    fun dismissInvalidLink() {
        _invalidLink.value = false
    }

    /** The challenge's seed (same slippers as the friend), or a new random run. */
    fun nextSeed(): Long = challenge.value?.seed ?: newRandomSeed()

    fun finish(score: Int, croquetas: Int) {
        viewModelScope.launch {
            store.update {
                it.copy(
                    best = maxOf(it.best, score),
                    runs = it.runs + 1,
                    croquetas =
                        it.croquetas + croquetas
                )
            }
        }
    }

    fun challengeLink(seed: Long, score: Int): String = Challenges.link(Challenge(seed, score))

    companion object {
        fun factory(store: SavedStore): ViewModelProvider.Factory = viewModelFactory {
            initializer { SlipperViewModel(store) }
        }
    }
}
