package com.jorgelillo.leaguecards.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jorgelillo.core.designsystem.AvatarStyle
import com.jorgelillo.core.designsystem.LilloAvatar
import com.jorgelillo.core.designsystem.isShortScreen
import com.jorgelillo.core.designsystem.isWideScreen
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
    val wide = isWideScreen()
    val points = @Composable {
        if (season.matchdayPoints != 0) {
            // One line at most: on small phones every line taken here is one the card loses.
            Text(
                stringResource(R.string.card_points, "%+d".format(season.matchdayPoints)),
                color = if (season.matchdayPoints > 0) Chat.Green else Chat.Red,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                textAlign = TextAlign.Center,
                maxLines = 1,
                autoSize = TextAutoSize.StepBased(minFontSize = 10.sp, maxFontSize = 16.sp)
            )
        }
    }
    val news = @Composable {
        season.justUnlocked.forEach { id -> league.achievement(id)?.let { AchievementBanner(it) } }
        season.lastOutcome?.let {
            ChatBubble(league, it.character, it.text, Modifier.testTag("outcome"), small = lowScreen())
        }
    }
    // The two answers (and the joker): under the card, or beside it on short screens.
    val answers = @Composable {
        val left = @Composable { m: Modifier ->
            ChoiceButton("← " + card.left.label, "choose_left", onClick = { onChoose(false) }, modifier = m)
        }
        val right = @Composable { m: Modifier ->
            ChoiceButton(card.right.label + " →", "choose_right", onClick = { onChoose(true) }, modifier = m)
        }
        if (short) {
            // The side column is narrow: one answer per row, so long labels (and large fonts) fit.
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                right(Modifier.fillMaxWidth())
                left(Modifier.fillMaxWidth())
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                left(Modifier.weight(1f))
                right(Modifier.weight(1f))
            }
        }
        if (Game.canUseJoker(league, season)) {
            val joker = league.joker!!
            TextButton(onClick = onJoker, modifier = Modifier.fillMaxWidth().testTag("joker")) {
                Text("🃏 " + joker.label + " · " + stringResource(R.string.joker_once), color = Chat.Gold)
            }
        }
    }
    Page(
        title = stringResource(R.string.matchday_of, season.matchday, season.matchdays(league)),
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
        // Wide screens: the answers go under the card, in its pane.
        readable = !wide,
        bottom = { if (!wide) answers() }
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
        } else if (wide) {
            // Tablets in landscape: meters, the chat and the table on the left; the card and its answers
            // on the right.
            Row(Modifier.weight(1f).padding(bottom = 20.dp), horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                    MetersRow(season, leaning?.effects)
                    Spacer(Modifier.height(16.dp))
                    news()
                    points()
                    Text(
                        stringResource(R.string.table),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Chat.Text,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )
                    Standings(league, season)
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SwipeCard(league, card, drag, leaning?.label, onChoose, Modifier.weight(1f))
                    answers()
                }
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
    // Grows with the label instead of clipping it (large font sizes on handhelds).
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = 56.dp).testTag(tag),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(text, color = Chat.Text, textAlign = TextAlign.Center)
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
                val value = season.meters[meter]
                MeterTile(MeterIcons.getValue(meter), value, MeterColors.getValue(meter))
                Text(
                    value.toString(),
                    color = if (value.inDanger()) Chat.Red else Chat.Text,
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Text(
                    stringResource(MeterLabels.getValue(meter)),
                    color = Chat.Text,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    autoSize = TextAutoSize.StepBased(minFontSize = 8.sp, maxFontSize = 11.sp)
                )
            }
        }
    }
}

/** Phones under 760dp tall (or with a large font, which eats the same room): smaller meters. */
@Composable
private fun lowScreen(): Boolean =
    with(LocalDensity.current) { LocalWindowInfo.current.containerSize.height.toDp() * (1f / fontScale) } <
        LOW_SCREEN_DP.dp

private const val LOW_SCREEN_DP = 760

/** Under this height the card drops its tagline and puts the avatar beside the name. */
private val COMPACT_CARD = 420.dp

/** The card's text never gets smaller than this; past it, it scrolls inside the card. */
private val MIN_CARD_TEXT = 14.sp

private fun Int.inDanger() = this <= 15 || this >= 85

private val MeterLabels = mapOf(
    Meter.MONEY to R.string.meter_money,
    Meter.SQUAD to R.string.meter_squad,
    Meter.PATIENCE to R.string.meter_patience,
    Meter.TEARS to R.string.meter_tears
)

/**
 * A tile that fills up from the bottom on a dark background, with a bright edge on top of the fill,
 * so the level reads at a glance even behind the emoji (the number goes under it). Red near either end.
 */
@Composable
private fun MeterTile(icon: String, value: Int, color: Color) {
    val fill = if (value.inDanger()) Chat.Red else color
    val shape = RoundedCornerShape(14.dp)
    Box(
        Modifier
            .fillMaxWidth()
            .height(if (lowScreen()) 44.dp else 64.dp)
            .clip(shape)
            .background(Chat.Ink)
            .border(2.dp, fill, shape),
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(Modifier.fillMaxWidth().fillMaxHeight(value / 100f)) {
            Box(Modifier.fillMaxWidth().height(3.dp).background(fill))
            Box(Modifier.fillMaxSize().background(fill.copy(alpha = 0.6f)))
        }
        Text(
            icon,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.align(Alignment.Center)
        )
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
        val compact = maxHeight < COMPACT_CARD
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
                .padding(if (isShortScreen() || compact) 16.dp else 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val name = character?.name ?: card.character
            val emoji = character?.emoji?.ifBlank { null } ?: name
            // The answer the card leans to while dragged: its own row on roomy cards, over the
            // header on compact ones.
            val leaning = @Composable { m: Modifier ->
                Box(m.fillMaxWidth().height(if (compact) 32.dp else 48.dp), contentAlignment = Alignment.Center) {
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
            }
            if (compact) {
                Box {
                    Row(
                        Modifier.fillMaxWidth().padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Avatar(emoji, color, size = 40)
                        Spacer(Modifier.size(10.dp))
                        Text(
                            name,
                            color = color,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    if (leaningLabel != null) leaning(Modifier.background(Chat.Raised))
                }
            } else {
                leaning(Modifier)
                Avatar(emoji, color, size = if (isShortScreen()) 48 else 72)
                Spacer(Modifier.height(10.dp))
                Text(name, color = color, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                character?.tagline?.takeIf { it.isNotBlank() }?.let {
                    Text(
                        it,
                        color = Chat.Muted,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }
            val style = MaterialTheme.typography.run { if (isShortScreen()) titleLarge else headlineSmall }
            CardText(card.text, style, Modifier.weight(1f).fillMaxWidth().padding(top = 8.dp))
        }
    }
}

/**
 * The card's text, as big as fits the room left (down to [MIN_CARD_TEXT]); when even that doesn't
 * fit (a long card on a small phone with a large font), it scrolls inside the card: always read whole.
 */
@Composable
private fun CardText(text: String, style: TextStyle, modifier: Modifier = Modifier) {
    val measurer = rememberTextMeasurer()
    BoxWithConstraints(modifier, contentAlignment = Alignment.Center) {
        val needed = measurer.measure(
            text,
            style.copy(fontSize = MIN_CARD_TEXT, textAlign = TextAlign.Center),
            constraints = Constraints(maxWidth = constraints.maxWidth)
        ).size.height
        if (needed > constraints.maxHeight) {
            val scroll = rememberScrollState()
            Text(
                text,
                style = style.copy(fontSize = MIN_CARD_TEXT, lineHeight = MIN_CARD_TEXT * 1.3f),
                textAlign = TextAlign.Center,
                color = Chat.Text,
                modifier = Modifier.fillMaxSize().verticalScroll(scroll).testTag("card_text")
            )
            // There is more below: the last line fades out until it is scrolled to.
            if (scroll.canScrollForward) {
                Box(
                    Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(28.dp)
                        .background(Brush.verticalGradient(listOf(Color.Transparent, Chat.Raised)))
                )
            }
        } else {
            Text(
                text,
                style = style,
                textAlign = TextAlign.Center,
                color = Chat.Text,
                autoSize = TextAutoSize.StepBased(minFontSize = MIN_CARD_TEXT, maxFontSize = style.fontSize),
                modifier = Modifier.testTag("card_text")
            )
        }
    }
}

/** A round avatar with the member's emoji (or initial) in their colour. */
@Composable
internal fun Avatar(name: String, color: Color, size: Int = 72) =
    LilloAvatar(name, color, size.dp, style = AvatarStyle.Tinted)
