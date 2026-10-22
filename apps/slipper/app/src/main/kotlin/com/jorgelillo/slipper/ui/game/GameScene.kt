package com.jorgelillo.slipper.ui.game

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import com.jorgelillo.slipper.domain.GameConfig as C
import com.jorgelillo.slipper.domain.World
import com.jorgelillo.slipper.ui.theme.Kitchen

/** Where the floor is, as a share of the canvas height: the kitchen above, the floor below. */
private const val FLOOR_AT = 0.72f

/** The grandma is drawn this many world units tall; the tiled wall this many units high. */
private const val GRANDMA_H = 17.0
private const val WALL_H = 34.0
private const val TILE = 5.0

/**
 * The kitchen at one moment of a run, all pixel art: a pure drawing of [world], so screenshot tests
 * and the home screen can show any moment without playing.
 */
@Composable
internal fun GameScene(world: World, sprites: GameSprites, modifier: Modifier = Modifier) {
    Canvas(modifier.clipToBounds()) {
        val unit = size.width / C.WORLD_WIDTH.toFloat()
        val floor = size.height * FLOOR_AT
        drawKitchen(world, unit, floor)

        for (c in world.croquetas) {
            val s = sprites["croqueta"]
            s.draw(this, x(c.x, unit), y(c.y, unit, floor), (C.CROQUETA_W * unit / s.width).toFloat())
        }

        val throwing = world.slippers.any { it.x < C.THROW_X + 6 }
        val stride = (world.scrolled / 7).toInt() % 2
        val grandma = sprites[
            if (throwing) {
                "grandma_throw"
            } else if (stride == 0) {
                "grandma_run_1"
            } else {
                "grandma_run_2"
            }
        ]
        val gPixel = (GRANDMA_H * unit / grandma.height).toFloat()
        grandma.draw(this, x(C.GRANDMA_X, unit) - grandma.width * gPixel / 2, floor, gPixel)

        val kid = sprites[
            if (!world.kid.onFloor) {
                "kid_jump"
            } else if (stride == 0) {
                "kid_run_1"
            } else {
                "kid_run_2"
            }
        ]
        val kPixel = (C.KID_H * unit / kid.height).toFloat()
        val kidCentre = x(C.KID_X + C.KID_W / 2, unit)
        kid.draw(this, kidCentre - kid.width * kPixel / 2, y(world.kid.y, unit, floor), kPixel)

        val slipper = sprites["slipper"]
        val sPixel = (C.SLIPPER_W * unit / slipper.width).toFloat()
        for (s in world.slippers) {
            val left = x(s.x, unit)
            val bottom = y(s.y, unit, floor)
            val centre = Offset(left + slipper.width * sPixel / 2, bottom - slipper.height * sPixel / 2)
            rotate((s.spin * 720).toFloat() % 360f, centre) { slipper.draw(this, left, bottom, sPixel) }
        }
    }
}

private fun x(units: Double, unit: Float) = (units * unit).toFloat()

private fun y(units: Double, unit: Float, floor: Float) = (floor - units * unit).toFloat()

/** Cream wall, a band of blue and white tiles scrolling slower than the floor, terracotta floor planks. */
private fun DrawScope.drawKitchen(world: World, unit: Float, floor: Float) {
    drawRect(Kitchen.Background, size = Size(size.width, floor))
    val tile = (TILE * unit).toFloat()
    val wallTop = floor - (WALL_H * unit).toFloat()
    val shift = ((world.scrolled * 0.5) % (TILE * 2)).toFloat() * unit
    drawRect(Kitchen.Grout, Offset(0f, wallTop), Size(size.width, floor - wallTop))
    var row = 0
    var top = floor - tile
    while (top > wallTop - tile) {
        var col = 0
        var left = -shift
        while (left < size.width) {
            val blue = (row + col) % 2 == 0
            drawRect(
                if (blue) Kitchen.Tile else Kitchen.TileWhite,
                Offset(left + unit * 0.3f, maxOf(top, wallTop) + unit * 0.3f),
                Size(tile - unit * 0.6f, minOf(tile, top + tile - wallTop) - unit * 0.6f)
            )
            left += tile
            col++
        }
        top -= tile
        row++
    }
    drawRect(Kitchen.Floor, Offset(0f, floor), Size(size.width, size.height - floor))
    drawRect(Kitchen.FloorEdge, Offset(0f, floor), Size(size.width, unit * 1.2f))
    val plank = 12.0
    var line = -((world.scrolled % plank) * unit).toFloat()
    while (line < size.width) {
        drawRect(Kitchen.Plank, Offset(line, floor + unit * 1.2f), Size(unit * 0.5f, size.height - floor))
        line += (plank * unit).toFloat()
    }
}
