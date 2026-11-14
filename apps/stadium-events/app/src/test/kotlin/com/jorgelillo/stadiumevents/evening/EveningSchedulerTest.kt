package com.jorgelillo.stadiumevents.evening

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.Clock
import androidx.work.Configuration
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * The evening chain: one job a day at 19:00 local time, replaced (never stacked) when the job plans the
 * next, and kept when the app starts.
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [36])
class EveningSchedulerTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    private val evening = LocalDateTime.of(2026, 11, 24, 18, 0)

    /** WorkManager's clock, moved by hand: the evening job becomes due without running. */
    private var clockMillis = evening.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

    @Before
    fun startWorkManager() {
        val config = Configuration.Builder()
            .setExecutor(SynchronousExecutor())
            .setClock(Clock { clockMillis })
            .build()
        WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
    }

    private fun jobs() = WorkManager.getInstance(context).getWorkInfosForUniqueWork("evening_reminder").get()

    private fun pending() = jobs().filter { it.state == WorkInfo.State.ENQUEUED }

    @Test
    fun theEveningIsSevenLocalAndNextRunIsTodayOrTomorrowAccordingly() {
        assertEquals(19, EveningScheduler.AT.hour)
        assertEquals(0, EveningScheduler.AT.minute)
        val day = LocalDateTime.of(2026, 11, 24, 0, 0)
        assertEquals(day.withHour(19), EveningScheduler.nextRun(day.withHour(8)))
        assertEquals(day.withHour(19), EveningScheduler.nextRun(day.withHour(18).withMinute(59)))
        assertEquals(day.plusDays(1).withHour(19), EveningScheduler.nextRun(day.withHour(19)))
        assertEquals(day.plusDays(1).withHour(19), EveningScheduler.nextRun(day.withHour(23).withMinute(30)))
    }

    @Test
    fun theJobWaitsUntilSevenInTheEveningAndNoLonger() {
        EveningScheduler.scheduleNext(context, LocalDateTime.of(2026, 11, 24, 8, 30))
        assertEquals(TimeUnit.MINUTES.toMillis(10 * 60 + 30), pending().single().initialDelayMillis)
    }

    @Test
    fun anEveningAlreadyPastPlansTomorrowsInstead() {
        EveningScheduler.scheduleNext(context, LocalDateTime.of(2026, 11, 24, 21, 0))
        assertEquals(TimeUnit.HOURS.toMillis(22), pending().single().initialDelayMillis)
    }

    @Test
    fun planningAgainReplacesTheJobInsteadOfStackingAnother() {
        EveningScheduler.scheduleNext(context, LocalDateTime.of(2026, 11, 24, 8, 30))
        EveningScheduler.scheduleNext(context, LocalDateTime.of(2026, 11, 24, 12, 0))
        EveningScheduler.scheduleNext(context, LocalDateTime.of(2026, 11, 24, 12, 0))
        val job = pending().single()
        assertEquals(TimeUnit.HOURS.toMillis(7), job.initialDelayMillis)
        assertTrue(jobs().filter { it.id != job.id }.all { it.state == WorkInfo.State.CANCELLED })
    }

    @Test
    fun theAppsStartKeepsTheEveningJobThatIsDue() {
        EveningScheduler.ensureNext(context, evening)
        val due = pending().single()
        // 19:01 with the process dead: WorkManager starts the app to run it, and onCreate runs first.
        clockMillis += TimeUnit.MINUTES.toMillis(61)
        EveningScheduler.ensureNext(context, evening.plusMinutes(61))
        assertEquals(due.id, pending().single().id)
        assertEquals(1, jobs().size)
    }
}
