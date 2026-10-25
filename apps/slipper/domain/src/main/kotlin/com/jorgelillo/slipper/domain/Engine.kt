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
        val events = w.events.filter { it.at > time - C.EVENT_LIFE }.toMutableList()
        fun event(kind: EventKind, x: Double, y: Double, value: Int = 0) =
            events.add(GameEvent(time, kind, x, y, value))

        val onSkates = w.skates > 0
        val speed = runSpeed(time) * when {
            onSkates -> C.SKATES_SPEED
            w.kid.stumble > 0 -> C.STUMBLE_SPEED
            else -> 1.0
        }
        val distance = w.distance + speed * dt
        val before = w.kid
        var kid = moveKid(w.kid, input, dt, groundUnder(w.furniture, distance, w.kid.y))
        if (kid.vy > 0 && before.vy <= 0) event(EventKind.JUMP, distance, kid.y)
        if (kid.standing && !before.standing) kid = kid.copy(landedAt = time)
        var gap = w.gap
        var shield = w.shield

        /** A trip (furniture, the cat, a slipper): the sandwich takes it if there is one. */
        fun trip(kind: EventKind, x: Double, y: Double): Boolean {
            if (shield) {
                shield = false
                event(EventKind.BLOCKED, x, y)
                return false
            }
            kid = kid.copy(stumble = C.STUMBLE_TIME)
            event(kind, x, y)
            return true
        }

        // Running into the side of a piece of furniture: a stumble (once per piece); on skates he
        // knocks it over instead.
        val body = Box(distance, kid.y, C.KID_W, C.KID_H).shrunk(C.FORGIVENESS)
        val furniture = w.furniture.map { f ->
            if (!f.bumped && body.overlaps(f.box) && kid.y < f.top - 1.0) {
                if (onSkates) event(EventKind.CRASH, f.x, f.top) else trip(EventKind.STUMBLE, f.x, f.top)
                f.copy(bumped = true)
            } else {
                f
            }
        }

        // The cat walks towards him; running into it trips him and it runs away.
        val cats = w.cats.map { c ->
            val moved = c.copy(x = c.x + (if (c.fled) C.CAT_SPEED * 2 else -C.CAT_SPEED) * dt)
            if (!c.fled && body.overlaps(moved.box) && kid.y < C.CAT_H - 1.0) {
                if (onSkates) event(EventKind.CRASH, moved.x, C.CAT_H) else trip(EventKind.STUMBLE, moved.x, C.CAT_H)
                moved.copy(fled = true)
            } else {
                moved
            }
        }

        // Slippers: thrown from her hand on a seeded schedule, flying after the kid.
        var nextThrow = w.nextThrow
        var nextKind = w.nextKind
        var throws = w.throws
        val slippers = w.slippers.map { it.copy(x = it.x + it.speed * dt, spin = it.spin + dt) }
            .filter { it.x < distance + C.BUILD_AHEAD }.toMutableList()
        if (time >= nextThrow) {
            val slipperSpeed = runSpeed(time) + min(C.MAX_SLIPPER_SPEED, C.SLIPPER_SPEED * (1 + w.anger))
            slippers += Slipper(distance - gap + 4, nextKind, slipperSpeed)
            val rng = Random(w.seed * 31 + 1 * 1_000_003 + throws * 7_919L)
            if (time >= C.DOUBLE_FROM && rng.nextDouble() < C.DOUBLE_SHARE) {
                // A pair: the other kind, right behind.
                nextThrow += C.DOUBLE_GAP
                nextKind = if (nextKind == SlipperKind.LOW) SlipperKind.HIGH else SlipperKind.LOW
            } else {
                nextThrow += throwGap(time) + rng.nextDouble() * C.THROW_JITTER
                nextKind = if (nextThrow >= C.HIGH_FROM && rng.nextDouble() < C.HIGH_SHARE) {
                    SlipperKind.HIGH
                } else {
                    SlipperKind.LOW
                }
            }
            throws++
        }
        val hit = slippers.firstOrNull { !it.passed && it.box.shrunk(C.FORGIVENESS / 2).overlaps(body) }
        if (hit != null) {
            slippers.remove(hit)
            if (trip(EventKind.HIT, hit.x, hit.y)) gap -= C.HIT_LOSS
        }

        // Slippers that just flew past him: a near miss if they were close.
        var bonus = w.bonus
        var nearMisses = w.nearMisses
        slippers.replaceAll { s ->
            if (s.passed || s.x <= distance + C.KID_W) return@replaceAll s
            val clearance = when (s.kind) {
                SlipperKind.LOW -> kid.y - (C.LOW_Y + C.SLIPPER_H)
                SlipperKind.HIGH -> C.HIGH_Y - (kid.y + C.KID_H)
            }
            if (clearance in 0.0..C.NEAR_MISS) {
                bonus += C.NEAR_MISS_POINTS
                nearMisses++
                gap += C.NEAR_MISS_GAIN
                event(EventKind.NEAR_MISS, s.x, s.y, C.NEAR_MISS_POINTS)
            }
            s.copy(passed = true)
        }

        // The chase: she runs at a pace of the kid's full speed; stumbling lets her close in.
        val pace = min(C.MAX_PACE, C.GRANDMA_PACE + w.anger * C.ANGER_PACE)
        gap = min(C.MAX_GAP, gap + (speed - runSpeed(time) * pace) * dt)
        kid = kid.copy(stumble = max(0.0, kid.stumble - dt))

        // Croquetas (the combo) and power-ups.
        val kidBox = Box(distance, kid.y, C.KID_W, C.KID_H)
        val eaten = w.croquetas.filter { it.box.overlaps(kidBox) }
        var combo = w.combo
        var lastCroquetaAt = w.lastCroquetaAt
        for (c in eaten) {
            combo = if (time - lastCroquetaAt <= C.COMBO_WINDOW) combo + 1 else 1
            lastCroquetaAt = time
            val points = C.POINTS_PER_CROQUETA * min(combo, C.MAX_COMBO)
            bonus += points
            event(EventKind.CROQUETA, c.x, c.y, points)
        }
        var skates = max(0.0, w.skates - dt)
        val taken = w.powerUps.filter { it.box.overlaps(kidBox) }
        for (p in taken) {
            when (p.kind) {
                PowerKind.SANDWICH -> shield = true
                PowerKind.SKATES -> skates = C.SKATES_TIME
            }
            event(EventKind.POWER, p.x, p.y)
        }

        val caught = gap <= C.CATCH_GAP
        if (caught) event(EventKind.CAUGHT, distance, kid.y)
        return build(
            w.copy(
                time = time,
                distance = distance,
                kid = kid,
                gap = gap,
                furniture = furniture,
                cats = cats,
                croquetas = w.croquetas - eaten.toSet(),
                powerUps = w.powerUps - taken.toSet(),
                slippers = slippers,
                eaten = w.eaten + eaten.size,
                bonus = bonus,
                combo = combo,
                lastCroquetaAt = lastCroquetaAt,
                nearMisses = nearMisses,
                shield = shield,
                skates = skates,
                nextThrow = nextThrow,
                nextKind = nextKind,
                throws = throws,
                events = events,
                caught = caught
            )
        )
    }

    /** Builds the house ahead (and a stretch behind the start) and forgets what fell far behind. */
    internal fun build(w: World): World {
        var chunks = w.chunks
        var furniture = w.furniture
        var croquetas = w.croquetas
        var cats = w.cats
        var powerUps = w.powerUps
        while ((chunks.lastOrNull()?.end ?: 0.0) < w.distance + C.BUILD_AHEAD) {
            val built = House.build(
                w.seed,
                chunks.size + (w.chunks.firstOrNull()?.index ?: 0),
                chunks.lastOrNull()?.end ?: -C.KEEP_BEHIND
            )
            chunks = chunks + built.chunk
            furniture = furniture + built.furniture
            croquetas = croquetas + built.croquetas
            cats = cats + built.cats
            powerUps = powerUps + built.powerUps
        }
        val behind = w.distance - C.KEEP_BEHIND
        return w.copy(
            chunks = chunks.filter { it.end > behind },
            furniture = furniture.filter { it.x + it.kind.w > behind },
            croquetas = croquetas.filter { it.x + C.CROQUETA_W > behind },
            cats = cats.filter { it.x + C.CAT_W > behind },
            powerUps = powerUps.filter { it.x + C.POWER_W > behind }
        )
    }

    /** The highest furniture top under the kid's feet (or the floor), among tops he is above. */
    fun groundUnder(furniture: List<Furniture>, x: Double, feet: Double): Double = furniture.filter {
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
