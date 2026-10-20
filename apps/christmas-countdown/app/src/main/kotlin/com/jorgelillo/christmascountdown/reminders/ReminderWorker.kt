package com.jorgelillo.christmascountdown.reminders

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.jorgelillo.christmascountdown.ChristmasCountdownApplication
import com.jorgelillo.christmascountdown.domain.ReminderSlot
import com.jorgelillo.christmascountdown.domain.Reminders
import java.time.LocalDate
import kotlinx.coroutines.flow.first

/** Shows the reminder planned for its date and slot, if it still applies, then plans the next one. */
class ReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val date = inputData.getString(ReminderScheduler.KEY_DATE)?.let(LocalDate::parse)
        val slot = inputData.getString(ReminderScheduler.KEY_SLOT)?.let(ReminderSlot::valueOf)
        // Ran late (phone off all day): a reminder for another day is no use any more.
        if (date != null && slot != null && date == LocalDate.now()) {
            val settings = (applicationContext as ChristmasCountdownApplication).settings
            val opened = date.dayOfMonth in settings.openedDoors(date.year).first()
            Reminders.due(date, slot, doorOpenedToday = opened)?.let { SeasonNotifications.show(applicationContext, it, date) }
        }
        ReminderScheduler.scheduleNext(applicationContext)
        return Result.success()
    }
}
