package com.example.expensetracker.data.model

data class RecentTransactionRow(
    val id: Long,
    val type: Int,
    val amount: Long,
    val note: String?,
    val spentAt: Long,
    val categoryName: String,
    val categoryIcon: String,
    val accountName: String?,
)
