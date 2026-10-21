import com.android.build.api.dsl.CommonExtension
import com.jorgelillo.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.dependencies

/**
 * Unit tests that need Android classes (Context, DataStore, ICU, Intent) run on the JVM with
 * Robolectric: `@RunWith(AndroidJUnit4::class)` in `src/test`, no emulator.
 */
class AndroidRobolectricConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.withPlugin("com.android.base") {
            extensions.getByType(CommonExtension::class.java).testOptions.unitTests.isIncludeAndroidResources = true
        }
        // Robolectric reaches into FileDescriptor (SDK 36 shared memory); recent JDKs need it opened.
        tasks.withType(Test::class.java).configureEach { jvmArgs("--add-opens=java.base/java.io=ALL-UNNAMED", "--add-exports=java.base/jdk.internal.access=ALL-UNNAMED") }
        dependencies {
            add("testImplementation", libs.findLibrary("junit").get())
            add("testImplementation", libs.findLibrary("kotlin-test").get())
            add("testImplementation", libs.findLibrary("robolectric").get())
            add("testImplementation", libs.findLibrary("androidx-test-ext-junit").get())
            add("testImplementation", libs.findLibrary("kotlinx-coroutines-test").get())
        }
    }
}
