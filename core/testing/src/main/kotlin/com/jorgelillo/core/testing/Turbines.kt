package com.jorgelillo.core.testing

import app.cash.turbine.ReceiveTurbine

/**
 * Skips items until one matches and returns it: state saved to disk (DataStore) lands
 * asynchronously, and a StateFlow may skip intermediate values.
 */
suspend fun <T> ReceiveTurbine<T>.awaitMatching(matches: (T) -> Boolean): T {
    while (true) {
        val item = awaitItem()
        if (matches(item)) return item
    }
}
