package com.jorgelillo.christmascountdown.reminders

import android.content.Context
import androidx.work.workDataOf
import com.jorgelillo.christmascountdown.domain.ReminderSlot
import com.jorgelillo.christmascountdown.domain.Reminders
import com.jorgelillo.core.reminders.LilloSchedule
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Keeps one pending job, at the next moment a reminder may go out (Reminders.nextSlot). The job
 * shows it if it still applies and schedules the following one ([scheduleNext]); the app's start
 * only makes sure a job is there ([ensureNext]), so it never cancels one that is due.
 */
object ReminderScheduler {
    private const val WORK = "season_reminders"
    const val KEY_DATE = "date"
    const val KEY_SLOT = "slot"

    /** From the job itself: replaces the pending one, so the chain moves on. */
    fun scheduleNext(context: Context, now: LocalDateTime = LocalDateTime.now()) {
        val (date, slot) = Reminders.nextSlot(now)
        LilloSchedule.at(context, WORK, ReminderWorker::class.java, date.atTime(slot.time), input(date, slot), now)
    }

    /** From the app's start: keeps a job that is running, due or planned for the next slot. */
    fun ensureNext(context: Context, now: LocalDateTime = LocalDateTime.now()) {
        val (date, slot) = Reminders.nextSlot(now)
        LilloSchedule.ensure(context, WORK, ReminderWorker::class.java, date.atTime(slot.time), input(date, slot), now)
    }

    private fun input(date: LocalDate, slot: ReminderSlot) =
        workDataOf(KEY_DATE to date.toString(), KEY_SLOT to slot.name)
}
