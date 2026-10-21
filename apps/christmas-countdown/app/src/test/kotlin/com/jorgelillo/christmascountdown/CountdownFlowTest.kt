package com.jorgelillo.christmascountdown

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * The app's journeys through the real MainActivity on the JVM, like tools/smoke_test.sh on an
 * emulator but in seconds: time and sleeps modes (saved), the advent calendar, the about sheet,
 * carols on and off (saved). Taps by on-screen text, as the smoke test does. One test per class:
 * the app's DataStore file can only be open once per process.
 */
@OptIn(ExperimentalTestApi::class)
@RunWith(AndroidJUnit4::class)
@Config(
    sdk = [36],
    qualifiers = RobolectricDeviceQualifiers.Pixel7,
    application = TestChristmasCountdownApplication::class
)
class CountdownFlowTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    private val settings get() = (compose.activity.application as ChristmasCountdownApplication).settings

    private fun tap(text: Int) {
        val label = compose.activity.getString(text)
        // A label or an icon's description, like the smoke test's taps.
        val node = hasText(label) or hasContentDescription(label)
        // The frozen clock moves while waiting, so saved changes reach the screen.
        compose.waitUntil(TIMEOUT) {
            compose.mainClock.advanceTimeBy(FRAME_STEP_MILLIS)
            compose.onAllNodes(node).fetchSemanticsNodes().isNotEmpty()
        }
        compose.onAllNodes(node).onFirst().performClick()
        compose.mainClock.advanceTimeBy(FRAME_STEP_MILLIS) // the tap's recomposition and transitions
    }

    private fun waitFor(saved: suspend () -> Boolean) = compose.waitUntil(TIMEOUT) { runBlocking { saved() } }

    @Test
    fun modesAdventAboutAndCarols() {
        // Snow and the ticking clock never let the screen go idle: time moves only when a step says so.
        compose.mainClock.autoAdvance = false
        compose.mainClock.advanceTimeBy(FRAME_STEP_MILLIS)

        tap(R.string.mode_sleeps)
        waitFor { settings.sleepsMode.first() }
        tap(R.string.mode_time)
        waitFor { !settings.sleepsMode.first() }

        tap(R.string.tab_advent)
        tap(R.string.action_about)
        compose.runOnUiThread { compose.activity.onBackPressedDispatcher.onBackPressed() }
        tap(R.string.tab_countdown)

        tap(R.string.action_music_on)
        waitFor { settings.musicEnabled.first() }
        tap(R.string.action_music_off)
        waitFor { !settings.musicEnabled.first() }
    }

    private companion object {
        const val TIMEOUT = 10_000L
        const val FRAME_STEP_MILLIS = 1_000L
    }
}
