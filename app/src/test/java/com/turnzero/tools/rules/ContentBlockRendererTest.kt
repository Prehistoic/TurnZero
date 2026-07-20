package com.turnzero.tools.rules

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import com.turnzero.core.ui.components.parseBoldText
import org.junit.Assert.assertEquals
import org.junit.Test

class ContentBlockRendererTest {

    @Test
    fun `parseBoldText returns plain text when no markers`() {
        val result = parseBoldText("Hello world")
        val expected = buildAnnotatedString { append("Hello world") }
        assertEquals(expected, result)
    }

    @Test
    fun `parseBoldText renders single bold span`() {
        val result = parseBoldText("Hello **world** end")
        val expected = buildAnnotatedString {
            append("Hello ")
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("world") }
            append(" end")
        }
        assertEquals(expected, result)
    }

    @Test
    fun `parseBoldText renders multiple bold spans`() {
        val result = parseBoldText("**first** middle **second**")
        val expected = buildAnnotatedString {
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("first") }
            append(" middle ")
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("second") }
        }
        assertEquals(expected, result)
    }

    @Test
    fun `parseBoldText handles adjacent bold spans`() {
        val result = parseBoldText("**a****b**")
        val expected = buildAnnotatedString {
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("a") }
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("b") }
        }
        assertEquals(expected, result)
    }

    @Test
    fun `parseBoldText returns bare text when markers have no closing`() {
        val result = parseBoldText("Unclosed **bold")
        val expected = buildAnnotatedString { append("Unclosed **bold") }
        assertEquals(expected, result)
    }

    @Test
    fun `parseBoldText handles empty bold markers as no visible text`() {
        val result = parseBoldText("text **** more")
        assertEquals("text  more", result.text)
    }
}
