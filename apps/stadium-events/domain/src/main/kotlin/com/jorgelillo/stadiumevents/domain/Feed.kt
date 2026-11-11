package com.jorgelillo.stadiumevents.domain

import java.time.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * The events file published daily at [FEED_URL] (built from the stadium's agenda and Atlético de
 * Madrid's fixtures by jorgelillo7.github.io's tools/stadium_events.py). Unknown fields are ignored
 * and unknown values fall back to the defaults, so the file can grow without breaking old apps.
 * The file is downloaded, so it is untrusted: [parse] refuses one over [MAX_CHARS] and cuts every
 * list and text to a size the screen, the widget and the evening notification can show.
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

        /** The real file is a few KB; anything this large is not ours. */
        const val MAX_CHARS = 256 * 1024
        const val MAX_EVENTS = 200
        const val MAX_DAYS = 7
        const val MAX_ID = 80
        const val MAX_TITLE = 120
        const val MAX_COMPETITION = 60
        const val MAX_TIME = 5
        const val MAX_URL = 300

        fun parse(text: String): EventFeed {
            require(text.length <= MAX_CHARS) { "feed too large: ${text.length} chars" }
            return json.decodeFromString(serializer(), text).bounded()
        }

        private fun EventFeed.bounded() = copy(
            events = events.take(MAX_EVENTS).map { e ->
                e.copy(
                    id = e.id.take(MAX_ID),
                    title = e.title.take(MAX_TITLE),
                    days = e.days.take(MAX_DAYS).map { it.take(10) },
                    time = e.time?.take(MAX_TIME),
                    competition = e.competition?.take(MAX_COMPETITION),
                    url = e.url?.take(MAX_URL)
                )
            }
        )
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
