package com.jorgelillo.christmascountdown.domain

/** The three parts of December, in order: get the house ready, do things at home, get together. */
enum class AdventBlock(val days: IntRange) {
    PREPARE(1..7),
    AT_HOME(8..16),
    TOGETHER(17..23)
}

/**
 * Everything a door can hold. The names are stable ids (texts live in the app's resources, keyed
 * by them), so never rename one: past years' calendars are recomputed from these ids.
 * Add new ones between January and November, never during an Advent in progress.
 */
enum class AdventActivity(val block: AdventBlock?) {
    // Get the house ready
    LETTER(AdventBlock.PREPARE),
    PAPER_SNOWFLAKES(AdventBlock.PREPARE),
    NATIVITY_OR_TREE(AdventBlock.PREPARE),
    DOOR_GARLAND(AdventBlock.PREPARE),
    PAPER_CHAIN(AdventBlock.PREPARE),
    STOCKING(AdventBlock.PREPARE),
    TREE_ORNAMENT(AdventBlock.PREPARE),
    WINDOW_LIGHTS(AdventBlock.PREPARE),
    PAPER_STARS(AdventBlock.PREPARE),
    GLITTER_PINECONES(AdventBlock.PREPARE),
    TABLE_WREATH(AdventBlock.PREPARE),

    // At home
    MOVIE_NIGHT(AdventBlock.AT_HOME),
    COOKIES(AdventBlock.AT_HOME),
    CHRISTMAS_CARDS(AdventBlock.AT_HOME),
    CHOCOLATE_CHURROS(AdventBlock.AT_HOME),
    DRAWING_GIFT(AdventBlock.AT_HOME),
    SING_A_CAROL(AdventBlock.AT_HOME),
    SOCK_SNOWMAN(AdventBlock.AT_HOME),
    JAR_LANTERN(AdventBlock.AT_HOME),
    FAMILY_STORY(AdventBlock.AT_HOME),
    TREE_PIZZA(AdventBlock.AT_HOME),
    SNOW_GLOBE(AdventBlock.AT_HOME),
    POLVORONES(AdventBlock.AT_HOME),

    // Get together and go out
    DONATE(AdventBlock.TOGETHER),
    LIGHTS_WALK(AdventBlock.TOGETHER),
    BOARD_GAMES(AdventBlock.TOGETHER),
    SECRET_SANTA(AdventBlock.TOGETHER),
    CHRISTMAS_MARKET(AdventBlock.TOGETHER),
    UGLY_SWEATERS(AdventBlock.TOGETHER),
    PARADE_OR_CONCERT(AdventBlock.TOGETHER),
    ICE_SKATING(AdventBlock.TOGETHER),
    VISIT_NATIVITY(AdventBlock.TOGETHER),
    FAMILY_PHOTO(AdventBlock.TOGETHER),
    CAROL_KARAOKE(AdventBlock.TOGETHER),

    /** Always door 24. */
    CHRISTMAS_EVE(null)
}

/** Music days: a song to listen to, opened on YouTube (official videos where they exist). */
enum class AdventSong(val title: String, val artist: String, val videoId: String) {
    ALL_I_WANT("All I Want for Christmas Is You", "Mariah Carey", "yXQViqx6GMY"),
    LAST_CHRISTMAS("Last Christmas", "Wham!", "E8gmARGvPlI"),
    TAMBORILERO("El tamborilero", "Raphael", "CHWY9KwXSWc"),
    BURRITO_SABANERO("Mi burrito sabanero", "La Rondallita", "xYX6pS3TvcU"),
    JINGLE_BELL_ROCK("Jingle Bell Rock", "Bobby Helms", "ZnDmmiiFSUU"),
    CAMPANA_SOBRE_CAMPANA("Campana sobre campana", "los Niños Cantores de Navidad", "CgRLvKeT9ss")
    ;

    val url: String get() = "https://www.youtube.com/watch?v=$videoId"
}

/** What one door holds: an activity or a song. */
sealed interface AdventDoor {
    data class Activity(val activity: AdventActivity) : AdventDoor
    data class Song(val song: AdventSong) : AdventDoor
}

/**
 * Which activity or song goes behind each door of a year's calendar.
 *
 * Every year picks, block by block, the activities used least in the years before (ties broken by
 * a fixed per-year shuffle), so the calendar changes from one year to the next and everything
 * comes round. The history is not stored anywhere: it is recomputed from [FIRST_YEAR], so every
 * phone shows the same calendar, also after a reinstall. Two or three days between the 8th and
 * the 23rd are music days, chosen the same way. Door 24 is always Christmas Eve.
 */
object AdventPlan {

    const val FIRST_YEAR = 2026
    private val MUSIC_DAYS = 8..23

    /** Door number (1..24) to its content for [year]. */
    fun forYear(year: Int): Map<Int, AdventDoor> {
        val activityUses = AdventActivity.entries.associateWith { 0 }.toMutableMap()
        val songUses = AdventSong.entries.associateWith { 0 }.toMutableMap()
        var plan = emptyMap<Int, AdventDoor>()
        for (y in FIRST_YEAR..maxOf(year, FIRST_YEAR)) {
            plan = plan(y, activityUses, songUses)
            plan.values.forEach { door ->
                when (door) {
                    is AdventDoor.Activity -> activityUses.merge(door.activity, 1, Int::plus)
                    is AdventDoor.Song -> songUses.merge(door.song, 1, Int::plus)
                }
            }
        }
        return plan
    }

    private fun plan(year: Int, activityUses: Map<AdventActivity, Int>, songUses: Map<AdventSong, Int>): Map<Int, AdventDoor> {
        val random = SplitMix(year.toLong() * 31 + 7)
        val musicCount = 2 + (random.next(2))
        val musicDays = MUSIC_DAYS.toList().shuffled(random).take(musicCount).toSet()
        val songs = leastUsed(AdventSong.entries, songUses, musicCount, random)
        val plan = mutableMapOf<Int, AdventDoor>(AdventCalendar.DOORS to AdventDoor.Activity(AdventActivity.CHRISTMAS_EVE))
        musicDays.sorted().zip(songs.shuffled(random)).forEach { (day, song) -> plan[day] = AdventDoor.Song(song) }
        for (block in AdventBlock.entries) {
            val days = block.days.filterNot { it in musicDays }
            val pool = AdventActivity.entries.filter { it.block == block }
            val picked = leastUsed(pool, activityUses, days.size, random).shuffled(random)
            days.zip(picked).forEach { (day, activity) -> plan[day] = AdventDoor.Activity(activity) }
        }
        return plan
    }

    /** The [count] items used least so far; ties in a shuffled order. */
    private fun <T> leastUsed(items: List<T>, uses: Map<T, Int>, count: Int, random: SplitMix): List<T> = items.shuffled(random).sortedBy { uses.getValue(it) }.take(count)

    private fun <T> List<T>.shuffled(random: SplitMix): List<T> {
        val list = toMutableList()
        for (i in list.lastIndex downTo 1) {
            val j = random.next(i + 1)
            list[i] = list[j].also { list[j] = list[i] }
        }
        return list
    }

    /** Own generator (SplitMix64), so plans never change when the standard library does. */
    private class SplitMix(seed: Long) {
        private var state = seed * 0x9E3779B97F4A7C15uL.toLong()

        fun next(bound: Int): Int {
            state += 0x9E3779B97F4A7C15uL.toLong()
            var z = state
            z = (z xor (z ushr 30)) * 0xBF58476D1CE4E5B9uL.toLong()
            z = (z xor (z ushr 27)) * 0x94D049BB133111EBuL.toLong()
            z = z xor (z ushr 31)
            return java.lang.Long.remainderUnsigned(z, bound.toLong()).toInt()
        }
    }
}
