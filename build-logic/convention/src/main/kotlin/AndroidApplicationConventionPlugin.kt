import com.android.build.api.dsl.ApplicationExtension
import com.github.triplet.gradle.play.PlayPublisherExtension
import com.jorgelillo.buildlogic.configureKotlinAndroid
import com.jorgelillo.buildlogic.intVersion
import com.jorgelillo.buildlogic.libs
import java.io.File
import java.util.Properties
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/**
 * Every app: SDK levels, Kotlin, and a release build that is shrunk with R8 and signed with the
 * app's upload key from the git-ignored `apps/<app>/keystore.properties`. Without that file the
 * release build is signed with the debug key, so the R8 build can still be smoke tested (Play
 * rejects debug-signed bundles). It also lists the app's languages (from its `values-*` folders and
 * `res/resources.properties`) so Android 13+ offers them in system settings and
 * `LilloLanguageDialog` works.
 *
 * Publishing (Gradle Play Publisher): the store listing, graphics and release notes live in
 * `app/src/main/play/`; `./gradlew :apps:<app>:app:publishBundle` uploads the signed bundle with
 * its notes to the internal testing track, `promoteArtifact --track production` moves it on and
 * `publishListing` updates the listing. One Google Cloud service account serves every app: its key
 * is read from the Gradle property `play.credentials`, else
 * `~/.config/lillo-android-apps/play-service-account.json`, else `ANDROID_PUBLISHER_CREDENTIALS`.
 */
class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.application")
        pluginManager.apply("com.github.triplet.play")
        val keystoreFile = file("../keystore.properties")
        val keystore = Properties().apply { if (keystoreFile.exists()) keystoreFile.inputStream().use { load(it) } }
        extensions.configure<ApplicationExtension> {
            configureKotlinAndroid(this)
            defaultConfig.minSdk = libs.intVersion("appMinSdk")
            defaultConfig.targetSdk = libs.intVersion("targetSdk")
            androidResources.generateLocaleConfig = true
            signingConfigs.create("release") {
                if (keystoreFile.exists()) {
                    storeFile = file(keystore.getProperty("storeFile").trim())
                    storePassword = keystore.getProperty("storePassword")?.trim()
                    keyAlias = keystore.getProperty("keyAlias")?.trim()
                    keyPassword = keystore.getProperty("keyPassword")?.trim()
                }
            }
            buildTypes.getByName("release") {
                isMinifyEnabled = true
                isShrinkResources = true
                proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
                signingConfig = signingConfigs.getByName(if (keystoreFile.exists()) "release" else "debug")
            }
        }
        extensions.configure<PlayPublisherExtension> {
            defaultToAppBundles.set(true)
            track.set("internal")
            val credentials = providers.gradleProperty("play.credentials").orNull?.let(::File)
                ?: File(System.getProperty("user.home"), ".config/lillo-android-apps/play-service-account.json")
            if (credentials.isFile) serviceAccountCredentials.set(credentials)
        }
    }
}
