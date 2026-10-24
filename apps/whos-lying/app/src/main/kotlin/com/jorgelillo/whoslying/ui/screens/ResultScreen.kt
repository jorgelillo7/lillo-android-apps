package com.jorgelillo.whoslying.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jorgelillo.core.designsystem.ConfettiRain
import com.jorgelillo.whoslying.R
import com.jorgelillo.whoslying.domain.Elimination
import com.jorgelillo.whoslying.domain.Game
import com.jorgelillo.whoslying.domain.Outcome
import com.jorgelillo.whoslying.domain.Role
import com.jorgelillo.whoslying.ui.DrawStroke
import com.jorgelillo.whoslying.ui.theme.Neon

/** End of the game: who won, the words, who was who, and confetti when someone won. */
@Composable
internal fun ResultScreen(
    game: Game,
    lastElimination: Elimination?,
    drawing: List<DrawStroke>,
    onShareDrawing: () -> Unit,
    /** Rounds left in the match after this game; null when there is no limit. */
    roundsLeft: Int?,
    onSeeWinner: () -> Unit,
    onReport: (() -> Unit)?,
    /** Goes to the ranking, which then starts the next round. */
    onContinue: () -> Unit,
    onHome: () -> Unit
) {
    val outcome = game.outcome
    val impostors = game.cards.filter { it.role == Role.IMPOSTOR }
    val drifter = game.cards.firstOrNull { it.role == Role.DRIFTER }
    val (emojis, title, subtitle) = when (outcome) {
        Outcome.CiviliansWin -> Triple(
            "😎🕵️😱",
            pluralStringResource(R.plurals.result_caught, impostors.size + (if (drifter != null) 1 else 0)),
            stringResource(R.string.result_sub_civilians)
        )

        Outcome.DrifterGuessed -> Triple(
            "🤷💡🤯",
            stringResource(R.string.result_drifter_guessed),
            stringResource(R.string.result_sub_drifter)
        )

        Outcome.NoCivilians -> Triple(
            "🎭🎭🎭",
            stringResource(R.string.result_nobody),
            stringResource(R.string.result_sub_nobody)
        )

        Outcome.EndedEarly -> Triple(
            "🏁",
            stringResource(R.string.result_ended),
            stringResource(R.string.result_sub_ended)
        )

        is Outcome.InfiltratorsWin -> Triple(
            "😏🕵️😏",
            when (outcome.roles) {
                setOf(Role.DRIFTER) -> stringResource(R.string.result_drifter)
                setOf(Role.IMPOSTOR) -> stringResource(R.string.result_impostors)
                else -> stringResource(R.string.result_impostors_drifter)
            },
            if (lastElimination?.role ==
                Role.CIVILIAN
            ) {
                stringResource(R.string.result_sub_infiltrators_vote)
            } else {
                stringResource(R.string.result_sub_infiltrators)
            }
        )

        null -> Triple("🕵️", "", "")
    }
    val background = when (outcome) {
        Outcome.CiviliansWin -> Neon.CiviliansBackdrop

        Outcome.DrifterGuessed -> Neon.DrifterBackdrop

        is Outcome.InfiltratorsWin -> if (Role.IMPOSTOR in
            outcome.roles
        ) {
            Neon.ImpostorsBackdrop
        } else {
            Neon.DrifterBackdrop
        }

        else -> Neon.RevealBackdrop
    }
    Box(Modifier.fillMaxSize()) {
        Page(
            title = null,
            onBack = null,
            sideActionsWhenShort = true,
            background = background,
            bottom = {
                if (roundsLeft == 0) {
                    BigButton(
                        stringResource(R.string.result_see_winner),
                        onSeeWinner,
                        Modifier.testTag("see_winner"),
                        color = Neon.Amber,
                        contentColor = Neon.Night
                    )
                } else {
                    BigButton(
                        stringResource(R.string.continue_),
                        onContinue,
                        Modifier.testTag("play_again"),
                        color = Neon.Go,
                        contentColor = Neon.Night
                    )
                }
                TextButton(onClick = onHome, modifier = Modifier.align(Alignment.CenterHorizontally).testTag("home")) {
                    Text(stringResource(R.string.home), color = Color.White.copy(alpha = 0.85f))
                }
            }
        ) {
            Column(
                Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(emojis, fontSize = 64.sp)
                Text(
                    title,
                    color = Color.White,
                    fontSize = 36.sp,
                    lineHeight = 42.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp).testTag("result_title")
                )
                Text(
                    subtitle,
                    color = Color.White.copy(alpha = 0.9f),
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )
                if (roundsLeft != null && roundsLeft > 0) {
                    Text(
                        pluralStringResource(R.plurals.scores_rounds_left, roundsLeft, roundsLeft),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(
                            top = 6.dp
                        ).clip(
                            CircleShape
                        ).background(Color.Black.copy(alpha = 0.2f)).padding(horizontal = 14.dp, vertical = 4.dp)
                    )
                }
                Spacer(Modifier.height(20.dp))
                ResultPanel {
                    LabelValue(stringResource(R.string.result_secret_word), game.civilianWord)
                    impostors.firstNotNullOfOrNull { it.word }?.let { decoy ->
                        LabelValue(stringResource(R.string.result_decoy), decoy)
                    }
                    if (impostors.isNotEmpty() && impostors.size < game.cards.size) {
                        LabelValue(
                            pluralStringResource(R.plurals.result_impostors_label, impostors.size),
                            impostors.joinToString {
                                it.player
                            }
                        )
                    }
                    drifter?.let { LabelValue(stringResource(R.string.role_drifter), it.player) }
                }
                if (drawing.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    ResultPanel {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                stringResource(R.string.result_drawing),
                                color = Color.White.copy(alpha = 0.7f),
                                style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(onClick = onShareDrawing, modifier = Modifier.testTag("share_drawing")) {
                                Text("↗ " + stringResource(R.string.result_share), color = Color.White)
                            }
                        }
                        DrawingPreview(drawing, Modifier.fillMaxWidth(0.8f).padding(top = 6.dp))
                    }
                }
                Spacer(Modifier.height(12.dp))
                ResultPanel {
                    Text(
                        stringResource(R.string.result_roles),
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.labelLarge
                    )
                    // Name and role on one line, the word under the name: a long word never squeezes
                    // the name into pieces ("Fr / an / co").
                    game.cards.forEach { card ->
                        Row(
                            Modifier.fillMaxWidth().padding(top = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(Modifier.size(10.dp).clip(CircleShape).background(roleColor(card.role)))
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    card.player,
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    card.word ?: "—",
                                    color = Color.White.copy(alpha = 0.7f),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Spacer(Modifier.width(10.dp))
                            Text(roleLabel(card.role), color = roleColor(card.role), fontWeight = FontWeight.Bold)
                        }
                    }
                }
                if (onReport != null) {
                    TextButton(onClick = onReport, modifier = Modifier.padding(top = 8.dp).testTag("report")) {
                        Text("⚠️  " + stringResource(R.string.report_action), color = Color.White.copy(alpha = 0.85f))
                    }
                }
            }
        }
        if (outcome != null && outcome != Outcome.EndedEarly) ConfettiRain(Neon.Confetti, Modifier.fillMaxSize())
    }
}

@Composable
private fun ResultPanel(content: @Composable () -> Unit) {
    Column(
        Modifier.fillMaxWidth().clip(
            MaterialTheme.shapes.large
        ).background(Color.Black.copy(alpha = 0.25f)).padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) { content() }
}

@Composable
private fun ColumnScope.LabelValue(label: String, value: String) {
    Text(
        label,
        color = Color.White.copy(alpha = 0.7f),
        style = MaterialTheme.typography.labelLarge,
        modifier = Modifier.padding(top = 6.dp)
    )
    Text(value, color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
}

@Composable
internal fun roleLabel(role: Role): String = stringResource(
    when (role) {
        Role.CIVILIAN -> R.string.role_civilian
        Role.IMPOSTOR -> R.string.role_impostor
        Role.DRIFTER -> R.string.role_drifter
    }
)

internal fun roleColor(role: Role): Color = when (role) {
    Role.CIVILIAN -> Neon.Turquoise
    Role.IMPOSTOR -> Neon.PinkText
    Role.DRIFTER -> Neon.AmberText
}
