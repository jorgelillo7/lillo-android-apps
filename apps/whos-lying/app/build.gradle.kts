plugins {
    alias(libs.plugins.jorgelillo.android.application)
    alias(libs.plugins.jorgelillo.android.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.jorgelillo.android.screenshots)
}

android {
    namespace = "com.jorgelillo.whoslying"

    defaultConfig {
        applicationId = "com.jorgelillo.whoslying"
        minSdk = 24 // navigation-compose needs API 24
        versionCode = 5
        versionName = "1.0"
    }

    // Release signing and R8: the jorgelillo.android.application convention plugin.

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(projects.apps.whosLying.domain)
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
