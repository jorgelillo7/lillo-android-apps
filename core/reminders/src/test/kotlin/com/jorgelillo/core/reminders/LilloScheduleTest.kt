package com.jorgelillo.core.reminders

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.Clock
import androidx.work.Configuration
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

class NoopWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result = Result.success()
}

class OtherWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result = Result.success()
}

@RunWith(AndroidJUnit4::class)
@Config(sdk = [36])
class LilloScheduleTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val now = LocalDateTime.of(2026, 12, 1, 10, 0)

    /** WorkManager's own clock, moved by hand: a job becomes due without running. */
    private var clockMillis = now.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

    @Before
    fun startWorkManager() {
        val config = Configuration.Builder()
            .setExecutor(SynchronousExecutor())
            .setClock(Clock { clockMillis })
            .build()
        WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
    }

    private fun infos(name: String): List<WorkInfo> =
        WorkManager.getInstance(context).getWorkInfosForUniqueWork(name).get()

    private fun pending(name: String) = infos(name).filter { it.state == WorkInfo.State.ENQUEUED }

    private fun schedule(name: String, at: LocalDateTime) =
        LilloSchedule.at(context, name, NoopWorker::class.java, at, now = now)

    private fun ensure(name: String, at: LocalDateTime, startedAt: LocalDateTime = now) =
        LilloSchedule.ensure(context, name, NoopWorker::class.java, at, now = startedAt)

    private fun passMinutes(minutes: Long) {
        clockMillis += TimeUnit.MINUTES.toMillis(minutes)
    }

    @Test
    fun aJobWaitsForTheGivenTime() {
        schedule("chain", now.plusHours(9))
        val job = pending("chain").single()
        assertEquals(TimeUnit.HOURS.toMillis(9), job.initialDelayMillis)
    }

    @Test
    fun aTimeInThePastRunsRightAway() {
        schedule("chain", now.minusHours(3))
        // With no delay the test WorkManager has already run it: nothing is left waiting.
        val job = infos("chain").single()
        assertEquals(0L, job.initialDelayMillis)
        assertEquals(WorkInfo.State.SUCCEEDED, job.state)
    }

    @Test
    fun schedulingAChainAgainReplacesItsPendingJobAndNeverStacksThem() {
        schedule("chain", now.plusHours(9))
        schedule("chain", now.plusHours(2))
        schedule("chain", now.plusHours(5))
        val job = pending("chain").single()
        assertEquals(TimeUnit.HOURS.toMillis(5), job.initialDelayMillis)
        assertTrue(infos("chain").filter { it.id != job.id }.all { it.state == WorkInfo.State.CANCELLED })
    }

    @Test
    fun eachChainKeepsItsOwnJob() {
        schedule("morning", now.plusHours(1))
        schedule("evening", now.plusHours(8))
        assertEquals(1, pending("morning").size)
        assertEquals(1, pending("evening").size)
        schedule("morning", now.plusHours(2))
        assertEquals(TimeUnit.HOURS.toMillis(8), pending("evening").single().initialDelayMillis)
    }

    @Test
    fun replacingAtStartUpCancelsTheJobThatIsDueWhichIsWhyStartUpEnsuresInstead() {
        schedule("chain", now.plusHours(1))
        val due = pending("chain").single()
        // The job's time comes with the app's process dead: WorkManager starts it, onCreate runs first.
        passMinutes(61)
        schedule("chain", now.plusDays(1).plusHours(1))
        val left = WorkManager.getInstance(context).getWorkInfoById(due.id).get()
        assertTrue(left == null || left.state == WorkInfo.State.CANCELLED, "the due job is gone: $left")
        assertNotEquals(due.id, pending("chain").single().id)
    }

    @Test
    fun startUpKeepsAJobThatIsDue() {
        schedule("chain", now.plusHours(1))
        val due = pending("chain").single()
        passMinutes(61)
        // At start-up the next time is already tomorrow's: the due one must still run.
        ensure("chain", now.plusDays(1).plusHours(1), now.plusMinutes(61))
        assertEquals(due.id, pending("chain").single().id)
        assertEquals(1, infos("chain").size)
    }

    @Test
    fun startUpKeepsTheJobForTheSameTimeAndPlansAMissingOne() {
        ensure("chain", now.plusHours(9))
        val planned = pending("chain").single()
        assertEquals(TimeUnit.HOURS.toMillis(9), planned.initialDelayMillis)
        passMinutes(30)
        ensure("chain", now.plusHours(9), now.plusMinutes(30))
        assertEquals(planned.id, pending("chain").single().id)
        assertEquals(1, infos("chain").size)
    }

    @Test
    fun startUpReplacesAJobPlannedLaterThanTheWantedTime() {
        // An update added an earlier slot, or the clock went back: the pending job would come too late.
        schedule("chain", now.plusHours(9))
        val late = pending("chain").single()
        ensure("chain", now.plusHours(2))
        val job = pending("chain").single()
        assertNotEquals(late.id, job.id)
        assertEquals(TimeUnit.HOURS.toMillis(2), job.initialDelayMillis)
    }

    @Test
    fun startUpReplacesAJobOfAnotherWorker() {
        // A worker renamed in an update could not be built when its old job came due.
        LilloSchedule.at(context, "chain", OtherWorker::class.java, now.plusHours(1), now = now)
        ensure("chain", now.plusHours(9))
        val job = pending("chain").single()
        assertTrue(NoopWorker::class.java.name in job.tags)
    }
}
