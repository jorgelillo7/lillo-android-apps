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
    val Beam = Color(0xFF7A4F28)
    val HallPaper = Color(0xFFE6EED8)
    val HallStripe = Color(0xFFCFDDBE)
    val HallFloor = Color(0xFF9C6B3E)
    val LivingPaper = Color(0xFFF3DCD0)
    val LivingFloor = Color(0xFFA8463E)
    val Wainscot = Color(0xFFB07A45)
    val WainscotEdge = Color(0xFF7A4F28)
    val Upstairs = Color(0xFFE9CFA8)
    val UpstairsLine = Color(0xFFD2B48C)
    val Croqueta = Color(0xFFD68C32)

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
