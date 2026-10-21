package com.jorgelillo.core.screenshottesting

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.captureRoboImage

/**
 * Up to 0.5% of the pixels may differ: images are recorded on a Mac and verified on Linux CI,
 * where text anti-aliasing differs a little. A real change moves far more than that.
 */
val LilloRoborazziOptions = RoborazziOptions(
    compareOptions = RoborazziOptions.CompareOptions(changeThreshold = 0.005f)
)

/**
 * Shows [content] and records or verifies it as `src/test/screenshots/<name>.png` (recordRoborazziDebug
 * / verifyRoborazziDebug). The clock is frozen first, so animations (confetti, countdowns) are
 * captured at their start every time: advance [ComposeContentTestRule.mainClock] in [beforeCapture]
 * to capture a later moment.
 */
fun ComposeContentTestRule.captureScreenshot(
    name: String,
    beforeCapture: ComposeContentTestRule.() -> Unit = {},
    content: @Composable () -> Unit
) {
    mainClock.autoAdvance = false
    setContent(content)
    mainClock.advanceTimeByFrame()
    beforeCapture()
    onRoot().captureRoboImage("src/test/screenshots/$name.png", roborazziOptions = LilloRoborazziOptions)
}
