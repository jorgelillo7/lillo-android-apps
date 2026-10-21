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
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityViewCheckResult
import com.google.android.apps.common.testing.accessibility.framework.checks.TextContrastCheck
import com.google.android.apps.common.testing.accessibility.framework.uielement.ViewHierarchyElement
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher

/**
 * Up to 0.5% of the pixels may differ: images are recorded on a Mac and verified on Linux CI,
 * where text anti-aliasing differs a little. A real change moves far more than that.
 */
val LilloRoborazziOptions = RoborazziOptions(
    compareOptions = RoborazziOptions.CompareOptions(changeThreshold = 0.005f)
)

private val ChecksButContrast = AccessibilityCheckPreset.getAccessibilityHierarchyChecksForPreset(AccessibilityCheckPreset.LATEST)
    .filterNot { it is TextContrastCheck }
    .toSet()

/**
 * Not text contrast problems: disabled controls (exempt in WCAG 1.4.3, Material dims them on purpose;
 * a disabled button's text is its own enabled node, so its ancestors are checked too) and emoji,
 * which are pictures that ATF measures as if they were letters.
 */
private val NotContrastProblems = object : TypeSafeMatcher<AccessibilityViewCheckResult>() {
    override fun describeTo(description: Description) {
        description.appendText("a disabled element or an emoji")
    }

    override fun matchesSafely(item: AccessibilityViewCheckResult): Boolean {
        val text = item.element?.text?.toString().orEmpty()
        if (text.isNotBlank() && text.none { it.isLetterOrDigit() }) return true
        var element: ViewHierarchyElement? = item.element
        while (element != null) {
            if (!element.isEnabled) return true
            // Typed as Any: ATF's return type carries an annotation from a library not on this classpath.
            val parent: Any? = element.parentView
            element = parent as? ViewHierarchyElement
        }
        return false
    }
}

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
    // accessibility audit too (labels, touch targets, text contrast at WCAG AA…).
    onRoot().checkRoboAccessibility(
        roborazziATFAccessibilityCheckOptions = RoborazziATFAccessibilityCheckOptions(
            checker = RoborazziATFAccessibilityChecker(checks = ChecksButContrast),
            failureLevel = RoborazziATFAccessibilityChecker.CheckLevel.Warning
        )
    )
    onRoot().checkRoboAccessibility(
        roborazziATFAccessibilityCheckOptions = RoborazziATFAccessibilityCheckOptions(
            checker = RoborazziATFAccessibilityChecker(checks = setOf(TextContrastCheck()), suppressions = NotContrastProblems),
            failureLevel = RoborazziATFAccessibilityChecker.CheckLevel.Warning
        )
    )
}
