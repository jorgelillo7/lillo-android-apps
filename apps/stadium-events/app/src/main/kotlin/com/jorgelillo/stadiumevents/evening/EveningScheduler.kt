package com.jorgelillo.stadiumevents.evening

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit

/**
 * Keeps one pending job, every evening at [AT]: it downloads the events, tells the neighbours if
 * tomorrow is busy, and plans the next evening. WorkManager survives reboots; Android may run it a
 * little late to save battery, which is fine for a reminder the evening before.
 */
object EveningScheduler {
    private const val WORK = "evening_reminder"
    const val KEY_DATE = "date"
    val AT: LocalTime = LocalTime.of(19, 0)

    fun nextRun(now: LocalDateTime): LocalDateTime {
        val today = now.toLocalDate().atTime(AT)
        return if (now < today) today else today.plusDays(1)
    }

    fun scheduleNext(context: Context, now: LocalDateTime = LocalDateTime.now()) {
        val at = nextRun(now)
        val request = OneTimeWorkRequestBuilder<EveningWorker>()
            .setInitialDelay(Duration.between(now, at).toMillis().coerceAtLeast(0), TimeUnit.MILLISECONDS)
            .setInputData(workDataOf(KEY_DATE to at.toLocalDate().toString()))
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(WORK, ExistingWorkPolicy.REPLACE, request)
    }
}
