package com.example.expensetracker.ui.stats

data class StatsUiState(
    val selectedType: Int = 0,
    val monthLabel: String = "",
    val selectedYear: Int = 0,
    val selectedMonth: Int = 0,
    val monthTotalText: String = "",
    val monthBudgetText: String? = null,
    val monthBudgetFraction: Float = 0f,
    val monthBudgetExceeded: Boolean = false,
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
    val budgetText: String? = null,
    val budgetFraction: Float = 0f,
    val budgetExceeded: Boolean = false,
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
