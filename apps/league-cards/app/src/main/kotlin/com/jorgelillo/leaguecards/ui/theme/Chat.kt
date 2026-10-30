package com.jorgelillo.leaguecards.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/** "Group chat at night" palette: the league lives in a chat, so the game looks like one. */
object Chat {
    val Ink = Color(0xFF00110E)
    val InkTop = Color(0xFF00251F)
    val Bubble = Color(0xFF002C25)
    val Mine = Color(0xFF005C4B)
    val Raised = Color(0xFF001E19)
    val Text = Color(0xFFE5E6E3)
    val Muted = Color(0xFFAAABA9)
    val Faint = Color(0x33E9EDEF)

    val Green = Color(0xFF00A884)
    val Gold = Color(0xFFFFC83D)

    /** Gold for text: a little softer, so it doesn't vibrate on the dark background. */
    val GoldText = Color(0xFFF5CF6E)

    /** Green for text: a little softer, so it doesn't vibrate on the dark background. */
    val GreenText = Color(0xFF5FD8BC)
    val Tear = Color(0xFF53BDEB)
    val Red = Color(0xFFF15C6D)
    val System = Color(0xFF001713)
    val SystemText = Color(0xFFFFD279)

    val Background = Brush.verticalGradient(listOf(InkTop, Ink))

    /** The home summary's gradient: emerald into bright blue, white text on top. */
    val Hero = listOf(Color(0xFF00695C), Color(0xFF1E5BB8))
    val Confetti = listOf(Gold, Green, Tear, Red, Text)
}

val ChatColorScheme = darkColorScheme(
    primary = Chat.Green,
    onPrimary = Chat.Ink,
    secondary = Chat.Tear,
    onSecondary = Chat.Ink,
    tertiary = Chat.Gold,
    background = Chat.Ink,
    onBackground = Chat.Text,
    surface = Chat.Ink,
    onSurface = Chat.Text,
    onSurfaceVariant = Chat.Muted,
    surfaceContainerLow = Chat.Raised,
    surfaceContainer = Chat.Raised,
    surfaceContainerHigh = Chat.Bubble,
    surfaceContainerHighest = Chat.Bubble,
    secondaryContainer = Chat.Bubble,
    onSecondaryContainer = Chat.Text,
    outline = Chat.Faint,
    outlineVariant = Chat.Faint
)
