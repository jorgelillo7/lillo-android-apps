package com.jorgelillo.slipper.ui.over

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jorgelillo.core.designsystem.LilloSecondaryButton
import com.jorgelillo.core.designsystem.isShortScreen
import com.jorgelillo.slipper.R
import com.jorgelillo.slipper.domain.Chunk
import com.jorgelillo.slipper.domain.Furniture
import com.jorgelillo.slipper.domain.FurnitureKind
import com.jorgelillo.slipper.domain.Room
import com.jorgelillo.slipper.domain.SlipperSkin
import com.jorgelillo.slipper.domain.World
import com.jorgelillo.slipper.ui.BigButton
import com.jorgelillo.slipper.ui.Page
import com.jorgelillo.slipper.ui.formatScore
import com.jorgelillo.slipper.ui.game.GameScene
import com.jorgelillo.slipper.ui.game.rememberGameSprites
import com.jorgelillo.slipper.ui.home.label
import com.jorgelillo.slipper.ui.theme.Kitchen

/** The end of every run: she has caught up with him, slipper raised. */
private val CaughtWorld = World(
    seed = 0,
    time = 30.0,
    distance = 100.0,
    gap = 3.0,
    chunks = (0 until 5).map { Chunk(it, 40.0 + it * 50, 90.0 + it * 50, Room.KITCHEN) },
    furniture = listOf(Furniture(FurnitureKind.CHAIR, 118.0)),
    nextThrow = 30.1,
    caught = false
)

/** What a finished run brings to the result screen. */
internal data class RunResult(
    val score: Int,
    val previousBest: Int,
    val croquetas: Int,
    val nearMisses: Int,
    val daily: Boolean,
    /** The skin this run unlocked. */
    val unlocked: SlipperSkin?,
    /** Croquetas still needed for the next skin; null when all are unlocked. */
    val croquetasToNext: Int?
)

/** The result: the score, a record or the challenge's verdict, what it unlocked, and the next run. */
@Composable
internal fun GameOverScreen(
    result: RunResult,
    skin: SlipperSkin,
    challenge: Int?,
    onRetry: () -> Unit,
    onChallengeFriend: () -> Unit,
    onHome: () -> Unit
) {
    Page(
        bottom = {
            BigButton(stringResource(R.string.retry), onRetry, "retry")
            LilloSecondaryButton(
                stringResource(R.string.challenge_friend),
                onChallengeFriend,
                Modifier.padding(top = 8.dp).testTag("challenge_friend"),
                contentColor = Kitchen.Ink
            )
            TextButton(onClick = onHome, modifier = Modifier.align(Alignment.CenterHorizontally).testTag("home")) {
                Text(stringResource(R.string.home), color = Kitchen.Muted)
            }
        }
    ) {
        Column(
            Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            GameScene(
                remember { CaughtWorld },
                rememberGameSprites(),
                Modifier.fillMaxWidth(
                    if (isShortScreen()) 0.6f else 1f
                ).aspectRatio(2.4f).clip(RoundedCornerShape(20.dp)),
                skin = skin
            )
            Text(
                stringResource(R.string.caught),
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Black,
                color = Kitchen.Shout,
                textAlign = TextAlign.Center
            )
            if (result.daily) {
                Text(stringResource(R.string.daily), color = Kitchen.Muted, fontWeight = FontWeight.Bold)
            }
            Text(
                stringResource(R.string.your_score, formatScore(result.score)),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Kitchen.Ink,
                modifier = Modifier.padding(top = 16.dp).testTag("final_score")
            )
            Text(
                stringResource(
                    R.string.run_stats,
                    pluralStringResource(R.plurals.croquetas_count, result.croquetas, result.croquetas),
                    pluralStringResource(R.plurals.near_misses, result.nearMisses, result.nearMisses)
                ),
                color = Kitchen.Muted,
                modifier = Modifier.padding(top = 4.dp).testTag("run_stats")
            )
            val verdict = when {
                challenge != null && result.score > challenge -> stringResource(R.string.challenge_won)
                challenge != null -> stringResource(R.string.challenge_lost, formatScore(challenge))
                result.score > result.previousBest && result.previousBest > 0 -> stringResource(R.string.new_record)
                else -> null
            }
            if (verdict != null) {
                Text(
                    verdict,
                    style = MaterialTheme.typography.titleLarge,
                    color = Kitchen.GreenText,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 12.dp).testTag("verdict")
                )
            }
            if (result.unlocked != null) {
                Text(
                    "🎉 " + stringResource(R.string.skin_unlocked, stringResource(result.unlocked.label)),
                    style = MaterialTheme.typography.titleMedium,
                    color = Kitchen.Shout,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 12.dp).testTag("unlocked")
                )
            } else if (result.croquetasToNext != null) {
                Text(
                    pluralStringResource(R.plurals.skin_next, result.croquetasToNext, result.croquetasToNext),
                    color = Kitchen.Muted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        }
    }
}
