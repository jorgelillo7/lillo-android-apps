package com.jorgelillo.grouppolls.ui

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.jorgelillo.core.platform.JsonStore
import com.jorgelillo.core.testing.MainDispatcherRule
import com.jorgelillo.core.testing.awaitMatching
import com.jorgelillo.grouppolls.data.FakePollRepository
import com.jorgelillo.grouppolls.data.LocalState
import com.jorgelillo.grouppolls.data.MyVote
import com.jorgelillo.grouppolls.data.PollException
import com.jorgelillo.grouppolls.data.PollRepository
import com.jorgelillo.grouppolls.domain.Duration
import com.jorgelillo.grouppolls.domain.Poll
import com.jorgelillo.grouppolls.domain.Side
import com.jorgelillo.grouppolls.domain.Visibility
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * The ViewModel against the in-memory backend (the same rules as Firestore's) and a real local
 * store (DataStore on Robolectric): links, creating, voting, predictions, reports, deleting data.
 */
@RunWith(AndroidJUnit4::class)
class PollsViewModelTest {
    @get:Rule
    val main = MainDispatcherRule()

    private var clock = 1_800_000_000_000L
    private val backend = FakePollRepository(now = { clock })

    // One DataStore per file and process: every test gets its own file.
    private fun viewModel(repository: PollRepository = backend) = PollsViewModel(
        repository,
        JsonStore(ApplicationProvider.getApplicationContext(), "test-${UUID.randomUUID()}", LocalState.serializer()) {
            LocalState()
        },
        language = "en",
        now = { clock }
    )

    /** Creates a poll and returns its code, as the UI gets it. */
    private suspend fun PollsViewModel.newPoll(visibility: Visibility = Visibility.PRIVATE): String {
        create("Friday dinner?", "Pizza", "Sushi", visibility, Duration.NO_LIMIT)
        return created.filterNotNull().first().also { consumeCreated() }
    }

    @Test
    fun anOpenedLinkIsShownAndRemembered() = runTest {
        val vm = viewModel()
        vm.local.filterNotNull().test {
            vm.open("see you there")
            vm.open("https://jorgelillo7.github.io/q/?c=abcdefghijkm")
            assertEquals("abcdefghijkm", vm.openCode.value)
            assertEquals(listOf("abcdefghijkm"), awaitMatching { it.myPolls.isNotEmpty() }.myPolls)
            vm.consumeOpenCode()
            assertNull(vm.openCode.value)
        }
    }

    @Test
    fun aCreatedPollCarriesTheNameAndIsRemembered() = runTest {
        val vm = viewModel()
        vm.local.filterNotNull().test {
            vm.setName("  Ana  ")
            awaitMatching { it.name.isNotEmpty() }
            val code = vm.newPoll()
            val poll = assertNotNull(backend.poll(code).first())
            assertEquals("Ana", poll.creatorName)
            assertEquals("en", poll.language)
            assertNull(poll.closesAt)
            assertEquals(code, awaitMatching { it.myPolls.isNotEmpty() }.myPolls.first())
        }
    }

    @Test
    fun aFailedCreateShowsTheError() = runTest {
        val offline = object : PollRepository by backend {
            override suspend fun create(poll: Poll) = throw PollException("offline")
        }
        val vm = viewModel(offline)
        vm.creating.test {
            assertFalse(awaitItem())
            vm.create("Q?", "A", "B", Visibility.PUBLIC, Duration.ONE_DAY)
            assertTrue(awaitItem())
            assertFalse(awaitItem()) // done: the button is back
        }
        assertNull(vm.created.value)
        assertEquals("offline", vm.error.value)
        vm.clearError()
        assertNull(vm.error.value)
    }

    @Test
    fun aVoteIsRememberedAndCannotBeCastTwice() = runTest {
        val vm = viewModel()
        val code = vm.newPoll()
        val poll = backend.poll(code).first()!!
        vm.local.filterNotNull().test {
            vm.vote(poll, Side.RED, prediction = Side.BLUE)
            assertEquals(MyVote(Side.RED, Side.BLUE), awaitMatching { code in it.votes }.votes[code])
        }
        assertEquals(1, backend.poll(code).first()!!.redVotes)

        vm.vote(poll, Side.BLUE, prediction = Side.BLUE).join()
        assertEquals("already voted", vm.error.value)
    }

    @Test
    fun aPredictionCountsOnceTheClosedPollHasAWinner() = runTest {
        val vm = viewModel()
        val code = vm.newPoll()
        vm.local.filterNotNull().test {
            vm.vote(backend.poll(code).first()!!, Side.RED, prediction = Side.RED)
            awaitMatching { code in it.votes }

            vm.resolvePrediction(backend.poll(code).first()!!) // still open: nothing to count
            vm.close(code).join()
            clock += 1
            val closed = backend.poll(code).first()!!
            vm.resolvePrediction(closed)
            val counted = awaitMatching { it.predictionsResolved == 1 }
            assertEquals(1, counted.predictionHits)
            vm.resolvePrediction(closed) // already counted
            vm.setName("Ana") // once this later write is saved, a second count would have been too
            assertEquals(1, awaitMatching { it.name == "Ana" }.predictionsResolved)
        }
    }

    @Test
    fun reportingAndHidingACreatorTakesTheirPollsOutOfTheFeed() = runTest {
        val vm = viewModel()
        lateinit var reported: Poll
        vm.feed.test {
            reported = awaitMatching { it.isNotEmpty() }.flatMap { it.second }.first()
            vm.report(reported, hideCreator = true)
            awaitMatching { feed -> feed.flatMap { it.second }.none { it.creatorId == reported.creatorId } }
        }
        vm.local.filterNotNull().test {
            assertTrue(reported.code in awaitMatching { it.reported.isNotEmpty() }.reported)
        }
    }

    @Test
    fun deletingMyDataForgetsEverythingHere() = runTest {
        val vm = viewModel()
        vm.local.filterNotNull().test {
            vm.setName("Ana")
            vm.acceptTerms()
            vm.newPoll()
            awaitMatching { it.myPolls.isNotEmpty() && it.acceptedTerms }
            var done = false
            vm.deleteMyData { done = true }.join()
            assertTrue(done)
            assertEquals(LocalState(), awaitMatching { it.myPolls.isEmpty() })
            assertFalse(vm.local.value!!.acceptedTerms)
        }
    }
}
