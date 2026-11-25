package com.jorgelillo.christmascountdown.ui.advent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.jorgelillo.christmascountdown.data.SettingsRepository
import com.jorgelillo.christmascountdown.domain.AdventCalendar
import com.jorgelillo.christmascountdown.domain.AdventDoor
import com.jorgelillo.christmascountdown.domain.AdventPlan
import com.jorgelillo.core.platform.LilloToday
import java.time.LocalDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AdventUiState(
    val unlockedDoors: Int,
    val doorOrder: List<Int>,
    /** This year's activity or song behind each door. */
    val plan: Map<Int, AdventDoor> = emptyMap(),
    val openedDoors: Set<Int> = emptySet()
) {
    val isSeason: Boolean get() = unlockedDoors > 0

    companion object {
        fun on(today: LocalDate, openedDoors: Set<Int> = emptySet()) = AdventUiState(
            unlockedDoors = AdventCalendar.unlockedDoors(today),
            doorOrder = AdventCalendar.doorOrder(today.year),
            plan = AdventPlan.forYear(today.year),
            openedDoors = openedDoors
        )
    }
}

/** The advent calendar for today; [onResume] moves it on when the app comes back on a new day. */
@OptIn(ExperimentalCoroutinesApi::class)
class AdventViewModel(private val settings: SettingsRepository, clock: () -> LocalDate = LocalDate::now) :
    ViewModel() {

    private val today = LilloToday(clock)

    val uiState: StateFlow<AdventUiState> = today.date
        .flatMapLatest { day -> settings.openedDoors(day.year).map { AdventUiState.on(day, it) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AdventUiState.on(today.date.value))

    fun canOpen(door: Int): Boolean = AdventCalendar.canOpen(door, today.date.value)

    fun open(door: Int) {
        if (!canOpen(door)) return
        val year = today.date.value.year
        viewModelScope.launch { settings.markDoorOpened(year, door) }
    }

    /** Back in the app: a new day may have opened another door. */
    fun onResume() = today.check()

    companion object {
        fun factory(settings: SettingsRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer { AdventViewModel(settings) }
        }
    }
}
