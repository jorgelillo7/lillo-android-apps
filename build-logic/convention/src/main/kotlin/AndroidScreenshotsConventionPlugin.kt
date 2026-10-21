import com.jorgelillo.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * Screenshot tests of Compose UI on the JVM (Roborazzi on Robolectric), written with
 * `captureScreenshot` from :core:screenshot-testing. Images live under `src/test/screenshots/`
 * (committed); `./gradlew recordRoborazziDebug` rewrites them after a deliberate change,
 * `verifyRoborazziDebug` (CI) fails when a screen changed by accident.
 */
class AndroidScreenshotsConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("jorgelillo.android.robolectric")
        pluginManager.apply("io.github.takahirom.roborazzi")
        dependencies {
            // captureScreenshot(), the shared tolerance, and Roborazzi / Robolectric / Compose test APIs.
            add("testImplementation", project(":core:screenshot-testing"))
            add("debugImplementation", libs.findLibrary("androidx-compose-ui-test-manifest").get())
        }
    }
}
