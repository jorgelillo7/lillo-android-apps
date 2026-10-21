package com.jorgelillo.grouppolls

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.jorgelillo.grouppolls.data.FakePollRepository
import com.jorgelillo.grouppolls.data.LocalState
import com.jorgelillo.grouppolls.domain.Side
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * The app's journeys through the real MainActivity on the JVM, like tools/smoke_test.sh on an
 * emulator but in seconds: pick a name, vote and predict on a public poll, create a private poll,
 * vote on it and close it. Runs on the in-memory backend only (never against a real Firebase
 * project). One test per class: the app's DataStore file can only be open once per process.
 */
@OptIn(ExperimentalTestApi::class)
@RunWith(AndroidJUnit4::class)
@Config(sdk = [36], qualifiers = RobolectricDeviceQualifiers.Pixel7)
class PollFlowTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    private val app get() = compose.activity.application as GroupPollsApplication

    @Before
    fun inMemoryBackendOnly() = assumeTrue("built with firebase.properties", app.repository is FakePollRepository)

    private fun tap(tag: String) {
        compose.waitUntilAtLeastOneExists(hasTestTag(tag), TIMEOUT)
        compose.onAllNodes(hasTestTag(tag)).onFirst().performClick()
    }

    private fun type(tag: String, text: String) {
        compose.waitUntilAtLeastOneExists(hasTestTag(tag), TIMEOUT)
        compose.onNodeWithTag(tag).performTextInput(text)
    }

    private fun hasTestTagStartingWith(prefix: String) = SemanticsMatcher("TestTag starts with $prefix") {
        it.config.getOrNull(SemanticsProperties.TestTag)?.startsWith(prefix) == true
    }

    private fun local(): LocalState = runBlocking { app.store.state.first() }

    @Test
    fun voteOnAPublicPollThenCreateAndCloseAPrivateOne() {
        type("name_input", "Alex")
        tap("start")

        // The first poll of the feed: a vote and a prediction.
        compose.waitUntilAtLeastOneExists(hasTestTagStartingWith("poll_"), TIMEOUT)
        compose.onAllNodes(hasTestTagStartingWith("poll_")).onFirst().performClick()
        tap("vote_red")
        tap("predict_blue")
        compose.waitUntil(TIMEOUT) { local().votes.isNotEmpty() }
        assertEquals(Side.BLUE, local().votes.values.single().prediction)
        tap("back")

        tap("create")
        type("question_input", "Friday dinner?")
        type("red_input", "Pizza")
        type("blue_input", "Sushi")
        tap("submit")
        compose.waitUntil(TIMEOUT) { local().myPolls.isNotEmpty() }
        val code = local().myPolls.first()
        val created = assertNotNull(runBlocking { app.repository.poll(code).first() })
        assertEquals("Alex", created.creatorName)

        tap("vote_blue")
        tap("predict_blue")
        compose.waitUntil(TIMEOUT) { code in local().votes }
        tap("close_poll")
        tap("confirm_close")
        compose.waitUntil(TIMEOUT) { runBlocking { app.repository.poll(code).first() }?.closesAt != null }
    }

    private companion object {
        const val TIMEOUT = 10_000L
    }
}
