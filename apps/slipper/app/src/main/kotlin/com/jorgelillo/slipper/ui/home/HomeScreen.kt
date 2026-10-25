package com.jorgelillo.slipper.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jorgelillo.core.designsystem.AboutTexts
import com.jorgelillo.core.designsystem.LilloAboutSheet
import com.jorgelillo.core.designsystem.LilloSecondaryButton
import com.jorgelillo.core.platform.LilloLinks
import com.jorgelillo.slipper.BuildConfig
import com.jorgelillo.slipper.R
import com.jorgelillo.slipper.data.Saved
import com.jorgelillo.slipper.domain.Cat
import com.jorgelillo.slipper.domain.Chunk
import com.jorgelillo.slipper.domain.Croqueta
import com.jorgelillo.slipper.domain.Furniture
import com.jorgelillo.slipper.domain.FurnitureKind
import com.jorgelillo.slipper.domain.GameConfig
import com.jorgelillo.slipper.domain.Kid
import com.jorgelillo.slipper.domain.PowerKind
import com.jorgelillo.slipper.domain.PowerUp
import com.jorgelillo.slipper.domain.Room
import com.jorgelillo.slipper.domain.Slipper
import com.jorgelillo.slipper.domain.SlipperKind
import com.jorgelillo.slipper.domain.SlipperSkin
import com.jorgelillo.slipper.domain.World
import com.jorgelillo.slipper.ui.BigButton
import com.jorgelillo.slipper.ui.Page
import com.jorgelillo.slipper.ui.formatScore
import com.jorgelillo.slipper.ui.game.GameScene
import com.jorgelillo.slipper.ui.game.GameSprites
import com.jorgelillo.slipper.ui.game.rememberGameSprites
import com.jorgelillo.slipper.ui.theme.Kitchen

/** A still of a run for the home screen: the kid leaping onto the table, grandma close behind. */
internal val PosterWorld = World(
    seed = 0,
    time = 9.0,
    distance = 40.0,
    kid = Kid(y = 13.0, vy = 10.0, airTime = 0.2),
    gap = 11.0,
    chunks = listOf(Chunk(0, -100.0, 300.0, Room.KITCHEN)),
    furniture = listOf(Furniture(FurnitureKind.TABLE, 50.0), Furniture(FurnitureKind.CHAIR, 75.0)),
    slippers = listOf(Slipper(33.0, SlipperKind.LOW, GameConfig.SLIPPER_SPEED, spin = 0.0)),
    cats = listOf(Cat(88.0)),
    powerUps = listOf(PowerUp(PowerKind.SANDWICH, 70.0, 18.0)),
    croquetas = listOf(Croqueta(54.0, 10.5), Croqueta(60.0, 10.5), Croqueta(66.0, 10.5), Croqueta(77.0, 14.0))
)

@Composable
internal fun HomeScreen(
    saved: Saved,
    today: Long,
    challenge: Int?,
    onPlay: () -> Unit,
    onDaily: () -> Unit,
    onDismissChallenge: () -> Unit,
    onSkin: (SlipperSkin) -> Unit,
    onToggleSound: () -> Unit,
    onAbout: () -> Unit
) {
    val sprites = rememberGameSprites()
    Page(
        actions = {
            val sound = stringResource(if (saved.muted) R.string.sound_off else R.string.sound_on)
            TextButton(
                onClick = onToggleSound,
                modifier = Modifier.testTag("sound").semantics { contentDescription = sound }
            ) {
                Text(if (saved.muted) "🔇" else "🔊", style = MaterialTheme.typography.titleLarge)
            }
            TextButton(onClick = onAbout, modifier = Modifier.testTag("about")) {
                Text(stringResource(R.string.about), color = Kitchen.Muted)
            }
        },
        bottom = {
            BigButton(stringResource(R.string.play), onPlay, "play")
            val best = saved.dailyBest(today)
            LilloSecondaryButton(
                "📅 " + stringResource(R.string.daily) +
                    if (best > 0) " · " + stringResource(R.string.daily_best, formatScore(best)) else "",
                onDaily,
                Modifier.padding(top = 8.dp).testTag("daily"),
                contentColor = Kitchen.Ink
            )
        }
    ) {
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
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
            GameScene(
                remember { PosterWorld },
                sprites,
                Modifier.fillMaxWidth().aspectRatio(1.4f).padding(vertical = 8.dp).clip(RoundedCornerShape(20.dp)),
                skin = saved.skin
            )
            if (saved.best > 0) {
                Text(
                    stringResource(R.string.best_score, formatScore(saved.best)),
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
            SkinPicker(saved, sprites, onSkin)
            Spacer(Modifier.height(8.dp))
        }
    }
}

/** The slipper she throws: unlocked with croquetas eaten over all runs; the locked ones say how many. */
@Composable
private fun SkinPicker(saved: Saved, sprites: GameSprites, onSkin: (SlipperSkin) -> Unit) = Column {
    Text(
        stringResource(R.string.skins_title),
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = Kitchen.Muted,
        modifier = Modifier.padding(top = 12.dp, bottom = 6.dp)
    )
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        SlipperSkin.entries.forEach { skin ->
            val unlocked = skin.unlocked(saved.croquetas)
            val selected = saved.skin == skin
            val name = stringResource(skin.label)
            Column(
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (selected) Kitchen.Raised else Kitchen.Raised.copy(alpha = 0.5f))
                    .border(2.dp, if (selected) Kitchen.Green else Color.Transparent, RoundedCornerShape(16.dp))
                    .clickable(enabled = unlocked) { onSkin(skin) }
                    .padding(vertical = 8.dp)
                    .testTag("skin_${skin.name.lowercase()}"),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val slipper = sprites.slipper(skin)
                Canvas(Modifier.size(44.dp, 22.dp).alpha(if (unlocked) 1f else 0.35f)) {
                    val pixel = size.width / slipper.width
                    slipper.draw(this, 0f, size.height / 2 + slipper.height * pixel / 2, pixel)
                }
                Text(
                    if (unlocked) {
                        name
                    } else {
                        "🔒 " +
                            pluralStringResource(R.plurals.skin_locked, skin.unlockAt, skin.unlockAt)
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = Kitchen.Ink,
                    textAlign = TextAlign.Center,
                    maxLines = 2
                )
            }
        }
    }
}

internal val SlipperSkin.label: Int get() = when (this) {
    SlipperSkin.CLASSIC -> R.string.skin_classic
    SlipperSkin.CHECKED -> R.string.skin_checked
    SlipperSkin.FLIP_FLOP -> R.string.skin_flip_flop
    SlipperSkin.BUNNY -> R.string.skin_bunny
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
        rate = stringResource(R.string.about_rate),
        privacy = stringResource(R.string.about_open_privacy)
    ),
    privacyUrl = LilloLinks.privacyPolicy("slipper"),
    playStoreUrl = LilloLinks.playStore(BuildConfig.APPLICATION_ID),
    onDismiss = onDismiss
)
