package com.example.expensetracker.ui.settings

import androidx.annotation.StringRes
import com.example.expensetracker.R
import com.example.expensetracker.common.CurrencyFormatter

data class SettingsUiState(
    val selectedCurrencyCode: String = CurrencyFormatter.DEFAULT_CURRENCY_CODE,
    val isClearingData: Boolean = false,
    val isExportingCsv: Boolean = false,
    val isBackingUp: Boolean = false,
    val isRestoring: Boolean = false,
    @StringRes val infoMessageResId: Int? = null,
    @StringRes val exportMessageResId: Int? = null,
    @StringRes val backupMessageResId: Int? = null,
    @StringRes val restoreMessageResId: Int? = null,
)

data class CurrencyOptionUiModel(
    val code: String,
    @StringRes val labelResId: Int,
)

val currencyOptions = listOf(
    CurrencyOptionUiModel(code = "CNY", labelResId = R.string.settings_currency_cny),
    CurrencyOptionUiModel(code = "USD", labelResId = R.string.settings_currency_usd),
    CurrencyOptionUiModel(code = "EUR", labelResId = R.string.settings_currency_eur),
    CurrencyOptionUiModel(code = "JPY", labelResId = R.string.settings_currency_jpy),
)
