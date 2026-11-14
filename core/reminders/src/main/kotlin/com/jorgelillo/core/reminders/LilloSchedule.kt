package com.jorgelillo.core.reminders

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.ListenableWorker
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

/**
 * Reminder chains: one pending job per name, at a given time, that does its work and plans the
 * next one. Two entry points:
 *
 * - [at], from the chain's own job: replaces whatever is pending, so the chain moves on.
 * - [ensure], from `Application.onCreate`: keeps a job that is running, due or planned no later
 *   than the wanted time, and replaces only a missing, later or foreign one. WorkManager starts a
 *   dead process to run a due job and `onCreate` runs first: replacing there cancels the very job
 *   the process was started for.
 *
 * WorkManager survives reboots; the time is approximate (Android may delay it to save battery).
 */
object LilloSchedule {
    /** How far a kept job may be planned after the wanted time: the same slot, computed a moment apart. */
    private val SAME_SLOT_MILLIS = TimeUnit.MINUTES.toMillis(1)

    fun at(
        context: Context,
        name: String,
        worker: Class<out ListenableWorker>,
        at: LocalDateTime,
        input: Data = Data.EMPTY,
        now: LocalDateTime = LocalDateTime.now()
    ) {
        WorkManager.getInstance(context)
            .enqueueUniqueWork(name, ExistingWorkPolicy.REPLACE, request(worker, delayMillis(now, at), input))
    }

    /**
     * Start-up planning: leaves the chain alone when its job of [worker] is running or will run no
     * later than [at] (it re-plans itself when it runs); otherwise plans [at] as [LilloSchedule.at]
     * does. The check runs on WorkManager's thread, never blocking the app's start.
     */
    fun ensure(
        context: Context,
        name: String,
        worker: Class<out ListenableWorker>,
        at: LocalDateTime,
        input: Data = Data.EMPTY,
        now: LocalDateTime = LocalDateTime.now()
    ) {
        val workManager = WorkManager.getInstance(context)
        val wanted = delayMillis(now, at)
        val infos = workManager.getWorkInfosForUniqueWork(name)
        infos.addListener({
            val wantedAt = workManager.configuration.clock.currentTimeMillis() + wanted
            val jobs = try {
                infos.get()
            } catch (_: Exception) {
                emptyList()
            }
            if (jobs.none { keeps(it, worker, wantedAt) }) {
                workManager.enqueueUniqueWork(name, ExistingWorkPolicy.REPLACE, request(worker, wanted, input))
            }
        }, Runnable::run)
    }

    private fun keeps(job: WorkInfo, worker: Class<out ListenableWorker>, wantedAt: Long): Boolean =
        worker.name in job.tags &&
            when (job.state) {
                WorkInfo.State.RUNNING -> true

                WorkInfo.State.ENQUEUED, WorkInfo.State.BLOCKED ->
                    job.nextScheduleTimeMillis <=
                        wantedAt + SAME_SLOT_MILLIS

                else -> false
            }

    private fun delayMillis(now: LocalDateTime, at: LocalDateTime) =
        Duration.between(now, at).toMillis().coerceAtLeast(0)

    // Millis, not Duration: the Duration overload needs API 26 and some apps support 24.
    private fun request(worker: Class<out ListenableWorker>, delayMillis: Long, input: Data) =
        OneTimeWorkRequest.Builder(worker)
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .setInputData(input)
            .build()
}
