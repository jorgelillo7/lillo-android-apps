package com.jorgelillo.stadiumevents.evening

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.testing.TestListenableWorkerBuilder
import com.jorgelillo.stadiumevents.ClockedApplication
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.test.assertNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

class NoPermissionApplication : ClockedApplication(LocalDate.of(2026, 11, 24))

/** Without the permission nothing is shown, so nothing counts as reminded. One test per class (DataStore). */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [36], application = NoPermissionApplication::class)
class NoPermissionWorkerTest {
    private val app: NoPermissionApplication = ApplicationProvider.getApplicationContext()

    @Test
    fun aReminderThatCouldNotBeShownIsNotMarkedAsGiven() {
        app.now = LocalDateTime.of(2026, 11, 24, 19, 0)
        runBlocking { TestListenableWorkerBuilder<EveningWorker>(app).build().doWork() }
        assertNull(runBlocking { app.repository.state.first().remindedFor })
    }
}
