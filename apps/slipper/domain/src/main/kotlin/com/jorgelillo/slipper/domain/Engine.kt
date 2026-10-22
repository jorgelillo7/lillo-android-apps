package com.jorgelillo.slipper.domain

import com.jorgelillo.slipper.domain.GameConfig as C
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

/**
 * The rules of a run. Pure and deterministic: the same seed and the same inputs, step by step, give
 * the same run, which is what a challenge link replays. Each throw and each croqueta draws from its
 * own random source (seed + stream + index), so eating a croqueta never shifts when the next slipper
 * comes; anger only makes slippers faster.
 */
object Engine {
    fun start(seed: Long) = World(seed = seed)

    fun step(w: World, input: Input, dt: Double = C.STEP): World {
        if (w.over) return w
        val time = w.time + dt
        val kid = moveKid(w.kid, input, dt)
        val speed = worldSpeed(time)

        var nextThrow = w.nextThrow
        var throws = w.throws
        val slippers = w.slippers.map { it.copy(x = it.x + it.speed * dt, spin = it.spin + dt) }
            .filter { it.x < C.WORLD_WIDTH }.toMutableList()
        if (time >= nextThrow) {
            val rng = random(w.seed, THROWS, throws)
            val high = time >= C.HIGH_FROM && rng.nextDouble() < C.HIGH_SHARE
            val slipperSpeed = min(C.MAX_SLIPPER_SPEED, C.SLIPPER_SPEED * (1 + w.anger))
            slippers += Slipper(C.THROW_X, if (high) SlipperKind.HIGH else SlipperKind.LOW, slipperSpeed)
            nextThrow += throwGap(time) + rng.nextDouble() * C.THROW_JITTER
            throws++
        }

        var nextCroqueta = w.nextCroqueta
        var spawned = w.croquetasSpawned
        val croquetas = w.croquetas.map { it.copy(x = it.x - speed * dt) }
            .filter { it.x + C.CROQUETA_W > 0 }.toMutableList()
        if (time >= nextCroqueta) {
            val rng = random(w.seed, CROQUETAS, spawned)
            val air = rng.nextDouble() < C.CROQUETA_AIR_SHARE
            croquetas += Croqueta(C.WORLD_WIDTH + 2, if (air) 14.0 + rng.nextDouble() * 8 else C.LOW_Y)
            nextCroqueta += C.CROQUETA_GAP_MIN + rng.nextDouble() * (C.CROQUETA_GAP_MAX - C.CROQUETA_GAP_MIN)
            spawned++
        }

        val body = kid.box.shrunk(C.FORGIVENESS)
        val eatenNow = croquetas.filter { it.box.overlaps(kid.box) }
        croquetas.removeAll(eatenNow)
        val caughtBy = slippers.firstOrNull { it.box.shrunk(C.FORGIVENESS / 2).overlaps(body) }

        return w.copy(
            time = time,
            kid = kid,
            slippers = slippers,
            croquetas = croquetas,
            eaten = w.eaten + eatenNow.size,
            scrolled = w.scrolled + speed * dt,
            nextThrow = nextThrow,
            throws = throws,
            nextCroqueta = nextCroqueta,
            croquetasSpawned = spawned,
            caughtBy = caughtBy
        )
    }

    /** Jump buffering, coyote time and a variable height (hold to go higher). */
    internal fun moveKid(k: Kid, input: Input, dt: Double): Kid {
        val buffered = if (input.tapped) 0.0 else k.buffered?.plus(dt)?.takeIf { it <= C.JUMP_BUFFER }
        val canJump = k.onFloor || k.airTime <= C.COYOTE_TIME
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
        if (y <= 0) {
            y = 0.0
            vy = 0.0
        }
        val airTime = if (y <= 0) 0.0 else k.airTime + dt
        return Kid(y, vy, held, airTime, pending)
    }

    internal fun worldSpeed(time: Double) = min(C.MAX_WORLD_SPEED, C.WORLD_SPEED + C.WORLD_SPEED_GAIN * time)

    internal fun throwGap(time: Double) = max(C.THROW_GAP_MIN, C.THROW_GAP_START - C.THROW_GAP_SHRINK * time)

    private const val THROWS = 1L
    private const val CROQUETAS = 2L

    private fun random(seed: Long, stream: Long, index: Int) = Random(seed * 31 + stream * 1_000_003 + index * 7_919L)
}
