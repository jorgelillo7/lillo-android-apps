package com.jorgelillo.buildlogic

import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

fun VersionCatalog.intVersion(alias: String): Int = findVersion(alias).get().requiredVersion.toInt()

/** The catalog's `compileSdk` ("37" or "37.1") as its API level and minor level. */
fun VersionCatalog.compileSdk(): Pair<Int, Int> {
    val parts = findVersion("compileSdk").get().requiredVersion.split('.')
    return parts[0].toInt() to (parts.getOrNull(1)?.toInt() ?: 0)
}
