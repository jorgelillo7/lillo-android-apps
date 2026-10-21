package com.jorgelillo.decisionwheel.data

import android.content.Context
import com.jorgelillo.core.platform.JsonStore
import com.jorgelillo.decisionwheel.domain.AppState
import com.jorgelillo.decisionwheel.domain.Wheel

/** The whole app state (wheels, history, settings) as one JSON document; seeded with the localized preset wheels. */
typealias StateRepository = JsonStore<AppState>

/** The app's single store: DataStore file "decision_wheel", as it has always been (users keep their data). */
fun stateRepository(context: Context, seed: () -> List<Wheel>): StateRepository =
    JsonStore(context, "decision_wheel", AppState.serializer()) { AppState(wheels = seed()) }
