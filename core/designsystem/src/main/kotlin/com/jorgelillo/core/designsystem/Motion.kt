package com.jorgelillo.core.designsystem

import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
