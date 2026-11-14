import org.gradle.api.Plugin
import org.gradle.api.artifacts.Configuration
import org.gradle.api.initialization.Settings

/**
 * Lifts transitive libraries with a published advisory to their first patched release, in every
 * configuration of every project and in each project's build-script classpath (AGP, Play Publisher).
 * A request already at or above the floor is left alone; Guava keeps its -android/-jre flavour
 * (-jre for the old unflavoured releases).
 * Drop a line once the library that pulls the old version ships the fix itself.
 */
class SecurityFloorsSettingsPlugin : Plugin<Settings> {
    override fun apply(target: Settings) {
        target.gradle.allprojects {
            buildscript.configurations.configureEach { liftToFloors() }
            configurations.configureEach { liftToFloors() }
        }
    }
}

/** group:name → first release without the advisory (Dependabot alerts of the dependency graph). */
internal val securityFloors = mapOf(
    "org.bouncycastle:bcprov-jdk18on" to "1.85",
    "org.bouncycastle:bcpkix-jdk18on" to "1.85",
    "org.bouncycastle:bcutil-jdk18on" to "1.85",
    "com.google.guava:guava" to "33.7.2",
    "org.jsoup:jsoup" to "1.23.2",
    "com.squareup.wire:wire-runtime" to "6.4.5",
    "com.squareup.wire:wire-runtime-jvm" to "6.4.5",
    "org.bitbucket.b_c:jose4j" to "0.9.6",
    "org.jdom:jdom2" to "2.0.6.1",
    "org.apache.commons:commons-lang3" to "3.18.0",
    "org.apache.httpcomponents:httpclient" to "4.5.14",
    "com.google.protobuf:protobuf-javalite" to "3.25.5"
)

private fun Configuration.liftToFloors() = resolutionStrategy.eachDependency {
    val module = "${requested.group}:${requested.name}"
    val floor = securityFloors[module] ?: return@eachDependency
    val version = requested.version?.takeIf { v -> v.none { it in "[]()+," } } ?: return@eachDependency
    liftedVersion(module, version, floor)?.let {
        useVersion(it)
        because("first release without the published advisory")
    }
}

/** The version of [module] to use instead of [requested], or null when it already meets [floor]. */
internal fun liftedVersion(module: String, requested: String, floor: String): String? {
    val numbers = { v: String -> v.substringBefore('-').split('.').map { it.toIntOrNull() ?: 0 } }
    val have = numbers(requested)
    val want = numbers(floor)
    for (i in 0 until maxOf(have.size, want.size)) {
        val h = have.getOrElse(i) { 0 }
        val w = want.getOrElse(i) { 0 }
        if (h != w) return if (h > w) null else floor + flavour(module, requested)
    }
    return null
}

private fun flavour(module: String, requested: String): String {
    val suffix = requested.substringAfter('-', "")
    return when {
        suffix.isNotEmpty() -> "-$suffix"
        module == "com.google.guava:guava" -> "-jre"
        else -> ""
    }
}
