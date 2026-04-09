package com.example.expensetracker.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.expensetracker.data.entity.PaymentMethodEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentMethodDao {
    @Query(
        """
        SELECT * FROM payment_methods
        WHERE is_archived = 0
        ORDER BY sort_order ASC, id ASC
        """,
    )
    suspend fun getActivePaymentMethods(): List<PaymentMethodEntity>

    @Query(
        """
        SELECT * FROM payment_methods
        WHERE is_archived = 0
        ORDER BY sort_order ASC, id ASC
        """,
    )
    fun observeActivePaymentMethods(): Flow<List<PaymentMethodEntity>>

    @Query("SELECT COUNT(*) FROM payment_methods")
    suspend fun countAll(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(paymentMethods: List<PaymentMethodEntity>)
}
