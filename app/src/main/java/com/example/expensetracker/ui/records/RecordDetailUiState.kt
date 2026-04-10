package com.example.expensetracker.ui.records

import androidx.annotation.StringRes
import com.example.expensetracker.ui.add.SelectOptionUiModel

data class RecordDetailUiState(
    val transactionId: Long = 0,
    val amount: String = "",
    val selectedCategoryId: Long? = null,
    val selectedPaymentMethodId: Long? = null,
    val note: String = "",
    val spentAtMillis: Long = 0L,
    val spentAtText: String = "",
    val categoryOptions: List<SelectOptionUiModel> = emptyList(),
    val paymentMethodOptions: List<SelectOptionUiModel> = emptyList(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isDeleting: Boolean = false,
    @StringRes val errorMessageResId: Int? = null,
)
