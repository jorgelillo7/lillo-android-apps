package com.jorgelillo.slipper.ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jorgelillo.core.designsystem.isShortScreen
import com.jorgelillo.slipper.R
import com.jorgelillo.slipper.domain.Engine
import com.jorgelillo.slipper.domain.EventKind
import com.jorgelillo.slipper.domain.GameConfig
import com.jorgelillo.slipper.domain.Input
import com.jorgelillo.slipper.domain.SlipperSkin
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
private const val CAUGHT_PAUSE_MS = 900L

/** A hit freezes the action this long: the moment reads, and it feels like a hit. */
private const val HIT_PAUSE_NANOS = 70_000_000L

/** The tap hint disappears after this many seconds. */
private const val HINT_SECONDS = 3.0

/** The words that pop up over the action, in the phone's language. */
@Composable
internal fun sceneLabels() = SceneLabels(
    nearMiss = stringResource(R.string.pop_near_miss),
    ouch = stringResource(R.string.pop_ouch),
    blocked = stringResource(R.string.pop_blocked),
    sandwich = stringResource(R.string.pop_sandwich),
    skates = stringResource(R.string.pop_skates),
    crash = stringResource(R.string.pop_crash)
)

/**
 * A run: the engine stepped at its fixed rate on every frame (catching up after slow frames, up to
 * a limit), drawn by [GameScene]. Anywhere on the screen is the jump button. New events play their
 * sound, vibrate on hits and freeze the action for a moment.
 */
@Composable
internal fun GameScreen(
    seed: Long,
    target: Int?,
    daily: Boolean,
    skin: SlipperSkin,
    muted: Boolean,
    /** Loaded once for the whole app ([rememberSounds]): a pool made per run would miss its first sounds. */
    sounds: Sounds?,
    onCatch: (World) -> Unit
) {
    val sprites = rememberGameSprites()
    val haptics = LocalHapticFeedback.current
    val finger = remember { Finger() }
    var world by remember(seed) { mutableStateOf(Engine.start(seed)) }
    val caught by rememberUpdatedState(onCatch)
    val quiet by rememberUpdatedState(muted)

    LaunchedEffect(seed) {
        var last = 0L
        var pending = 0.0
        var frozenUntil = 0L
        while (!world.over) {
            withFrameNanos { now ->
                if (last != 0L && now >= frozenUntil) pending += min(0.1, (now - last) / 1e9)
                last = now
                var w = world
                while (pending >= GameConfig.STEP && !w.over) {
                    val next = Engine.step(w, finger.take())
                    for (e in next.eventsSince(w.time)) {
                        if (!quiet) sounds?.play(e.kind, next.combo)
                        when (e.kind) {
                            EventKind.HIT, EventKind.STUMBLE, EventKind.CAUGHT -> {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                frozenUntil = now + HIT_PAUSE_NANOS
                            }

                            EventKind.NEAR_MISS, EventKind.POWER -> haptics.performHapticFeedback(
                                HapticFeedbackType.TextHandleMove
                            )

                            else -> Unit
                        }
                    }
                    w = next
                    pending -= GameConfig.STEP
                    if (now < frozenUntil) pending = 0.0
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
        GameScene(world, sprites, Modifier.fillMaxSize(), skin = skin, labels = sceneLabels())
        Hud(world, target, daily, sprites)
    }
}

/**
 * Score (and combo), croquetas, the power-up, how close she is, the challenge or daily banner. On
 * short screens (landscape, handhelds) it all goes in one row: the jumps reach the top of the screen.
 */
@Composable
internal fun Hud(world: World, target: Int?, daily: Boolean, sprites: GameSprites) {
    val banner = when {
        target != null -> stringResource(R.string.challenge_banner, formatScore(target))
        daily -> stringResource(R.string.daily_banner)
        else -> null
    }
    val short = isShortScreen()
    Column(Modifier.fillMaxSize().statusBarsPadding().padding(if (short) 12.dp else 20.dp)) {
        if (short) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Score(world, Modifier.panel())
                ChaseMeter(
                    world.gap,
                    sprites,
                    stringResource(R.string.chase_description),
                    Modifier.weight(1f).padding(horizontal = 8.dp).panel()
                )
                Counts(world, Modifier.panel())
            }
        } else {
            Row(Modifier.fillMaxWidth().panel(), verticalAlignment = Alignment.CenterVertically) {
                Score(world)
                Spacer(Modifier.weight(1f))
                Counts(world)
            }
            ChaseMeter(
                world.gap,
                sprites,
                stringResource(R.string.chase_description),
                Modifier.padding(top = 8.dp).panel()
            )
        }
        if (banner != null) {
            Text(
                banner,
                color = Kitchen.Shout,
                fontWeight = FontWeight.Bold,
                style = if (short) MaterialTheme.typography.labelLarge else MaterialTheme.typography.bodyLarge,
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

@Composable
private fun Score(world: World, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(
            formatScore(world.score),
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Black,
            color = Kitchen.Ink,
            modifier = Modifier.testTag("score")
        )
        if (world.comboLive) {
            Spacer(Modifier.width(10.dp))
            Text(
                "×${minOf(world.combo, GameConfig.MAX_COMBO)}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = Kitchen.Raised,
                modifier = Modifier
                    .background(Kitchen.Shout, RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 2.dp)
                    .testTag("combo")
            )
        }
    }
}

@Composable
private fun Counts(world: World, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.End) {
        Text(
            pluralStringResource(R.plurals.croquetas_count, world.eaten, world.eaten),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Kitchen.Muted
        )
        val power = when {
            world.skates > 0 -> stringResource(R.string.pop_skates)
            world.shield -> stringResource(R.string.pop_sandwich)
            else -> null
        }
        if (power != null) {
            Text(power, color = Kitchen.GreenText, fontWeight = FontWeight.Bold, modifier = Modifier.testTag("power"))
        }
    }
}

/** HUD text sits on a cream panel: in landscape the tiled wall reaches the top of the screen. */
private fun Modifier.panel() = background(Kitchen.Raised.copy(alpha = 0.92f), RoundedCornerShape(20.dp))
    .padding(horizontal = 16.dp, vertical = 6.dp)
