package com.jorgelillo.whoslying.domain

/**
 * Built-in packs, written for this app ([spanishPacks], [englishPacks]). Each line is "word /
 * similar word / similar word": civilians get the first, the impostor (CLASSIC and DRIFTER modes)
 * one of the others.
 */
object WordPacks {

    fun builtIn(language: String): List<WordPack> = if (language == "es") spanishPacks else englishPacks

    internal fun pack(id: String, name: String, emoji: String, pairs: String) = WordPack(
        id = id,
        name = name,
        emoji = emoji,
        entries = parseEntries(pairs)
    )

    /**
     * One entry per line: "Playa / Piscina / Lago" → Entry(Playa, [Piscina, Lago]); "Playa" →
     * Entry(Playa). Blank lines, empty or repeated similar words and repeated entries are dropped.
     */
    fun parseEntries(text: String): List<Entry> = text.lines()
        .map { line -> line.split("/").map { it.trim() } }
        .filter { it.first().isNotEmpty() }
        .map { parts ->
            val word = parts.first()
            Entry(word, parts.drop(1).filter { it.isNotEmpty() && !it.equals(word, ignoreCase = true) }.distinctBy { it.lowercase() })
        }
        .distinctBy { it.word.lowercase() }

    /** Inverse of [parseEntries] for one entry. */
    fun toLine(entry: Entry): String = (listOf(entry.word) + entry.decoys).joinToString(" / ")
}
