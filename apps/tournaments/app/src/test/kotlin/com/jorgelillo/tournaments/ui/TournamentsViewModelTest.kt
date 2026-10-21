package com.jorgelillo.tournaments.ui

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.jorgelillo.core.platform.JsonStore
import com.jorgelillo.core.testing.MainDispatcherRule
import com.jorgelillo.core.testing.awaitMatching
import com.jorgelillo.tournaments.domain.Format
import com.jorgelillo.tournaments.domain.Library
import com.jorgelillo.tournaments.domain.Share
import com.jorgelillo.tournaments.domain.Tournaments
import java.util.UUID
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** The ViewModel against a real store (DataStore on Robolectric): saves, changes and shared links. */
@RunWith(AndroidJUnit4::class)
class TournamentsViewModelTest {
    @get:Rule
    val main = MainDispatcherRule()

    // One DataStore per file and process: every test gets its own file.
    private fun viewModel() = TournamentsViewModel(
        JsonStore(ApplicationProvider.getApplicationContext(), "test-${UUID.randomUUID()}", Library.serializer()) {
            Library()
        }
    )

    private fun cup(id: String = "cup") = Tournaments.create(
        id,
        "Cup",
        epochDay = 20_000,
        names = listOf("Ann", "Bob", "Cat", "Dan"),
        Format.ELIMINATION,
        random = Random(1)
    )

    @Test
    fun newIdsAreShortAndDistinct() {
        val vm = viewModel()
        val ids = List(50) { vm.newId() }
        assertTrue(ids.all { it.length == 10 })
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun aChangeAppliesToTheStoredCopy() = runTest {
        val vm = viewModel()
        vm.library.filterNotNull().test {
            vm.save(cup())
            awaitMatching { it.tournament("cup") != null }
            // Two changes in a row: the second must see the first, not the copy the screen held.
            vm.change("cup") { it.copy(name = "Spring cup") }
            vm.change("cup") { it.copy(game = "Ping-pong") }
            val cup = awaitMatching { it.tournament("cup")?.game == "Ping-pong" }.tournament("cup")!!
            assertEquals("Spring cup", cup.name)

            vm.change("gone") { it.copy(name = "Nothing") } // a deleted tournament: ignored
            vm.delete("cup")
            assertTrue(awaitMatching { it.tournaments.isEmpty() }.tournaments.isEmpty())
        }
    }

    @Test
    fun aSharedLinkIsImportedOnceThenUpdated() = runTest {
        val vm = viewModel()
        val link = Share.link(cup())
        vm.imports.test {
            vm.importText("Our cup: $link see you there")
            assertEquals(ImportEvent(Library.ImportResult.ADDED, "cup"), awaitItem())
            vm.importText(link)
            assertEquals(ImportEvent(Library.ImportResult.ALREADY_UP_TO_DATE, "cup"), awaitItem())
            vm.importText(Share.link(cup().let { it.copy(name = "Final", revision = it.revision + 1) }))
            assertEquals(ImportEvent(Library.ImportResult.UPDATED, "cup"), awaitItem())
        }
        vm.library.filterNotNull().test {
            assertEquals("Final", awaitMatching { it.tournament("cup")?.name == "Final" }.tournament("cup")?.name)
        }
    }

    @Test
    fun aBrokenLinkSaysSo() = runTest {
        val vm = viewModel()
        vm.imports.test {
            vm.importText("https://jorgelillo7.github.io/t/#not-a-tournament")
            val event = awaitItem()
            assertNull(event.result)
            assertNull(event.id)
        }
    }
}
