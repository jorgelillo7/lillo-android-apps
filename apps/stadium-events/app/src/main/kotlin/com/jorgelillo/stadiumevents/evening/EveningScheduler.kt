package com.jorgelillo.stadiumevents.evening

import android.content.Context
import com.jorgelillo.core.reminders.LilloSchedule
import com.jorgelillo.stadiumevents.domain.Jaleo
import java.time.LocalDateTime

/**
 * One pending job, at the next 09:00 or 19:00 (Jaleo.nextSlot): it downloads the events, shows the
 * reminder that is due and plans the next run. Android may run it a little late; the job decides
 * from the clock, so a late run still does the right thing.
 */
object EveningScheduler {
    // The name stays from 1.0, when there was only the evening: phones already hold a job under it.
    private const val WORK = "evening_reminder"

    /** From the job itself: replaces the pending one, so the chain moves on. */
    fun scheduleNext(context: Context, now: LocalDateTime = LocalDateTime.now()) =
        LilloSchedule.at(context, WORK, EveningWorker::class.java, Jaleo.nextSlot(now), now = now)

    /** From the app's start: keeps a job that is running, due or planned for the next slot. */
    fun ensureNext(context: Context, now: LocalDateTime = LocalDateTime.now()) =
        LilloSchedule.ensure(context, WORK, EveningWorker::class.java, Jaleo.nextSlot(now), now = now)
}
