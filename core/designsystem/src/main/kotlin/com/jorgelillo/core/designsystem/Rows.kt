package com.jorgelillo.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * A settings-style row on a rounded card: optional [leading] emoji, a title and an optional
 * subtitle, and an optional [trailing] control (a switch, a stepper). Tappable when [onClick] is set.
 */
@Composable
fun LilloSettingRow(
    title: String,
    containerColor: Color,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    subtitleColor: Color = LocalContentColor.current.copy(alpha = 0.7f),
    leading: String? = null,
    borderColor: Color? = null,
    tag: String? = null,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    onClick: (() -> Unit)? = null,
    trailing: (@Composable RowScope.() -> Unit)? = null,
) {
    val shape = MaterialTheme.shapes.large
    Row(
        modifier
            .fillMaxWidth()
            .clip(shape)
            .background(containerColor)
            .then(if (borderColor != null) Modifier.border(1.dp, borderColor, shape) else Modifier)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(contentPadding)
            .then(if (tag != null) Modifier.testTag(tag) else Modifier),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leading != null) Text(leading, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(end = 14.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.bodySmall, color = subtitleColor)
        }
        if (trailing != null) Row(verticalAlignment = Alignment.CenterVertically, content = trailing)
    }
}

/** A small heading over a group of rows or cards. */
@Composable
fun LilloSectionTitle(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    uppercase: Boolean = false,
    style: TextStyle = MaterialTheme.typography.labelLarge,
    fontWeight: FontWeight? = null,
) {
    Text(if (uppercase) text.uppercase() else text, color = color, style = style, fontWeight = fontWeight, modifier = modifier)
}
