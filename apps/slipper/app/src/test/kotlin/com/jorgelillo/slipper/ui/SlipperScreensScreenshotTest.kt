package com.jorgelillo.slipper.ui

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.jorgelillo.core.designsystem.LilloTheme
import com.jorgelillo.core.screenshottesting.captureScreenshot
import com.jorgelillo.slipper.ui.game.GameScreen
import com.jorgelillo.slipper.ui.home.HomeScreen
import com.jorgelillo.slipper.ui.over.GameOverScreen
import com.jorgelillo.slipper.ui.theme.KitchenColorScheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** The game's screens, upright and on a short landscape screen; the pixel art is part of the picture. */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
// A bare Application: the app's own singletons have no place in a screenshot.
@Config(sdk = [36], qualifiers = RobolectricDeviceQualifiers.Pixel7, application = Application::class)
class SlipperScreensScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    private fun shot(name: String, content: @Composable () -> Unit) = compose.captureScreenshot(name) {
        LilloTheme(KitchenColorScheme) { content() }
    }

    private fun home(name: String) = shot(name) { HomeScreen(best = 1240, challenge = 980, {}, {}, {}) }

    /** The first frame of a run: the kitchen, the HUD and the tap hint. */
    private fun run(name: String) = shot(name) { GameScreen(seed = 1, target = null) {} }

    private fun record(name: String) = shot(name) {
        GameOverScreen(1310, previousBest = 1240, challenge = null, {}, {}, {})
    }

    private fun challengeWon(name: String) = shot(name) {
        GameOverScreen(1310, previousBest = 2000, challenge = 980, {}, {}, {})
    }

    @Test fun homePortrait() = home("home")

    @Test
    @Config(qualifiers = "+land")
    fun homeLandscape() = home("home_land")

    @Test fun runPortrait() = run("run")

    @Test
    @Config(qualifiers = "+land")
    fun runLandscape() = run("run_land")

    @Test fun newRecord() = record("result_record")

    @Test
    @Config(qualifiers = "+land")
    fun newRecordLandscape() = record("result_record_land")

    @Test fun challengeBeaten() = challengeWon("result_challenge_won")
}
