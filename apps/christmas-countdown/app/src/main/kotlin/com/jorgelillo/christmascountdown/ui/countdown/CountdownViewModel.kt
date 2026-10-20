package com.jorgelillo.christmascountdown.ui.countdown

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.jorgelillo.christmascountdown.ChristmasCountdownApplication
import com.jorgelillo.christmascountdown.domain.ChristmasCountdown
import com.jorgelillo.christmascountdown.domain.CountdownState
import com.jorgelillo.christmascountdown.widget.updateCountdownWidgets
import java.time.ZonedDateTime
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CountdownUiState(
    val countdown: CountdownState,
    val sleepsMode: Boolean = false,
    val musicEnabled: Boolean = false
)

class CountdownViewModel(private val app: ChristmasCountdownApplication) : ViewModel() {

    /** Emits the current time at the start of every wall-clock second. */
    private val clock = flow {
        while (true) {
            val now = ZonedDateTime.now()
            emit(now)
            delay(1_000L - now.nano / 1_000_000)
        }
    }

    val uiState: StateFlow<CountdownUiState> =
        combine(clock, app.settings.sleepsMode, app.settings.musicEnabled) { now, sleeps, music ->
            CountdownUiState(ChristmasCountdown.stateAt(now), sleeps, music)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = CountdownUiState(ChristmasCountdown.stateAt(ZonedDateTime.now()))
        )

    init {
        viewModelScope.launch { updateCountdownWidgets(app) }
    }

    fun setSleepsMode(enabled: Boolean) {
        viewModelScope.launch {
            app.settings.setSleepsMode(enabled)
            updateCountdownWidgets(app)
        }
    }

    fun setMusicEnabled(enabled: Boolean) {
        viewModelScope.launch { app.settings.setMusicEnabled(enabled) }
    }

    companion object {
        fun factory(app: ChristmasCountdownApplication): ViewModelProvider.Factory = viewModelFactory {
            initializer { CountdownViewModel(app) }
        }
    }
}
