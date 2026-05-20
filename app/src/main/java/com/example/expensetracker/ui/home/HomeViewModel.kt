package com.example.expensetracker.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.common.CurrencyFormatter
import com.example.expensetracker.common.DateFormats
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
) : ViewModel() {
    private val initialUiState = HomeUiState(
        todayExpenseText = CurrencyFormatter.formatCent(0),
        todayIncomeText = CurrencyFormatter.formatCent(0),
        monthExpenseText = CurrencyFormatter.formatCent(0),
        monthIncomeText = CurrencyFormatter.formatCent(0),
    )

    val uiState: StateFlow<HomeUiState> = combine(
        transactionRepository.observeTodayExpense(),
        transactionRepository.observeTodayIncome(),
        transactionRepository.observeMonthExpense(),
        transactionRepository.observeMonthIncome(),
        transactionRepository.observeRecentTransactions(),
    ) { todayExpense, todayIncome, monthExpense, monthIncome, recentTransactions ->
        HomeUiState(
            todayExpenseText = CurrencyFormatter.formatCent(todayExpense),
            todayIncomeText = CurrencyFormatter.formatCent(todayIncome),
            monthExpenseText = CurrencyFormatter.formatCent(monthExpense),
            monthIncomeText = CurrencyFormatter.formatCent(monthIncome),
            recentRecords = recentTransactions.map { item ->
                HomeRecentRecordUiModel(
                    id = item.id,
                    type = item.type,
                    title = item.note?.takeIf { it.isNotBlank() } ?: item.categoryName,
                    categoryIcon = item.categoryIcon,
                    subtitleArgs = listOf(DateFormats.formatMonthDay(item.spentAt), item.categoryName),
                    amountText = CurrencyFormatter.formatCentWithSign(item.amount, item.type),
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
