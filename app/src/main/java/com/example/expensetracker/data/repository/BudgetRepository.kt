package com.example.expensetracker.data.repository

import com.example.expensetracker.data.dao.BudgetDao
import com.example.expensetracker.data.entity.BudgetEntity
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class BudgetRepository @Inject constructor(
    private val budgetDao: BudgetDao,
) {
    fun observeAll(): Flow<List<BudgetEntity>> = budgetDao.observeAll()

    suspend fun getByCategoryId(categoryId: Long?): BudgetEntity? =
        budgetDao.getByCategoryId(categoryId)

    suspend fun upsert(categoryId: Long?, amountInCent: Long) {
        val now = System.currentTimeMillis()
        val existing = budgetDao.getByCategoryId(categoryId)
        if (existing != null) {
            budgetDao.update(existing.copy(amount = amountInCent, updatedAt = now))
        } else {
            budgetDao.insert(
                BudgetEntity(
                    categoryId = categoryId,
                    amount = amountInCent,
                    createdAt = now,
                    updatedAt = now,
                ),
            )
        }
    }

    suspend fun delete(budget: BudgetEntity) = budgetDao.delete(budget)
}
