package com.jorgelillo.slipper.ui.over

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jorgelillo.slipper.R
import com.jorgelillo.slipper.ui.BigButton
import com.jorgelillo.slipper.ui.Page
import com.jorgelillo.slipper.ui.formatScore
import com.jorgelillo.slipper.ui.theme.Kitchen

/** The result: the score, a record or the challenge's verdict, and the way to the next run. */
@Composable
internal fun GameOverScreen(
    score: Int,
    previousBest: Int,
    challenge: Int?,
    onRetry: () -> Unit,
    onChallengeFriend: () -> Unit,
    onHome: () -> Unit
) {
    Page(
        bottom = {
            BigButton(stringResource(R.string.retry), onRetry, "retry")
            OutlinedButton(
                onClick = onChallengeFriend,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp).testTag("challenge_friend")
            ) {
                Text(stringResource(R.string.challenge_friend), color = Kitchen.Ink)
            }
            TextButton(onClick = onHome, modifier = Modifier.align(Alignment.CenterHorizontally).testTag("home")) {
                Text(stringResource(R.string.home), color = Kitchen.Muted)
            }
        }
    ) {
        Column(
            Modifier.fillMaxWidth().weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                stringResource(R.string.caught),
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Black,
                color = Kitchen.Shout,
                textAlign = TextAlign.Center
            )
            Text(
                stringResource(R.string.your_score, formatScore(score)),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Kitchen.Ink,
                modifier = Modifier.padding(top = 16.dp).testTag("final_score")
            )
            val verdict = when {
                challenge != null && score > challenge -> stringResource(R.string.challenge_won)
                challenge != null -> stringResource(R.string.challenge_lost, formatScore(challenge))
                score > previousBest && previousBest > 0 -> stringResource(R.string.new_record)
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
        }
    }
}
