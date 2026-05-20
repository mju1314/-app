package com.example.expensetracker.data.model

data class CategoryExpenseSummaryRow(
    val categoryId: Long,
    val categoryName: String,
    val totalAmount: Long,
    val transactionCount: Long,
)
