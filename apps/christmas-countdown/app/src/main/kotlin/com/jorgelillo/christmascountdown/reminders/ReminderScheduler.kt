package com.jorgelillo.christmascountdown.reminders

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.jorgelillo.christmascountdown.domain.Reminders
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

/**
 * Keeps one pending job, at the next moment a reminder may go out (Reminders.nextSlot). The job
 * shows it if it still applies and schedules the following one. WorkManager survives reboots; the
 * time is approximate (Android may delay it a little to save battery), which is fine here.
 */
object ReminderScheduler {
    private const val WORK = "season_reminders"
    const val KEY_DATE = "date"
    const val KEY_SLOT = "slot"

    fun scheduleNext(context: Context, now: LocalDateTime = LocalDateTime.now()) {
        val (date, slot) = Reminders.nextSlot(now)
        val delay = Duration.between(now, date.atTime(slot.time))
        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            // Millis, not Duration: the Duration overload needs API 26 and the app supports 23.
            .setInitialDelay(delay.toMillis().coerceAtLeast(0), TimeUnit.MILLISECONDS)
            .setInputData(workDataOf(KEY_DATE to date.toString(), KEY_SLOT to slot.name))
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(WORK, ExistingWorkPolicy.REPLACE, request)
    }
}
