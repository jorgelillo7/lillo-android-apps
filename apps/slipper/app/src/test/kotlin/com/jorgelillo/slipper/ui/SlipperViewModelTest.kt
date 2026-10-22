package com.jorgelillo.slipper.ui

import androidx.lifecycle.viewModelScope
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.jorgelillo.core.platform.JsonStore
import com.jorgelillo.core.testing.MainDispatcherRule
import com.jorgelillo.core.testing.awaitMatching
import com.jorgelillo.slipper.data.Saved
import com.jorgelillo.slipper.domain.Challenge
import com.jorgelillo.slipper.domain.Challenges
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

/** The ViewModel against a real store (DataStore on Robolectric): best score, totals, challenges. */
@RunWith(AndroidJUnit4::class)
class SlipperViewModelTest {
    @get:Rule
    val main = MainDispatcherRule()

    // One DataStore per file and process: every test gets its own file.
    private fun viewModel(seed: Long = 7) = SlipperViewModel(
        JsonStore(ApplicationProvider.getApplicationContext(), "test-${UUID.randomUUID()}", Saved.serializer()) {
            Saved()
        },
        newRandomSeed = { seed }
    ).also { main.cancelAtEnd(it.viewModelScope) }

    @Test
    fun aRunKeepsTheBestScoreAndAddsUpTotals() = runTest {
        val vm = viewModel()
        vm.saved.filterNotNull().test {
            vm.finish(score = 500, croquetas = 3)
            awaitMatching { it.runs == 1 }
            vm.finish(score = 200, croquetas = 2)
            assertEquals(Saved(best = 500, runs = 2, croquetas = 5), awaitMatching { it.runs == 2 })
        }
    }

    @Test
    fun aChallengeLinkSetsTheSeedUntilDismissed() = runTest {
        val vm = viewModel(seed = 7)
        assertEquals(7, vm.nextSeed())
        vm.importText("¿Me ganas? " + Challenges.link(Challenge(seed = 99, score = 1240)))
        assertEquals(Challenge(99, 1240), vm.challenge.value)
        assertEquals(99, vm.nextSeed())
        vm.dismissChallenge()
        assertNull(vm.challenge.value)
        assertEquals(7, vm.nextSeed())
    }

    @Test
    fun textWithoutAChallengeIsReported() = runTest {
        val vm = viewModel()
        vm.importText("see you at 8")
        assertTrue(vm.invalidLink.value)
        assertNull(vm.challenge.value)
        vm.dismissInvalidLink()
        assertFalse(vm.invalidLink.value)
    }

    @Test
    fun theChallengeLinkCarriesSeedAndScore() {
        val link = viewModel().challengeLink(seed = 5, score = 321)
        assertEquals(Challenge(5, 321), Challenges.fromText(link))
    }
}
