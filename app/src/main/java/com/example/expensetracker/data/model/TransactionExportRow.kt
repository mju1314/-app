package com.example.expensetracker.data.model

data class TransactionExportRow(
    val id: Long,
    val amount: Long,
    val note: String?,
    val spentAt: Long,
    val categoryName: String,
    val paymentMethodName: String,
    val createdAt: Long,
    val updatedAt: Long,
)
