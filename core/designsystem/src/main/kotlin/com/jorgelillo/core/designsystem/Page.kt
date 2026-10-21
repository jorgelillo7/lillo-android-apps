package com.jorgelillo.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Full-screen page: background, a header with an optional back button (test tag "back"), title
 * and actions, the content, and a bottom action area. Apps wrap it with their own colours.
 *
 * [sideActionsWhenShort]: on short screens (landscape, handhelds) the actions go in a column beside
 * the content instead of below it, and a page with no title or back button drops its header, so
 * the content keeps the screen's full height. Off by default: only for screens checked that way.
 */
@Composable
fun LilloPage(
    title: String?,
    onBack: (() -> Unit)?,
    background: Brush,
    contentColor: Color,
    modifier: Modifier = Modifier,
    titleWeight: FontWeight = FontWeight.Bold,
    actions: @Composable RowScope.() -> Unit = {},
    sideActionsWhenShort: Boolean = false,
    bottom: @Composable ColumnScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) = CompositionLocalProvider(LocalContentColor provides contentColor) {
    val side = sideActionsWhenShort && isShortScreen()
    Column(modifier.fillMaxSize().background(background).statusBarsPadding().navigationBarsPadding()) {
        if (!side || title != null || onBack != null) {
            Row(Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                if (onBack != null) {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("back")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.lillo_back))
                    }
                } else {
                    Spacer(Modifier.width(12.dp))
                }
                Text(
                    title.orEmpty(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = titleWeight,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                actions()
            }
        }
        if (side) {
            Row(Modifier.weight(1f).fillMaxWidth()) {
                Column(Modifier.weight(1f).fillMaxHeight().padding(horizontal = 20.dp), content = content)
                Column(
                    Modifier.width(SIDE_ACTIONS_WIDTH).fillMaxHeight().padding(20.dp),
                    verticalArrangement = Arrangement.Center,
                    content = bottom
                )
            }
        } else {
            Column(Modifier.weight(1f).fillMaxWidth().padding(horizontal = 20.dp), content = content)
            Column(Modifier.fillMaxWidth().padding(20.dp), content = bottom)
        }
    }
}

private val SIDE_ACTIONS_WIDTH = 300.dp

/** The main call to action: full width, tall, bold. */
@Composable
fun LilloBigButton(
    text: String,
    onClick: () -> Unit,
    color: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    height: Dp = 60.dp,
    fontSize: TextUnit = 20.sp,
    shape: Shape = ButtonDefaults.shape
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth().height(height),
        shape = shape,
        colors = ButtonDefaults.buttonColors(containerColor = color, contentColor = contentColor)
    ) { Text(text, fontSize = fontSize, fontWeight = FontWeight.Black) }
}

/**
 * Exposes test tags as resource ids so `scripts/tap-text.sh` (UI Automator) finds them. Apply it
 * to the app's root and to every dialog or sheet: they live in their own window.
 */
@OptIn(ExperimentalComposeUiApi::class)
val TestTagsAsResourceIds = Modifier.semantics { testTagsAsResourceId = true }

/**
 * Landscape phones and handhelds (e.g. AYN Thor): too short for the full vertical layouts. Measures
 * the app's window, so split screen and freeform windows count too.
 */
@Composable
fun isShortScreen(): Boolean = with(LocalDensity.current) { LocalWindowInfo.current.containerSize.height.toDp() } < SHORT_SCREEN_DP.dp

private const val SHORT_SCREEN_DP = 480
