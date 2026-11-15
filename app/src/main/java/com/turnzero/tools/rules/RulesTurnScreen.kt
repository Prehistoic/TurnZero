package com.turnzero.tools.rules

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.turnzero.core.ui.components.TZEmptyState

/**
 * Turn structure drill-down: phase list, then the selected phase's step list, then one step's
 * content with previous/next navigation. Purely stateless — the drill-down position is passed in
 * and every navigation intent is reported up. The back affordance lives in the tool's top bar.
 */
@Composable
fun TurnStructureScreen(
    phases: List<Phase>,
    phaseIndex: Int?,
    stepIndex: Int?,
    onOpenPhase: (Int) -> Unit,
    onOpenStep: (Int) -> Unit,
    onNextStep: () -> Unit,
    onPreviousStep: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (phases.isEmpty()) {
        TZEmptyState(message = "No turn structure available", modifier = modifier)
        return
    }

    if (phaseIndex == null) {
        PhaseList(phases = phases, onOpenPhase = onOpenPhase, modifier = modifier)
        return
    }
    val phase = phases.getOrNull(phaseIndex)
    if (phase == null) {
        PhaseList(phases = phases, onOpenPhase = onOpenPhase, modifier = modifier)
        return
    }

    if (stepIndex == null) {
        StepList(phase = phase, onOpenStep = onOpenStep, modifier = modifier)
        return
    }
    val step = phase.steps.getOrNull(stepIndex)
    if (step == null) {
        StepList(phase = phase, onOpenStep = onOpenStep, modifier = modifier)
        return
    }

    StepDetail(
        phases = phases,
        phaseIndex = phaseIndex,
        stepIndex = stepIndex,
        step = step,
        onNextStep = onNextStep,
        onPreviousStep = onPreviousStep,
        modifier = modifier,
    )
}

@Composable
private fun PhaseList(
    phases: List<Phase>,
    onOpenPhase: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        itemsIndexed(phases) { index, phase ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenPhase(index) },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                ),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "${index + 1}.",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = phase.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = stepCountLabel(phase.steps.size),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StepList(
    phase: Phase,
    onOpenStep: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        itemsIndexed(phase.steps) { index, step ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenStep(index) },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                ),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "${index + 1}.",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = step.title.orEmpty(),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun StepDetail(
    phases: List<Phase>,
    phaseIndex: Int,
    stepIndex: Int,
    step: RuleItem,
    onNextStep: () -> Unit,
    onPreviousStep: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val phase = phases[phaseIndex]
    val isFirstStep = phaseIndex == 0 && stepIndex == 0
    val isLastStepOfLastPhase =
        phaseIndex == phases.lastIndex && stepIndex == phase.steps.lastIndex

    Column(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Step ${stepIndex + 1} of ${phase.steps.size} · ${phase.name}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            RulesItemContent(step)
        }
        HorizontalDivider()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedButton(
                onClick = onPreviousStep,
                enabled = !isFirstStep,
                modifier = Modifier.weight(1f),
            ) {
                Text("← Prev")
            }
            Button(
                onClick = onNextStep,
                enabled = !isLastStepOfLastPhase,
                modifier = Modifier.weight(1f),
            ) {
                Text("Next →")
            }
        }
    }
}

private fun stepCountLabel(count: Int): String = if (count == 1) "1 step" else "$count steps"
