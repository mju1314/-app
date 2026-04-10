package com.example.expensetracker.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.common.CurrencyFormatter
import com.example.expensetracker.data.model.CategoryExpenseSummaryRow
import com.example.expensetracker.data.model.DailyExpenseTotalRow
import com.example.expensetracker.data.preferences.UserPreferencesRepository
import com.example.expensetracker.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlin.math.roundToLong
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {
    private val today = LocalDate.now()
    private val currentMonth = today.withDayOfMonth(1)
    private val monthFormatter = DateTimeFormatter.ofPattern("yyyy-MM")
    private val dayFormatter = DateTimeFormatter.ofPattern("MM-dd")
    private val rangeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    private val selectedMonth = MutableStateFlow(currentMonth)
    private val selectedTrendWindowDays = MutableStateFlow(DEFAULT_TREND_WINDOW_DAYS)

    private val initialUiState = StatsUiState(
        monthLabel = currentMonth.format(monthFormatter),
        monthTotalText = CurrencyFormatter.formatCent(0, CurrencyFormatter.DEFAULT_CURRENCY_CODE),
        averageDailyText = CurrencyFormatter.formatCent(0, CurrencyFormatter.DEFAULT_CURRENCY_CODE),
        averageDailyHint = currentMonth.toAverageDailyHint(today),
        selectedTrendWindowDays = DEFAULT_TREND_WINDOW_DAYS,
        trendRangeLabel = currentMonth
            .toTrendEndDate(today)
            .let { endDate ->
                endDate.minusDays((DEFAULT_TREND_WINDOW_DAYS - 1).toLong()).formatRangeTo(endDate)
            },
        isLoading = true,
    )

    val uiState: StateFlow<StatsUiState> = combine(
        selectedMonth,
        selectedTrendWindowDays,
        userPreferencesRepository.defaultCurrencyCode,
    ) { month, trendWindowDays, currencyCode ->
        StatsQuery(
            month = month.withDayOfMonth(1),
            trendWindowDays = trendWindowDays,
            currencyCode = currencyCode,
        )
    }.flatMapLatest { query ->
        val trendEndDate = query.month.toTrendEndDate(today)
        combine(
            transactionRepository.observeMonthTotal(query.month),
            transactionRepository.observeMonthCategorySummary(query.month),
            transactionRepository.observeRecentDailyTotals(
                days = query.trendWindowDays,
                now = trendEndDate,
            ),
        ) { monthTotal, categoryRows, dailyRows ->
            buildUiState(
                query = query,
                monthTotal = monthTotal,
                categoryRows = categoryRows,
                dailyRows = dailyRows,
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialUiState,
    )

    fun showPreviousMonth() {
        selectedMonth.update { month -> month.minusMonths(1).withDayOfMonth(1) }
    }

    fun showNextMonth() {
        selectedMonth.update { month ->
            if (month.isBefore(currentMonth)) {
                month.plusMonths(1).withDayOfMonth(1)
            } else {
                month
            }
        }
    }

    fun selectTrendWindow(days: Int) {
        if (days == DEFAULT_TREND_WINDOW_DAYS || days == EXTENDED_TREND_WINDOW_DAYS) {
            selectedTrendWindowDays.value = days
        }
    }

    fun selectMonth(year: Int, month: Int) {
        if (month !in 1..12) return
        if (year !in 1..currentMonth.year) return

        val targetMonth = LocalDate.of(year, month, 1)
        if (targetMonth.isAfter(currentMonth)) return
        selectedMonth.value = targetMonth
    }

    private fun buildUiState(
        query: StatsQuery,
        monthTotal: Long,
        categoryRows: List<CategoryExpenseSummaryRow>,
        dailyRows: List<DailyExpenseTotalRow>,
    ): StatsUiState {
        val trendEndDate = query.month.toTrendEndDate(today)
        val trendStartDate = trendEndDate.minusDays((query.trendWindowDays - 1).toLong())
        val trendDates = List(query.trendWindowDays) { index ->
            trendStartDate.plusDays(index.toLong())
        }
        val dailyAmounts = dailyRows.associateByDate()
        val maxDailyAmount = trendDates.maxOfOrNull { date -> dailyAmounts[date] ?: 0L } ?: 0L
        val categorySummaries = categoryRows.toUiModels(
            monthTotal = monthTotal,
            currencyCode = query.currencyCode,
        )
        val averageDailyDivisor = query.month.toAverageDailyDivisor(today)
        val averageDailyAmount = if (averageDailyDivisor > 0) {
            (monthTotal.toDouble() / averageDailyDivisor.toDouble()).roundToLong()
        } else {
            0L
        }

        return StatsUiState(
            monthLabel = query.month.format(monthFormatter),
            selectedYear = query.month.year,
            selectedMonth = query.month.monthValue,
            monthTotalText = CurrencyFormatter.formatCent(monthTotal, query.currencyCode),
            averageDailyText = CurrencyFormatter.formatCent(averageDailyAmount, query.currencyCode),
            averageDailyHint = query.month.toAverageDailyHint(today),
            topCategory = categorySummaries.firstOrNull()?.toTopCategory(),
            categorySummaries = categorySummaries,
            selectedTrendWindowDays = query.trendWindowDays,
            trendRangeLabel = trendStartDate.formatRangeTo(trendEndDate),
            recentDailyTrends = trendDates.map { date ->
                val amount = dailyAmounts[date] ?: 0L
                StatsTrendPointUiModel(
                    dayLabel = date.format(dayFormatter),
                    amountText = CurrencyFormatter.formatCent(amount, query.currencyCode),
                    barFraction = if (maxDailyAmount > 0L) {
                        amount.toFloat() / maxDailyAmount.toFloat()
                    } else {
                        0f
                    },
                )
            },
            canNavigateToNextMonth = query.month.isBefore(currentMonth),
            isCurrentMonth = query.month == currentMonth,
            isLoading = false,
        )
    }

    private fun LocalDate.toTrendEndDate(today: LocalDate): LocalDate =
        if (year == today.year && month == today.month) {
            today
        } else {
            withDayOfMonth(lengthOfMonth())
        }

    private fun LocalDate.toAverageDailyDivisor(today: LocalDate): Int =
        if (year == today.year && month == today.month) {
            today.dayOfMonth
        } else {
            lengthOfMonth()
        }

    private fun LocalDate.toAverageDailyHint(today: LocalDate): String {
        val divisor = toAverageDailyDivisor(today)
        return if (year == today.year && month == today.month) {
            "\u6309\u5df2\u8fc7 ${divisor} \u5929\u8ba1\u7b97"
        } else {
            "\u6309 ${divisor} \u5929\u8ba1\u7b97"
        }
    }

    private fun LocalDate.formatRangeTo(other: LocalDate): String =
        "${format(rangeFormatter)} - ${other.format(rangeFormatter)}"

    private fun List<CategoryExpenseSummaryRow>.toUiModels(
        monthTotal: Long,
        currencyCode: String,
    ): List<StatsCategorySummaryUiModel> =
        map { row ->
            val ratio = row.toRatio(monthTotal)
            StatsCategorySummaryUiModel(
                categoryName = row.categoryName,
                amountText = CurrencyFormatter.formatCent(row.totalAmount, currencyCode),
                ratioText = ratio.toPercentText(),
                ratio = ratio,
                transactionCount = row.transactionCount,
            )
        }

    private fun List<DailyExpenseTotalRow>.associateByDate(): Map<LocalDate, Long> =
        associate { row ->
            LocalDate.parse(row.day) to row.totalAmount
        }

    private fun CategoryExpenseSummaryRow.toRatio(monthTotal: Long): Float =
        if (monthTotal > 0L) {
            totalAmount.toFloat() / monthTotal.toFloat()
        } else {
            0f
        }

    private fun Float.toPercentText(): String {
        val percentage = this * 100f
        val rounded = (percentage * 10).roundToLong() / 10.0
        val text = if (rounded % 1.0 == 0.0) {
            rounded.toLong().toString()
        } else {
            rounded.toString()
        }
        return "$text%"
    }

    private fun StatsCategorySummaryUiModel.toTopCategory(): StatsTopCategoryUiModel =
        StatsTopCategoryUiModel(
            categoryName = categoryName,
            amountText = amountText,
            ratioText = ratioText,
            transactionCount = transactionCount,
        )

    private data class StatsQuery(
        val month: LocalDate,
        val trendWindowDays: Int,
        val currencyCode: String,
    )

    private companion object {
        const val DEFAULT_TREND_WINDOW_DAYS = 7
        const val EXTENDED_TREND_WINDOW_DAYS = 30
    }
}
