package com.example.expensetracker.data.model

data class TransactionExportRow(
    val id: Long,
    val type: Int,
    val amount: Long,
    val note: String?,
    val spentAt: Long,
    val categoryName: String,
    val accountName: String?,
    val createdAt: Long,
    val updatedAt: Long,
)
