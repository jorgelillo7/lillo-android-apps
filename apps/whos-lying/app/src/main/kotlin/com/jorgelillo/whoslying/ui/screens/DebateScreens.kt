package com.jorgelillo.whoslying.ui.screens

import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Handler
import android.os.Looper
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jorgelillo.core.designsystem.isShortScreen
import com.jorgelillo.whoslying.R
import com.jorgelillo.whoslying.domain.Game
import com.jorgelillo.whoslying.domain.Role
import com.jorgelillo.whoslying.ui.theme.Neon
import kotlinx.coroutines.delay

/** Clue round: who starts, the optional countdown and the way to the vote. */
@Composable
internal fun DebateScreen(
    game: Game,
    /** Players still in. Passed in (not read from the mutable [game]) so the screen recomposes. */
    alive: Set<String>,
    discussionSeconds: Int,
    onVote: () -> Unit,
    onTimeUp: () -> Unit,
    onRevealAll: () -> Unit,
    onQuit: () -> Unit
) {
    Page(
        title = null,
        onBack = onQuit,
        background = Neon.RevealBackdrop,
        bottom = {
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
                Text(stringResource(R.string.vote_reveal_all), color = Color.White.copy(alpha = 0.75f))
            }
        }
    ) {
        Column(
            Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Emoji("💬", size = 72)
            Text(
                stringResource(R.string.debate_title),
                color = Color.White,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black
            )
            Text(
                stringResource(R.string.debate_desc),
                color = Color.White.copy(alpha = 0.85f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 6.dp)
            )
            // After the first vote, the next round starts with whoever is still in.
            val starter =
                game.starter.takeIf { it in alive }
                    ?: game.cards.first { it.player in alive && it.role != Role.DRIFTER }.player
            Text(
                stringResource(R.string.countdown_starts, starter),
                color = Neon.AmberText,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 12.dp)
            )
            Spacer(Modifier.height(24.dp))
            if (discussionSeconds > 0) {
                // Keyed by the players left, so every round of discussion gets the full time.
                key(alive.size) { CircularTimer(discussionSeconds, onTimeUp) }
            } else {
                Emoji("🗣️", size = 110)
                Text(
                    stringResource(R.string.debate_no_timer),
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
            val out = game.cards.filter { it.player !in alive }
            if (out.isNotEmpty()) {
                val labels = Role.entries.associateWith { roleLabel(it).lowercase() }
                Text(
                    stringResource(
                        R.string.debate_out,
                        out.joinToString {
                            "${it.player} (${labels.getValue(it.role)})"
                        }
                    ),
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 20.dp).testTag("debate_out")
                )
            }
        }
    }
}

/** Big round countdown. Tap to pause or resume; at zero it beeps and the vote is forced. */
@Composable
private fun CircularTimer(seconds: Int, onTimeUp: () -> Unit) {
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
        else -> Color.White
    }
    Box(
        Modifier
            .widthIn(max = 260.dp)
            .fillMaxWidth(0.75f)
            .aspectRatio(1f)
            .clip(CircleShape)
            .clickable(enabled = left > 0) { paused = !paused }
            .testTag("timer"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize().padding(8.dp)) {
            val stroke = 12.dp.toPx()
            drawCircle(Color.Black.copy(alpha = 0.18f), radius = size.minDimension / 2 - stroke / 2)
            drawCircle(
                Color.White.copy(alpha = 0.2f),
                radius = size.minDimension / 2 - stroke / 2,
                style = Stroke(stroke)
            )
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * left / seconds,
                useCenter = false,
                topLeft = Offset(stroke / 2, stroke / 2),
                size = Size(size.width - stroke, size.height - stroke),
                style = Stroke(stroke, cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("%d:%02d".format(left / 60, left % 60), color = color, fontSize = 64.sp, fontWeight = FontWeight.Black)
            Text(
                stringResource(
                    when {
                        left == 0 -> R.string.timer_up
                        paused -> R.string.timer_paused
                        else -> R.string.timer_pause
                    }
                ),
                color = if (left == 0) Neon.PinkText else Color.White.copy(alpha = 0.8f),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (left == 0) FontWeight.Bold else null,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }
    }
}

internal fun beep() {
    runCatching {
        val tone = ToneGenerator(AudioManager.STREAM_NOTIFICATION, ToneGenerator.MAX_VOLUME)
        tone.startTone(ToneGenerator.TONE_PROP_BEEP2, 600)
        Handler(Looper.getMainLooper()).postDelayed({ tone.release() }, 1_000)
    }
}

/** "Time's up!" for a moment, then straight to the vote. */
@Composable
internal fun TimeUpScreen(onDone: () -> Unit) {
    val done by rememberUpdatedState(onDone)
    LaunchedEffect(Unit) {
        delay(1_800)
        done()
    }
    BackHandler {}
    Page(title = null, onBack = null, background = Neon.ImpostorsBackdrop) {
        val short = isShortScreen()
        Column(
            Modifier.weight(1f).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Emoji("⏰", size = if (short) 64 else 110)
            Text(
                stringResource(R.string.time_up),
                color = Color.White,
                fontSize = if (short) 36.sp else 44.sp,
                lineHeight = if (short) 40.sp else 50.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 16.dp).testTag("time_up")
            )
        }
    }
}
