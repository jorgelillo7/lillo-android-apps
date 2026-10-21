package com.jorgelillo.tournaments.ui.tournament

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.text.TextPaint
import android.text.TextUtils
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.createBitmap
import com.jorgelillo.core.platform.shareFile
import com.jorgelillo.core.platform.sharedCacheFile
import com.jorgelillo.tournaments.R
import com.jorgelillo.tournaments.domain.BracketLayout
import com.jorgelillo.tournaments.domain.Stage
import com.jorgelillo.tournaments.domain.Standings
import com.jorgelillo.tournaments.domain.Stats
import com.jorgelillo.tournaments.domain.Tournament
import com.jorgelillo.tournaments.ui.formatDay
import com.jorgelillo.tournaments.ui.roundName
import com.jorgelillo.tournaments.ui.theme.Arena

private const val MARGIN = 48f
private const val TITLE_H = 150f
private const val MAX_SIDE = 4096f

/** The bracket (or the Swiss table if there is no bracket yet) as a PNG, to the share sheet. */
fun shareImage(context: Context, t: Tournament) {
    val bitmap = if (t.eliminationRounds > 0) bracketBitmap(context, t) else standingsBitmap(context, t)
    val file = context.sharedCacheFile("tournament.png")
    file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    context.shareFile(file, "image/png", subject = t.name)
}

fun shareCsv(context: Context, t: Tournament) {
    val file = context.sharedCacheFile("tournament.csv")
    file.writeText(Stats.csv(t) { context.resources.roundName(t, it) })
    context.shareFile(file, "text/csv", subject = t.name)
}

private fun paint(color: Color, size: Float, bold: Boolean = false) = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
    this.color = color.toArgb()
    textSize = size
    typeface = if (bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
}

private fun Canvas.text(s: String, x: Float, y: Float, p: TextPaint, maxWidth: Float) =
    drawText(TextUtils.ellipsize(s, p, maxWidth, TextUtils.TruncateAt.END).toString(), x, y, p)

/** Background, title, subtitle and (when decided) the champion line; returns the canvas. */
private fun frame(context: Context, t: Tournament, width: Float, height: Float): Pair<Bitmap, Canvas> {
    val bitmap = createBitmap(width.toInt(), height.toInt())
    val canvas = Canvas(bitmap)
    canvas.drawRect(
        0f,
        0f,
        width,
        height,
        Paint().apply {
            shader =
                LinearGradient(0f, 0f, 0f, height, Arena.InkTop.toArgb(), Arena.Ink.toArgb(), Shader.TileMode.CLAMP)
        }
    )
    canvas.text(t.name, MARGIN, MARGIN + 52, paint(Arena.Cream, 56f, bold = true), width - 2 * MARGIN)
    val subtitle = listOf(t.game, formatDay(t.epochDay)).filter { it.isNotBlank() }.joinToString(" · ")
    val champion = t.player(t.champion)?.let { "🏆 " + context.getString(R.string.champion_label) + ": " + it.name }
    canvas.text(
        listOfNotNull(subtitle, champion).joinToString("   "),
        MARGIN,
        MARGIN + 100,
        paint(Arena.Gold, 32f, bold = true),
        width - 2 * MARGIN
    )
    canvas.text(context.getString(R.string.app_name), MARGIN, height - 22, paint(Arena.Muted, 24f), width - 2 * MARGIN)
    return bitmap to canvas
}

private fun bracketBitmap(context: Context, t: Tournament): Bitmap {
    val grid = BracketLayout.of(t.eliminationRounds)
    var g = BracketGeometry(cardW = 280f, cardH = 112f, pitch = 140f, gap = 52f, header = 48f)
    // Huge brackets are scaled down so the PNG stays shareable.
    val scale = minOf(1f, MAX_SIDE / (g.width(grid) + 2 * MARGIN), MAX_SIDE / (g.height(grid) + TITLE_H + 2 * MARGIN))
    g = BracketGeometry(g.cardW * scale, g.cardH * scale, g.pitch * scale, g.gap * scale, g.header * scale)
    val width = maxOf(g.width(grid) + 2 * MARGIN, 1080f)
    val height = g.height(grid) + TITLE_H + 2 * MARGIN + 30
    val (bitmap, canvas) = frame(context, t, width, height)
    val left = (width - g.width(grid)) / 2
    val top = TITLE_H + MARGIN
    canvas.translate(left, top)

    val champion = t.champion
    val path = champion?.let { BracketLayout.pathOf(t, it).map { m -> m.round to m.slot }.toSet() }.orEmpty()
    val line = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    grid.cells.forEach { cell ->
        val points = g.connector(grid, cell) ?: return@forEach
        val m = t.round(Stage.ELIMINATION, cell.round).getOrNull(cell.slot)
        val won = m != null && !m.isBye && m.winner(t.bestOf) != null
        line.color = (if (won) Arena.Gold else Arena.Faint).toArgb()
        line.strokeWidth = (
            if ((cell.round to cell.slot) in path) {
                10f
            } else if (won) {
                6f
            } else {
                3f
            }
            ) * scale
        val p = android.graphics.Path().apply {
            moveTo(points[0].x, points[0].y)
            points.drop(1).forEach { lineTo(it.x, it.y) }
        }
        canvas.drawPath(p, line)
    }
    val header = paint(Arena.Gold, 26f * scale, bold = true).apply { textAlign = Paint.Align.CENTER }
    grid.cells.distinctBy { it.column }.forEach { cell ->
        canvas.drawText(
            context.resources.roundName(t, Stage.ELIMINATION, cell.round),
            g.x(cell) + g.cardW / 2,
            30f * scale,
            header
        )
    }
    val card = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Arena.Raised.toArgb() }
    val border = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }
    grid.cells.forEach { cell ->
        val m = t.round(Stage.ELIMINATION, cell.round).getOrNull(cell.slot) ?: return@forEach
        val x = g.x(cell)
        val y = g.y(cell)
        val rect = RectF(x, y, x + g.cardW, y + g.cardH)
        canvas.drawRoundRect(rect, 18f * scale, 18f * scale, card)
        val winner = m.winner(t.bestOf)
        val done = winner != null && !m.isBye
        border.color = (if (cell.side == BracketLayout.Side.CENTER && done) Arena.Gold else Arena.Faint).toArgb()
        canvas.drawRoundRect(rect, 18f * scale, 18f * scale, border)
        val rows = if (m.isBye && m.round == 1) {
            listOf(
                Triple(t.player(m.a ?: m.b)?.name, null, false),
                Triple(context.getString(R.string.bracket_bye), null, false)
            )
        } else {
            listOf(
                Triple(
                    t.player(m.a)?.name,
                    m.winsA.takeIf {
                        done
                    },
                    done && winner == m.a
                ),
                Triple(
                    t.player(m.b)?.name,
                    m.winsB.takeIf { done },
                    done && winner == m.b
                )
            )
        }
        rows.forEachIndexed { i, (name, score, won) ->
            val baseline = y + g.cardH / 2 * i + g.cardH / 4 + 10f * scale
            val p = paint(
                if (won) {
                    Arena.Gold
                } else if (name == null || done) {
                    Arena.Muted
                } else {
                    Arena.Cream
                },
                28f * scale,
                bold = won
            )
            canvas.text(name ?: "—", x + 16f * scale, baseline, p, g.cardW - 64f * scale)
            if (score != null) {
                val sp = paint(if (won) Arena.Gold else Arena.Muted, 30f * scale, bold = true).apply {
                    textAlign =
                        Paint.Align.RIGHT
                }
                canvas.drawText("$score", x + g.cardW - 16f * scale, baseline, sp)
            }
        }
    }
    return bitmap
}

private fun standingsBitmap(context: Context, t: Tournament): Bitmap {
    val rows = Standings.of(t)
    val rowH = 64f
    val width = 1080f
    val height = TITLE_H + 2 * MARGIN + 60 + rows.size * rowH + 30
    val (bitmap, canvas) = frame(context, t, width, height)
    var y = TITLE_H + MARGIN + 40
    val head = paint(Arena.Muted, 26f)
    canvas.drawText(context.getString(R.string.standings_player), MARGIN + 70, y, head)
    canvas.drawText(context.getString(R.string.standings_points), width - MARGIN - 300, y, head)
    canvas.drawText(context.getString(R.string.standings_record), width - MARGIN - 160, y, head)
    rows.forEachIndexed { i, s ->
        y += rowH
        val gold = i == 0 && s.points > 0
        val p = paint(if (gold) Arena.Gold else Arena.Cream, 32f, bold = gold)
        canvas.drawText("${i + 1}", MARGIN, y, paint(Arena.Muted, 32f, bold = true))
        canvas.text(t.player(s.player)?.name.orEmpty(), MARGIN + 70, y, p, width - 2 * MARGIN - 400)
        canvas.drawText("${s.points}", width - MARGIN - 300, y, paint(Arena.Cream, 32f, bold = true))
        canvas.drawText("${s.wins}-${s.draws}-${s.losses}", width - MARGIN - 160, y, paint(Arena.Cream, 32f))
    }
    return bitmap
}
