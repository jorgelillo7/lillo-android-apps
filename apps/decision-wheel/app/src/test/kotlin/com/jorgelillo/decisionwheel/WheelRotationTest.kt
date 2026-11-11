package com.jorgelillo.decisionwheel

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.jorgelillo.decisionwheel.domain.AppState
import com.jorgelillo.decisionwheel.domain.DecisionEngine
import com.jorgelillo.decisionwheel.ui.wheel.WheelDegrees
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * Turning the phone recreates the activity: a spin in flight still lands and shows what the pointer
 * shows, and a finished spin keeps its angle. One test per class: the app's DataStore file can only
 * be open once per process.
 */
@OptIn(ExperimentalTestApi::class)
@RunWith(AndroidJUnit4::class)
@Config(sdk = [36], qualifiers = RobolectricDeviceQualifiers.Pixel7)
class WheelRotationTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    private fun saved(): AppState =
        runBlocking { (compose.activity.application as DecisionWheelApplication).repository.state.first() }

    private fun wheelDegrees(): Float =
        compose.onNode(SemanticsMatcher.keyIsDefined(WheelDegrees)).fetchSemanticsNode().config[WheelDegrees]

    private fun recreate() {
        compose.activityRule.scenario.recreate()
        compose.waitForIdle()
    }

    @Test
    fun aSpinSurvivesTheActivityBeingRecreated() {
        val eatOut = compose.activity.getString(R.string.preset_eat_out)
        compose.waitUntilExactlyOneExists(hasText(eatOut), TIMEOUT)
        compose.onNodeWithText(eatOut).performClick()
        compose.waitUntilExactlyOneExists(hasTestTag("spin"), TIMEOUT)

        // Turned mid-spin: the spin lands where it was going and its result is the option under the pointer.
        compose.mainClock.autoAdvance = false
        compose.onNodeWithTag("spin").performClick()
        compose.mainClock.advanceTimeBy(400)
        compose.onNodeWithTag("spin").assertIsNotEnabled()
        compose.mainClock.autoAdvance = true
        recreate()
        compose.waitUntilExactlyOneExists(hasTestTag("accept"), TIMEOUT)
        val options = saved().wheel("preset-eat-out")!!.options
        val weights = DecisionEngine.weights(options, emptySet(), emptyList(), 0L, avoidRepeats = true)
        val underPointer = DecisionEngine.segmentAtPointer(
            DecisionEngine.segments(weights),
            wheelDegrees().toDouble()
        )!!.option
        compose.onNodeWithTag("accept").performClick()
        compose.waitUntil(TIMEOUT) { saved().history.isNotEmpty() }
        assertEquals(underPointer, saved().history.single().option)

        // Turned after a spin: the wheel stays where it landed, under the result it shows.
        compose.onNodeWithTag("spin").performClick()
        compose.waitUntilExactlyOneExists(hasTestTag("accept"), TIMEOUT)
        val landed = wheelDegrees()
        recreate()
        compose.waitUntilExactlyOneExists(hasTestTag("accept"), TIMEOUT)
        assertEquals(landed, wheelDegrees())
    }

    private companion object {
        const val TIMEOUT = 10_000L
    }
}
