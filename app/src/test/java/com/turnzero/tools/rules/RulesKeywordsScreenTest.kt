package com.turnzero.tools.rules

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36])
class RulesKeywordsScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val keywords = listOf(
        KeywordItem(
            name = "Aardvark",
            category = "Combat",
            text = listOf("Strikes first."),
            bullets = listOf("Cannot be blocked."),
        ),
        KeywordItem(name = "Zebra", category = "Movement", text = listOf("Moves last.")),
    )

    private val allCategories = listOf("Combat", "Movement")

    /**
     * Hosts [RulesKeywordsScreen] with the query/category filtering and expansion state the
     * ViewModel owns, so the screen's contract can be exercised through the UI.
     */
    @Composable
    private fun TestScreen() {
        var searchQuery by remember { mutableStateOf("") }
        var selectedCategory by remember { mutableStateOf<String?>(null) }
        var expandedIndex by remember { mutableStateOf<Int?>(null) }

        val filtered = keywords.filter { keyword ->
            keyword.name.contains(searchQuery, ignoreCase = true) &&
                (selectedCategory == null || keyword.category == selectedCategory)
        }

        RulesKeywordsScreen(
            keywords = filtered,
            searchQuery = searchQuery,
            selectedCategory = selectedCategory,
            allCategories = allCategories,
            expandedIndex = expandedIndex,
            onToggle = { index -> expandedIndex = if (expandedIndex == index) null else index },
            onSearchQueryChange = { searchQuery = it },
            onFilterCategory = { selectedCategory = it },
        )
    }

    @Test
    fun `search filters displayed keywords`() {
        composeRule.setContent { TestScreen() }

        composeRule.onNodeWithText("Aardvark").assertIsDisplayed()
        composeRule.onNodeWithText("Zebra").assertIsDisplayed()

        composeRule.onNodeWithText("Search keywords...").performTextInput("aa")

        composeRule.onNodeWithText("Aardvark").assertIsDisplayed()
        composeRule.onNodeWithText("Zebra").assertDoesNotExist()
    }

    @Test
    fun `category chip filters and clear chip resets`() {
        composeRule.setContent { TestScreen() }

        // The category row chip comes before the keyword cards, whose chips show the same label.
        composeRule.onAllNodesWithText("Movement").onFirst().performClick()

        composeRule.onNodeWithText("1 result").assertIsDisplayed()
        composeRule.onNodeWithText("Zebra").assertIsDisplayed()
        composeRule.onNodeWithText("Aardvark").assertDoesNotExist()

        composeRule.onNodeWithContentDescription("Clear filter").performClick()

        composeRule.onNodeWithText("Aardvark").assertIsDisplayed()
        composeRule.onNodeWithText("Zebra").assertIsDisplayed()
    }

    @Test
    fun `tap expands keyword card to show text`() {
        composeRule.setContent { TestScreen() }

        composeRule.onNodeWithText("Strikes first.").assertDoesNotExist()

        composeRule.onNodeWithText("Aardvark").performClick()

        composeRule.onNodeWithText("Strikes first.").assertIsDisplayed()
        composeRule.onNodeWithText("\u2022 Cannot be blocked.").assertIsDisplayed()
    }
}
