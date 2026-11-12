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
import androidx.work.testing.WorkManagerTestInitHelper
import androidx.work.workDataOf
import com.jorgelillo.stadiumevents.StadiumEventsApplication
import com.jorgelillo.stadiumevents.data.FeedFetcher
import com.jorgelillo.stadiumevents.domain.EventFeed
import com.jorgelillo.stadiumevents.domain.Kind
import com.jorgelillo.stadiumevents.domain.Level
import com.jorgelillo.stadiumevents.domain.StadiumEvent
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

/** The app with a feed whose only event is a week away: tomorrow is quiet. */
class QuietTomorrowApplication : StadiumEventsApplication() {
    override val fetcher = FeedFetcher {
        val far = StadiumEvent(
            "later",
            "Atlético de Madrid vs Betis",
            Kind.MATCH,
            Level.BUSY,
            listOf(LocalDate.now().plusDays(7).toString()),
            time = "21:00"
        )
        Json.encodeToString(EventFeed.serializer(), EventFeed(events = listOf(far)))
    }

    override fun onCreate() {
        WorkManagerTestInitHelper.initializeTestWorkManager(this)
        super.onCreate()
    }
}

/** The evening run when tomorrow is quiet. One test per class: the app's DataStore file opens once per process. */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [36], application = QuietTomorrowApplication::class)
class EveningWorkerCalmTest {
    private val app: Application = ApplicationProvider.getApplicationContext()

    @Test
    fun aQuietTomorrowShowsNothingButTheChainGoesOn() {
        shadowOf(app).grantPermissions(Manifest.permission.POST_NOTIFICATIONS)
        WorkManager.getInstance(app).cancelUniqueWork("evening_reminder").result.get()
        val result = runBlocking {
            TestListenableWorkerBuilder<EveningWorker>(app)
                .setInputData(workDataOf(EveningScheduler.KEY_DATE to LocalDate.now().toString()))
                .build()
                .doWork()
        }
        assertEquals(ListenableWorker.Result.success(), result)
        assertEquals(0, shadowOf(app.getSystemService(NotificationManager::class.java)).size())
        val pending = WorkManager.getInstance(app).getWorkInfosForUniqueWork("evening_reminder").get()
            .filter { it.state == WorkInfo.State.ENQUEUED }
        assertEquals(1, pending.size)
    }
}
