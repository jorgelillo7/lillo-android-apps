package com.jorgelillo.tournaments.domain

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LayoutStatsLibraryTest {

    private fun played(names: List<String>, id: String = "t", game: String = "Magic", seed: Int = 1): Tournament {
        var t = Tournaments.create(id, "Cup", 0, names, Format.ELIMINATION, game = game, random = Random(seed))
        while (Tournaments.pending(t).isNotEmpty()) t = Tournaments.record(t, Tournaments.pending(t).first(), 2, 1)
        return t
    }

    @Test
    fun twoSidedLayoutForEightPlayers() {
        val grid = BracketLayout.of(3)
        assertEquals(5, grid.columns)
        assertEquals(2, grid.rows)
        assertEquals(BracketLayout.Side.LEFT, grid.cell(1, 1)!!.side)
        assertEquals(BracketLayout.Side.RIGHT, grid.cell(1, 2)!!.side)
        assertEquals(4, grid.cell(1, 3)!!.column)
        assertEquals(0.5f, grid.cell(2, 0)!!.y)
        assertEquals(3, grid.cell(2, 1)!!.column)
        assertEquals(2, grid.cell(3, 0)!!.column)
        assertEquals(BracketLayout.Side.CENTER, grid.cell(3, 0)!!.side)
        assertEquals(7, grid.cells.size)
    }

    @Test
    fun aFinalAloneIsCentred() {
        assertEquals(1, BracketLayout.of(1).columns)
    }

    @Test
    fun championPathEndsAtTheFinal() {
        val t = played((1..8).map { "P$it" })
        val path = BracketLayout.pathOf(t, t.champion!!).sortedBy { it.round }
        assertEquals(3, path.size)
        assertEquals(listOf(1, 2, 3), path.map { it.round })
        assertEquals(t.round(Stage.ELIMINATION, t.eliminationRounds).single(), path.last())
    }

    @Test
    fun statsMatchPlayersByName() {
        val a = played(listOf("Pepe", "Luis", "Ana", "Marta"), id = "a")
        val b = played(listOf(" pepe ", "luis", "Ana"), id = "b", game = "Pokémon", seed = 7)
        val all = Stats.of(listOf(a, b))
        assertEquals(4, all.size)
        assertEquals(2, all.single { it.name.equals("pepe", true) }.tournaments)
        assertEquals(2, all.sumOf { it.titles })
        assertEquals(1, Stats.of(listOf(a, b), game = "pokémon").sumOf { it.titles })
        assertTrue(all.first().titles >= 1)
    }

    /** Titles first, then finals, then match win rate; each column counted per player. */
    @Test
    fun theHallOfFameRanksByTitlesThenFinalsThenWinRate() {
        fun players(vararg names: String) = names.mapIndexed { i, n -> Player(i + 1, n) }
        fun m(stage: Stage, round: Int, slot: Int, a: Int, b: Int, winsA: Int, winsB: Int) =
            Match(stage, round, slot, a, b, winsA, winsB)
        val e = Stage.ELIMINATION
        // X beats P and then Y in the final; Y beat Q to get there.
        val cup = Tournament(
            "cup",
            "Cup",
            epochDay = 0,
            players = players("X", "Y", "P", "Q"),
            matches = listOf(m(e, 1, 0, 1, 3, 2, 0), m(e, 1, 1, 2, 4, 2, 1), m(e, 2, 0, 1, 2, 2, 0))
        )
        // Three Swiss rounds, no cut: Lead wins all (the title), W wins two, L1 one, L2 none.
        val s = Stage.SWISS
        val league = Tournament(
            "league",
            "League",
            epochDay = 0,
            format = Format.SWISS,
            swissRounds = 3,
            players = players("Lead", "W", "L1", "L2"),
            matches = listOf(
                m(s, 1, 0, 1, 2, 2, 0),
                m(s, 1, 1, 3, 4, 2, 0),
                m(s, 2, 0, 2, 3, 2, 0),
                m(s, 2, 1, 1, 4, 2, 0),
                m(s, 3, 0, 2, 4, 2, 0),
                m(s, 3, 1, 1, 3, 2, 0)
            )
        )
        val rows = Stats.of(listOf(cup, league))
        fun row(name: String) = rows.single { it.name == name }

        assertEquals(1, row("X").titles)
        assertEquals(1, row("Lead").titles)
        assertEquals(1, row("X").finals)
        assertEquals(1, row("Y").finals)
        assertEquals(0, row("Lead").finals)
        assertEquals(0.5, row("Y").winRate)
        assertEquals(2.0 / 3, row("W").winRate)
        // X before Lead: same titles, more finals. Lead before Y: a title beats a final.
        // Y before W: a final beats a better win rate. W before L1: the win rate decides.
        assertEquals(listOf("X", "Lead", "Y", "W", "L1"), rows.take(5).map { it.name })
        assertEquals(setOf("P", "Q", "L2"), rows.drop(5).map { it.name }.toSet())
    }

    @Test
    fun csvQuotesCommas() {
        var t = Tournaments.create("t", "Cup, 2026", 0, listOf("Pepe", "Luis"), Format.ELIMINATION)
        t = Tournaments.record(t, t.matches.single(), 2, 0, "20 vidas a 0, \"fácil\"")
        val lines = Stats.csv(t) { "Final" }.lines()
        assertTrue(lines[1].startsWith("\"Cup, 2026\""))
        assertTrue(lines[1].endsWith("\"20 vidas a 0, \"\"fácil\"\"\""))
    }

    @Test
    fun importKeepsTheNewestCopy() {
        val t = Tournaments.create("t", "Cup", 0, listOf("Pepe", "Luis"), Format.ELIMINATION)
        val played = Tournaments.record(t, t.matches.single(), 2, 0)
        val (lib, added) = Library().import(t)
        assertEquals(Library.ImportResult.ADDED, added)
        val (updated, result) = lib.import(played)
        assertEquals(Library.ImportResult.UPDATED, result)
        assertEquals(Library.ImportResult.NEWER_HERE, updated.import(t).second)
        assertEquals(Library.ImportResult.ALREADY_UP_TO_DATE, updated.import(played).second)
        assertEquals(listOf("Pepe", "Luis"), updated.knownNames.sorted().reversed())
    }

    /**
     * Only a newer copy of a tournament already here replaces something, so only that one is
     * asked about: a phone's own results could be in the copy it would overwrite.
     */
    @Test
    fun onlyANewerCopyReplacesWhatIsHere() {
        val t = Tournaments.create("t", "Cup", 0, listOf("Pepe", "Luis"), Format.ELIMINATION)
        val played = Tournaments.record(t, t.matches.single(), 2, 0)
        val lib = Library().save(t)
        assertEquals(t, lib.replacedBy(played))
        assertEquals(null, lib.replacedBy(t))
        assertEquals(null, lib.save(played).replacedBy(t))
        assertEquals(null, Library().replacedBy(played))
        assertEquals(0, t.playedMatches)
        assertEquals(1, played.playedMatches)
    }
}
