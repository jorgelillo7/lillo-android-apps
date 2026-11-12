package com.jorgelillo.core.reminders

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.testing.WorkManagerTestInitHelper
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

class NoopWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result = Result.success()
}

@RunWith(AndroidJUnit4::class)
@Config(sdk = [36])
class LilloScheduleTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val now = LocalDateTime.of(2026, 12, 1, 10, 0)

    @Before
    fun startWorkManager() {
        WorkManagerTestInitHelper.initializeTestWorkManager(context)
    }

    private fun infos(name: String): List<WorkInfo> =
        WorkManager.getInstance(context).getWorkInfosForUniqueWork(name).get()

    private fun pending(name: String) = infos(name).filter { it.state == WorkInfo.State.ENQUEUED }

    private fun schedule(name: String, at: LocalDateTime) =
        LilloSchedule.at(context, name, NoopWorker::class.java, at, now = now)

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
}
