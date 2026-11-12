package com.jorgelillo.core.platform

import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import org.junit.Test

class ResultsTest {
    @Test
    fun aValueIsASuccess() {
        assertEquals(Result.success(2), runCatchingCancellable { 1 + 1 })
    }

    @Test
    fun aFailureIsCaptured() {
        assertIs<IOException>(runCatchingCancellable { throw IOException("no network") }.exceptionOrNull())
    }

    @Test
    fun aCancellationIsRethrown() {
        assertFailsWith<CancellationException> { runCatchingCancellable { throw CancellationException("left") } }
    }
}
