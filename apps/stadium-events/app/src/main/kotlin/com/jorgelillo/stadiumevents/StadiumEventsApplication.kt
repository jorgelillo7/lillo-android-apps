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

/**
 * Holds the app-wide singletons (manual DI). Open for the JVM tests' subclass, which starts a test
 * WorkManager first and swaps the download for a fake.
 */
open class StadiumEventsApplication : Application() {
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
        // Re-plans on every start (cheap, idempotent): covers updates, time changes and the first run.
        EveningScheduler.scheduleNext(this)
    }
}
