package com.jorgelillo.grouppolls.ui

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.jorgelillo.core.designsystem.LilloTheme
import com.jorgelillo.core.screenshottesting.captureScreenshot
import com.jorgelillo.grouppolls.data.FakePollRepository
import com.jorgelillo.grouppolls.data.LocalState
import com.jorgelillo.grouppolls.ui.screens.HomeScreen
import com.jorgelillo.grouppolls.ui.screens.PollScreen
import com.jorgelillo.grouppolls.ui.screens.SettingsScreen
import com.jorgelillo.grouppolls.ui.screens.WelcomeScreen
import com.jorgelillo.grouppolls.ui.theme.PollsColorScheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** The in-memory backend's sample polls, at a fixed time so "2 h ago" / "closes in…" never move. */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
// A bare Application: the app's own onCreate (WorkManager, Firebase…) has no place in a screenshot.
@Config(sdk = [36], qualifiers = RobolectricDeviceQualifiers.Pixel7, application = Application::class)
class PollScreensScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    private val now = 1_760_000_000_000L
    private val repository = FakePollRepository { now }
    private val feed = runBlocking { repository.feed().first() }.groupBy { it.language }.toList().sortedBy { it.first != "en" }
    private val private = runBlocking { repository.poll(FakePollRepository.SAMPLE_PRIVATE).first()!! }
    private val votes = runBlocking { repository.votes(FakePollRepository.SAMPLE_PRIVATE).first() }
    private val local = LocalState(name = "Alex", acceptedTerms = true)

    private fun shot(name: String, content: @Composable () -> Unit) = compose.captureScreenshot(name) { LilloTheme(PollsColorScheme) { content() } }

    private fun home(name: String) = shot(name) {
        HomeScreen(feed, local, pollFlow = { code -> flowOf(feed.flatMap { it.second }.firstOrNull { it.code == code }) }, {}, {}, {}, clock = { now })
    }

    private fun poll(name: String) = shot(name) {
        PollScreen(private, votes, local, "local-user", shareOnOpen = false, { _, _, _ -> }, {}, { _, _ -> }, {}, {}, clock = { now })
    }

    @Test fun welcomePortrait() = shot("welcome") { WelcomeScreen {} }

    @Test fun homePortrait() = home("home")

    @Test
    @Config(qualifiers = "+land")
    fun homeLandscape() = home("home_landscape")

    @Test fun pollPortrait() = poll("poll")

    @Test
    @Config(qualifiers = "+land")
    fun pollLandscape() = poll("poll_landscape")

    @Test fun settingsPortrait() = shot("settings") { SettingsScreen(local, {}, {}, {}) }
}
