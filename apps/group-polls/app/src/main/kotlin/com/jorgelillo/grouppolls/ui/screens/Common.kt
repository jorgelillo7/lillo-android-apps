package com.jorgelillo.grouppolls.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jorgelillo.core.designsystem.LilloBigButton
import com.jorgelillo.core.designsystem.LilloPage
import com.jorgelillo.core.designsystem.LilloSectionTitle
import com.jorgelillo.core.designsystem.TestTagsAsResourceIds
import com.jorgelillo.core.platform.LilloLinks
import com.jorgelillo.core.platform.shareText
import com.jorgelillo.grouppolls.BuildConfig
import com.jorgelillo.grouppolls.R
import com.jorgelillo.grouppolls.domain.Poll
import com.jorgelillo.grouppolls.domain.Split
import com.jorgelillo.grouppolls.ui.theme.Polls

const val LINK_BASE = "https://jorgelillo7.github.io/q/?c="
val PRIVACY_POLICY_URL = LilloLinks.privacyPolicy("group-polls")
val PLAY_STORE_URL = LilloLinks.playStore(BuildConfig.APPLICATION_ID)

/** Dialogs and sheets live in their own window: expose their test tags as resource ids too. */
val DialogTags = TestTagsAsResourceIds

/** Full-screen page with a back button and an optional bottom action area. */
@Composable
fun Page(
    title: String?,
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
    background: Brush = Polls.Header,
    actions: @Composable RowScope.() -> Unit = {},
    bottom: @Composable ColumnScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) = LilloPage(title, onBack, background, Polls.Ink, modifier, FontWeight.Black, actions = actions, bottom = bottom, content = content)

@Composable
fun BigButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    color: Color = Polls.Blue,
) = LilloBigButton(text, onClick, color, Color.White, modifier, enabled, height = 56.dp, fontSize = 18.sp, shape = CircleShape)

/** The red/blue bar: proportional to the votes, half and half while nobody has voted. */
@Composable
fun SplitBar(poll: Poll, modifier: Modifier = Modifier, height: Int = 14) {
    val split = Split.of(poll.redVotes, poll.blueVotes)
    val red = if (poll.totalVotes == 0) 0.5f else split.red / 100f
    Row(modifier.fillMaxWidth().height(height.dp).clip(CircleShape)) {
        if (red > 0f) Box(Modifier.weight(red.coerceAtLeast(0.001f)).fillMaxHeight().background(Polls.Red))
        if (red < 1f) Box(Modifier.weight((1 - red).coerceAtLeast(0.001f)).fillMaxHeight().background(Polls.Blue))
    }
}

/** Opens the share sheet with the poll's link. */
fun sharePoll(context: Context, poll: Poll, text: String) = context.shareText("$text\n$LINK_BASE${poll.code}")

/** "2 h", "3 d"… for the feed cards. */
@Composable
fun age(createdAt: Long, now: Long): String {
    val minutes = ((now - createdAt) / 60_000).coerceAtLeast(0)
    return when {
        minutes < 60 -> stringResource(R.string.age_minutes, minutes.coerceAtLeast(1))
        minutes < 24 * 60 -> stringResource(R.string.age_hours, minutes / 60)
        else -> stringResource(R.string.age_days, minutes / (24 * 60))
    }
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) =
    LilloSectionTitle(text, Polls.Muted, modifier.padding(top = 16.dp, bottom = 8.dp), uppercase = true, fontWeight = FontWeight.Bold)

@Composable
fun Dot(color: Color, size: Int = 10) {
    Box(Modifier.width(size.dp).height(size.dp).clip(CircleShape).background(color))
}
