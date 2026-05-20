package com.example.expensetracker.ui.settings

import androidx.annotation.StringRes

data class SettingsUiState(
    val accounts: List<AccountUiModel> = emptyList(),
    val totalBalanceText: String = "",
    val budgets: List<BudgetUiModel> = emptyList(),
    val categoryOptions: List<CategoryOptionUiModel> = emptyList(),
    val isClearingData: Boolean = false,
    val isExportingCsv: Boolean = false,
    val isBackingUp: Boolean = false,
    val isRestoring: Boolean = false,
    @StringRes val infoMessageResId: Int? = null,
    @StringRes val exportMessageResId: Int? = null,
    @StringRes val backupMessageResId: Int? = null,
    @StringRes val restoreMessageResId: Int? = null,
)

data class AccountUiModel(
    val id: Long,
    val name: String,
    val balanceText: String,
    val balanceInCent: Long,
)

data class BudgetUiModel(
    val id: Long,
    val categoryId: Long?,
    val categoryName: String?,
    val amountText: String,
    val amountInCent: Long,
)

data class CategoryOptionUiModel(
    val id: Long,
    val name: String,
)
