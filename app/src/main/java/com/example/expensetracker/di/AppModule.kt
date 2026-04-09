package com.example.expensetracker.di

import android.content.Context
import androidx.room.Room
import com.example.expensetracker.data.dao.CategoryDao
import com.example.expensetracker.data.dao.PaymentMethodDao
import com.example.expensetracker.data.dao.TransactionDao
import com.example.expensetracker.data.db.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME,
        ).build()

    @Provides
    fun provideCategoryDao(database: AppDatabase): CategoryDao = database.categoryDao()

    @Provides
    fun providePaymentMethodDao(database: AppDatabase): PaymentMethodDao = database.paymentMethodDao()

    @Provides
    fun provideTransactionDao(database: AppDatabase): TransactionDao = database.transactionDao()
}
