package com.jorgelillo.christmascountdown.reminders

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.WorkManagerTestInitHelper
import com.jorgelillo.christmascountdown.TestChristmasCountdownApplication
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * The job planned at start runs through WorkManager's own queue and plans the next one. One test
 * per class: the worker opens the app's DataStore, which can only be open once per process.
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [36], application = TestChristmasCountdownApplication::class)
class ReminderChainTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    private fun jobs() = WorkManager.getInstance(context).getWorkInfosForUniqueWork("season_reminders").get()

    @Test
    fun theJobPlannedAtStartRunsAndPlansTheNextOne() {
        val first = jobs().single { it.state == WorkInfo.State.ENQUEUED }
        WorkManagerTestInitHelper.getTestDriver(context)!!.setInitialDelayMet(first.id)
        val deadline = System.currentTimeMillis() + 10_000
        while (jobs().none { it.id != first.id && it.state == WorkInfo.State.ENQUEUED } &&
            System.currentTimeMillis() < deadline
        ) {
            Thread.sleep(20)
        }
        val next = jobs().filter { it.state == WorkInfo.State.ENQUEUED }
        assertTrue(next.size == 1, "one job planned after the run: $next")
        assertNotEquals(first.id, next.single().id)
    }
}
