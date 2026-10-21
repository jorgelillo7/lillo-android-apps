package com.jorgelillo.leaguecards.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.jorgelillo.core.designsystem.AvatarStyle
import com.jorgelillo.core.designsystem.LilloAvatar
import com.jorgelillo.core.designsystem.isShortScreen
import com.jorgelillo.leaguecards.R
import com.jorgelillo.leaguecards.domain.Card
import com.jorgelillo.leaguecards.domain.Effects
import com.jorgelillo.leaguecards.domain.Game
import com.jorgelillo.leaguecards.domain.League
import com.jorgelillo.leaguecards.domain.Meter
import com.jorgelillo.leaguecards.domain.Season
import com.jorgelillo.leaguecards.ui.theme.Chat
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

/** One card at a time: swipe it (or tap a button) to answer; the four meters on top. */
@Composable
internal fun PlayScreen(
    league: League,
    season: Season,
    onChoose: (right: Boolean) -> Unit,
    onJoker: () -> Unit,
    onTable: () -> Unit,
    onBack: () -> Unit
) {
    val card = season.currentCard?.let(league::card) ?: return
    val drag = remember(season.matchday, season.handIndex) { Animatable(0f) }
    val leaning = when {
        drag.value > 24f -> card.right
        drag.value < -24f -> card.left
        else -> null
    }
    val short = isShortScreen()
    val points = @Composable {
        if (season.matchdayPoints != 0) {
            Text(
                stringResource(R.string.card_points, "%+d".format(season.matchdayPoints)),
                color = if (season.matchdayPoints > 0) Chat.Green else Chat.Red,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                textAlign = TextAlign.Center
            )
        }
    }
    val news = @Composable {
        season.justUnlocked.forEach { id -> league.achievement(id)?.let { AchievementBanner(it) } }
        season.lastOutcome?.let { ChatBubble(league, it.character, it.text, Modifier.testTag("outcome")) }
    }
    Page(
        title = stringResource(R.string.matchday_of, season.matchday, league.matchdays),
        onBack = onBack,
        actions = {
            TextButton(onClick = onTable, modifier = Modifier.testTag("table")) {
                Text(
                    stringResource(R.string.position_short, Game.position(season)),
                    color = Chat.Text,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        sideActionsWhenShort = true,
        bottom = {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ChoiceButton(
                    "← " + card.left.label,
                    "choose_left",
                    onClick = { onChoose(false) },
                    modifier = Modifier.weight(1f)
                )
                ChoiceButton(
                    card.right.label + " →",
                    "choose_right",
                    onClick = { onChoose(true) },
                    modifier = Modifier.weight(1f)
                )
            }
            if (Game.canUseJoker(league, season)) {
                val joker = league.joker!!
                TextButton(onClick = onJoker, modifier = Modifier.fillMaxWidth().testTag("joker")) {
                    Text("🃏 " + joker.label + " · " + stringResource(R.string.joker_once), color = Chat.Gold)
                }
            }
        }
    ) {
        if (short) {
            // Landscape and handhelds: meters and news on the left, the card at full height beside them.
            Row(Modifier.weight(1f).padding(bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                    MetersRow(season, leaning?.effects)
                    Spacer(Modifier.height(8.dp))
                    news()
                    points()
                }
                SwipeCard(league, card, drag, leaning?.label, onChoose, Modifier.weight(1f).fillMaxHeight())
            }
        } else {
            MetersRow(season, leaning?.effects)
            Spacer(Modifier.height(12.dp))
            news()
            Spacer(Modifier.height(12.dp))
            SwipeCard(league, card, drag, leaning?.label, onChoose, Modifier.weight(1f))
            points()
        }
    }
}

@Composable
private fun ChoiceButton(text: String, tag: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(onClick = onClick, modifier = modifier.height(56.dp).testTag(tag)) {
        Text(text, color = Chat.Text, maxLines = 2, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center)
    }
}

private val MeterIcons = mapOf(Meter.MONEY to "💰", Meter.SQUAD to "💪", Meter.PATIENCE to "🙄", Meter.TEARS to "😭")
private val MeterColors = mapOf(
    Meter.MONEY to Chat.Gold,
    Meter.SQUAD to Chat.Green,
    Meter.PATIENCE to Color(0xFFA791F5),
    Meter.TEARS to Chat.Tear
)

/**
 * The four meters as tiles that fill up from the bottom (like Reigns), red near either end; while
 * a card leans to one side, a dot marks the meters that choice moves (bigger = moves more).
 */
@Composable
private fun MetersRow(season: Season, preview: Effects?) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Meter.entries.forEach { meter ->
            val change = preview?.let {
                when (meter) {
                    Meter.MONEY -> it.money
                    Meter.SQUAD -> it.squad
                    Meter.PATIENCE -> it.patience
                    Meter.TEARS -> it.tears
                }
            } ?: 0
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f).testTag("meter_${meter.name.lowercase()}")
            ) {
                Box(Modifier.height(12.dp), contentAlignment = Alignment.Center) {
                    Box(
                        Modifier
                            .size(if (abs(change) >= 10) 10.dp else 6.dp)
                            .alpha(if (change != 0) 1f else 0f)
                            .background(Chat.Text, CircleShape)
                    )
                }
                MeterTile(MeterIcons.getValue(meter), season.meters[meter], MeterColors.getValue(meter))
                Text(
                    stringResource(MeterLabels.getValue(meter)),
                    color = Chat.Muted,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

private val MeterLabels = mapOf(
    Meter.MONEY to R.string.meter_money,
    Meter.SQUAD to R.string.meter_squad,
    Meter.PATIENCE to R.string.meter_patience,
    Meter.TEARS to R.string.meter_tears
)

@Composable
private fun MeterTile(icon: String, value: Int, color: Color) {
    val danger = value <= 15 || value >= 85
    val shape = RoundedCornerShape(16.dp)
    Box(
        Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(Chat.Bubble, shape)
            .border(1.5.dp, if (danger) Chat.Red else color.copy(alpha = 0.35f), shape),
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            Modifier.fillMaxWidth().fillMaxHeight(value / 100f).background(
                (if (danger) Chat.Red else color).copy(alpha = 0.55f),
                shape
            )
        )
        Text(icon, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.align(Alignment.Center))
    }
}

@Composable
private fun SwipeCard(
    league: League,
    card: Card,
    drag: Animatable<Float, *>,
    leaningLabel: String?,
    onChoose: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val color = league.colorOf(card.character)
    val character = league.character(card.character)
    BoxWithConstraints(modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        val threshold = with(LocalDensity.current) { maxWidth.toPx() } * 0.28f
        Column(
            Modifier
                .fillMaxSize()
                .offset { IntOffset(drag.value.roundToInt(), 0) }
                .graphicsLayer { rotationZ = drag.value / 30f }
                .background(Chat.Raised, RoundedCornerShape(24.dp))
                .border(2.dp, color.copy(alpha = 0.6f), RoundedCornerShape(24.dp))
                .pointerInput(card.id) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            scope.launch {
                                if (abs(drag.value) > threshold) {
                                    val right = drag.value > 0
                                    drag.animateTo(if (right) threshold * 4 else -threshold * 4)
                                    onChoose(right)
                                } else {
                                    drag.animateTo(0f)
                                }
                            }
                        },
                        onDragCancel = { scope.launch { drag.animateTo(0f) } }
                    ) { change, amount ->
                        change.consume()
                        scope.launch { drag.snapTo(drag.value + amount) }
                    }
                }
                .testTag("card")
                .padding(if (isShortScreen()) 16.dp else 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // The answer the card leans to while dragged; the room is kept when there is none.
            Box(Modifier.fillMaxWidth().height(48.dp), contentAlignment = Alignment.Center) {
                if (leaningLabel != null) {
                    Text(
                        leaningLabel,
                        color = Chat.Gold,
                        fontWeight = FontWeight.Black,
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }
            Avatar(
                character?.emoji?.ifBlank { null } ?: character?.name ?: card.character,
                color,
                size = if (isShortScreen()) 48 else 72
            )
            Spacer(Modifier.height(10.dp))
            Text(
                character?.name ?: card.character,
                color = color,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge
            )
            character?.tagline?.takeIf { it.isNotBlank() }?.let {
                Text(it, color = Chat.Muted, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
            }
            Spacer(Modifier.weight(1f))
            Text(
                card.text,
                style = if (isShortScreen()) {
                    MaterialTheme.typography.titleLarge
                } else {
                    MaterialTheme.typography.headlineSmall
                },
                textAlign = TextAlign.Center,
                color = Chat.Text
            )
            Spacer(Modifier.weight(1f))
        }
    }
}

/** A round avatar with the member's emoji (or initial) in their colour. */
@Composable
internal fun Avatar(name: String, color: Color, size: Int = 72) =
    LilloAvatar(name, color, size.dp, style = AvatarStyle.Tinted)
