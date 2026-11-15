package com.turnzero.tools.rules

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties

/** Compact top-bar chip summarizing the confirmed [Selection]. */
@Composable
fun SpecificityPill(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}

/**
 * Every specificity of [manifest] in manifest order. The editor only reports taps; the
 * ViewModel owns the selection and applies the select/deselect dependency closure.
 */
@Composable
fun SpecificityEditor(
    manifest: RulesManifest,
    selection: Selection,
    onSelectOption: (String, String) -> Unit,
    onNumberChange: (String, Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        manifest.specificities.forEach { spec ->
            SpecificitySection(
                spec = spec,
                selection = selection,
                onSelectOption = onSelectOption,
                onNumberChange = onNumberChange,
            )
        }
    }
}

/** Centered, non-dismissible setup dialog shown on first open and when the pill is tapped. */
@Composable
fun SpecificityDialog(
    manifest: RulesManifest,
    selection: Selection,
    onSelectOption: (String, String) -> Unit,
    onNumberChange: (String, Int) -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = {},
        title = { Text("Choose your game settings") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                SpecificityEditor(
                    manifest = manifest,
                    selection = selection,
                    onSelectOption = onSelectOption,
                    onNumberChange = onNumberChange,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Start")
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
        ),
    )
}

@Composable
private fun SpecificitySection(
    spec: SpecificityDef,
    selection: Selection,
    onSelectOption: (String, String) -> Unit,
    onNumberChange: (String, Int) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = spec.name, style = MaterialTheme.typography.titleSmall)
        when (spec.type) {
            SpecificityType.SINGLE -> spec.options.forEach { option ->
                RadioOptionRow(
                    label = option.name,
                    selected = selection.single[spec.id] == option.id,
                    onClick = { onSelectOption(spec.id, option.id) },
                )
            }

            SpecificityType.NUMBER -> {
                val min = spec.min ?: 0
                val max = spec.max ?: min
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    (min..max).forEach { value ->
                        FilterChip(
                            selected = selection.number[spec.id] == value,
                            onClick = { onNumberChange(spec.id, value) },
                            label = { Text("${value}P") },
                        )
                    }
                }
            }

            SpecificityType.MULTI -> spec.options.forEach { option ->
                CheckboxOptionRow(
                    label = option.name,
                    checked = option.id in selection.multi[spec.id].orEmpty(),
                    onClick = { onSelectOption(spec.id, option.id) },
                )
            }
        }
    }
}

@Composable
private fun RadioOptionRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = null)
        Spacer(Modifier.width(8.dp))
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun CheckboxOptionRow(
    label: String,
    checked: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(value = checked, role = Role.Checkbox, onValueChange = { onClick() })
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(checked = checked, onCheckedChange = null)
        Spacer(Modifier.width(8.dp))
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
    }
}
