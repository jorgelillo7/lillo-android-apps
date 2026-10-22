package com.jorgelillo.slipper.ui.game

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalContext
import com.jorgelillo.slipper.domain.Sprite
import com.jorgelillo.slipper.domain.Sprites

/** A sprite as horizontal runs of one colour: a few dozen rectangles to draw instead of every pixel. */
internal class PixelSprite(sprite: Sprite) {
    val width = sprite.width
    val height = sprite.height
    private val runs: List<Run>

    private class Run(val x: Int, val y: Int, val length: Int, val color: Color)

    init {
        val out = mutableListOf<Run>()
        for (y in 0 until sprite.height) {
            var x = 0
            while (x < sprite.width) {
                val argb = sprite[x, y]
                var end = x
                while (end < sprite.width && sprite[end, y] == argb) end++
                if (argb != 0) out += Run(x, y, end - x, Color(argb))
                x = end
            }
        }
        runs = out
    }

    /** Draws it with its bottom-left corner at ([left], [bottom]), each pixel [pixel] px wide. */
    fun draw(scope: DrawScope, left: Float, bottom: Float, pixel: Float) {
        val top = bottom - height * pixel
        // A fraction of a pixel more on each rectangle: no hairline seams between runs.
        for (r in runs) {
            scope.drawRect(
                r.color,
                Offset(left + r.x * pixel, top + r.y * pixel),
                Size(
                    r.length * pixel + 0.6f,
                    pixel + 0.6f
                )
            )
        }
    }
}

/** Every sprite of assets/sprites.txt, ready to draw. */
internal class GameSprites(all: Map<String, Sprite>) {
    private val sprites = all.mapValues { PixelSprite(it.value) }

    operator fun get(name: String): PixelSprite = sprites.getValue(name)
}

@Composable
internal fun rememberGameSprites(): GameSprites {
    val assets = LocalContext.current.assets
    return remember { GameSprites(Sprites.parse(assets.open("sprites.txt").bufferedReader().use { it.readText() })) }
}
