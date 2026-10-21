package com.jorgelillo.decisionwheel.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.jorgelillo.core.designsystem.LilloTheme
import com.jorgelillo.core.screenshottesting.captureScreenshot
import com.jorgelillo.decisionwheel.domain.AppState
import com.jorgelillo.decisionwheel.domain.Decision
import com.jorgelillo.decisionwheel.domain.Wheel
import com.jorgelillo.decisionwheel.ui.home.HomeScreen
import com.jorgelillo.decisionwheel.ui.theme.WheelColorScheme
import com.jorgelillo.decisionwheel.ui.wheel.WheelScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = RobolectricDeviceQualifiers.Pixel7)
class WheelScreensScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    private val food = Wheel("food", "Where should we eat?", listOf("Pizza", "Sushi", "Tacos", "Burger", "Thai", "Indian"), favorite = true)
    private val state = AppState(
        wheels = listOf(food, Wheel("plan", "Friday plan", listOf("Cinema", "Board games", "Bowling"))),
        history = listOf(Decision("food", "Sushi", 1_760_000_000_000))
    )

    private fun shot(name: String, content: @Composable () -> Unit) = compose.captureScreenshot(name) { LilloTheme(WheelColorScheme) { content() } }

    private fun home(name: String) = shot(name) { HomeScreen(state, {}, {}, {}, {}, {}, {}, {}) }

    private fun wheel(name: String) = shot(name) { WheelScreen(food, state, {}, {}, {}, {}, {}, {}) }

    @Test fun homePortrait() = home("home")

    @Test
    @Config(qualifiers = "+land")
    fun homeLandscape() = home("home_landscape")

    @Test fun wheelPortrait() = wheel("wheel")

    @Test
    @Config(qualifiers = "+land")
    fun wheelLandscape() = wheel("wheel_landscape")
}
