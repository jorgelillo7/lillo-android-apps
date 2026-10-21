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
import com.jorgelillo.christmascountdown.ui.theme.ChristmasColorScheme
import com.jorgelillo.christmascountdown.ui.theme.ChristmasColors
import com.jorgelillo.core.designsystem.GradientBackground
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

    private fun shot(name: String, content: @Composable () -> Unit) = compose.captureScreenshot(name) {
        LilloTheme(ChristmasColorScheme) { GradientBackground(ChristmasColors.Background) { content() } }
    }

    private fun countdown(name: String, sleeps: Boolean = false) = shot(name) {
        CountdownScreen(CountdownUiState(ChristmasCountdown.stateAt(october), sleepsMode = sleeps), onSleepsModeChange = {})
    }

    private fun advent(name: String) = shot(name) {
        val state = AdventUiState(
            unlockedDoors = AdventCalendar.unlockedDoors(december18),
            doorOrder = AdventCalendar.doorOrder(2026),
            plan = AdventPlan.forYear(2026),
            openedDoors = (1..14).toSet()
        )
        AdventScreen(state, canOpen = { it <= 18 }, onOpen = {})
    }

    @Test fun countdownPortrait() = countdown("countdown")

    @Test fun sleepsPortrait() = countdown("sleeps", sleeps = true)

    @Test
    @Config(qualifiers = "+land")
    fun countdownLandscape() = countdown("countdown_landscape")

    @Test fun adventPortrait() = advent("advent")

    @Test
    @Config(qualifiers = "+land")
    fun adventLandscape() = advent("advent_landscape")
}
