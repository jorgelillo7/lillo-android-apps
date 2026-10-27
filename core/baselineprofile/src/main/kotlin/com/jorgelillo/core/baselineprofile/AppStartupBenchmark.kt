package com.jorgelillo.core.baselineprofile

import androidx.benchmark.macro.BaselineProfileMode
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import org.junit.Rule
import org.junit.Test

/**
 * An app's cold start, without and with its baseline profile, to see what the profile buys. An app's
 * baselineprofile module subclasses it (`class StartupBenchmark : AppStartupBenchmark()`). Run with
 * `./gradlew :apps:<app>:baselineprofile:connectedBenchmarkReleaseAndroidTest`; results (median
 * time to initial display) in the console and `build/outputs/connected_android_test_additional_output`.
 * Emulator numbers are only good for comparing the two; a real phone gives the real ones.
 */
abstract class AppStartupBenchmark {
    @get:Rule
    val rule = MacrobenchmarkRule()

    @Test
    fun startupWithoutProfile() = startup(CompilationMode.None())

    @Test
    fun startupWithProfile() = startup(CompilationMode.Partial(BaselineProfileMode.Require))

    private fun startup(mode: CompilationMode) = rule.measureRepeated(
        packageName = targetPackage,
        metrics = listOf(StartupTimingMetric()),
        compilationMode = mode,
        startupMode = StartupMode.COLD,
        iterations = ITERATIONS
    ) {
        pressHome()
        startActivityAndWait()
    }

    private companion object {
        const val ITERATIONS = 10
    }
}
