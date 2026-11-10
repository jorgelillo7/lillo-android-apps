package com.jorgelillo.stadiumevents

import com.jorgelillo.stadiumevents.domain.EventFeed
import com.jorgelillo.stadiumevents.domain.Kind
import com.jorgelillo.stadiumevents.domain.Level
import com.jorgelillo.stadiumevents.domain.StadiumEvent
import java.time.LocalDate
import kotlinx.serialization.json.Json

/** A feed like the real one, around [today]: a Champions match tomorrow, then LaLiga, a final and a tour. */
object SampleFeed {
    fun events(today: LocalDate): List<StadiumEvent> = listOf(
        StadiumEvent(
            "atletico-vs-viking",
            "Atlético de Madrid vs Viking FK",
            Kind.MATCH,
            Level.BUSY,
            listOf(today.plusDays(1).toString()),
            time = "21:00",
            competition = "UEFA Champions League"
        ),
        StadiumEvent(
            "atletico-vs-betis",
            "Atlético de Madrid vs Betis",
            Kind.MATCH,
            Level.BUSY,
            listOf(today.plusDays(11).toString(), today.plusDays(12).toString()),
            either = true,
            competition = "LaLiga"
        ),
        StadiumEvent(
            "2027-uefa-champions-league-final",
            "2027 UEFA Champions League Final",
            Kind.FINAL,
            Level.MAX,
            listOf(today.plusDays(193).toString())
        ),
        StadiumEvent(
            "karol-g",
            "Karol G",
            Kind.CONCERT,
            Level.MAX,
            (212L..215L).map { today.plusDays(it).toString() }
        )
    )

    fun feed(today: LocalDate) = EventFeed(events = events(today))

    fun json(today: LocalDate): String = Json.encodeToString(EventFeed.serializer(), feed(today))
}
