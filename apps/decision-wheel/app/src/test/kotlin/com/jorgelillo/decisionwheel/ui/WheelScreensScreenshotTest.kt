package com.jorgelillo.decisionwheel.ui

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.jorgelillo.core.designsystem.LilloTheme
import com.jorgelillo.core.screenshottesting.captureScreenshot
import com.jorgelillo.decisionwheel.domain.AppState
import com.jorgelillo.decisionwheel.domain.Decision
import com.jorgelillo.decisionwheel.domain.Wheel
import com.jorgelillo.decisionwheel.ui.home.HomeScreen
import com.jorgelillo.decisionwheel.ui.theme.WheelDark
import com.jorgelillo.decisionwheel.ui.theme.WheelLight
import com.jorgelillo.decisionwheel.ui.wheel.WheelScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
// A bare Application: the app's own onCreate (WorkManager, Firebase…) has no place in a screenshot.
@Config(sdk = [36], qualifiers = RobolectricDeviceQualifiers.Pixel7, application = Application::class)
class WheelScreensScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    private val food =
        Wheel(
            "food",
            "Where should we eat?",
            listOf("Pizza", "Sushi", "Tacos", "Burger", "Thai", "Indian"),
            favorite = true
        )
    private val state = AppState(
        wheels = listOf(food, Wheel("plan", "Friday plan", listOf("Cinema", "Board games", "Bowling"))),
        history = listOf(
            Decision("food", "Pizza", 1_759_000_000_000),
            Decision("plan", "Bowling", 1_759_500_000_000),
            Decision("food", "Pizza", 1_759_800_000_000),
            Decision("food", "Sushi", 1_760_000_000_000)
        )
    )

    /** Each screen in the dark look under its name, and in the light look with `_light` added. */
    private fun shot(name: String, dark: Boolean, content: @Composable () -> Unit) =
        compose.captureScreenshot(if (dark) name else "${name}_light") {
            LilloTheme(if (dark) WheelDark else WheelLight) { content() }
        }

    private fun home(name: String, dark: Boolean = true) =
        shot(name, dark) { HomeScreen(state, {}, {}, {}, {}, {}, {}, {}) }

    /** No decision yet: the card under the wheels offers a first spin on the favourite. */
    private fun firstSpin(name: String, dark: Boolean = true) =
        shot(name, dark) { HomeScreen(state.copy(history = emptyList()), {}, {}, {}, {}, {}, {}, {}) }

    private fun wheel(name: String, dark: Boolean = true) =
        shot(name, dark) { WheelScreen(food, state, {}, {}, {}, {}, {}, {}) }

    @Test fun homePortrait() = home("home")

    @Test fun homePortraitLight() = home("home", dark = false)

    @Test
    @Config(qualifiers = "+land")
    fun homeLandscape() = home("home_landscape")

    @Test
    @Config(qualifiers = "+land")
    fun homeLandscapeLight() = home("home_landscape", dark = false)

    @Test
    fun homeFirstSpin() = firstSpin("home_first_spin")

    @Test
    fun homeFirstSpinLight() = firstSpin("home_first_spin", dark = false)

    // With the largest text the card needs more room: it shows only where it still fits.
    @Test
    @Config(fontScale = 2f)
    fun homeLargestFont() = home("home_large_font", dark = false)

    /** Wheels down to the bottom of the screen leave no room: the last result is a line under the title. */
    @Test
    fun theLastResultNeedsRoom() {
        val many = (1..8).map { Wheel("w$it", "Wheel $it", listOf("A", "B")) }
        compose.setContent {
            LilloTheme(WheelLight) { HomeScreen(state.copy(wheels = many + state.wheels), {}, {}, {}, {}, {}, {}, {}) }
        }
        compose.onNodeWithTag("last_result").assertDoesNotExist()
        compose.onNodeWithTag("hero_last").assertExists()
    }

    /** With room for the card, the last result is said once: no line under the title. */
    @Test
    fun theLastResultIsSaidOnce() {
        compose.setContent { LilloTheme(WheelLight) { HomeScreen(state, {}, {}, {}, {}, {}, {}, {}) } }
        compose.onNodeWithTag("last_result").assertExists()
        compose.onNodeWithTag("hero_last").assertDoesNotExist()
    }

    @Test fun wheelPortrait() = wheel("wheel")

    @Test fun wheelPortraitLight() = wheel("wheel", dark = false)

    @Test
    @Config(qualifiers = "+land")
    fun wheelLandscape() = wheel("wheel_landscape")

    @Test
    @Config(qualifiers = "+land")
    fun wheelLandscapeLight() = wheel("wheel_landscape", dark = false)

    // The wheel is the screen people come for: it must still fit with the largest text.
    @Test
    @Config(fontScale = 2f)
    fun wheelLargestFont() = wheel("wheel_large_font", dark = false)

    @Test
    @Config(qualifiers = "+land", fontScale = 2f)
    fun wheelLandscapeLargestFont() = wheel("wheel_landscape_large_font", dark = false)
}
