package com.jorgelillo.slipper.ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jorgelillo.slipper.R
import com.jorgelillo.slipper.domain.Engine
import com.jorgelillo.slipper.domain.GameConfig
import com.jorgelillo.slipper.domain.Input
import com.jorgelillo.slipper.domain.World
import com.jorgelillo.slipper.ui.formatScore
import com.jorgelillo.slipper.ui.theme.Kitchen
import kotlin.math.min
import kotlinx.coroutines.delay

/** The finger, between frames: a touch is one [tapped] step, [holding] while it stays down. */
private class Finger {
    var tapped = false
    var holding = false

    fun take(): Input = Input(tapped, holding).also { tapped = false }
}

/** How long the caught moment stays on screen before the result. */
private const val CAUGHT_PAUSE_MS = 700L

/** The tap hint disappears after this many seconds. */
private const val HINT_SECONDS = 3.0

/**
 * A run: the engine stepped at its fixed rate on every frame (catching up after slow frames, up to
 * a limit), drawn by [GameScene]. Anywhere on the screen is the jump button.
 */
@Composable
internal fun GameScreen(seed: Long, target: Int?, onCatch: (World) -> Unit) {
    val sprites = rememberGameSprites()
    val finger = remember { Finger() }
    var world by remember(seed) { mutableStateOf(Engine.start(seed)) }
    val caught by rememberUpdatedState(onCatch)

    LaunchedEffect(seed) {
        var last = 0L
        var pending = 0.0
        while (!world.over) {
            withFrameNanos { now ->
                if (last != 0L) pending += min(0.1, (now - last) / 1e9)
                last = now
                var w = world
                while (pending >= GameConfig.STEP && !w.over) {
                    w = Engine.step(w, finger.take())
                    pending -= GameConfig.STEP
                }
                world = w
            }
        }
        delay(CAUGHT_PAUSE_MS)
        caught(world)
    }

    Box(
        Modifier
            .fillMaxSize()
            .testTag("game")
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown()
                    finger.tapped = true
                    finger.holding = true
                    do {
                        val event = awaitPointerEvent()
                    } while (event.changes.any { it.pressed })
                    finger.holding = false
                }
            }
    ) {
        GameScene(world, sprites, Modifier.fillMaxSize())
        Hud(world, target, sprites)
    }
}

@Composable
private fun Hud(world: World, target: Int?, sprites: GameSprites) {
    Column(Modifier.fillMaxSize().statusBarsPadding().padding(20.dp)) {
        Row(
            Modifier.fillMaxWidth().panel(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                formatScore(world.score),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Black,
                color = Kitchen.Ink,
                modifier = Modifier.weight(1f).testTag("score")
            )
            Text(
                pluralStringResource(R.plurals.croquetas_count, world.eaten, world.eaten),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Kitchen.Muted
            )
        }
        ChaseMeter(
            world.gap,
            sprites,
            stringResource(R.string.chase_description),
            Modifier.padding(top = 8.dp).panel()
        )
        if (target != null) {
            Text(
                stringResource(R.string.challenge_banner, formatScore(target)),
                color = Kitchen.Shout,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp).panel()
            )
        }
        if (world.over) {
            Text(
                stringResource(R.string.caught),
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Black,
                color = Kitchen.Shout,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 32.dp).fillMaxWidth().panel()
            )
        } else if (world.time < HINT_SECONDS) {
            Text(
                stringResource(R.string.tap_to_jump),
                color = Kitchen.Muted,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 32.dp).fillMaxWidth().panel()
            )
        }
    }
}

/** HUD text sits on a cream panel: in landscape the tiled wall reaches the top of the screen. */
private fun Modifier.panel() = background(Kitchen.Raised.copy(alpha = 0.92f), RoundedCornerShape(20.dp))
    .padding(horizontal = 16.dp, vertical = 6.dp)
