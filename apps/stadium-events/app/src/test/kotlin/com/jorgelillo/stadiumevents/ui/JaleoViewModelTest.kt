package com.jorgelillo.stadiumevents.ui

import androidx.lifecycle.viewModelScope
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.jorgelillo.core.platform.JsonStore
import com.jorgelillo.core.testing.MainDispatcherRule
import com.jorgelillo.core.testing.StoreTimeout
import com.jorgelillo.core.testing.awaitMatching
import com.jorgelillo.stadiumevents.SampleFeed
import com.jorgelillo.stadiumevents.data.FeedFetcher
import com.jorgelillo.stadiumevents.data.FeedRepository
import com.jorgelillo.stadiumevents.domain.AppState
import com.jorgelillo.stadiumevents.domain.Tomorrow
import java.io.IOException
import java.time.LocalDate
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** The ViewModel against a real store (DataStore on Robolectric) and a fake download. */
@RunWith(AndroidJUnit4::class)
class JaleoViewModelTest {
    @get:Rule
    val main = MainDispatcherRule()

    private val today = LocalDate.of(2026, 11, 24)
    private val now = 1_795_000_000_000L
    private var downloads = 0

    private fun store(initial: AppState = AppState()) =
        JsonStore(ApplicationProvider.getApplicationContext(), "test-${UUID.randomUUID()}", AppState.serializer()) {
            initial
        }

    private fun viewModel(
        store: JsonStore<AppState> = store(),
        fetcher: FeedFetcher = FeedFetcher {
            downloads++
            SampleFeed.json(today)
        }
    ) = JaleoViewModel(FeedRepository(store, fetcher), today = { today }, now = { now })
        .also { main.cancelAtEnd(it.viewModelScope) }

    @Test
    fun firstOpenDownloadsTheEventsAndAnswersTomorrow() = runTest {
        val vm = viewModel()
        vm.state.filterNotNull().test(timeout = StoreTimeout) {
            val state = awaitMatching { it.fetchedAt != null }
            val busy = assertIs<Tomorrow.Busy>(state.tomorrow)
            assertEquals("atletico-vs-viking", busy.event.id)
            assertEquals(
                listOf("atletico-vs-betis", "2027-uefa-champions-league-final", "karol-g"),
                state.upcoming.map { it.event.id }
            )
            assertEquals(now, state.fetchedAt)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun aFailedDownloadKeepsTheLastCopyAndSaysSo() = runTest {
        val saved = store(AppState(SampleFeed.feed(today), fetchedAt = now - 5 * 60 * 60 * 1000L))
        val vm = viewModel(saved, FeedFetcher { throw IOException("no network") })
        vm.state.filterNotNull().test(timeout = StoreTimeout) {
            val state = awaitMatching { it.offline }
            assertIs<Tomorrow.Busy>(state.tomorrow)
            assertEquals(now - 5 * 60 * 60 * 1000L, state.fetchedAt)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun aRecentCopyIsNotDownloadedAgain() = runTest {
        val saved = store(AppState(SampleFeed.feed(today), fetchedAt = now - 60 * 60 * 1000L))
        val vm = viewModel(saved)
        vm.state.filterNotNull().test(timeout = StoreTimeout) {
            awaitMatching { it.fetchedAt != null }
            assertEquals(0, downloads)
            vm.refresh()
            awaitMatching { it.fetchedAt == now }
            assertEquals(1, downloads)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun theNotificationsQuestionIsAskedOnce() = runTest {
        val saved = store()
        val vm = viewModel(saved)
        vm.state.filterNotNull().test(timeout = StoreTimeout) {
            assertEquals(false, awaitMatching { it.fetchedAt != null }.notificationsAsked)
            vm.notificationsAsked()
            awaitMatching { it.notificationsAsked }
            cancelAndIgnoreRemainingEvents()
        }
        assertTrue(saved.state.first().notificationsAsked)
    }
}
