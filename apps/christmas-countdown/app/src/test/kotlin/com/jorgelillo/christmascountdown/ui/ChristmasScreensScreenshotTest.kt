package com.jorgelillo.christmascountdown.ui

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.jorgelillo.christmascountdown.domain.AdventCalendar
import com.jorgelillo.christmascountdown.domain.AdventPlan
import com.jorgelillo.christmascountdown.domain.ChristmasCountdown
import com.jorgelillo.christmascountdown.ui.advent.AdventScreen
import com.jorgelillo.christmascountdown.ui.advent.AdventUiState
import com.jorgelillo.christmascountdown.ui.countdown.CountdownScreen
import com.jorgelillo.christmascountdown.ui.countdown.CountdownUiState
import com.jorgelillo.christmascountdown.ui.theme.ChristmasDark
import com.jorgelillo.christmascountdown.ui.theme.ChristmasLight
import com.jorgelillo.core.designsystem.LilloTheme
import com.jorgelillo.core.screenshottesting.captureScreenshot
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Fixed dates, so the countdown and the calendar look the same every day the tests run. */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
// A bare Application: the app's own onCreate (WorkManager, Firebase…) has no place in a screenshot.
@Config(sdk = [36], qualifiers = RobolectricDeviceQualifiers.Pixel7, application = Application::class)
class ChristmasScreensScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    private val october = ZonedDateTime.of(2026, 10, 21, 18, 30, 0, 0, ZoneId.of("Europe/Madrid"))
    private val december18 = LocalDate.of(2026, 12, 18)

    /** Each screen in the dark look under its name, and in the light look with `_light` added. */
    private fun shot(name: String, dark: Boolean, content: @Composable () -> Unit) =
        compose.captureScreenshot(if (dark) name else "${name}_light") {
            LilloTheme(if (dark) ChristmasDark else ChristmasLight) { GradientBackground { content() } }
        }

    private fun countdown(name: String, dark: Boolean = true, sleeps: Boolean = false) = shot(name, dark) {
        CountdownScreen(
            CountdownUiState(ChristmasCountdown.stateAt(october), sleepsMode = sleeps),
            onSleepsModeChange = {}
        )
    }

    private fun advent(name: String, dark: Boolean = true) = shot(name, dark) {
        val state = AdventUiState(
            unlockedDoors = AdventCalendar.unlockedDoors(december18),
            doorOrder = AdventCalendar.doorOrder(2026),
            plan = AdventPlan.forYear(2026),
            openedDoors = (1..14).toSet()
        )
        AdventScreen(state, canOpen = { it <= 18 }, onOpen = {})
    }

    @Test
    fun countdownPortrait() = countdown("countdown")

    @Test
    fun countdownPortraitLight() = countdown("countdown", dark = false)

    @Test
    fun sleepsPortrait() = countdown("sleeps", sleeps = true)

    @Test
    fun sleepsPortraitLight() = countdown("sleeps", dark = false, sleeps = true)

    @Test
    @Config(qualifiers = "+land")
    fun countdownLandscape() = countdown("countdown_landscape")

    @Test
    @Config(qualifiers = "+land")
    fun countdownLandscapeLight() = countdown("countdown_landscape", dark = false)

    // The countdown is the screen people open the app for: it must still read with the largest text.
    @Test
    @Config(fontScale = 2f)
    fun countdownLargestFont() = countdown("countdown_large_font", dark = false)

    @Test
    @Config(qualifiers = "+land", fontScale = 2f)
    fun countdownLandscapeLargestFont() = countdown("countdown_landscape_large_font", dark = false)

    @Test
    fun adventPortrait() = advent("advent")

    @Test
    fun adventPortraitLight() = advent("advent", dark = false)

    @Test
    @Config(qualifiers = "+land")
    fun adventLandscape() = advent("advent_landscape")

    @Test
    @Config(qualifiers = "+land")
    fun adventLandscapeLight() = advent("advent_landscape", dark = false)
}
