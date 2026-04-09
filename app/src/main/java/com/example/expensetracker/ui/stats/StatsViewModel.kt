package com.example.expensetracker.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.common.CurrencyFormatter
import com.example.expensetracker.data.preferences.UserPreferencesRepository
import com.example.expensetracker.data.model.CategoryExpenseSummaryRow
import com.example.expensetracker.data.model.DailyExpenseTotalRow
import com.example.expensetracker.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class StatsViewModel @Inject constructor(
    transactionRepository: TransactionRepository,
    userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {
    private val today = LocalDate.now()
    private val monthFormatter = DateTimeFormatter.ofPattern("yyyy-MM")
    private val dayFormatter = DateTimeFormatter.ofPattern("MM-dd")

    val uiState: StateFlow<StatsUiState> = combine(
        transactionRepository.observeMonthTotal(today),
        transactionRepository.observeMonthCategorySummary(today),
        transactionRepository.observeRecentDailyTotals(days = 7, now = today),
        userPreferencesRepository.defaultCurrencyCode,
    ) { monthTotal, categoryRows, dailyRows, currencyCode ->
        val recentDays = (6 downTo 0).map { offset -> today.minusDays(offset.toLong()) }
        val dailyAmounts = dailyRows.associateByDate()
        val maxDailyAmount = recentDays.maxOfOrNull { date -> dailyAmounts[date] ?: 0L } ?: 0L

        StatsUiState(
            monthLabel = monthFormatter.format(today),
            monthTotalText = CurrencyFormatter.formatCent(monthTotal, currencyCode),
            categorySummaries = categoryRows.toUiModels(monthTotal, currencyCode),
            recentDailyTrends = recentDays.map { date ->
                val amount = dailyAmounts[date] ?: 0L
                StatsTrendPointUiModel(
                    dayLabel = date.format(dayFormatter),
                    amountText = CurrencyFormatter.formatCent(amount, currencyCode),
                    barFraction = if (maxDailyAmount > 0L) amount.toFloat() / maxDailyAmount else 0f,
                )
            },
            isLoading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = StatsUiState(),
    )
}

private fun List<CategoryExpenseSummaryRow>.toUiModels(
    monthTotal: Long,
    currencyCode: String,
): List<StatsCategorySummaryUiModel> =
    map { row ->
        StatsCategorySummaryUiModel(
            categoryName = row.categoryName,
            amountText = CurrencyFormatter.formatCent(row.totalAmount, currencyCode),
            ratio = if (monthTotal > 0L) row.totalAmount.toFloat() / monthTotal else 0f,
            transactionCount = row.transactionCount,
        )
    }

private fun List<DailyExpenseTotalRow>.associateByDate(): Map<LocalDate, Long> =
    associate { row ->
        LocalDate.parse(row.day) to row.totalAmount
    }
