package com.example.expensetracker.data.repository

import com.example.expensetracker.data.dao.PaymentMethodDao
import com.example.expensetracker.data.entity.PaymentMethodEntity
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class PaymentMethodRepository @Inject constructor(
    private val paymentMethodDao: PaymentMethodDao,
) {
    suspend fun getActivePaymentMethods(): List<PaymentMethodEntity> = paymentMethodDao.getActivePaymentMethods()
    fun observeActivePaymentMethods(): Flow<List<PaymentMethodEntity>> = paymentMethodDao.observeActivePaymentMethods()
    suspend fun countAll(): Int = paymentMethodDao.countAll()
    suspend fun insertAll(items: List<PaymentMethodEntity>) = paymentMethodDao.insertAll(items)
}
