package com.jorgelillo.whoslying.ui

import androidx.lifecycle.viewModelScope
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.jorgelillo.core.platform.JsonStore
import com.jorgelillo.core.testing.MainDispatcherRule
import com.jorgelillo.core.testing.awaitMatching
import com.jorgelillo.whoslying.data.SavedState
import com.jorgelillo.whoslying.domain.Outcome
import com.jorgelillo.whoslying.domain.Role
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * The ViewModel against a real store (DataStore on Robolectric): what each action saves, and that a
 * finished game is scored exactly once. The game rules themselves are tested in :domain.
 */
@RunWith(AndroidJUnit4::class)
class GameViewModelTest {
    @get:Rule
    val main = MainDispatcherRule()

    // One DataStore per file and process: every test gets its own file.
    private fun viewModel() = GameViewModel(
        JsonStore(ApplicationProvider.getApplicationContext(), "test-${UUID.randomUUID()}", SavedState.serializer()) {
            SavedState()
        },
        language = "en"
    ).also { main.cancelAtEnd(it.viewModelScope) }

    private fun GameViewModel.addPlayers(vararg names: String) = names.forEach(::addPlayer)

    @Test
    fun addPlayerTrimsAndSkipsBlanksAndDuplicates() = runTest {
        val vm = viewModel()
        vm.saved.filterNotNull().test {
            vm.addPlayers("  Ana ", "", "ana", "Bea")
            assertEquals(listOf("Ana", "Bea"), awaitMatching { it.players.size == 2 }.players)
            vm.removePlayer("Ana")
            assertEquals(listOf("Bea"), awaitMatching { it.players.size == 1 }.players)
        }
    }

    @Test
    fun updateSettingsKeepsImpostorsWithinWhatThePlayersAllow() = runTest {
        val vm = viewModel()
        vm.saved.filterNotNull().test {
            vm.addPlayers("Ana", "Bea", "Carlos", "Dani", "Eva", "Fran", "Gus")
            awaitMatching { it.players.size == 7 }
            vm.updateSettings { it.copy(impostors = 5) }
            // Seven players leave room for three impostors (fewer than half of the others).
            assertEquals(3, awaitMatching { it.settings.impostors != 1 }.settings.impostors)
        }
    }

    @Test
    fun choosingPacksKeepsASubsetAndEveryPackMeansAll() = runTest {
        val vm = viewModel()
        val only = vm.builtInPacks.first().id
        vm.saved.filterNotNull().test {
            vm.choosePacks(setOf(only, "gone"))
            assertEquals(setOf(only), awaitMatching { it.settings.packIds == setOf(only) }.settings.packIds)
            vm.choosePacks(vm.builtInPacks.map { it.id }.toSet())
            assertTrue(awaitMatching { it.settings.packIds.isEmpty() }.settings.packIds.isEmpty())
        }
    }

    @Test
    fun aGameNeedsEnoughPlayers() = runTest {
        val vm = viewModel()
        vm.saved.filterNotNull().test {
            vm.addPlayers("Ana", "Bea")
            awaitMatching { it.players.size == 2 }
            assertFalse(vm.startGame())
            assertNull(vm.game.value)
        }
    }

    @Test
    fun startingAGameDealsAndRemembersTheWord() = runTest {
        val vm = viewModel()
        vm.saved.filterNotNull().test {
            vm.addPlayers("Ana", "Bea", "Carlos", "Dani")
            awaitMatching { it.players.size == 4 }
            vm.game.test {
                assertNull(awaitItem())
                assertTrue(vm.startGame())
                val game = assertNotNull(awaitItem())
                assertEquals(1, game.cards.count { it.role == Role.IMPOSTOR })
            }
            val word = vm.game.value!!.civilianWord
            assertEquals(listOf(word), awaitMatching { it.recentWords.isNotEmpty() }.recentWords)
        }
    }

    @Test
    fun catchingTheImpostorScoresTheGameOnce() = runTest {
        val vm = viewModel()
        vm.saved.filterNotNull().test {
            vm.addPlayers("Ana", "Bea", "Carlos", "Dani")
            awaitMatching { it.players.size == 4 }
            vm.startGame()
            val impostor = vm.game.value!!.cards.first { it.role == Role.IMPOSTOR }.player

            vm.eliminate(impostor)
            assertEquals(Outcome.CiviliansWin, vm.game.value!!.outcome)
            assertEquals(impostor, vm.lastElimination.value?.player)
            val scored = awaitMatching { it.roundsPlayed == 1 }
            // The three civilians score.
            assertEquals(3, scored.scores.size)
            assertEquals(vm.lastPoints.value, scored.scores)

            // Revealing a finished game must not add the same points again; once a later write is
            // saved, a second scoring would have been too.
            vm.revealAll()
            vm.addPlayer("Eva")
            val later = awaitMatching { it.players.size == 5 }
            assertEquals(1, later.roundsPlayed)
            assertEquals(scored.scores, later.scores)

            vm.dismissElimination()
            assertNull(vm.lastElimination.value)
            vm.resetScores()
            val reset = awaitMatching { it.roundsPlayed == 0 }
            assertTrue(reset.scores.isEmpty())
        }
    }

    @Test
    fun aGameEndedEarlyIsNotARound() = runTest {
        val vm = viewModel()
        vm.saved.filterNotNull().test {
            vm.addPlayers("Ana", "Bea", "Carlos", "Dani")
            awaitMatching { it.players.size == 4 }
            vm.startGame()
            awaitMatching { it.recentWords.isNotEmpty() }

            vm.revealAll()
            assertEquals(Outcome.EndedEarly, vm.game.value!!.outcome)
            vm.addPlayer("Eva") // once this later write is saved, any scoring would have been too
            val state = awaitMatching { it.players.size == 5 }
            assertEquals(0, state.roundsPlayed)
            assertTrue(state.scores.isEmpty())
        }
    }

    @Test
    fun importPackIgnoresTextWithoutAPack() = runTest {
        assertNull(viewModel().importPack("see you at 8"))
    }
}
