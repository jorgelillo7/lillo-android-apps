package com.jorgelillo.leaguecards.ui

import android.content.Context
import androidx.lifecycle.viewModelScope
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.jorgelillo.core.platform.JsonStore
import com.jorgelillo.core.testing.MainDispatcherRule
import com.jorgelillo.core.testing.awaitMatching
import com.jorgelillo.leaguecards.domain.League
import com.jorgelillo.leaguecards.domain.Saved
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** The ViewModel against a real store (DataStore on Robolectric) and the sample deck the app ships. */
@RunWith(AndroidJUnit4::class)
class LeagueViewModelTest {
    @get:Rule
    val main = MainDispatcherRule()

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val league = context.assets.open("league.json").bufferedReader().use { League.parse(it.readText()) }

    // One DataStore per file and process: every test gets its own file.
    private fun viewModel() = LeagueViewModel(
        league,
        JsonStore(context, "test-${UUID.randomUUID()}", Saved.serializer()) {
            Saved()
        }
    ).also { main.cancelAtEnd(it.viewModelScope) }

    @Test
    fun movesWithoutASeasonAreIgnored() = runTest {
        val vm = viewModel()
        vm.saved.filterNotNull().test {
            vm.choose(right = true)
            vm.useJoker()
            vm.newSeason() // once this later write is saved, the earlier ones were too
            val saved = awaitMatching { it.season != null }
            assertEquals(0, saved.season!!.handIndex)
            assertFalse(saved.season!!.jokerUsed)
        }
    }

    @Test
    fun aNewSeasonIsDealtAndSaved() = runTest {
        val vm = viewModel()
        vm.saved.filterNotNull().test {
            vm.newSeason()
            val season = awaitMatching { it.season != null }.season!!
            assertEquals(1, season.matchday)
            assertTrue(season.hand.isNotEmpty())
            assertNull(season.ending)
        }
    }

    @Test
    fun choicesAdvanceTheHandAndTheReportIsDismissed() = runTest {
        val vm = viewModel()
        vm.saved.filterNotNull().test {
            vm.newSeason()
            var season = awaitMatching { it.season != null }.season!!
            // Answer cards until the matchday's report comes up. The deal is random: a meter can
            // also run out first and end the season, which leaves nothing to answer.
            while (!season.reportPending && season.ending == null) {
                val before = season
                vm.choose(right = true)
                season = awaitMatching { it.season != before }.season!!
            }
            if (season.reportPending) {
                vm.dismissReport()
                assertFalse(awaitMatching { !it.season!!.reportPending }.season!!.reportPending)
            }
        }
    }

    @Test
    fun theJokerIsUsedOnce() = runTest {
        val vm = viewModel()
        vm.saved.filterNotNull().test {
            vm.newSeason()
            awaitMatching { it.season != null }
            vm.useJoker()
            assertTrue(awaitMatching { it.season!!.jokerUsed }.season!!.jokerUsed)
        }
    }
}
