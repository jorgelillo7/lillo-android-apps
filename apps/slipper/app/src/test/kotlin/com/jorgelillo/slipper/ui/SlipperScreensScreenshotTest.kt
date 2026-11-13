package com.jorgelillo.slipper.ui

import android.app.Application
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.jorgelillo.core.designsystem.LilloTheme
import com.jorgelillo.core.screenshottesting.captureScreenshot
import com.jorgelillo.slipper.data.Saved
import com.jorgelillo.slipper.domain.Cat
import com.jorgelillo.slipper.domain.Chunk
import com.jorgelillo.slipper.domain.Croqueta
import com.jorgelillo.slipper.domain.EventKind
import com.jorgelillo.slipper.domain.Furniture
import com.jorgelillo.slipper.domain.FurnitureKind
import com.jorgelillo.slipper.domain.GameEvent
import com.jorgelillo.slipper.domain.Kid
import com.jorgelillo.slipper.domain.PowerKind
import com.jorgelillo.slipper.domain.PowerUp
import com.jorgelillo.slipper.domain.Room
import com.jorgelillo.slipper.domain.Slipper
import com.jorgelillo.slipper.domain.SlipperKind
import com.jorgelillo.slipper.domain.SlipperSkin
import com.jorgelillo.slipper.domain.World
import com.jorgelillo.slipper.ui.game.GameScene
import com.jorgelillo.slipper.ui.game.GameScreen
import com.jorgelillo.slipper.ui.game.Hud
import com.jorgelillo.slipper.ui.game.rememberGameSprites
import com.jorgelillo.slipper.ui.game.sceneLabels
import com.jorgelillo.slipper.ui.home.HomeScreen
import com.jorgelillo.slipper.ui.over.GameOverScreen
import com.jorgelillo.slipper.ui.over.RunResult
import com.jorgelillo.slipper.ui.theme.KitchenDark
import com.jorgelillo.slipper.ui.theme.KitchenLight
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

    /** Each screen in the night look under its name, and in the day look with `_light` added. */
    private fun shot(name: String, dark: Boolean, content: @Composable () -> Unit) =
        compose.captureScreenshot(if (dark) name else "${name}_light") {
            LilloTheme(if (dark) KitchenDark else KitchenLight) { content() }
        }

    private val saved =
        Saved(best = 1240, croquetas = 210, dailyDay = 20_000, dailyBest = 860, skin = SlipperSkin.CHECKED)

    private fun home(name: String, dark: Boolean = true) = shot(name, dark) {
        HomeScreen(saved, today = 20_000, challenge = 980, {}, {}, {}, {}, {}, {}, challengeSameRun = false)
    }

    /** The first frame of a run: the kitchen, the HUD and the tap hint. */
    private fun run(name: String, dark: Boolean = true) = shot(name, dark) {
        GameScreen(seed = 1, target = null, daily = false, skin = SlipperSkin.CLASSIC, muted = true, sounds = null) {}
    }

    /**
     * The middle of a run, everything at once: grandma close and winding up, a slipper flying, the
     * kid in the air over a near miss, a croqueta combo popping, a power-up and the cat ahead.
     */
    private val action = World(
        seed = 0,
        time = 20.0,
        distance = 200.0,
        kid = Kid(y = 6.0, vy = 20.0, airTime = 0.2),
        gap = 12.0,
        // Chunks as long as the house makes them: each brings its wall decoration.
        chunks = (0 until 6).map { Chunk(it, 100.0 + it * 50, 150.0 + it * 50, Room.KITCHEN) },
        furniture = listOf(Furniture(FurnitureKind.CHAIR, 228.0), Furniture(FurnitureKind.TOY, 244.0)),
        croquetas = listOf(Croqueta(214.0, 14.0), Croqueta(220.0, 14.0)),
        cats = listOf(Cat(250.0)),
        powerUps = listOf(PowerUp(PowerKind.SKATES, 236.0, 9.0)),
        slippers = listOf(Slipper(203.0, SlipperKind.LOW, 80.0, spin = 0.2, passed = true)),
        eaten = 7,
        bonus = 95,
        combo = 3,
        lastCroquetaAt = 19.7,
        nextThrow = 20.2,
        nextKind = SlipperKind.HIGH,
        events = listOf(
            GameEvent(19.7, EventKind.CROQUETA, 208.0, 12.0, 30),
            GameEvent(19.85, EventKind.NEAR_MISS, 203.0, 0.5, 5)
        )
    )

    private fun action(name: String, dark: Boolean = true) = shot(name, dark) {
        val sprites = rememberGameSprites()
        Box(Modifier.fillMaxSize()) {
            GameScene(action, sprites, Modifier.fillMaxSize(), skin = SlipperSkin.CLASSIC, labels = sceneLabels())
            Hud(action, target = null, daily = true, sprites = sprites)
        }
    }

    /** She is off screen, about to throw a high one: the warning at the left edge. */
    private fun warning(name: String, dark: Boolean = true) = shot(name, dark) {
        val sprites = rememberGameSprites()
        val w = action.copy(gap = 26.0, events = emptyList(), slippers = emptyList(), combo = 0)
        Box(Modifier.fillMaxSize()) {
            GameScene(w, sprites, Modifier.fillMaxSize(), labels = sceneLabels())
            Hud(w, target = 1500, daily = false, sprites = sprites)
        }
    }

    private fun result(skin: SlipperSkin?, previousBest: Int, daily: Boolean) = RunResult(
        score = 1310,
        previousBest = previousBest,
        croquetas = 23,
        nearMisses = 4,
        daily = daily,
        unlocked = skin,
        croquetasToNext = 167
    )

    private fun record(name: String, dark: Boolean = true) = shot(name, dark) {
        GameOverScreen(
            result(
                SlipperSkin.CHECKED,
                previousBest = 1240,
                daily = false
            ),
            SlipperSkin.CLASSIC,
            challenge = null,
            {
            },
            {},
            {}
        )
    }

    /** A run that unlocked a slipper still not worn: "Wear it now" and where to change it. */
    @Test fun unlockedNotWornLight() = unlocked("over_unlocked", dark = false)

    @Test fun unlockedNotWorn() = unlocked("over_unlocked")

    private fun unlocked(name: String, dark: Boolean = true) = shot(name, dark) {
        GameOverScreen(result(SlipperSkin.CHECKED, previousBest = 2000, daily = false), SlipperSkin.CLASSIC, null, {
        }, {}, {})
    }

    private fun challengeWon(name: String, dark: Boolean = true) = shot(name, dark) {
        GameOverScreen(result(null, previousBest = 2000, daily = false), SlipperSkin.BUNNY, challenge = 980, {}, {}, {})
    }

    @Test fun actionPortrait() = action("action")

    @Test fun actionPortraitLight() = action("action", dark = false)

    @Test
    @Config(qualifiers = "+land")
    fun actionLandscape() = action("action_land")

    @Test
    @Config(qualifiers = "+land")
    fun actionLandscapeLight() = action("action_land", dark = false)

    @Test fun warningPortrait() = warning("warning")

    @Test fun warningPortraitLight() = warning("warning", dark = false)

    @Test
    @Config(qualifiers = "+land")
    fun warningLandscape() = warning("warning_land")

    @Test
    @Config(qualifiers = "+land")
    fun warningLandscapeLight() = warning("warning_land", dark = false)

    @Test fun homePortrait() = home("home")

    @Test fun homePortraitLight() = home("home", dark = false)

    @Test
    @Config(qualifiers = "+land")
    fun homeLandscape() = home("home_land")

    @Test
    @Config(qualifiers = "+land")
    fun homeLandscapeLight() = home("home_land", dark = false)

    @Test fun runPortrait() = run("run")

    @Test fun runPortraitLight() = run("run", dark = false)

    @Test
    @Config(qualifiers = "+land")
    fun runLandscape() = run("run_land")

    @Test
    @Config(qualifiers = "+land")
    fun runLandscapeLight() = run("run_land", dark = false)

    @Test fun newRecord() = record("result_record")

    @Test fun newRecordLight() = record("result_record", dark = false)

    @Test
    @Config(qualifiers = "+land")
    fun newRecordLandscape() = record("result_record_land")

    @Test
    @Config(qualifiers = "+land")
    fun newRecordLandscapeLight() = record("result_record_land", dark = false)

    @Test fun challengeBeaten() = challengeWon("result_challenge_won")

    @Test fun challengeBeatenLight() = challengeWon("result_challenge_won", dark = false)

    @Test
    @Config(qualifiers = "+land")
    fun challengeBeatenLandscape() = challengeWon("result_challenge_won_land")

    @Test
    @Config(qualifiers = "+land")
    fun challengeBeatenLandscapeLight() = challengeWon("result_challenge_won_land", dark = false)
}
