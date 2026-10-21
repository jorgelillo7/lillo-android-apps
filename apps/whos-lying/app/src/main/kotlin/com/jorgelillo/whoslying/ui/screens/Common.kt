package com.jorgelillo.whoslying.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jorgelillo.core.designsystem.LilloAvatar
import com.jorgelillo.core.designsystem.LilloBigButton
import com.jorgelillo.core.designsystem.LilloPage
import com.jorgelillo.whoslying.R
import com.jorgelillo.whoslying.ui.theme.Neon

/** Full-screen page with the neon backdrop, an optional back button and a bottom action area. */
@Composable
internal fun Page(
    title: String?,
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
    background: Brush = Neon.Backdrop,
    actions: @Composable RowScope.() -> Unit = {},
    /** Short screens: actions beside the content (see [LilloPage]). */
    sideActionsWhenShort: Boolean = false,
    bottom: @Composable ColumnScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) = LilloPage(
    title,
    onBack,
    background,
    modifier = modifier,
    actions = actions,
    sideActionsWhenShort = sideActionsWhenShort,
    bottom = bottom,
    content = content
)

@Composable
internal fun BigButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    color: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary
) = LilloBigButton(text, onClick, modifier, color = color, contentColor = contentColor, enabled = enabled)

/** Player colors for avatars, by position in the group. */
val avatarColors = listOf(Neon.Pink, Neon.Turquoise, Neon.VioletLight, Neon.Amber)

/** A player's avatar in their group colour (by position in [players]). */
@Composable
internal fun Avatar(
    player: String,
    players: List<String>,
    size: Dp,
    modifier: Modifier = Modifier,
    ring: Color? = null
) = LilloAvatar(
    player,
    avatarColors[players.indexOf(player).coerceAtLeast(0) % avatarColors.size],
    size,
    modifier,
    contentColor = Neon.Night,
    ring = ring
)

/** Big emoji used as illustration (no image assets needed). */
@Composable
internal fun Emoji(value: String, modifier: Modifier = Modifier, size: Int = 72) {
    Box(modifier, contentAlignment = Alignment.Center) { Text(value, fontSize = size.sp) }
}
