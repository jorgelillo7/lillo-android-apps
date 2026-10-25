package com.jorgelillo.slipper

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.jorgelillo.slipper.data.Saved
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * A whole run through the real MainActivity on the JVM: play, get caught (no taps: the kid runs
 * into furniture and slippers until the grandma closes the gap), the result, and the next run. The game asks for a frame every frame, so
 * the test moves Compose's clock itself. One test per class: the DataStore file opens once per process.
 */
@OptIn(ExperimentalTestApi::class)
@RunWith(AndroidJUnit4::class)
@Config(sdk = [36], qualifiers = RobolectricDeviceQualifiers.Pixel7)
class SlipperFlowTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    private fun saved(): Saved = runBlocking {
        (compose.activity.application as SlipperApplication).store.state.first()
    }

    private fun shown(tag: String) = compose.onAllNodes(hasTestTag(tag)).fetchSemanticsNodes().isNotEmpty()

    /** Moves the frozen clock until [tag] is on screen (the run plays meanwhile). */
    private fun advanceUntilShown(tag: String) {
        compose.waitUntil(TIMEOUT) {
            compose.mainClock.advanceTimeBy(FRAME_STEP_MILLIS)
            shown(tag)
        }
    }

    private fun tap(tag: String) {
        advanceUntilShown(tag)
        compose.onNodeWithTag(tag).performClick()
        compose.mainClock.advanceTimeBy(FRAME_STEP_MILLIS)
    }

    @Test
    fun aRunEndsCaughtAndTheNextOneStarts() {
        compose.mainClock.autoAdvance = false
        tap("play")
        advanceUntilShown("final_score")
        compose.waitUntil(TIMEOUT) {
            compose.mainClock.advanceTimeBy(FRAME_STEP_MILLIS)
            saved().runs == 1
        }
        assertTrue(saved().best > 0)

        tap("retry")
        // The previous result fades out first: wait for it to go, then for the next one.
        compose.waitUntil(TIMEOUT) {
            compose.mainClock.advanceTimeBy(FRAME_STEP_MILLIS)
            !shown("final_score")
        }
        advanceUntilShown("final_score")
        compose.waitUntil(TIMEOUT) {
            compose.mainClock.advanceTimeBy(FRAME_STEP_MILLIS)
            saved().runs == 2
        }

        // The run of the day, from home: its best is kept for today.
        tap("home")
        // Let the result fade out first: a tap during the transition goes nowhere.
        compose.mainClock.advanceTimeBy(1_000)
        tap("daily")
        compose.waitUntil(TIMEOUT) {
            compose.mainClock.advanceTimeBy(FRAME_STEP_MILLIS)
            saved().runs == 3
        }
        assertTrue(saved().dailyBest(java.time.LocalDate.now().toEpochDay()) > 0)
    }

    private companion object {
        const val TIMEOUT = 60_000L
        const val FRAME_STEP_MILLIS = 100L
    }
}
