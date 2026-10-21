package com.jorgelillo.core.designsystem

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.jorgelillo.core.screenshottesting.captureScreenshot
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import sergio.sastre.composable.preview.scanner.android.AndroidComposablePreviewScanner
import sergio.sastre.composable.preview.scanner.android.AndroidPreviewInfo
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview

/**
 * Every `@Preview` in the component catalogue ([Catalogue.kt]) as a screenshot test, with the same
 * tolerance and accessibility checks as any other: a new component only needs a preview. Previews
 * on a landscape device run in [Landscape]. `./gradlew recordRoborazziDebug` after a deliberate change.
 */
@RunWith(ParameterizedRobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = RobolectricDeviceQualifiers.Pixel7, application = Application::class)
class CatalogueScreenshotTest(private val preview: ComposablePreview<AndroidPreviewInfo>) {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun capture() = compose.captureScreenshot("catalogue_${preview.methodName}") { preview() }

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
        fun previews() = cataloguePreviews().filterNot { it.isLandscape() }
    }

    @RunWith(ParameterizedRobolectricTestRunner::class)
    @GraphicsMode(GraphicsMode.Mode.NATIVE)
    @Config(sdk = [36], qualifiers = RobolectricDeviceQualifiers.Pixel7, application = Application::class)
    class Landscape(private val preview: ComposablePreview<AndroidPreviewInfo>) {
        @get:Rule
        val compose = createComposeRule()

        @Test
        @Config(qualifiers = "+land")
        fun capture() = compose.captureScreenshot("catalogue_${preview.methodName}") { preview() }

        companion object {
            @JvmStatic
            @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
            fun previews() = cataloguePreviews().filter { it.isLandscape() }
        }
    }
}

private fun cataloguePreviews(): List<ComposablePreview<AndroidPreviewInfo>> = AndroidComposablePreviewScanner()
    .scanPackageTrees("com.jorgelillo.core.designsystem")
    .includePrivatePreviews()
    .getPreviews()

private fun ComposablePreview<AndroidPreviewInfo>.isLandscape() = "orientation=landscape" in previewInfo.device
