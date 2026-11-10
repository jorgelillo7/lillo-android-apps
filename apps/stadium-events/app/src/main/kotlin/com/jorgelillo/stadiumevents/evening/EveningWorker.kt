package com.jorgelillo.stadiumevents.evening

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.jorgelillo.stadiumevents.StadiumEventsApplication
import com.jorgelillo.stadiumevents.domain.Jaleo
import com.jorgelillo.stadiumevents.domain.Tomorrow
import com.jorgelillo.stadiumevents.widget.updateJaleoWidgets
import java.time.LocalDate
import kotlinx.coroutines.flow.first

/** The evening run: fresh events, the reminder if tomorrow is busy, the widget, the next evening. */
class EveningWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as StadiumEventsApplication
        val planned = inputData.getString(EveningScheduler.KEY_DATE)?.let(LocalDate::parse)
        try {
            app.repository.refresh() // offline: the last copy on the phone still answers
            val today = LocalDate.now()
            // Ran late (phone off until the next day): that evening's reminder is no use any more.
            if (planned == today) {
                val answer = Jaleo.tomorrow(app.repository.state.first().feed.events, today)
                if (answer is Tomorrow.Busy) JaleoNotifications.show(applicationContext, answer)
            }
            updateJaleoWidgets(applicationContext)
        } finally {
            // Whatever happened to this one, the chain goes on (else it waits for the app to open).
            EveningScheduler.scheduleNext(applicationContext)
        }
        return Result.success()
    }
}
