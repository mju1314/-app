package com.example.expensetracker.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.expensetracker.data.entity.TransactionEntity
import com.example.expensetracker.data.model.CategoryExpenseSummaryRow
import com.example.expensetracker.data.model.DailyExpenseTotalRow
import com.example.expensetracker.data.model.RecentTransactionRow
import com.example.expensetracker.data.model.TransactionExportRow
import com.example.expensetracker.data.model.TransactionDetailRow
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity): Long

    @Query(
        """
        SELECT
            t.id AS id,
            t.amount AS amount,
            t.note AS note,
            t.spent_at AS spentAt,
            c.name AS categoryName,
            p.name AS paymentMethodName
        FROM transactions t
        INNER JOIN categories c ON c.id = t.category_id
        INNER JOIN payment_methods p ON p.id = t.payment_method_id
        ORDER BY t.spent_at DESC
        LIMIT :limit
        """,
    )
    fun observeRecentTransactions(limit: Int = 10): Flow<List<RecentTransactionRow>>

    @Query(
        """
        SELECT COALESCE(SUM(amount), 0) FROM transactions
        WHERE spent_at >= :startOfDay AND spent_at < :endOfDay
        """,
    )
    fun observeTodayTotal(startOfDay: Long, endOfDay: Long): Flow<Long>

    @Query(
        """
        SELECT COALESCE(SUM(amount), 0) FROM transactions
        WHERE spent_at >= :startOfMonth AND spent_at < :endOfMonth
        """,
    )
    fun observeMonthTotal(startOfMonth: Long, endOfMonth: Long): Flow<Long>

    @Query(
        """
        SELECT
            c.name AS categoryName,
            COALESCE(SUM(t.amount), 0) AS totalAmount,
            COUNT(t.id) AS transactionCount
        FROM transactions t
        INNER JOIN categories c ON c.id = t.category_id
        WHERE t.spent_at >= :startOfMonth AND t.spent_at < :endOfMonth
        GROUP BY t.category_id, c.name
        ORDER BY totalAmount DESC, transactionCount DESC, c.id ASC
        """,
    )
    fun observeMonthCategorySummary(
        startOfMonth: Long,
        endOfMonth: Long,
    ): Flow<List<CategoryExpenseSummaryRow>>

    @Query(
        """
        SELECT
            date(t.spent_at / 1000, 'unixepoch', 'localtime') AS day,
            COALESCE(SUM(t.amount), 0) AS totalAmount
        FROM transactions t
        WHERE t.spent_at >= :startOfPeriod AND t.spent_at < :endOfPeriod
        GROUP BY date(t.spent_at / 1000, 'unixepoch', 'localtime')
        ORDER BY day ASC
        """,
    )
    fun observeRecentDailyTotals(
        startOfPeriod: Long,
        endOfPeriod: Long,
    ): Flow<List<DailyExpenseTotalRow>>

    @Query(
        """
        SELECT
            t.id AS id,
            t.amount AS amount,
            t.note AS note,
            t.spent_at AS spentAt,
            c.name AS categoryName,
            p.name AS paymentMethodName
        FROM transactions t
        INNER JOIN categories c ON c.id = t.category_id
        INNER JOIN payment_methods p ON p.id = t.payment_method_id
        ORDER BY t.spent_at DESC, t.id DESC
        """,
    )
    fun observeAllTransactions(): Flow<List<RecentTransactionRow>>

    @Query(
        """
        SELECT
            t.id AS id,
            t.amount AS amount,
            t.note AS note,
            t.spent_at AS spentAt,
            c.name AS categoryName,
            p.name AS paymentMethodName
        FROM transactions t
        INNER JOIN categories c ON c.id = t.category_id
        INNER JOIN payment_methods p ON p.id = t.payment_method_id
        WHERE (:categoryId IS NULL OR t.category_id = :categoryId)
          AND (:startTime IS NULL OR t.spent_at >= :startTime)
          AND (:endTime IS NULL OR t.spent_at < :endTime)
          AND (
            :keyword = '' OR
            IFNULL(t.note, '') LIKE '%' || :keyword || '%' OR
            c.name LIKE '%' || :keyword || '%' OR
            p.name LIKE '%' || :keyword || '%'
          )
        ORDER BY t.spent_at DESC, t.id DESC
        """,
    )
    fun observeFilteredTransactions(
        keyword: String,
        categoryId: Long?,
        startTime: Long?,
        endTime: Long?,
    ): Flow<List<RecentTransactionRow>>

    @Query(
        """
        SELECT
            t.id AS id,
            t.amount AS amount,
            t.note AS note,
            t.spent_at AS spentAt,
            c.name AS categoryName,
            p.name AS paymentMethodName,
            t.created_at AS createdAt,
            t.updated_at AS updatedAt
        FROM transactions t
        INNER JOIN categories c ON c.id = t.category_id
        INNER JOIN payment_methods p ON p.id = t.payment_method_id
        ORDER BY t.spent_at DESC, t.id DESC
        """,
    )
    suspend fun getAllTransactionsForExport(): List<TransactionExportRow>

    @Query(
        """
        SELECT
            t.id AS id,
            t.amount AS amount,
            t.category_id AS categoryId,
            c.name AS categoryName,
            t.payment_method_id AS paymentMethodId,
            p.name AS paymentMethodName,
            t.note AS note,
            t.spent_at AS spentAt,
            t.created_at AS createdAt,
            t.updated_at AS updatedAt
        FROM transactions t
        INNER JOIN categories c ON c.id = t.category_id
        INNER JOIN payment_methods p ON p.id = t.payment_method_id
        WHERE t.id = :transactionId
        LIMIT 1
        """,
    )
    fun observeTransactionDetail(transactionId: Long): Flow<TransactionDetailRow?>

    @Query("SELECT * FROM transactions WHERE id = :transactionId LIMIT 1")
    suspend fun getTransactionById(transactionId: Long): TransactionEntity?

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Delete
    suspend fun delete(transaction: TransactionEntity)

    @Query("DELETE FROM transactions")
    suspend fun clearAll()
}
