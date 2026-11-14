dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

rootProject.name = "build-logic"
include(":convention")
// Settings plugins: a project of their own, so applying one does not load the convention
// plugins into the settings classloader, where AGP is not visible.
include(":settings")
