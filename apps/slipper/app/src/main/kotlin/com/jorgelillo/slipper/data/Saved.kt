package com.jorgelillo.slipper.data

import android.content.Context
import com.jorgelillo.core.platform.JsonStore
import kotlinx.serialization.Serializable

/** What the phone remembers: the best score and a few totals. */
@Serializable
data class Saved(val best: Int = 0, val runs: Int = 0, val croquetas: Int = 0)

typealias SavedStore = JsonStore<Saved>

fun savedStore(context: Context): SavedStore = JsonStore(context, "slipper", Saved.serializer()) { Saved() }
