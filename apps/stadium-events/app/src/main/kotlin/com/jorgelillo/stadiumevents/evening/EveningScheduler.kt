package com.jorgelillo.stadiumevents.evening

import android.content.Context
import androidx.work.workDataOf
import com.jorgelillo.core.reminders.LilloSchedule
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * One pending job, every evening at [AT]: it downloads the events, tells the neighbours if tomorrow
 * is busy, and plans the next evening. Android may run it a little late, fine for the evening before.
 */
object EveningScheduler {
    private const val WORK = "evening_reminder"
    const val KEY_DATE = "date"
    val AT: LocalTime = LocalTime.of(19, 0)

    fun nextRun(now: LocalDateTime): LocalDateTime {
        val today = now.toLocalDate().atTime(AT)
        return if (now < today) today else today.plusDays(1)
    }

    /** From the job itself: replaces the pending one, so the chain moves on. */
    fun scheduleNext(context: Context, now: LocalDateTime = LocalDateTime.now()) {
        val at = nextRun(now)
        LilloSchedule.at(context, WORK, EveningWorker::class.java, at, input(at), now)
    }

    /** From the app's start: keeps a job that is running, due or planned for the next evening. */
    fun ensureNext(context: Context, now: LocalDateTime = LocalDateTime.now()) {
        val at = nextRun(now)
        LilloSchedule.ensure(context, WORK, EveningWorker::class.java, at, input(at), now)
    }

    private fun input(at: LocalDateTime) = workDataOf(KEY_DATE to at.toLocalDate().toString())
}
