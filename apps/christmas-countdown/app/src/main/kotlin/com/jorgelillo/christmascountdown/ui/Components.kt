package com.jorgelillo.christmascountdown.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.jorgelillo.christmascountdown.ui.theme.ChristmasLook
import com.jorgelillo.core.designsystem.LilloColors

/** Translucent "frosted glass" surface for content placed over the app's gradient. */
@Composable
internal fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.medium,
    content: @Composable BoxScope.() -> Unit
) {
    val look = ChristmasLook.current
    Box(
        modifier = modifier
            .clip(shape)
            .background(look.glass)
            .border(1.dp, look.glassBorder, shape)
    ) {
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface) { content() }
    }
}

/** The app's background gradient ([LilloColors]), full screen. */
@Composable
internal fun GradientBackground(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LilloColors.current.background)
    ) {
        // Content sits directly on the gradient, outside any Surface, so set its default text colour here.
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface) { content() }
    }
}
