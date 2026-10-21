plugins {
    alias(libs.plugins.jorgelillo.jvm.library)
}

// Test helpers every app's unit tests share (added to them by jorgelillo.android.robolectric).
dependencies {
    api(libs.junit)
    api(libs.kotlinx.coroutines.test)
    api(libs.turbine)
}
