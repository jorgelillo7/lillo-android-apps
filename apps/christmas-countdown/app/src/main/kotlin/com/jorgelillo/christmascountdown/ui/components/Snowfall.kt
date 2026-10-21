package com.jorgelillo.christmascountdown.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

private class Flake(val x: Float, val y: Float, val radius: Float, val speed: Float, val phase: Float)

/** Gently falling snow. Only the draw phase reads the clock, so it never triggers recomposition. */
@Composable
internal fun Snowfall(modifier: Modifier = Modifier, flakeCount: Int = 70) {
    val flakes = remember {
        List(flakeCount) {
            val depth = Random.nextFloat()
            Flake(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                radius = 1f + depth * 2.5f,
                speed = 0.015f + depth * 0.04f,
                phase = Random.nextFloat() * 2 * PI.toFloat()
            )
        }
    }
    var time by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        var last = withFrameNanos { it }
        while (true) {
            withFrameNanos { now ->
                time += (now - last) / 1_000_000_000f
                last = now
            }
        }
    }
    Canvas(modifier) {
        val sway = 10.dp.toPx()
        for (flake in flakes) {
            val y = ((flake.y + time * flake.speed) % 1f) * size.height
            val x = flake.x * size.width + sin(time * 0.7f + flake.phase) * sway
            drawCircle(
                color = Color.White.copy(alpha = 0.25f + flake.radius * 0.12f),
                radius = flake.radius.dp.toPx(),
                center = Offset(x, y)
            )
        }
    }
}
