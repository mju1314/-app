package com.example.expensetracker.ui.add

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.expensetracker.R
import com.example.expensetracker.data.entity.TransactionEntity
import com.example.expensetracker.ui.common.CategoryIcons
import com.example.expensetracker.ui.components.EditableDateTimeField
import com.example.expensetracker.ui.components.SectionCard

@Composable
fun AddExpenseRoute(
    contentPadding: PaddingValues,
    onNavigateBack: () -> Unit,
    viewModel: AddExpenseViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val warningMessage = stringResource(id = R.string.warning_balance_negative)
    val budgetExceededTotal = stringResource(id = R.string.budget_exceeded_total)
    val budgetExceededCategory = stringResource(id = R.string.budget_exceeded_category)
    AddExpenseScreen(
        contentPadding = contentPadding,
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onTypeChanged = viewModel::updateTransactionType,
        onAmountChanged = viewModel::updateAmount,
        onNoteChanged = viewModel::updateNote,
        onSpentAtChanged = viewModel::updateSpentAt,
        onCategorySelected = viewModel::selectCategory,
        onAccountSelected = viewModel::selectAccount,
        onSaveClick = {
            viewModel.saveExpense(
                onSuccess = onNavigateBack,
                onBalanceWarning = {
                    Toast.makeText(context, warningMessage, Toast.LENGTH_LONG).show()
                },
                onBudgetExceeded = { categoryName ->
                    val message = if (categoryName.isEmpty()) {
                        budgetExceededTotal
                    } else {
                        budgetExceededCategory.replace("%1\$s", categoryName)
                    }
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                },
            )
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddExpenseScreen(
    contentPadding: PaddingValues,
    uiState: AddExpenseUiState,
    onNavigateBack: () -> Unit,
    onTypeChanged: (Int) -> Unit,
    onAmountChanged: (String) -> Unit,
    onNoteChanged: (String) -> Unit,
    onSpentAtChanged: (Long) -> Unit,
    onCategorySelected: (Long) -> Unit,
    onAccountSelected: (Long?) -> Unit,
    onSaveClick: () -> Unit,
) {
    val isIncome = uiState.transactionType == TransactionEntity.TYPE_INCOME
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(
                            id = if (isIncome) R.string.title_add_income else R.string.title_add_expense,
                        ),
                    )
                },
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) {
                        Text(text = stringResource(id = R.string.action_back))
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(contentPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = !isIncome,
                    onClick = { onTypeChanged(TransactionEntity.TYPE_EXPENSE) },
                    label = { Text(text = stringResource(id = R.string.transaction_type_expense)) },
                )
                FilterChip(
                    selected = isIncome,
                    onClick = { onTypeChanged(TransactionEntity.TYPE_INCOME) },
                    label = { Text(text = stringResource(id = R.string.transaction_type_income)) },
                )
            }

            SectionCard(title = stringResource(id = R.string.label_amount)) {
                OutlinedTextField(
                    value = uiState.amount,
                    onValueChange = onAmountChanged,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(text = stringResource(id = R.string.hint_amount))
                    },
                    singleLine = true,
                )
            }

            SectionCard(title = stringResource(id = R.string.label_category)) {
                if (uiState.categoryOptions.isEmpty()) {
                    Text(text = stringResource(id = R.string.empty_categories))
                } else {
                    FlowChipRow(
                        options = uiState.categoryOptions,
                        selectedId = uiState.selectedCategoryId,
                        onSelected = onCategorySelected,
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SectionCard(
                    title = stringResource(id = R.string.label_spent_at),
                    modifier = Modifier.weight(1f),
                ) {
                    EditableDateTimeField(
                        timestamp = uiState.spentAtMillis,
                        valueText = uiState.spentAtText,
                        onTimestampSelected = onSpentAtChanged,
                    )
                }
                if (uiState.accountOptions.isNotEmpty()) {
                    SectionCard(
                        title = stringResource(id = R.string.label_account),
                        modifier = Modifier.weight(1f),
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = uiState.selectedAccountId == null,
                                onClick = { onAccountSelected(null) },
                                label = { Text(text = stringResource(id = R.string.label_no_account)) },
                            )
                            uiState.accountOptions.forEach { option ->
                                FilterChip(
                                    selected = option.id == uiState.selectedAccountId,
                                    onClick = { onAccountSelected(option.id) },
                                    label = { Text(text = option.label) },
                                )
                            }
                        }
                    }
                }
            }

            SectionCard(title = stringResource(id = R.string.label_note)) {
                OutlinedTextField(
                    value = uiState.note,
                    onValueChange = onNoteChanged,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(text = stringResource(id = R.string.hint_note_optional))
                    },
                )
            }

            uiState.errorMessageResId?.let { messageResId ->
                Text(
                    text = stringResource(id = messageResId),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            Button(
                onClick = onSaveClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isSaving,
            ) {
                Text(
                    text = if (uiState.isSaving) {
                        stringResource(id = R.string.action_saving)
                    } else {
                        stringResource(id = R.string.action_save_expense)
                    },
                )
            }
        }
    }
}

@Composable
private fun FlowChipRow(
    options: List<SelectOptionUiModel>,
    selectedId: Long?,
    onSelected: (Long) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        options.chunked(3).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                rowItems.forEach { option ->
                    val emoji = CategoryIcons.getEmoji(option.icon)
                    FilterChip(
                        selected = option.id == selectedId,
                        onClick = { onSelected(option.id) },
                        label = { Text(text = "$emoji ${option.label}") },
                    )
                }
            }
        }
    }
}
