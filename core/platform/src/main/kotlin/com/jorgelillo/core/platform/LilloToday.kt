package com.jorgelillo.core.platform

import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Today's date as a flow. An app left in the background crosses midnight without noticing, so it
 * calls [check] on every return (a `LifecycleResumeEffect`) and [date] moves on when the day changed.
 */
class LilloToday(private val clock: () -> LocalDate = LocalDate::now) {
    private val current = MutableStateFlow(clock())

    val date: StateFlow<LocalDate> = current.asStateFlow()

    fun check() {
        current.value = clock()
    }
}
