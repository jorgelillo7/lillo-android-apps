package com.jorgelillo.christmascountdown

import androidx.work.testing.WorkManagerTestInitHelper

/**
 * The app on the JVM: Robolectric doesn't run App Startup, so WorkManager (reminders, widgets) gets
 * its test instance before the app's own onCreate schedules anything.
 */
class TestChristmasCountdownApplication : ChristmasCountdownApplication() {
    override fun onCreate() {
        WorkManagerTestInitHelper.initializeTestWorkManager(this)
        super.onCreate()
    }
}
