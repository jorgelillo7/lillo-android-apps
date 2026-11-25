// The game's Google Play side: Gradle Play Publisher with the listing, graphics and release notes
// in src/main/play/. Godot builds the app; this module never compiles anything. Its version is the
// one in ../export_presets.cfg, so the listing and the bundle agree.
//   ./gradlew :engine:summandos:play:publishListing
import com.github.triplet.gradle.play.PlayPublisherExtension

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.play.publisher)
}

val preset = file("../export_presets.cfg").readLines()
fun presetValue(key: String) = preset.first { it.startsWith("$key=") }.substringAfter('=').trim('"')

android {
    namespace = "com.jorgelillo.summandos"
    compileSdk = libs.versions.compileSdk.get().substringBefore('.').toInt()
    defaultConfig {
        applicationId = presetValue("package/unique_name")
        minSdk = libs.versions.appMinSdk.get().toInt()
        versionCode = presetValue("version/code").toInt()
        versionName = presetValue("version/name")
    }
}

configure<PlayPublisherExtension> {
    defaultToAppBundles.set(true)
    track.set("internal")
    val credentials = providers.gradleProperty("play.credentials").orNull?.let(::File)
        ?: File(System.getProperty("user.home"), ".config/lillo-android-apps/play-service-account.json")
    if (credentials.isFile) serviceAccountCredentials.set(credentials)
}
