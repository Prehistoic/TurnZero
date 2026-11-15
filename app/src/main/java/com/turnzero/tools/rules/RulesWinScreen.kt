package com.turnzero.tools.rules

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.turnzero.core.ui.components.TZEmptyState

/** Scrollable list of win conditions, one bordered card per condition. */
@Composable
fun WinConditionScreen(
    conditions: List<RuleItem>,
    modifier: Modifier = Modifier,
) {
    if (conditions.isEmpty()) {
        TZEmptyState(message = "No win conditions available", modifier = modifier)
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        for (condition in conditions) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(
                    width = 3.dp,
                    color = MaterialTheme.colorScheme.primary,
                ),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                ),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    RulesItemContent(condition)
                }
            }
        }
    }
}
