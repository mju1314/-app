package com.example.expensetracker.ui.records

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.expensetracker.R
import com.example.expensetracker.ui.add.SelectOptionUiModel
import com.example.expensetracker.ui.components.EditableDateTimeField
import com.example.expensetracker.ui.components.SectionCard

@Composable
fun RecordDetailRoute(
    contentPadding: PaddingValues,
    onNavigateBack: () -> Unit,
    viewModel: RecordDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    RecordDetailScreen(
        contentPadding = contentPadding,
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onAmountChanged = viewModel::updateAmount,
        onNoteChanged = viewModel::updateNote,
        onSpentAtChanged = viewModel::updateSpentAt,
        onCategorySelected = viewModel::selectCategory,
        onPaymentMethodSelected = viewModel::selectPaymentMethod,
        onSaveClick = { viewModel.saveChanges(onNavigateBack) },
        onDeleteClick = { viewModel.deleteRecord(onNavigateBack) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecordDetailScreen(
    contentPadding: PaddingValues,
    uiState: RecordDetailUiState,
    onNavigateBack: () -> Unit,
    onAmountChanged: (String) -> Unit,
    onNoteChanged: (String) -> Unit,
    onSpentAtChanged: (Long) -> Unit,
    onCategorySelected: (Long) -> Unit,
    onPaymentMethodSelected: (Long) -> Unit,
    onSaveClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.title_record_detail)) },
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) {
                        Text(text = stringResource(id = R.string.action_back))
                    }
                },
            )
        },
    ) { innerPadding ->
        if (uiState.isLoading) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(contentPadding),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(contentPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SectionCard(title = stringResource(id = R.string.label_amount)) {
                OutlinedTextField(
                    value = uiState.amount,
                    onValueChange = onAmountChanged,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }

            SectionCard(title = stringResource(id = R.string.label_category)) {
                OptionChips(
                    options = uiState.categoryOptions,
                    selectedId = uiState.selectedCategoryId,
                    onSelected = onCategorySelected,
                )
            }

            SectionCard(title = stringResource(id = R.string.label_payment_method)) {
                OptionChips(
                    options = uiState.paymentMethodOptions,
                    selectedId = uiState.selectedPaymentMethodId,
                    onSelected = onPaymentMethodSelected,
                )
            }

            SectionCard(title = stringResource(id = R.string.label_spent_at)) {
                EditableDateTimeField(
                    timestamp = uiState.spentAtMillis,
                    valueText = uiState.spentAtText,
                    onTimestampSelected = onSpentAtChanged,
                )
            }

            SectionCard(title = stringResource(id = R.string.label_note)) {
                OutlinedTextField(
                    value = uiState.note,
                    onValueChange = onNoteChanged,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            uiState.errorMessageResId?.let { messageResId ->
                Text(
                    text = stringResource(id = messageResId),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onSaveClick,
                    modifier = Modifier.weight(1f),
                    enabled = !uiState.isSaving && !uiState.isDeleting,
                ) {
                    Text(text = stringResource(id = R.string.action_update))
                }
                Button(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.weight(1f),
                    enabled = !uiState.isSaving && !uiState.isDeleting,
                ) {
                    Text(text = stringResource(id = R.string.action_delete))
                }
            }
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text(text = stringResource(id = R.string.title_delete_confirm)) },
                text = { Text(text = stringResource(id = R.string.message_delete_confirm)) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDeleteDialog = false
                            onDeleteClick()
                        },
                    ) {
                        Text(text = stringResource(id = R.string.action_delete))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text(text = stringResource(id = R.string.action_cancel))
                    }
                },
            )
        }
    }
}

@Composable
private fun OptionChips(
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
                    FilterChip(
                        selected = option.id == selectedId,
                        onClick = { onSelected(option.id) },
                        label = { Text(text = option.label) },
                    )
                }
            }
        }
    }
}
