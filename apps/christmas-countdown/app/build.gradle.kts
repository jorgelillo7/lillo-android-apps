plugins {
    alias(libs.plugins.jorgelillo.android.application)
    alias(libs.plugins.jorgelillo.android.compose)
}

android {
    namespace = "com.jorgelillo.christmascountdown"

    defaultConfig {
        applicationId = "com.jorgelillo.christmascountdown"
        versionCode = 5
        versionName = "4.1"
    }

    // Release signing and R8: the jorgelillo.android.application convention plugin.

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(projects.apps.christmasCountdown.domain)
    implementation(projects.core.designsystem)
    implementation(projects.core.platform)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.dataStore.preferences)
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.material3)
    implementation(libs.androidx.lifecycle.runtimeCompose)
    implementation(libs.androidx.lifecycle.viewModelCompose)
    implementation(libs.androidx.work.runtime)
}
