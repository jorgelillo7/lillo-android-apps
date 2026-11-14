package com.jorgelillo.stadiumevents

import androidx.work.testing.WorkManagerTestInitHelper
import com.jorgelillo.stadiumevents.data.FeedFetcher
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * The app on a clock the test moves ([now]), with the sample feed around [feedDay]: the Viking match
 * is the day after it at 21:00. Robolectric needs a no-argument class: each test subclasses it.
 */
abstract class ClockedApplication(private val feedDay: LocalDate) : StadiumEventsApplication() {
    var now: LocalDateTime = feedDay.atTime(12, 0)

    override val clock: Clock get() = Clock.fixed(
        now.atZone(ZoneId.systemDefault()).toInstant(),
        ZoneId.systemDefault()
    )

    override val fetcher = FeedFetcher { SampleFeed.json(feedDay) }

    override fun onCreate() {
        WorkManagerTestInitHelper.initializeTestWorkManager(this)
        super.onCreate()
    }
}
