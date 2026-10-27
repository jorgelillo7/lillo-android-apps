plugins {
    alias(libs.plugins.jorgelillo.android.application)
    alias(libs.plugins.jorgelillo.android.compose)
    alias(libs.plugins.jorgelillo.android.screenshots)
}

// A league's own deck (real people and their in-jokes) never goes into this public repository: it
// lives in a private folder, by default ../lloros-league/content next to this repo, or wherever
// the Gradle property leagueCards.privateDir points. When the folder holds league-private.json,
// the app plays it instead of the bundled sample; without it (CI, anyone else) it plays the sample.
val privateDeckDir: File = providers.gradleProperty("leagueCards.privateDir").orNull?.let(::File)
    ?: rootDir.resolve("../lloros-league/content")

android {
    namespace = "com.jorgelillo.leaguecards"

    defaultConfig {
        applicationId = "com.jorgelillo.leaguecards"
        versionCode = 3
        versionName = "1.1"
    }

    // Release signing and R8: the jorgelillo.android.application convention plugin.

    sourceSets {
        getByName("main") {
            if (privateDeckDir.resolve("league-private.json").isFile) assets.directories.add(privateDeckDir.path)
        }
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(projects.apps.leagueCards.domain)
    implementation(projects.core.designsystem)
    implementation(projects.core.platform)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.dataStore.preferences)
    implementation(libs.androidx.lifecycle.runtimeCompose)
    implementation(libs.androidx.lifecycle.viewModelCompose)
}
