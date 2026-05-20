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
            t.type AS type,
            t.amount AS amount,
            t.note AS note,
            t.spent_at AS spentAt,
            c.name AS categoryName,
            c.icon AS categoryIcon,
            a.name AS accountName
        FROM transactions t
        INNER JOIN categories c ON c.id = t.category_id
        LEFT JOIN bank_cards a ON a.id = t.bank_card_id
        ORDER BY t.spent_at DESC
        LIMIT :limit
        """,
    )
    fun observeRecentTransactions(limit: Int = 10): Flow<List<RecentTransactionRow>>

    @Query(
        """
        SELECT COALESCE(SUM(amount), 0) FROM transactions
        WHERE type = :type AND spent_at >= :startOfDay AND spent_at < :endOfDay
        """,
    )
    fun observeDayTotalByType(type: Int, startOfDay: Long, endOfDay: Long): Flow<Long>

    @Query(
        """
        SELECT COALESCE(SUM(amount), 0) FROM transactions
        WHERE type = :type AND spent_at >= :startOfMonth AND spent_at < :endOfMonth
        """,
    )
    fun observeMonthTotalByType(type: Int, startOfMonth: Long, endOfMonth: Long): Flow<Long>

    @Query(
        """
        SELECT
            t.category_id AS categoryId,
            c.name AS categoryName,
            COALESCE(SUM(t.amount), 0) AS totalAmount,
            COUNT(t.id) AS transactionCount
        FROM transactions t
        INNER JOIN categories c ON c.id = t.category_id
        WHERE t.type = :type AND t.spent_at >= :startOfMonth AND t.spent_at < :endOfMonth
        GROUP BY t.category_id, c.name
        ORDER BY totalAmount DESC, transactionCount DESC, c.id ASC
        """,
    )
    fun observeMonthCategorySummary(
        type: Int,
        startOfMonth: Long,
        endOfMonth: Long,
    ): Flow<List<CategoryExpenseSummaryRow>>

    @Query(
        """
        SELECT
            date(t.spent_at / 1000, 'unixepoch', 'localtime') AS day,
            COALESCE(SUM(t.amount), 0) AS totalAmount
        FROM transactions t
        WHERE t.type = :type AND t.spent_at >= :startOfPeriod AND t.spent_at < :endOfPeriod
        GROUP BY date(t.spent_at / 1000, 'unixepoch', 'localtime')
        ORDER BY day ASC
        """,
    )
    fun observeRecentDailyTotals(
        type: Int,
        startOfPeriod: Long,
        endOfPeriod: Long,
    ): Flow<List<DailyExpenseTotalRow>>

    @Query(
        """
        SELECT
            t.id AS id,
            t.type AS type,
            t.amount AS amount,
            t.note AS note,
            t.spent_at AS spentAt,
            c.name AS categoryName,
            c.icon AS categoryIcon,
            a.name AS accountName
        FROM transactions t
        INNER JOIN categories c ON c.id = t.category_id
        LEFT JOIN bank_cards a ON a.id = t.bank_card_id
        ORDER BY t.spent_at DESC, t.id DESC
        """,
    )
    fun observeAllTransactions(): Flow<List<RecentTransactionRow>>

    @Query(
        """
        SELECT
            t.id AS id,
            t.type AS type,
            t.amount AS amount,
            t.note AS note,
            t.spent_at AS spentAt,
            c.name AS categoryName,
            c.icon AS categoryIcon,
            a.name AS accountName
        FROM transactions t
        INNER JOIN categories c ON c.id = t.category_id
        LEFT JOIN bank_cards a ON a.id = t.bank_card_id
        WHERE (:type IS NULL OR t.type = :type)
          AND (:categoryId IS NULL OR t.category_id = :categoryId)
          AND (:accountId IS NULL OR t.bank_card_id = :accountId)
          AND (:startTime IS NULL OR t.spent_at >= :startTime)
          AND (:endTime IS NULL OR t.spent_at < :endTime)
          AND (:minAmount IS NULL OR t.amount >= :minAmount)
          AND (:maxAmount IS NULL OR t.amount <= :maxAmount)
          AND (
            :keyword = '' OR
            IFNULL(t.note, '') LIKE '%' || :keyword || '%' OR
            c.name LIKE '%' || :keyword || '%' OR
            IFNULL(a.name, '') LIKE '%' || :keyword || '%'
          )
        ORDER BY t.spent_at DESC, t.id DESC
        """,
    )
    fun observeFilteredTransactions(
        keyword: String,
        type: Int?,
        categoryId: Long?,
        accountId: Long?,
        startTime: Long?,
        endTime: Long?,
        minAmount: Long?,
        maxAmount: Long?,
    ): Flow<List<RecentTransactionRow>>

    @Query(
        """
        SELECT
            t.id AS id,
            t.type AS type,
            t.amount AS amount,
            t.note AS note,
            t.spent_at AS spentAt,
            c.name AS categoryName,
            a.name AS accountName,
            t.created_at AS createdAt,
            t.updated_at AS updatedAt
        FROM transactions t
        INNER JOIN categories c ON c.id = t.category_id
        LEFT JOIN bank_cards a ON a.id = t.bank_card_id
        ORDER BY t.spent_at DESC, t.id DESC
        """,
    )
    suspend fun getAllTransactionsForExport(): List<TransactionExportRow>

    @Query(
        """
        SELECT
            t.id AS id,
            t.type AS type,
            t.amount AS amount,
            t.category_id AS categoryId,
            c.name AS categoryName,
            t.bank_card_id AS accountId,
            a.name AS accountName,
            t.note AS note,
            t.spent_at AS spentAt,
            t.created_at AS createdAt,
            t.updated_at AS updatedAt
        FROM transactions t
        INNER JOIN categories c ON c.id = t.category_id
        LEFT JOIN bank_cards a ON a.id = t.bank_card_id
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

    @Query(
        """
        SELECT COALESCE(SUM(amount), 0) FROM transactions
        WHERE type = 0 AND category_id = :categoryId AND spent_at >= :startOfMonth AND spent_at < :endOfMonth
        """,
    )
    suspend fun getMonthCategoryTotal(categoryId: Long, startOfMonth: Long, endOfMonth: Long): Long

    @Query(
        """
        SELECT COALESCE(SUM(amount), 0) FROM transactions
        WHERE type = 0 AND spent_at >= :startOfMonth AND spent_at < :endOfMonth
        """,
    )
    suspend fun getMonthTotal(startOfMonth: Long, endOfMonth: Long): Long
}
