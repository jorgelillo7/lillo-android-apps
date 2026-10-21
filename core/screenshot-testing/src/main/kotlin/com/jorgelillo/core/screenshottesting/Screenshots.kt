package com.jorgelillo.core.screenshottesting

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.RoborazziATFAccessibilityCheckOptions
import com.github.takahirom.roborazzi.RoborazziATFAccessibilityChecker
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.captureRoboImage
import com.github.takahirom.roborazzi.checkRoboAccessibility
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckPreset
import com.google.android.apps.common.testing.accessibility.framework.checks.TextContrastCheck

/**
 * Up to 0.5% of the pixels may differ: images are recorded on a Mac and verified on Linux CI,
 * where text anti-aliasing differs a little. A real change moves far more than that.
 */
val LilloRoborazziOptions = RoborazziOptions(
    compareOptions = RoborazziOptions.CompareOptions(changeThreshold = 0.005f)
)

private val FailingChecks = AccessibilityCheckPreset.getAccessibilityHierarchyChecksForPreset(AccessibilityCheckPreset.LATEST)
    .filterNot { it is TextContrastCheck }
    .toSet()

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
    // The same screen through Google's Accessibility Test Framework: every screenshot test is an
    // accessibility audit too. Labels, touch targets and the rest fail the test; text contrast is
    // only logged until the semantic colours land (PENDING.md), then it joins the failing checks.
    onRoot().checkRoboAccessibility(
        roborazziATFAccessibilityCheckOptions = RoborazziATFAccessibilityCheckOptions(
            checker = RoborazziATFAccessibilityChecker(checks = FailingChecks),
            failureLevel = RoborazziATFAccessibilityChecker.CheckLevel.Warning
        )
    )
    onRoot().checkRoboAccessibility(
        roborazziATFAccessibilityCheckOptions = RoborazziATFAccessibilityCheckOptions(
            checker = RoborazziATFAccessibilityChecker(checks = setOf(TextContrastCheck())),
            failureLevel = RoborazziATFAccessibilityChecker.CheckLevel.LogOnly
        )
    )
}
