package com.example.expensetracker.ui.records

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.R
import com.example.expensetracker.common.CurrencyFormatter
import com.example.expensetracker.data.model.RecentTransactionRow
import com.example.expensetracker.data.preferences.UserPreferencesRepository
import com.example.expensetracker.data.repository.CategoryRepository
import com.example.expensetracker.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class RecordsViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    categoryRepository: CategoryRepository,
    userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {
    private val zoneId = ZoneId.systemDefault()
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val filterState = MutableStateFlow(
        RecordsUiState(
            selectedRange = RecordsDateRange.THIS_MONTH,
        ),
    )

    private val filteredTransactions = filterState.flatMapLatest { state ->
        val range = state.selectedRange.toFilterRange(zoneId)
        transactionRepository.observeFilteredTransactions(
            keyword = state.keyword,
            categoryId = state.selectedCategoryId,
            startTime = range.startTime,
            endTime = range.endTime,
        )
    }

    val uiState: StateFlow<RecordsUiState> = combine(
        filterState,
        filteredTransactions,
        categoryRepository.observeActiveCategories().map { categories ->
            categories.map { RecordFilterOptionUiModel(id = it.id, label = it.name) }
        },
        userPreferencesRepository.defaultCurrencyCode,
    ) { filter, rows, categories, currencyCode ->
        val grouped = rows.groupBy { row ->
            Instant.ofEpochMilli(row.spentAt)
                .atZone(zoneId)
                .toLocalDate()
        }

        val selectedCategoryName = filter.selectedCategoryId
            ?.let { targetId -> categories.firstOrNull { it.id == targetId }?.label }

        filter.copy(
            selectedCategoryName = selectedCategoryName,
            categoryOptions = categories,
            groups = grouped.entries
                .sortedByDescending { it.key }
                .map { entry ->
                    entry.toUiGroup(dateFormatter, currencyCode)
                },
            isLoading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = RecordsUiState(
            selectedRange = RecordsDateRange.THIS_MONTH,
        ),
    )

    fun updateKeyword(keyword: String) {
        filterState.value = filterState.value.copy(
            keyword = keyword,
            isLoading = true,
        )
    }

    fun selectCategory(categoryId: Long?) {
        filterState.value = filterState.value.copy(
            selectedCategoryId = categoryId,
            isLoading = true,
        )
    }

    fun selectRange(range: RecordsDateRange) {
        filterState.value = filterState.value.copy(
            selectedRange = range,
            isLoading = true,
        )
    }

    fun clearFilters() {
        filterState.value = RecordsUiState(
            selectedRange = RecordsDateRange.THIS_MONTH,
            isLoading = true,
        )
    }
}

private fun Map.Entry<LocalDate, List<RecentTransactionRow>>.toUiGroup(
    formatter: DateTimeFormatter,
    currencyCode: String,
): RecordDayGroupUiModel {
    val items = value.map { row ->
        RecordListItemUiModel(
            id = row.id,
            title = row.note?.takeIf { it.isNotBlank() } ?: row.categoryName,
            subtitleArgs = listOf(row.categoryName, row.paymentMethodName),
            amountText = CurrencyFormatter.formatCent(row.amount, currencyCode),
        )
    }
    return RecordDayGroupUiModel(
        dateLabel = key.format(formatter),
        totalAmountText = CurrencyFormatter.formatCent(value.sumOf { it.amount }, currencyCode),
        items = items,
        titleResId = R.string.records_group_title,
    )
}

private fun RecordsDateRange.toFilterRange(zoneId: ZoneId): RecordsFilterRange {
    val today = LocalDate.now(zoneId)
    return when (this) {
        RecordsDateRange.ALL -> RecordsFilterRange(startTime = null, endTime = null)
        RecordsDateRange.LAST_7_DAYS -> {
            val start = today.minusDays(6).atStartOfDay(zoneId).toInstant().toEpochMilli()
            val end = today.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
            RecordsFilterRange(startTime = start, endTime = end)
        }

        RecordsDateRange.THIS_MONTH -> {
            val start = today.withDayOfMonth(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
            val end = today.plusMonths(1).withDayOfMonth(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
            RecordsFilterRange(startTime = start, endTime = end)
        }
    }
}
