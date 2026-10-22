plugins {
    alias(libs.plugins.jorgelillo.android.application)
    alias(libs.plugins.jorgelillo.android.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.jorgelillo.android.screenshots)
}

android {
    namespace = "com.jorgelillo.slipper"

    defaultConfig {
        // Permanent once uploaded to Play: confirm it before the first bundle (docs/plans/slipper.md).
        applicationId = "com.jorgelillo.slipper"
        versionCode = 1
        versionName = "0.1"
    }

    // Release signing and R8: the jorgelillo.android.application convention plugin.

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(projects.apps.slipper.domain)
    implementation(projects.core.designsystem)
    implementation(projects.core.platform)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.dataStore.preferences)
    implementation(libs.androidx.lifecycle.runtimeCompose)
    implementation(libs.androidx.lifecycle.viewModelCompose)
    implementation(libs.androidx.navigation.compose)
}
