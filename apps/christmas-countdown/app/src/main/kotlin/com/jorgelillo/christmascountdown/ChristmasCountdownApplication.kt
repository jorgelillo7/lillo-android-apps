package com.jorgelillo.christmascountdown

import android.app.Application
import com.jorgelillo.christmascountdown.data.SettingsRepository
import com.jorgelillo.christmascountdown.music.MusicBoxPlayer
import com.jorgelillo.christmascountdown.reminders.ReminderScheduler
import com.jorgelillo.christmascountdown.reminders.SeasonNotifications
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

/**
 * Holds the app-wide singletons (manual DI: the app is small enough not to need Hilt). Open for the
 * JVM tests' subclass, which starts a test WorkManager first.
 */
open class ChristmasCountdownApplication : Application() {
    val appScope = CoroutineScope(SupervisorJob())
    val settings by lazy { SettingsRepository(this) }
    val musicPlayer by lazy { MusicBoxPlayer(appScope) }

    override fun onCreate() {
        super.onCreate()
        SeasonNotifications.createChannels(this)
        // Re-plans on every start (cheap, idempotent): covers updates, time changes and the first run.
        ReminderScheduler.scheduleNext(this)
    }
}
