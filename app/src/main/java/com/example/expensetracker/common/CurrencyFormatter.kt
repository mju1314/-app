package com.example.expensetracker.common

import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

object CurrencyFormatter {
    const val DEFAULT_CURRENCY_CODE = "CNY"

    fun formatCent(amountInCent: Long): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale.CHINA).apply {
            currency = Currency.getInstance(DEFAULT_CURRENCY_CODE)
            maximumFractionDigits = 2
            minimumFractionDigits = 2
        }
        return formatter.format(amountInCent / 100.0)
    }

    fun formatCentWithSign(amountInCent: Long, type: Int): String {
        val formatted = formatCent(amountInCent)
        return if (type == 1) "+$formatted" else "-$formatted"
    }
}
