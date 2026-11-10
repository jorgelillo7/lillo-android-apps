package com.jorgelillo.core.reminders

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.ListenableWorker
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

/**
 * Reminder chains: one pending job per [name], at a given time, that does its work and plans the
 * next one. Scheduling again replaces the pending job, so apps re-plan on every start (cheap,
 * idempotent: covers updates, time changes and the first run). WorkManager survives reboots; the
 * time is approximate (Android may delay it a little to save battery).
 */
object LilloSchedule {
    fun at(
        context: Context,
        name: String,
        worker: Class<out ListenableWorker>,
        at: LocalDateTime,
        input: Data = Data.EMPTY,
        now: LocalDateTime = LocalDateTime.now()
    ) {
        val request = OneTimeWorkRequest.Builder(worker)
            // Millis, not Duration: the Duration overload needs API 26 and some apps support 24.
            .setInitialDelay(Duration.between(now, at).toMillis().coerceAtLeast(0), TimeUnit.MILLISECONDS)
            .setInputData(input)
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(name, ExistingWorkPolicy.REPLACE, request)
    }
}
