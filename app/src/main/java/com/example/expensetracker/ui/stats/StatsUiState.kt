package com.example.expensetracker.ui.stats

data class StatsUiState(
    val monthLabel: String = "",
    val monthTotalText: String = "",
    val averageDailyText: String = "",
    val averageDailyHint: String = "",
    val topCategory: StatsTopCategoryUiModel? = null,
    val categorySummaries: List<StatsCategorySummaryUiModel> = emptyList(),
    val selectedTrendWindowDays: Int = 7,
    val trendRangeLabel: String = "",
    val recentDailyTrends: List<StatsTrendPointUiModel> = emptyList(),
    val canNavigateToNextMonth: Boolean = false,
    val isCurrentMonth: Boolean = true,
    val isLoading: Boolean = true,
)

data class StatsCategorySummaryUiModel(
    val categoryName: String,
    val amountText: String,
    val ratioText: String,
    val ratio: Float,
    val transactionCount: Long,
)

data class StatsTopCategoryUiModel(
    val categoryName: String,
    val amountText: String,
    val ratioText: String,
    val transactionCount: Long,
)

data class StatsTrendPointUiModel(
    val dayLabel: String,
    val amountText: String,
    val barFraction: Float,
)
