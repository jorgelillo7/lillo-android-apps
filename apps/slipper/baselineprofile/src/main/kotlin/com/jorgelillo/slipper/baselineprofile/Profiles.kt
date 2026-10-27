package com.jorgelillo.slipper.baselineprofile

import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.test.uiautomator.By
import com.jorgelillo.core.baselineprofile.AppBaselineProfile
import com.jorgelillo.core.baselineprofile.AppStartupBenchmark
import com.jorgelillo.core.baselineprofile.tap

class BaselineProfile : AppBaselineProfile() {
    /** A run: the game loop and its drawing, jumping now and then until she catches him. */
    override fun MacrobenchmarkScope.journeys() {
        tap("play")
        repeat(JUMPS) {
            device.findObject(By.res("game"))?.click()
            Thread.sleep(JUMP_EVERY_MS)
        }
    }

    private companion object {
        const val JUMPS = 12
        const val JUMP_EVERY_MS = 500L
    }
}

class StartupBenchmark : AppStartupBenchmark()
