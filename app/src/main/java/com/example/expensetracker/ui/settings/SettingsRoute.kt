package com.example.expensetracker.ui.settings

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.expensetracker.BuildConfig
import com.example.expensetracker.R
import com.example.expensetracker.common.AppRestarter
import com.example.expensetracker.ui.components.SectionCard
import java.math.BigDecimal
import java.math.RoundingMode

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
        onAddAccount = viewModel::addAccount,
        onUpdateAccount = viewModel::updateAccount,
        onDeleteAccount = viewModel::deleteAccount,
        onSaveBudget = viewModel::saveBudget,
        onDeleteBudget = viewModel::deleteBudget,
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
    onAddAccount: (String, Long) -> Unit,
    onUpdateAccount: (Long, String, Long) -> Unit,
    onDeleteAccount: (Long) -> Unit,
    onSaveBudget: (Long?, Long) -> Unit,
    onDeleteBudget: (Long) -> Unit,
    onExportCsvClick: () -> Unit,
    onBackupClick: () -> Unit,
    onRestoreClick: () -> Unit,
    onClearDataConfirmed: () -> Unit,
) {
    var showClearDialog by remember { mutableStateOf(false) }
    var showRestoreDialog by remember { mutableStateOf(false) }
    var showAccountDialog by remember { mutableStateOf(false) }
    var editingAccount by remember { mutableStateOf<AccountUiModel?>(null) }
    var deletingAccountId by remember { mutableStateOf<Long?>(null) }
    var showBudgetDialog by remember { mutableStateOf(false) }
    var editingBudget by remember { mutableStateOf<BudgetUiModel?>(null) }
    var deletingBudgetId by remember { mutableStateOf<Long?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SectionCard(title = stringResource(id = R.string.settings_account_title)) {
            Text(
                text = stringResource(id = R.string.settings_account_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (uiState.accounts.isNotEmpty()) {
                var totalVisible by remember { mutableStateOf(false) }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .clickable { totalVisible = !totalVisible },
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                    shape = MaterialTheme.shapes.large,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(id = R.string.settings_account_total),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                text = if (totalVisible) uiState.totalBalanceText else "****",
                                modifier = Modifier.animateContentSize(),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Icon(
                                imageVector = if (totalVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
            if (uiState.accounts.isEmpty()) {
                Text(
                    text = stringResource(id = R.string.settings_account_empty),
                    modifier = Modifier.padding(top = 12.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                var expanded by remember { mutableStateOf(false) }
                val visibleAccounts = if (expanded) uiState.accounts else uiState.accounts.take(2)

                Column(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    visibleAccounts.forEach { account ->
                        AccountItem(
                            account = account,
                            onClick = {
                                editingAccount = account
                                showAccountDialog = true
                            },
                            onDeleteClick = { deletingAccountId = account.id },
                        )
                    }
                    if (uiState.accounts.size > 2) {
                        TextButton(
                            onClick = { expanded = !expanded },
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                        ) {
                            Text(
                                text = if (expanded) {
                                    stringResource(id = R.string.settings_account_collapse)
                                } else {
                                    stringResource(id = R.string.settings_account_expand, uiState.accounts.size)
                                },
                            )
                        }
                    }
                }
            }
            Button(
                onClick = {
                    editingAccount = null
                    showAccountDialog = true
                },
                modifier = Modifier.padding(top = 12.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .size(18.dp),
                )
                Text(text = stringResource(id = R.string.settings_account_add))
            }
        }

        SectionCard(title = stringResource(id = R.string.settings_budget_title)) {
            Text(
                text = stringResource(id = R.string.settings_budget_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (uiState.budgets.isEmpty()) {
                Text(
                    text = stringResource(id = R.string.settings_budget_empty),
                    modifier = Modifier.padding(top = 12.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Column(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    uiState.budgets.forEach { budget ->
                        BudgetItem(
                            budget = budget,
                            onClick = {
                                editingBudget = budget
                                showBudgetDialog = true
                            },
                            onDeleteClick = { deletingBudgetId = budget.id },
                        )
                    }
                }
            }
            Button(
                onClick = {
                    editingBudget = null
                    showBudgetDialog = true
                },
                modifier = Modifier.padding(top = 12.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .size(18.dp),
                )
                Text(text = stringResource(id = R.string.settings_budget_add))
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

    if (showAccountDialog) {
        AccountDialog(
            editing = editingAccount,
            onDismiss = { showAccountDialog = false },
            onSave = { name, balanceInCent ->
                val existing = editingAccount
                if (existing != null) {
                    onUpdateAccount(existing.id, name, balanceInCent)
                } else {
                    onAddAccount(name, balanceInCent)
                }
                showAccountDialog = false
            },
        )
    }

    deletingAccountId?.let { accountId ->
        AlertDialog(
            onDismissRequest = { deletingAccountId = null },
            title = { Text(text = stringResource(id = R.string.settings_account_delete_confirm_title)) },
            text = { Text(text = stringResource(id = R.string.settings_account_delete_confirm_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteAccount(accountId)
                        deletingAccountId = null
                    },
                ) {
                    Text(text = stringResource(id = R.string.settings_account_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingAccountId = null }) {
                    Text(text = stringResource(id = R.string.action_cancel))
                }
            },
        )
    }

    if (showBudgetDialog) {
        BudgetDialog(
            editing = editingBudget,
            existingBudgets = uiState.budgets,
            categoryOptions = uiState.categoryOptions,
            onDismiss = { showBudgetDialog = false },
            onSave = { categoryId, amountInCent ->
                onSaveBudget(categoryId, amountInCent)
                showBudgetDialog = false
            },
        )
    }

    deletingBudgetId?.let { budgetId ->
        AlertDialog(
            onDismissRequest = { deletingBudgetId = null },
            title = { Text(text = stringResource(id = R.string.settings_budget_delete_confirm_title)) },
            text = { Text(text = stringResource(id = R.string.settings_budget_delete_confirm_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteBudget(budgetId)
                        deletingBudgetId = null
                    },
                ) {
                    Text(text = stringResource(id = R.string.settings_budget_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingBudgetId = null }) {
                    Text(text = stringResource(id = R.string.action_cancel))
                }
            },
        )
    }
}

@Composable
private fun AccountItem(
    account: AccountUiModel,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    var balanceVisible by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        shape = MaterialTheme.shapes.large,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = account.name,
                    style = MaterialTheme.typography.bodyLarge,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = stringResource(
                            id = R.string.settings_account_balance,
                            if (balanceVisible) account.balanceText else "****",
                        ),
                        modifier = Modifier.animateContentSize(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Icon(
                        imageVector = if (balanceVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = null,
                        modifier = Modifier
                            .size(16.dp)
                            .clickable { balanceVisible = !balanceVisible },
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = stringResource(id = R.string.settings_account_delete),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun AccountDialog(
    editing: AccountUiModel?,
    onDismiss: () -> Unit,
    onSave: (String, Long) -> Unit,
) {
    var name by remember(editing) { mutableStateOf(editing?.name.orEmpty()) }
    var balanceText by remember(editing) {
        mutableStateOf(
            if (editing != null) {
                BigDecimal(editing.balanceInCent)
                    .divide(BigDecimal(100))
                    .setScale(2, RoundingMode.HALF_UP)
                    .toPlainString()
            } else {
                ""
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(
                    id = if (editing != null) {
                        R.string.settings_account_dialog_edit_title
                    } else {
                        R.string.settings_account_dialog_add_title
                    },
                ),
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(text = stringResource(id = R.string.settings_account_name_hint)) },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = balanceText,
                    onValueChange = { value ->
                        balanceText = value.filter { it.isDigit() || it == '.' || it == '-' }.let { text ->
                            val firstDot = text.indexOf('.')
                            if (firstDot < 0) {
                                text
                            } else {
                                val integerPart = text.substring(0, firstDot + 1)
                                val decimalPart = text.substring(firstDot + 1).replace(".", "").take(2)
                                integerPart + decimalPart
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(text = stringResource(id = R.string.settings_account_balance_hint)) },
                    singleLine = true,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val trimmedName = name.trim()
                    if (trimmedName.isBlank()) return@TextButton
                    val balanceInCent = runCatching {
                        BigDecimal(balanceText.ifBlank { "0" })
                            .multiply(BigDecimal(100))
                            .setScale(0, RoundingMode.HALF_UP)
                            .longValueExact()
                    }.getOrDefault(0L)
                    onSave(trimmedName, balanceInCent)
                },
            ) {
                Text(text = stringResource(id = R.string.settings_account_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(id = R.string.action_cancel))
            }
        },
    )
}

@Composable
private fun BudgetItem(
    budget: BudgetUiModel,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        shape = MaterialTheme.shapes.large,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = budget.categoryName
                        ?: stringResource(id = R.string.settings_budget_total_label),
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    text = budget.amountText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = stringResource(id = R.string.settings_budget_delete),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun BudgetDialog(
    editing: BudgetUiModel?,
    existingBudgets: List<BudgetUiModel>,
    categoryOptions: List<CategoryOptionUiModel>,
    onDismiss: () -> Unit,
    onSave: (Long?, Long) -> Unit,
) {
    var selectedCategoryId by remember(editing) { mutableStateOf(editing?.categoryId) }
    var amountText by remember(editing) {
        mutableStateOf(
            if (editing != null) {
                BigDecimal(editing.amountInCent)
                    .divide(BigDecimal(100))
                    .setScale(2, RoundingMode.HALF_UP)
                    .toPlainString()
            } else {
                ""
            },
        )
    }

    val isEditing = editing != null
    val usedCategoryIds = existingBudgets
        .filter { it.id != (editing?.id ?: -1) }
        .map { it.categoryId }
    val hasTotalBudget = null in usedCategoryIds

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(
                    id = if (isEditing) {
                        R.string.settings_budget_dialog_edit_title
                    } else {
                        R.string.settings_budget_dialog_add_title
                    },
                ),
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (!isEditing) {
                    Text(
                        text = stringResource(id = R.string.settings_budget_type_label),
                        style = MaterialTheme.typography.labelLarge,
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (!hasTotalBudget) {
                            FilterChip(
                                selected = selectedCategoryId == null,
                                onClick = { selectedCategoryId = null },
                                label = { Text(text = stringResource(id = R.string.settings_budget_type_total)) },
                            )
                        }
                        categoryOptions
                            .filter { it.id !in usedCategoryIds }
                            .forEach { category ->
                                FilterChip(
                                    selected = selectedCategoryId == category.id,
                                    onClick = { selectedCategoryId = category.id },
                                    label = { Text(text = category.name) },
                                )
                            }
                    }
                }
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { value ->
                        amountText = value.filter { it.isDigit() || it == '.' }.let { text ->
                            val firstDot = text.indexOf('.')
                            if (firstDot < 0) {
                                text
                            } else {
                                val integerPart = text.substring(0, firstDot + 1)
                                val decimalPart = text.substring(firstDot + 1).replace(".", "").take(2)
                                integerPart + decimalPart
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(text = stringResource(id = R.string.settings_budget_amount_hint)) },
                    singleLine = true,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amountInCent = runCatching {
                        BigDecimal(amountText.ifBlank { "0" })
                            .multiply(BigDecimal(100))
                            .setScale(0, RoundingMode.HALF_UP)
                            .longValueExact()
                    }.getOrDefault(0L)
                    if (amountInCent > 0) {
                        onSave(selectedCategoryId, amountInCent)
                    }
                },
            ) {
                Text(text = stringResource(id = R.string.settings_budget_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(id = R.string.action_cancel))
            }
        },
    )
}
