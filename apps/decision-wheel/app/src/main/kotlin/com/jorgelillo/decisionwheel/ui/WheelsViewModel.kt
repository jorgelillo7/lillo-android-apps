package com.jorgelillo.decisionwheel.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.jorgelillo.decisionwheel.data.StateRepository
import com.jorgelillo.decisionwheel.domain.AppState
import com.jorgelillo.decisionwheel.domain.Decision
import com.jorgelillo.decisionwheel.domain.Wheel
import java.util.UUID
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Single ViewModel for the whole (small) app: exposes the state and every mutation. */
class WheelsViewModel(private val repository: StateRepository) : ViewModel() {

    /** Null until the first read from disk, so screens don't flash empty. */
    val state: StateFlow<AppState?> = repository.state
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun newWheelId(): String = UUID.randomUUID().toString()

    /** [then] runs once the wheel is on disk: opening a new wheel earlier could find it missing. */
    fun save(wheel: Wheel, then: () -> Unit = {}) = update(then) { state ->
        val exists = state.wheels.any { it.id == wheel.id }
        state.copy(
            wheels = if (exists) {
                state.wheels.map {
                    if (it.id ==
                        wheel.id
                    ) {
                        wheel
                    } else {
                        it
                    }
                }
            } else {
                state.wheels + wheel
            }
        )
    }

    fun delete(wheelId: String) = update { state ->
        state.copy(
            wheels = state.wheels.filterNot { it.id == wheelId },
            history = state.history.filterNot {
                it.wheelId ==
                    wheelId
            }
        )
    }

    fun accept(wheelId: String, option: String) = update {
        it.withDecision(Decision(wheelId, option, System.currentTimeMillis()))
    }

    fun clearHistory(wheelId: String) = update { state ->
        state.copy(
            history = state.history.filterNot {
                it.wheelId ==
                    wheelId
            }
        )
    }

    fun setAvoidRepeats(enabled: Boolean) = update { it.copy(avoidRepeats = enabled) }

    fun setSound(enabled: Boolean) = update { it.copy(soundOn = enabled) }

    fun toggleFavorite(wheelId: String) = update { it.toggleFavorite(wheelId) }

    fun restorePresets(presets: List<Wheel>) = update { it.restorePresets(presets) }

    private fun update(then: () -> Unit = {}, transform: (AppState) -> AppState) {
        viewModelScope.launch {
            repository.update(transform)
            then()
        }
    }

    companion object {
        fun factory(repository: StateRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer { WheelsViewModel(repository) }
        }
    }
}
