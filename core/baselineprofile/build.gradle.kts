plugins {
    alias(libs.plugins.jorgelillo.android.library)
}

// What every app's baselineprofile module shares: the generator and the startup benchmark.
android {
    namespace = "com.jorgelillo.core.baselineprofile"
    defaultConfig.minSdk = 28
}

dependencies {
    api(libs.androidx.benchmark.macro.junit4)
    api(libs.androidx.test.ext.junit)
}
