package com.jorgelillo.core.designsystem

import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

/**
 * False when the phone's "Remove animations" (animator duration scale 0) is on: decorative motion
 * then stays still. Screenshot tests turn it off too, so captures show the final state.
 */
@Composable
fun animationsEnabled(): Boolean {
    val resolver = LocalContext.current.contentResolver
    return remember(resolver) { Settings.Global.getFloat(resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) > 0f }
}

/**
 * Fades and lifts the element in once, when it first shows, after [delayMillis] (stagger a few
 * siblings with it). Nothing moves with animations off.
 */
@Composable
fun Modifier.entrance(delayMillis: Int = 0): Modifier {
    val animate = animationsEnabled()
    val progress = remember { Animatable(if (animate) 0f else 1f) }
    LaunchedEffect(Unit) {
        progress.animateTo(1f, tween(ENTRANCE_MILLIS, delayMillis, FastOutSlowInEasing))
    }
    return graphicsLayer {
        alpha = progress.value
        translationY = (1f - progress.value) * ENTRANCE_LIFT.toPx()
    }
}

private const val ENTRANCE_MILLIS = 450
private val ENTRANCE_LIFT = 16.dp

/**
 * A coloured bar along the start edge of a card: its state or kind at a glance (in progress /
 * finished, a wheel's colour…). Put it after the card's clip so the bar follows its corners.
 */
fun Modifier.accentEdge(color: Color, width: Dp = 5.dp): Modifier = drawWithContent {
    drawContent()
    val w = width.toPx()
    val x = if (layoutDirection == LayoutDirection.Rtl) size.width - w else 0f
    drawRect(color, Offset(x, 0f), Size(w, size.height))
}

/**
 * Shrinks the element a little while [interactionSource] is pressed and springs it back on release,
 * with a small bounce (Material 3 Expressive's press feedback). Still with animations off.
 */
@Composable
fun Modifier.springPress(interactionSource: InteractionSource): Modifier {
    val pressed by interactionSource.collectIsPressedAsState()
    val animate = animationsEnabled()
    val scale by animateFloatAsState(
        targetValue = if (pressed && animate) PRESSED_SCALE else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "springPress"
    )
    return graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

private const val PRESSED_SCALE = 0.94f

/** A slow, gentle bob up and down, like something waiting to be picked up. Still with animations off. */
@Composable
fun Modifier.idleFloat(): Modifier {
    if (!animationsEnabled()) return this
    val transition = rememberInfiniteTransition(label = "idleFloat")
    val offset by transition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(FLOAT_MILLIS, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "idleFloatOffset"
    )
    return graphicsLayer { translationY = offset * FLOAT_DISTANCE.toPx() }
}

private const val FLOAT_MILLIS = 1_800
private val FLOAT_DISTANCE = 4.dp
