plugins {
    `kotlin-dsl`
}

group = "com.jorgelillo.buildlogic"

kotlin {
    compilerOptions {
        // The plugin APIs below are built with a newer Kotlin than the one embedded in Gradle.
        freeCompilerArgs.add("-Xskip-metadata-version-check")
    }
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.compose.gradlePlugin)
    compileOnly(libs.playPublisher.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = libs.plugins.jorgelillo.android.application.get().pluginId
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = libs.plugins.jorgelillo.android.library.get().pluginId
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("androidCompose") {
            id = libs.plugins.jorgelillo.android.compose.get().pluginId
            implementationClass = "AndroidComposeConventionPlugin"
        }
        register("jvmLibrary") {
            id = libs.plugins.jorgelillo.jvm.library.get().pluginId
            implementationClass = "JvmLibraryConventionPlugin"
        }
    }
}
