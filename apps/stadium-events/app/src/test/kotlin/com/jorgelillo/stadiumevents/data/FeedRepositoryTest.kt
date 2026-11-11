package com.jorgelillo.stadiumevents.data

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.jorgelillo.core.platform.JsonStore
import com.jorgelillo.stadiumevents.domain.AppState
import java.io.IOException
import java.util.UUID
import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FeedRepositoryTest {
    private fun repository(fetcher: FeedFetcher) = FeedRepository(
        JsonStore(ApplicationProvider.getApplicationContext(), "test-${UUID.randomUUID()}", AppState.serializer()) {
            AppState()
        },
        fetcher
    )

    @Test
    fun aFailedDownloadReportsFalse() = runTest {
        assertFalse(repository { throw IOException("no network") }.refresh())
    }

    @Test
    fun aCancelledDownloadStopsTheCallerInsteadOfReadingAsOffline() = runTest {
        val repository = repository { throw CancellationException("screen left") }
        assertFailsWith<CancellationException> { repository.refresh() }
    }
}
