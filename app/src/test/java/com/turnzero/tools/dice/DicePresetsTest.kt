package com.turnzero.tools.dice

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DicePresetsTest {

    @Test
    fun `getPresetsForGame returns yugioh presets unchanged`() {
        val presets = getPresetsForGame("yugioh")
        assertEquals(listOf("1d6", "2d6"), presets.map { it.name })
        assertTrue(presets.flatMap { it.dice }.all { it.color == DiceColor.WHITE })
        assertTrue(presets.all { it.gameId == "yugioh" })
        assertUniqueIds(presets)
    }

    @Test
    fun `getPresetsForGame returns mtg presets`() {
        val presets = getPresetsForGame("mtg")
        assertEquals(listOf("1d20", "1d6", "2d6"), presets.map { it.name })
        assertEquals(listOf(20, 6, 6, 6), presets.flatMap { it.dice }.map { it.faceCount })
        assertTrue(presets.flatMap { it.dice }.all { it.color == DiceColor.WHITE })
        assertTrue(presets.all { it.gameId == "mtg" })
        assertUniqueIds(presets)
    }

    @Test
    fun `getPresetsForGame returns pokemon presets`() {
        val presets = getPresetsForGame("pokemon")
        assertEquals(listOf("1d20", "1d6"), presets.map { it.name })
        assertTrue(presets.flatMap { it.dice }.all { it.color == DiceColor.WHITE })
        assertTrue(presets.all { it.gameId == "pokemon" })
        assertUniqueIds(presets)
    }

    @Test
    fun `getPresetsForGame returns generic presets for unknown game`() {
        val presets = getPresetsForGame("swu")
        assertEquals(listOf("1d6", "2d6", "1d20"), presets.map { it.name })
        assertTrue(presets.flatMap { it.dice }.all { it.color == DiceColor.WHITE })
        assertTrue(presets.all { it.gameId == "swu" })
        assertUniqueIds(presets)
    }

    private fun assertUniqueIds(presets: List<DicePreset>) {
        val presetIds = presets.map { it.id }
        assertEquals(presetIds.size, presetIds.toSet().size)
        val dieIds = presets.flatMap { preset -> preset.dice.map { it.id } }
        assertEquals(dieIds.size, dieIds.toSet().size)
    }
}
