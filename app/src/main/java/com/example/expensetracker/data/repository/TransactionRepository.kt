package com.example.expensetracker.data.repository

import com.example.expensetracker.data.dao.TransactionDao
import com.example.expensetracker.data.model.CategoryExpenseSummaryRow
import com.example.expensetracker.data.model.DailyExpenseTotalRow
import com.example.expensetracker.data.entity.TransactionEntity
import com.example.expensetracker.data.model.RecentTransactionRow
import com.example.expensetracker.data.model.TransactionExportRow
import com.example.expensetracker.data.model.TransactionDetailRow
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class TransactionRepository @Inject constructor(
    private val transactionDao: TransactionDao,
) {
    private val zoneId = ZoneId.systemDefault()

    fun observeAllTransactions(): Flow<List<RecentTransactionRow>> =
        transactionDao.observeAllTransactions()

    fun observeFilteredTransactions(
        keyword: String,
        categoryId: Long?,
        startTime: Long?,
        endTime: Long?,
    ): Flow<List<RecentTransactionRow>> =
        transactionDao.observeFilteredTransactions(
            keyword = keyword.trim(),
            categoryId = categoryId,
            startTime = startTime,
            endTime = endTime,
        )

    fun observeRecentTransactions(limit: Int = 10): Flow<List<RecentTransactionRow>> =
        transactionDao.observeRecentTransactions(limit)

    fun observeTransactionDetail(transactionId: Long): Flow<TransactionDetailRow?> =
        transactionDao.observeTransactionDetail(transactionId)

    fun observeTodayTotal(now: LocalDate = LocalDate.now()): Flow<Long> {
        val range = dayRange(now)
        return transactionDao.observeTodayTotal(range.start, range.end)
    }

    fun observeMonthTotal(now: LocalDate = LocalDate.now()): Flow<Long> {
        val range = monthRange(now)
        return transactionDao.observeMonthTotal(range.start, range.end)
    }

    fun observeMonthCategorySummary(
        now: LocalDate = LocalDate.now(),
    ): Flow<List<CategoryExpenseSummaryRow>> {
        val range = monthRange(now)
        return transactionDao.observeMonthCategorySummary(range.start, range.end)
    }

    fun observeRecentDailyTotals(
        days: Int = 7,
        now: LocalDate = LocalDate.now(),
    ): Flow<List<DailyExpenseTotalRow>> {
        require(days > 0) { "days must be greater than 0" }

        val start = now.minusDays((days - 1).toLong()).atStartOfDay(zoneId).toInstant().toEpochMilli()
        val end = now.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
        return transactionDao.observeRecentDailyTotals(start, end)
    }

    suspend fun insert(transaction: TransactionEntity): Long = transactionDao.insert(transaction)

    suspend fun getTransactionById(transactionId: Long): TransactionEntity? =
        transactionDao.getTransactionById(transactionId)

    suspend fun getAllTransactionsForExport(): List<TransactionExportRow> =
        transactionDao.getAllTransactionsForExport()

    suspend fun update(transaction: TransactionEntity) = transactionDao.update(transaction)

    suspend fun delete(transaction: TransactionEntity) = transactionDao.delete(transaction)

    suspend fun clearAll() = transactionDao.clearAll()

    private fun dayRange(date: LocalDate): TimeRange {
        val start = date.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val end = date.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
        return TimeRange(start = start, end = end)
    }

    private fun monthRange(date: LocalDate): TimeRange {
        val start = date.withDayOfMonth(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
        val end = date.plusMonths(1).withDayOfMonth(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
        return TimeRange(start = start, end = end)
    }
}

private data class TimeRange(
    val start: Long,
    val end: Long,
)
