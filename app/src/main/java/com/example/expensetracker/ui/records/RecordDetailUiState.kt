package com.example.expensetracker.ui.records

import androidx.annotation.StringRes
import com.example.expensetracker.ui.add.SelectOptionUiModel

data class RecordDetailUiState(
    val transactionId: Long = 0,
    val transactionType: Int = 0,
    val amount: String = "",
    val selectedCategoryId: Long? = null,
    val selectedAccountId: Long? = null,
    val note: String = "",
    val spentAtMillis: Long = 0L,
    val spentAtText: String = "",
    val categoryOptions: List<SelectOptionUiModel> = emptyList(),
    val accountOptions: List<SelectOptionUiModel> = emptyList(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isDeleting: Boolean = false,
    @StringRes val errorMessageResId: Int? = null,
)
