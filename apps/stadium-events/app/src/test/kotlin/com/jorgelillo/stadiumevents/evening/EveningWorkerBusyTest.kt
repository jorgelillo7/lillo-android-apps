package com.jorgelillo.stadiumevents.evening

import android.Manifest
import android.app.Application
import android.app.NotificationManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.workDataOf
import com.jorgelillo.stadiumevents.R
import com.jorgelillo.stadiumevents.TestStadiumEventsApplication
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

/**
 * The evening run when tomorrow is busy (the sample feed has a match tomorrow). One test per class:
 * the app's DataStore file can only be open once per process.
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [36], application = TestStadiumEventsApplication::class)
class EveningWorkerBusyTest {
    private val app: Application = ApplicationProvider.getApplicationContext()

    private fun run(planned: LocalDate) = runBlocking {
        TestListenableWorkerBuilder<EveningWorker>(app)
            .setInputData(workDataOf(EveningScheduler.KEY_DATE to planned.toString()))
            .build()
            .doWork()
    }

    private fun shown() = shadowOf(app.getSystemService(NotificationManager::class.java))

    private fun pending() = WorkManager.getInstance(app)
        .getWorkInfosForUniqueWork("evening_reminder").get()
        .filter { it.state == WorkInfo.State.ENQUEUED }

    @Test
    fun theEveningBeforeABusyDayTheReminderIsShownOnceThenTheNextEveningIsPlannedAndALateRunIsSilent() {
        shadowOf(app).grantPermissions(Manifest.permission.POST_NOTIFICATIONS)
        assertEquals(1, pending().size, "the app plans the first evening when it starts")

        // Only the run itself can plan the next evening: take away the one planned at start.
        WorkManager.getInstance(app).cancelUniqueWork("evening_reminder").result.get()
        assertEquals(0, pending().size)
        assertEquals(ListenableWorker.Result.success(), run(planned = LocalDate.now()))
        val reminder = shown().allNotifications.single()
        assertEquals("tomorrow", reminder.channelId)
        assertEquals(
            app.getString(R.string.notification_title),
            reminder.extras.getCharSequence("android.title").toString()
        )
        assertTrue("Atlético – Viking FK" in reminder.extras.getCharSequence("android.text").toString())
        assertEquals(1, pending().size, "the chain goes on after every run")

        // The phone slept through the planned evening: it ran the next day, and that reminder is no use.
        app.getSystemService(NotificationManager::class.java).cancelAll()
        run(planned = LocalDate.now().minusDays(1))
        assertEquals(0, shown().size())
        assertEquals(1, pending().size)
    }
}
