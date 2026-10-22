package com.jorgelillo.slipper.domain

import com.jorgelillo.slipper.domain.FurnitureKind.CHAIR
import com.jorgelillo.slipper.domain.FurnitureKind.SOFA
import com.jorgelillo.slipper.domain.FurnitureKind.STOOL
import com.jorgelillo.slipper.domain.FurnitureKind.TABLE
import com.jorgelillo.slipper.domain.GameConfig.LOW_Y
import kotlin.random.Random

/**
 * The house the kid runs through, built ahead of him one chunk at a time. Each chunk depends only
 * on the seed and its index, so a challenge link rebuilds the same house. The first chunks are bare
 * floor (learn to jump the slippers first); then each one is a small pattern of furniture with
 * croquetas placed as a reward for the riskier line (on the table, above the chair).
 */
object House {
    const val WARM_UP_CHUNKS = 2
    const val CHUNKS_PER_ROOM = 6

    /** What one chunk adds: the chunk, its furniture and its croquetas. */
    data class Built(val chunk: Chunk, val furniture: List<Furniture>, val croquetas: List<Croqueta>)

    fun room(index: Int): Room = Room.entries[(index / CHUNKS_PER_ROOM) % Room.entries.size]

    fun build(seed: Long, index: Int, start: Double): Built {
        val rng = Random(seed * 31 + 3 * 1_000_003 + index * 7_919L)
        val furniture = mutableListOf<Furniture>()
        val croquetas = mutableListOf<Croqueta>()
        fun place(kind: FurnitureKind, at: Double) = furniture.add(Furniture(kind, start + at))
        fun croqueta(at: Double, y: Double) = croquetas.add(Croqueta(start + at, y))

        val length: Double
        if (index < WARM_UP_CHUNKS) {
            length = 40.0
            croqueta(16.0, LOW_Y)
            croqueta(24.0, LOW_Y)
        } else {
            val lead = 12.0 + rng.nextDouble() * 10
            length = lead + when (rng.nextInt(6)) {
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

                else -> { // two chairs, a hop apart
                    place(CHAIR, lead)
                    place(CHAIR, lead + 15)
                    15 + CHAIR.w
                }
            } + 12.0 + rng.nextDouble() * 14
        }
        return Built(Chunk(index, start, start + length, room(index)), furniture, croquetas)
    }
}
