package com.jorgelillo.christmascountdown.reminders

import android.content.Context
import androidx.work.workDataOf
import com.jorgelillo.christmascountdown.domain.Reminders
import com.jorgelillo.core.reminders.LilloSchedule
import java.time.LocalDateTime

/**
 * Keeps one pending job, at the next moment a reminder may go out (Reminders.nextSlot). The job
 * shows it if it still applies and schedules the following one.
 */
object ReminderScheduler {
    private const val WORK = "season_reminders"
    const val KEY_DATE = "date"
    const val KEY_SLOT = "slot"

    fun scheduleNext(context: Context, now: LocalDateTime = LocalDateTime.now()) {
        val (date, slot) = Reminders.nextSlot(now)
        LilloSchedule.at(
            context,
            WORK,
            ReminderWorker::class.java,
            date.atTime(slot.time),
            workDataOf(KEY_DATE to date.toString(), KEY_SLOT to slot.name),
            now
        )
    }
}
