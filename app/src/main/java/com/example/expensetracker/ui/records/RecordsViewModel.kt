package com.example.expensetracker.ui.records

import com.example.expensetracker.R
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.common.CurrencyFormatter
import com.example.expensetracker.data.preferences.UserPreferencesRepository
import com.example.expensetracker.data.model.RecentTransactionRow
import com.example.expensetracker.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class RecordsViewModel @Inject constructor(
    transactionRepository: TransactionRepository,
    userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    val uiState: StateFlow<RecordsUiState> = combine(
        transactionRepository.observeAllTransactions(),
        userPreferencesRepository.defaultCurrencyCode,
    ) { rows, currencyCode ->
            val grouped = rows.groupBy { row ->
                Instant.ofEpochMilli(row.spentAt)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
            }

            RecordsUiState(
                groups = grouped.entries
                    .sortedByDescending { it.key }
                    .map { entry ->
                        entry.toUiGroup(dateFormatter, currencyCode)
                    },
                isLoading = false,
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = RecordsUiState(),
        )
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
