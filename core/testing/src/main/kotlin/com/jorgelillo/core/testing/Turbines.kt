package com.jorgelillo.core.testing

import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.withTurbineTimeout
import kotlin.time.Duration.Companion.seconds

/**
 * How long to wait for something saved to disk: Turbine's 3 s default is too short for a cold
 * DataStore on a busy CI runner. `flow.test(timeout = StoreTimeout) { … }` where an item waits on disk.
 */
val StoreTimeout = 10.seconds

/**
 * Skips items until one matches and returns it: state saved to disk (DataStore) lands
 * asynchronously, and a StateFlow may skip intermediate values.
 */
suspend fun <T> ReceiveTurbine<T>.awaitMatching(matches: (T) -> Boolean): T {
    while (true) {
        val item = withTurbineTimeout(StoreTimeout) { awaitItem() }
        if (matches(item)) return item
    }
}
