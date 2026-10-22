package com.jorgelillo.slipper.ui.theme

import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/** "Grandma's kitchen": cream walls, blue and white tiles, terracotta floor, her red slipper. */
object Kitchen {
    val Cream = Color(0xFFFFECC8)
    val CreamDeep = Color(0xFFFFD6AA)
    val Ink = Color(0xFF3C2828)
    val Muted = Color(0xFF6B4A3A)
    val Tile = Color(0xFF468CC8)
    val TileWhite = Color(0xFFEBF0F5)
    val Grout = Color(0xFFD9C9B0)
    val Floor = Color(0xFFB0623E)
    val FloorEdge = Color(0xFF8C4628)
    val Plank = Color(0xFF9E5434)
    val Green = Color(0xFF1F7A5A) // white text on it: 5.4:1
    val GreenText = Color(0xFF155C44) // on cream: 6:1
    val Slipper = Color(0xFFE5465A)
    val Shout = Color(0xFFB0263A) // red text on cream: 5.6:1
    val Raised = Color(0xFFFFF6E6)

    val Background = Brush.verticalGradient(listOf(Cream, CreamDeep))
}

val KitchenColorScheme = lightColorScheme(
    primary = Kitchen.Green,
    onPrimary = Color.White,
    secondary = Kitchen.Slipper,
    background = Kitchen.Cream,
    onBackground = Kitchen.Ink,
    surface = Kitchen.Raised,
    onSurface = Kitchen.Ink,
    surfaceContainer = Kitchen.Raised,
    surfaceContainerLow = Kitchen.Raised,
    onSurfaceVariant = Kitchen.Muted
)
