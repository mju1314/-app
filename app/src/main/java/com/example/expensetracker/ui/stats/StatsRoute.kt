package com.example.expensetracker.ui.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.expensetracker.R
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
    )
}

@Composable
private fun StatsScreen(
    contentPadding: PaddingValues,
    uiState: StatsUiState,
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

    androidx.compose.foundation.layout.Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SectionCard(title = stringResource(id = R.string.stats_month_total_title)) {
            Text(
                text = uiState.monthLabel,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = uiState.monthTotalText,
                modifier = Modifier.padding(top = 4.dp),
                style = MaterialTheme.typography.headlineMedium,
            )
        }

        SectionCard(title = stringResource(id = R.string.stats_category_summary_title)) {
            if (uiState.categorySummaries.isEmpty()) {
                Text(
                    text = stringResource(id = R.string.stats_empty_category_summary),
                    style = MaterialTheme.typography.bodyMedium,
                )
            } else {
                androidx.compose.foundation.layout.Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    uiState.categorySummaries.forEach { item ->
                        CategorySummaryRow(item = item)
                    }
                }
            }
        }

        SectionCard(title = stringResource(id = R.string.stats_recent_seven_days_title)) {
            androidx.compose.foundation.layout.Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                uiState.recentDailyTrends.forEach { item ->
                    TrendRow(item = item)
                }
            }
        }
    }
}

@Composable
private fun CategorySummaryRow(item: StatsCategorySummaryUiModel) {
    androidx.compose.foundation.layout.Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
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
            text = stringResource(id = R.string.stats_transaction_count, item.transactionCount),
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

private fun Float.visibleBarFraction(): Float =
    when {
        this <= 0f -> 0f
        this < 0.06f -> 0.06f
        else -> this.coerceAtMost(1f)
    }
