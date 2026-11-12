package com.jorgelillo.core.platform

import kotlin.coroutines.cancellation.CancellationException

/**
 * [runCatching] for suspend code: failures become a [Result], but a cancellation is rethrown so
 * the coroutine still stops (a cancelled download must not read as "no network").
 */
inline fun <T> runCatchingCancellable(block: () -> T): Result<T> = try {
    Result.success(block())
} catch (e: CancellationException) {
    throw e
} catch (e: Throwable) {
    Result.failure(e)
}
