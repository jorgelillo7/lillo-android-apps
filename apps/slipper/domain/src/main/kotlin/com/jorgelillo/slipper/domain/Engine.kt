package com.jorgelillo.slipper.domain

import com.jorgelillo.slipper.domain.GameConfig as C
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

/**
 * The rules of a run. Pure and deterministic: the same seed and the same inputs, step by step, give
 * the same run, which is what a challenge link replays. The house comes from [House] (seed + chunk
 * index); each throw draws from its own random source (seed + index), so eating a croqueta never
 * shifts when the next slipper comes.
 */
object Engine {
    fun start(seed: Long): World = build(World(seed = seed))

    fun step(w: World, input: Input, dt: Double = C.STEP): World {
        if (w.over) return w
        val time = w.time + dt
        val speed = runSpeed(time) * if (w.kid.stumble > 0) C.STUMBLE_SPEED else 1.0
        val distance = w.distance + speed * dt
        var kid = moveKid(w.kid, input, dt, groundUnder(w.furniture, distance, w.kid.y))
        var gap = w.gap

        // Running into the side of a piece of furniture: a stumble (once per piece).
        val body = Box(distance, kid.y, C.KID_W, C.KID_H).shrunk(C.FORGIVENESS)
        val furniture = w.furniture.map { f ->
            if (!f.bumped && body.overlaps(f.box) && kid.y < f.top - 1.0) {
                kid = kid.copy(stumble = C.STUMBLE_TIME)
                f.copy(bumped = true)
            } else {
                f
            }
        }

        // Slippers: thrown from her hand on a seeded schedule, flying after the kid.
        var nextThrow = w.nextThrow
        var throws = w.throws
        val slippers = w.slippers.map { it.copy(x = it.x + it.speed * dt, spin = it.spin + dt) }
            .filter { it.x < distance + C.BUILD_AHEAD }.toMutableList()
        if (time >= nextThrow) {
            val rng = Random(w.seed * 31 + 1 * 1_000_003 + throws * 7_919L)
            val high = time >= C.HIGH_FROM && rng.nextDouble() < C.HIGH_SHARE
            val slipperSpeed = runSpeed(time) + min(C.MAX_SLIPPER_SPEED, C.SLIPPER_SPEED * (1 + w.anger))
            slippers += Slipper(distance - gap + 4, if (high) SlipperKind.HIGH else SlipperKind.LOW, slipperSpeed)
            nextThrow += throwGap(time) + rng.nextDouble() * C.THROW_JITTER
            throws++
        }
        val hit = slippers.firstOrNull { it.box.shrunk(C.FORGIVENESS / 2).overlaps(body) }
        if (hit != null) {
            slippers.remove(hit)
            gap -= C.HIT_LOSS
            kid = kid.copy(stumble = C.STUMBLE_TIME)
        }

        // The chase: she runs at a pace of the kid's full speed; stumbling lets her close in.
        val pace = min(C.MAX_PACE, C.GRANDMA_PACE + w.anger * C.ANGER_PACE)
        gap = min(C.MAX_GAP, gap + (speed - runSpeed(time) * pace) * dt)
        kid = kid.copy(stumble = max(0.0, kid.stumble - dt))

        val kidBox = Box(distance, kid.y, C.KID_W, C.KID_H)
        val eaten = w.croquetas.filter { it.box.overlaps(kidBox) }

        return build(
            w.copy(
                time = time,
                distance = distance,
                kid = kid,
                gap = gap,
                furniture = furniture,
                croquetas = w.croquetas - eaten.toSet(),
                slippers = slippers,
                eaten = w.eaten + eaten.size,
                nextThrow = nextThrow,
                throws = throws,
                caught = gap <= C.CATCH_GAP
            )
        )
    }

    /** Builds the house ahead and forgets what fell far behind. */
    internal fun build(w: World): World {
        var chunks = w.chunks
        var furniture = w.furniture
        var croquetas = w.croquetas
        while ((chunks.lastOrNull()?.end ?: 0.0) < w.distance + C.BUILD_AHEAD) {
            val built = House.build(
                w.seed,
                chunks.size + (w.chunks.firstOrNull()?.index ?: 0),
                chunks.lastOrNull()?.end ?: 0.0
            )
            chunks = chunks + built.chunk
            furniture = furniture + built.furniture
            croquetas = croquetas + built.croquetas
        }
        val behind = w.distance - C.KEEP_BEHIND
        return w.copy(
            chunks = chunks.filter { it.end > behind },
            furniture = furniture.filter { it.x + it.kind.w > behind },
            croquetas = croquetas.filter { it.x + C.CROQUETA_W > behind }
        )
    }

    /** The highest furniture top under the kid's feet (or the floor), among tops he is above. */
    internal fun groundUnder(furniture: List<Furniture>, x: Double, feet: Double): Double = furniture.filter {
        it.x < x + C.KID_W - C.FORGIVENESS && x + C.FORGIVENESS < it.x + it.kind.w &&
            it.top <= feet + 0.5
    }
        .maxOfOrNull { it.top } ?: 0.0

    /** Jump buffering, coyote time, a variable height (hold to go higher) and landing on [ground]. */
    internal fun moveKid(k: Kid, input: Input, dt: Double, ground: Double): Kid {
        val buffered = if (input.tapped) 0.0 else k.buffered?.plus(dt)?.takeIf { it <= C.JUMP_BUFFER }
        val canJump = k.airTime <= C.COYOTE_TIME
        var vy = k.vy
        var held = k.held
        var pending = buffered
        if (pending != null && canJump && vy <= 0) {
            vy = C.JUMP_SPEED
            held = 0.0
            pending = null
        }
        val holding = input.holding && vy > 0 && held < C.MAX_HOLD
        if (holding) held += dt else held = C.MAX_HOLD
        vy -= (if (holding) C.HOLD_GRAVITY else C.GRAVITY) * dt
        var y = k.y + vy * dt
        var airTime = k.airTime + dt
        if (y <= ground) {
            y = ground
            vy = 0.0
            airTime = 0.0
        }
        return k.copy(y = y, vy = vy, held = held, airTime = airTime, buffered = pending)
    }

    internal fun runSpeed(time: Double) = min(C.MAX_RUN_SPEED, C.RUN_SPEED + C.RUN_SPEED_GAIN * time)

    internal fun throwGap(time: Double) = max(C.THROW_GAP_MIN, C.THROW_GAP_START - C.THROW_GAP_SHRINK * time)
}
