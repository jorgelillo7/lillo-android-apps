package com.jorgelillo.stadiumevents.evening

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.jorgelillo.stadiumevents.StadiumEventsApplication
import com.jorgelillo.stadiumevents.domain.Jaleo
import com.jorgelillo.stadiumevents.widget.updateJaleoWidgets
import java.time.LocalDateTime
import kotlinx.coroutines.flow.first

/**
 * A run of the reminder chain (09:00 and 19:00): fresh events, the reminder that is due by the clock
 * (Jaleo.reminder), the widget, and the next run. The class name is stored in WorkManager's queue:
 * renaming it would orphan the job phones already have.
 */
class EveningWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as StadiumEventsApplication
        try {
            app.repository.refresh() // offline: the last copy on the phone still answers
            val state = app.repository.state.first()
            Jaleo.reminder(state.feed.events, LocalDateTime.now(app.clock), state.remindedFor)?.let {
                if (JaleoNotifications.show(applicationContext, it)) app.repository.markReminded(it.day.date)
            }
            updateJaleoWidgets(applicationContext)
        } finally {
            // Whatever happened to this one, the chain goes on (else it waits for the app to open).
            EveningScheduler.scheduleNext(applicationContext, LocalDateTime.now(app.clock))
        }
        return Result.success()
    }
}
