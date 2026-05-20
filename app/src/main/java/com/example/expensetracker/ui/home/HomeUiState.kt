package com.example.expensetracker.ui.home

import androidx.annotation.StringRes
import com.example.expensetracker.R

data class HomeUiState(
    val todayExpenseText: String = "",
    val todayIncomeText: String = "",
    val monthExpenseText: String = "",
    val monthIncomeText: String = "",
    val recentRecords: List<HomeRecentRecordUiModel> = emptyList(),
    val isLoading: Boolean = true,
)

data class HomeRecentRecordUiModel(
    val id: Long,
    val type: Int = 0,
    val title: String,
    val categoryIcon: String = "",
    @StringRes val subtitleResId: Int = R.string.home_recent_record_subtitle,
    val subtitleArgs: List<String> = emptyList(),
    val amountText: String,
)
