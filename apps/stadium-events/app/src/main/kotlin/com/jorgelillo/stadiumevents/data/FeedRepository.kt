package com.jorgelillo.stadiumevents.data

import com.jorgelillo.core.platform.JsonStore
import com.jorgelillo.stadiumevents.domain.AppState
import com.jorgelillo.stadiumevents.domain.EventFeed
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/** Downloads the events file; a fake in tests. */
fun interface FeedFetcher {
    suspend fun fetch(): String
}

/** A plain GET of [EventFeed.FEED_URL]: a few KB, so no HTTP library. */
class HttpFeedFetcher(private val userAgent: String) : FeedFetcher {
    override suspend fun fetch(): String = withContext(Dispatchers.IO) {
        val connection = URL(EventFeed.FEED_URL).openConnection() as HttpURLConnection
        try {
            connection.connectTimeout = TIMEOUT_MILLIS
            connection.readTimeout = TIMEOUT_MILLIS
            connection.setRequestProperty("User-Agent", userAgent)
            check(connection.responseCode == HttpURLConnection.HTTP_OK) { "HTTP ${connection.responseCode}" }
            connection.inputStream.bufferedReader().use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }

    private companion object {
        const val TIMEOUT_MILLIS = 15_000
    }
}

/**
 * The events the app shows, kept on the phone so it works offline: [refresh] replaces them with a
 * fresh download, and a failed download keeps the last good copy.
 */
class FeedRepository(
    private val store: JsonStore<AppState>,
    private val fetcher: FeedFetcher,
    private val onUpdated: suspend () -> Unit = {}
) {
    val state: Flow<AppState> = store.state

    /** True if the download worked. */
    suspend fun refresh(now: Long = System.currentTimeMillis()): Boolean {
        val feed = runCatching { EventFeed.parse(fetcher.fetch()) }.getOrNull() ?: return false
        store.update { it.copy(feed = feed, fetchedAt = now) }
        onUpdated()
        return true
    }

    suspend fun markNotificationsAsked() = store.update { it.copy(notificationsAsked = true) }
}
