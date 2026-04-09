package com.example.expensetracker.data.model

data class TransactionDetailRow(
    val id: Long,
    val amount: Long,
    val categoryId: Long,
    val categoryName: String,
    val paymentMethodId: Long,
    val paymentMethodName: String,
    val note: String?,
    val spentAt: Long,
    val createdAt: Long,
    val updatedAt: Long,
)

