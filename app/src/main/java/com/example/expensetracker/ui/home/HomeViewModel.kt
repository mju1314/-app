package com.example.expensetracker.ui.home

import com.example.expensetracker.R
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.annotation.StringRes
import com.example.expensetracker.common.CurrencyFormatter
import com.example.expensetracker.common.DateFormats
import com.example.expensetracker.data.preferences.UserPreferencesRepository
import com.example.expensetracker.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class HomeViewModel @Inject constructor(
    transactionRepository: TransactionRepository,
    userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {
    private val initialUiState = HomeUiState(
        todayTotalText = CurrencyFormatter.formatCent(0, CurrencyFormatter.DEFAULT_CURRENCY_CODE),
        monthTotalText = CurrencyFormatter.formatCent(0, CurrencyFormatter.DEFAULT_CURRENCY_CODE),
    )

    val uiState: StateFlow<HomeUiState> = combine(
        transactionRepository.observeTodayTotal(),
        transactionRepository.observeMonthTotal(),
        transactionRepository.observeRecentTransactions(),
        userPreferencesRepository.defaultCurrencyCode,
    ) { todayTotal, monthTotal, recentTransactions, currencyCode ->
        HomeUiState(
            todayTotalText = CurrencyFormatter.formatCent(todayTotal, currencyCode),
            monthTotalText = CurrencyFormatter.formatCent(monthTotal, currencyCode),
            recentRecords = recentTransactions.map { item ->
                HomeRecentRecordUiModel(
                    id = item.id,
                    title = item.note?.takeIf { it.isNotBlank() } ?: item.categoryName,
                    subtitleArgs = listOf(DateFormats.formatMonthDay(item.spentAt), item.categoryName),
                    amountText = CurrencyFormatter.formatCent(item.amount, currencyCode),
                )
            },
            isLoading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialUiState,
    )
}
