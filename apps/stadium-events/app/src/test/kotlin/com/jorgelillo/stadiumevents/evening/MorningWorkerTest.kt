package com.jorgelillo.stadiumevents.evening

import android.Manifest
import android.app.NotificationManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.TestListenableWorkerBuilder
import com.jorgelillo.stadiumevents.ClockedApplication
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

/** The Viking match is on Wednesday 25 November 2026 at 21:00; nothing reminded the evening before. */
class MorningApplication : ClockedApplication(LocalDate.of(2026, 11, 24))

/** The morning catch-up on the event day. One test per class: the app's DataStore opens once per process. */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [36], application = MorningApplication::class)
class MorningWorkerTest {
    private val app: MorningApplication = ApplicationProvider.getApplicationContext()
    private val manager get() = app.getSystemService(NotificationManager::class.java)

    private fun runAt(now: LocalDateTime) = runBlocking {
        app.now = now
        TestListenableWorkerBuilder<EveningWorker>(app).build().doWork()
    }

    @Test
    fun theMorningOfABusyDayRemindsWhenTheEveningSaidNothingAndOnlyOnce() {
        shadowOf(app).grantPermissions(Manifest.permission.POST_NOTIFICATIONS)

        runAt(LocalDateTime.of(2026, 11, 25, 8, 30))
        assertEquals(0, shadowOf(manager).size(), "never before nine")

        runAt(LocalDateTime.of(2026, 11, 25, 9, 0))
        val reminder = shadowOf(manager).allNotifications.single()
        assertEquals("Busy around the stadium today", reminder.extras.getCharSequence("android.title").toString())
        assertEquals(LocalDate.of(2026, 11, 25), runBlocking { app.repository.state.first().remindedFor })

        manager.cancelAll()
        runAt(LocalDateTime.of(2026, 11, 25, 10, 0))
        runAt(LocalDateTime.of(2026, 11, 25, 21, 30))
        assertEquals(0, shadowOf(manager).size(), "once a day, and the 26th is calm")
        val pending = WorkManager.getInstance(app).getWorkInfosForUniqueWork("evening_reminder").get()
            .filter { it.state == WorkInfo.State.ENQUEUED }
        assertEquals(1, pending.size)
    }
}
