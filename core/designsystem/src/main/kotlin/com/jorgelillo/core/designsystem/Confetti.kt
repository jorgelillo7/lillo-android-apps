package com.jorgelillo.core.designsystem

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private class Piece(val angle: Float, val speed: Float, val spin: Float, val colorIndex: Int, val wide: Boolean)

/** A short burst of confetti from the centre of [modifier]'s area, replayed whenever [key] changes. */
@Composable
fun ConfettiBurst(key: Any?, colors: List<Color>, modifier: Modifier = Modifier) {
    if (key == null) return
    val progress = remember(key) { Animatable(0f) }
    val pieces = remember(key) {
        List(48) {
            Piece(
                angle = Random.nextFloat() * 360f,
                speed = 0.55f + Random.nextFloat() * 0.6f,
                spin = Random.nextFloat() * 720f - 360f,
                colorIndex = Random.nextInt(colors.size),
                wide = Random.nextBoolean(),
            )
        }
    }
    LaunchedEffect(key) { progress.animateTo(1f, tween(1400, easing = LinearEasing)) }

    Canvas(modifier) {
        val t = progress.value
        if (t >= 1f) return@Canvas
        val reach = size.minDimension * 0.6f
        val center = Offset(size.width / 2f, size.height / 2f)
        val gravity = size.height * 0.35f * t * t
        pieces.forEach { piece ->
            val rad = Math.toRadians(piece.angle.toDouble())
            val distance = reach * piece.speed * (1f - (1f - t) * (1f - t))
            val position = Offset(
                center.x + cos(rad).toFloat() * distance,
                center.y + sin(rad).toFloat() * distance + gravity,
            )
            val w = if (piece.wide) 10.dp.toPx() else 6.dp.toPx()
            rotate(piece.spin * t, pivot = position) {
                drawRect(
                    color = colors[piece.colorIndex].copy(alpha = 1f - t * t),
                    topLeft = Offset(position.x - w / 2, position.y - 3.dp.toPx()),
                    size = Size(w, 6.dp.toPx()),
                )
            }
        }
    }
}

private class RainPiece(val x: Float, val speed: Float, val drift: Float, val spin: Float, val color: Color, val width: Float, val delay: Float)

/** A few seconds of confetti falling over [modifier]'s area (a win that fills the screen). */
@Composable
fun ConfettiRain(colors: List<Color>, modifier: Modifier = Modifier) {
    val pieces = remember {
        val random = Random(System.nanoTime())
        List(90) {
            RainPiece(
                x = random.nextFloat(),
                speed = 0.18f + random.nextFloat() * 0.25f,
                drift = (random.nextFloat() - 0.5f) * 0.15f,
                spin = random.nextFloat() * 720f - 360f,
                color = colors[random.nextInt(colors.size)],
                width = 6f + random.nextFloat() * 8f,
                delay = random.nextFloat() * 1.2f,
            )
        }
    }
    var time by remember { mutableFloatStateOf(0f) }
    val fade = remember { Animatable(1f) }
    LaunchedEffect(Unit) {
        val start = withFrameNanos { it }
        while (time < 6f) {
            time = (withFrameNanos { it } - start) / 1e9f
        }
        fade.animateTo(0f, tween(600))
    }
    if (fade.value == 0f) return
    Canvas(modifier) {
        pieces.forEach { p ->
            val t = (time - p.delay).coerceAtLeast(0f)
            val y = -0.05f + t * p.speed
            if (y > 1.05f || t == 0f) return@forEach
            val x = p.x + p.drift * t + 0.02f * sin(t * 3f + p.x * 10f)
            val center = Offset(x * size.width, y * size.height)
            rotate(p.spin * t, center) {
                drawRect(
                    p.color.copy(alpha = fade.value),
                    topLeft = center - Offset(p.width / 2, p.width / 4),
                    size = Size(p.width, p.width / 2),
                )
            }
        }
    }
}
