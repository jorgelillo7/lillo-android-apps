package com.jorgelillo.stadiumevents.evening

import android.Manifest
import android.app.NotificationManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.TestListenableWorkerBuilder
import com.jorgelillo.stadiumevents.ClockedApplication
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

/** Tuesday 24 November 2026; the Viking match is on Wednesday 25 at 21:00. */
class EveningApplication : ClockedApplication(LocalDate.of(2026, 11, 24))

/**
 * The evening before a busy day, and the slots after it. One test per class: the app's DataStore
 * file can only be open once per process.
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [36], application = EveningApplication::class)
class EveningWorkerBusyTest {
    private val app: EveningApplication = ApplicationProvider.getApplicationContext()
    private val manager get() = app.getSystemService(NotificationManager::class.java)

    private fun runAt(now: LocalDateTime): ListenableWorker.Result = runBlocking {
        app.now = now
        TestListenableWorkerBuilder<EveningWorker>(app).build().doWork()
    }

    private fun pending() = WorkManager.getInstance(app)
        .getWorkInfosForUniqueWork("evening_reminder").get()
        .filter { it.state == WorkInfo.State.ENQUEUED }

    @Test
    fun theEveningBeforeABusyDayRemindsOnceAndTheMorningAfterStaysSilent() {
        shadowOf(app).grantPermissions(Manifest.permission.POST_NOTIFICATIONS)
        // Only the run itself can plan the next one: take away the one planned at start.
        WorkManager.getInstance(app).cancelUniqueWork("evening_reminder").result.get()

        assertEquals(ListenableWorker.Result.success(), runAt(LocalDateTime.of(2026, 11, 24, 19, 0)))
        val reminder = shadowOf(manager).allNotifications.single()
        assertEquals("tomorrow", reminder.channelId)
        assertEquals(
            "Tomorrow, Wednesday 25, it's busy around the stadium",
            reminder.extras.getCharSequence("android.title").toString()
        )
        assertTrue("Atlético – Viking FK" in reminder.extras.getCharSequence("android.text").toString())
        assertEquals(LocalDate.of(2026, 11, 25), runBlocking { app.repository.state.first().remindedFor })
        assertEquals(1, pending().size, "the chain goes on after every run")

        manager.cancelAll()
        runAt(LocalDateTime.of(2026, 11, 24, 19, 5))
        runAt(LocalDateTime.of(2026, 11, 25, 0, 30))
        runAt(LocalDateTime.of(2026, 11, 25, 9, 0))
        assertEquals(0, shadowOf(manager).size(), "a second evening run, the night and the morning after stay silent")
        assertEquals(1, pending().size)
    }
}
