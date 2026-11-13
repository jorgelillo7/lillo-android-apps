plugins {
    alias(libs.plugins.jorgelillo.android.application)
    alias(libs.plugins.jorgelillo.android.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.jorgelillo.android.screenshots)
}

android {
    namespace = "com.jorgelillo.stadiumevents"

    defaultConfig {
        applicationId = "com.jorgelillo.stadiumevents"
        versionCode = 2
        versionName = "1.1"
    }

    // Release signing and R8: the jorgelillo.android.application convention plugin.

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(projects.apps.stadiumEvents.domain)
    implementation(projects.core.designsystem)
    implementation(projects.core.platform)
    implementation(projects.core.reminders)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.dataStore.preferences)
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.material3)
    implementation(libs.androidx.lifecycle.runtimeCompose)
    implementation(libs.androidx.lifecycle.viewModelCompose)
    testImplementation(libs.androidx.work.testing)
}
