package com.jorgelillo.stadiumevents.domain

import java.time.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * The events file published daily at [FEED_URL] (built from the stadium's agenda and Atlético de
 * Madrid's fixtures by jorgelillo7.github.io's tools/stadium_events.py). Unknown fields are ignored
 * and unknown values fall back to the defaults, so the file can grow without breaking old apps.
 */
@Serializable
data class EventFeed(val version: Int = 1, val events: List<StadiumEvent> = emptyList()) {
    companion object {
        const val FEED_URL = "https://jorgelillo7.github.io/stadium/events.json"
        const val STADIUM_URL = "https://www.riyadhairmetropolitano.com/"

        private val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }

        fun parse(text: String): EventFeed = json.decodeFromString(serializer(), text)
    }
}

@Serializable
data class StadiumEvent(
    val id: String,
    val title: String,
    val kind: Kind = Kind.OTHER,
    val level: Level = Level.BUSY,
    /** ISO dates. With [either], the event is on one of them (LaLiga hasn't fixed it yet); else on all. */
    val days: List<String>,
    val either: Boolean = false,
    /** Kick-off or start, "21:00"; null when the sources don't give one. */
    val time: String? = null,
    val competition: String? = null,
    val url: String? = null
) {
    val dates: List<LocalDate> get() = days.mapNotNull { runCatching { LocalDate.parse(it) }.getOrNull() }.sorted()
}

@Serializable
enum class Kind {
    @SerialName("match")
    MATCH,

    @SerialName("final")
    FINAL,

    @SerialName("concert")
    CONCERT,

    @SerialName("other")
    OTHER
}

@Serializable
enum class Level {
    /** A match or another big event: more traffic and people around the stadium. */
    @SerialName("busy")
    BUSY,

    /** Concerts and finals: the biggest days. */
    @SerialName("max")
    MAX
}

/** What the app keeps on the phone: the last feed it downloaded and when. */
@Serializable
data class AppState(
    val feed: EventFeed = EventFeed(),
    /** Epoch millis of the last successful download; null before the first one. */
    val fetchedAt: Long? = null,
    /** The notifications permission was asked once at first launch: never ask again. */
    val notificationsAsked: Boolean = false
)
