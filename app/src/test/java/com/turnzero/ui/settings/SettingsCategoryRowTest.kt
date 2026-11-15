package com.turnzero.ui.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SystemUpdate
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36])
class SettingsCategoryRowTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `enabled row reports the click`() {
        var clicked = false
        composeRule.setContent {
            MaterialTheme {
                SettingsCategoryRow(
                    icon = Icons.Outlined.SystemUpdate,
                    title = "Check for updates",
                    description = "Check for new versions",
                    onClick = { clicked = true },
                )
            }
        }

        composeRule.onNodeWithText("Check for updates").assertIsEnabled()
        composeRule.onNodeWithText("Check for updates").performClick()

        assertTrue(clicked)
    }

    @Test
    fun `disabled row is not clickable`() {
        composeRule.setContent {
            MaterialTheme {
                SettingsCategoryRow(
                    icon = Icons.Outlined.SystemUpdate,
                    title = "Check for updates",
                    description = "Downloading update… 42%",
                    onClick = {},
                    enabled = false,
                )
            }
        }

        composeRule.onNodeWithText("Check for updates").assertIsNotEnabled()
    }
}
