package com.jorgelillo.christmascountdown.reminders

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.Clock
import androidx.work.Configuration
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import com.jorgelillo.christmascountdown.TestChristmasCountdownApplication
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/** The season's chain: the app's start never cancels the job WorkManager started the process for. */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [36], application = TestChristmasCountdownApplication::class)
class ReminderSchedulerTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val morning = LocalDateTime.of(2026, 11, 25, 9, 0)
    private var clockMillis = morning.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

    @Before
    fun startWorkManagerOnOurClock() {
        val config = Configuration.Builder()
            .setExecutor(SynchronousExecutor())
            .setClock(Clock { clockMillis })
            .build()
        WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
    }

    private fun pending() = WorkManager.getInstance(context).getWorkInfosForUniqueWork("season_reminders").get()
        .filter { it.state == WorkInfo.State.ENQUEUED }

    @Test
    fun theAppsStartKeepsTheReminderThatIsDue() {
        ReminderScheduler.ensureNext(context, morning)
        val due = pending().single()
        assertEquals(TimeUnit.HOURS.toMillis(1), due.initialDelayMillis, "25 November at 10:00: one month left")

        // 10:01 with the process dead: WorkManager starts the app to run it, and onCreate runs first.
        clockMillis += TimeUnit.MINUTES.toMillis(61)
        ReminderScheduler.ensureNext(context, morning.plusMinutes(61))
        assertEquals(due.id, pending().single().id)
    }

    @Test
    fun theJobItselfMovesTheChainOn() {
        ReminderScheduler.ensureNext(context, morning)
        val first = pending().single()
        clockMillis += TimeUnit.MINUTES.toMillis(61)
        ReminderScheduler.scheduleNext(context, morning.plusMinutes(61))
        val next = pending().single()
        assertNotEquals(first.id, next.id)
        val toAdvent = Duration.between(morning.plusMinutes(61), LocalDateTime.of(2026, 12, 1, 10, 0))
        assertEquals(toAdvent.toMillis(), next.initialDelayMillis, "next: Advent starts, 1 December at 10:00")
    }
}
