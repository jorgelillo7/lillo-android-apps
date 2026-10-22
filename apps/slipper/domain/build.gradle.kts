plugins {
    alias(libs.plugins.jorgelillo.jvm.library)
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    api(libs.kotlinx.serialization.json)
    implementation(projects.core.sharelink)
    testImplementation(libs.kotlin.test)
}
