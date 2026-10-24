package com.jorgelillo.whoslying.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jorgelillo.whoslying.R
import com.jorgelillo.whoslying.domain.Game
import com.jorgelillo.whoslying.domain.Match
import com.jorgelillo.whoslying.domain.Role
import com.jorgelillo.whoslying.ui.DrawStroke
import com.jorgelillo.whoslying.ui.InkColors
import com.jorgelillo.whoslying.ui.PaperColor
import com.jorgelillo.whoslying.ui.drawStrokes
import com.jorgelillo.whoslying.ui.theme.Neon
import kotlinx.coroutines.delay

/** Drawing mode's clue round: everyone adds to one picture, passing the phone, no talking. */
@Composable
internal fun DrawingScreen(
    game: Game,
    alive: Set<String>,
    strokes: SnapshotStateList<DrawStroke>,
    discussionSeconds: Int,
    onVote: () -> Unit,
    onTimeUp: () -> Unit,
    onRevealAll: () -> Unit,
    onQuit: () -> Unit
) {
    // Saved as ARGB so a colour picked on the wheel survives rotation like a fixed one.
    var inkArgb by rememberSaveable { mutableIntStateOf(InkColors[0].toArgb()) }
    val ink = Color(inkArgb)
    val starter =
        game.starter.takeIf { it in alive } ?: game.cards.first { it.player in alive && it.role != Role.DRIFTER }.player
    val voteButtons: @Composable ColumnScope.() -> Unit = {
        BigButton(
            stringResource(R.string.vote_button),
            onVote,
            Modifier.testTag("go_vote"),
            color = Neon.Go,
            contentColor = Neon.Night
        )
        TextButton(
            onClick = onRevealAll,
            modifier = Modifier.align(Alignment.CenterHorizontally).testTag("reveal_all")
        ) {
            Text(stringResource(R.string.vote_reveal_all), color = Neon.Muted)
        }
    }
    val turns: @Composable () -> Unit = {
        Text(
            stringResource(R.string.drawing_turns, starter),
            color = Neon.Amber,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
    BoxWithConstraints(Modifier.fillMaxSize()) {
        if (maxWidth > maxHeight) {
            // Landscape (and wide handhelds): the canvas takes the full height, controls go to the side.
            Page(title = null, onBack = onQuit, background = Neon.Backdrop) {
                Row(
                    Modifier.weight(1f).fillMaxWidth().padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Box(Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                        DrawingCanvas(
                            strokes,
                            ink,
                            Modifier.fillMaxHeight().aspectRatio(4f / 5f, matchHeightConstraintsFirst = true)
                        )
                    }
                    Column(
                        Modifier.width(300.dp).fillMaxHeight().verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        turns()
                        if (discussionSeconds > 0) key(alive.size) { TimerBar(discussionSeconds, onTimeUp) }
                        Tools(ink, strokes, columns = 5) { inkArgb = it.toArgb() }
                        Spacer(Modifier.height(4.dp))
                        voteButtons()
                    }
                }
            }
        } else {
            Page(title = null, onBack = onQuit, background = Neon.Backdrop, bottom = voteButtons) {
                turns()
                if (discussionSeconds > 0) {
                    Spacer(Modifier.height(8.dp))
                    key(alive.size) { TimerBar(discussionSeconds, onTimeUp) }
                }
                Spacer(Modifier.height(10.dp))
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    DrawingCanvas(strokes, ink, Modifier.aspectRatio(4f / 5f))
                }
                Spacer(Modifier.height(10.dp))
                Tools(ink, strokes, columns = 5) { inkArgb = it.toArgb() }
            }
        }
    }
}

/** Inks, the colour wheel, undo and clear, in rows of [columns]. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Tools(ink: Color, strokes: SnapshotStateList<DrawStroke>, columns: Int, onInk: (Color) -> Unit) {
    var wheel by rememberSaveable { mutableStateOf(false) }
    if (wheel) {
        ColorWheelDialog(ink, onPick = {
            onInk(it)
            wheel = false
        }, onDismiss = { wheel = false })
    }
    val custom = ink !in InkColors
    FlowRow(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalArrangement = Arrangement.spacedBy(10.dp),
        maxItemsInEachRow = columns
    ) {
        InkColors.forEachIndexed { i, color ->
            val label = stringResource(R.string.drawing_ink, i + 1)
            Swatch(color == ink, SolidColor(color), {
                onInk(color)
            }, Modifier.semantics { contentDescription = label }.testTag("ink_$i"))
        }
        // Any other colour: the rainbow opens the colour wheel.
        val wheelLabel = stringResource(R.string.drawing_pick_color)
        Swatch(custom, RainbowBrush, {
            wheel = true
        }, Modifier.semantics { contentDescription = wheelLabel }.testTag("ink_wheel"))
        ToolButton("↶", "undo", enabled = strokes.isNotEmpty()) { strokes.removeAt(strokes.lastIndex) }
        ToolButton("🗑", "clear", enabled = strokes.isNotEmpty()) { strokes.clear() }
    }
}

/** One colour choice: a 48dp touch target around a dot that grows and gets a white ring when [selected]. */
@Composable
private fun Swatch(selected: Boolean, fill: Brush, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier.size(48.dp).clip(CircleShape).clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Box(
            Modifier
                .size(if (selected) 36.dp else 30.dp)
                .clip(CircleShape)
                .background(fill)
                .border(3.dp, if (selected) Color.White else Color.White.copy(alpha = 0.15f), CircleShape)
        )
    }
}

@Composable
private fun ToolButton(symbol: String, tag: String, enabled: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(Neon.CardHigh)
            .clickable(enabled = enabled, onClick = onClick)
            .testTag(tag),
        contentAlignment = Alignment.Center
    ) { Text(symbol, fontSize = 20.sp, color = if (enabled) Neon.Ink else Neon.Muted) }
}

@Composable
private fun DrawingCanvas(strokes: SnapshotStateList<DrawStroke>, ink: Color, modifier: Modifier = Modifier) {
    val currentInk by rememberUpdatedState(ink)
    Canvas(
        modifier
            .clip(MaterialTheme.shapes.large)
            .background(PaperColor)
            .testTag("canvas")
            .pointerInput(Unit) {
                detectTapGestures { p ->
                    strokes += DrawStroke(currentInk).apply { points += Offset(p.x / size.width, p.y / size.height) }
                }
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { p ->
                        strokes +=
                            DrawStroke(currentInk).apply { points += Offset(p.x / size.width, p.y / size.height) }
                    },
                    onDrag = { change, _ ->
                        val p = change.position
                        strokes.lastOrNull()?.points?.add(
                            Offset((p.x / size.width).coerceIn(0f, 1f), (p.y / size.height).coerceIn(0f, 1f))
                        )
                    }
                )
            }
    ) { drawStrokes(strokes) }
}

/** Countdown as a shrinking bar. Tap to pause or resume; at zero it beeps and the vote is forced. */
@Composable
private fun TimerBar(seconds: Int, onTimeUp: () -> Unit) {
    var left by rememberSaveable { mutableIntStateOf(seconds) }
    var paused by rememberSaveable { mutableStateOf(false) }
    val timeUp by rememberUpdatedState(onTimeUp)
    LaunchedEffect(paused) {
        while (!paused && left > 0) {
            delay(1_000)
            left--
            if (left == 0) {
                beep()
                timeUp()
            }
        }
    }
    val color = when {
        left == 0 -> Neon.Pink
        left <= 10 -> Neon.Amber
        else -> Neon.Turquoise
    }
    Box(
        Modifier
            .fillMaxWidth()
            .height(40.dp)
            .clip(CircleShape)
            .background(Neon.Card)
            .clickable(enabled = left > 0) { paused = !paused }
            .testTag("timer"),
        contentAlignment = Alignment.Center
    ) {
        Box(
            Modifier.align(
                Alignment.CenterStart
            ).fillMaxHeight().fillMaxWidth(left.toFloat() / seconds).background(color.copy(alpha = 0.35f))
        )
        Text(
            if (left ==
                0
            ) {
                stringResource(R.string.timer_up)
            } else {
                "%d:%02d".format(left / 60, left % 60) +
                    if (paused) "  ⏸" else ""
            },
            color = if (left == 0) Neon.Pink else Neon.Ink,
            fontWeight = FontWeight.Black
        )
    }
}

/** The finished drawing, read only, for the result screen. */
@Composable
internal fun DrawingPreview(strokes: List<DrawStroke>, modifier: Modifier = Modifier) {
    Canvas(modifier.clip(MaterialTheme.shapes.large).background(PaperColor).aspectRatio(4f / 5f)) {
        drawStrokes(strokes)
    }
}
