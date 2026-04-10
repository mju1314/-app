package com.example.expensetracker.ui.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.input.KeyboardType
import java.time.LocalDate
import com.example.expensetracker.ui.components.SectionCard

@Composable
fun StatsRoute(
    contentPadding: PaddingValues,
    viewModel: StatsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    StatsScreen(
        contentPadding = contentPadding,
        uiState = uiState,
        onPreviousMonthClick = viewModel::showPreviousMonth,
        onNextMonthClick = viewModel::showNextMonth,
        onMonthSelected = viewModel::selectMonth,
        onTrendWindowSelected = viewModel::selectTrendWindow,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StatsScreen(
    contentPadding: PaddingValues,
    uiState: StatsUiState,
    onPreviousMonthClick: () -> Unit,
    onNextMonthClick: () -> Unit,
    onMonthSelected: (Int, Int) -> Unit,
    onTrendWindowSelected: (Int) -> Unit,
) {
    var showMonthPicker by remember { mutableStateOf(false) }

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SectionCard(title = stringResource(id = R.string.stats_month_total_title)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onPreviousMonthClick) {
                    Icon(
                        imageVector = Icons.Filled.ChevronLeft,
                        contentDescription = stringResource(id = R.string.stats_previous_month),
                    )
                }
                Row(
                    modifier = Modifier.clickable { showMonthPicker = true },
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = uiState.monthLabel,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Icon(
                        imageVector = Icons.Filled.CalendarMonth,
                        contentDescription = stringResource(id = R.string.stats_select_month),
                        modifier = Modifier.size(18.dp),
                    )
                }
                IconButton(
                    onClick = onNextMonthClick,
                    enabled = uiState.canNavigateToNextMonth,
                ) {
                    Icon(
                        imageVector = Icons.Filled.ChevronRight,
                        contentDescription = stringResource(id = R.string.stats_next_month),
                    )
                }
            }
            Text(
                text = stringResource(id = R.string.stats_month_picker_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = uiState.monthTotalText,
                modifier = Modifier.padding(top = 8.dp),
                style = MaterialTheme.typography.headlineMedium,
            )
            MetricHighlight(
                modifier = Modifier.padding(top = 12.dp),
                title = stringResource(id = R.string.stats_average_daily_title),
            ) {
                Text(
                    text = uiState.averageDailyText,
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    text = uiState.averageDailyHint,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        SectionCard(title = stringResource(id = R.string.stats_top_category_title)) {
            val topCategory = uiState.topCategory
            if (topCategory == null) {
                Text(
                    text = stringResource(id = R.string.stats_top_category_empty),
                    style = MaterialTheme.typography.bodyMedium,
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = topCategory.categoryName,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Text(
                            text = topCategory.amountText,
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                    Text(
                        text = "${topCategory.ratioText} | ${
                            stringResource(
                                id = R.string.stats_transaction_count,
                                topCategory.transactionCount,
                            )
                        }",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        SectionCard(title = stringResource(id = R.string.stats_category_summary_title)) {
            if (uiState.categorySummaries.isEmpty()) {
                Text(
                    text = stringResource(id = R.string.stats_empty_category_summary),
                    style = MaterialTheme.typography.bodyMedium,
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    uiState.categorySummaries.forEach { item ->
                        CategorySummaryRow(item = item)
                    }
                }
            }
        }

        SectionCard(title = stringResource(id = R.string.stats_trend_title)) {
            Text(
                text = uiState.trendRangeLabel,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf(7, 30).forEach { days ->
                    FilterChip(
                        selected = uiState.selectedTrendWindowDays == days,
                        onClick = { onTrendWindowSelected(days) },
                        label = {
                            Text(
                                text = stringResource(
                                    id = if (days == 7) {
                                        R.string.stats_trend_window_7
                                    } else {
                                        R.string.stats_trend_window_30
                                    },
                                ),
                            )
                        },
                    )
                }
            }
            Column(
                modifier = Modifier.padding(top = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                uiState.recentDailyTrends.forEach { item ->
                    TrendRow(item = item)
                }
            }
        }
    }

    if (showMonthPicker) {
        MonthPickerDialog(
            selectedYear = uiState.selectedYear,
            selectedMonth = uiState.selectedMonth,
            onDismiss = { showMonthPicker = false },
            onConfirm = { year, month ->
                onMonthSelected(year, month)
                showMonthPicker = false
            },
        )
    }
}

@Composable
private fun CategorySummaryRow(item: StatsCategorySummaryUiModel) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = item.categoryName,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = item.amountText,
                style = MaterialTheme.typography.titleMedium,
            )
        }

        Text(
            text = "${item.ratioText} | ${
                stringResource(
                    id = R.string.stats_transaction_count,
                    item.transactionCount,
                )
            }",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(999.dp),
                ),
        ) {
            val fraction = item.ratio.visibleBarFraction()
            if (fraction > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction)
                        .height(8.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(999.dp),
                        ),
                )
            }
        }
    }
}

@Composable
private fun MetricHighlight(
    modifier: Modifier = Modifier,
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        shape = MaterialTheme.shapes.large,
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            content()
        }
    }
}

@Composable
private fun TrendRow(item: StatsTrendPointUiModel) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = item.dayLabel,
            modifier = Modifier.width(44.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .height(10.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(999.dp),
                ),
        ) {
            val fraction = item.barFraction.visibleBarFraction()
            if (fraction > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction)
                        .height(10.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(999.dp),
                        ),
                )
            }
        }

        Text(
            text = item.amountText,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MonthPickerDialog(
    selectedYear: Int,
    selectedMonth: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit,
) {
    val currentDate = LocalDate.now()
    var draftYearText by remember(selectedYear) { mutableStateOf(selectedYear.toString()) }
    var draftMonth by remember(selectedMonth) { mutableIntStateOf(selectedMonth) }
    val draftYear = draftYearText.toIntOrNull()
    val isValidSelection = draftYear != null &&
        draftYear in 1..currentDate.year &&
        !(draftYear == currentDate.year && draftMonth > currentDate.monthValue)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(id = R.string.stats_select_month)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = stringResource(id = R.string.stats_month_picker_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedTextField(
                    value = draftYearText,
                    onValueChange = { value ->
                        draftYearText = value.filter(Char::isDigit).take(4)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(text = stringResource(id = R.string.stats_year_input_label)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    (1..12).forEach { month ->
                        FilterChip(
                            selected = draftMonth == month,
                            onClick = { draftMonth = month },
                            label = { Text(text = month.toMonthLabel()) },
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(checkNotNull(draftYear), draftMonth) },
                enabled = isValidSelection,
            ) {
                Text(text = stringResource(id = R.string.action_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(id = R.string.action_cancel))
            }
        },
    )
}

private fun Int.toMonthLabel(): String = toString().padStart(2, '0')

private fun Float.visibleBarFraction(): Float =
    when {
        this <= 0f -> 0f
        this < 0.06f -> 0.06f
        else -> this.coerceAtMost(1f)
    }
