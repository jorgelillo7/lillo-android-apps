package com.jorgelillo.leaguecards.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/** "Group chat at night" palette: the league lives in a chat, so the game looks like one. */
object Chat {
    val Ink = Color(0xFF0B141A)
    val InkTop = Color(0xFF16252E)
    val Bubble = Color(0xFF202C33)
    val Mine = Color(0xFF005C4B)
    val Raised = Color(0xFF1A262D)
    val Text = Color(0xFFE9EDEF)
    val Muted = Color(0xFF8696A0)
    val Faint = Color(0x33E9EDEF)

    val Green = Color(0xFF00A884)
    val Gold = Color(0xFFFFC83D)
    val Tear = Color(0xFF53BDEB)
    val Red = Color(0xFFF15C6D)
    val System = Color(0xFF182229)
    val SystemText = Color(0xFFFFD279)

    val Background = Brush.verticalGradient(listOf(InkTop, Ink))
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
