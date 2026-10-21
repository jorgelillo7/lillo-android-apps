package com.jorgelillo.whoslying.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jorgelillo.core.designsystem.ConfettiRain
import com.jorgelillo.core.designsystem.isShortScreen
import com.jorgelillo.whoslying.R
import com.jorgelillo.whoslying.domain.Match
import com.jorgelillo.whoslying.ui.theme.Neon

/** Players by points, best first. */
private fun ranking(players: List<String>, scores: Map<String, Int>) =
    (players + scores.keys).distinct().sortedByDescending {
        scores[it]
            ?: 0
    }

@Composable
private fun RankRow(player: String, players: List<String>, points: Int, gained: Int?) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.extraLarge)
            .background(Color.Black.copy(alpha = 0.25f))
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("rank_$player"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Avatar(player, players, 40.dp)
        Text(
            player,
            Modifier.weight(1f).padding(horizontal = 14.dp),
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        if (gained != null) {
            Text(
                "+$gained",
                color = Neon.Night,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(
                    end = 12.dp
                ).clip(CircleShape).background(Neon.Go).padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }
        Text("$points", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
    }
}

/** Between rounds: the running ranking with what the last game gave, then the next round. */
@Composable
internal fun RankingScreen(
    players: List<String>,
    scores: Map<String, Int>,
    lastPoints: Map<String, Int>,
    /** Rounds left in the match; null when there is no limit. */
    roundsLeft: Int?,
    roundsPlayed: Int,
    onNextRound: () -> Unit,
    onReset: () -> Unit,
    onBack: () -> Unit
) {
    var confirmReset by rememberSaveable { mutableStateOf(false) }
    Page(
        title = null,
        onBack = onBack,
        sideActionsWhenShort = true,
        background = Neon.RevealBackdrop,
        bottom = {
            BigButton(
                stringResource(R.string.continue_),
                onNextRound,
                Modifier.testTag("next_round"),
                color = Neon.Go,
                contentColor = Neon.Night
            )
            TextButton(
                onClick = {
                    if (confirmReset) {
                        onReset()
                        confirmReset = false
                    } else {
                        confirmReset = true
                    }
                },
                modifier = Modifier.align(Alignment.CenterHorizontally).testTag("reset_scores")
            ) {
                Text(
                    stringResource(if (confirmReset) R.string.scores_reset_confirm else R.string.scores_reset),
                    color = Color.White.copy(alpha = 0.75f)
                )
            }
        }
    ) {
        Column(
            Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("🏆", fontSize = 56.sp)
            Text(
                stringResource(R.string.scores_title),
                color = Color.White,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black
            )
            Text(
                if (roundsLeft != null) {
                    pluralStringResource(R.plurals.scores_rounds_left, roundsLeft, roundsLeft)
                } else {
                    stringResource(R.string.scores_rounds_free, roundsPlayed)
                },
                color = Color.White.copy(alpha = 0.9f),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp).testTag("rounds_left")
            )
            ranking(players, scores).forEach { player ->
                RankRow(player, players, scores[player] ?: 0, lastPoints[player])
                Spacer(Modifier.height(10.dp))
            }
            Text(
                stringResource(R.string.scores_rules),
                color = Color.White.copy(alpha = 0.85f),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }
    }
}

/** End of a match: the winner big in the middle with fireworks, everyone else below. */
@Composable
internal fun PodiumScreen(players: List<String>, scores: Map<String, Int>, onNewMatch: () -> Unit, onHome: () -> Unit) {
    val leaders = Match.leaders(scores)
    Box(Modifier.fillMaxSize()) {
        Page(
            title = null,
            onBack = null,
            sideActionsWhenShort = true,
            background = Neon.RevealBackdrop,
            bottom = {
                BigButton(
                    stringResource(R.string.podium_new_match),
                    onNewMatch,
                    Modifier.testTag("new_match"),
                    color = Neon.Go,
                    contentColor = Neon.Night
                )
                TextButton(onClick = onHome, modifier = Modifier.align(Alignment.CenterHorizontally).testTag("home")) {
                    Text(stringResource(R.string.home), color = Color.White.copy(alpha = 0.85f))
                }
            }
        ) {
            val short = isShortScreen()
            val winnerBlock = @Composable {
                Text("🏆", fontSize = if (short) 32.sp else 44.sp)
                if (leaders.size == 1) {
                    val winner = leaders.single()
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = if (short) 4.dp else 12.dp)
                    ) {
                        Text("🎆", fontSize = if (short) 36.sp else 56.sp)
                        Spacer(Modifier.width(16.dp))
                        Avatar(winner, players, if (short) 72.dp else 128.dp, ring = Neon.Amber)
                        Spacer(Modifier.width(16.dp))
                        Text("🎆", fontSize = if (short) 36.sp else 56.sp)
                    }
                    Text(
                        winner,
                        color = Color.White,
                        fontSize = if (short) 28.sp else 34.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(top = if (short) 4.dp else 12.dp).testTag("podium_title")
                    )
                    Text(
                        "${scores[winner] ?: 0}",
                        color = Neon.AmberText,
                        fontSize = if (short) 36.sp else 56.sp,
                        fontWeight = FontWeight.Black
                    )
                } else {
                    Text(
                        if (leaders.isEmpty()) {
                            stringResource(
                                R.string.podium_nobody
                            )
                        } else {
                            stringResource(R.string.podium_tie, leaders.joinToString(" · "))
                        },
                        color = Color.White,
                        fontSize = if (short) 26.sp else 32.sp,
                        lineHeight = if (short) 32.sp else 38.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = if (short) 8.dp else 16.dp).testTag("podium_title")
                    )
                }
            }
            val others = @Composable {
                ranking(players, scores).filterNot { leaders.size == 1 && it == leaders.single() }.forEach { player ->
                    RankRow(player, players, scores[player] ?: 0, gained = null)
                    Spacer(Modifier.height(10.dp))
                }
            }
            if (short) {
                // Landscape and handhelds: the winner beside the rest, or the ranking falls below the fold.
                Row(
                    Modifier.weight(1f).fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        Modifier.weight(1f).verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        winnerBlock()
                    }
                    Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) { others() }
                }
            } else {
                Column(
                    Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    winnerBlock()
                    Spacer(Modifier.height(20.dp))
                    others()
                }
            }
        }
        if (leaders.isNotEmpty()) ConfettiRain(Neon.Confetti, Modifier.fillMaxSize())
    }
}
