plugins {
    alias(libs.plugins.jorgelillo.jvm.library)
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    api(libs.kotlinx.serialization.json)
    testImplementation(libs.kotlin.test)
}

// GameTest also checks the league's private deck when it is on this machine (outside this repo,
// see the app's README): rerun the tests whenever that deck changes.
tasks.test {
    val deck = rootDir.resolve("../lloros-league/content/league-private.json")
    if (deck.isFile) inputs.file(deck).withPropertyName("privateDeck")
}
