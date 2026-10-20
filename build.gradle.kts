// Plugins are declared here (not applied) so every module resolves them from one classloader.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.play.publisher) apply false
    alias(libs.plugins.spotless)
}

// One formatter for the whole repo (rules in .editorconfig): ./gradlew spotlessApply fixes, spotlessCheck verifies.
spotless {
    kotlin {
        target("**/*.kt")
        targetExclude("**/build/**")
        ktlint()
    }
    kotlinGradle {
        target("**/*.gradle.kts")
        targetExclude("**/build/**")
        ktlint()
    }
}
