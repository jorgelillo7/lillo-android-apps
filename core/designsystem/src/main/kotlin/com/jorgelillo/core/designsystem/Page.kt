package com.jorgelillo.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.style.TextAlign
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
 *
 * [readable]: lists, forms and text keep [LilloReadableWidth] at most, centred, on tablets and
 * unfolded foldables (full width on phones, upright or turned). Off for brackets, canvases and grids, which use every
 * bit of width they get.
 */
@Composable
fun LilloPage(
    title: String?,
    onBack: (() -> Unit)?,
    background: Brush,
    modifier: Modifier = Modifier,
    contentColor: Color = MaterialTheme.colorScheme.onBackground,
    titleWeight: FontWeight = FontWeight.Bold,
    actions: @Composable RowScope.() -> Unit = {},
    sideActionsWhenShort: Boolean = false,
    readable: Boolean = false,
    bottom: @Composable ColumnScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) = CompositionLocalProvider(LocalContentColor provides contentColor) {
    val side = sideActionsWhenShort && isShortScreen()
    // System bars and, in landscape, the camera cutout; the background still draws behind them.
    Column(modifier.fillMaxSize().background(background).windowInsetsPadding(PageInsets)) {
        if (!side || title != null || onBack != null) {
            Row(
                Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onBack != null) {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("back")) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.lillo_back)
                        )
                    }
                } else {
                    Spacer(Modifier.width(12.dp))
                }
                if (title != null) {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = titleWeight,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    // No empty text node: screen readers announce it as an unlabelled item.
                    Spacer(Modifier.weight(1f))
                }
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
            // Not on short screens (phones in landscape): there the width is what they have to spare.
            val width = if (readable && !isShortScreen()) {
                Modifier.align(Alignment.CenterHorizontally).widthIn(max = LilloReadableWidth).fillMaxWidth()
            } else {
                Modifier.fillMaxWidth()
            }
            Column(Modifier.weight(1f).then(width).padding(horizontal = 20.dp), content = content)
            Column(width.padding(20.dp), content = bottom)
        }
    }
}

private val SIDE_ACTIONS_WIDTH = 300.dp

/** The widest a [LilloPage] with `readable` gets: lists, forms and text on tablets. */
val LilloReadableWidth = 640.dp

/** What a page keeps clear of: the system bars and the display cutout (landscape notches). */
private val PageInsets: WindowInsets
    @Composable get() = WindowInsets.systemBars.union(WindowInsets.displayCutout)

/** The main call to action: full width, tall, bold. */
@Composable
fun LilloBigButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
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
 * The second action under a [LilloBigButton]: full width, outlined, at least 52dp tall and growing
 * with its label (large fonts, long texts). [contentColor] is the label's colour.
 */
@Composable
fun LilloSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentColor: Color = MaterialTheme.colorScheme.onBackground
) {
    OutlinedButton(onClick = onClick, modifier = modifier.fillMaxWidth().heightIn(min = 52.dp)) {
        Text(text, color = contentColor, textAlign = TextAlign.Center)
    }
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
fun isShortScreen(): Boolean =
    with(LocalDensity.current) { LocalWindowInfo.current.containerSize.height.toDp() } < SHORT_SCREEN_DP.dp

private const val SHORT_SCREEN_DP = 480
