package com.jorgelillo.slipper.data

import android.content.Context
import com.jorgelillo.core.platform.JsonStore
import com.jorgelillo.slipper.domain.SlipperSkin
import kotlinx.serialization.Serializable

/**
 * What the phone remembers: the best score and a few totals, today's best in the run of the day
 * ([dailyDay] as epoch day), the slipper skin chosen and whether the sound is off.
 */
@Serializable
data class Saved(
    val best: Int = 0,
    val runs: Int = 0,
    val croquetas: Int = 0,
    val dailyDay: Long = -1,
    val dailyBest: Int = 0,
    val skin: SlipperSkin = SlipperSkin.CLASSIC,
    val muted: Boolean = false
) {
    /** Today's best in the run of the day, 0 if not played yet today. */
    fun dailyBest(today: Long): Int = if (dailyDay == today) dailyBest else 0
}

typealias SavedStore = JsonStore<Saved>

fun savedStore(context: Context): SavedStore = JsonStore(context, "slipper", Saved.serializer()) { Saved() }
