package com.example.expensetracker.common

import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

object CurrencyFormatter {
    const val DEFAULT_CURRENCY_CODE = "CNY"

    fun formatCent(
        amountInCent: Long,
        currencyCode: String = DEFAULT_CURRENCY_CODE,
    ): String {
        val formatter = NumberFormat.getCurrencyInstance(localeFor(currencyCode)).apply {
            currency = runCatching { Currency.getInstance(currencyCode) }
                .getOrDefault(Currency.getInstance(DEFAULT_CURRENCY_CODE))
            maximumFractionDigits = 2
            minimumFractionDigits = 2
        }
        return formatter.format(amountInCent / 100.0)
    }

    private fun localeFor(currencyCode: String): Locale =
        when (currencyCode) {
            "USD" -> Locale.US
            "EUR" -> Locale.GERMANY
            "JPY" -> Locale.JAPAN
            else -> Locale.CHINA
        }
}
