package com.example.expensetracker.data.model

data class TransactionDetailRow(
    val id: Long,
    val type: Int,
    val amount: Long,
    val categoryId: Long,
    val categoryName: String,
    val accountId: Long?,
    val accountName: String?,
    val note: String?,
    val spentAt: Long,
    val createdAt: Long,
    val updatedAt: Long,
)
