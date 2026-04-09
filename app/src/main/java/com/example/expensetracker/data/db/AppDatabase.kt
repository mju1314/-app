package com.example.expensetracker.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.expensetracker.data.dao.CategoryDao
import com.example.expensetracker.data.dao.PaymentMethodDao
import com.example.expensetracker.data.dao.TransactionDao
import com.example.expensetracker.data.entity.CategoryEntity
import com.example.expensetracker.data.entity.PaymentMethodEntity
import com.example.expensetracker.data.entity.TransactionEntity

@Database(
    entities = [
        CategoryEntity::class,
        PaymentMethodEntity::class,
        TransactionEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun paymentMethodDao(): PaymentMethodDao
    abstract fun transactionDao(): TransactionDao

    companion object {
        const val DATABASE_NAME = "expense_tracker.db"
    }
}
