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
import com.jorgelillo.leaguecards.domain.League
import com.jorgelillo.leaguecards.domain.YOU
import com.jorgelillo.leaguecards.ui.theme.Chat

@Composable
fun Page(
    title: String?,
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
    bottom: @Composable ColumnScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) = LilloPage(title, onBack, Chat.Background, Chat.Text, modifier, actions = actions, bottom = bottom, content = content)

@Composable
fun BigButton(text: String, onClick: () -> Unit, tag: String, modifier: Modifier = Modifier, color: Color = Chat.Green) =
    LilloBigButton(text, onClick, color, Chat.Ink, modifier.testTag(tag))

/** A member's name colour; the player is always green, like your own messages. */
fun League.colorOf(id: String): Color =
    if (id == YOU) Chat.Green else character(id)?.color?.let { runCatching { Color(it.toColorInt()) }.getOrNull() } ?: Chat.Muted

@Composable
fun League.nameOf(id: String): String = if (id == YOU) stringResource(R.string.you) else character(id)?.name ?: id

/** A group-chat message: the sender's name in their colour over the text. */
@Composable
fun ChatBubble(league: League, from: String, text: String, modifier: Modifier = Modifier, time: String? = null) {
    Column(
        modifier
            .widthIn(max = 320.dp)
            .background(Chat.Bubble, RoundedCornerShape(topStart = 4.dp, topEnd = 14.dp, bottomStart = 14.dp, bottomEnd = 14.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        if (from.isNotEmpty()) {
            Text(league.nameOf(from), color = league.colorOf(from), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
        }
        Text(text, style = MaterialTheme.typography.bodyLarge, color = Chat.Text)
        if (time != null) {
            Text(time, style = MaterialTheme.typography.labelSmall, color = Chat.Muted, modifier = Modifier.align(Alignment.End))
        }
    }
}

/** A yellow "system" notice in the middle of the chat (an event of the matchday). */
@Composable
fun SystemNotice(text: String, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        Text(
            text,
            color = Chat.SystemText,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .background(Chat.System, RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp),
        )
    }
}
