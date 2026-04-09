package com.example.expensetracker.ui.stats

data class StatsUiState(
    val monthLabel: String = "",
    val monthTotalText: String = "",
    val categorySummaries: List<StatsCategorySummaryUiModel> = emptyList(),
    val recentDailyTrends: List<StatsTrendPointUiModel> = emptyList(),
    val isLoading: Boolean = true,
)

data class StatsCategorySummaryUiModel(
    val categoryName: String,
    val amountText: String,
    val ratio: Float,
    val transactionCount: Long,
)

data class StatsTrendPointUiModel(
    val dayLabel: String,
    val amountText: String,
    val barFraction: Float,
)
