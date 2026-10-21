package com.jorgelillo.whoslying.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Paint
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.createBitmap
import com.jorgelillo.core.platform.shareFile
import com.jorgelillo.core.platform.sharedCacheFile

/** One finger stroke. Points are relative to the canvas (0..1) so it redraws at any size. */
class DrawStroke(val color: Color) {
    val points: SnapshotStateList<Offset> = mutableStateListOf()
}

/** Canvas background and the ink colors offered while drawing. */
val PaperColor = Color(0xFFEEF0FF)
val InkColors = listOf(
    Color(0xFF14121F),
    Color(0xFFFF8A3D),
    Color(0xFFFFC94D),
    Color(0xFF1FA855),
    Color(0xFF2F6BFF),
    Color(0xFF8B3DFF),
    Color(0xFFFF2E93)
)

/** Stroke width as a share of the canvas width, so previews look like the original. */
private const val STROKE_WIDTH = 0.014f

fun DrawScope.drawStrokes(strokes: List<DrawStroke>) {
    val width = size.width * STROKE_WIDTH
    strokes.forEach { stroke ->
        val points = stroke.points.toList()
        if (points.isEmpty()) return@forEach
        if (points.size == 1) {
            drawCircle(stroke.color, radius = width / 2, center = points[0].scaled(size.width, size.height))
            return@forEach
        }
        val path = Path().apply {
            points.forEachIndexed { i, p ->
                val (x, y) = p.scaled(size.width, size.height)
                if (i == 0) moveTo(x, y) else lineTo(x, y)
            }
        }
        drawPath(path, stroke.color, style = Stroke(width, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

private fun Offset.scaled(width: Float, height: Float) = Offset(x * width, y * height)

/** Renders the drawing to a PNG in the cache and opens the share sheet. */
fun shareDrawing(context: Context, strokes: List<DrawStroke>, caption: String) {
    val width = 1080
    val height = 1350
    val bitmap = createBitmap(width, height)
    val canvas = android.graphics.Canvas(bitmap)
    canvas.drawColor(PaperColor.toArgb())
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        strokeWidth = width * STROKE_WIDTH
    }
    strokes.forEach { stroke ->
        val points = stroke.points.toList()
        if (points.isEmpty()) return@forEach
        paint.color = stroke.color.toArgb()
        if (points.size == 1) {
            canvas.drawPoint(points[0].x * width, points[0].y * height, paint)
        } else {
            val path = android.graphics.Path()
            points.forEachIndexed { i, p -> if (i == 0) path.moveTo(p.x * width, p.y * height) else path.lineTo(p.x * width, p.y * height) }
            canvas.drawPath(path, paint)
        }
    }
    val file = context.sharedCacheFile("drawing.png")
    file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    context.shareFile(file, "image/png", text = caption)
}
