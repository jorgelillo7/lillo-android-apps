package com.jorgelillo.stadiumevents.ui

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.jorgelillo.core.designsystem.LilloTheme
import com.jorgelillo.core.screenshottesting.captureScreenshot
import com.jorgelillo.stadiumevents.SampleFeed
import com.jorgelillo.stadiumevents.domain.AppState
import com.jorgelillo.stadiumevents.ui.theme.DarkPalette
import com.jorgelillo.stadiumevents.ui.theme.JaleoDarkScheme
import com.jorgelillo.stadiumevents.ui.theme.JaleoLightScheme
import com.jorgelillo.stadiumevents.ui.theme.JaleoPalette
import com.jorgelillo.stadiumevents.ui.theme.LightPalette
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Fixed dates and time zone, so the screen looks the same every day the tests run, on any machine. */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
// A bare Application: the app's own onCreate (WorkManager, downloads) has no place in a screenshot.
@Config(sdk = [36], qualifiers = RobolectricDeviceQualifiers.Pixel7, application = Application::class)
class JaleoScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    private val madrid = ZoneId.of("Europe/Madrid")
    private val today = LocalDate.of(2026, 11, 24)
    private val checked = ZonedDateTime.of(2026, 11, 24, 8, 5, 0, 0, madrid).toInstant().toEpochMilli()

    private fun state(day: LocalDate = today, fetched: Boolean = true, offline: Boolean = false) = JaleoUiState.from(
        AppState(SampleFeed.feed(today), fetchedAt = if (fetched) checked else null),
        day,
        offline = offline
    ).copy(zone = madrid)

    private fun shot(name: String, state: JaleoUiState, dark: Boolean = false) {
        val palette: JaleoPalette = if (dark) DarkPalette else LightPalette
        compose.captureScreenshot(name) {
            LilloTheme(if (dark) JaleoDarkScheme else JaleoLightScheme) {
                JaleoScreen(state, palette, onAbout = {}, onRetry = {})
            }
        }
    }

    @Test fun busyTomorrow() = shot("busy", state())

    @Test
    @Config(qualifiers = "+land")
    fun busyTomorrowLandscape() = shot("busy_landscape", state())

    @Test fun quietTomorrow() = shot("calm", state(day = today.plusDays(3)))

    @Test
    @Config(qualifiers = "+land")
    fun quietTomorrowLandscape() = shot("calm_landscape", state(day = today.plusDays(3)))

    @Test fun maybeTomorrow() = shot("maybe", state(day = today.plusDays(10)))

    @Test fun biggestDay() = shot("final", state(day = today.plusDays(192)))

    @Test fun firstOpenOffline() = shot("offline", state(fetched = false, offline = true))

    @Test fun darkMode() = shot("busy_dark", state(), dark = true)

    @Test
    @Config(qualifiers = "+es")
    fun busyTomorrowInSpanish() = shot("busy_es", state())
}
