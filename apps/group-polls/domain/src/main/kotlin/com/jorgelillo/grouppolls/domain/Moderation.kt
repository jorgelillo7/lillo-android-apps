package com.jorgelillo.grouppolls.domain

/**
 * First line of defence for public polls: a short es/en list of slurs, sexual terms and the like.
 * It is deliberately small; reports, auto-hide and manual review do the rest.
 */
object Moderation {
    private val blocked: Set<String> = setOf(
        // Spanish
        "puta", "puto", "putas", "putos", "zorra", "maricon", "maricones", "polla", "coño", "follar", "folla",
        "mierda", "gilipollas", "subnormal", "retrasado", "negrata", "sudaca", "moro de mierda", "nazi",
        "porno", "sexo", "pene", "vagina", "violar", "violacion", "matar", "suicidio", "drogas", "cocaina",
        // English
        "fuck", "fucking", "shit", "bitch", "cunt", "dick", "pussy", "whore", "slut", "fag", "faggot",
        "nigger", "nigga", "retard", "porn", "sex", "rape", "kill", "suicide", "cocaine", "nazi"
    ).map(Text::normalize).toSet()

    /** True when [text] contains a blocked word or phrase as a whole word. */
    fun isBlocked(text: String): Boolean {
        val normalized = " " + Text.normalize(text).replace(Regex("[^a-z0-9ñ ]"), " ").replace(Regex("\\s+"), " ") + " "
        return blocked.any { normalized.contains(" $it ") }
    }
}
