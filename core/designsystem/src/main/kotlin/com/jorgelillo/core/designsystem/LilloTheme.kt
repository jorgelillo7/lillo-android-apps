package com.jorgelillo.core.designsystem

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Base theme shared by all apps. Each app passes its own [colorScheme]; typography and shapes
 * stay consistent across the portfolio.
 */
@Composable
fun LilloTheme(colorScheme: ColorScheme = darkColorScheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = colorScheme,
        typography = LilloTypography,
        shapes = LilloShapes,
        content = content
    )
}

private val Base = Typography()

/** Tabular figures keep counters from jittering as digits change. */
private const val TABULAR_NUMBERS = "tnum"

val LilloTypography = Base.copy(
    displayLarge = Base.displayLarge.copy(fontWeight = FontWeight.Black, fontFeatureSettings = TABULAR_NUMBERS),
    displayMedium = Base.displayMedium.copy(fontWeight = FontWeight.ExtraBold, fontFeatureSettings = TABULAR_NUMBERS),
    displaySmall = Base.displaySmall.copy(fontWeight = FontWeight.ExtraBold, fontFeatureSettings = TABULAR_NUMBERS),
    headlineMedium = Base.headlineMedium.copy(fontWeight = FontWeight.Bold),
    titleLarge = Base.titleLarge.copy(fontWeight = FontWeight.Bold),
    labelSmall = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.2.sp)
)

val LilloShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp)
)
