package com.jorgelillo.core.baselineprofile

import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test

/**
 * Generates an app's baseline profile: a cold start to its first screen, then [journeys]. An app's
 * baselineprofile module subclasses it (`class BaselineProfile : AppBaselineProfile()`) and
 * overrides [journeys] with what users do most, driven by test tags (`By.res("tag")`; every app
 * exposes them as resource ids). Run with `./gradlew :apps:<app>:app:generateBaselineProfile`.
 */
abstract class AppBaselineProfile {
    @get:Rule
    val rule = BaselineProfileRule()

    /** The main journeys after startup; the default is startup alone. */
    open fun MacrobenchmarkScope.journeys() = Unit

    @Test
    fun generate() = rule.collect(packageName = targetPackage, includeInStartupProfile = true) {
        pressHome()
        startActivityAndWait()
        device.waitForIdle()
        journeys()
    }
}

/** The app under test: the test module's own package without `.baselineprofile`. */
val targetPackage: String
    get() = InstrumentationRegistry.getInstrumentation().context.packageName.removeSuffix(".baselineprofile")

/** Taps the element with this test tag once it shows (fails the run if it never does). */
fun MacrobenchmarkScope.tap(tag: String, timeoutMs: Long = 5_000) {
    val node = device.wait(Until.findObject(By.res(tag)), timeoutMs) ?: error("$tag never showed up")
    node.click()
    device.waitForIdle()
}
