package com.jorgelillo.grouppolls.ui.theme

import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.jorgelillo.grouppolls.domain.Side

/** Light, rounded and friendly, with two big colours: red against blue. */
object Polls {
    val Paper = Color(0xFFF3F6FB)
    val Card = Color(0xFFFFFFFF)
    val Ink = Color(0xFF1B2433)
    val Muted = Color(0xFF5A6577)
    val Line = Color(0xFFE2E8F1)

    // Red and blue are deep enough for text on their soft tints and for white text on their fills (4.5:1).
    val Red = Color(0xFFC81E3A)
    val RedSoft = Color(0xFFFFE3E6)
    val Blue = Color(0xFF1E50E0)
    val BlueSoft = Color(0xFFE1EAFF)
    val Sky = Color(0xFF3DB7FF)
    val Gold = Color(0xFFFFB627)

    val Header = Brush.verticalGradient(listOf(Color(0xFFDDEBFF), Paper))
    val RedFill = Brush.verticalGradient(listOf(Color(0xFFD93348), Red))
    val BlueFill = Brush.verticalGradient(listOf(Color(0xFF3366F0), Blue))

    fun color(side: Side) = if (side == Side.RED) Red else Blue
    fun soft(side: Side) = if (side == Side.RED) RedSoft else BlueSoft
    fun fill(side: Side) = if (side == Side.RED) RedFill else BlueFill
}

val PollsColorScheme = lightColorScheme(
    primary = Polls.Blue,
    onPrimary = Color.White,
    secondary = Polls.Red,
    onSecondary = Color.White,
    background = Polls.Paper,
    onBackground = Polls.Ink,
    surface = Polls.Card,
    onSurface = Polls.Ink,
    onSurfaceVariant = Polls.Muted,
    surfaceContainerLow = Polls.Card,
    surfaceContainer = Polls.Card,
    surfaceContainerHigh = Polls.Paper,
    outline = Polls.Line,
    error = Polls.Red
)
