plugins {
    alias(libs.plugins.jorgelillo.android.library)
    alias(libs.plugins.jorgelillo.android.robolectric)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.jorgelillo.core.platform"
}

dependencies {
    api(libs.androidx.dataStore.preferences)
    api(libs.kotlinx.serialization.json)
    implementation(libs.androidx.core.ktx)
}
