package com.jorgelillo.slipper.ui

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.jorgelillo.core.designsystem.LilloBigButton
import com.jorgelillo.core.designsystem.LilloPage
import com.jorgelillo.slipper.ui.theme.Kitchen
import java.text.NumberFormat

@Composable
internal fun Page(
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
    /** Lists and text keep a readable width on tablets (see [LilloPage]); off for brackets and canvases. */
    readable: Boolean = true,
    bottom: @Composable ColumnScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) = LilloPage(
    title = null,
    onBack = null,
    background = Kitchen.Background,
    modifier = modifier,
    actions = actions,
    sideActionsWhenShort = true,
    readable = readable,
    bottom = bottom,
    content = content
)

@Composable
internal fun BigButton(text: String, onClick: () -> Unit, tag: String, modifier: Modifier = Modifier) =
    LilloBigButton(text, onClick, modifier = modifier.testTag(tag))

/** 1240 → "1,240" or "1.240", as the phone's language writes it. */
internal fun formatScore(score: Int): String = NumberFormat.getIntegerInstance().format(score)
