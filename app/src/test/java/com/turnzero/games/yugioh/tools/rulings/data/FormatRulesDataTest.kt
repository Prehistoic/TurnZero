package com.turnzero.games.yugioh.tools.rulings.data

import org.junit.Assert.*
import org.junit.Test

class FormatRulesDataTest {

    // -- Corrected dates ---------------------------------------------------------

    @Test
    fun `Goat date is Apr 2005`() {
        val meta = FormatRulesData.getMeta("Goat")
        assertEquals("Apr. 2005", meta?.dateLabel)
    }

    @Test
    fun `Stein date is Sep 2006`() {
        val meta = FormatRulesData.getMeta("Stein")
        assertEquals("Sep. 2006", meta?.dateLabel)
    }

    @Test
    fun `Trooper date is Jun 2007`() {
        val meta = FormatRulesData.getMeta("Trooper")
        assertEquals("Jun. 2007", meta?.dateLabel)
    }

    @Test
    fun `Circle date is Sep 2007`() {
        val meta = FormatRulesData.getMeta("Circle")
        assertEquals("Sep. 2007", meta?.dateLabel)
    }

    @Test
    fun `ReturnDAD date is Mar 2008`() {
        val meta = FormatRulesData.getMeta("ReturnDAD")
        assertEquals("Mar. 2008", meta?.dateLabel)
    }

    @Test
    fun `Gladiator date is May 2008`() {
        val meta = FormatRulesData.getMeta("Gladiator")
        assertEquals("May 2008", meta?.dateLabel)
    }

    @Test
    fun `TeleDAD date is Sep 2008`() {
        val meta = FormatRulesData.getMeta("TeleDAD")
        assertEquals("Sep. 2008", meta?.dateLabel)
    }

    @Test
    fun `Cat date is Mar 2009`() {
        val meta = FormatRulesData.getMeta("Cat")
        assertEquals("Mar. 2009", meta?.dateLabel)
    }

    @Test
    fun `Edison date is Mar 2010`() {
        val meta = FormatRulesData.getMeta("Edison")
        assertEquals("Mar. 2010", meta?.dateLabel)
    }

    @Test
    fun `Tengu date is Sep 2011`() {
        val meta = FormatRulesData.getMeta("Tengu")
        assertEquals("Sep. 2011", meta?.dateLabel)
    }

    @Test
    fun `Meadowlands date is Mar 2013`() {
        val meta = FormatRulesData.getMeta("Meadowlands")
        assertEquals("Mar. 2013", meta?.dateLabel)
    }

    @Test
    fun `FireWater date is Jan 2014`() {
        val meta = FormatRulesData.getMeta("FireWater")
        assertEquals("Jan. 2014", meta?.dateLabel)
    }

    @Test
    fun `Vegas date is Apr 2014`() {
        val meta = FormatRulesData.getMeta("Vegas")
        assertEquals("Apr. 2014", meta?.dateLabel)
    }

    @Test
    fun `HAT date is Apr 2014`() {
        val meta = FormatRulesData.getMeta("HAT")
        assertEquals("Apr. 2014", meta?.dateLabel)
    }

    @Test
    fun `Base Modern date is Current`() {
        val meta = FormatRulesData.getMeta("Base")
        assertEquals("Current", meta?.dateLabel)
    }

    // -- Chronological ordering ---------------------------------------------------

    @Test
    fun `Gladiator comes before TeleDAD in chronological order`() {
        val gladIndex = FormatRulesData.keyOrder["Gladiator"] ?: -1
        val teleIndex = FormatRulesData.keyOrder["TeleDAD"] ?: -1
        assertTrue("Gladiator ($gladIndex) should precede TeleDAD ($teleIndex)", gladIndex < teleIndex)
    }

    @Test
    fun `ReturnDAD comes before TeleDAD in chronological order`() {
        val retIndex = FormatRulesData.keyOrder["ReturnDAD"] ?: -1
        val teleIndex = FormatRulesData.keyOrder["TeleDAD"] ?: -1
        assertTrue("ReturnDAD ($retIndex) should precede TeleDAD ($teleIndex)", retIndex < teleIndex)
    }

    @Test
    fun `Meadowlands comes before FireWater in chronological order`() {
        val meadIndex = FormatRulesData.keyOrder["Meadowlands"] ?: -1
        val fwIndex = FormatRulesData.keyOrder["FireWater"] ?: -1
        assertTrue("Meadowlands ($meadIndex) should precede FireWater ($fwIndex)", meadIndex < fwIndex)
    }

    @Test
    fun `Vegas and HAT share Apr 2014, Vegas precedes HAT by event date`() {
        val vegasIndex = FormatRulesData.keyOrder["Vegas"] ?: -1
        val hatIndex = FormatRulesData.keyOrder["HAT"] ?: -1
        assertTrue("Vegas ($vegasIndex) should precede HAT ($hatIndex)", vegasIndex < hatIndex)
    }

    @Test
    fun `Circle precedes ReturnDAD`() {
        val circleIndex = FormatRulesData.keyOrder["Circle"] ?: -1
        val retIndex = FormatRulesData.keyOrder["ReturnDAD"] ?: -1
        assertTrue("Circle ($circleIndex) should precede ReturnDAD ($retIndex)", circleIndex < retIndex)
    }

    // -- sortByChronological ------------------------------------------------------

    @Test
    fun `sortByChronological returns keys in correct order`() {
        val unsorted = listOf("HAT", "Goat", "Edison", "Stein")
        val sorted = FormatRulesData.sortByChronological(unsorted)
        assertEquals(listOf("Goat", "Stein", "Edison", "HAT"), sorted)
    }

    @Test
    fun `sortByChronological puts unknown keys at end`() {
        val unsorted = listOf("Unknown", "Goat", "ZZZ")
        val sorted = FormatRulesData.sortByChronological(unsorted)
        assertEquals("Goat", sorted[0])
        assertTrue(sorted[1] != sorted[2])
    }

    @Test
    fun `sortByChronological preserves input size`() {
        val unsorted = listOf("Stein", "Cat", "Edison", "Tengu")
        val sorted = FormatRulesData.sortByChronological(unsorted)
        assertEquals(unsorted.size, sorted.size)
        assertEquals(unsorted.toSet(), sorted.toSet())
    }

    // -- Metadata lookups ---------------------------------------------------------

    @Test
    fun `getMeta returns null for unknown format`() {
        assertNull(FormatRulesData.getMeta("NonExistent"))
    }

    @Test
    fun `getMeta is case-insensitive`() {
        val meta = FormatRulesData.getMeta("goat")
        assertNotNull(meta)
        assertEquals("Goat", meta?.displayName)
    }

    @Test
    fun `getDisplayName returns display name for known format`() {
        assertEquals("Fire/Water", FormatRulesData.getDisplayName("FireWater"))
    }

    @Test
    fun `getDisplayName returns key for unknown format`() {
        assertEquals("UnknownFormat", FormatRulesData.getDisplayName("UnknownFormat"))
    }

    @Test
    fun `getLabelWithDate combines name and date`() {
        assertEquals("Goat - Apr. 2005", FormatRulesData.getLabelWithDate("Goat"))
    }

    @Test
    fun `getLabelWithDate returns key for unknown format`() {
        assertEquals("ZZZ", FormatRulesData.getLabelWithDate("ZZZ"))
    }

    // -- Structural properties ---------------------------------------------------

    @Test
    fun `allFormats contains exactly 15 entries`() {
        assertEquals(15, FormatRulesData.allFormats.size)
    }

    @Test
    fun `all keys are unique`() {
        val keys = FormatRulesData.allFormats.map { it.key }
        assertEquals(keys.size, keys.toSet().size)
    }

    @Test
    fun `defaultEnabledKeys has five formats`() {
        val defaultKeys = FormatRulesData.defaultEnabledKeys
        assertEquals(5, defaultKeys.size)
        assertTrue(defaultKeys.contains("Goat"))
        assertTrue(defaultKeys.contains("Edison"))
        assertTrue(defaultKeys.contains("Tengu"))
        assertTrue(defaultKeys.contains("HAT"))
        assertTrue(defaultKeys.contains("Base"))
    }

    @Test
    fun `formats with rules have non-empty rule lists`() {
        val formatsWithRules = FormatRulesData.allFormats.filter { it.rules.isNotEmpty() }
        val keysWithRules = formatsWithRules.map { it.key }.toSet()
        assertEquals(setOf("Goat", "Edison", "Tengu", "HAT"), keysWithRules)
        for (meta in formatsWithRules) {
            assertTrue("${meta.key} rules should not be empty", meta.rules.isNotEmpty())
        }
    }

    @Test
    fun `keyOrder maps all format keys to their indices`() {
        for (i in FormatRulesData.allFormats.indices) {
            val key = FormatRulesData.allFormats[i].key
            assertEquals(i, FormatRulesData.keyOrder[key])
        }
    }

    @Test
    fun `date labels follow Mon YYYY or Current format`() {
        val datePattern = Regex("^(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)\\.? \\d{4}$")
        for (meta in FormatRulesData.allFormats) {
            if (meta.dateLabel == "Current") continue
            assertTrue(
                "Date label '${meta.dateLabel}' for ${meta.key} doesn't match expected format",
                meta.dateLabel.matches(datePattern)
            )
        }
    }

    @Test
    fun `labelWithDate includes displayName and dateLabel`() {
        for (meta in FormatRulesData.allFormats) {
            assertEquals("${meta.displayName} - ${meta.dateLabel}", meta.labelWithDate)
        }
    }
}
