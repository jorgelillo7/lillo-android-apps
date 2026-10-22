package com.jorgelillo.slipper.domain

/**
 * Pixel-art sprites as text, one character per pixel: the app draws them and the asset scripts
 * (icon, store graphics) read the same file. Format:
 *
 * ```
 * palette
 * K 2B1D1D        # a character and its RGB colour; '.' is always transparent
 * end
 * sprite kid_run_1
 * ..KK..
 * .KKKK.
 * end
 * ```
 */
class Sprite(val name: String, val width: Int, val height: Int, private val pixels: IntArray) {
    /** ARGB colour at ([x], [y]) from the top-left; 0 is transparent. */
    operator fun get(x: Int, y: Int): Int = pixels[y * width + x]
}

object Sprites {
    fun parse(text: String): Map<String, Sprite> {
        val palette = mutableMapOf('.' to 0)
        val sprites = mutableMapOf<String, Sprite>()
        var section: String? = null
        var rows = mutableListOf<String>()
        for (raw in text.lines()) {
            val line = raw.substringBefore('#').trimEnd()
            if (line.isBlank()) continue
            when {
                line == "palette" -> section = "palette"

                line.startsWith("sprite ") -> {
                    section = line.removePrefix("sprite ").trim()
                    rows = mutableListOf()
                }

                line == "end" -> {
                    val name = section
                    if (name != null && name != "palette") sprites[name] = build(name, rows, palette)
                    section = null
                }

                section == "palette" -> {
                    val (key, hex) = line.trim().split(Regex("\\s+"), limit = 2)
                    require(key.length == 1 && hex.length == 6) { "bad palette line: $line" }
                    palette[key[0]] = (0xFF000000 or hex.toLong(16)).toInt()
                }

                section != null -> rows += line.trim()
            }
        }
        return sprites
    }

    private fun build(name: String, rows: List<String>, palette: Map<Char, Int>): Sprite {
        require(rows.isNotEmpty() && rows.all { it.length == rows[0].length }) { "sprite $name: ragged rows" }
        val pixels = IntArray(rows.size * rows[0].length) { i ->
            val c = rows[i / rows[0].length][i % rows[0].length]
            palette[c] ?: error("sprite $name: '$c' is not in the palette")
        }
        return Sprite(name, rows[0].length, rows.size, pixels)
    }
}
