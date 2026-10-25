package com.jorgelillo.slipper.ui.game

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalContext
import com.jorgelillo.slipper.domain.SlipperSkin
import com.jorgelillo.slipper.domain.Sprite
import com.jorgelillo.slipper.domain.Sprites

/**
 * A sprite as horizontal runs of one colour: a few dozen rectangles to draw instead of every pixel.
 * [recolor] maps ARGB colours (a slipper skin repaints the slipper).
 */
internal class PixelSprite(sprite: Sprite, recolor: (Int) -> Int = { it }) {
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
                if (argb != 0) out += Run(x, y, end - x, Color(recolor(argb)))
                x = end
            }
        }
        runs = out
    }

    /**
     * Draws it with its bottom-left corner at ([left], [bottom]), each pixel [pixel] px wide and
     * [pixelY] px tall (a squash on landing), at [alpha].
     */
    fun draw(scope: DrawScope, left: Float, bottom: Float, pixel: Float, pixelY: Float = pixel, alpha: Float = 1f) {
        val top = bottom - height * pixelY
        // A fraction of a pixel more on each rectangle: no hairline seams between runs.
        for (r in runs) {
            scope.drawRect(
                r.color,
                Offset(left + r.x * pixel, top + r.y * pixelY),
                Size(r.length * pixel + 0.6f, pixelY + 0.6f),
                alpha = alpha
            )
        }
    }
}

/** Every sprite of assets/sprites.txt, ready to draw, and the slipper in every skin. */
internal class GameSprites(all: Map<String, Sprite>) {
    private val sprites = all.mapValues { PixelSprite(it.value) }
    private val slippers = SlipperSkin.entries.associateWith { skin ->
        val colors = SkinColors.getValue(skin)
        PixelSprite(all.getValue("slipper")) { argb -> colors[argb] ?: argb }
    }

    operator fun get(name: String): PixelSprite = sprites.getValue(name)

    fun slipper(skin: SlipperSkin): PixelSprite = slippers.getValue(skin)
}

/** The slipper's red, its shadow and the strap, repainted per skin. */
private const val RED = 0xFFE5465A.toInt()
private const val RED_SHADOW = 0xFFB02E44.toInt()
private const val STRAP = 0xFFF7BE3C.toInt()

private val SkinColors: Map<SlipperSkin, Map<Int, Int>> = mapOf(
    SlipperSkin.CLASSIC to emptyMap(),
    SlipperSkin.CHECKED to
        mapOf(RED to 0xFF6A7FB5.toInt(), RED_SHADOW to 0xFF3E4F80.toInt(), STRAP to 0xFFE8E0D0.toInt()),
    SlipperSkin.FLIP_FLOP to
        mapOf(RED to 0xFFF7BE3C.toInt(), RED_SHADOW to 0xFFC99A2E.toInt(), STRAP to 0xFF4FA35A.toInt()),
    SlipperSkin.BUNNY to mapOf(RED to 0xFFF4B6C8.toInt(), RED_SHADOW to 0xFFD98AA3.toInt(), STRAP to 0xFFFFFFFF.toInt())
)

@Composable
internal fun rememberGameSprites(): GameSprites {
    val assets = LocalContext.current.assets
    return remember { GameSprites(Sprites.parse(assets.open("sprites.txt").bufferedReader().use { it.readText() })) }
}
