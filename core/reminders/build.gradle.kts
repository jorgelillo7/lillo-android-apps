plugins {
    alias(libs.plugins.jorgelillo.android.library)
    alias(libs.plugins.jorgelillo.android.robolectric)
}

// What apps with reminders share: notifications, one-off jobs at a given time (WorkManager), and
// the R8 rule WorkManager's Room database needs, applied to every app that depends on this module.
android {
    namespace = "com.jorgelillo.core.reminders"
    defaultConfig {
        consumerProguardFiles("consumer-rules.pro")
    }
}

dependencies {
    api(libs.androidx.work.runtime)
    implementation(libs.androidx.core.ktx)
    testImplementation(libs.androidx.work.testing)
}
