// What every screenshot test shares (jorgelillo.android.screenshots adds it to test sources): one
// way to capture, the same tolerance, a frozen clock. Only test code depends on this module.
plugins {
    alias(libs.plugins.jorgelillo.android.library)
    alias(libs.plugins.jorgelillo.android.compose)
}

android {
    namespace = "com.jorgelillo.core.screenshottesting"
}

dependencies {
    api(platform(libs.androidx.compose.bom))
    api(libs.androidx.compose.ui.test.junit4)
    api(libs.roborazzi)
    api(libs.roborazzi.compose)
    api(libs.roborazzi.accessibilityCheck)
    api(libs.robolectric)
}
