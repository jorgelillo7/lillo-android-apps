package com.jorgelillo.whoslying.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jorgelillo.whoslying.R
import com.jorgelillo.whoslying.ui.theme.Neon
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

/** The rainbow used for the wheel and for the button that opens it. */
internal val RainbowBrush = Brush.sweepGradient(
    listOf(Color.Red, Color.Yellow, Color.Green, Color.Cyan, Color.Blue, Color.Magenta, Color.Red)
)

/** [ColorWheel] in a dialog: "Use" picks the colour, "Cancel" keeps the current one. */
@Composable
internal fun ColorWheelDialog(initial: Color, onPick: (Color) -> Unit, onDismiss: () -> Unit) {
    var color by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.drawing_pick_color)) },
        text = { ColorWheel(initial, onChange = { color = it }) },
        confirmButton = {
            TextButton(onClick = { onPick(color) }, modifier = Modifier.testTag("color_use")) {
                Text(stringResource(R.string.drawing_use_color), color = Neon.TurquoiseText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.vote_cancel), color = Neon.Muted) }
        }
    )
}

/**
 * The usual colour wheel: the angle picks the hue, the distance from the centre the saturation,
 * and a slider below the brightness. Tap or drag on the wheel.
 */
@Composable
internal fun ColorWheel(initial: Color, onChange: (Color) -> Unit) {
    val start = remember(initial) { FloatArray(3).also { android.graphics.Color.colorToHSV(initial.toArgb(), it) } }
    var hue by remember { mutableFloatStateOf(start[0]) }
    var saturation by remember { mutableFloatStateOf(start[1]) }
    // A black or near-black start would hide the wheel's effect: start bright enough to see it.
    var brightness by remember { mutableFloatStateOf(start[2].coerceAtLeast(0.35f)) }
    val color = Color.hsv(hue, saturation, brightness)
    val changed by rememberUpdatedState(onChange)
    LaunchedEffect(color) { changed(color) }
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        fun pick(p: Offset, side: Float) {
            val radius = side / 2
            val dx = p.x - radius
            val dy = p.y - radius
            hue = ((Math.toDegrees(atan2(dy, dx).toDouble()) + 360) % 360).toFloat()
            saturation = (hypot(dx, dy) / radius).coerceIn(0f, 1f)
        }
        Canvas(
            Modifier
                .size(220.dp)
                .testTag("color_wheel")
                .pointerInput(Unit) { detectTapGestures { pick(it, size.width.toFloat()) } }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { pick(it, size.width.toFloat()) },
                        onDrag = { change, _ -> pick(change.position, size.width.toFloat()) }
                    )
                }
        ) {
            val radius = size.minDimension / 2
            drawCircle(RainbowBrush, radius)
            drawCircle(Brush.radialGradient(listOf(Color.White, Color.Transparent), center, radius), radius)
            drawCircle(Color.Black.copy(alpha = 1 - brightness), radius)
            val angle = Math.toRadians(hue.toDouble())
            val knob = Offset(
                center.x + (cos(angle) * saturation * radius).toFloat(),
                center.y + (sin(angle) * saturation * radius).toFloat()
            )
            drawCircle(color, 12.dp.toPx(), knob)
            drawCircle(Color.White, 12.dp.toPx(), knob, style = Stroke(3.dp.toPx()))
        }
        Slider(
            value = brightness,
            onValueChange = { brightness = it },
            modifier = Modifier.fillMaxWidth().testTag("color_brightness"),
            colors = SliderDefaults.colors(thumbColor = color, activeTrackColor = color)
        )
        Spacer(Modifier.height(4.dp))
        Box(
            Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(color)
                .border(3.dp, Color.White, CircleShape)
        )
    }
}
