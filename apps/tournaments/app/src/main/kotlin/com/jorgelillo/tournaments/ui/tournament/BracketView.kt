package com.jorgelillo.tournaments.ui.tournament

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jorgelillo.core.designsystem.isShortScreen
import com.jorgelillo.tournaments.R
import com.jorgelillo.tournaments.domain.BracketLayout
import com.jorgelillo.tournaments.domain.Match
import com.jorgelillo.tournaments.domain.Stage
import com.jorgelillo.tournaments.domain.Tournament
import com.jorgelillo.tournaments.ui.roundName
import com.jorgelillo.tournaments.ui.theme.Arena
import kotlin.math.roundToInt

private val CARD_W = 140.dp
private val CARD_H = 60.dp
private val PITCH = 76.dp
private val GAP = 26.dp
private val HEADER = 28.dp

/** Same geometry for the screen and the exported image (pixels there, dp here). */
data class BracketGeometry(val cardW: Float, val cardH: Float, val pitch: Float, val gap: Float, val header: Float) {
    fun x(cell: BracketLayout.Cell) = cell.column * (cardW + gap)
    fun y(cell: BracketLayout.Cell) = header + cell.y * pitch
    fun width(grid: BracketLayout.Grid) = grid.columns * cardW + (grid.columns - 1) * gap
    fun height(grid: BracketLayout.Grid) = header + (grid.rows - 1) * pitch + cardH

    /** Elbow from a match to the one its winner goes to: out the side facing the centre. */
    fun connector(grid: BracketLayout.Grid, cell: BracketLayout.Cell): List<Offset>? {
        val next = grid.cell(cell.round + 1, cell.slot / 2) ?: return null
        val fromLeft = cell.side == BracketLayout.Side.LEFT
        val start = Offset(if (fromLeft) x(cell) + cardW else x(cell), y(cell) + cardH / 2)
        val end = Offset(if (fromLeft) x(next) else x(next) + cardW, y(next) + cardH / 2)
        val midX = (start.x + end.x) / 2
        return listOf(start, Offset(midX, start.y), Offset(midX, end.y), end)
    }
}

/**
 * Two-sided bracket: first half on the left, second half on the right, final in the centre.
 * Opens fitted to the screen (down to MIN_ZOOM: below that names are unreadable, so it scrolls),
 * re-fits on rotation, and zooms with a pinch or the -/+ buttons. A decided match sends a gold
 * line (the victory line) towards the winner's next match; the champion's path is thicker.
 */
@Composable
fun BracketView(t: Tournament, onEdit: (Match) -> Unit) {
    val rounds = t.eliminationRounds
    val grid = remember(rounds) { BracketLayout.of(rounds) }
    val geometry = BracketGeometry(CARD_W.value, CARD_H.value, PITCH.value, GAP.value, HEADER.value)
    val champion = t.champion
    val championPath = champion?.let { BracketLayout.pathOf(t, it).map { m -> m.round to m.slot }.toSet() }.orEmpty()
    val contentWidth = geometry.width(grid)
    val contentHeight = geometry.height(grid) + 16
    // Null = follow the fit, so rotating re-fits; set once the user zooms by hand.
    var userZoom by rememberSaveable(rounds) { mutableStateOf<Float?>(null) }
    val short = isShortScreen()

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val screenWidth = maxWidth
        val fit = minOf(maxWidth.value / contentWidth, maxHeight.value / contentHeight).coerceIn(MIN_ZOOM, 1f)
        val zoom = userZoom ?: fit
        Box(
            Modifier
                .fillMaxSize()
                .pointerInput(Unit) { detectPinch { factor -> userZoom = ((userZoom ?: fit) * factor).coerceIn(MIN_ZOOM, MAX_ZOOM) } }
                .horizontalScroll(rememberScrollState())
                .verticalScroll(rememberScrollState())
                .testTag("bracket")
        ) {
            // Centred when it is narrower than the screen.
            Box(Modifier.widthIn(min = screenWidth), contentAlignment = Alignment.TopCenter) {
                Box(Modifier.scaled(zoom).size(contentWidth.dp, contentHeight.dp)) {
                    Connectors(t, grid, geometry, championPath)
                    // Round names over each column.
                    grid.cells.distinctBy { it.column }.forEach { cell ->
                        Text(
                            roundName(t, Stage.ELIMINATION, cell.round),
                            color = Arena.Gold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            modifier = Modifier.offset(x = geometry.x(cell).dp).width(CARD_W)
                        )
                    }
                    grid.cells.forEach { cell ->
                        val match = t.round(Stage.ELIMINATION, cell.round).getOrNull(cell.slot) ?: return@forEach
                        BracketCard(
                            t,
                            match,
                            isFinal = cell.side == BracketLayout.Side.CENTER,
                            onEdit = onEdit,
                            modifier = Modifier.offset(x = geometry.x(cell).dp, y = geometry.y(cell).dp)
                        )
                    }
                }
            }
        }
        // Only in portrait, where it doesn't fit at full size; in landscape they'd cover cards (pinch still works).
        if (fit < 1f && !short) {
            ZoomButtons(
                onOut = { userZoom = (zoom / ZOOM_STEP).coerceAtLeast(MIN_ZOOM) },
                onIn = { userZoom = (zoom * ZOOM_STEP).coerceAtMost(MAX_ZOOM) },
                onFit = { userZoom = null },
                modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp)
            )
        }
    }
}

@Composable
private fun BoxScope.Connectors(t: Tournament, grid: BracketLayout.Grid, geometry: BracketGeometry, championPath: Set<Pair<Int, Int>>) {
    Canvas(Modifier.matchParentSize()) {
        grid.cells.forEach { cell ->
            val points = geometry.connector(grid, cell) ?: return@forEach
            val match = t.round(Stage.ELIMINATION, cell.round).getOrNull(cell.slot)
            val won = match != null && !match.isBye && match.winner(t.bestOf) != null
            val path = Path().apply {
                moveTo(points[0].x.dp.toPx(), points[0].y.dp.toPx())
                points.drop(1).forEach { lineTo(it.x.dp.toPx(), it.y.dp.toPx()) }
            }
            val width = when {
                (cell.round to cell.slot) in championPath -> 5.dp
                won -> 3.dp
                else -> 1.5.dp
            }
            drawPath(path, if (won) Arena.Gold else Arena.Faint, style = Stroke(width.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
        }
    }
}

private const val MIN_ZOOM = 0.4f
private const val MAX_ZOOM = 1.6f
private const val ZOOM_STEP = 1.25f

/** Lays the content out at [scale]: the scroll containers see the scaled size. */
private fun Modifier.scaled(scale: Float) = layout { measurable, constraints ->
    val placeable = measurable.measure(constraints)
    layout((placeable.width * scale).roundToInt(), (placeable.height * scale).roundToInt()) {
        placeable.placeWithLayer(0, 0) {
            scaleX = scale
            scaleY = scale
            transformOrigin = TransformOrigin(0f, 0f)
        }
    }
}

/** Two-finger pinch only, so one-finger drags still scroll. */
private suspend fun PointerInputScope.detectPinch(onZoom: (Float) -> Unit) = awaitEachGesture {
    awaitFirstDown(requireUnconsumed = false)
    do {
        val event = awaitPointerEvent()
        if (event.changes.count { it.pressed } >= 2) {
            val factor = event.calculateZoom()
            if (factor != 1f) {
                onZoom(factor)
                event.changes.forEach { it.consume() }
            }
        }
    } while (event.changes.any { it.pressed })
}

@Composable
private fun ZoomButtons(onOut: () -> Unit, onIn: () -> Unit, onFit: () -> Unit, modifier: Modifier) {
    Row(modifier.background(Arena.High, RoundedCornerShape(20.dp)), verticalAlignment = Alignment.CenterVertically) {
        TextButton(onClick = onOut, modifier = Modifier.testTag("zoom_out")) { Text("−", fontSize = 20.sp, color = Arena.Cream) }
        TextButton(onClick = onFit, modifier = Modifier.testTag("zoom_fit")) { Text(stringResource(R.string.bracket_fit), color = Arena.Cream) }
        TextButton(onClick = onIn, modifier = Modifier.testTag("zoom_in")) { Text("+", fontSize = 20.sp, color = Arena.Cream) }
    }
}

@Composable
private fun BracketCard(t: Tournament, m: Match, isFinal: Boolean, onEdit: (Match) -> Unit, modifier: Modifier) {
    val winner = m.winner(t.bestOf)
    val done = winner != null && !m.isBye
    val playable = m.a != null && m.b != null
    Column(
        modifier
            .size(CARD_W, CARD_H)
            .background(Arena.Raised, RoundedCornerShape(10.dp))
            .border(BorderStroke(if (isFinal && done) 2.dp else 1.dp, if (isFinal && done) Arena.Gold else Arena.Faint), RoundedCornerShape(10.dp))
            .clickable(enabled = playable) { onEdit(m) }
            .testTag("card_${m.round}_${m.slot}")
    ) {
        if (m.isBye && m.round == 1) {
            Slot(t.player(m.a ?: m.b)?.name, null, won = false, done = false, CARD_H / 2)
            Slot(stringResource(R.string.bracket_bye), null, won = false, done = true, CARD_H / 2)
            return@Column
        }
        Slot(t.player(m.a)?.name, m.winsA.takeIf { done }, won = done && winner == m.a, done = done, CARD_H / 2)
        Box(Modifier.fillMaxWidth().height(1.dp).background(Arena.Faint))
        Slot(t.player(m.b)?.name, m.winsB.takeIf { done }, won = done && winner == m.b, done = done, CARD_H / 2 - 1.dp)
    }
}

@Composable
private fun Slot(name: String?, score: Int?, won: Boolean, done: Boolean, height: Dp) {
    Row(Modifier.fillMaxWidth().height(height).padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(
            name ?: "—",
            color = when {
                won -> Arena.Gold
                name == null || done -> Arena.Muted
                else -> Arena.Cream
            },
            fontWeight = if (won) FontWeight.Black else FontWeight.Medium,
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        if (score != null) {
            Spacer(Modifier.width(4.dp))
            Text("$score", color = if (won) Arena.Gold else Arena.Muted, fontWeight = FontWeight.Black, fontSize = 14.sp)
        }
    }
}
