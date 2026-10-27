import androidx.baselineprofile.gradle.producer.BaselineProfileProducerExtension
import com.android.build.api.dsl.TestExtension
import com.jorgelillo.buildlogic.configureKotlinAndroid
import com.jorgelillo.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

/**
 * An app's `apps/<app>/baselineprofile` module: generates the app's baseline profile
 * (`./gradlew :apps:<app>:app:generateBaselineProfile`, on a connected phone or emulator) and runs
 * its startup Macrobenchmark (`:apps:<app>:baselineprofile:connectedBenchmarkReleaseAndroidTest`).
 * The module only declares its namespace (`<app id>.baselineprofile`) and one file with the app's
 * journeys, built on `:core:baselineprofile`. The app is its sibling `app` module, which picks the
 * profile up through `jorgelillo.android.application`.
 */
class AndroidBaselineProfileConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.test")
        pluginManager.apply("androidx.baselineprofile")
        extensions.configure<TestExtension> {
            configureKotlinAndroid(this)
            // Generating a profile without root needs Android 13; Macrobenchmark itself, Android 9.
            defaultConfig.minSdk = MIN_SDK
            defaultConfig.targetSdk = libs.findVersion("targetSdk").get().requiredVersion.toInt()
            defaultConfig.testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            // Emulators give noisy timings: fine for generating profiles and for comparing, flagged
            // in the results.
            defaultConfig.testInstrumentationRunnerArguments["androidx.benchmark.suppressErrors"] = "EMULATOR"
            targetProjectPath = path.substringBeforeLast(':') + ":app"
            experimentalProperties["android.experimental.self-instrumenting"] = true
        }
        extensions.configure<BaselineProfileProducerExtension> {
            useConnectedDevices = true
        }
        dependencies {
            add("implementation", project(":core:baselineprofile"))
        }
    }

    private companion object {
        const val MIN_SDK = 28
    }
}
