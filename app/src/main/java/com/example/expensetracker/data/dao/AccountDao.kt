package com.example.expensetracker.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.expensetracker.data.entity.AccountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Query("SELECT * FROM bank_cards ORDER BY sort_order ASC, id ASC")
    fun observeAll(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM bank_cards WHERE id = :id")
    suspend fun getById(id: Long): AccountEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(account: AccountEntity): Long

    @Update
    suspend fun update(account: AccountEntity)

    @Delete
    suspend fun delete(account: AccountEntity)

    @Query("UPDATE bank_cards SET balance = balance - :amount, updated_at = :now WHERE id = :id")
    suspend fun deductBalance(id: Long, amount: Long, now: Long)

    @Query("UPDATE bank_cards SET balance = balance + :amount, updated_at = :now WHERE id = :id")
    suspend fun restoreBalance(id: Long, amount: Long, now: Long)

    @Query("SELECT balance FROM bank_cards WHERE id = :id")
    suspend fun getBalanceById(id: Long): Long?

    @Query("SELECT COUNT(*) FROM bank_cards")
    suspend fun countAll(): Int
}
