plugins {
    alias(libs.plugins.jorgelillo.android.library)
    alias(libs.plugins.jorgelillo.android.compose)
    alias(libs.plugins.jorgelillo.android.screenshots)
}

android {
    namespace = "com.jorgelillo.core.designsystem"
}

dependencies {
    api(libs.androidx.compose.foundation)
    api(libs.androidx.compose.material3)
    api(libs.androidx.compose.material.iconsCore)
    api(libs.androidx.compose.ui)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.zxing.core)
}
