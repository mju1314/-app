package com.example.expensetracker.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.common.CurrencyFormatter
import com.example.expensetracker.data.model.CategoryExpenseSummaryRow
import com.example.expensetracker.data.model.DailyExpenseTotalRow
import com.example.expensetracker.data.entity.BudgetEntity
import com.example.expensetracker.data.repository.BudgetRepository
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
    private val budgetRepository: BudgetRepository,
) : ViewModel() {
    private val today = LocalDate.now()
    private val currentMonth = today.withDayOfMonth(1)
    private val monthFormatter = DateTimeFormatter.ofPattern("yyyy-MM")
    private val dayFormatter = DateTimeFormatter.ofPattern("MM-dd")
    private val rangeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    private val selectedMonth = MutableStateFlow(currentMonth)
    private val selectedTrendWindowDays = MutableStateFlow(DEFAULT_TREND_WINDOW_DAYS)
    private val selectedType = MutableStateFlow(0)

    private val initialUiState = StatsUiState(
        monthLabel = currentMonth.format(monthFormatter),
        monthTotalText = CurrencyFormatter.formatCent(0),
        averageDailyText = CurrencyFormatter.formatCent(0),
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
        selectedType,
    ) { month, trendWindowDays, type ->
        StatsQuery(
            month = month.withDayOfMonth(1),
            trendWindowDays = trendWindowDays,
            type = type,
        )
    }.flatMapLatest { query ->
        val trendEndDate = query.month.toTrendEndDate(today)
        combine(
            transactionRepository.observeMonthExpense(query.month).let { expenseFlow ->
                if (query.type == 0) expenseFlow
                else transactionRepository.observeMonthIncome(query.month)
            },
            transactionRepository.observeMonthCategorySummary(query.type, query.month),
            transactionRepository.observeRecentDailyTotals(
                type = query.type,
                days = query.trendWindowDays,
                now = trendEndDate,
            ),
        ) { monthTotal, categoryRows, dailyRows ->
            Triple(monthTotal, categoryRows, dailyRows)
        }.combine(budgetRepository.observeAll()) { (monthTotal, categoryRows, dailyRows), budgets ->
            buildUiState(
                query = query,
                monthTotal = monthTotal,
                categoryRows = categoryRows,
                dailyRows = dailyRows,
                budgets = budgets,
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

    fun selectType(type: Int) {
        selectedType.value = type
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
        budgets: List<BudgetEntity>,
    ): StatsUiState {
        val trendEndDate = query.month.toTrendEndDate(today)
        val trendStartDate = trendEndDate.minusDays((query.trendWindowDays - 1).toLong())
        val trendDates = List(query.trendWindowDays) { index ->
            trendStartDate.plusDays(index.toLong())
        }
        val dailyAmounts = dailyRows.associateByDate()
        val maxDailyAmount = trendDates.maxOfOrNull { date -> dailyAmounts[date] ?: 0L } ?: 0L

        val isExpenseMode = query.type == 0

        val budgetByCategoryName = if (isExpenseMode) {
            budgets.filter { it.categoryId != null }.associateBy { it.categoryId }
        } else {
            emptyMap()
        }
        val totalBudget = if (isExpenseMode) {
            budgets.firstOrNull { it.categoryId == null }
        } else {
            null
        }

        val categorySummaries = categoryRows.toUiModels(monthTotal, budgetByCategoryName)
        val averageDailyDivisor = query.month.toAverageDailyDivisor(today)
        val averageDailyAmount = if (averageDailyDivisor > 0) {
            (monthTotal.toDouble() / averageDailyDivisor.toDouble()).roundToLong()
        } else {
            0L
        }

        val monthBudgetText = totalBudget?.let {
            "${CurrencyFormatter.formatCent(monthTotal)} / ${CurrencyFormatter.formatCent(it.amount)}"
        }
        val monthBudgetFraction = totalBudget?.let {
            if (it.amount > 0) (monthTotal.toFloat() / it.amount.toFloat()).coerceAtMost(1f) else 0f
        } ?: 0f
        val monthBudgetExceeded = totalBudget != null && monthTotal > totalBudget.amount

        return StatsUiState(
            selectedType = query.type,
            monthLabel = query.month.format(monthFormatter),
            selectedYear = query.month.year,
            selectedMonth = query.month.monthValue,
            monthTotalText = CurrencyFormatter.formatCent(monthTotal),
            monthBudgetText = monthBudgetText,
            monthBudgetFraction = monthBudgetFraction,
            monthBudgetExceeded = monthBudgetExceeded,
            averageDailyText = CurrencyFormatter.formatCent(averageDailyAmount),
            averageDailyHint = query.month.toAverageDailyHint(today),
            topCategory = categorySummaries.firstOrNull()?.toTopCategory(),
            categorySummaries = categorySummaries,
            selectedTrendWindowDays = query.trendWindowDays,
            trendRangeLabel = trendStartDate.formatRangeTo(trendEndDate),
            recentDailyTrends = trendDates.map { date ->
                val amount = dailyAmounts[date] ?: 0L
                StatsTrendPointUiModel(
                    dayLabel = date.format(dayFormatter),
                    amountText = CurrencyFormatter.formatCent(amount),
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
        budgetMap: Map<Long?, BudgetEntity>,
    ): List<StatsCategorySummaryUiModel> =
        map { row ->
            val ratio = row.toRatio(monthTotal)
            val budget = budgetMap[row.categoryId]
            val budgetText = budget?.let {
                "${CurrencyFormatter.formatCent(row.totalAmount)} / ${CurrencyFormatter.formatCent(it.amount)}"
            }
            val budgetFraction = budget?.let {
                if (it.amount > 0) (row.totalAmount.toFloat() / it.amount.toFloat()).coerceAtMost(1f) else 0f
            } ?: 0f
            val budgetExceeded = budget != null && row.totalAmount > budget.amount
            StatsCategorySummaryUiModel(
                categoryName = row.categoryName,
                amountText = CurrencyFormatter.formatCent(row.totalAmount),
                ratioText = ratio.toPercentText(),
                ratio = ratio,
                transactionCount = row.transactionCount,
                budgetText = budgetText,
                budgetFraction = budgetFraction,
                budgetExceeded = budgetExceeded,
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
        val type: Int = 0,
    )

    private companion object {
        const val DEFAULT_TREND_WINDOW_DAYS = 7
        const val EXTENDED_TREND_WINDOW_DAYS = 30
    }
}
