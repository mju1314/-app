package com.example.expensetracker.ui.add

import androidx.annotation.StringRes

data class AddExpenseUiState(
    val amount: String = "",
    val selectedCategoryId: Long? = null,
    val selectedCategoryName: String? = null,
    val selectedPaymentMethodId: Long? = null,
    val selectedPaymentMethodName: String? = null,
    val note: String = "",
    val spentAtMillis: Long = 0L,
    val spentAtText: String = "",
    val categoryOptions: List<SelectOptionUiModel> = emptyList(),
    val paymentMethodOptions: List<SelectOptionUiModel> = emptyList(),
    val isSaving: Boolean = false,
    @StringRes val errorMessageResId: Int? = null,
)

data class SelectOptionUiModel(
    val id: Long,
    val label: String,
)
