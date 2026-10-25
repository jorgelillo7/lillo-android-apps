package com.jorgelillo.slipper.domain

import com.jorgelillo.slipper.domain.FurnitureKind.CHAIR
import com.jorgelillo.slipper.domain.FurnitureKind.SOFA
import com.jorgelillo.slipper.domain.FurnitureKind.STOOL
import com.jorgelillo.slipper.domain.FurnitureKind.TABLE
import com.jorgelillo.slipper.domain.FurnitureKind.TOY
import com.jorgelillo.slipper.domain.GameConfig.LOW_Y
import kotlin.random.Random

/**
 * The house the kid runs through, built ahead of him one chunk at a time. Each chunk depends only
 * on the seed and its index, so a challenge link rebuilds the same house. The first chunks are bare
 * floor (the first behind the start, so the screen is never empty; then learn to jump the slippers);
 * then each one is a small pattern of furniture with croquetas placed as a reward for the riskier
 * line (on the table, above the chair). Later on the cat comes out, and power-ups float in the air.
 */
object House {
    const val WARM_UP_CHUNKS = 3
    const val CHUNKS_PER_ROOM = 6

    /** What one chunk adds: the chunk, its furniture and its croquetas. */
    data class Built(
        val chunk: Chunk,
        val furniture: List<Furniture>,
        val croquetas: List<Croqueta>,
        val cats: List<Cat> = emptyList(),
        val powerUps: List<PowerUp> = emptyList()
    )

    fun room(index: Int): Room = Room.entries[(index / CHUNKS_PER_ROOM) % Room.entries.size]

    fun build(seed: Long, index: Int, start: Double): Built {
        val rng = Random(seed * 31 + 3 * 1_000_003 + index * 7_919L)
        val furniture = mutableListOf<Furniture>()
        val croquetas = mutableListOf<Croqueta>()
        fun place(kind: FurnitureKind, at: Double) = furniture.add(Furniture(kind, start + at))
        fun croqueta(at: Double, y: Double) = croquetas.add(Croqueta(start + at, y))
        val cats = mutableListOf<Cat>()
        val powerUps = mutableListOf<PowerUp>()

        val length: Double
        if (index < WARM_UP_CHUNKS) {
            length = 40.0
            croqueta(16.0, LOW_Y)
            croqueta(24.0, LOW_Y)
        } else {
            val lead = 12.0 + rng.nextDouble() * 10
            val patterns = if (index >= GameConfig.CATS_FROM_CHUNK) 9 else 7
            length = lead + when (rng.nextInt(patterns)) {
                0 -> { // bare floor, a line of croquetas
                    for (i in 0..2) croqueta(lead + i * 6, LOW_Y)
                    18.0
                }

                1 -> { // a chair, croquetas in an arc above it
                    place(CHAIR, lead)
                    croqueta(lead - 4, 12.0)
                    croqueta(lead + 1, 16.0)
                    croqueta(lead + 6, 12.0)
                    CHAIR.w
                }

                2 -> { // the table: croquetas on top for whoever jumps up
                    place(TABLE, lead)
                    for (i in 0..2) croqueta(lead + 3 + i * 5, TABLE.h + 0.5)
                    TABLE.w
                }

                3 -> { // a stool, then a chair
                    place(STOOL, lead)
                    place(CHAIR, lead + 16)
                    croqueta(lead, 9.0)
                    16 + CHAIR.w
                }

                4 -> { // the sofa, a croqueta high above
                    place(SOFA, lead)
                    croqueta(lead + 6, 15.0)
                    SOFA.w
                }

                5 -> { // two chairs, a hop apart
                    place(CHAIR, lead)
                    place(CHAIR, lead + 15)
                    15 + CHAIR.w
                }

                6 -> { // toy bricks on the floor, croquetas between them
                    place(TOY, lead)
                    croqueta(lead + 8, LOW_Y)
                    place(TOY, lead + 16)
                    16 + TOY.w
                }

                7 -> { // the cat, walking towards you (it starts further on: it moves)
                    cats.add(Cat(start + lead + 30))
                    croqueta(lead + 22, 10.0)
                    30.0
                }

                else -> { // the cat under the table
                    place(TABLE, lead)
                    cats.add(Cat(start + lead + 40))
                    croqueta(lead + 8, TABLE.h + 0.5)
                    TABLE.w
                }
            } + 12.0 + rng.nextDouble() * 14
        }
        // A power-up now and then, floating over the start of the chunk: a jump to get it.
        val powerRng = Random(seed * 31 + 4 * 1_000_003 + index * 7_919L)
        if (index >= GameConfig.POWERS_FROM_CHUNK && powerRng.nextDouble() < GameConfig.POWER_SHARE) {
            val kind = if (powerRng.nextBoolean()) PowerKind.SANDWICH else PowerKind.SKATES
            powerUps.add(PowerUp(kind, start + 4, GameConfig.POWER_Y))
        }
        return Built(Chunk(index, start, start + length, room(index)), furniture, croquetas, cats, powerUps)
    }
}
