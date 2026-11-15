package com.turnzero.tools.rules

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.turnzero.core.ui.components.TZEmptyState

/**
 * Rulebook sub-tool: resolves the rulebook matching the current selection — asking the user to
 * pick one when several match — and shows it in a page-aware [RulebookPdfViewer].
 *
 * [rulebooks] is already filtered to the current selection by the view model.
 */
@Composable
fun RulebookScreen(
    rulebooks: List<RulebookDef>,
    selectedRulebookId: String?,
    initialPage: Int,
    showSelector: Boolean,
    onSelectRulebook: (String) -> Unit,
    onDismissSelector: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // A single matching rulebook needs no choice, so it is used when nothing is selected yet.
    val resolved = rulebooks.firstOrNull { it.id == selectedRulebookId } ?: rulebooks.singleOrNull()

    // Also re-ask when the previously chosen rulebook no longer matches the selection (stale id).
    // Cancelling the dialog goes through the view model, which closes the sub-tool, so it cannot
    // loop back into this condition.
    if (showSelector || (resolved == null && rulebooks.isNotEmpty())) {
        AlertDialog(
            onDismissRequest = onDismissSelector,
            title = { Text("Choose Rules PDF") },
            text = {
                Column {
                    rulebooks.forEach { rulebook ->
                        TextButton(
                            onClick = { onSelectRulebook(rulebook.id) },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(rulebook.name, modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = onDismissSelector) { Text("Cancel") }
            },
        )
        return
    }

    if (resolved == null) {
        TZEmptyState(message = "No rulebook available", modifier = modifier)
        return
    }

    RulebookPdfViewer(
        assetPath = resolved.asset,
        initialPage = initialPage,
        modifier = modifier,
    )
}
