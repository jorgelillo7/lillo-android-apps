package com.jorgelillo.leaguecards.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import com.jorgelillo.core.designsystem.LilloBigButton
import com.jorgelillo.core.designsystem.LilloPage
import com.jorgelillo.leaguecards.R
import com.jorgelillo.leaguecards.domain.Achievement
import com.jorgelillo.leaguecards.domain.League
import com.jorgelillo.leaguecards.domain.YOU
import com.jorgelillo.leaguecards.ui.theme.Chat

@Composable
internal fun Page(
    title: String?,
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
    /** Short screens: actions beside the content (see LilloPage). */
    sideActionsWhenShort: Boolean = false,
    /** Lists and text keep a readable width on tablets (see [LilloPage]); off for brackets and canvases. */
    readable: Boolean = true,
    bottom: @Composable ColumnScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) = LilloPage(
    title,
    onBack,
    Chat.Background,
    modifier = modifier,
    actions = actions,
    sideActionsWhenShort = sideActionsWhenShort,
    readable = readable,
    bottom = bottom,
    content = content
)

@Composable
internal fun BigButton(
    text: String,
    onClick: () -> Unit,
    tag: String,
    modifier: Modifier = Modifier,
    color: Color = Chat.Green
) = LilloBigButton(text, onClick, modifier.testTag(tag), color = color)

/** A member's name colour; the player is always green, like your own messages. */
fun League.colorOf(id: String): Color = if (id ==
    YOU
) {
    Chat.Green
} else {
    character(id)?.color?.let { runCatching { Color(it.toColorInt()) }.getOrNull() } ?: Chat.Muted
}

@Composable
fun League.nameOf(id: String): String = if (id == YOU) stringResource(R.string.you) else character(id)?.name ?: id

/** A group-chat message: the sender's name in their colour over the text. */
@Composable
internal fun ChatBubble(
    league: League,
    from: String,
    text: String,
    modifier: Modifier = Modifier,
    time: String? = null,
    /** Smaller text: over the card on low screens, where every line counts. */
    small: Boolean = false
) {
    Column(
        modifier
            .widthIn(max = 320.dp)
            .background(
                Chat.Bubble,
                RoundedCornerShape(topStart = 4.dp, topEnd = 14.dp, bottomStart = 14.dp, bottomEnd = 14.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        if (from.isNotEmpty()) {
            Text(
                league.nameOf(from),
                color = league.colorOf(from),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelLarge
            )
        }
        Text(
            text,
            style = if (small) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge,
            color = Chat.Text
        )
        if (time != null) {
            Text(
                time,
                style = MaterialTheme.typography.labelSmall,
                color = Chat.Muted,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

/** A yellow "system" notice in the middle of the chat (an event of the matchday). */
@Composable
internal fun SystemNotice(text: String, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        Text(
            text,
            color = Chat.SystemText,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .background(Chat.System, RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

/** "New achievement" banner, in gold. */
@Composable
internal fun AchievementBanner(a: Achievement, modifier: Modifier = Modifier) {
    Row(
        modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(Chat.Gold.copy(alpha = 0.16f), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .testTag("achievement_unlocked"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(a.emoji, style = MaterialTheme.typography.headlineSmall)
        Column(Modifier.padding(start = 10.dp)) {
            Text(
                stringResource(R.string.achievement_new),
                color = Chat.GoldText,
                style = MaterialTheme.typography.labelSmall
            )
            Text(a.title, color = Chat.Text, fontWeight = FontWeight.Bold)
        }
    }
}
