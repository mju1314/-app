package com.example.expensetracker.data.model

data class RecentTransactionRow(
    val id: Long,
    val amount: Long,
    val note: String?,
    val spentAt: Long,
    val categoryName: String,
    val paymentMethodName: String,
)

