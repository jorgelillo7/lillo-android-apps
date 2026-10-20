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
// Same folder as the app's build.gradle.kts: the leagueCards.privateDir property, else the default.
tasks.test {
    val privateDeckDir = providers.gradleProperty("leagueCards.privateDir").orNull?.let(::File)
        ?: rootDir.resolve("../lloros-league/content")
    val deck = privateDeckDir.resolve("league-private.json")
    if (deck.isFile) inputs.file(deck).withPropertyName("privateDeck")
    systemProperty("leagueCards.privateDeck", deck.path)
}
