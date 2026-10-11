package com.jorgelillo.christmascountdown.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class AdventPlanTest {

    private val years = AdventPlan.FIRST_YEAR..AdventPlan.FIRST_YEAR + 12

    @Test
    fun everyDoorHasSomethingAndNothingRepeatsInAYear() {
        for (year in years) {
            val plan = AdventPlan.forYear(year)
            assertEquals((1..24).toSet(), plan.keys, "year $year")
            assertEquals(plan.size, plan.values.toSet().size, "repeat in $year")
        }
    }

    @Test
    fun christmasEveIsAlwaysDoor24() {
        for (year in years) assertEquals(AdventDoor.Activity(AdventActivity.CHRISTMAS_EVE), AdventPlan.forYear(year)[24])
    }

    @Test
    fun activitiesStayInTheirBlockAndSongsBetweenThe8thAndThe23rd() {
        for (year in years) {
            val plan = AdventPlan.forYear(year)
            val songDays = plan.filterValues { it is AdventDoor.Song }.keys
            assertTrue(songDays.size in 2..3, "year $year: ${songDays.size} songs")
            assertTrue(songDays.all { it in 8..23 })
            plan.forEach { (day, door) ->
                if (door is AdventDoor.Activity && day != 24) assertTrue(day in door.activity.block!!.days, "$year: $door on $day")
            }
        }
    }

    @Test
    fun theCalendarChangesEveryYearAndEverythingComesRound() {
        assertNotEquals(AdventPlan.forYear(2026), AdventPlan.forYear(2027))
        // After three Decembers every activity has been out at least once…
        val firstThree = (2026..2028).flatMap { AdventPlan.forYear(it).values }.toSet()
        AdventActivity.entries.forEach { assertTrue(AdventDoor.Activity(it) in firstThree, "$it never came out") }
        // …and the least-used rule keeps uses within one of each other in every block.
        val uses = years.flatMap { AdventPlan.forYear(it).values }.filterIsInstance<AdventDoor.Activity>()
            .groupingBy { it.activity }.eachCount()
        for (block in AdventBlock.entries) {
            val counts = AdventActivity.entries.filter { it.block == block }.map { uses[it] ?: 0 }
            assertTrue(counts.max() - counts.min() <= 1, "$block uneven: $counts")
        }
    }

    @Test
    fun the2026CalendarNeverChanges() {
        // Snapshot: users see this calendar from December 2026, and later years are computed from it.
        val actual = (1..24).joinToString(",") { day ->
            when (val door = AdventPlan.forYear(2026).getValue(day)) {
                is AdventDoor.Activity -> door.activity.name
                is AdventDoor.Song -> "♪" + door.song.name
            }
        }
        assertEquals(EXPECTED_2026, actual)
    }

    @Test
    fun aYearsPlanIsStable() {
        assertEquals(AdventPlan.forYear(2026), AdventPlan.forYear(2026))
        assertEquals(AdventPlan.forYear(2020), AdventPlan.forYear(2026)) // before the first year: the first year
    }

    private companion object {
        const val EXPECTED_2026 = "PAPER_CHAIN,TABLE_WREATH,STOCKING,TREE_ORNAMENT,WINDOW_LIGHTS,NATIVITY_OR_TREE," +
            "GLITTER_PINECONES,SNOW_GLOBE,CHRISTMAS_CARDS,POLVORONES,JAR_LANTERN,CHOCOLATE_CHURROS,FAMILY_STORY," +
            "TREE_PIZZA,MOVIE_NIGHT,SOCK_SNOWMAN,♪CAMPANA_SOBRE_CAMPANA,♪JINGLE_BELL_ROCK,CHRISTMAS_MARKET," +
            "♪BURRITO_SABANERO,DONATE,ICE_SKATING,CAROL_KARAOKE,CHRISTMAS_EVE"
    }
}
