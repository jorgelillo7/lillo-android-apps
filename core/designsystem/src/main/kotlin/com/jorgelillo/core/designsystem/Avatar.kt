package com.jorgelillo.core.designsystem

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * The first thing a person would read in [name], upper-cased: a whole emoji (even one built from
 * several code points), an accented letter, or "?" for a blank name.
 */
fun avatarInitial(name: String): String {
    val clean = name.trim()
    if (clean.isEmpty()) return "?"
    val end = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        android.icu.text.BreakIterator.getCharacterInstance().apply { setText(clean) }.next()
    } else {
        java.text.BreakIterator.getCharacterInstance().apply { setText(clean) }.next()
    }
    return clean.substring(0, end).uppercase()
}

/** How a [LilloAvatar] is painted. */
enum class AvatarStyle {
    /** Solid circle in the colour, the initial in [LilloAvatar]'s contentColor. */
    Filled,

    /** Faint circle with a ring in the colour, the initial in the colour too. */
    Tinted,
}

/** A person as a circle with their initial (or emoji), in their colour. [ring] adds an outline. */
@Composable
fun LilloAvatar(
    name: String,
    color: Color,
    size: Dp,
    modifier: Modifier = Modifier,
    style: AvatarStyle = AvatarStyle.Filled,
    contentColor: Color = Color.Black,
    ring: Color? = null,
) {
    val ringColor = ring ?: color.takeIf { style == AvatarStyle.Tinted }
    val ringWidth = if (ring != null) size / 16 else 2.dp
    Box(
        modifier
            .size(size)
            .clip(CircleShape)
            .background(if (style == AvatarStyle.Filled) color else color.copy(alpha = 0.25f))
            .then(if (ringColor != null) Modifier.border(ringWidth, ringColor, CircleShape) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            avatarInitial(name),
            color = if (style == AvatarStyle.Filled) contentColor else color,
            fontSize = (size.value * 0.45f).sp,
            fontWeight = FontWeight.Black,
        )
    }
}
