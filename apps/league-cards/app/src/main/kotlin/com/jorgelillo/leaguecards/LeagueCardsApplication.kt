package com.jorgelillo.leaguecards

import android.app.Application
import com.jorgelillo.core.platform.JsonStore
import com.jorgelillo.leaguecards.domain.League
import com.jorgelillo.leaguecards.domain.Saved

/** App-wide singletons (manual DI: the app is small enough not to need Hilt). */
class LeagueCardsApplication : Application() {
    val store by lazy { JsonStore(this, "league", Saved.serializer()) { Saved() } }

    /** The league's own deck when the build carries it (see build.gradle.kts), else the sample. */
    val league: League by lazy {
        val file = if (PRIVATE_DECK in assets.list("").orEmpty()) PRIVATE_DECK else SAMPLE_DECK
        assets.open(file).bufferedReader().use { League.parse(it.readText()) }
    }

    private companion object {
        const val PRIVATE_DECK = "league-private.json"
        const val SAMPLE_DECK = "league.json"
    }
}
