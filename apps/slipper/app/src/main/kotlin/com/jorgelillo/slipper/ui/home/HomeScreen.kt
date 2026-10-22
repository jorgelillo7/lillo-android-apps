package com.jorgelillo.slipper.ui.home

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jorgelillo.core.designsystem.AboutTexts
import com.jorgelillo.core.designsystem.LilloAboutSheet
import com.jorgelillo.core.platform.LilloLinks
import com.jorgelillo.slipper.BuildConfig
import com.jorgelillo.slipper.R
import com.jorgelillo.slipper.domain.Croqueta
import com.jorgelillo.slipper.domain.GameConfig
import com.jorgelillo.slipper.domain.Kid
import com.jorgelillo.slipper.domain.Slipper
import com.jorgelillo.slipper.domain.SlipperKind
import com.jorgelillo.slipper.domain.World
import com.jorgelillo.slipper.ui.BigButton
import com.jorgelillo.slipper.ui.Page
import com.jorgelillo.slipper.ui.formatScore
import com.jorgelillo.slipper.ui.game.GameScene
import com.jorgelillo.slipper.ui.game.rememberGameSprites
import com.jorgelillo.slipper.ui.theme.Kitchen

/** A still of a run for the home screen: the kid mid-jump over a slipper, croquetas ahead. */
internal val PosterWorld = World(
    seed = 0,
    time = 9.0,
    kid = Kid(y = 9.0, vy = 10.0),
    slippers = listOf(Slipper(50.0, SlipperKind.LOW, GameConfig.SLIPPER_SPEED, spin = 0.0)),
    croquetas = listOf(Croqueta(78.0, 18.0), Croqueta(86.0, 20.0), Croqueta(94.0, GameConfig.LOW_Y)),
    scrolled = 3.0
)

@Composable
internal fun HomeScreen(
    best: Int,
    challenge: Int?,
    onPlay: () -> Unit,
    onDismissChallenge: () -> Unit,
    onAbout: () -> Unit
) {
    val sprites = rememberGameSprites()
    Page(
        actions = {
            TextButton(onClick = onAbout, modifier = Modifier.testTag("about")) {
                Text(stringResource(R.string.about), color = Kitchen.Muted)
            }
        },
        bottom = { BigButton(stringResource(R.string.play), onPlay, "play") }
    ) {
        Text(
            stringResource(R.string.app_name),
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Black,
            color = Kitchen.Shout,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        )
        Text(
            stringResource(R.string.home_tagline),
            color = Kitchen.Muted,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        )
        GameScene(remember { PosterWorld }, sprites, Modifier.fillMaxWidth().aspectRatio(1.6f).padding(vertical = 8.dp))
        if (best > 0) {
            Text(
                stringResource(R.string.best_score, formatScore(best)),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Kitchen.Ink,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().testTag("best")
            )
        }
        if (challenge != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().testTag("challenge_banner")
            ) {
                Text(
                    stringResource(R.string.challenge_banner, formatScore(challenge)),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Kitchen.Shout,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDismissChallenge, modifier = Modifier.testTag("dismiss_challenge")) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = stringResource(R.string.dismiss_challenge),
                        tint = Kitchen.Muted
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
internal fun AboutSheet(onDismiss: () -> Unit) = LilloAboutSheet(
    icon = painterResource(R.drawable.ic_launcher_foreground),
    iconBackground = Kitchen.Tile,
    containerColor = Kitchen.Raised,
    texts = AboutTexts(
        title = stringResource(R.string.app_name),
        version = stringResource(R.string.about_version, BuildConfig.VERSION_NAME),
        body = stringResource(R.string.about_body),
        rate = null,
        privacy = stringResource(R.string.about_open_privacy)
    ),
    privacyUrl = LilloLinks.privacyPolicy("slipper"),
    playStoreUrl = null, // not on Google Play yet
    onDismiss = onDismiss
)
