package com.turnzero.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.turnzero.core.ui.components.TZTopBar

data class LanguageOption(val code: String, val label: String)

val languages = listOf(
    LanguageOption("en", "English"),
    LanguageOption("fr", "Fran\u00E7ais")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSelectionScreen(
    currentLanguage: String,
    onLanguageSelected: (String) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TZTopBar(
                title = "Language",
                showBackButton = true,
                onBackClick = onBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(vertical = 8.dp)
        ) {
            languages.forEach { lang ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = currentLanguage == lang.code,
                            onClick = { onLanguageSelected(lang.code) },
                            role = Role.RadioButton
                        )
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = currentLanguage == lang.code,
                        onClick = null
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(text = lang.label, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}
