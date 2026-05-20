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
        type: Int? = null,
        categoryId: Long?,
        accountId: Long? = null,
        startTime: Long?,
        endTime: Long?,
        minAmount: Long? = null,
        maxAmount: Long? = null,
    ): Flow<List<RecentTransactionRow>> =
        transactionDao.observeFilteredTransactions(
            keyword = keyword.trim(),
            type = type,
            categoryId = categoryId,
            accountId = accountId,
            startTime = startTime,
            endTime = endTime,
            minAmount = minAmount,
            maxAmount = maxAmount,
        )

    fun observeRecentTransactions(limit: Int = 10): Flow<List<RecentTransactionRow>> =
        transactionDao.observeRecentTransactions(limit)

    fun observeTransactionDetail(transactionId: Long): Flow<TransactionDetailRow?> =
        transactionDao.observeTransactionDetail(transactionId)

    fun observeTodayExpense(now: LocalDate = LocalDate.now()): Flow<Long> {
        val range = dayRange(now)
        return transactionDao.observeDayTotalByType(TransactionEntity.TYPE_EXPENSE, range.start, range.end)
    }

    fun observeTodayIncome(now: LocalDate = LocalDate.now()): Flow<Long> {
        val range = dayRange(now)
        return transactionDao.observeDayTotalByType(TransactionEntity.TYPE_INCOME, range.start, range.end)
    }

    fun observeMonthExpense(now: LocalDate = LocalDate.now()): Flow<Long> {
        val range = monthRange(now)
        return transactionDao.observeMonthTotalByType(TransactionEntity.TYPE_EXPENSE, range.start, range.end)
    }

    fun observeMonthIncome(now: LocalDate = LocalDate.now()): Flow<Long> {
        val range = monthRange(now)
        return transactionDao.observeMonthTotalByType(TransactionEntity.TYPE_INCOME, range.start, range.end)
    }

    fun observeMonthCategorySummary(
        type: Int = TransactionEntity.TYPE_EXPENSE,
        now: LocalDate = LocalDate.now(),
    ): Flow<List<CategoryExpenseSummaryRow>> {
        val range = monthRange(now)
        return transactionDao.observeMonthCategorySummary(type, range.start, range.end)
    }

    fun observeRecentDailyTotals(
        type: Int = TransactionEntity.TYPE_EXPENSE,
        days: Int = 7,
        now: LocalDate = LocalDate.now(),
    ): Flow<List<DailyExpenseTotalRow>> {
        require(days > 0) { "days must be greater than 0" }

        val start = now.minusDays((days - 1).toLong()).atStartOfDay(zoneId).toInstant().toEpochMilli()
        val end = now.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
        return transactionDao.observeRecentDailyTotals(type, start, end)
    }

    suspend fun insert(transaction: TransactionEntity): Long = transactionDao.insert(transaction)

    suspend fun getTransactionById(transactionId: Long): TransactionEntity? =
        transactionDao.getTransactionById(transactionId)

    suspend fun getAllTransactionsForExport(): List<TransactionExportRow> =
        transactionDao.getAllTransactionsForExport()

    suspend fun update(transaction: TransactionEntity) = transactionDao.update(transaction)

    suspend fun delete(transaction: TransactionEntity) = transactionDao.delete(transaction)

    suspend fun clearAll() = transactionDao.clearAll()

    suspend fun getMonthCategoryTotal(categoryId: Long, month: LocalDate = LocalDate.now()): Long {
        val range = monthRange(month)
        return transactionDao.getMonthCategoryTotal(categoryId, range.start, range.end)
    }

    suspend fun getMonthTotal(month: LocalDate = LocalDate.now()): Long {
        val range = monthRange(month)
        return transactionDao.getMonthTotal(range.start, range.end)
    }

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
