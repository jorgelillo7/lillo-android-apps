pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "lillo-android-apps"

// Shared modules
include(":core:designsystem")
include(":core:platform")
include(":core:sharelink")
include(":core:screenshot-testing")
include(":core:testing")
include(":core:baselineprofile")

// Christmas Countdown
include(":apps:christmas-countdown:app")
include(":apps:christmas-countdown:domain")
include(":apps:christmas-countdown:baselineprofile")

// Decision wheel ("What next?")
include(":apps:decision-wheel:app")
include(":apps:decision-wheel:domain")
include(":apps:decision-wheel:baselineprofile")

// Who's lying? (impostor party game)
include(":apps:whos-lying:app")
include(":apps:whos-lying:domain")
include(":apps:whos-lying:baselineprofile")

// ¿Qué votáis? (group polls)
include(":apps:group-polls:app")
include(":apps:group-polls:domain")

// ¿Quién gana? (tournaments)
include(":apps:tournaments:app")
include(":apps:tournaments:domain")
include(":apps:tournaments:baselineprofile")

// ¿Quién llora esta jornada? (fantasy-league card game)
include(":apps:league-cards:app")
include(":apps:league-cards:domain")
include(":apps:league-cards:baselineprofile")

// ¿Te pilla la chancla? (slipper runner)
include(":apps:slipper:app")
include(":apps:slipper:domain")
include(":apps:slipper:baselineprofile")
