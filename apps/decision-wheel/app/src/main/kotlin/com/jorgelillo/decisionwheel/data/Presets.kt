package com.jorgelillo.decisionwheel.data

import android.content.Context
import com.jorgelillo.decisionwheel.R
import com.jorgelillo.decisionwheel.domain.Wheel

/** Ready-made wheels in the user's language. They are plain wheels: editable and deletable. */
object Presets {

    private val presets = listOf(
        Triple("preset-eat-out", R.string.preset_eat_out, R.array.preset_eat_out_options),
        Triple("preset-weekend", R.string.preset_weekend, R.array.preset_weekend_options),
        Triple("preset-dinner", R.string.preset_dinner, R.array.preset_dinner_options),
        Triple("preset-yes-no", R.string.preset_yes_no, R.array.preset_yes_no_options)
    )

    fun seed(context: Context): List<Wheel> = presets.map { (id, name, options) ->
        Wheel(id, context.getString(name), context.resources.getStringArray(options).toList())
    }
}
