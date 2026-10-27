package com.jorgelillo.leaguecards.baselineprofile

import androidx.benchmark.macro.MacrobenchmarkScope
import com.jorgelillo.core.baselineprofile.AppBaselineProfile
import com.jorgelillo.core.baselineprofile.AppStartupBenchmark
import com.jorgelillo.core.baselineprofile.tap

class BaselineProfile : AppBaselineProfile() {
    /** A new season and its first cards: the swipe card and the meters. */
    override fun MacrobenchmarkScope.journeys() {
        tap("new_season")
        repeat(3) { tap("choose_right") }
    }
}

class StartupBenchmark : AppStartupBenchmark()
