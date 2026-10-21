package com.jorgelillo.decisionwheel.ui.wheel

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jorgelillo.decisionwheel.domain.DecisionEngine
import com.jorgelillo.decisionwheel.domain.Segment
import com.jorgelillo.decisionwheel.ui.theme.WheelColors
import kotlin.math.PI
import kotlin.math.min
import kotlin.math.sin

/**
 * Draws the wheel turned by [rotationDegrees] with a fixed pointer at the top.
 * Each slice's size is its real chance. When [winner] is set, the other slices dim.
 */
@Composable
internal fun WheelCanvas(
    segments: List<Segment>,
    colorOf: (String) -> Color,
    rotationDegrees: Float,
    winner: String?,
    modifier: Modifier = Modifier
) {
    val measurer = rememberTextMeasurer()
    val colors = segments.map { colorOf(it.option) }

    Canvas(modifier) {
        val radius = size.minDimension / 2f - 10.dp.toPx()
        val center = Offset(size.width / 2f, size.height / 2f + 6.dp.toPx())
        val arcTopLeft = Offset(center.x - radius, center.y - radius)
        val arcSize = Size(radius * 2, radius * 2)

        // Soft halo behind the wheel.
        drawCircle(WheelColors.NightRaised, radius + 8.dp.toPx(), center)

        rotate(rotationDegrees, pivot = center) {
            segments.forEachIndexed { i, segment ->
                val start = -90f + (segment.startFraction * 360).toFloat()
                val sweep = (segment.sweepFraction * 360).toFloat()
                val dimmed = winner != null && segment.option != winner
                val color = colors[i].copy(alpha = if (dimmed) 0.28f else 1f)
                drawArc(color, start, sweep, useCenter = true, topLeft = arcTopLeft, size = arcSize)
                if (segments.size > 1) {
                    drawArc(
                        WheelColors.Night,
                        start,
                        sweep,
                        useCenter = true,
                        topLeft = arcTopLeft,
                        size = arcSize,
                        style = Stroke(width = 3.dp.toPx())
                    )
                }

                // Label along the radius, sized to fit the slice's width at its middle.
                val chord = 2 * radius * 0.6f * sin(min(sweep, 170f) / 2f * PI.toFloat() / 180f)
                val fontSize = (chord / 2.6f).coerceIn(9.sp.toPx(), 18.sp.toPx())
                if (chord >= 11.sp.toPx()) {
                    val layout = measurer.measure(
                        text = segment.option,
                        style = TextStyle(
                            color = WheelColors.onSegment(colors[i]).copy(alpha = if (dimmed) 0.45f else 1f),
                            fontSize = fontSize.toSp(),
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        constraints = Constraints(maxWidth = (radius * 0.68f).toInt())
                    )
                    rotate(start + sweep / 2f, pivot = center) {
                        drawText(
                            layout,
                            topLeft = Offset(
                                center.x + radius * 0.92f - layout.size.width,
                                center.y - layout.size.height / 2f
                            )
                        )
                    }
                }
            }
        }

        // Hub.
        drawCircle(WheelColors.Night, 30.dp.toPx(), center)
        drawCircle(WheelColors.Cream, 22.dp.toPx(), center)
        drawCircle(WheelColors.Sun, 8.dp.toPx(), center)

        // Pointer at the top, outside the rotation.
        val tip = Offset(center.x, center.y - radius + 14.dp.toPx())
        val pointer = Path().apply {
            moveTo(tip.x, tip.y)
            lineTo(center.x - 16.dp.toPx(), center.y - radius - 18.dp.toPx())
            lineTo(center.x + 16.dp.toPx(), center.y - radius - 18.dp.toPx())
            close()
        }
        drawPath(pointer, WheelColors.Cream)
        drawPath(pointer, WheelColors.Night, style = Stroke(width = 3.dp.toPx()))
    }
}

/**
 * Colour of each option by its position in the wheel's full list, so a slice keeps its colour
 * (and matches its chip) when other options are vetoed or shrink.
 */
fun segmentColors(count: Int): List<Color> =
    DecisionEngine.colorSlots(count, WheelColors.Segments.size).map { WheelColors.Segments[it] }
