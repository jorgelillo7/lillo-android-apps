package com.jorgelillo.slipper

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.lifecycle.Lifecycle
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * A run through the real MainActivity, interrupted: leaving the app pauses it until a tap, and
 * turning the phone (the activity recreated) keeps it instead of starting over. The game asks for
 * a frame every frame, so the test moves Compose's clock itself. One test per class: the DataStore
 * file opens once per process.
 */
@OptIn(ExperimentalTestApi::class)
@RunWith(AndroidJUnit4::class)
@Config(sdk = [36], qualifiers = RobolectricDeviceQualifiers.Pixel7)
class RunInterruptionTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    private fun shown(tag: String) = compose.onAllNodes(hasTestTag(tag)).fetchSemanticsNodes().isNotEmpty()

    private fun advance(millis: Long = FRAME_STEP_MILLIS) = compose.mainClock.advanceTimeBy(millis)

    private fun advanceUntilShown(tag: String) {
        compose.waitUntil(TIMEOUT) {
            advance()
            shown(tag)
        }
    }

    private fun score(): Int = compose.onNodeWithTag("score").fetchSemanticsNode()
        .config[SemanticsProperties.Text].joinToString("").filter { it.isDigit() }.toInt()

    @Test
    fun aRunWaitsWhileTheAppIsAwayAndSurvivesARotation() {
        compose.mainClock.autoAdvance = false
        advanceUntilShown("play")
        compose.onNodeWithTag("play").performClick()
        advanceUntilShown("score")
        repeat(10) { advance() }
        assertTrue(score() > 0)

        // Away (a call, another app): the run stops and waits for the player.
        val away = score()
        compose.activityRule.scenario.moveToState(Lifecycle.State.STARTED)
        compose.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        advance()
        assertTrue(shown("paused"))
        repeat(20) { advance() }
        assertEquals(away, score())

        // A tap goes on with the same run.
        compose.onNodeWithTag("game").performClick()
        repeat(10) { advance() }
        assertTrue(!shown("paused"))
        val beforeTurning = score()
        assertTrue(beforeTurning > away)

        // Turned: the same run comes back, paused, not a new one from zero.
        compose.activityRule.scenario.recreate()
        advanceUntilShown("score")
        assertTrue(shown("paused"))
        assertTrue(score() >= beforeTurning)
    }

    private companion object {
        const val TIMEOUT = 60_000L
        const val FRAME_STEP_MILLIS = 100L
    }
}
