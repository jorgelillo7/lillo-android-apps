import java.util.Properties

plugins {
    alias(libs.plugins.jorgelillo.android.application)
    alias(libs.plugins.jorgelillo.android.compose)
    alias(libs.plugins.jorgelillo.android.screenshots)
    alias(libs.plugins.kotlin.serialization)
}

// Firebase client config (public identifiers, not secrets) from the Firebase console. Without the
// file the app runs on its in-memory backend.
val firebasePropertiesFile = file("../firebase.properties")
val firebaseProperties = Properties().apply {
    if (firebasePropertiesFile.exists()) firebasePropertiesFile.inputStream().use { load(it) }
}
fun firebase(key: String) = "\"" + firebaseProperties.getProperty(key, "").trim() + "\""

android {
    namespace = "com.jorgelillo.grouppolls"

    defaultConfig {
        applicationId = "com.jorgelillo.grouppolls"
        minSdk = 24 // navigation-compose needs API 24
        versionCode = 1
        versionName = "1.0"
        buildConfigField("String", "FIREBASE_PROJECT_ID", firebase("projectId"))
        buildConfigField("String", "FIREBASE_APP_ID", firebase("appId"))
        buildConfigField("String", "FIREBASE_API_KEY", firebase("apiKey"))
    }

    // Release signing and R8: the jorgelillo.android.application convention plugin.

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(projects.apps.groupPolls.domain)
    implementation(libs.kotlinx.serialization.json)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.appcheck.playintegrity)
    debugImplementation(libs.firebase.appcheck.debug)
    implementation(libs.kotlinx.coroutines.play.services)
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
