package com.jorgelillo.slipper.ui.game

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import com.jorgelillo.slipper.domain.Chunk
import com.jorgelillo.slipper.domain.FurnitureKind
import com.jorgelillo.slipper.domain.GameConfig as C
import com.jorgelillo.slipper.domain.Room
import com.jorgelillo.slipper.domain.World
import com.jorgelillo.slipper.ui.theme.Kitchen
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

/** Where the floor is, as a share of the canvas height; the room (floor to ceiling) above it. */
private const val FLOOR_AT = 0.8f
private const val ROOM_H = 65.0

/** At least this many world units across (portrait), at most this many (landscape). */
private const val VIEW_W = 50.0
private const val MAX_VIEW_W = 110.0

/** The kid's place on screen, as a share of the width: the rest is what's coming. */
private const val KID_AT = 0.35

private const val GRANDMA_H = 17.0
private const val TILE = 5.0
private const val TILE_BAND = 30.0

/** The camera: world units → canvas pixels. */
private class Camera(val unit: Float, val left: Double, val floor: Float) {
    fun x(units: Double) = ((units - left) * unit).toFloat()

    fun y(units: Double) = (floor - units * unit).toFloat()
}

/**
 * The house at one moment of a run, all pixel art: a pure drawing of [world], so screenshot tests
 * and the home screen can show any moment without playing. The camera follows the kid; the room
 * (floor to ceiling) fills a band, with the HUD above the ceiling in portrait.
 */
@Composable
internal fun GameScene(world: World, sprites: GameSprites, modifier: Modifier = Modifier) {
    Canvas(modifier.clipToBounds()) {
        val floorPx = size.height * FLOOR_AT
        val unit = max(size.width / MAX_VIEW_W, min(size.width / VIEW_W, floorPx * 0.85 / ROOM_H)).toFloat()
        val view = size.width / unit
        val cam = Camera(unit, world.distance - view * KID_AT, floorPx)

        drawRect(Kitchen.Background, size = size)
        for (chunk in world.chunks) drawRoom(chunk, cam, sprites)
        drawRect(Kitchen.Beam, Offset(0f, cam.y(ROOM_H) - unit * 2), Size(size.width, unit * 2))

        for (f in world.furniture) {
            val s = sprites[f.kind.sprite]
            s.draw(this, cam.x(f.x), cam.y(0.0), (f.kind.h * unit / s.height).toFloat())
        }
        for (c in world.croquetas) {
            val s = sprites["croqueta"]
            s.draw(this, cam.x(c.x), cam.y(c.y), (C.CROQUETA_W * unit / s.width).toFloat())
        }

        val stride = (world.distance / 7).toInt() % 2
        val throwing = world.slippers.any { it.x < world.grandmaX + 10 }
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
        grandma.draw(this, cam.x(world.grandmaX) - grandma.width * gPixel, cam.y(0.0), gPixel)

        val kidName = if (!world.kid.standing) {
            "kid_jump"
        } else if (stride == 0) {
            "kid_run_1"
        } else {
            "kid_run_2"
        }
        val kid = sprites[kidName]
        val kPixel = (C.KID_H * unit / kid.height).toFloat()
        val kidLeft = cam.x(world.distance + C.KID_W / 2) - kid.width * kPixel / 2
        // Stumbling: he blinks.
        if (world.kid.stumble <= 0 ||
            (world.time * 12).toInt() % 2 == 0
        ) {
            kid.draw(this, kidLeft, cam.y(world.kid.y), kPixel)
        }

        val slipper = sprites["slipper"]
        val sPixel = (C.SLIPPER_W * unit / slipper.width).toFloat()
        for (s in world.slippers) {
            val left = cam.x(s.x)
            val bottom = cam.y(s.y)
            val centre = Offset(left + slipper.width * sPixel / 2, bottom - slipper.height * sPixel / 2)
            rotate((s.spin * 720).toFloat() % 360f, centre) { slipper.draw(this, left, bottom, sPixel) }
        }
    }
}

private val FurnitureKind.sprite: String get() = name.lowercase()

/** One chunk of a room: wall, its decoration and the floor, across the chunk's width. */
private fun DrawScope.drawRoom(chunk: Chunk, cam: Camera, sprites: GameSprites) {
    val left = cam.x(chunk.start)
    val right = cam.x(chunk.end)
    if (right < 0 || left > size.width) return
    val ceiling = cam.y(ROOM_H)
    val width = right - left
    val wall = when (chunk.room) {
        Room.KITCHEN -> Kitchen.Cream
        Room.HALL -> Kitchen.HallPaper
        Room.LIVING_ROOM -> Kitchen.LivingPaper
    }
    drawRect(wall, Offset(left, ceiling), Size(width, cam.floor - ceiling))
    when (chunk.room) {
        Room.KITCHEN -> drawTiles(chunk, cam)

        Room.HALL -> {
            var x = floor(chunk.start / 4) * 4
            while (x < chunk.end) {
                drawRect(Kitchen.HallStripe, Offset(cam.x(x), ceiling), Size(cam.unit * 1.2f, cam.floor - ceiling))
                x += 4
            }
        }

        Room.LIVING_ROOM -> {
            drawRect(Kitchen.Wainscot, Offset(left, cam.y(12.0)), Size(width, cam.unit * 12f))
            drawRect(Kitchen.WainscotEdge, Offset(left, cam.y(12.0)), Size(width, cam.unit * 1f))
        }
    }
    // One decoration per chunk, chosen from the chunk's index: always the same house for a seed.
    val decor = when (chunk.room) {
        Room.KITCHEN -> listOf("window", "shelf")
        Room.HALL -> listOf("frame", "clock")
        Room.LIVING_ROOM -> listOf("window", "frame", "plant")
    }.let { it[chunk.index % it.size] }
    val s = sprites[decor]
    val pixel = cam.unit * 0.6f
    val bottom = if (decor == "plant") cam.floor else cam.y(if (decor == "shelf") 38.0 else 34.0)
    s.draw(this, left + width * 0.45f, bottom, pixel)

    val floorColor = when (chunk.room) {
        Room.KITCHEN -> Kitchen.Floor
        Room.HALL -> Kitchen.HallFloor
        Room.LIVING_ROOM -> Kitchen.LivingFloor
    }
    drawRect(floorColor, Offset(left, cam.floor), Size(width, size.height - cam.floor))
    drawRect(Kitchen.FloorEdge, Offset(left, cam.floor), Size(width, cam.unit * 1.2f))
    var x = floor(chunk.start / 12) * 12
    while (x < chunk.end) {
        drawRect(
            Kitchen.Plank,
            Offset(cam.x(x), cam.floor + cam.unit * 1.2f),
            Size(
                cam.unit * 0.5f,
                size.height - cam.floor
            )
        )
        x += 12
    }
}

/** Blue and white tiles from the floor up to [TILE_BAND], anchored to the world (they scroll with it). */
private fun DrawScope.drawTiles(chunk: Chunk, cam: Camera) {
    drawRect(
        Kitchen.Grout,
        Offset(cam.x(chunk.start), cam.y(TILE_BAND)),
        Size(
            cam.x(chunk.end) - cam.x(chunk.start),
            cam.unit * TILE_BAND.toFloat()
        )
    )
    val gap = cam.unit * 0.3f
    var col = floor(chunk.start / TILE).toInt()
    while (col * TILE < chunk.end) {
        var row = 0
        while (row * TILE < TILE_BAND) {
            val color: Color = if ((row + col) % 2 == 0) Kitchen.Tile else Kitchen.TileWhite
            val x0 = max(cam.x(col * TILE), cam.x(chunk.start))
            val x1 = min(cam.x((col + 1) * TILE), cam.x(chunk.end))
            if (x1 >
                x0
            ) {
                drawRect(
                    color,
                    Offset(x0 + gap, cam.y((row + 1) * TILE) + gap),
                    Size(
                        x1 - x0 - 2 * gap,
                        cam.unit * TILE.toFloat() - 2 * gap
                    )
                )
            }
            row++
        }
        col++
    }
}
