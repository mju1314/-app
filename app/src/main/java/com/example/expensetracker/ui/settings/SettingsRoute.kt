package com.example.expensetracker.ui.settings

import android.app.Activity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.expensetracker.BuildConfig
import com.example.expensetracker.R
import com.example.expensetracker.common.AppRestarter
import com.example.expensetracker.common.CurrencyFormatter
import com.example.expensetracker.ui.components.SectionCard

@Composable
fun SettingsRoute(
    contentPadding: PaddingValues,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv"),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        viewModel.exportCsv(
            openOutputStream = { context.contentResolver.openOutputStream(uri) },
            onCompleted = {},
        )
    }
    val backupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip"),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        viewModel.backupData(
            openOutputStream = { context.contentResolver.openOutputStream(uri) },
            onCompleted = {},
        )
    }
    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        viewModel.restoreData(
            openInputStream = { context.contentResolver.openInputStream(uri) },
            onCompleted = { success ->
                if (success && activity != null) {
                    AppRestarter.restart(activity)
                }
            },
        )
    }

    SettingsScreen(
        contentPadding = contentPadding,
        uiState = uiState,
        onCurrencySelected = viewModel::selectCurrency,
        onExportCsvClick = {
            exportLauncher.launch(context.getString(R.string.settings_export_csv_filename))
        },
        onBackupClick = {
            backupLauncher.launch(context.getString(R.string.settings_backup_filename))
        },
        onRestoreClick = {
            restoreLauncher.launch(arrayOf("application/zip"))
        },
        onClearDataConfirmed = viewModel::clearAllData,
    )
}

@Composable
private fun SettingsScreen(
    contentPadding: PaddingValues,
    uiState: SettingsUiState,
    onCurrencySelected: (String) -> Unit,
    onExportCsvClick: () -> Unit,
    onBackupClick: () -> Unit,
    onRestoreClick: () -> Unit,
    onClearDataConfirmed: () -> Unit,
) {
    var showClearDialog by remember { mutableStateOf(false) }
    var showRestoreDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SectionCard(title = stringResource(id = R.string.settings_currency_title)) {
            Text(
                text = stringResource(id = R.string.settings_currency_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = CurrencyFormatter.formatCent(
                    amountInCent = 12345,
                    currencyCode = uiState.selectedCurrencyCode,
                ),
                modifier = Modifier.padding(top = 12.dp),
                style = MaterialTheme.typography.headlineSmall,
            )
            Column(
                modifier = Modifier.padding(top = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                currencyOptions.forEach { option ->
                    CurrencyOptionRow(
                        option = option,
                        selected = option.code == uiState.selectedCurrencyCode,
                        onClick = { onCurrencySelected(option.code) },
                    )
                }
            }
        }

        SectionCard(title = stringResource(id = R.string.settings_data_title)) {
            Text(
                text = stringResource(id = R.string.settings_export_csv_title),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = stringResource(id = R.string.settings_export_csv_description),
                modifier = Modifier.padding(top = 4.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(
                onClick = onExportCsvClick,
                modifier = Modifier.padding(top = 12.dp),
                enabled = !uiState.isExportingCsv && !uiState.isClearingData,
            ) {
                if (uiState.isExportingCsv) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(16.dp),
                        strokeWidth = 2.dp,
                    )
                }
                Text(text = stringResource(id = R.string.settings_export_csv_action))
            }
            uiState.exportMessageResId?.let { messageResId ->
                Text(
                    text = stringResource(id = messageResId),
                    modifier = Modifier.padding(top = 12.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Text(
                text = stringResource(id = R.string.settings_backup_title),
                modifier = Modifier.padding(top = 20.dp),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = stringResource(id = R.string.settings_backup_description),
                modifier = Modifier.padding(top = 4.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(
                onClick = onBackupClick,
                modifier = Modifier.padding(top = 12.dp),
                enabled = !uiState.isBackingUp && !uiState.isClearingData && !uiState.isExportingCsv,
            ) {
                if (uiState.isBackingUp) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(16.dp),
                        strokeWidth = 2.dp,
                    )
                }
                Text(text = stringResource(id = R.string.settings_backup_action))
            }
            uiState.backupMessageResId?.let { messageResId ->
                Text(
                    text = stringResource(id = messageResId),
                    modifier = Modifier.padding(top = 12.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Text(
                text = stringResource(id = R.string.settings_restore_title),
                modifier = Modifier.padding(top = 20.dp),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = stringResource(id = R.string.settings_restore_description),
                modifier = Modifier.padding(top = 4.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(
                onClick = { showRestoreDialog = true },
                modifier = Modifier.padding(top = 12.dp),
                enabled = !uiState.isRestoring && !uiState.isClearingData && !uiState.isExportingCsv && !uiState.isBackingUp,
            ) {
                if (uiState.isRestoring) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(16.dp),
                        strokeWidth = 2.dp,
                    )
                }
                Text(text = stringResource(id = R.string.settings_restore_action))
            }
            uiState.restoreMessageResId?.let { messageResId ->
                Text(
                    text = stringResource(id = messageResId),
                    modifier = Modifier.padding(top = 12.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Text(
                text = stringResource(id = R.string.settings_clear_data_description),
                modifier = Modifier.padding(top = 20.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(
                onClick = { showClearDialog = true },
                modifier = Modifier.padding(top = 12.dp),
                enabled = !uiState.isClearingData,
            ) {
                if (uiState.isClearingData) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(16.dp),
                        strokeWidth = 2.dp,
                    )
                }
                Text(text = stringResource(id = R.string.settings_clear_data_action))
            }
            uiState.infoMessageResId?.let { messageResId ->
                Text(
                    text = stringResource(id = messageResId),
                    modifier = Modifier.padding(top = 12.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }

        SectionCard(title = stringResource(id = R.string.settings_about_title)) {
            Text(
                text = stringResource(id = R.string.app_name),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = stringResource(id = R.string.settings_about_version, BuildConfig.VERSION_NAME),
                modifier = Modifier.padding(top = 4.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(id = R.string.settings_about_description),
                modifier = Modifier.padding(top = 12.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text(text = stringResource(id = R.string.settings_clear_data_confirm_title)) },
            text = { Text(text = stringResource(id = R.string.settings_clear_data_confirm_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearDialog = false
                        onClearDataConfirmed()
                    },
                ) {
                    Text(text = stringResource(id = R.string.settings_clear_data_action))
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text(text = stringResource(id = R.string.action_cancel))
                }
            },
        )
    }

    if (showRestoreDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreDialog = false },
            title = { Text(text = stringResource(id = R.string.settings_restore_confirm_title)) },
            text = { Text(text = stringResource(id = R.string.settings_restore_confirm_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRestoreDialog = false
                        onRestoreClick()
                    },
                ) {
                    Text(text = stringResource(id = R.string.settings_restore_action))
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreDialog = false }) {
                    Text(text = stringResource(id = R.string.action_cancel))
                }
            },
        )
    }
}

@Composable
private fun CurrencyOptionRow(
    option: CurrencyOptionUiModel,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
        )
        Text(
            text = stringResource(id = option.labelResId),
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}
