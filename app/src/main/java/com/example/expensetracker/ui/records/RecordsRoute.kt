package com.example.expensetracker.ui.records

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.expensetracker.R
import com.example.expensetracker.data.entity.TransactionEntity
import com.example.expensetracker.ui.common.CategoryIcons
import com.example.expensetracker.ui.components.SectionCard
import kotlinx.coroutines.launch

@Composable
fun RecordsRoute(
    contentPadding: PaddingValues,
    onRecordClick: (Long) -> Unit,
    onDeleteRequest: (TransactionEntity) -> Unit = {},
    viewModel: RecordsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()
    RecordsScreen(
        contentPadding = contentPadding,
        uiState = uiState,
        onRecordClick = onRecordClick,
        onKeywordChanged = viewModel::updateKeyword,
        onCategorySelected = viewModel::selectCategory,
        onAccountSelected = viewModel::selectAccount,
        onMinAmountChanged = viewModel::updateMinAmount,
        onMaxAmountChanged = viewModel::updateMaxAmount,
        onRangeSelected = viewModel::selectRange,
        onClearFilters = viewModel::clearFilters,
        onSwipeDelete = { itemId ->
            coroutineScope.launch {
                val entity = viewModel.getTransactionById(itemId)
                if (entity != null) {
                    onDeleteRequest(entity)
                }
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecordsScreen(
    contentPadding: PaddingValues,
    uiState: RecordsUiState,
    onRecordClick: (Long) -> Unit,
    onKeywordChanged: (String) -> Unit,
    onCategorySelected: (Long?) -> Unit,
    onAccountSelected: (Long?) -> Unit,
    onMinAmountChanged: (String) -> Unit,
    onMaxAmountChanged: (String) -> Unit,
    onRangeSelected: (RecordsDateRange) -> Unit,
    onClearFilters: () -> Unit,
    onSwipeDelete: (Long) -> Unit,
) {
    if (uiState.isLoading) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CircularProgressIndicator()
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            RecordsFilterCard(
                uiState = uiState,
                onKeywordChanged = onKeywordChanged,
                onCategorySelected = onCategorySelected,
                onAccountSelected = onAccountSelected,
                onMinAmountChanged = onMinAmountChanged,
                onMaxAmountChanged = onMaxAmountChanged,
                onRangeSelected = onRangeSelected,
                onClearFilters = onClearFilters,
            )
        }

        if (uiState.groups.isEmpty()) {
            item {
                EmptyRecordsState(
                    text = stringResource(id = R.string.records_filter_empty),
                )
            }
            return@LazyColumn
        }

        items(uiState.groups) { group ->
            SectionCard(
                title = stringResource(
                    id = group.titleResId,
                    group.dateLabel,
                    stringResource(id = R.string.label_daily_total),
                    group.totalAmountText,
                ),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    group.items.forEach { item ->
                        SwipeToDeleteRecordItem(
                            item = item,
                            onRecordClick = onRecordClick,
                            onSwipeDelete = onSwipeDelete,
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDeleteRecordItem(
    item: RecordListItemUiModel,
    onRecordClick: (Long) -> Unit,
    onSwipeDelete: (Long) -> Unit,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onSwipeDelete(item.id)
                true
            } else {
                false
            }
        },
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = MaterialTheme.colorScheme.error,
                        shape = RoundedCornerShape(8.dp),
                    )
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = null,
                    tint = Color.White,
                )
            }
        },
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onRecordClick(item.id) },
            color = MaterialTheme.colorScheme.surface,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        text = item.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    text = item.amountText,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (item.type == TransactionEntity.TYPE_INCOME) {
                        MaterialTheme.colorScheme.tertiary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                )
            }
        }
    }
}

@Composable
private fun EmptyRecordsState(text: String) {
    SectionCard(title = stringResource(id = R.string.nav_records)) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun RecordsFilterCard(
    uiState: RecordsUiState,
    onKeywordChanged: (String) -> Unit,
    onCategorySelected: (Long?) -> Unit,
    onAccountSelected: (Long?) -> Unit,
    onMinAmountChanged: (String) -> Unit,
    onMaxAmountChanged: (String) -> Unit,
    onRangeSelected: (RecordsDateRange) -> Unit,
    onClearFilters: () -> Unit,
) {
    val hasActiveFilters = uiState.keyword.isNotBlank() ||
        uiState.selectedCategoryId != null ||
        uiState.selectedAccountId != null ||
        uiState.minAmountText.isNotBlank() ||
        uiState.maxAmountText.isNotBlank() ||
        uiState.selectedRange != RecordsDateRange.THIS_MONTH

    var expanded by remember(hasActiveFilters) { mutableStateOf(hasActiveFilters) }

    val activeSummary = buildList {
        if (uiState.keyword.isNotBlank()) add(uiState.keyword)
        uiState.selectedCategoryName?.let(::add)
        uiState.selectedAccountName?.let(::add)
        if (uiState.minAmountText.isNotBlank() || uiState.maxAmountText.isNotBlank()) {
            val min = uiState.minAmountText.ifBlank { "0" }
            val max = uiState.maxAmountText.ifBlank { "∞" }
            add("¥$min-$max")
        }
        if (uiState.selectedRange != RecordsDateRange.THIS_MONTH) {
            add(stringResource(id = uiState.selectedRange.labelResId))
        }
    }.joinToString(separator = " | ")

    val collapsedSummary = when {
        activeSummary.isNotBlank() -> activeSummary
        else -> stringResource(id = R.string.records_filter_collapsed_summary)
    }

    SectionCard(title = stringResource(id = R.string.records_filter_title)) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded },
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
                        text = stringResource(id = R.string.records_filter_title),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = collapsedSummary,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = stringResource(id = R.string.records_filter_expand),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (expanded) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                shape = MaterialTheme.shapes.large,
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    OutlinedTextField(
                        value = uiState.keyword,
                        onValueChange = onKeywordChanged,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                text = stringResource(id = R.string.records_filter_keyword_hint),
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(20.dp),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = null,
                            )
                        },
                        trailingIcon = {
                            if (uiState.keyword.isNotBlank()) {
                                IconButton(onClick = { onKeywordChanged("") }) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = null,
                                    )
                                }
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        ),
                    )

                    FilterSection(label = stringResource(id = R.string.records_filter_range_label)) {
                        uiState.availableRanges.forEach { range ->
                            FilterChip(
                                selected = uiState.selectedRange == range,
                                onClick = { onRangeSelected(range) },
                                label = { Text(text = stringResource(id = range.labelResId)) },
                            )
                        }
                    }

                    FilterSection(label = stringResource(id = R.string.records_filter_category_label)) {
                        FilterChip(
                            selected = uiState.selectedCategoryId == null,
                            onClick = { onCategorySelected(null) },
                            label = { Text(text = stringResource(id = R.string.records_filter_category_all)) },
                        )
                        uiState.categoryOptions.forEach { option ->
                            val emoji = CategoryIcons.getEmoji(option.icon)
                            FilterChip(
                                selected = uiState.selectedCategoryId == option.id,
                                onClick = { onCategorySelected(option.id) },
                                label = { Text(text = "$emoji ${option.label}") },
                            )
                        }
                    }

                    if (uiState.accountOptions.isNotEmpty()) {
                        FilterSection(label = stringResource(id = R.string.records_filter_account_label)) {
                            FilterChip(
                                selected = uiState.selectedAccountId == null,
                                onClick = { onAccountSelected(null) },
                                label = { Text(text = stringResource(id = R.string.records_filter_account_all)) },
                            )
                            uiState.accountOptions.forEach { option ->
                                FilterChip(
                                    selected = uiState.selectedAccountId == option.id,
                                    onClick = { onAccountSelected(option.id) },
                                    label = { Text(text = option.label) },
                                )
                            }
                        }
                    }

                    FilterSection(label = stringResource(id = R.string.records_filter_amount_label)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            OutlinedTextField(
                                value = uiState.minAmountText,
                                onValueChange = onMinAmountChanged,
                                modifier = Modifier.weight(1f),
                                placeholder = {
                                    Text(
                                        text = stringResource(id = R.string.records_filter_amount_min_hint),
                                        style = MaterialTheme.typography.bodySmall,
                                    )
                                },
                                singleLine = true,
                            )
                            Text(
                                text = "—",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            OutlinedTextField(
                                value = uiState.maxAmountText,
                                onValueChange = onMaxAmountChanged,
                                modifier = Modifier.weight(1f),
                                placeholder = {
                                    Text(
                                        text = stringResource(id = R.string.records_filter_amount_max_hint),
                                        style = MaterialTheme.typography.bodySmall,
                                    )
                                },
                                singleLine = true,
                            )
                        }
                    }

                    if (hasActiveFilters) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            shape = MaterialTheme.shapes.medium,
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = activeSummary,
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                TextButton(onClick = onClearFilters) {
                                    Text(text = stringResource(id = R.string.records_filter_clear))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FilterSection(
    label: String,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            content()
        }
    }
}
