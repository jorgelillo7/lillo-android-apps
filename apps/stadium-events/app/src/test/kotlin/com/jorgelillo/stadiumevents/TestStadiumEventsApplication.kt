package com.jorgelillo.stadiumevents

import androidx.work.testing.WorkManagerTestInitHelper
import com.jorgelillo.stadiumevents.data.FeedFetcher
import java.time.LocalDate

/**
 * The app on the JVM: Robolectric doesn't run App Startup, so WorkManager (evening reminder,
 * widget) gets its test instance before the app's own onCreate schedules anything; and the download
 * is a sample feed around today instead of the network.
 */
class TestStadiumEventsApplication : StadiumEventsApplication() {
    override val fetcher = FeedFetcher { SampleFeed.json(LocalDate.now()) }

    override fun onCreate() {
        WorkManagerTestInitHelper.initializeTestWorkManager(this)
        super.onCreate()
    }
}
