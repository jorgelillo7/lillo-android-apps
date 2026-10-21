package com.jorgelillo.christmascountdown.domain

import kotlin.math.pow

/** A note as a MIDI number (null = rest) and its length in beats. */
data class Note(val midi: Int?, val beats: Double) {
    val frequency: Double? get() = midi?.let { 440.0 * 2.0.pow((it - 69) / 12.0) }
}

data class Melody(val title: String, val bpm: Int, val notes: List<Note>, val beatsPerBar: Int = 4) {
    val totalBeats: Double get() = notes.sumOf { it.beats }

    companion object {
        private val PITCH_CLASSES = mapOf('C' to 0, 'D' to 2, 'E' to 4, 'F' to 5, 'G' to 7, 'A' to 9, 'B' to 11)

        /**
         * Parses a compact score: space-separated `<pitch><octave>:<beats>` tokens, e.g. `F#5:0.5`,
         * with `R:<beats>` for rests and `|` as an optional bar separator.
         */
        fun parse(title: String, bpm: Int, score: String, beatsPerBar: Int = 4): Melody {
            val notes = score.split(Regex("\\s+")).filter { it.isNotBlank() && it != "|" }.map { token ->
                val (pitch, beats) = token.split(':').let { it[0] to it[1].toDouble() }
                if (pitch == "R") return@map Note(null, beats)
                val pitchClass = PITCH_CLASSES.getValue(pitch[0])
                val sharp = if (pitch[1] == '#') 1 else 0
                val octave = pitch.substring(1 + sharp).toInt()
                Note(12 * (octave + 1) + pitchClass + sharp, beats)
            }
            return Melody(title, bpm, notes, beatsPerBar)
        }
    }
}

/** Public-domain carols, transcribed one octave up so they sound like a music box. */
object Carols {

    val jingleBells = Melody.parse(
        title = "Jingle Bells",
        bpm = 132,
        score = """
            E5:1 E5:1 E5:2 | E5:1 E5:1 E5:2 | E5:1 G5:1 C5:1.5 D5:0.5 | E5:4
            F5:1 F5:1 F5:1.5 F5:0.5 | F5:1 E5:1 E5:1 E5:0.5 E5:0.5 | E5:1 D5:1 D5:1 E5:1 | D5:2 G5:2
            E5:1 E5:1 E5:2 | E5:1 E5:1 E5:2 | E5:1 G5:1 C5:1.5 D5:0.5 | E5:4
            F5:1 F5:1 F5:1.5 F5:0.5 | F5:1 E5:1 E5:1 E5:0.5 E5:0.5 | G5:1 G5:1 F5:1 D5:1 | C5:4
        """
    )

    val weWishYouAMerryChristmas = Melody.parse(
        title = "We Wish You a Merry Christmas",
        bpm = 150,
        beatsPerBar = 3,
        score = """
            R:2 D5:1 | G5:1 G5:0.5 A5:0.5 G5:0.5 F#5:0.5 | E5:1 E5:1 E5:1
            A5:1 A5:0.5 B5:0.5 A5:0.5 G5:0.5 | F#5:1 D5:1 D5:1
            B5:1 B5:0.5 C6:0.5 B5:0.5 A5:0.5 | G5:1 E5:1 D5:0.5 D5:0.5 | E5:1 A5:1 F#5:1 | G5:3
        """
    )

    val silentNight = Melody.parse(
        title = "Silent Night",
        bpm = 84,
        beatsPerBar = 3,
        score = """
            G5:1.5 A5:0.5 G5:1 | E5:3 | G5:1.5 A5:0.5 G5:1 | E5:3
            D6:2 D6:1 | B5:3 | C6:2 C6:1 | G5:3
            A5:2 A5:1 | C6:1.5 B5:0.5 A5:1 | G5:1.5 A5:0.5 G5:1 | E5:3
            A5:2 A5:1 | C6:1.5 B5:0.5 A5:1 | G5:1.5 A5:0.5 G5:1 | E5:3
            D6:2 D6:1 | F6:1.5 D6:0.5 B5:1 | C6:3 | E6:3
            C6:1 G5:1 E5:1 | G5:1.5 F5:0.5 D5:1 | C5:3 | R:3
        """
    )

    val oChristmasTree = Melody.parse(
        title = "O Christmas Tree",
        bpm = 96,
        beatsPerBar = 3,
        score = """
            C5:1 | F5:0.75 F5:0.25 F5:1.5 G5:0.5 | A5:0.75 A5:0.25 A5:1.5 A5:0.5
            G5:0.5 A5:0.5 A#5:1 E5:1 | G5:1 F5:1 R:0.5 C6:0.5
            C6:0.5 A5:0.5 D6:1.5 C6:0.5 | C6:0.5 A#5:0.5 A#5:1.5 A#5:0.5
            A#5:0.5 G5:0.5 C6:1.5 A#5:0.5 | A#5:0.5 A5:0.5 A5:1 C5:1
            F5:0.75 F5:0.25 F5:1.5 G5:0.5 | A5:0.75 A5:0.25 A5:1.5 A5:0.5
            G5:0.5 A5:0.5 A#5:1 E5:1 | G5:1 F5:2 | R:2
        """
    )

    val joyToTheWorld = Melody.parse(
        title = "Joy to the World",
        bpm = 112,
        beatsPerBar = 2,
        score = """
            D6:1 C#6:0.75 B5:0.25 | A5:1.5 G5:0.5 | F#5:1 E5:1 | D5:1.5 A5:0.5
            B5:1.5 B5:0.5 | C#6:1.5 C#6:0.5 | D6:1.5 D6:0.5
            D6:0.5 C#6:0.5 B5:0.5 A5:0.5 | A5:0.75 G5:0.25 F#5:0.5 D6:0.5
            D6:0.5 C#6:0.5 B5:0.5 A5:0.5 | A5:0.75 G5:0.25 F#5:0.5 F#5:0.5
            F#5:0.5 F#5:0.5 F#5:0.5 F#5:0.25 G5:0.25 | A5:1.5 G5:0.25 F#5:0.25
            E5:0.5 E5:0.5 E5:0.5 E5:0.25 F#5:0.25 | G5:1.5 F#5:0.25 E5:0.25
            D5:0.5 D6:1 B5:0.5 | A5:0.75 G5:0.25 F#5:0.5 G5:0.5 | F#5:1 E5:1 | D5:2
        """
    )

    val all: List<Melody> = listOf(jingleBells, weWishYouAMerryChristmas, silentNight, oChristmasTree, joyToTheWorld)
}
