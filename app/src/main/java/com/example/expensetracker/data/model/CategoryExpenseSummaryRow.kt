package com.example.expensetracker.data.model

data class CategoryExpenseSummaryRow(
    val categoryName: String,
    val totalAmount: Long,
    val transactionCount: Long,
)
