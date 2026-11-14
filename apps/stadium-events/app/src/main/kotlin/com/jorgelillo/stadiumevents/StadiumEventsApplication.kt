package com.jorgelillo.stadiumevents

import android.app.Application
import com.jorgelillo.core.platform.JsonStore
import com.jorgelillo.stadiumevents.data.FeedFetcher
import com.jorgelillo.stadiumevents.data.FeedRepository
import com.jorgelillo.stadiumevents.data.HttpFeedFetcher
import com.jorgelillo.stadiumevents.domain.AppState
import com.jorgelillo.stadiumevents.evening.EveningScheduler
import com.jorgelillo.stadiumevents.evening.JaleoNotifications
import com.jorgelillo.stadiumevents.widget.updateJaleoWidgets
import java.time.Clock
import java.time.LocalDateTime

/**
 * Holds the app-wide singletons (manual DI). Open for the JVM tests' subclasses, which start a test
 * WorkManager first and swap the download for a fake and the clock for a fixed one.
 */
open class StadiumEventsApplication : Application() {
    /** The reminder job's clock: a fixed time in the JVM tests. */
    open val clock: Clock get() = Clock.systemDefaultZone()

    open val fetcher: FeedFetcher by lazy {
        HttpFeedFetcher("StadiumEvents/${BuildConfig.VERSION_NAME} (Android; +https://jorgelillo7.github.io/)")
    }

    val repository by lazy {
        FeedRepository(
            JsonStore(this, "stadium_events", AppState.serializer()) { AppState() },
            fetcher,
            onUpdated = { updateJaleoWidgets(this) }
        )
    }

    override fun onCreate() {
        super.onCreate()
        JaleoNotifications.createChannel(this)
        // On every start (cheap, idempotent): covers updates, time changes and the first run, and
        // keeps the job WorkManager may have started this process to run.
        EveningScheduler.ensureNext(this, LocalDateTime.now(clock))
    }
}
