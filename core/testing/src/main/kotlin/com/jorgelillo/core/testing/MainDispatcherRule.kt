package com.jorgelillo.core.testing

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * Runs `Dispatchers.Main` (and so `viewModelScope`) on a test dispatcher for each test:
 * `@get:Rule val main = MainDispatcherRule()` in a ViewModel test, and every ViewModel the test
 * creates goes through [cancelAtEnd] (`.also { main.cancelAtEnd(it.viewModelScope) }`).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(private val dispatcher: TestDispatcher = UnconfinedTestDispatcher()) : TestWatcher() {
    private val scopes = mutableListOf<CoroutineScope>()

    /**
     * Cancels [scope] when the test ends, before Main is reset. A ViewModel still collecting its
     * store would otherwise reach Main while the next test sets it ("used concurrently with setting it").
     */
    fun <T : CoroutineScope> cancelAtEnd(scope: T): T = scope.also { scopes += it }

    override fun starting(description: Description) = Dispatchers.setMain(dispatcher)

    override fun finished(description: Description) {
        scopes.forEach { it.cancel() }
        scopes.clear()
        Dispatchers.resetMain()
    }
}
