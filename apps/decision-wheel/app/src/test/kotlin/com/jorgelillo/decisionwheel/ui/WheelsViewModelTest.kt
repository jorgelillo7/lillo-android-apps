package com.jorgelillo.decisionwheel.ui

import androidx.lifecycle.viewModelScope
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.jorgelillo.core.platform.JsonStore
import com.jorgelillo.core.testing.MainDispatcherRule
import com.jorgelillo.core.testing.awaitMatching
import com.jorgelillo.decisionwheel.domain.AppState
import com.jorgelillo.decisionwheel.domain.Wheel
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** The ViewModel against a real store (DataStore on Robolectric): what each action saves. */
@RunWith(AndroidJUnit4::class)
class WheelsViewModelTest {
    @get:Rule
    val main = MainDispatcherRule()

    private val dinner = Wheel("dinner", "Dinner", listOf("Pizza", "Sushi", "Tacos"))
    private val movie = Wheel("movie", "Movie", listOf("Comedy", "Horror"))

    // One DataStore per file and process: every test gets its own file.
    private fun viewModel() = WheelsViewModel(
        JsonStore(ApplicationProvider.getApplicationContext(), "test-${UUID.randomUUID()}", AppState.serializer()) {
            AppState(wheels = listOf(dinner, movie))
        }
    ).also { main.cancelAtEnd(it.viewModelScope) }

    @Test
    fun savingAddsANewWheelAndReplacesAnExistingOne() = runTest {
        val vm = viewModel()
        vm.state.filterNotNull().test {
            val walk = Wheel(vm.newWheelId(), "Walk", listOf("Park", "Beach"))
            vm.save(walk)
            assertEquals(listOf(dinner, movie, walk), awaitMatching { it.wheels.size == 3 }.wheels)

            vm.save(dinner.copy(options = listOf("Pizza")))
            val saved = awaitMatching { it.wheel("dinner")?.options == listOf("Pizza") }
            assertEquals(listOf("dinner", "movie", walk.id), saved.wheels.map { it.id }) // same place
        }
    }

    @Test
    fun deletingAWheelDropsItsHistoryOnly() = runTest {
        val vm = viewModel()
        vm.state.filterNotNull().test {
            vm.accept("dinner", "Sushi")
            vm.accept("movie", "Horror")
            awaitMatching { it.history.size == 2 }

            vm.delete("dinner")
            val state = awaitMatching { it.wheels.size == 1 }
            assertEquals(listOf("movie"), state.history.map { it.wheelId })
        }
    }

    @Test
    fun acceptingRecordsTheDecisionNewestFirst() = runTest {
        val vm = viewModel()
        vm.state.filterNotNull().test {
            vm.accept("dinner", "Pizza")
            vm.accept("dinner", "Tacos")
            val state = awaitMatching { it.history.size == 2 }
            assertEquals(listOf("Tacos", "Pizza"), state.history.map { it.option })

            vm.clearHistory("dinner")
            assertTrue(awaitMatching { it.history.isEmpty() }.history.isEmpty())
        }
    }

    @Test
    fun settingsAndFavouritesAreSaved() = runTest {
        val vm = viewModel()
        vm.state.filterNotNull().test {
            vm.setAvoidRepeats(false)
            vm.setSound(false)
            vm.toggleFavorite("movie")
            val state = awaitMatching { it.wheel("movie")?.favorite == true }
            assertFalse(state.avoidRepeats)
            assertFalse(state.soundOn)
            assertEquals(listOf("movie", "dinner"), state.sortedWheels.map { it.id })
        }
    }

    @Test
    fun restoringPresetsBringsBackOnlyTheMissingOnes() = runTest {
        val vm = viewModel()
        vm.state.filterNotNull().test {
            vm.delete("movie")
            awaitMatching { it.wheels.size == 1 }
            vm.restorePresets(listOf(dinner.copy(name = "Renamed"), movie))
            val state = awaitMatching { it.wheels.size == 2 }
            assertEquals("Dinner", state.wheel("dinner")?.name) // the user's copy is kept
        }
    }
}
