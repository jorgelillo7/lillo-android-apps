package com.jorgelillo.stadiumevents.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.jorgelillo.core.platform.WidgetOffer
import com.jorgelillo.core.platform.WidgetPins
import com.jorgelillo.stadiumevents.data.FeedRepository
import com.jorgelillo.stadiumevents.domain.AppState
import com.jorgelillo.stadiumevents.domain.Jaleo
import com.jorgelillo.stadiumevents.domain.Tomorrow
import com.jorgelillo.stadiumevents.domain.UpcomingDay
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** What the one screen shows. */
data class JaleoUiState(
    val today: LocalDate,
    val tomorrow: Tomorrow,
    val upcoming: List<UpcomingDay>,
    /** Null until the first download worked: the screen says it is looking for events. */
    val fetchedAt: Long?,
    val refreshing: Boolean = false,
    val offline: Boolean = false,
    val notificationsAsked: Boolean = true,
    /** For "checked at": the phone's, fixed in tests so screenshots don't depend on the machine. */
    val zone: ZoneId = ZoneId.systemDefault()
) {
    companion object {
        fun from(app: AppState, today: LocalDate, refreshing: Boolean = false, offline: Boolean = false) = JaleoUiState(
            today = today,
            tomorrow = Jaleo.tomorrow(app.feed.events, today),
            upcoming = Jaleo.upcoming(app.feed.events, today),
            fetchedAt = app.fetchedAt,
            refreshing = refreshing,
            offline = offline,
            notificationsAsked = app.notificationsAsked
        )
    }
}

class JaleoViewModel(
    private val repository: FeedRepository,
    private val widgets: WidgetPins,
    private val today: () -> LocalDate = LocalDate::now,
    private val now: () -> Long = System::currentTimeMillis
) : ViewModel() {
    private val refreshing = MutableStateFlow(false)
    private val offline = MutableStateFlow(false)
    private val _widget = MutableStateFlow(widgets.offer())

    /** The home-screen widget: offered until one is placed (checked again on every return to the app). */
    val widget: StateFlow<WidgetOffer> = _widget.asStateFlow()

    /** Null until the first read from disk, so the screen doesn't flash the wrong answer. */
    val state: StateFlow<JaleoUiState?> = combine(repository.state, refreshing, offline) { app, busy, failed ->
        JaleoUiState.from(app, today(), busy, failed)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    init {
        refreshIfStale()
    }

    /** On open: download again if the copy on the phone is older than [STALE_MILLIS]. */
    fun refreshIfStale() {
        viewModelScope.launch {
            val fetchedAt = repository.state.first().fetchedAt
            if (fetchedAt == null || now() - fetchedAt > STALE_MILLIS) refresh()
        }
    }

    fun refresh() {
        if (refreshing.value) return
        refreshing.value = true
        viewModelScope.launch {
            try {
                offline.value = !repository.refresh(now())
            } finally {
                refreshing.value = false
            }
        }
    }

    /** Back in the app: the user may have added (or removed) the widget from the launcher meanwhile. */
    fun refreshWidget() {
        _widget.value = widgets.offer()
    }

    /** Asks the launcher to place the widget; the card keeps the way by hand for launchers that drop it. */
    fun addWidget() {
        widgets.requestPin()
        refreshWidget()
    }

    fun notificationsAsked() {
        viewModelScope.launch { repository.markNotificationsAsked() }
    }

    companion object {
        const val STALE_MILLIS = 3 * 60 * 60 * 1000L

        fun factory(repository: FeedRepository, widgets: WidgetPins): ViewModelProvider.Factory = viewModelFactory {
            initializer { JaleoViewModel(repository, widgets) }
        }
    }
}
