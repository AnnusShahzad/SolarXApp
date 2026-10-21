package io.github.annusshahzad.solarx.ui.monitor

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import io.github.annusshahzad.solarx.R
import io.github.annusshahzad.solarx.data.GridAlert

/** Parses the threshold typed by the user, or returns null if it is not a positive whole number. */
fun parseThreshold(text: String): Int? = text.trim().toIntOrNull()?.takeIf { it > 0 }

@Composable
fun SettingsScreen(
    gridAlert: GridAlert,
    onAlertEnabledChange: (Boolean) -> Unit,
    onThresholdChange: (Int) -> Unit,
    onChangeInverter: () -> Unit,
    contentPadding: PaddingValues,
) {
    var editingThreshold by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .verticalScroll(rememberScrollState()),
    ) {
        ScreenHeader(stringResource(R.string.tab_settings))
        ListItem(
            modifier = Modifier.toggleable(
                value = gridAlert.enabled,
                role = Role.Switch,
                onValueChange = onAlertEnabledChange,
            ),
            headlineContent = { Text(stringResource(R.string.settings_alert_title)) },
            supportingContent = { Text(stringResource(R.string.settings_alert_summary)) },
            trailingContent = { Switch(checked = gridAlert.enabled, onCheckedChange = null) },
        )
        if (gridAlert.enabled) {
            ListItem(
                modifier = Modifier.clickable { editingThreshold = true },
                headlineContent = { Text(stringResource(R.string.settings_threshold_title)) },
                trailingContent = {
                    Text(
                        text = stringResource(R.string.watts, gridAlert.thresholdW),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                },
            )
        }
        HorizontalDivider()
        ListItem(
            modifier = Modifier.clickable(onClick = onChangeInverter),
            headlineContent = { Text(stringResource(R.string.settings_change_inverter)) },
            supportingContent = { Text(stringResource(R.string.settings_change_inverter_summary)) },
        )
    }

    if (editingThreshold) {
        ThresholdDialog(
            initial = gridAlert.thresholdW,
            onDismiss = { editingThreshold = false },
            onSave = {
                onThresholdChange(it)
                editingThreshold = false
            },
        )
    }
}

@Composable
private fun ThresholdDialog(initial: Int, onDismiss: () -> Unit, onSave: (Int) -> Unit) {
    var text by rememberSaveable { mutableStateOf(initial.toString()) }
    val threshold = parseThreshold(text)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_threshold_title)) },
        text = {
            Column {
                Text(stringResource(R.string.settings_threshold_dialog_text))
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    singleLine = true,
                    suffix = { Text("W") },
                    isError = threshold == null,
                    supportingText = { if (threshold == null) Text(stringResource(R.string.settings_threshold_error)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { threshold?.let(onSave) }),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { threshold?.let(onSave) }, enabled = threshold != null) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        },
    )
}
