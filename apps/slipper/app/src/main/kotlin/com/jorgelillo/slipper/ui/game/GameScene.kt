package com.jorgelillo.slipper.ui.game

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import com.jorgelillo.slipper.domain.Chunk
import com.jorgelillo.slipper.domain.Engine
import com.jorgelillo.slipper.domain.EventKind
import com.jorgelillo.slipper.domain.FurnitureKind
import com.jorgelillo.slipper.domain.GameConfig as C
import com.jorgelillo.slipper.domain.GameEvent
import com.jorgelillo.slipper.domain.PowerKind
import com.jorgelillo.slipper.domain.Room
import com.jorgelillo.slipper.domain.SlipperKind
import com.jorgelillo.slipper.domain.SlipperSkin
import com.jorgelillo.slipper.domain.World
import com.jorgelillo.slipper.ui.theme.Kitchen
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/** Where the floor is, as a share of the canvas height (upright, wide); the room above it. */
private const val FLOOR_AT = 0.8f
private const val FLOOR_AT_WIDE = 0.88f
private const val ROOM_H = 56.0

/** The band that has to fit: the highest jump and the high slippers. */
private const val ACTION_H = 34.0

/** At least this many world units across (portrait), at most this many (landscape). */
private const val VIEW_W = 68.0
private const val MAX_VIEW_W = 110.0

/** The kid's place on screen, as a share of the width: the rest is what's coming. */
private const val KID_AT = 0.36

private const val GRANDMA_H = 17.0
private const val TILE = 5.0
private const val TILE_BAND = 30.0

/** How long the screen shakes after a hit, and how far (world units). */
private const val SHAKE_TIME = 0.25
private const val SHAKE = 1.2

/** The words that pop up over the action; null on the home screen's still. */
internal class SceneLabels(
    val nearMiss: String,
    val ouch: String,
    val blocked: String,
    val sandwich: String,
    val skates: String,
    val crash: String
)

/** The camera: world units → canvas pixels. */
private class Camera(val unit: Float, val left: Double, val floor: Float) {
    fun x(units: Double) = ((units - left) * unit).toFloat()

    fun y(units: Double) = (floor - units * unit).toFloat()
}

/**
 * The house at one moment of a run, all pixel art: a pure drawing of [world] (shake, pop-ups,
 * crumbs and warnings come from its events and times), so screenshot tests and the home screen can
 * show any moment without playing. The camera follows the kid; the grandma runs in from the left
 * edge as she closes in, and a warning at that edge shows what she is about to throw.
 */
@Composable
internal fun GameScene(
    world: World,
    sprites: GameSprites,
    modifier: Modifier = Modifier,
    skin: SlipperSkin = SlipperSkin.CLASSIC,
    labels: SceneLabels? = null
) {
    val text = rememberTextMeasurer()
    Canvas(modifier.clipToBounds()) {
        val floorPx = size.height * if (size.width > size.height) FLOOR_AT_WIDE else FLOOR_AT
        val unit = max(size.width / MAX_VIEW_W, min(size.width / VIEW_W, floorPx * 0.9 / ACTION_H)).toFloat()
        val view = size.width / unit
        val cam = Camera(unit, world.distance - view * KID_AT, floorPx)

        drawRect(Kitchen.Background, size = size)
        val (dx, dy) = shake(world)
        translate(dx * unit, dy * unit) {
            for (chunk in world.chunks) drawRoom(chunk, cam, sprites)
            drawRect(Kitchen.Beam, Offset(0f, cam.y(ROOM_H) - unit * 2), Size(size.width, unit * 2))
            drawThings(world, cam, sprites)
            drawGrandma(world, cam, sprites)
            drawKid(world, cam, sprites)
            drawSlippers(world, cam, sprites, skin)
            drawEffects(world, cam, text, labels)
        }
        drawWarning(world, cam, sprites, skin)
    }
}

/** A short, fading shake after a hit, a stumble or the catch (world units). */
private fun shake(w: World): Pair<Float, Float> {
    val last =
        w.events.lastOrNull { it.kind == EventKind.HIT || it.kind == EventKind.STUMBLE || it.kind == EventKind.CAUGHT }
            ?: return 0f to 0f
    val age = w.time - last.at
    if (age > SHAKE_TIME) return 0f to 0f
    val strength = SHAKE * (1 - age / SHAKE_TIME) * if (last.kind == EventKind.CAUGHT) 2 else 1
    return (strength * sin(age * 90)).toFloat() to (strength * cos(age * 70) * 0.6).toFloat()
}

private val FurnitureKind.sprite: String get() = name.lowercase()

/** Furniture (knocked over on skates), toys, croquetas and power-ups bobbing, the cat. */
private fun DrawScope.drawThings(w: World, cam: Camera, sprites: GameSprites) {
    val unit = cam.unit
    for (f in w.furniture) {
        val s = sprites[f.kind.sprite]
        val pixel = (f.kind.h * unit / s.height).toFloat()
        val left = cam.x(f.x)
        if (left > size.width || left + s.width * pixel < 0) continue
        drawShadow(left + s.width * pixel / 2, cam.y(0.0), s.width * pixel * 0.55f, unit)
        val crashed =
            w.events.any { it.kind == EventKind.CRASH && abs(it.x - f.x) < 0.01 } || (f.bumped && w.skates > 0)
        if (crashed) {
            // Knocked over by the skates: tipped forward.
            rotate(35f, Offset(left + s.width * pixel, cam.y(0.0))) { s.draw(this, left, cam.y(0.0), pixel) }
        } else {
            s.draw(this, left, cam.y(0.0), pixel)
        }
    }
    val croqueta = sprites["croqueta"]
    for (c in w.croquetas) {
        val bob = sin((w.time * 4 + c.x * 0.3)).toFloat() * 0.4f
        croqueta.draw(this, cam.x(c.x), cam.y(c.y + bob), (C.CROQUETA_W * unit / croqueta.width).toFloat())
    }
    for (p in w.powerUps) {
        val s = sprites[if (p.kind == PowerKind.SANDWICH) "sandwich" else "skates"]
        val bob = sin(w.time * 5 + p.x).toFloat() * 0.6f
        val centre = Offset(cam.x(p.x + C.POWER_W / 2), cam.y(p.y + C.POWER_H / 2 + bob))
        // A soft glow: something worth jumping for.
        drawCircle(Color.White.copy(alpha = 0.55f), radius = unit * 3.4f, center = centre)
        drawCircle(
            Kitchen.Slipper.copy(alpha = 0.25f),
            radius = unit * 3.4f,
            center = centre,
            style = androidx.compose.ui.graphics.drawscope.Stroke(unit * 0.4f)
        )
        val pixel = (C.POWER_W * unit / s.width).toFloat()
        s.draw(this, centre.x - s.width * pixel / 2, centre.y + s.height * pixel / 2, pixel)
    }
    for (cat in w.cats) {
        val s = sprites[if ((w.time * 8).toInt() % 2 == 0) "cat_walk_1" else "cat_walk_2"]
        val pixel = (C.CAT_W * unit / s.width).toFloat()
        val left = cam.x(cat.x)
        drawShadow(left + s.width * pixel / 2, cam.y(0.0), s.width * pixel * 0.5f, unit)
        if (cat.fled) {
            // Running off the other way, mirrored.
            scaleX(-1f, left + s.width * pixel / 2) { s.draw(this, left, cam.y(0.0), pixel) }
        } else {
            s.draw(this, left, cam.y(0.0), pixel)
        }
    }
}

private inline fun DrawScope.scaleX(scale: Float, pivotX: Float, block: DrawScope.() -> Unit) =
    drawContext.transform.let { t ->
        t.scale(scale, 1f, Offset(pivotX, 0f))
        block()
        t.scale(1 / scale, 1f, Offset(pivotX, 0f))
    }

private fun DrawScope.drawShadow(centreX: Float, floor: Float, halfWidth: Float, unit: Float) {
    drawOval(
        Color.Black.copy(alpha = 0.16f),
        Offset(centreX - halfWidth, floor - unit * 0.5f),
        Size(halfWidth * 2, unit * 1.0f)
    )
}

/** The grandma: running, or winding up with the slipper raised (and a "!" over her head). */
private fun DrawScope.drawGrandma(w: World, cam: Camera, sprites: GameSprites) {
    val stride = (w.distance / 7).toInt() % 2
    val throwing = w.windingUp || w.slippers.any { it.x < w.grandmaX + 10 }
    val grandma = sprites[
        if (throwing) {
            "grandma_throw"
        } else if (stride == 0) {
            "grandma_run_1"
        } else {
            "grandma_run_2"
        }
    ]
    val pixel = (GRANDMA_H * cam.unit / grandma.height).toFloat()
    val right = cam.x(w.grandmaX)
    val left = right - grandma.width * pixel
    if (right < 0) return
    drawShadow(left + grandma.width * pixel / 2, cam.y(0.0), grandma.width * pixel * 0.45f, cam.unit)
    val hop = if (throwing) 0f else abs(sin(w.distance * 0.45)).toFloat() * cam.unit * 0.6f
    grandma.draw(this, left, cam.y(0.0) - hop, pixel)
    if (w.windingUp) {
        val top = cam.y(0.0) - grandma.height * pixel - cam.unit * 2
        drawBubble(Offset(left + grandma.width * pixel / 2, top), cam.unit)
    }
}

/** A red "!" in a white bubble. */
private fun DrawScope.drawBubble(centre: Offset, unit: Float) {
    drawCircle(Color.White, radius = unit * 2.2f, center = centre)
    drawCircle(
        Kitchen.Shout,
        radius = unit * 2.2f,
        center = centre,
        style = androidx.compose.ui.graphics.drawscope.Stroke(unit * 0.4f)
    )
    drawRect(Kitchen.Shout, Offset(centre.x - unit * 0.35f, centre.y - unit * 1.3f), Size(unit * 0.7f, unit * 1.6f))
    drawRect(Kitchen.Shout, Offset(centre.x - unit * 0.35f, centre.y + unit * 0.6f), Size(unit * 0.7f, unit * 0.7f))
}

/** The kid: running, jumping, squashed for a moment on landing, blinking while he stumbles. */
private fun DrawScope.drawKid(w: World, cam: Camera, sprites: GameSprites) {
    val stride = (w.distance / 7).toInt() % 2
    val kid = sprites[
        if (!w.kid.standing) {
            "kid_jump"
        } else if (stride == 0) {
            "kid_run_1"
        } else {
            "kid_run_2"
        }
    ]
    val pixel = (C.KID_H * cam.unit / kid.height).toFloat()
    val centreX = cam.x(w.distance + C.KID_W / 2)
    val ground = Engine.groundUnder(w.furniture, w.distance, w.kid.y)
    // His shadow on whatever is under him, smaller the higher he is.
    val lift = ((w.kid.y - ground) / 25).coerceIn(0.0, 0.7).toFloat()
    drawShadow(centreX, cam.y(ground), kid.width * pixel * 0.45f * (1 - lift), cam.unit)

    // Dust behind his feet while he runs on the floor.
    if (w.kid.standing) {
        for (i in 0..2) {
            val phase = ((w.distance * 0.6 + i / 3.0) % 1.0).toFloat()
            drawCircle(
                Kitchen.Grout.copy(alpha = 0.7f * (1 - phase)),
                radius = cam.unit * (0.5f + phase),
                center = Offset(centreX - cam.unit * (3 + phase * 6), cam.y(ground + 0.6 + phase))
            )
        }
    }
    // Skates: speed lines, the skates under his feet.
    if (w.skates > 0) {
        for (i in 0..3) {
            val y = cam.y(w.kid.y + 2.0 + i * 2.5)
            drawRect(
                Color.White.copy(alpha = 0.8f),
                Offset(centreX - cam.unit * (9 + i * 2), y),
                Size(
                    cam.unit * 5,
                    cam.unit * 0.4f
                )
            )
        }
    }
    val squash = if (w.time - w.kid.landedAt < 0.1) 0.82f else 1f
    val pixelX = pixel * (2 - squash)
    val pixelY = pixel * squash
    val blinking = w.kid.stumble > 0 && (w.time * 14).toInt() % 2 == 1
    kid.draw(
        this,
        centreX - kid.width * pixelX / 2,
        cam.y(w.kid.y),
        pixelX,
        pixelY,
        alpha = if (blinking) 0.35f else 1f
    )
    if (w.skates > 0) {
        val s = sprites["skates"]
        val sp = (C.KID_W * 0.8 * cam.unit / s.width).toFloat()
        s.draw(this, centreX - s.width * sp / 2, cam.y(w.kid.y) + sp * 2, sp)
    }
    if (w.shield) {
        // The sandwich in hand, floating over his head.
        val s = sprites["sandwich"]
        val sp = (3.0 * cam.unit / s.width).toFloat()
        val bob = sin(w.time * 6).toFloat() * cam.unit * 0.3f
        s.draw(this, centreX - s.width * sp / 2, cam.y(w.kid.y + C.KID_H + 1.0) + bob, sp)
    }
}

private fun DrawScope.drawSlippers(w: World, cam: Camera, sprites: GameSprites, skin: SlipperSkin) {
    val slipper = sprites.slipper(skin)
    val pixel = (C.SLIPPER_W * cam.unit / slipper.width).toFloat()
    for (s in w.slippers) {
        val left = cam.x(s.x)
        val bottom = cam.y(s.y)
        val centre = Offset(left + slipper.width * pixel / 2, bottom - slipper.height * pixel / 2)
        // A motion trail, then the spinning slipper.
        drawRect(
            Color.White.copy(alpha = 0.5f),
            Offset(left - cam.unit * 6, centre.y - cam.unit * 0.3f),
            Size(cam.unit * 5, cam.unit * 0.6f)
        )
        rotate((s.spin * 720).toFloat() % 360f, centre) { slipper.draw(this, left, bottom, pixel) }
    }
}

/** Crumbs, stars and the words that pop up where something happened. */
private fun DrawScope.drawEffects(w: World, cam: Camera, text: TextMeasurer, labels: SceneLabels?) {
    for (e in w.events) {
        val age = (w.time - e.at).toFloat()
        val fade = (1 - age / C.EVENT_LIFE.toFloat()).coerceIn(0f, 1f)
        val at = Offset(cam.x(e.x + 2), cam.y(e.y + 2))
        when (e.kind) {
            EventKind.CROQUETA -> {
                burst(at, age, cam.unit, Kitchen.Croqueta, count = 6, fade = fade)
                pop(text, "+${e.value}", at, age, cam.unit, Kitchen.GreenText, fade, big = e.value >= 30)
            }

            EventKind.NEAR_MISS -> labels?.let {
                pop(text, "${it.nearMiss} +${e.value}", at, age, cam.unit, Kitchen.Shout, fade)
            }

            EventKind.HIT, EventKind.STUMBLE -> {
                burst(at, age, cam.unit, Color(0xFFFFD54A), count = 5, fade = fade)
                if (e.kind == EventKind.HIT) labels?.let { pop(text, it.ouch, at, age, cam.unit, Kitchen.Shout, fade) }
            }

            EventKind.BLOCKED -> labels?.let { pop(text, it.blocked, at, age, cam.unit, Kitchen.GreenText, fade) }

            EventKind.POWER -> {
                burst(at, age, cam.unit, Color.White, count = 8, fade = fade)
                labels?.let {
                    pop(
                        text,
                        if (w.skates >
                            0
                        ) {
                            it.skates
                        } else {
                            it.sandwich
                        },
                        at,
                        age,
                        cam.unit,
                        Kitchen.GreenText,
                        fade,
                        big = true
                    )
                }
            }

            EventKind.CRASH -> {
                burst(at, age, cam.unit, Kitchen.Beam, count = 6, fade = fade)
                labels?.let { pop(text, it.crash, at, age, cam.unit, Kitchen.Shout, fade) }
            }

            else -> Unit
        }
    }
}

/** Little squares flying out of [at]. */
private fun DrawScope.burst(at: Offset, age: Float, unit: Float, color: Color, count: Int, fade: Float) {
    for (i in 0 until count) {
        val angle = 2 * PI * i / count + 0.4
        val r = unit * (1 + age * 18)
        val p = Offset(at.x + (cos(angle) * r).toFloat(), at.y + (sin(angle) * r).toFloat() + age * age * unit * 30)
        drawRect(color.copy(alpha = fade), p, Size(unit * 0.8f, unit * 0.8f))
    }
}

/** A word that rises and fades. */
private fun DrawScope.pop(
    text: TextMeasurer,
    word: String,
    at: Offset,
    age: Float,
    unit: Float,
    color: Color,
    fade: Float,
    big: Boolean = false
) {
    val style = TextStyle(
        color = color.copy(alpha = fade),
        fontSize = (if (big) 22 else 17).sp,
        fontWeight = FontWeight.Black
    )
    val layout = text.measure(word, style)
    val topLeft = Offset(at.x - layout.size.width / 2, at.y - layout.size.height - age * unit * 8)
    drawRoundRect(
        Color.White.copy(alpha = 0.85f * fade),
        topLeft - Offset(unit * 0.6f, unit * 0.2f),
        Size(layout.size.width + unit * 1.2f, layout.size.height + unit * 0.4f),
        CornerRadius(unit)
    )
    drawText(layout, topLeft = topLeft)
}

/**
 * She is off screen and about to throw: a blinking warning at the left edge, at the height the
 * slipper will fly (jump the low ones, stay down for the high ones).
 */
private fun DrawScope.drawWarning(w: World, cam: Camera, sprites: GameSprites, skin: SlipperSkin) {
    if (!w.windingUp || cam.x(w.grandmaX) > 0) return
    if ((w.time * 10).toInt() % 2 == 1) return
    val y = cam.y(if (w.nextKind == SlipperKind.LOW) C.LOW_Y + 1.5 else C.HIGH_Y + 1.5)
    val u = cam.unit
    val path = Path().apply {
        moveTo(u * 0.5f, y)
        lineTo(u * 3.5f, y - u * 2.5f)
        lineTo(u * 3.5f, y + u * 2.5f)
        close()
    }
    drawPath(path, Kitchen.Shout)
    val slipper = sprites.slipper(skin)
    val pixel = (4.5 * u / slipper.width).toFloat()
    drawRoundRect(Color.White, Offset(u * 4f, y - u * 2.2f), Size(u * 5.5f, u * 4.4f), CornerRadius(u))
    slipper.draw(this, u * 4.5f, y + slipper.height * pixel / 2, pixel)
}

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
    // The wall from the top of the screen: above the ceiling beam is the floor upstairs, darker.
    drawRect(Kitchen.Upstairs, Offset(left, 0f), Size(width, ceiling))
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
    // Decoration chosen from the chunk's index: always the same house for a seed. One low piece
    // and one high piece per chunk, and a lamp from the ceiling, so the wall is never bare.
    val pixel = cam.unit * 0.6f
    val (low, high) = when (chunk.room) {
        Room.KITCHEN -> listOf("window", "shelf")[chunk.index % 2] to "cabinet"
        Room.HALL -> listOf("frame", "clock")[chunk.index % 2] to "frame"
        Room.LIVING_ROOM -> listOf("window", "plant")[chunk.index % 2] to "frame"
    }
    val lowSprite = sprites[low]
    val lowBottom = if (low == "plant") cam.floor else cam.y(if (low == "shelf") 38.0 else 34.0)
    lowSprite.draw(this, left + width * 0.45f, lowBottom, pixel)
    sprites[high].draw(this, left + width * 0.1f, cam.y(47.0), pixel)
    val lamp = sprites["lamp"]
    lamp.draw(this, left + width * 0.75f, ceiling + lamp.height * pixel * 1.2f, pixel * 1.2f)
    // Upstairs (upright screens show it under the score): its skirting and a window per chunk, so
    // the band above the ceiling isn't empty either.
    drawRect(Kitchen.UpstairsLine, Offset(left, ceiling - cam.unit * 3), Size(width, cam.unit * 0.8f))
    if (ceiling > cam.unit * 16) {
        val upstairs = sprites[if (chunk.index % 3 == 0) "frame" else "window"]
        upstairs.draw(this, left + width * 0.35f, ceiling - cam.unit * 5, pixel)
    }

    val floorColor = when (chunk.room) {
        Room.KITCHEN -> Kitchen.Floor
        Room.HALL -> Kitchen.HallFloor
        Room.LIVING_ROOM -> Kitchen.LivingFloor
    }
    drawRect(floorColor, Offset(left, cam.floor), Size(width, size.height - cam.floor))
    drawRect(Kitchen.FloorEdge, Offset(left, cam.floor), Size(width, cam.unit * 1.2f))
    // Floor tiles in rows, offset like real ones: the floor reads as a floor, not a slab.
    val rowH = cam.unit * 4
    var row = 0
    var top = cam.floor + cam.unit * 1.2f
    while (top < size.height) {
        var x = floor(chunk.start / 12) * 12 + if (row % 2 == 0) 0.0 else 6.0
        while (x < chunk.end) {
            drawRect(Kitchen.Plank, Offset(cam.x(x), top), Size(cam.unit * 0.5f, rowH))
            x += 12
        }
        drawRect(Kitchen.Plank, Offset(left, top + rowH - cam.unit * 0.25f), Size(width, cam.unit * 0.5f))
        top += rowH
        row++
    }
}

/** Blue and white tiles from the floor up to [TILE_BAND], anchored to the world (they scroll with it). */
private fun DrawScope.drawTiles(chunk: Chunk, cam: Camera) {
    drawRect(
        Kitchen.Grout,
        Offset(cam.x(chunk.start), cam.y(TILE_BAND)),
        Size(cam.x(chunk.end) - cam.x(chunk.start), cam.unit * TILE_BAND.toFloat())
    )
    val gap = cam.unit * 0.3f
    var col = floor(chunk.start / TILE).toInt()
    while (col * TILE < chunk.end) {
        var row = 0
        while (row * TILE < TILE_BAND) {
            val color: Color = if ((row + col) % 2 == 0) Kitchen.Tile else Kitchen.TileWhite
            val x0 = max(cam.x(col * TILE), cam.x(chunk.start))
            val x1 = min(cam.x((col + 1) * TILE), cam.x(chunk.end))
            if (x1 > x0) {
                drawRect(
                    color,
                    Offset(x0 + gap, cam.y((row + 1) * TILE) + gap),
                    Size(x1 - x0 - 2 * gap, cam.unit * TILE.toFloat() - 2 * gap)
                )
            }
            row++
        }
        col++
    }
}

/** The newest events since [after] (run time): what the sounds and the vibration react to. */
internal fun World.eventsSince(after: Double): List<GameEvent> = events.filter { it.at > after }
