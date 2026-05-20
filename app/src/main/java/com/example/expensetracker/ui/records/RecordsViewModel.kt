package com.example.expensetracker.ui.records

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.R
import com.example.expensetracker.common.CurrencyFormatter
import com.example.expensetracker.data.entity.TransactionEntity
import com.example.expensetracker.data.model.RecentTransactionRow
import com.example.expensetracker.data.repository.AccountRepository
import com.example.expensetracker.data.repository.CategoryRepository
import com.example.expensetracker.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class RecordsViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    categoryRepository: CategoryRepository,
    accountRepository: AccountRepository,
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
            accountId = state.selectedAccountId,
            startTime = range.startTime,
            endTime = range.endTime,
            minAmount = state.minAmountText.toAmountInCent(),
            maxAmount = state.maxAmountText.toAmountInCent(),
        )
    }

    val uiState: StateFlow<RecordsUiState> = combine(
        filterState,
        filteredTransactions,
        categoryRepository.observeActiveCategories().map { categories ->
            categories.map { RecordFilterOptionUiModel(id = it.id, label = it.name, icon = it.icon) }
        },
        accountRepository.observeAll().map { accounts ->
            accounts.map { RecordFilterOptionUiModel(id = it.id, label = it.name) }
        },
    ) { filter, rows, categories, accounts ->
        val grouped = rows.groupBy { row ->
            Instant.ofEpochMilli(row.spentAt)
                .atZone(zoneId)
                .toLocalDate()
        }

        val selectedCategoryName = filter.selectedCategoryId
            ?.let { targetId -> categories.firstOrNull { it.id == targetId }?.label }

        val selectedAccountName = filter.selectedAccountId
            ?.let { targetId -> accounts.firstOrNull { it.id == targetId }?.label }

        filter.copy(
            selectedCategoryName = selectedCategoryName,
            categoryOptions = categories,
            selectedAccountName = selectedAccountName,
            accountOptions = accounts,
            groups = grouped.entries
                .sortedByDescending { it.key }
                .map { entry ->
                    entry.toUiGroup(dateFormatter)
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

    fun selectAccount(accountId: Long?) {
        filterState.value = filterState.value.copy(
            selectedAccountId = accountId,
            isLoading = true,
        )
    }

    fun updateMinAmount(value: String) {
        filterState.value = filterState.value.copy(
            minAmountText = value.sanitizeAmountInput(),
            isLoading = true,
        )
    }

    fun updateMaxAmount(value: String) {
        filterState.value = filterState.value.copy(
            maxAmountText = value.sanitizeAmountInput(),
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

    suspend fun getTransactionById(id: Long): TransactionEntity? =
        transactionRepository.getTransactionById(id)
}

private fun String.sanitizeAmountInput(): String =
    filter { it.isDigit() || it == '.' }.let { text ->
        val firstDot = text.indexOf('.')
        if (firstDot < 0) text
        else text.substring(0, firstDot + 1) + text.substring(firstDot + 1).replace(".", "").take(2)
    }

private fun String.toAmountInCent(): Long? {
    if (isBlank()) return null
    return runCatching {
        BigDecimal(this)
            .multiply(BigDecimal(100))
            .setScale(0, RoundingMode.HALF_UP)
            .longValueExact()
    }.getOrNull()
}

private fun Map.Entry<LocalDate, List<RecentTransactionRow>>.toUiGroup(
    formatter: DateTimeFormatter,
): RecordDayGroupUiModel {
    val items = value.map { row ->
        val subtitle = row.accountName
        RecordListItemUiModel(
            id = row.id,
            type = row.type,
            title = row.note?.takeIf { it.isNotBlank() } ?: row.categoryName,
            subtitle = if (subtitle != null) "${row.categoryName} · $subtitle" else row.categoryName,
            amountText = CurrencyFormatter.formatCentWithSign(row.amount, row.type),
        )
    }
    val expenseTotal = value.filter { it.type == 0 }.sumOf { it.amount }
    return RecordDayGroupUiModel(
        dateLabel = key.format(formatter),
        totalAmountText = CurrencyFormatter.formatCent(expenseTotal),
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
