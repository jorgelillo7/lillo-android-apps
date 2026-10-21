package com.jorgelillo.decisionwheel

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.jorgelillo.decisionwheel.domain.AppState
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * The app's main journeys through the real MainActivity on the JVM, like tools/smoke_test.sh on an
 * emulator but in seconds: spin a preset wheel and accept the result, then create a wheel. One test
 * per class: the app's DataStore file can only be open once per process.
 */
@OptIn(ExperimentalTestApi::class)
@RunWith(AndroidJUnit4::class)
@Config(sdk = [36], qualifiers = RobolectricDeviceQualifiers.Pixel7)
class WheelFlowTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    private fun tap(tag: String) {
        compose.waitUntilExactlyOneExists(hasTestTag(tag), TIMEOUT)
        compose.onNodeWithTag(tag).performClick()
    }

    private fun saved(): AppState =
        runBlocking { (compose.activity.application as DecisionWheelApplication).repository.state.first() }

    @Test
    fun spinAcceptAndCreateAWheel() {
        val eatOut = compose.activity.getString(R.string.preset_eat_out)
        compose.waitUntilExactlyOneExists(hasText(eatOut), TIMEOUT)
        compose.onNodeWithText(eatOut).performClick()

        // The spin takes a few seconds of animation; the result card offers to accept it.
        tap("spin")
        tap("accept")
        compose.waitUntil(TIMEOUT) { saved().history.isNotEmpty() }
        val decision = saved().history.single()
        assertEquals("preset-eat-out", decision.wheelId)
        assert(decision.option in saved().wheel("preset-eat-out")!!.options)

        tap("back")
        tap("new_wheel")
        compose.onNodeWithTag("name").performTextInput("Smoke")
        listOf("One", "Two", "Three").forEach { option ->
            compose.onNodeWithTag("new_option").performTextInput(option)
            compose.onNodeWithTag("new_option").performImeAction()
        }
        tap("save")
        // Saving a new wheel opens it, ready to spin.
        compose.waitUntilExactlyOneExists(hasTestTag("spin"), TIMEOUT)
        val created = saved().wheels.single { it.name == "Smoke" }
        assertEquals(listOf("One", "Two", "Three"), created.options)
    }

    private companion object {
        const val TIMEOUT = 10_000L
    }
}
